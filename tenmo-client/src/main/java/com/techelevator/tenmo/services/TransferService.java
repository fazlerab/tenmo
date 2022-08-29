package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class TransferService {
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private String authToken;

    public TransferService(String baseUrl) {
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

    public User[] getOtherUsers(Long myUserId) {
        User[] otherUsers = null;
        try {
            ResponseEntity<User[]> response = restTemplate.exchange(baseUrl + "otherUsers/" + myUserId,
                    HttpMethod.GET, makeEntity(), User[].class);
            otherUsers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return otherUsers;
    }

    public boolean send(Transfer sendMoney) {
        Boolean success = false;
        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(baseUrl + "send",
                    HttpMethod.POST, makeTransferEntity(sendMoney), Boolean.class);
            success = response.getBody();
        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success != null && success;
    }

    public boolean request(Transfer requestMoney) {
        Boolean success = false;
        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(baseUrl + "request",
                    HttpMethod.POST, makeTransferEntity(requestMoney), Boolean.class);
            success = response.getBody();
        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success != null && success;
    }

    public Transfer[] getPendingTransferDetails(Long userId) {
        Transfer[] transfers  = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(baseUrl + "pendings/" + userId,
                    HttpMethod.GET, makeEntity(), Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }
    
    public boolean approveTransfer(Transfer transfer) {
        Boolean success = false;
        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(baseUrl + "approve", HttpMethod.PUT,
                    makeTransferEntity(transfer), Boolean.class);
            success = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }

        return success != null && success;
    }

    public boolean rejectTransfer(Transfer transfer) {
        Boolean success = false;
        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(baseUrl + "reject", HttpMethod.PUT,
                    makeTransferEntity(transfer), Boolean.class);
            success = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success != null && success;
    }

    public Transfer[] getTransfer(Long userId) {
        Transfer[] transfers  = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(baseUrl + "transfers/" + userId,
                    HttpMethod.GET, makeEntity(), Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    private HttpEntity<Void> makeEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(transfer, headers);
    }
}
