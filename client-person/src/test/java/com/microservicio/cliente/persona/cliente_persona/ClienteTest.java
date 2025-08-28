package com.microservicio.cliente.persona.cliente_persona;

import com.microservicio.cliente.persona.cliente_persona.entities.Client;
import com.microservicio.cliente.persona.cliente_persona.entities.Person;
import com.microservicio.cliente.persona.cliente_persona.services.EncryptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClienteTest {

    private Client client;

    @Mock
    private EncryptService encryptService;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setCardId("1234567890");
        client.setName("Jose Lema");
        client.setGender("Masculino");
        client.setAge(30);
        client.setAddress("Otavalo sn y principal");
        client.setPhone("098254785");
        client.setPassword("pass1234");
        client.setEmail("jose.lema@email.com");
        client.setState("true");
    }

    @Test
    @DisplayName("Debe validar herencia de Person")
    void validateInheritanceOfPerson() {
        // Assert
        assertTrue(client instanceof Person);
        assertNotNull(client.getCardId());
        assertNotNull(client.getName());
    }

    @Test
    @DisplayName("Debe permitir modificar contraseña")
    void validateAllowsChangeYourPassword() {
        // Arrange
        String passwordOriginal = "nuevaPassword123";
        String passwordEncriptado = "encrypted_nuevaPassword123";

        when(encryptService.encryptPassword(passwordOriginal)).thenReturn(passwordEncriptado);

        // Act
        String nuevaContraseña = encryptService.encryptPassword(passwordOriginal);
        client.setPassword(nuevaContraseña);
        // Assert
        assertEquals(passwordEncriptado, client.getPassword());
    }

    @Test
    @DisplayName("Debe manejar valores nulos correctamente")
    void validateClientCorrectNullValues() {
        // Arrange
        Client clienteVacio = new Client();

        // Act & Assert
        assertNull(clienteVacio.getName());
        assertNull(clienteVacio.getEmail());
        assertNull(clienteVacio.getPassword());
        assertNull(clienteVacio.getState());
        assertEquals(0, clienteVacio.getAge());
    }
}