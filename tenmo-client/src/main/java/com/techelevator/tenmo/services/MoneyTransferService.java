package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

public class MoneyTransferService {
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private String authToken;

    public MoneyTransferService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public BigDecimal getBalanceByUserId(Long id) {
        BigDecimal balance = null;
        try {
            ResponseEntity<BigDecimal> response = restTemplate.exchange(baseUrl + "balance/" + id, HttpMethod.GET,
                    makeEntity(), BigDecimal.class);
            balance = response.getBody();
        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return balance;
    }

    public User[] getOtherUsers(Long myId) {
        User[] otherUsers = null;
        try {
            ResponseEntity<User[]> response = restTemplate.exchange(baseUrl + "otherUsers/" + myId,
                    HttpMethod.GET, makeEntity(), User[].class);
            otherUsers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return otherUsers;
    }

    private HttpEntity<Void> makeEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }
}
