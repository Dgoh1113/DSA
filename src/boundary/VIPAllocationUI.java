package boundary;

import adt.DoublyLinkedList;
import control.VIPAllocationController;
import control.VIPAllocationController.VIPReservation;
import entity.Guest;
import entity.Reservation;
import java.util.Scanner;

/**
 * Boundary: VIP Allocation UI (Module 2).
 * Console dashboard for VIP priority booking and room allocation.
 * All System.out / Scanner interactions live here — no business logic.
 */
public class VIPAllocationUI {

    private VIPAllocationController controller;
    private Scanner scanner;
    private Guest authenticatedGuest;

    public VIPAllocationUI(VIPAllocationController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void setAuthenticatedGuest(Guest authenticatedGuest) {
        this.authenticatedGuest = authenticatedGuest;
    }

    public void show() {
        boolean exitToMainMenu = false;
        do {
            utility.UIUtils.clearScreen();
            utility.UIUtils.printModule2Header();
            displayMenu();
            int choice = utility.UIUtils.safeReadInt(scanner);

            switch (choice) {
                case 1:
                    if (addVIPBooking()) {
                        exitToMainMenu = true;
                    } else {
                        exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    }
                    break;
                case 2:
                    allocateNextVIP();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 3:
                    peekNextVIP();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 4:
                    viewVIPQueue();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 5:
                    priorityQueueSummary();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 6:
                    allocationPerformanceReport();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 7:
                    vipAllocationDemandReport();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 0:
                    exitToMainMenu = true;
                    break;
                default:
                    System.out.println(utility.UIUtils.RED + "Invalid option. Please try again." + utility.UIUtils.RESET);
                    utility.UIUtils.pressEnterToContinue(scanner);
            }
        } while (!exitToMainMenu);
    }

    private void displayMenu() {
        utility.UIUtils.printSectionHeader("VIP BOOKING & ALLOCATION", utility.UIUtils.YELLOW);
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "1." + utility.UIUtils.RESET + " Add VIP Booking");
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "2." + utility.UIUtils.RESET + " Allocate Room to Next VIP (Max-Heap Priority)");

