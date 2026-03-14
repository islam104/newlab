package com.recruting.demo.license.service;

import com.recruting.demo.license.dto.ActivateLicenseRequest;
import com.recruting.demo.license.dto.CheckLicenseRequest;
import com.recruting.demo.license.dto.CreateLicenseRequest;
import com.recruting.demo.license.dto.LicenseResponse;
import com.recruting.demo.license.dto.RenewLicenseRequest;
import com.recruting.demo.license.dto.Ticket;
import com.recruting.demo.license.dto.TicketResponse;
import com.recruting.demo.license.entity.Device;
import com.recruting.demo.license.entity.DeviceLicense;
import com.recruting.demo.license.entity.License;
import com.recruting.demo.license.entity.LicenseHistory;
import com.recruting.demo.license.entity.LicenseHistoryStatus;
import com.recruting.demo.license.entity.LicenseType;
import com.recruting.demo.license.entity.Product;
import com.recruting.demo.license.repository.DeviceLicenseRepository;
import com.recruting.demo.license.repository.DeviceRepository;
import com.recruting.demo.license.repository.LicenseHistoryRepository;
import com.recruting.demo.license.repository.LicenseRepository;
import com.recruting.demo.license.repository.LicenseTypeRepository;
import com.recruting.demo.license.repository.ProductRepository;
import com.recruting.demo.security.entity.AppUser;
import com.recruting.demo.security.exception.BadRequestException;
import com.recruting.demo.security.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final ProductRepository productRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final LicenseRepository licenseRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;
    private final AppUserRepository appUserRepository;
    private final TicketSignerService ticketSignerService;

    @Value("${security.ticket.ttl-seconds:300}")
    private long ticketTtlSeconds;

    @Transactional
    public LicenseResponse createLicense(CreateLicenseRequest request, Long adminId) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BadRequestException("Product not found"));
        LicenseType type = licenseTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new BadRequestException("License type not found"));
        AppUser owner = appUserRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new BadRequestException("Owner user not found"));

        String code = generateUniqueCode();

        License license = new License();
        license.setCode(code);
        license.setProduct(product);
        license.setType(type);
        license.setOwner(owner);
        license.setDeviceCount(request.getDeviceCount());
        license.setDescription(request.getDescription());

        License saved = licenseRepository.save(license);

        LicenseHistory history = new LicenseHistory();
        history.setLicense(saved);
        history.setUser(resolveUser(adminId));
        history.setStatus(LicenseHistoryStatus.CREATED);
        history.setChangeDate(LocalDateTime.now());
        history.setDescription("License created");
        licenseHistoryRepository.save(history);

        return toResponse(saved);
    }

    @Transactional
    public TicketResponse activateLicense(ActivateLicenseRequest request, Long userId) {
        AppUser user = resolveUser(userId);
        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new BadRequestException("License not found"));

        if (license.isBlocked()) {
            throw new BadRequestException("License is blocked");
        }

        if (license.getUser() != null && !license.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("License owned by another user");
        }

        Device device = deviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseGet(() -> createDevice(request.getDeviceName(), request.getDeviceMac(), user));
        if (device.getUser() != null && !device.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Device owned by another user");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean firstActivation = license.getUser() == null;

        if (firstActivation) {
            license.setUser(user);
            license.setFirstActivationDate(now);
            license.setEndingDate(now.plusDays(license.getType().getDefaultDurationInDays()));
            licenseRepository.save(license);
        } else {
            long used = deviceLicenseRepository.countByLicenseId(license.getId());
            if (!deviceLicenseRepository.existsByLicenseIdAndDeviceId(license.getId(), device.getId())
                    && used >= license.getDeviceCount()) {
                throw new BadRequestException("Device limit reached");
            }
        }

        if (!deviceLicenseRepository.existsByLicenseIdAndDeviceId(license.getId(), device.getId())) {
            DeviceLicense deviceLicense = new DeviceLicense();
            deviceLicense.setLicense(license);
            deviceLicense.setDevice(device);
            deviceLicense.setActivationDate(now);
            deviceLicenseRepository.save(deviceLicense);
        }

        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(user);
        history.setStatus(LicenseHistoryStatus.ACTIVATED);
        history.setChangeDate(now);
        history.setDescription(firstActivation ? "First activation" : "Activation on device");
        licenseHistoryRepository.save(history);

        return buildTicketResponse(license, device);
    }

    @Transactional(readOnly = true)
    public TicketResponse checkLicense(CheckLicenseRequest request, Long userId) {
        AppUser user = resolveUser(userId);
        Device device = deviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseThrow(() -> new BadRequestException("Device not found"));
        if (!device.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Device owned by another user");
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BadRequestException("Product not found"));

        License license = licenseRepository.findActiveByDeviceUserAndProduct(
                device.getId(), user, product, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("License not found"));

        return buildTicketResponse(license, device);
    }

    @Transactional
    public TicketResponse renewLicense(RenewLicenseRequest request, Long userId) {
        AppUser user = resolveUser(userId);
        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new BadRequestException("License not found"));

        if (license.isBlocked()) {
            throw new BadRequestException("License is blocked");
        }

        if (license.getUser() == null || !license.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("License owned by another user");
        }

        LocalDateTime now = LocalDateTime.now();
        if (license.getEndingDate() != null && license.getEndingDate().isAfter(now.plusDays(7))) {
            throw new BadRequestException("Renewal not allowed yet");
        }

        LocalDateTime base = license.getEndingDate() == null || license.getEndingDate().isBefore(now)
                ? now
                : license.getEndingDate();
        license.setEndingDate(base.plusDays(license.getType().getDefaultDurationInDays()));
        licenseRepository.save(license);

        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(user);
        history.setStatus(LicenseHistoryStatus.RENEWED);
        history.setChangeDate(now);
        history.setDescription("License renewed");
        licenseHistoryRepository.save(history);

        Device device = deviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseThrow(() -> new BadRequestException("Device not found"));
        if (!device.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Device owned by another user");
        }

        return buildTicketResponse(license, device);
    }

    private Device createDevice(String name, String mac, AppUser user) {
        Device device = new Device();
        device.setName(name);
        device.setMacAddress(mac);
        device.setUser(user);
        return deviceRepository.save(device);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "");
        } while (licenseRepository.existsByCode(code));
        return code;
    }

    private LicenseResponse toResponse(License license) {
        return LicenseResponse.builder()
                .id(license.getId())
                .code(license.getCode())
                .productId(license.getProduct().getId())
                .typeId(license.getType().getId())
                .ownerId(license.getOwner().getId())
                .userId(license.getUser() == null ? null : license.getUser().getId())
                .firstActivationDate(license.getFirstActivationDate())
                .endingDate(license.getEndingDate())
                .blocked(license.isBlocked())
                .deviceCount(license.getDeviceCount())
                .description(license.getDescription())
                .build();
    }

    private TicketResponse buildTicketResponse(License license, Device device) {
        Ticket ticket = Ticket.builder()
                .serverDate(Instant.now())
                .ttlSeconds(ticketTtlSeconds)
                .activationDate(license.getFirstActivationDate())
                .endingDate(license.getEndingDate())
                .userId(license.getUser() == null ? null : license.getUser().getId())
                .deviceId(device == null ? null : device.getId())
                .blocked(license.isBlocked())
                .build();

        String signature = ticketSignerService.sign(ticket);
        return TicketResponse.builder()
                .ticket(ticket)
                .signature(signature)
                .build();
    }

    private AppUser resolveUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}
