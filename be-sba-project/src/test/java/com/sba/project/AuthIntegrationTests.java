package com.sba.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Checks real JWT login, rotation and revocation with the merged security configuration. */
@SpringBootTest(properties = "spring.config.import=")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTests {
    /** Executes requests through the application security chains. */
    @Autowired
    private MockMvc mvc;
    /** Reads issued tokens from the authentication response. */
    @Autowired
    private ObjectMapper json;

    /** Registration, login, refresh and logout preserve Auth's JSON contract and revoke tokens. */
    @Test
    void authenticationLifecycle() throws Exception {
        String credentials = """
                {"email":"merge-test@example.com","password":"MergeTest_2026"}
                """;
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("USER"));
        var login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var tokens = json.readTree(login).get("data");
        String access = tokens.get("accessToken").asText();
        String refresh = tokens.get("refreshToken").asText();
        mvc.perform(get("/api/v1/rooms").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/rooms").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());

        String refreshBody = "{\"refreshToken\":\"" + refresh + "\"}";
        var rotated = mvc.perform(post("/api/auth/refresh-token").contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        mvc.perform(post("/api/auth/refresh-token").contentType(MediaType.APPLICATION_JSON).content(refreshBody))
                .andExpect(status().isBadRequest());
        var next = json.readTree(rotated).get("data");
        String nextAccess = next.get("accessToken").asText();
        String nextRefreshBody = "{\"refreshToken\":\"" + next.get("refreshToken").asText() + "\"}";
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + nextAccess)
                        .contentType(MediaType.APPLICATION_JSON).content(nextRefreshBody))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/rooms").header("Authorization", "Bearer " + nextAccess))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh-token").contentType(MediaType.APPLICATION_JSON).content(nextRefreshBody))
                .andExpect(status().isBadRequest());
    }

    /** Invalid login is rejected while Swagger and public room search remain accessible. */
    @Test
    void loginFailureAndPublicEndpoints() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing@example.com\",\"password\":\"incorrect\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.success").value(false));
        mvc.perform(get("/api/v1/rooms")).andExpect(status().isUnauthorized());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/public/rooms")).andExpect(status().isOk());
    }
}
