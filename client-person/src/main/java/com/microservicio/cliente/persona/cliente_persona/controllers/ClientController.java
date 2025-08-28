package com.microservicio.cliente.persona.cliente_persona.controllers;


import com.microservicio.cliente.persona.cliente_persona.models.ClientDTO;
import com.microservicio.cliente.persona.cliente_persona.models.ClientInputDTO;
import com.microservicio.cliente.persona.cliente_persona.models.ResponseVo;
import com.microservicio.cliente.persona.cliente_persona.services.ClientService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/clients")
@Validated
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        return new ResponseEntity<>(clientService.getClientById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<?> getAllClients() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ResponseVo(true, "", clientService.getAllClients()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> saveClient(@Valid @RequestBody ClientInputDTO clientInputDTO, BindingResult result) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseVo(true, "Registered customer", clientService.saveClient(clientInputDTO)));
    }

    @PostMapping("/register-with-account")
    public ResponseEntity<?> saveClientWithAccount(@RequestBody @Valid ClientInputDTO clientInputDTO, BindingResult result) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseVo(true, "Registered customer with account", clientService.saveClientWithAccount(clientInputDTO)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateClientById(@PathVariable Long id,@RequestBody Map<String, Object> actualizaciones, BindingResult result) {
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseVo(true, "Updated Client", clientService.updateClient(actualizaciones, id)));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteClientById(@PathVariable Long id) {
        clientService.deleteClientById(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseVo(true, String.format("Deleted Client : %s", id)));
    }

    @GetMapping("client/{clientId}")
    public ResponseEntity<?> getNameClientById(@PathVariable Long clientId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(clientService.getNameClientById(clientId));
    }

    @GetMapping("client/")
    public List<Long> getAllIdClients() {
        return clientService.getAllIdClients();
    }
}
