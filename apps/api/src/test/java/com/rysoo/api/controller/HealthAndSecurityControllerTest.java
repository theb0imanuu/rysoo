package com.rysoo.api.controller;

import com.rysoo.api.security.AuthenticatedUser;
import com.rysoo.infrastructure.persistence.repository.ProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthAndSecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    @DisplayName("Public health endpoint returns 200 OK without authentication")
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("UP")))
                .andExpect(jsonPath("$.data.service", is("rysoo-api")));
    }

    @Test
    @DisplayName("Protected endpoint returns 401 Unauthorized when unauthenticated")
    void protectedEndpointRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint returns 200 OK with valid JWT and maps AuthenticatedUser")
    void protectedEndpointSucceedsWithJwt() throws Exception {
        UUID testUserId = UUID.randomUUID();
        String testEmail = "testuser@example.com";

        mockMvc.perform(get("/api/v1/profile")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt
                                .subject(testUserId.toString())
                                .claim("email", testEmail)
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(testUserId.toString())))
                .andExpect(jsonPath("$.data.email", is(testEmail)))
                .andExpect(jsonPath("$.meta.correlationId", notNullValue()));
    }
}