        utility.UIUtils.printSectionHeader("PRIORITY QUEUE MONITORING", utility.UIUtils.YELLOW);
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "3." + utility.UIUtils.RESET + " View Next Highest Priority VIP");
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "4." + utility.UIUtils.RESET + " View VIP Priority Queue");

        utility.UIUtils.printSectionHeader("REPORTS & ANALYTICS", utility.UIUtils.YELLOW);
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "5." + utility.UIUtils.RESET + " Generate VIP Queue Demand Report");
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "6." + utility.UIUtils.RESET + " Generate Allocation Performance Report");
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "7." + utility.UIUtils.RESET + " Generate VIP Allocation Demand Report (Filter + MergeSort)");

        utility.UIUtils.printSectionHeader("NAVIGATION", utility.UIUtils.RED);
        System.out.println("  " + utility.UIUtils.RED + utility.UIUtils.BOLD + "0." + utility.UIUtils.RESET + " Back to Main Menu");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.print(utility.UIUtils.BOLD + "Enter your choice: " + utility.UIUtils.RESET);
    }

    private boolean addVIPBooking() {
        utility.UIUtils.printSubHeader("MODULE 2 > ADD VIP BOOKING", utility.UIUtils.YELLOW);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");

        Guest guest;
        if (authenticatedGuest != null) {
            guest = authenticatedGuest;
        } else {
            utility.StepResult result = utility.ValidationUtils.readValidStringStep(
                    scanner, "Guest ID", "", false);
            if (result.isQuitToMain()) return true;
            if (result.isCancel() || result.isGoBack()) return false;

            guest = controller.findGuestById(result.getValue());
            if (guest == null) {
                System.out.println("\n  [!] Guest ID not found. Register the guest through Module 1 first.");
                return false;
            }
        }
        if (!guest.isVIP()) {
            System.out.println("\n  [!] " + guest.getName() + " (" + guest.getGuestId()
                    + ") is " + guest.getLoyaltyTier() + " and is not VIP eligible.");
            return false;
        }
        return addVIPBookingForGuest(guest);
    }

    //-----old code for addVIPBooking() method, kept for reference-----
    // private boolean addVIPBooking() {
    //     utility.UIUtils.printSubHeader("MODULE 2 > ADD VIP BOOKING", utility.UIUtils.YELLOW);
    //     System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");

    //     Guest guest;
    //     if (authenticatedGuest != null) {
    //         guest = authenticatedGuest;
    //     } else {
    //         utility.StepResult result = utility.ValidationUtils.readValidStringStep(
    //                 scanner, "Guest ID", "", false);
    //         if (result.isQuitToMain()) return true;
    //         if (result.isCancel() || result.isGoBack()) return false;

    //         guest = controller.findGuestById(result.getValue());
    //         if (guest == null) {
    //             System.out.println("\n  [!] Guest ID not found. Register the guest through Module 1 first.");
    //             return false;
    //         }
    //     }
    //     if (!guest.isVIP()) {
    //         System.out.println("\n  [!] " + guest.getName() + " (" + guest.getGuestId()
    //                 + ") is " + guest.getLoyaltyTier() + " and is not VIP eligible.");
    //         return false;
    //     }
    //     return addVIPBookingForGuest(guest);
    // }

    private boolean addVIPBookingForGuest(Guest guest) {
        System.out.println("\n  VIP Member      : " + guest.getName());
        System.out.println("  Guest ID        : " + guest.getGuestId());
        System.out.println("  Loyalty Tier    : " + guest.getLoyaltyTier());
        System.out.println("  Contact Number  : " + guest.getContactNo());
        System.out.println("\n  Only the room and stay dates are required.\n");

        String roomType = "";
        String checkIn = "";
        String checkOut = "";
        int step = 0;
        while (step <= 2) {
            switch (step) {
                case 0: {
                    System.out.println("  Room Options: STANDARD | DELUXE | SUITE");
                    utility.StepResult result = utility.ValidationUtils.readValidRoomTypeStep(
                            scanner, "Step 1/3 - Preferred Room", roomType);
                    if (result.isGoBack() || result.isCancel()) return false;
                    if (result.isQuitToMain()) return true;
                    roomType = result.getValue();
                    step++;
                    break;
                }
                case 1: {
                    utility.StepResult result = utility.ValidationUtils.readValidDateStep(
                            scanner, "Step 2/3 - Check-In (YYYY-MM-DD)", checkIn);
                    if (result.isGoBack()) { step--; break; }
                    if (result.isCancel()) return false;
                    if (result.isQuitToMain()) return true;
                    checkIn = result.getValue();
                    step++;
                    break;
                }
                case 2: {
                    utility.StepResult result = utility.ValidationUtils.readValidCheckOutDateStep(
                            scanner, "Step 3/3 - Check-Out Date", checkIn, checkOut);
                    if (result.isGoBack()) { step--; break; }
                    if (result.isCancel()) return false;
                    if (result.isQuitToMain()) return true;
                    checkOut = result.getValue();
                    step++;
                    break;
                }
            }
        }

        Reservation reservation = controller.addVIPBookingForGuest(
                guest, roomType, checkIn, checkOut);
        if (reservation == null) {
            System.out.println("\n  [!] Booking failed because this account is not VIP eligible.");
            return false;
        }

        System.out.println("\n+------------------------------------------+");
        System.out.println("  VIP BOOKING REGISTERED!");
        System.out.println("+------------------------------------------+");
        System.out.println("  Confirmation No : " + reservation.getConfirmationNo());
        System.out.println("  Guest ID        : " + reservation.getGuestId());
        System.out.println("  Loyalty Tier    : " + guest.getLoyaltyTier());
        System.out.println("  Priority Score  : " + reservation.getPriorityScore());
        System.out.println("  Room Type       : " + reservation.getRoomType());
        System.out.println("  Check-In        : " + reservation.getCheckInDate());
        System.out.println("  Check-Out       : " + reservation.getCheckOutDate());
        System.out.println("  Status          : " + reservation.getBookingStatus());
        System.out.println("  VIP Queue Size  : " + controller.getVIPQueueSize());
        System.out.println("+------------------------------------------+");
        return false;
    }

    private void allocateNextVIP() {
        utility.UIUtils.printSubHeader("MODULE 2 > ALLOCATE ROOM TO NEXT VIP", utility.UIUtils.YELLOW);

        if (controller.isVIPQueueEmpty()) {
            System.out.println("The VIP priority queue is EMPTY. No VIP guests waiting.");
            return;
        }

        Reservation res = controller.allocateNextVIP();
        if (res != null) {
            System.out.println("\n+------------------------------------------+");
            if ("CONFIRMED".equals(res.getBookingStatus())) {
                System.out.println("  VIP ROOM ALLOCATED SUCCESSFULLY!");
                System.out.println("+------------------------------------------+");
                System.out.println("  Confirmation No : " + res.getConfirmationNo());
                System.out.println("  Guest ID        : " + res.getGuestId());
                System.out.println("  Guest Name      : " + (res.getGuest() != null ? res.getGuest().getName() : "N/A"));
                System.out.println("  Priority Score  : " + res.getPriorityScore());
                System.out.println("  Room Assigned   : " + res.getAssignedRoomNo());
                System.out.println("  Status          : " + res.getBookingStatus());
            } else {
                System.out.println("  NO ROOMS AVAILABLE FOR VIP");
                System.out.println("+------------------------------------------+");
                System.out.println("  Confirmation No : " + res.getConfirmationNo());
                System.out.println("  VIP guest dequeued but no room could be assigned.");
            }
            System.out.println("  Remaining VIPs  : " + controller.getVIPQueueSize());
            System.out.println("+------------------------------------------+");
        }
    }

    private void peekNextVIP() {
        utility.UIUtils.printSubHeader("MODULE 2 > PEEK HIGHEST PRIORITY VIP", utility.UIUtils.YELLOW);

        Reservation res = controller.peekNextVIP();
        if (res == null) {
            System.out.println("The VIP priority queue is EMPTY.");
        } else {
            System.out.println("  Highest Priority VIP:");
            System.out.println("  Guest            : " + (res.getGuest() != null ? res.getGuest().getName() : res.getGuestId()));
            System.out.println("  Confirmation No  : " + res.getConfirmationNo());
            System.out.println("  Loyalty Tier     : " + (res.getGuest() != null ? res.getGuest().getLoyaltyTier() : "N/A"));
            System.out.println("  Priority Score   : " + res.getPriorityScore());
            System.out.println("  Room Type        : " + res.getRoomType());
        }
    }

    private void viewVIPQueue() {
        utility.UIUtils.printSubHeader("MODULE 2 > VIEW VIP PRIORITY QUEUE", utility.UIUtils.YELLOW);
        System.out.println("Queue Size: " + controller.getVIPQueueSize());

        if (controller.isVIPQueueEmpty()) {
            System.out.println("The VIP queue is empty.");
            return;
        }

DoublyLinkedList<VIPReservation> list = controller.getVIPQueueList();
    System.out.println("+-----+----------------+----------------------+----------------+-----------+----------+----------+------------+------------+--------+");
    System.out.println("| No. | Confirmation   | Guest Name           | Guest ID       | Points    | Tier     | Priority | Check-In   | Check-Out  | Status |");
    System.out.println("+-----+----------------+----------------------+----------------+-----------+----------+----------+------------+------------+--------+");

        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            Reservation res = list.getEntry(i).getReservation();
            String tier = (res.getGuest() != null) ? res.getGuest().getLoyaltyTier() : "N/A";
        String guestName = res.getGuest() != null ? res.getGuest().getName() : "N/A";
        Integer currentPoints = controller.getCurrentPointsByGuestId(res.getGuestId());
        System.out.printf("| %-3d | %-14s | %-20s | %-14s | %-9s | %-8s | %-8d | %-10s | %-10s | %-6s |%n",
            i, res.getConfirmationNo(), guestName, res.getGuestId(),
            currentPoints != null ? currentPoints.toString() : "N/A",
            tier, res.getPriorityScore(),
            res.getCheckInDate() != null ? res.getCheckInDate() : "N/A",
            res.getCheckOutDate() != null ? res.getCheckOutDate() : "N/A",
            res.getBookingStatus());
        }
    System.out.println("+-----+----------------+----------------------+----------------+-----------+----------+----------+------------+------------+--------+");
    }

    private void priorityQueueSummary() {
        utility.UIUtils.printSubHeader("MODULE 2 > VIP QUEUE DEMAND REPORT", utility.UIUtils.YELLOW);
        VIPAllocationController.PriorityQueueSummary summary = controller.generatePriorityQueueSummary();

        System.out.println("+------------------------------------------+");
        System.out.println("          VIP QUEUE DEMAND REPORT");
        System.out.println("+------------------------------------------+");
        System.out.println("  Pending VIPs       : " + summary.getPendingCount());
        System.out.println("  Highest Priority   : " + summary.getHighestPriorityScore());
        System.out.println("  SILVER             : " + summary.getSilverCount());
        System.out.println("  GOLD               : " + summary.getGoldCount());
        System.out.println("  PLATINUM           : " + summary.getPlatinumCount());
        System.out.println("  DIAMOND            : " + summary.getDiamondCount());
        System.out.println("  Room Demand        : STANDARD=" + summary.getStandardRoomCount()
            + " | DELUXE=" + summary.getDeluxeRoomCount()
            + " | SUITE=" + summary.getSuiteRoomCount());
        System.out.println("  Rooms Available    : STANDARD=" + summary.getAvailableStandardRooms()
            + " | DELUXE=" + summary.getAvailableDeluxeRooms()
            + " | SUITE=" + summary.getAvailableSuiteRooms());
        System.out.println("  Room Shortage      : STANDARD=" + summary.getStandardRoomShortage()
            + " | DELUXE=" + summary.getDeluxeRoomShortage()
            + " | SUITE=" + summary.getSuiteRoomShortage());
        System.out.println("+------------------------------------------+");
    }

    private void allocationPerformanceReport() {
        utility.UIUtils.printSubHeader("MODULE 2 > ALLOCATION PERFORMANCE REPORT", utility.UIUtils.YELLOW);
        VIPAllocationController.AllocationPerformanceReport report = controller.generateAllocationPerformanceReport();

        System.out.println("+------------------------------------------+");
        System.out.println("       ALLOCATION PERFORMANCE REPORT");
        System.out.println("+------------------------------------------+");
        System.out.println("  Total VIP Bookings : " + report.getTotalBookings());
        System.out.println("  Allocated Rooms    : " + report.getAllocatedBookings());
        System.out.println("  Pending Bookings   : " + report.getPendingBookings());
        System.out.println("  Cancelled Bookings : " + report.getCancelledBookings());
        System.out.printf("  Allocation Rate     : %.2f%%%n", report.getAllocationRate());
        System.out.println("+------------------------------------------+");
    }

    /** Collects three management filters and displays the priority-ranked results. */
    private void vipAllocationDemandReport() {
        utility.UIUtils.printSubHeader("MODULE 2 > VIP ALLOCATION DEMAND REPORT", utility.UIUtils.YELLOW);
        String minimumTier = readReportChoice("Minimum tier (SILVER | GOLD | PLATINUM | DIAMOND): ",
                "SILVER|GOLD|PLATINUM|DIAMOND");
        String roomType = readReportChoice("Room type (ALL | STANDARD | DELUXE | SUITE): ",
                "ALL|STANDARD|DELUXE|SUITE");
        String bookingStatus = readReportChoice(
                "Status (ALL | PENDING | CONFIRMED | CHECKED_IN | CHECKED_OUT | CANCELLED): ",
                "ALL|PENDING|CONFIRMED|CHECKED_IN|CHECKED_OUT|CANCELLED");

        VIPAllocationController.VIPAllocationDemandReport report =
                controller.generateVIPAllocationDemandReport(minimumTier, roomType, bookingStatus);
        DoublyLinkedList<Reservation> reservations = report.getReservations();

        System.out.println("\n+--------------------------------------------------------------------------------------------------+");
        System.out.println("                         VIP ALLOCATION DEMAND REPORT");
        System.out.println("+--------------------------------------------------------------------------------------------------+");
        System.out.println("  Filters: Minimum Tier = " + report.getMinimumTier()
                + " | Room Type = " + report.getRoomType()
                + " | Status = " + report.getBookingStatus());
        System.out.println("  Search: reservation registry scan  |  Sort: MergeSort by priority (highest first)");
        System.out.println("+-----+--------------+----------------------+----------+----------+----------+------------+------------+");
        System.out.println("| No. | Confirmation | Guest Name           | Tier     | Room     | Priority | Check-In   | Status     |");
        System.out.println("+-----+--------------+----------------------+----------+----------+----------+------------+------------+");
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            String guestName = reservation.getGuest() == null ? "N/A" : reservation.getGuest().getName();
            String tier = reservation.getGuest() == null ? "N/A" : reservation.getGuest().getLoyaltyTier();
            System.out.printf("| %-3d | %-12s | %-20s | %-8s | %-8s | %-8d | %-10s | %-10s |%n",
                    i, reservation.getConfirmationNo(), guestName, tier, reservation.getRoomType(),
                    reservation.getPriorityScore(), reservation.getCheckInDate(), reservation.getBookingStatus());
        }
        if (reservations.isEmpty()) {
            System.out.println("|                              No VIP reservations match the selected filters.                         |");
        }
        System.out.println("+-----+--------------+----------------------+----------+----------+----------+------------+------------+");
        System.out.println("  Total Requests : " + report.getTotalRequests()
                + " | Pending: " + report.getPendingCount()
                + " | Allocated: " + report.getAllocatedCount());
        System.out.printf("  Allocation Rate: %.2f%%%n", report.getAllocationRate());
        System.out.println("  Tier Breakdown : Silver=" + report.getSilverCount() + " | Gold=" + report.getGoldCount()
                + " | Platinum=" + report.getPlatinumCount() + " | Diamond=" + report.getDiamondCount());
        System.out.println("+--------------------------------------------------------------------------------------------------+");
    }

    private String readReportChoice(String prompt, String permittedChoices) {
        while (true) {
            System.out.print("  " + prompt);
            // Read the complete line so the post-report navigation prompt does
            // not immediately consume a leftover newline and clear the report.
            String choice = scanner.nextLine().trim().toUpperCase();
            if (("|" + permittedChoices + "|").contains("|" + choice + "|")) {
                return choice;
            }
            System.out.println("  [!] Invalid option. Choose from: " + permittedChoices.replace("|", " | "));
        }
    }
    
    private int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }
}
