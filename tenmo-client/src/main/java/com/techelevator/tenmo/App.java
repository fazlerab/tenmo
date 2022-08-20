package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.MoneyTransferService;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class App {

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
		
	}

	private void viewPendingRequests() {
		// TODO Auto-generated method stub
        TransferDetail[] transferDetails = moneyTransferService.getPendingTransferDetails(currentUser.getUser().getId());
        System.out.println("size: " + transferDetails.length);
        consoleService.printTransferBasic(transferDetails);
        int transferId = consoleService.promptForInt("Please enter transfer ID to approve/reject (0 to cancel): \"");

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
		// TODO Auto-generated method stub
		
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
