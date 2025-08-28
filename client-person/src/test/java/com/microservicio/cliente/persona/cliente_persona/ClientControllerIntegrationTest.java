package com.microservicio.cliente.persona.cliente_persona;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservicio.cliente.persona.cliente_persona.controllers.ClientController;
import com.microservicio.cliente.persona.cliente_persona.models.ClientDTO;
import com.microservicio.cliente.persona.cliente_persona.models.ClientInputDTO;
import com.microservicio.cliente.persona.cliente_persona.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class ClientControllerIntegrationTest {
    @Mock
    private ClientService clientService;
    @InjectMocks
    private ClientController clientController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetAllClients_Success() throws Exception {
        // Given
        List<ClientDTO> clients = Arrays.asList(createClientDTO());
        when(clientService.getAllClients()).thenReturn(clients);
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isAccepted());
        verify(clientService, times(1)).getAllClients();
    }

    @Test
    void testSaveClient_Success() throws Exception {
        // Given
        ClientInputDTO clientInputDTO = createClientInputDTO();
        ClientDTO savedClient = createClientDTO();
        when(clientService.saveClient(any(ClientInputDTO.class))).thenReturn(savedClient);
        // When & Then
        mockMvc.perform(post("/api/clients/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientInputDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Registered customer"))
                .andExpect(jsonPath("$.data").exists());
        verify(clientService, times(1)).saveClient(any(ClientInputDTO.class));
    }

    private ClientDTO createClientDTO() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setClientId(5L);
        clientDTO.setName("John Doe");
        clientDTO.setCardId("1234567890");
        clientDTO.setAddress("123 Main St");
        clientDTO.setPhone("555-1234");
        return clientDTO;
    }

    private ClientInputDTO createClientInputDTO() {
        ClientInputDTO clientInputDTO = new ClientInputDTO();
        clientInputDTO.setName("John Doe");
        clientInputDTO.setGender("M");
        clientInputDTO.setAge(30);
        clientInputDTO.setCardId("1234567890");
        clientInputDTO.setAddress("123 Main St");
        clientInputDTO.setPhone("555-1234");
        clientInputDTO.setPassword("password123");
        clientInputDTO.setState("true");
        return clientInputDTO;
    }
}
