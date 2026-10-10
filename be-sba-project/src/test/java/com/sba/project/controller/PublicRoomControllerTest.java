package com.sba.project.controller;

import com.sba.project.dto.request.RoomSearchRequest;
import com.sba.project.dto.response.PublicRoomResponse;
import com.sba.project.exception.BusinessException;
import com.sba.project.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @Test
    void search_returns200AndPage() throws Exception {
        when(roomService.searchPublicRooms(any(RoomSearchRequest.class), any()))
                .thenReturn(new PageImpl<>(List.of(response(UUID.randomUUID()))));

        mockMvc.perform(get("/api/v1/public/rooms").param("address", "Main St")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].branchName").value("Central"));
    }

    @Test
    void search_invalidSort_returns400() throws Exception {
        when(roomService.searchPublicRooms(any(RoomSearchRequest.class), any()))
                .thenThrow(new BusinessException("unsupported sort"));

        mockMvc.perform(get("/api/v1/public/rooms").param("sort", "managerId,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void detail_unknownId_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(roomService.getPublicRoomById(id)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/v1/public/rooms/{roomId}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void detail_jsonHasNoManagerId() throws Exception {
        UUID id = UUID.randomUUID();
        when(roomService.getPublicRoomById(id)).thenReturn(response(id));

        mockMvc.perform(get("/api/v1/public/rooms/{roomId}", id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managerId").doesNotExist())
                .andExpect(jsonPath("$.tenant").doesNotExist())
                .andExpect(jsonPath("$.contract").doesNotExist())
                .andExpect(jsonPath("$.debt").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("managerId"))));
    }

    private PublicRoomResponse response(UUID id) {
        return PublicRoomResponse.builder().roomId(id).branchId(UUID.randomUUID()).branchName("Central")
                .address("Main St").roomTypeId(UUID.randomUUID()).typeName("Studio")
                .roomCode("A-01").referencePrice(new BigDecimal("100.00"))
                .capacity(2).status("AVAILABLE").description("Bright").build();
    }
}
