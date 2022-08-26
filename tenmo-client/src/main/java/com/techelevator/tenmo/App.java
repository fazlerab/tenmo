package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.MoneyTransferService;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class App {
    private static final int APPROVED = 1;
    private static final int REJECTED = 2;

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final MoneyTransferService moneyTransferService = new MoneyTransferService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        } else {
            moneyTransferService.setAuthToken(currentUser.getToken());
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
        BigDecimal balance = moneyTransferService.getBalanceByUserId(currentUser.getUser().getId());
		System.out.println("Your current balance is: $" + balance.toString());
	}

	private void viewTransferHistory() {
		// TODO Auto-generated method stub
        TransferDetail[] transferDetails = moneyTransferService.getTransferDetails(currentUser.getUser().getId());
        consoleService.printTransfers(transferDetails, currentUser.getUser());
        Long transferId = consoleService.promptForLong("Please enter transfer ID to view details (0 to cancel): ");

        if (transferId == 0) {
            System.out.println("Transaction canceled.");
            return;
        }

        if (!isTransferIdValid(transferDetails, transferId)){
            return;
        }

        for(TransferDetail t : transferDetails){
            if (t.getId().equals(transferId)){
                consoleService.printTransferDetail(t);
            }
        }
		
	}

	private void viewPendingRequests() {
        TransferDetail[] transferDetails = moneyTransferService.getPendingTransferDetails(currentUser.getUser().getId());
        consoleService.printPendingTransfers(transferDetails);
        Long transferId = consoleService.promptForLong("Please enter transfer ID to approve/reject (0 to cancel): ");
        if (transferId == 0) {
            System.out.println("Action canceled.");
            return;
        }

        TransferDetail transfer = null;
        for(TransferDetail t : transferDetails) {
            if (t.getId().equals(transferId)) {
                transfer = t;
                break;
            }
        }

        consoleService.printApproveRejectMenu();
        int action = consoleService.promptForInt("Please choose an option: ");
        if (action == APPROVED) {
            BigDecimal balance = moneyTransferService.getBalanceByUserId(currentUser.getUser().getId());
            BigDecimal amount = transfer.getAmount();

            if (amount.compareTo(balance) > 0) {
                System.out.println("You do not have enough balance to approve transfer.");
                return;
            }

            boolean success = moneyTransferService.approveTransfer(transfer);
            if (success) {
                System.out.println("Transfer approved.");
            }
            else {
                System.out.println("Failed to approve transfer");
            }
        }
        else if (action == REJECTED)  {
            boolean success = moneyTransferService.rejectTransfer(transfer);
            if (success) {
                System.out.println("Transfer rejected.");
            }
            else {
                System.out.println("Failed to reject transfer");
            }
        }
        else {
            System.out.println("Action Canceled.");
        }
	}

	private void sendBucks() {
        User[] users = moneyTransferService.getOtherUsers(currentUser.getUser().getId());
        consoleService.printUsers(users);

        int choice = consoleService.promptForMenuSelection("Enter ID of user you are sending to (0 to cancel):");
        if (choice == 0) {
            System.out.println("Transaction canceled.");
            return;
        }
        Long sendToUserId = Long.valueOf(choice);
        if (!isUserIdValid(users, sendToUserId)) {
            return;
        }

        BigDecimal amount =  consoleService.promptForBigDecimal("Enter Amount: ");
        BigDecimal balance = moneyTransferService.getBalanceByUserId(currentUser.getUser().getId());
        if (!isAmountValid(amount, balance)) {
            return;
        }

        MoneyTransfer sendMoney = new MoneyTransfer(currentUser.getUser().getId(), sendToUserId, amount);
        boolean success = moneyTransferService.send(sendMoney);
        if (success) {
            System.out.println("Money transferred successfully.");
        }
        else {
            System.out.println("Money transfer failed.");
        }
	}

	private void requestBucks() {
        User[] users = moneyTransferService.getOtherUsers(currentUser.getUser().getId());
        consoleService.printUsers(users);

        Long currentUserId = currentUser.getUser().getId();

		Long transferUserId = consoleService.promptForLong("Enter Id of user you are requesting from (0 to cancel): ");
        if (transferUserId.equals(currentUser.getUser().getId())){
            System.out.println("You cannot select to transfer money from yourself.");
        }
        if (transferUserId != 0){
            BigDecimal amount = consoleService.promptForBigDecimal("Enter amount: ");

//            Long accountFrom = moneyTransferService.getAccountByUserId(transferUserId).getAccount_id();
//            Long accountTo = moneyTransferService.getAccountByUserId(currentUserId).getAccount_id();

            MoneyTransfer requestMoney = new MoneyTransfer(transferUserId, currentUserId, amount);
            boolean success = moneyTransferService.request(requestMoney);
            if (success) {
                System.out.println("Request made successfully.");
            }
            else {
                System.out.println("Request failed.");
            }
        }
	}

    private boolean isUserIdValid(User[] users, Long sendUserId) {
        Set<Long> userIdSet = new HashSet<>();
        for (User u: users) {
            userIdSet.add(u.getId());
        }

        if (!userIdSet.contains(sendUserId)) {
            consoleService.printErrorMessage("You have selected an invalid user Id.");
            return false;
        }
        return true;
    }

    private boolean isTransferIdValid(TransferDetail[] transfers, Long transferId) {
        Set<Long> transferIdSet = new HashSet<>();
        for (TransferDetail t: transfers) {
            transferIdSet.add(t.getId());
        }

        if (!transferIdSet.contains(transferId)) {
            consoleService.printErrorMessage("You have selected an invalid transfer Id.");
            return false;
        }
        return true;
    }

    private boolean isAmountValid(BigDecimal amount, BigDecimal balance) {
        BigDecimal zero = new BigDecimal("0.0");
        if (amount.compareTo(zero) <= 0) {
            System.out.println("You have entered invalid amount.");
            return false;
        }

        if (amount.compareTo(balance) > 0) {
            System.out.println("You have insufficient fund.");
            return false;
        }

        return true;
    }
}
