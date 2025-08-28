package com.microservicio.account.transaction.account_transaction.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ClientApiService {

    private final RestTemplate restTemplate;

    @Autowired
    public ClientApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private static final String CLIENT_PERSON_URL = "http://client-person:8080";
    private static final String PREFIJO_URL ="/api/clients/client/";

    public String getNameClientById(Long clientId) {
        String url = CLIENT_PERSON_URL + PREFIJO_URL + clientId;
        return restTemplate.getForObject(url, String.class);
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getAllIdClients() {
        String url = CLIENT_PERSON_URL + PREFIJO_URL;
        return restTemplate.getForObject(url, List.class);
    }
}
