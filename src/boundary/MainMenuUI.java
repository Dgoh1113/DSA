package boundary;

import adt.DoublyLinkedList;
import control.FrontDeskController;
import control.LoyaltyController;
import control.StandardBookingController;
import control.VIPAllocationController;
import control.VIPAllocationController.VIPReservation;
import entity.Guest;
import entity.LoyaltyAccount;
import entity.Reservation;
import java.util.Scanner;

/**
 * Boundary: Main Menu UI.
 * Displays the primary menu and routes to sub-module UIs.
 * All System.out / Scanner interactions live here.
 */
public class MainMenuUI {

    private StandardBookingUI standardBookingUI;
    private VIPAllocationUI vipAllocationUI;
    private FrontDeskUI frontDeskUI;
    private LoyaltyUI loyaltyUI;
    private Scanner scanner;
    private StandardBookingController bookingController;
    private VIPAllocationController vipController;
    private LoyaltyController loyaltyController;

    public MainMenuUI(StandardBookingController mod1,
                      VIPAllocationController mod2,
                      FrontDeskController mod3,
                      LoyaltyController mod4) {
        this.scanner = new Scanner(System.in);
        this.bookingController = mod1;
        this.vipController = mod2;
        this.loyaltyController = mod4;
        this.standardBookingUI = new StandardBookingUI(mod1, scanner);
        this.vipAllocationUI = new VIPAllocationUI(mod2, scanner);
        this.frontDeskUI = new FrontDeskUI(mod3, scanner);
        this.loyaltyUI = new LoyaltyUI(mod4, scanner);
    }

    public void start() {
        int choice = 0;
        do {
            utility.UIUtils.clearScreen();
            utility.UIUtils.printMainTitleHeader();
            displayMenu();
            choice = utility.UIUtils.safeReadInt(scanner);

            switch (choice) {
                case 1:
                    standardBookingUI.show();
                    break;
                case 2:
                    vipAllocationUI.show();
                    break;
                case 3:
                    frontDeskUI.show();
                    break;
                case 4:
                    loyaltyUI.show();
                    break;
                case 9:
                    showQueueManagement();
                    break;
                case 0:
                    utility.UIUtils.clearScreen();
                    System.out.println(utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "=====================================================================" + utility.UIUtils.RESET);
                    System.out.println(utility.UIUtils.WHITE + utility.UIUtils.BOLD + "  Thank you for using TARUMT Resorts System." + utility.UIUtils.RESET);
                    System.out.println(utility.UIUtils.CYAN + "  All session data saved. Goodbye!" + utility.UIUtils.RESET);
                    System.out.println(utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "=====================================================================" + utility.UIUtils.RESET);
                    break;
                default:
                    System.out.println(utility.UIUtils.RED + "Invalid option. Please try again." + utility.UIUtils.RESET);
                    utility.UIUtils.pressEnterToContinue(scanner);
            }
        } while (choice != 0);
    }

