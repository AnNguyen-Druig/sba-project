package com.sba.project.common.security;

import com.sba.project.modules.identity.repository.InvalidatedTokenRepository;
import com.sba.project.modules.property.controller.PublicRoomController;
import com.sba.project.modules.property.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicRoomController.class,
        properties = {"logging.level.root=ERROR", "spring.main.banner-mode=off"})
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
class PublicRoomSecurityTests {
    @Autowired private MockMvc mvc;
    @MockitoBean private RoomService service;
    @MockitoBean private JwtTokenService tokens;
    @MockitoBean private InvalidatedTokenRepository invalidatedTokens;

    @Test
    void guestCanReadPublicRoomListAndDetail() throws Exception {
        when(service.searchPublicRooms(any(), any())).thenReturn(Page.empty(PageRequest.of(0, 6)));
        mvc.perform(get("/api/v1/public/rooms")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/public/rooms/20000000-0000-4000-8000-000000000001"))
                .andExpect(status().isOk());
    }

    @Test
    void publicReadPermissionDoesNotOpenWritesManagementOrOtherPublicRoutes() throws Exception {
        mvc.perform(post("/api/v1/public/rooms")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/rooms")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/public/rooms/20000000-0000-4000-8000-000000000001/availability"))
                .andExpect(status().isUnauthorized());
    }
}
