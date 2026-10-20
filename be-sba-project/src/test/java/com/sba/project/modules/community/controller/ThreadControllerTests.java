package com.sba.project.modules.community.controller;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.common.exceptions.GlobalExceptionHandler;
import com.sba.project.modules.community.dto.response.ThreadResponse;
import com.sba.project.modules.community.enums.ThreadStatus;
import com.sba.project.modules.community.service.ThreadService;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ThreadControllerTests {
    private ThreadService service;
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        service = mock(ThreadService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new ThreadController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    @AfterEach
    void closeValidator() {
        validator.close();
    }

    @Test
    void createReturns201() throws Exception {
        when(service.create(any())).thenReturn(response());

        mvc.perform(post("/api/threads").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Room\",\"description\":\"Near campus\",\"criteria\":{\"capacity\":2}}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    void createValidatesTitleDescriptionAndCriteria() throws Exception {
        mvc.perform(post("/api/threads").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \",\"description\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.data.length()").value(3));
        mvc.perform(post("/api/threads").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"a\",\"description\":\"b\",\"criteria\":{\"capacity\":0}}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void anonymousCallsMapToUnauthenticated() throws Exception {
        when(service.browse(any(), any())).thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

        mvc.perform(get("/api/threads")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1100));
    }

    @Test
    void stateAndOwnershipErrorsUseCatalogCodes() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.close(id)).thenThrow(new AppException(ErrorCode.THREAD_STATE_CONFLICT));
        mvc.perform(post("/api/threads/{id}/close", id)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(2001));

        when(service.get(id)).thenThrow(new AppException(ErrorCode.THREAD_NOT_FOUND));
        mvc.perform(get("/api/threads/{id}", id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(2000));
    }

    @Test
    void pageBoundsAreEnforced() throws Exception {
        mvc.perform(get("/api/threads").param("page", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/threads/my").param("size", "101")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    private static ThreadResponse response() {
        return new ThreadResponse(UUID.randomUUID(), UUID.randomUUID(), "Room", "Near campus", null,
                ThreadStatus.OPEN, null, null, null, null);
    }
}
