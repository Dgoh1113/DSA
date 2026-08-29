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

    public VIPAllocationUI(VIPAllocationController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
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
                    cancelVIPBookingRequest();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 3:
                    allocateNextVIP();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 4:
                    peekNextVIP();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 5:
                    viewVIPQueue();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 6:
                    priorityQueueSummary();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 7:
                    allocationPerformanceReport();
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
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "1." + utility.UIUtils.RESET + " Add VIP Booking Request");
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "2." + utility.UIUtils.RESET + " Cancel VIP Booking Request");
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "3." + utility.UIUtils.RESET + " Allocate Room to Next VIP (Max-Heap Priority)");

        utility.UIUtils.printSectionHeader("PRIORITY QUEUE MONITORING", utility.UIUtils.YELLOW);
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "4." + utility.UIUtils.RESET + " View Next Highest Priority VIP");
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "5." + utility.UIUtils.RESET + " View VIP Priority Queue");

        utility.UIUtils.printSectionHeader("REPORTS & ANALYTICS", utility.UIUtils.YELLOW);
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "6." + utility.UIUtils.RESET + " Generate VIP Queue Status Report");
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "7." + utility.UIUtils.RESET + " Generate Room Allocation Efficiency Report");

        utility.UIUtils.printSectionHeader("NAVIGATION", utility.UIUtils.RED);
        System.out.println("  " + utility.UIUtils.RED + utility.UIUtils.BOLD + "0." + utility.UIUtils.RESET + " Back to Main Menu");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.print(utility.UIUtils.BOLD + "Enter your choice: " + utility.UIUtils.RESET);
    }

    private void cancelVIPBookingRequest() {
        utility.UIUtils.printSubHeader("MODULE 2 > CANCEL VIP BOOKING REQUEST", utility.UIUtils.YELLOW);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");

        DoublyLinkedList<VIPReservation> queue = controller.getVIPQueueList();
        if (queue.isEmpty()) {
            System.out.println("No pending VIP booking requests are available for cancellation.");
            return;
        }

        System.out.println("  PENDING VIP BOOKING REQUESTS");
        System.out.println("  +-----+----------------+----------------+----------------------+----------+----------+------------+------------+-----------+");
        System.out.println("  | No. | Confirmation   | Guest ID       | Guest Name           | Room     | Priority | Check-In   | Check-Out  | Status    |");
        System.out.println("  +-----+----------------+----------------+----------------------+----------+----------+------------+------------+-----------+");
        for (int i = 1; i <= queue.getNumberOfEntries(); i++) {
            Reservation reservation = queue.getEntry(i).getReservation();
            String guestName = reservation.getGuest() == null ? "N/A" : reservation.getGuest().getName();
            System.out.printf("  | %-3d | %-14s | %-14s | %-20s | %-8s | %-8d | %-10s | %-10s | %-9s |%n",
                i, reservation.getConfirmationNo(), reservation.getGuestId(),
                guestName, reservation.getRoomType(), reservation.getPriorityScore(),
                reservation.getCheckInDate(), reservation.getCheckOutDate(),
                reservation.getBookingStatus());
        }
        System.out.println("  +-----+----------------+----------------+----------------------+----------+----------+------------+------------+-----------+");

        utility.StepResult result = utility.ValidationUtils.readValidStringStep(
                scanner, "Confirmation No", "", false);
        if (result.isQuitToMain() || result.isCancel() || result.isGoBack()) return;

        if (controller.cancelVIPBookingRequest(result.getValue())) {
            System.out.println("\n*** VIP BOOKING REQUEST CANCELLED ***");
        } else {
            System.out.println("\n[!] No pending VIP booking request found with that confirmation number.");
        }
    }

    private boolean addVIPBooking() {
        utility.UIUtils.printSubHeader("MODULE 2 > ADD VIP BOOKING REQUEST", utility.UIUtils.YELLOW);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");
        displayEligibleVIPMembers();

        utility.StepResult result = utility.ValidationUtils.readValidStringStep(
                scanner, "Guest ID", "", false);
        if (result.isQuitToMain()) return true;
        if (result.isCancel() || result.isGoBack()) return false;

        Guest guest = controller.findGuestById(result.getValue());
        if (guest == null) {
            System.out.println("\n  [!] Guest ID not found. Please register the guest before creating a booking.");
            return false;
        }
        if (!guest.isVIP()) {
            System.out.println("\n  [!] " + guest.getName() + " (" + guest.getGuestId()
                    + ") is " + guest.getLoyaltyTier() + " and is not VIP eligible.");
            return false;
        }
        return addVIPBookingForGuest(guest);
    }

    private void displayEligibleVIPMembers() {
        DoublyLinkedList<Guest> members = controller.getVIPEligibleGuests();
        System.out.println("  AVAILABLE VIP MEMBERS");
        System.out.println("  +-----+----------------+----------------------+------------+");
        System.out.println("  | No. | Guest ID       | Guest Name           | Tier       |");
        System.out.println("  +-----+----------------+----------------------+------------+");
        for (int i = 1; i <= members.getNumberOfEntries(); i++) {
            Guest guest = members.getEntry(i);
            System.out.printf("  | %-3d | %-14s | %-20s | %-10s |%n",
                    i, guest.getGuestId(), guest.getName(), guest.getLoyaltyTier());
        }
        if (members.isEmpty()) {
            System.out.println("  |       No VIP-eligible members are registered.       |");
        }
        System.out.println("  +-----+----------------+----------------------+------------+\n");
    }

    //-----old code for addVIPBooking() method, kept for reference-----
    // private boolean addVIPBooking() {
    //     utility.UIUtils.printSubHeader("MODULE 2 > ADD VIP BOOKING REQUEST", utility.UIUtils.YELLOW);
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
        utility.UIUtils.printSubHeader("MODULE 2 > VIEW NEXT HIGHEST PRIORITY VIP", utility.UIUtils.YELLOW);

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
        utility.UIUtils.printSubHeader("MODULE 2 > VIP QUEUE STATUS REPORT", utility.UIUtils.YELLOW);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");
        String minimumTier = readReportChoice("Minimum tier (SILVER | GOLD | PLATINUM | DIAMOND): ",
            "SILVER|GOLD|PLATINUM|DIAMOND");
        if (minimumTier == null) return;
        String roomType = readReportChoice("Room type (ALL | STANDARD | DELUXE | SUITE): ",
            "ALL|STANDARD|DELUXE|SUITE");
        if (roomType == null) return;
        String bookingStatus = readReportChoice(
            "Status (ALL | PENDING | CONFIRMED | CHECKED_IN | CHECKED_OUT | CANCELLED): ",
            "ALL|PENDING|CONFIRMED|CHECKED_IN|CHECKED_OUT|CANCELLED");
        if (bookingStatus == null) return;
        String sortOrder = readReportChoice(
            "Sort Priority (Choose 1 or 2):\n"
                + "    1. HIGH_TO_LOW\n"
                + "    2. LOW_TO_HIGH\n"
                + "    Enter choice: ", "1|2");
        if (sortOrder == null) return;

        DoublyLinkedList<Reservation> reservations = controller.getVIPQueueStatusReport(
            minimumTier, roomType, bookingStatus, "2".equals(sortOrder));
        System.out.println("+-----+----------------+----------------------+----------+----------+------------+------------+------------+");
        System.out.println("| No. | Confirmation   | Guest Name           | Tier     | Room     | Priority   | Check-In   | Status     |");
        System.out.println("+-----+----------------+----------------------+----------+----------+------------+------------+------------+");
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            String guestName = reservation.getGuest() == null ? "N/A" : reservation.getGuest().getName();
            String tier = reservation.getGuest() == null ? "N/A" : reservation.getGuest().getLoyaltyTier();
            System.out.printf("| %-3d | %-14s | %-20s | %-8s | %-8s | %-10d | %-10s | %-10s |%n",
                i, reservation.getConfirmationNo(), guestName, tier, reservation.getRoomType(),
                reservation.getPriorityScore(), reservation.getCheckInDate(), reservation.getBookingStatus());
        }
        if (reservations.isEmpty()) {
            System.out.println("|                 No VIP reservations match the selected filters.                 |");
        }
        System.out.println("+-----+----------------+----------------------+----------+----------+------------+------------+------------+");
    }

    private void allocationPerformanceReport() {
        utility.UIUtils.printSubHeader("MODULE 2 > ROOM ALLOCATION EFFICIENCY REPORT", utility.UIUtils.YELLOW);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");
        String minimumTier = readReportChoice("Minimum tier (SILVER | GOLD | PLATINUM | DIAMOND): ",
            "SILVER|GOLD|PLATINUM|DIAMOND");
        if (minimumTier == null) return;
        String roomType = readReportChoice("Room type (ALL | STANDARD | DELUXE | SUITE): ",
            "ALL|STANDARD|DELUXE|SUITE");
        if (roomType == null) return;
        String bookingStatus = readReportChoice(
            "Status (ALL | PENDING | CONFIRMED | CHECKED_IN | CHECKED_OUT | CANCELLED): ",
            "ALL|PENDING|CONFIRMED|CHECKED_IN|CHECKED_OUT|CANCELLED");
        if (bookingStatus == null) return;
        String sortMetric = readReportChoice(
            "Sort by (1. ALLOCATION_RATE | 2. REQUESTS | 3. PENDING | 4. AVERAGE_PRIORITY): ", "1|2|3|4");
        if (sortMetric == null) return;
        String sortOrder = readReportChoice(
            "Sort Priority (Choose 1 or 2):\n"
                + "    1. HIGH_TO_LOW\n"
                + "    2. LOW_TO_HIGH\n"
                + "    Enter choice: ", "1|2");
        if (sortOrder == null) return;
        boolean ascending = "2".equals(sortOrder);

        VIPAllocationController.VIPAllocationEfficiencyReport report =
            controller.generateVIPAllocationEfficiencyReport(
                minimumTier, roomType, bookingStatus, sortMetric, ascending);

        System.out.println("+--------------------------------------------------------------------------------+");
        System.out.println("                         ROOM ALLOCATION EFFICIENCY REPORT");
        System.out.println("+--------------------------------------------------------------------------------+");
        System.out.println("  Filters: Minimum Tier = " + report.getMinimumTier()
            + " | Room Type = " + report.getRoomType()
            + " | Status = " + report.getBookingStatus());
        System.out.println("  Sort: " + report.getSortMetric() + " ("
            + (ascending ? "lowest first" : "highest first") + ") using MergeSort");
        System.out.println("+----------+----------+----------+----------+----------+------------+----------+");
        System.out.println("| Room     | Requests | Allocated| Pending  | Cancelled| Alloc. Rate | Avg Pri. |");
        System.out.println("+----------+----------+----------+----------+----------+------------+----------+");
        for (int i = 1; i <= report.getRows().getNumberOfEntries(); i++) {
            VIPAllocationController.VIPAllocationEfficiencyRow row = report.getRows().getEntry(i);
            System.out.printf("| %-8s | %-8d | %-8d | %-8d | %-8d | %9.2f%% | %-8.2f |%n",
            row.getRoomType(), row.getRequests(), row.getAllocated(), row.getPending(),
                row.getCancelled(), row.getAllocationRate(), row.getAveragePriority());
        }
        if (report.getRows().isEmpty()) {
            System.out.println("|             No VIP reservations match the selected filters.                  |");
        }
        System.out.println("+----------+----------+----------+----------+----------+------------+----------+");
    }

    /** Collects three management filters and displays the priority-ranked results. */
    private void vipAllocationDemandReport() {
        utility.UIUtils.printSubHeader("MODULE 2 > VIP ALLOCATION DEMAND REPORT", utility.UIUtils.YELLOW);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");
        String minimumTier = readReportChoice("Minimum tier (SILVER | GOLD | PLATINUM | DIAMOND): ",
                "SILVER|GOLD|PLATINUM|DIAMOND");
        if (minimumTier == null) return;
        String roomType = readReportChoice("Room type (ALL | STANDARD | DELUXE | SUITE): ",
                "ALL|STANDARD|DELUXE|SUITE");
        if (roomType == null) return;
        String bookingStatus = readReportChoice(
                "Status (ALL | PENDING | CONFIRMED | CHECKED_IN | CHECKED_OUT | CANCELLED): ",
                "ALL|PENDING|CONFIRMED|CHECKED_IN|CHECKED_OUT|CANCELLED");
        if (bookingStatus == null) return;
        String sortOrder = readReportChoice(
            "Sort Priority (Choose 1 or 2):\n"
                + "    1. HIGH_TO_LOW\n"
                + "    2. LOW_TO_HIGH\n"
                + "    Enter choice: ", "1|2");
        if (sortOrder == null) return;
        boolean ascending = "2".equals(sortOrder);

        VIPAllocationController.VIPAllocationDemandReport report =
            controller.generateVIPAllocationDemandReport(minimumTier, roomType, bookingStatus, ascending);
        DoublyLinkedList<Reservation> reservations = report.getReservations();

        System.out.println("\n+--------------------------------------------------------------------------------------------------+");
        System.out.println("                         VIP ALLOCATION DEMAND REPORT");
        System.out.println("+--------------------------------------------------------------------------------------------------+");
        System.out.println("  Filters: Minimum Tier = " + report.getMinimumTier()
                + " | Room Type = " + report.getRoomType()
                + " | Status = " + report.getBookingStatus());
        System.out.println("  Search: reservation registry scan  |  Sort: MergeSort by priority ("
            + (ascending ? "lowest first" : "highest first") + ")");
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
            if ("CANCEL".equals(choice) || "EXIT".equals(choice) || "B".equals(choice)
                    || "BACK".equals(choice) || "0".equals(choice) || "MAIN".equals(choice)) {
                System.out.println("  Report generation cancelled.");
                return null;
            }
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
  