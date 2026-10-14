package com.sba.project.modules.maintenance.controller;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.common.exceptions.GlobalExceptionHandler;
import com.sba.project.modules.maintenance.dto.response.FurnitureResponse;
import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import com.sba.project.modules.maintenance.service.FurnitureService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP contract only: service is mocked, no database or security filter chain. */
class FurnitureControllerTests {
    private static final UUID ROOM = UUID.randomUUID();

    private FurnitureService furnitureService;
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        furnitureService = mock(FurnitureService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new FurnitureController(furnitureService))
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    @AfterEach
    void closeValidator() {
        validator.close();
    }

    @Test
    void createReturns201WithMatchingBodyStatus() throws Exception {
        when(furnitureService.createFurniture(any())).thenReturn(response(FurnitureStatus.IN_USE));

        mvc.perform(post("/api/furnitures").contentType(MediaType.APPLICATION_JSON).content("""
                        {"roomId":"%s","assetCode":"DESK-1","name":"Desk","assetValue":1500000,"status":"IN_USE"}
                        """.formatted(ROOM)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201)).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.assetCode").value("DESK-1"));
    }

    @Test
    void invalidCreateBodyReturns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/furnitures").contentType(MediaType.APPLICATION_JSON).content("""
                        {"roomId":"%s","assetCode":"","name":"Desk","assetValue":-1,"status":"IN_USE"}
                        """.formatted(ROOM)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.data.length()").value(2));
        verifyNoInteractions(furnitureService);
    }

    @Test
    void unknownStatusValueIsMalformedRequest() throws Exception {
        mvc.perform(put("/api/furnitures/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assetCode\":\"DESK-1\",\"name\":\"Desk\",\"status\":\"DELETED\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(1002));
        verifyNoInteractions(furnitureService);
    }

    @Test
    void searchPassesFiltersAndZeroBasedPageable() throws Exception {
        when(furnitureService.searchFurniture(eq(ROOM), eq(FurnitureStatus.DAMAGED), isNull(), any()))
                .thenReturn(PagedResponse.of(List.of(response(FurnitureStatus.DAMAGED)), 2, 5, 6));

        mvc.perform(get("/api/furnitures").param("roomId", ROOM.toString()).param("status", "DAMAGED")
                        .param("page", "2").param("size", "5").param("sortBy", "name").param("direction", "DESC"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.number").value(2))
                .andExpect(jsonPath("$.data.content[0].status").value("DAMAGED"));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(furnitureService).searchFurniture(eq(ROOM), eq(FurnitureStatus.DAMAGED), isNull(), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "name"));
    }

    @Test
    void searchRequiresRoomAndBoundedPageSize() throws Exception {
        mvc.perform(get("/api/furnitures")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/furnitures").param("roomId", ROOM.toString()).param("size", "101"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/furnitures").param("roomId", ROOM.toString()).param("page", "0"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(furnitureService);
    }

    @Test
    void businessErrorsUseCatalogCodes() throws Exception {
        UUID id = UUID.randomUUID();
        when(furnitureService.getFurniture(id)).thenThrow(new AppException(ErrorCode.UNAUTHORIZED));
        mvc.perform(get("/api/furnitures/{id}", id))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value(1101));

        when(furnitureService.updateFurniture(eq(id), any()))
                .thenThrow(new AppException(ErrorCode.FURNITURE_IN_OPEN_REPAIR));
        mvc.perform(put("/api/furnitures/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assetCode\":\"DESK-1\",\"name\":\"Desk\",\"status\":\"RETIRED\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value(1707));
    }

    @Test
    void warrantyRequiresProviderAndDates() throws Exception {
        mvc.perform(post("/api/furnitures/{id}/warranties", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"provider\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.data.length()").value(3));
        verifyNoInteractions(furnitureService);
    }

    private static FurnitureResponse response(FurnitureStatus status) {
        return new FurnitureResponse(UUID.randomUUID(), ROOM, "DESK-1", "Desk", null, null, null, null, null, status);
    }
}
