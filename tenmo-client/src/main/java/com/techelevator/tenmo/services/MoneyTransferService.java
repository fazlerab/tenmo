package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.MoneyTransfer;
import com.techelevator.tenmo.model.TransferDetail;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

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

    public boolean send(MoneyTransfer sendMoney) {
        Boolean success = false;
        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(baseUrl + "send",
                    HttpMethod.POST, makeMoneyTransferEntity(sendMoney), Boolean.class);
            success = response.getBody();
        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success != null && success;
    }

    public boolean request(MoneyTransfer sendMoney) {
        Boolean success = false;
        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(baseUrl + "request",
                    HttpMethod.POST, makeMoneyTransferEntity(sendMoney), Boolean.class);
            success = response.getBody();
        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success;
    }

    public TransferDetail[] getPendingTransferDetails(Long userId) {
        TransferDetail[] transfers  = null;
        try {
            ResponseEntity<TransferDetail[]> response = restTemplate.exchange(baseUrl + "pendings/" + userId,
                    HttpMethod.GET, makeEntity(), TransferDetail[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }
    
    public boolean approveTransfer(TransferDetail transfer) {
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

    public boolean rejectTransfer(TransferDetail transfer) {
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

    public TransferDetail[] getTransferDetails(Long userId) {
        TransferDetail[] transfers  = null;
        try {
            ResponseEntity<TransferDetail[]> response = restTemplate.exchange(baseUrl + "transfers/" + userId,
                    HttpMethod.GET, makeEntity(), TransferDetail[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public Account getAccountByUserId(long userId){
        Account account = null;
        try{
            ResponseEntity<Account> response =
                    restTemplate.exchange(this.baseUrl + "users/" + userId + "/account",
                            HttpMethod.GET, makeEntity(), Account.class);
            account = response.getBody();
        }catch (RestClientResponseException | ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }
        return account;
    }

    private HttpEntity<Void> makeEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<MoneyTransfer> makeMoneyTransferEntity(MoneyTransfer moneyTransfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(moneyTransfer, headers);
    }

    private HttpEntity<TransferDetail> makeTransferEntity(TransferDetail transferDetail) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(transferDetail, headers);
    }
}
