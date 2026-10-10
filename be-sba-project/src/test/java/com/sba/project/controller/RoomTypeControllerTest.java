package com.sba.project.controller;

import com.sba.project.dto.response.RoomTypeResponse;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @Test
    void post_valid_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        when(roomService.createRoomType(any())).thenReturn(response(id));

        mockMvc.perform(post("/api/v1/room-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"typeName":"Studio","defaultCapacity":2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomTypeId").value(id.toString()));
    }

    @Test
    void post_blankTypeName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/room-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"typeName":" ","defaultCapacity":2}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_nonPositiveDefaultCapacity_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/room-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"typeName":"Studio","defaultCapacity":0}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_unknownId_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(roomService.getRoomTypeById(id)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/v1/room-types/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getListAndPut_delegateToService() throws Exception {
        UUID id = UUID.randomUUID();
        when(roomService.listRoomTypes(any())).thenReturn(new PageImpl<>(List.of(response(id))));
        when(roomService.updateRoomType(any(), any())).thenReturn(response(id));

        mockMvc.perform(get("/api/v1/room-types?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].typeName").value("Studio"));
        mockMvc.perform(put("/api/v1/room-types/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"typeName":"Studio","defaultCapacity":2}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(roomService).deleteRoomType(id);

        mockMvc.perform(delete("/api/v1/room-types/{id}", id))
                .andExpect(status().isNoContent());
    }

    private RoomTypeResponse response(UUID id) {
        return RoomTypeResponse.builder().roomTypeId(id).typeName("Studio").defaultCapacity(2).build();
    }
}
