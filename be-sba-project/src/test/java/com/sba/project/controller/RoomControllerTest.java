package com.sba.project.controller;

import com.sba.project.dto.request.RoomSearchRequest;
import com.sba.project.dto.response.RoomResponse;
import com.sba.project.exception.DuplicateResourceException;
import com.sba.project.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @Test
    void post_valid_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        when(roomService.create(any())).thenReturn(response(id));

        mockMvc.perform(post("/api/v1/rooms").contentType(MediaType.APPLICATION_JSON).content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(id.toString()));
    }

    @Test
    void post_missingBranchId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/rooms").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"managerId":"%s","roomTypeId":"%s","roomCode":"A-01","referencePrice":100,"capacity":2,"status":"AVAILABLE"}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_negativePrice_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/rooms").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"branchId":"%s","managerId":"%s","roomTypeId":"%s","roomCode":"A-01","referencePrice":-1,"capacity":2,"status":"AVAILABLE"}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_duplicateCode_returns409() throws Exception {
        when(roomService.create(any())).thenThrow(new DuplicateResourceException("duplicate"));

        mockMvc.perform(post("/api/v1/rooms").contentType(MediaType.APPLICATION_JSON).content(validRequest()))
                .andExpect(status().isConflict());
    }

    @Test
    void search_bindsQueryParams() throws Exception {
        UUID branchId = UUID.randomUUID();
        when(roomService.search(any(RoomSearchRequest.class), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/rooms").param("branchId", branchId.toString())
                        .param("address", "Main St").param("minPrice", "100").param("status", "AVAILABLE")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());

        var captor = org.mockito.ArgumentCaptor.forClass(RoomSearchRequest.class);
        verify(roomService).search(captor.capture(), any());
        org.junit.jupiter.api.Assertions.assertEquals(branchId, captor.getValue().getBranchId());
        org.junit.jupiter.api.Assertions.assertEquals("Main St", captor.getValue().getAddress());
        org.junit.jupiter.api.Assertions.assertEquals("100", captor.getValue().getMinPrice().toPlainString());
    }

    @Test
    void getPutAndDelete_delegateToService() throws Exception {
        UUID id = UUID.randomUUID();
        when(roomService.getById(id)).thenReturn(response(id));
        when(roomService.update(any(), any())).thenReturn(response(id));
        doNothing().when(roomService).delete(id);

        mockMvc.perform(get("/api/v1/rooms/{id}", id)).andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/rooms/{id}", id).contentType(MediaType.APPLICATION_JSON).content(validRequest()))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/rooms/{id}", id)).andExpect(status().isNoContent());
    }

    private String validRequest() {
        return """
                {"branchId":"%s","managerId":"%s","roomTypeId":"%s","roomCode":"A-01","referencePrice":100,"capacity":2,"status":"AVAILABLE"}
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }

    private RoomResponse response(UUID id) {
        return RoomResponse.builder().roomId(id).roomCode("A-01").referencePrice(new BigDecimal("100"))
                .capacity(2).status("AVAILABLE").build();
    }
}