    private void displayMenu() {
        utility.UIUtils.printSectionHeader("CORE SYSTEM MODULES", utility.UIUtils.CYAN);
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "1." + utility.UIUtils.RESET + " Walk-In & Standard Booking  " + utility.UIUtils.CYAN + "[ Queue ADT ]" + utility.UIUtils.RESET);
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "2." + utility.UIUtils.RESET + " VIP Priority Allocation     " + utility.UIUtils.YELLOW + "[ Max-Heap ADT ]" + utility.UIUtils.RESET);
        System.out.println("  " + utility.UIUtils.GREEN + utility.UIUtils.BOLD + "3." + utility.UIUtils.RESET + " Front-Desk Service          " + utility.UIUtils.GREEN + "[ BST Search ADT ]" + utility.UIUtils.RESET);
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "4." + utility.UIUtils.RESET + " Loyalty & Rewards Program   " + utility.UIUtils.MAGENTA + "[ Doubly-Linked List + Sorting ]" + utility.UIUtils.RESET);

        utility.UIUtils.printSectionHeader("SYSTEM CONTROL", utility.UIUtils.RED);
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "9." + utility.UIUtils.RESET + " Check / Approve Customer Queue");
        System.out.println("  " + utility.UIUtils.RED + utility.UIUtils.BOLD + "0." + utility.UIUtils.RESET + " Exit Application");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.print(utility.UIUtils.BOLD + "Enter your choice: " + utility.UIUtils.RESET);
    }

    /**
     * Lets front-desk staff review and approve waiting bookings. Standard
     * bookings remain FIFO; VIP bookings retain
     * their max-heap priority order.
     */
    private void showQueueManagement() {
        boolean backToMainMenu = false;
        while (!backToMainMenu) {
            utility.UIUtils.clearScreen();
            utility.UIUtils.printMainTitleHeader();
            utility.UIUtils.printSectionHeader("QUEUE CHECK & APPROVAL", utility.UIUtils.CYAN);
            System.out.println("  Standard customers waiting : " + bookingController.getQueueSize());
            System.out.println("  VIP customers waiting      : " + vipController.getVIPQueueSize());
            System.out.println("\n  1. View All Waiting Customers");
            System.out.println("  2. Approve Next Standard Booking (FIFO)");
            System.out.println("  3. Approve Next VIP Booking (Priority)");
            System.out.println("  0. Back to Main Menu");
            System.out.print("Enter your choice: ");

            switch (utility.UIUtils.safeReadInt(scanner)) {
                case 1:
                    displayWaitingCustomers();
                    utility.UIUtils.pressEnterToContinue(scanner);
                    break;
                case 2:
                    approveNextStandardBooking();
                    utility.UIUtils.pressEnterToContinue(scanner);
                    break;
                case 3:
                    approveNextVIPBooking();
                    utility.UIUtils.pressEnterToContinue(scanner);
                    break;
                case 0:
                    backToMainMenu = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    utility.UIUtils.pressEnterToContinue(scanner);
            }
        }
    }

    private void displayWaitingCustomers() {
        utility.UIUtils.clearScreen();
        utility.UIUtils.printSectionHeader("CUSTOMERS STILL IN QUEUE", utility.UIUtils.CYAN);

        DoublyLinkedList<Reservation> standardQueue = bookingController.getQueueList();
        System.out.println("\nSTANDARD QUEUE (FIFO)");
        if (standardQueue.isEmpty()) {
            System.out.println("  No standard customers are waiting.");
        } else {
            System.out.println("+-----+----------------+----------------------+-----------+------------+");
            System.out.println("| No. | Confirmation   | Customer             | Room Type | Status     |");
            System.out.println("+-----+----------------+----------------------+-----------+------------+");
            for (int i = 1; i <= standardQueue.getNumberOfEntries(); i++) {
                Reservation reservation = standardQueue.getEntry(i);
                printQueueRow(i, reservation);
            }
            System.out.println("+-----+----------------+----------------------+-----------+------------+");
        }

        DoublyLinkedList<VIPReservation> vipQueue = vipController.getVIPQueueList();
        System.out.println("\nVIP QUEUE (HIGHEST PRIORITY IS APPROVED FIRST)");
        if (vipQueue.isEmpty()) {
            System.out.println("  No VIP customers are waiting.");
        } else {
            System.out.println("+-----+----------------+----------------------+-----------+----------+------------+");
            System.out.println("| No. | Confirmation   | Customer             | Room Type | Priority | Status     |");
            System.out.println("+-----+----------------+----------------------+-----------+----------+------------+");
            for (int i = 1; i <= vipQueue.getNumberOfEntries(); i++) {
                Reservation reservation = vipQueue.getEntry(i).getReservation();
                String name = reservation.getGuest() == null
                        ? reservation.getGuestId() : reservation.getGuest().getName();
                System.out.printf("| %-3d | %-14s | %-20s | %-9s | %-8d | %-10s |%n",
                        i, reservation.getConfirmationNo(), name, reservation.getRoomType(),
                        reservation.getPriorityScore(), reservation.getBookingStatus());
            }
            System.out.println("+-----+----------------+----------------------+-----------+----------+------------+");
        }
    }

    private void approveNextStandardBooking() {
        Reservation nextBooking = bookingController.peekNextBooking();
        if (nextBooking == null) {
            System.out.println("No standard customers are waiting for approval.");
            return;
        }

        System.out.println("\nNext standard customer (FIFO):");
        printReservationSummary(nextBooking);
        if (!confirmApproval()) return;

        Reservation approvedBooking = bookingController.processNextBooking();
        displayApprovalResult(approvedBooking, "standard");
    }

    private void approveNextVIPBooking() {
        Reservation nextBooking = vipController.peekNextVIP();
        if (nextBooking == null) {
            System.out.println("No VIP customers are waiting for approval.");
            return;
        }

        System.out.println("\nHighest-priority VIP customer:");
        printReservationSummary(nextBooking);
        if (!confirmApproval()) return;

        Reservation approvedBooking = vipController.allocateNextVIP();
        displayApprovalResult(approvedBooking, "VIP");
    }

    private boolean confirmApproval() {
        System.out.print("Approve this booking and assign a room? (Y/N): ");
        if (!"Y".equalsIgnoreCase(utility.UIUtils.safeReadLine(scanner))) {
            System.out.println("Approval cancelled. The customer remains in the queue.");
            return false;
        }
        return true;
    }

    private void displayApprovalResult(Reservation reservation, String queueType) {
        if (reservation == null) {
            System.out.println("The queue could not be processed.");
        } else if ("CONFIRMED".equals(reservation.getBookingStatus())) {
            System.out.println("\nBooking approved. Room " + reservation.getAssignedRoomNo()
                    + " assigned to " + customerName(reservation) + ".");
        } else {
            System.out.println("\nNo room is currently available. " + customerName(reservation)
                    + " remains in the " + queueType + " queue.");
        }
    }

    private void printReservationSummary(Reservation reservation) {
        System.out.println("  Customer        : " + customerName(reservation));
        System.out.println("  Confirmation No : " + reservation.getConfirmationNo());
        System.out.println("  Room Type       : " + reservation.getRoomType());
        System.out.println("  Check-In Date   : " + reservation.getCheckInDate());
    }

    private void printQueueRow(int position, Reservation reservation) {
        System.out.printf("| %-3d | %-14s | %-20s | %-9s | %-10s |%n",
                position, reservation.getConfirmationNo(), customerName(reservation),
                reservation.getRoomType(), reservation.getBookingStatus());
    }

    private String customerName(Reservation reservation) {
        return reservation.getGuest() == null ? reservation.getGuestId() : reservation.getGuest().getName();
    }

    private int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }
}

