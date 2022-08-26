package com.techelevator.tenmo.services;


import com.techelevator.tenmo.model.TransferDetail;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;

import java.math.BigDecimal;
import java.util.Scanner;

public class ConsoleService {

    private final Scanner scanner = new Scanner(System.in);

    public int promptForMenuSelection(String prompt) {
        int menuSelection;
        System.out.print(prompt);
        try {
            menuSelection = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            menuSelection = -1;
        }
        return menuSelection;
    }

    public void printGreeting() {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");
    }

    public void printLoginMenu() {
        System.out.println();
        System.out.println("1: Register");
        System.out.println("2: Login");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printMainMenu() {
        System.out.println();
        System.out.println("1: View your current balance");
        System.out.println("2: View your past transfers");
        System.out.println("3: View your pending requests");
        System.out.println("4: Send TE bucks");
        System.out.println("5: Request TE bucks");
        System.out.println("0: Exit");
        System.out.println();
    }

    public UserCredentials promptForCredentials() {
        String username = promptForString("Username: ");
        String password = promptForString("Password: ");
        return new UserCredentials(username, password);
    }

    public String promptForString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int promptForInt(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    public BigDecimal promptForBigDecimal(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a decimal number.");
            }
        }
    }

    public Long promptForLong(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return Long.parseLong(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid Id.");
            }
        }
    }

    public void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void printUsers(User[] users) {
        System.out.println();
        System.out.println("----------------------------");
        System.out.println("User");
        System.out.println("Id          Name");
        System.out.println("----------------------------");
        for (User u: users) {
            System.out.println(u.getId() + "       " + u.getUsername());
        }
        System.out.println();
    }

    public void printErrorMessage() {
        System.out.println("An error occurred. Check the log for details.");
    }

    public void printErrorMessage(String errMessage) {
        System.out.println(errMessage);
    }

    public void printPendingTransfers(TransferDetail[] transferDetails) {
        System.out.println("-----------------------------------------");
        System.out.println("Pending Tranfers");
        System.out.printf("%-10s %-20s %-15s %n", "ID", "To", "Amount");
        System.out.println("-----------------------------------------");
        for(TransferDetail t : transferDetails) {
            System.out.printf("%-10d %-20s %-13.2f%n", t.getId(), t.getToUser().getUsername(), t.getAmount());
        }
        System.out.println("-----------------------------------------");
        System.out.println();
    }

    public void printApproveRejectMenu() {
        System.out.println("1. Approve");
        System.out.println("2. Reject");
        System.out.println("0. Don't approve or reject");
        System.out.println("--------------------------");
    }

    public void printTransfers(TransferDetail[] transferDetails, User user){
        System.out.println("---------------------------------------------");
        System.out.println("Transfers");
        System.out.printf("%-14s %-23s %s \n", "ID", "From/To", "Amount");
        System.out.println("---------------------------------------------");
        for(TransferDetail t: transferDetails) {
            if (t.getType().equals("Send") || t.getToUser().getId().equals(user.getId())) {
                System.out.printf("%-14s To:%-18s $%s \n", t.getId().toString() ,t.getToUser().getUsername() , t.getAmount().toString());
            } else if(t.getType().equals("Request")){
                System.out.printf("%-14s From:%-18s $%s \n", t.getId().toString() ,t.getFromUser().getUsername() , t.getAmount().toString());
            }
        }
        System.out.println("---------");
    }

    public void printTransferDetail(TransferDetail transferDetails){
        System.out.println("---------------------------------------------");
        System.out.println("Transfer Details");
        System.out.println("---------------------------------------------");
        System.out.printf(" %-2s %s \n" , "Id:", transferDetails.getId());
        System.out.printf(" %-4s %s \n" ,"From:", transferDetails.getFromUser().getUsername());
        System.out.printf(" %-2s %s \n" ,"To:", transferDetails.getToUser().getUsername());
        System.out.printf(" %-4s %s \n" ,"Type:", transferDetails.getType());
        System.out.printf(" %-6s %s \n" ,"Status:", transferDetails.getStatus());
        System.out.printf(" %-6s $%s \n" ,"Amount:", transferDetails.getAmount());
    }
}