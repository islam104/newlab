package com.recruting.demo.license.controller;

import com.recruting.demo.license.dto.ActivateLicenseRequest;
import com.recruting.demo.license.dto.CheckLicenseRequest;
import com.recruting.demo.license.dto.CreateLicenseRequest;
import com.recruting.demo.license.dto.LicenseResponse;
import com.recruting.demo.license.dto.RenewLicenseRequest;
import com.recruting.demo.license.dto.TicketResponse;
import com.recruting.demo.license.service.LicenseService;
import com.recruting.demo.security.entity.AppUser;
import com.recruting.demo.security.exception.BadRequestException;
import com.recruting.demo.security.repository.AppUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/licenses")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;
    private final AppUserRepository appUserRepository;

    @PostMapping("/create")
    public ResponseEntity<LicenseResponse> create(@Valid @RequestBody CreateLicenseRequest request,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.status(201).body(licenseService.createLicense(request, userId));
    }

    @PostMapping("/activate")
    public ResponseEntity<TicketResponse> activate(@Valid @RequestBody ActivateLicenseRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(licenseService.activateLicense(request, userId));
    }

    @PostMapping("/check")
    public ResponseEntity<TicketResponse> check(@Valid @RequestBody CheckLicenseRequest request,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(licenseService.checkLicense(request, userId));
    }

    @PostMapping("/renew")
    public ResponseEntity<TicketResponse> renew(@Valid @RequestBody RenewLicenseRequest request,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(licenseService.renewLicense(request, userId));
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new BadRequestException("User not authenticated");
        }
        AppUser user = appUserRepository.findByUsernameOrEmail(userDetails.getUsername(), userDetails.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));
        return user.getId();
    }
}
