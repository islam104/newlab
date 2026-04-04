package com.recruting.demo.license;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruting.demo.license.dto.TicketResponse;
import com.recruting.demo.license.entity.License;
import com.recruting.demo.license.entity.LicenseType;
import com.recruting.demo.license.entity.Product;
import com.recruting.demo.license.repository.LicenseRepository;
import com.recruting.demo.license.repository.LicenseTypeRepository;
import com.recruting.demo.license.repository.ProductRepository;
import com.recruting.demo.security.entity.AppUser;
import com.recruting.demo.security.repository.AppUserRepository;
import com.recruting.demo.signature.SignatureVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LicenseTicketSignatureApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LicenseTypeRepository licenseTypeRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private SignatureVerificationService signatureVerificationService;

    @Test
    void activateCheckRenewReturnTicketWithValidSignature() throws Exception {
        AppUser owner = createUser("owner");
        AppUser apiUser = createUser("api-user");
        Product product = createProduct();
        LicenseType licenseType = createLicenseType();
        License license = createLicense(owner, product, licenseType);

        String deviceMac = "AA:BB:CC:DD:EE:01";

        TicketResponse activateResponse = performTicketRequest(
                "/api/licenses/activate",
                apiUser.getUsername(),
                Map.of(
                        "activationKey", license.getCode(),
                        "deviceName", "Integration Test Device",
                        "deviceMac", deviceMac
                )
        );
        assertValidSignature(activateResponse);
        assertEquals(apiUser.getId(), activateResponse.getTicket().getUserId());
        assertNotNull(activateResponse.getTicket().getDeviceId());

        TicketResponse checkResponse = performTicketRequest(
                "/api/licenses/check",
                apiUser.getUsername(),
                Map.of(
                        "productId", product.getId(),
                        "deviceMac", deviceMac
                )
        );
        assertValidSignature(checkResponse);
        assertEquals(activateResponse.getTicket().getDeviceId(), checkResponse.getTicket().getDeviceId());

        TicketResponse renewResponse = performTicketRequest(
                "/api/licenses/renew",
                apiUser.getUsername(),
                Map.of(
                        "activationKey", license.getCode(),
                        "deviceMac", deviceMac
                )
        );
        assertValidSignature(renewResponse);
        assertTrue(renewResponse.getTicket().getEndingDate().isAfter(checkResponse.getTicket().getEndingDate()));
    }

    private void assertValidSignature(TicketResponse response) {
        assertNotNull(response.getTicket());
        assertNotNull(response.getSignature());
        assertTrue(signatureVerificationService.verify(response.getTicket(), response.getSignature()));
    }

    private TicketResponse performTicketRequest(String endpoint, String username, Map<String, Object> body) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(endpoint)
                        .with(user(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TicketResponse.class);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialize test payload", ex);
        }
    }

    private AppUser createUser(String prefix) {
        String suffix = Long.toString(System.nanoTime());
        AppUser user = new AppUser();
        user.setUsername(prefix + "-" + suffix);
        user.setEmail(prefix + "-" + suffix + "@test.local");
        user.setPasswordHash("not-used-in-mockmvc-user-postprocessor");
        user.setRole("ROLE_CANDIDATE");
        return appUserRepository.save(user);
    }

    private Product createProduct() {
        Product product = new Product();
        product.setName("Product-" + UUID.randomUUID());
        product.setBlocked(false);
        return productRepository.save(product);
    }

    private LicenseType createLicenseType() {
        LicenseType type = new LicenseType();
        type.setName("Type-" + UUID.randomUUID());
        type.setDefaultDurationInDays(1);
        type.setDescription("Integration test type");
        return licenseTypeRepository.save(type);
    }

    private License createLicense(AppUser owner, Product product, LicenseType type) {
        License license = new License();
        license.setCode(UUID.randomUUID().toString().replace("-", ""));
        license.setOwner(owner);
        license.setProduct(product);
        license.setType(type);
        license.setDeviceCount(2);
        license.setBlocked(false);
        license.setDescription("Integration test license");
        return licenseRepository.save(license);
    }
}
