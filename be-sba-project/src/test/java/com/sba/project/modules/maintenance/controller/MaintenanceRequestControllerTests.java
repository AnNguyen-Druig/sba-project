package com.sba.project.modules.maintenance.controller;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.common.exceptions.GlobalExceptionHandler;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRecordResponse;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRequestDetailResponse;
import com.sba.project.modules.maintenance.enums.ReportRecordRecordType;
import com.sba.project.modules.maintenance.enums.ReportRequestPriority;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import com.sba.project.modules.maintenance.service.MaintenanceRecordService;
import com.sba.project.modules.maintenance.service.MaintenanceRequestService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP contract only: services are mocked. */
class MaintenanceRequestControllerTests {
    private static final UUID ROOM = UUID.randomUUID();
    private static final UUID ID = UUID.randomUUID();

    private MaintenanceRequestService requestService;
    private MaintenanceRecordService recordService;
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        requestService = mock(MaintenanceRequestService.class);
        recordService = mock(MaintenanceRecordService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new MaintenanceRequestController(requestService, recordService))
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    @AfterEach
    void closeValidator() {
        validator.close();
    }

    @Test
    void createReturns201() throws Exception {
        when(requestService.createRequest(any())).thenReturn(detail());

        mvc.perform(post("/api/maintenance-requests").contentType(MediaType.APPLICATION_JSON).content("""
                        {"roomId":"%s","title":"Broken desk","description":"Loose leg","priority":"HIGH",
                         "furnitureItems":[{"furnitureId":"%s"}]}
                        """.formatted(ROOM, UUID.randomUUID())))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    void createValidatesRequiredFieldsAndEnums() throws Exception {
        mvc.perform(post("/api/maintenance-requests").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\":\"%s\",\"title\":\" \",\"description\":\"\"}".formatted(ROOM)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.data.length()").value(3));
        mvc.perform(post("/api/maintenance-requests").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\":\"%s\",\"title\":\"a\",\"description\":\"b\",\"priority\":\"SOON\"}"
                                .formatted(ROOM)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(1002));
        verifyNoInteractions(requestService);
    }

    @Test
    void actionsAcceptEmptyBodyAndMapErrors() throws Exception {
        when(requestService.accept(eq(ID), any())).thenReturn(detail());
        mvc.perform(post("/api/maintenance-requests/{id}/accept", ID)).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        when(requestService.start(eq(ID), any())).thenThrow(new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT));
        mvc.perform(post("/api/maintenance-requests/{id}/start", ID)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1703));

        when(requestService.reject(eq(ID), any())).thenThrow(new AppException(ErrorCode.UNAUTHORIZED));
        mvc.perform(post("/api/maintenance-requests/{id}/reject", ID)).andExpect(status().isForbidden());

        when(requestService.complete(eq(ID), any())).thenThrow(new AppException(ErrorCode.CONCURRENT_MODIFICATION));
        mvc.perform(post("/api/maintenance-requests/{id}/complete", ID)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1008));
    }

    @Test
    void assignAndEvidenceAndRecordsValidateBodies() throws Exception {
        mvc.perform(post("/api/maintenance-requests/{id}/assign", ID).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/maintenance-requests/{id}/evidence", ID).contentType(MediaType.APPLICATION_JSON)
                .content("{\"publicId\":\"x\",\"resourceType\":\"image\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/maintenance-requests/{id}/records", ID).contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordType\":\"COST\",\"description\":\"p\",\"amount\":-5}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/maintenance-requests/{id}/records/{c}/liabilities", ID, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content("{\"responsibleParty\":\"OWNER\",\"description\":\"d\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(recordService);
    }

    @Test
    void recordCreationReturns201AndDecisionsReturn200() throws Exception {
        MaintenanceRecordResponse record = new MaintenanceRecordResponse(UUID.randomUUID(), ID,
                ReportRecordRecordType.WORK, null, null, null, null, "done", UUID.randomUUID(), null, null, null,
                null, null, null, null);
        when(recordService.addRecord(eq(ID), any())).thenReturn(record);
        when(recordService.approveRecord(eq(ID), any())).thenReturn(record);

        mvc.perform(post("/api/maintenance-requests/{id}/records", ID).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordType\":\"WORK\",\"description\":\"done\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value(201));
        mvc.perform(post("/api/maintenance-requests/{id}/records/{r}/approve", ID, UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    void ratingIsBoundedToOneThroughFive() throws Exception {
        mvc.perform(post("/api/maintenance-requests/{id}/rating", ID).contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":6}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/maintenance-requests/{id}/rating", ID).contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":0}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/maintenance-requests/{id}/rating", ID).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isBadRequest());
        verifyNoInteractions(requestService);

        when(requestService.rate(eq(ID), any())).thenReturn(detail());
        mvc.perform(post("/api/maintenance-requests/{id}/rating", ID).contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":5,\"comment\":\"great\"}")).andExpect(status().isOk());
    }

    @Test
    void costStatsAndOverdueUseTheirQueryParameters() throws Exception {
        when(requestService.costStats(ROOM, null)).thenReturn(List.of());
        mvc.perform(get("/api/maintenance-requests/cost-stats").param("roomId", ROOM.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(0));

        when(requestService.costStats(null, null)).thenThrow(new AppException(ErrorCode.INVALID_REQUEST));
        mvc.perform(get("/api/maintenance-requests/cost-stats")).andExpect(status().isBadRequest());

        when(requestService.searchRoomRequests(eq(ROOM), any(), any(), eq(true), any()))
                .thenReturn(com.sba.project.common.base.PagedResponse.of(List.of(), 1, 20, 0));
        mvc.perform(get("/api/maintenance-requests").param("roomId", ROOM.toString()).param("overdue", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void listsBoundPageSizeAndRequireRoom() throws Exception {
        mvc.perform(get("/api/maintenance-requests")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/maintenance-requests/my").param("size", "101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/maintenance-requests").param("roomId", ROOM.toString()).param("page", "0"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(requestService);
    }

    private static MaintenanceRequestDetailResponse detail() {
        return new MaintenanceRequestDetailResponse(ID, ROOM, UUID.randomUUID(), UUID.randomUUID(), "t", "d",
                ReportRequestPriority.HIGH, ReportRequestStatus.OPEN, null, null, null, null, null, null, null, null, null, 0L, List.of(),
                List.of(), List.of());
    }
}
