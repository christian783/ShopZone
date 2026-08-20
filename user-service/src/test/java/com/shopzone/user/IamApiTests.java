package com.shopzone.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopzone.user.auth.TokenHasher;
import com.shopzone.user.config.JwtProperties;
import com.shopzone.user.domain.PasswordResetToken;
import com.shopzone.user.domain.User;
import com.shopzone.user.repository.PasswordResetTokenRepository;
import com.shopzone.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IamApiTests {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    JwtProperties jwtProperties;

    @Test
    void loginRefreshReuseAndRbac() throws Exception {
        JsonNode tokens = login("admin@shopzone.dev", "Admin123!");
        String access = tokens.get("accessToken").asText();
        String refresh1 = tokens.get("refreshToken").asText();

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("admin@shopzone.dev"));

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh1 + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        String refresh2 = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .get("data").get("refreshToken").asText();
        assertThat(refresh2).isNotEqualTo(refresh1);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh1 + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Refresh token reuse detected"));

        String customerEmail = "cust-" + UUID.randomUUID() + "@shopzone.dev";
        MvcResult register = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Secret123","fullName":"Customer One"}
                                """.formatted(customerEmail)))
                .andExpect(status().isCreated())
                .andReturn();
        String customerAccess = objectMapper.readTree(register.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();

        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + customerAccess))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void validationMessagesHonorFrenchLocale() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "fr")
                        .content("{\"email\":\"not-an-email\",\"password\":\"secret12\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Échec de la validation"))
                .andExpect(jsonPath("$.data.errors[0].message").value("L'e-mail n'est pas valide"));
    }

    @Test
    void forgotPasswordDoesNotLeakExistence() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@shopzone.dev\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("If that email is registered, a reset link has been issued"));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@shopzone.dev\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("If that email is registered, a reset link has been issued"));
    }

    @Test
    void resetPasswordRevokesRefreshTokens() throws Exception {
        String email = "reset-" + UUID.randomUUID() + "@shopzone.dev";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Secret123","fullName":"Reset User"}
                                """.formatted(email)))
                .andExpect(status().isCreated());

        User user = userRepository.findWithRolesByEmailIgnoreCaseAndDeletedAtIsNull(email).orElseThrow();
        String plaintext = TokenHasher.randomToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(TokenHasher.sha256(plaintext));
        token.setExpiresAt(Instant.now().plus(jwtProperties.getResetTokenTtl()));
        passwordResetTokenRepository.save(token);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","newPassword":"NewSecret9"}
                                """.formatted(plaintext)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"NewSecret9"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    private JsonNode login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }
}
