package com.techelevator.tenmo;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.MoneyTransferService;
import org.apiguardian.api.API;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
		
	}

	private void sendBucks() {
		// TODO Auto-generated method stub
        User[] users = moneyTransferService.getOtherUsers(currentUser.getUser().getId());
        consoleService.printUsers(users);
        int id = consoleService.promptForMenuSelection("Enter ID of user you are sending to (0 to cancel):");
        Long sendUserId = Long.valueOf(id);

        if (validateUserId(users, sendUserId) ) {
            BigDecimal amount =  consoleService.promptForBigDecimal("Enter Amount: ");
            if (validatefAmount(amount)) {

            }

        }
	}

	private void requestBucks() {
		// TODO Auto-generated method stub
		
	}

    private boolean validateUserId(User[] users, Long sendUserId) {
        Set<Long> userIdSet = new HashSet<>();
        for (User u: users) {
            userIdSet.add(u.getId());
        }

        boolean valid = true;
        if (sendUserId == 0) {
            System.out.println("Transcation canceled.");
            valid = false;
        }
        else if (!userIdSet.contains(sendUserId)) {
            consoleService.printErrorMessage("You have selected an invalid user Id.");
            valid = false;
        }
        return valid;
    }

    private boolean validatefAmount(BigDecimal amount) {
        BigDecimal balance = moneyTransferService.getBalanceByUserId(currentUser.getUser().getId());
        BigDecimal zero = new BigDecimal("0.0");

        boolean valid = true;

        if (amount.compareTo(zero) <= 0) {
            System.out.println("Invalid amount.");
            valid = false;
        }
        else if (balance.compareTo(zero) <= 0 || amount.compareTo(balance) > 0) {
            System.out.println("Insufficient fund.");
            valid = false;
        }

        return valid;
    }
}
