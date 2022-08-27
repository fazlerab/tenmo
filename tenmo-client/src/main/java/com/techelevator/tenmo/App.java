package com.techelevator.tenmo;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class App {

    private static final int APPROVED = 1;
    private static final int REJECTED = 2;

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final TransferService transferService = new TransferService(API_BASE_URL);

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
            transferService.setAuthToken(currentUser.getToken());
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
        BigDecimal balance = transferService.getBalanceByUserId(currentUser.getUser().getId());
		System.out.println("Your current balance is: $" + balance.toString());
	}

	private void viewTransferHistory() {
        Transfer[] transfers = transferService.getTransfer(currentUser.getUser().getId());
        consoleService.printTransfers(transfers, currentUser.getUser());
        Long transferId = consoleService.promptForLong("Please enter transfer ID to view details (0 to cancel): ");

        if (transferId == 0) {
            System.out.println("Transaction canceled.");
            return;
        }

        if (!isTransferIdValid(transfers, transferId)){
            return;
        }

        for(Transfer t : transfers){
            if (t.getId().equals(transferId)){
                consoleService.printTransferDetail(t);
            }
        }
		
	}

	private void viewPendingRequests() {
        Transfer[] transfers = transferService.getPendingTransferDetails(currentUser.getUser().getId());
        consoleService.printPendingTransfers(transfers);
        Long transferId = consoleService.promptForLong("Please enter transfer ID to approve/reject (0 to cancel): ");

        if (transferId == 0L) {
            System.out.println("Transfer canceled.");
            return;
        }

        if (!isTransferIdValid(transfers, transferId)) {
            return;
        }

        Transfer transfer = null;
        for(Transfer t : transfers) {
            if (t.getId().equals(transferId)) {
                transfer = t;
                break;
            }
        }

        consoleService.printApproveRejectMenu();
        int action = consoleService.promptForInt("Please choose an option: ");
        if (action == APPROVED) {
            BigDecimal balance = transferService.getBalanceByUserId(currentUser.getUser().getId());
            BigDecimal amount = transfer.getAmount();

            if (amount.compareTo(balance) > 0) {
                System.out.println("You do not have enough balance to approve transfer.");
                return;
            }

            boolean success = transferService.approveTransfer(transfer);
            if (success) {
                System.out.println("Transfer approved.");
            }
            else {
                System.out.println("Failed to approve transfer");
            }
        }
        else if (action == REJECTED)  {
            boolean success = transferService.rejectTransfer(transfer);
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
        User[] users = transferService.getOtherUsers(currentUser.getUser().getId());
        consoleService.printUsers(users);

        Long userId = consoleService.promptForLong("Enter ID of user you are sending to (0 to cancel):");
        if (userId == 0) {
            System.out.println("Transaction canceled.");
            return;
        }

        if (!isUserIdValid(users, userId)) {
            return;
        }

        BigDecimal amount =  consoleService.promptForBigDecimal("Enter Amount: ");
        BigDecimal balance = transferService.getBalanceByUserId(currentUser.getUser().getId());
        if (!isAmountValid(amount, balance)) {
            return;
        }

        Transfer sendMoney = new Transfer(currentUser.getUser(), getUser(users, userId),
                "Send", "Approved", amount);
        boolean success = transferService.send(sendMoney);
        if (success) {
            System.out.println("Money transferred successfully.");
        }
        else {
            System.out.println("Money transfer failed.");
        }
	}

	private void requestBucks() {
        User[] users = transferService.getOtherUsers(currentUser.getUser().getId());
        consoleService.printUsers(users);

		Long fromUserId = consoleService.promptForLong("Enter Id of user you are requesting from (0 to cancel): ");

        if (fromUserId == 0) {
            System.out.println("Transaction canceled.");
            return;
        }

        if (!isUserIdValid(users, fromUserId)) {
            return;
        }

        if (fromUserId.equals(currentUser.getUser().getId())){
            System.out.println("You cannot select to transfer money from yourself.");
        }
        BigDecimal zero = new BigDecimal("0.0");
        BigDecimal amount = consoleService.promptForBigDecimal("Enter amount: ");
        if (amount.compareTo(zero) <= 0) {
            System.out.println("You have entered invalid amount.");
            return;
        }

        Transfer requestMoney = new Transfer(getUser(users, fromUserId), currentUser.getUser(), "Request",
                "Pending", amount);
        boolean success = transferService.request(requestMoney);
        if (success) {
            System.out.println("Request made successfully.");
        }
        else {
            System.out.println("Request failed.");
        }
	}

    private User getUser(User[] users, Long userId) {
        for(User u : users) {
            if (u.getId().equals(userId)) {
                return u;
            }
        }
        return null;
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

    private boolean isTransferIdValid(Transfer[] transfers, Long transferId) {
        Set<Long> transferIdSet = new HashSet<>();
        for (Transfer t: transfers) {
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
