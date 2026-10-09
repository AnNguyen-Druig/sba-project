package com.sba.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Do not import developer credentials while running the context smoke test.
@SpringBootTest(properties = "spring.config.import=")
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SbaProjectApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	@WithMockUser
	void swaggerUiResourcesAreAvailable() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/swagger-ui/index.html"));
		mockMvc.perform(get("/swagger-ui/index.html"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Swagger UI")));
		mockMvc.perform(get("/swagger-ui/swagger-ui-bundle.js")).andExpect(status().isOk());
		mockMvc.perform(get("/swagger-ui/swagger-initializer.js"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("/v3/api-docs/swagger-config")));
	}

	@Test
	@WithMockUser
	void openApiDocumentAndUiConfigurationAreAvailable() throws Exception {
		mockMvc.perform(get("/v3/api-docs/swagger-config"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.url").value("/v3/api-docs"));
		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.openapi").exists())
			.andExpect(jsonPath("$.paths").isNotEmpty());
	}

}
