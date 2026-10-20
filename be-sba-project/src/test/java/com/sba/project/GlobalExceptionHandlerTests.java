package com.sba.project;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.common.exceptions.GlobalExceptionHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Isolated HTTP contract tests: no database, production endpoints or security filter changes. */
class GlobalExceptionHandlerTests {
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setup() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new ErrorTestController())
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    @AfterEach
    void closeValidator() {
        validator.close();
    }

    @Test
    void catalogHasUniqueErrorCodesAtLeast1000() {
        var codes = Arrays.stream(ErrorCode.values()).map(ErrorCode::getCode).toList();
        assertThat(codes).doesNotHaveDuplicates().allMatch(code -> code >= 1000);
        assertThat(Arrays.stream(ErrorCode.values()).map(ErrorCode::getHttpStatusCode).toList())
                .allMatch(status -> status.isError());
    }

    @Test
    void successRetainsExistingFieldsAndUsesCodeZero() throws Exception {
        mvc.perform(get("/test/success")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200)).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("Success")).andExpect(jsonPath("$.data").value("ok"));
        assertThat(ApiResponse.success("Created", "data").getMessage()).isEqualTo("Created");
    }

    @Test
    void businessErrorReturnsCatalogCodeStatusAndSafeMessage() throws Exception {
        var result = mvc.perform(get("/test/business")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.code").value(1202))
                .andExpect(jsonPath("$.message").value("Room not found")).andReturn();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("private-detail");
    }

    @Test
    void namedAndDefaultValidationMessagesBothProduceSafe400() throws Exception {
        mvc.perform(post("/test/validate").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(1117))
                .andExpect(jsonPath("$.data[0].field").value("name"));
        mvc.perform(post("/test/default-validation").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(1001));
        mvc.perform(post("/test/invalid-key").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void malformedJsonAndParameterErrorsDoNotBecome500() throws Exception {
        mvc.perform(post("/test/validate").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(1002));
        mvc.perform(get("/test/parameter"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(1001));
        mvc.perform(get("/test/parameter").param("id", "not-a-uuid"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void methodAndMediaErrorsRetainHttpStatusAndAllowHeader() throws Exception {
        mvc.perform(put("/test/success")).andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow")).andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.code").value(1004));
        mvc.perform(post("/test/validate").contentType(MediaType.TEXT_PLAIN).content("text"))
                .andExpect(status().isUnsupportedMediaType()).andExpect(jsonPath("$.code").value(1006));
    }

    @Test
    void databaseAndUnexpectedErrorsNeverExposeInternalDetails() throws Exception {
        var conflict = mvc.perform(get("/test/database")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409)).andExpect(jsonPath("$.code").value(1007)).andReturn();
        var unexpected = mvc.perform(get("/test/unexpected")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500)).andExpect(jsonPath("$.code").value(1000)).andReturn();
        assertThat(conflict.getResponse().getContentAsString()).doesNotContain("secret", "rental.users", "password_hash");
        assertThat(unexpected.getResponse().getContentAsString()).doesNotContain("secret", "SELECT");
    }

    @Test
    void authorizationAndConcurrencyHaveDistinctCodes() throws Exception {
        mvc.perform(get("/test/denied")).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403)).andExpect(jsonPath("$.code").value(1101));
        mvc.perform(get("/test/concurrent")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1008));
    }

    @RestController
    static class ErrorTestController {
        @GetMapping("/test/success") ApiResponse<String> success() { return ApiResponse.success("ok"); }
        @GetMapping("/test/business") void business() { throw new AppException(ErrorCode.ROOM_NOT_FOUND, "private-detail"); }
        @GetMapping("/test/database") void database() { throw new DataIntegrityViolationException("rental.users password_hash=secret"); }
        @GetMapping("/test/unexpected") void unexpected() { throw new IllegalStateException("SELECT secret"); }
        @GetMapping("/test/denied") void denied() { throw new AccessDeniedException("internal scope"); }
        @GetMapping("/test/concurrent") void concurrent() { throw new OptimisticLockingFailureException("stale record"); }
        @GetMapping("/test/parameter") void parameter(@RequestParam("id") UUID id) { }
        @PostMapping("/test/validate") void validate(@Valid @RequestBody NamedRequest body) { }
        @PostMapping("/test/default-validation") void defaultValidation(@Valid @RequestBody DefaultRequest body) { }
        @PostMapping("/test/invalid-key") void invalidKey(@Valid @RequestBody InvalidKeyRequest body) { }
    }

    record NamedRequest(@NotBlank(message = "NAME_REQUIRED") String name) { }
    record DefaultRequest(@NotBlank String name) { }
    record InvalidKeyRequest(@NotBlank(message = "INTERNAL_ERROR") String name) { }
}
