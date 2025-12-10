package com.tripjoy.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.service.impl.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserCreationRequest request;

    private UserResponse response;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void initData(){
        request = UserCreationRequest.builder()
                .username("testuser")
                .password("Test@1234")
                .email("testuser@example.com")
                .fullName("Test User")
                .build();

        UUID uuid = UUID.randomUUID();

        response = UserResponse.builder()
                .id(uuid)
                .username("testuser")
                .email("testuser@example.com")
                .fullName("Test User")
                .build();
    }

    @Test
    public void createUser_validRequest_success() throws Exception {
        // GIVEN:
        ObjectMapper mapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(request);

        Mockito.when(userService.createUser(ArgumentMatchers.any())).thenReturn(response);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value("1000")
        );
    }

    @Test
    public void createUser_invalidUsername_fail() throws Exception {
        // GIVEN:
        request.setUsername("joh"); // Invalid username (too short)
        ObjectMapper mapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(request);

        Mockito.when(userService.createUser(ArgumentMatchers.any())).thenReturn(response);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value("1000")
                );
    }
}
