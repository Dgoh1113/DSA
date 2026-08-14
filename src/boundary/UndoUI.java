package boundary;

import adt.DoublyLinkedList;
import control.UndoController;
import entity.UndoAction;
import java.util.Scanner;
import utility.UIUtils;

/**
 * Boundary: Undo Control Center UI.
 * Provides the interactive boundary interface for viewing, peeking, and
 * executing transaction reversals via the CustomQueue<UndoAction> (Queue ADT).
 */
public class UndoUI {

    private UndoController controller;
    private Scanner scanner;

    public UndoUI(UndoController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void show() {
        boolean exitToMainMenu = false;
        do {
            UIUtils.clearScreen();
            printUndoHeader();
            displayMenu();
            int choice = UIUtils.safeReadInt(scanner);

            switch (choice) {
                case 1:
                    processNextUndo();
                    exitToMainMenu = UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 2:
                    peekNextUndo();
                    exitToMainMenu = UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 3:
                    viewUndoQueue();
                    exitToMainMenu = UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 4:
                    clearQueue();
                    exitToMainMenu = UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 0:
                    exitToMainMenu = true;
                    break;
                default:
                    System.out.println(UIUtils.RED + "Invalid option. Please try again." + UIUtils.RESET);
                    UIUtils.pressEnterToContinue(scanner);
            }
        } while (!exitToMainMenu);
    }

    private void printUndoHeader() {
        System.out.println(UIUtils.PURPLE + UIUtils.BOLD + "=====================================================================" + UIUtils.RESET);
        System.out.println(UIUtils.PURPLE + UIUtils.BOLD + "    CENTRAL TRANSACTION UNDO UTILITY  [ STACK ADT - LIFO ]" + UIUtils.RESET);
        System.out.println(UIUtils.WHITE + "    Cross-Module System Utility — Reverses Most Recent Action First" + UIUtils.RESET);
        System.out.println(UIUtils.PURPLE + UIUtils.BOLD + "=====================================================================" + UIUtils.RESET);
        System.out.println("  " + UIUtils.YELLOW + "Pending Reversible Actions on Stack: " + controller.getQueueSize() + UIUtils.RESET);
        System.out.println();
    }

    private void displayMenu() {
        UIUtils.printSectionHeader("STACK UNDO REVERSAL CONTROLS", UIUtils.PURPLE);
        System.out.println("  " + UIUtils.PURPLE + UIUtils.BOLD + "1." + UIUtils.RESET + " Process Most Recent Undo (Pop & Execute Reversal)");
        System.out.println("  " + UIUtils.PURPLE + UIUtils.BOLD + "2." + UIUtils.RESET + " Peek Top Action on Undo Stack");

        UIUtils.printSectionHeader("STACK AUDIT & MANAGEMENT", UIUtils.PURPLE);
        System.out.println("  " + UIUtils.PURPLE + UIUtils.BOLD + "3." + UIUtils.RESET + " View Entire Undo Stack");
        System.out.println("  " + UIUtils.PURPLE + UIUtils.BOLD + "4." + UIUtils.RESET + " Clear Undo Stack History");

        UIUtils.printSectionHeader("NAVIGATION", UIUtils.RED);
        System.out.println("  " + UIUtils.RED + UIUtils.BOLD + "0." + UIUtils.RESET + " Back to Main Menu");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.print(UIUtils.BOLD + "Enter your choice: " + UIUtils.RESET);
    }

    private void processNextUndo() {
        UIUtils.printSubHeader("SYSTEM UTILITY > PROCESS MOST RECENT UNDO (POP)", UIUtils.PURPLE);

        if (controller.isQueueEmpty()) {
            System.out.println(UIUtils.YELLOW + "The Undo Stack is currently EMPTY. No actions available to undo." + UIUtils.RESET);
            return;
        }

        UndoAction peekAction = controller.peekNextUndo();
        System.out.println("Most recent action to reverse (Top of Stack):");
        System.out.println("  ID          : " + peekAction.getActionId());
        System.out.println("  Module      : " + peekAction.getModuleName());
        System.out.println("  Description : " + peekAction.getDescription());
        System.out.println("  Logged At   : " + peekAction.getTimestamp());

        System.out.print("\nConfirm execution of undo reversal? (Y/N): ");
        String confirm = UIUtils.safeReadLine(scanner).toUpperCase();

        if ("Y".equals(confirm)) {
            UndoAction undone = controller.processNextUndo();
            if (undone != null) {
                System.out.println("\n" + UIUtils.GREEN + UIUtils.BOLD + "+------------------------------------------+" + UIUtils.RESET);
                System.out.println(UIUtils.GREEN + UIUtils.BOLD + "  TRANSACTION UNDONE SUCCESSFULLY!" + UIUtils.RESET);
                System.out.println(UIUtils.GREEN + UIUtils.BOLD + "+------------------------------------------+" + UIUtils.RESET);
                System.out.println("  Action ID   : " + undone.getActionId());
                System.out.println("  Module      : " + undone.getModuleName());
                System.out.println("  Reverted    : " + undone.getDescription());
                System.out.println("  Remaining on Stack: " + controller.getQueueSize());
                System.out.println("+------------------------------------------+");
            } else {
                System.out.println(UIUtils.RED + "Failed to execute undo." + UIUtils.RESET);
            }
        } else {
            System.out.println("Undo operation cancelled.");
        }
    }

    private void peekNextUndo() {
        UIUtils.printSubHeader("SYSTEM UTILITY > PEEK TOP STACK UNDO", UIUtils.PURPLE);

        UndoAction action = controller.peekNextUndo();
        if (action == null) {
            System.out.println(UIUtils.YELLOW + "The Undo Stack is currently EMPTY." + UIUtils.RESET);
        } else {
            System.out.println("  Top Action on Stack:");
            System.out.println("  Action ID   : " + action.getActionId());
            System.out.println("  Module      : " + action.getModuleName());
            System.out.println("  Description : " + action.getDescription());
            System.out.println("  Timestamp   : " + action.getTimestamp());
        }
    }

    private void viewUndoQueue() {
        UIUtils.printSubHeader("SYSTEM UTILITY > VIEW UNDO STACK HISTORY", UIUtils.PURPLE);
        System.out.println("Stack Size: " + controller.getQueueSize());

        if (controller.isQueueEmpty()) {
            System.out.println(UIUtils.YELLOW + "The Undo Stack is empty." + UIUtils.RESET);
            return;
        }

        DoublyLinkedList<UndoAction> list = controller.getUndoQueueList();
        System.out.println("+-----+----------+----------+-----------------------------+----------------------------------------------+");
        System.out.println("| Pos | ID       | Time     | Originating Module          | Description                                  |");
        System.out.println("+-----+----------+----------+-----------------------------+----------------------------------------------+");

        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            UndoAction action = list.getEntry(i);
            System.out.printf("| %-3d | %-8s | %-8s | %-27s | %-44s |%n",
                    i, action.getActionId(), action.getTimestamp(),
                    truncate(action.getModuleName(), 27),
                    truncate(action.getDescription(), 44));
        }
        System.out.println("+-----+----------+----------+-----------------------------+----------------------------------------------+");
    }

    private void clearQueue() {
        UIUtils.printSubHeader("SYSTEM UTILITY > CLEAR UNDO STACK", UIUtils.PURPLE);
        if (controller.isQueueEmpty()) {
            System.out.println("The stack is already empty.");
            return;
        }

        System.out.print("Are you sure you want to clear all " + controller.getQueueSize() + " pending undo actions on the stack? (Y/N): ");
        String confirm = UIUtils.safeReadLine(scanner).toUpperCase();
        if ("Y".equals(confirm)) {
            controller.clearUndoQueue();
            System.out.println(UIUtils.GREEN + "Undo Stack cleared successfully." + UIUtils.RESET);
        } else {
            System.out.println("Action cancelled.");
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
