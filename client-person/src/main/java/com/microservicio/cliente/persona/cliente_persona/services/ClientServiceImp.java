package com.microservicio.cliente.persona.cliente_persona.services;

import com.microservicio.cliente.persona.cliente_persona.entities.Client;
import com.microservicio.cliente.persona.cliente_persona.exceptions.*;
import com.microservicio.cliente.persona.cliente_persona.models.ClientDTO;
import com.microservicio.cliente.persona.cliente_persona.models.ClientInputDTO;
import com.microservicio.cliente.persona.cliente_persona.repositories.ClientRepository;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;


@Slf4j
@Service
public class ClientServiceImp implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired(required = true)
    private EncryptService encryptService;

    @Autowired
    private ClientProducerService clientProducerService;

    private static final String EMAIL_CONSTRAINT = "UKbfgjs3fem0hmjhvih80158x29";
    private static final String CARD_ID_CONSTRAINT = "UKccxlhn4kvfl9rcx4pprpd47w3";

    @Transactional(readOnly = true)
    public List<ClientDTO> getAllClients() {
        return Optional.of(clientRepository.findAll())
                .filter(clients -> !clients.isEmpty())
                .map(clients -> clients.stream()
                        .map(ClientMapper.INSTANCE::ClientToDto)
                        .toList())
                .orElseThrow(() -> new NoClientsFoundException("No clients were found in the database."));
    }

    @Transactional(readOnly = true)
    public ClientDTO getClientById(@NonNull Long id) {
        return ClientMapper.INSTANCE.ClientToDto(getClientDbById(id));
    }

    @Transactional
    public ClientDTO saveClient(ClientInputDTO client) {
        return executeWithExceptionHandling(() -> {
            Client clientDb = mapInputToEntity(client);
            return ClientMapper.INSTANCE.ClientToDto(clientRepository.save(clientDb));
        });
    }

    @Transactional
    public ClientDTO updateClient(Map<String, Object> actualizaciones, Long id) {
        return executeWithExceptionHandling(() -> {
            Client clientDb = getClientDbById(id);
            updateClientFields(clientDb, actualizaciones);
            return ClientMapper.INSTANCE.ClientToDto(clientRepository.save(clientDb));
        });
    }

    @Transactional
    public ClientDTO deleteClientById(Long id) {
        return executeWithExceptionHandling(() -> {
            Client clientDb = getClientDbById(id);
            clientRepository.deleteById(id);
            return ClientMapper.INSTANCE.ClientToDto(clientDb);
        });
    }

    @Transactional(readOnly = true)
    public String getNameClientById(Long clienteId) {
        return executeWithExceptionHandling(() -> getClientDbById(clienteId).getName());
    }

    @Transactional(readOnly = true)
    public List<Long> getAllIdClients() {
        return clientRepository.findAllIdClients()
                .stream()
                .map(Number::longValue)
                .toList();
    }

    @Transactional
    public ClientDTO saveClientWithAccount(ClientInputDTO client) {
        return executeWithExceptionHandling(() -> {
            Client clientDb = mapInputToEntity(client);
            ClientDTO clientDTO = ClientMapper.INSTANCE.ClientToDto(clientRepository.save(clientDb));
            clientProducerService.sendClientMessage(clientDTO.getClientId());
            return clientDTO;
        });
    }

    private Client getClientDbById(Long id) {
        return clientRepository.findById(id).orElseThrow(() ->
                new ClientNotFoundException("Cliente no encontrado con ID: " + id));
    }

    private Client mapInputToEntity(ClientInputDTO client) {
        Client clientDb = new Client();
        clientDb.setCardId(client.getCardId());
        clientDb.setName(client.getName());
        clientDb.setEmail(client.getEmail());
        clientDb.setAddress(client.getAddress());
        clientDb.setPhone(client.getPhone());
        clientDb.setGender(client.getGender());
        clientDb.setAge(client.getAge());
        clientDb.setState(client.getState());
        clientDb.setPassword(encryptService.encryptPassword(client.getPassword()));
        return clientDb;
    }

    private void updateClientFields(Client clientDb, Map<String, Object> actualizaciones) {
        // Usando Map de funciones para hacer el código más limpio
        Map<String, Consumer<Object>> fieldUpdaters = Map.of(
                "phone", value -> clientDb.setPhone((String) value), // Corregido: era setName
                "address", value -> clientDb.setAddress((String) value),
                "email", value -> clientDb.setEmail((String) value), // Corregido: era setAddress
                "password", value -> clientDb.setPassword(encryptService.encryptPassword((String) value))
        );
        actualizaciones.forEach((key, value) ->
                Optional.ofNullable(fieldUpdaters.get(key))
                        .ifPresent(updater -> updater.accept(value))
        );
    }

    private <T> T executeWithExceptionHandling(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (ConstraintViolationException e) {
            log.error("Constraint violation occurred", e);
            throw new ConstraintViolationException(e.getConstraintViolations());
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation occurred", e);
            handleDataIntegrityViolation(e);
            throw new RuntimeException("Ocurrió un error en el servidor.");
        } catch (ClientNotFoundException e) {
            throw e; // Re-lanzar sin envolver
        } catch (RuntimeException e) {
            log.error("Runtime exception occurred", e);
            throw new InternalServerException(e.getMessage());
        }
    }

    private void handleDataIntegrityViolation(DataIntegrityViolationException e) {
        Optional.ofNullable(e.getCause())
                .map(Throwable::getMessage)
                .filter(message -> message.contains("Duplicate entry"))
                .ifPresent(message -> {
                    if (message.contains(EMAIL_CONSTRAINT)) {
                        throw new DuplicateEmailException("El correo electrónico ya está en uso.");
                    } else if (message.contains(CARD_ID_CONSTRAINT)) {
                        throw new DuplicateCardIdException("El ID de tarjeta ya está en uso.");
                    }
                });
    }
}
