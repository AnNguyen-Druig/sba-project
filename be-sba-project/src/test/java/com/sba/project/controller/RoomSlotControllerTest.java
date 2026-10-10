package com.sba.project.controller;

import com.sba.project.dto.response.RoomSlotResponse;
import com.sba.project.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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

@WebMvcTest(RoomSlotController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @Test
    void post_valid_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        when(roomService.createRoomSlot(any())).thenReturn(response(id));

        mockMvc.perform(post("/api/v1/room-slots").contentType(MediaType.APPLICATION_JSON).content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomSlotId").value(id.toString()));
    }

    @Test
    void post_blankSlotCode_returns400() throws Exception {
        UUID roomId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/room-slots").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roomId":"%s","slotCode":" ","slotName":"Bed","status":"AVAILABLE"}
                                """.formatted(roomId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(roomService.getRoomSlotById(id)).thenReturn(response(id));

        mockMvc.perform(get("/api/v1/room-slots/{id}", id)).andExpect(status().isOk());
    }

    @Test
    void listByRoom_returns200() throws Exception {
        UUID roomId = UUID.randomUUID();
        when(roomService.listRoomSlotsByRoom(roomId)).thenReturn(List.of(response(roomId)));

        mockMvc.perform(get("/api/v1/rooms/{roomId}/slots", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slotCode").value("B1"));
    }

    @Test
    void put_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(roomService.updateRoomSlot(any(), any())).thenReturn(response(id));

        mockMvc.perform(put("/api/v1/room-slots/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(validRequest()))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(roomService).deleteRoomSlot(id);

        mockMvc.perform(delete("/api/v1/room-slots/{id}", id)).andExpect(status().isNoContent());
    }

    private String validRequest() {
        return """
                {"roomId":"%s","slotCode":"B1","slotName":"Bed 1","status":"AVAILABLE"}
                """.formatted(UUID.randomUUID());
    }

    private RoomSlotResponse response(UUID id) {
        return RoomSlotResponse.builder().roomSlotId(id).roomId(UUID.randomUUID())
                .slotCode("B1").slotName("Bed 1").status("AVAILABLE").build();
    }
}
