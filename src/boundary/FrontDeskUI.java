package boundary;

import adt.DoublyLinkedList;
import control.FrontDeskController;
import entity.Guest;
import entity.Reservation;
import entity.Room;
import java.util.Scanner;

/**
 * Boundary: Front-Desk Service UI (Module 3).
 * Console search screen for guest lookup, check-in/out, and reservation listing.
 * All System.out / Scanner interactions live here — no business logic.
 */
public class FrontDeskUI {

    private FrontDeskController controller;
    private Scanner scanner;

    public FrontDeskUI(FrontDeskController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void show() {
        boolean exitToMainMenu = false;
        do {
            utility.UIUtils.clearScreen();
            utility.UIUtils.printModule3Header();
            displayMenu();
            int choice = utility.UIUtils.safeReadInt(scanner);

            switch (choice) {
                case 1:
                    searchReservation();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 2:
                    checkInGuest();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 3:
                    checkOutGuest();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 4:
                    cancelReservation();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 5:
                    viewAllReservations();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 6:
                    viewRoomStatus();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 7:
                    queryBillingDetails();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 8:
                    recordPayment();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 9:
                    viewStoredFrontDeskRecords();
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
        utility.UIUtils.printSectionHeader("GUEST SEARCH & CHECK-IN SERVICES", utility.UIUtils.GREEN);
        System.out.println("  " + utility.UIUtils.GREEN + utility.UIUtils.BOLD + "1." + utility.UIUtils.RESET + " Search Reservation (BST O(log n) Lookup)");
        System.out.println("  " + utility.UIUtils.GREEN + utility.UIUtils.BOLD + "2." + utility.UIUtils.RESET + " Check-In Guest");
        System.out.println("  " + utility.UIUtils.GREEN + utility.UIUtils.BOLD + "3." + utility.UIUtils.RESET + " Check-Out Guest");
        System.out.println("  " + utility.UIUtils.GREEN + utility.UIUtils.BOLD + "4." + utility.UIUtils.RESET + " Cancel Reservation");

        utility.UIUtils.printSectionHeader("SYSTEM RECORDS & INVENTORY", utility.UIUtils.GREEN);
        System.out.println("  " + utility.UIUtils.GREEN + utility.UIUtils.BOLD + "5." + utility.UIUtils.RESET + " View All Reservations (BST In-Order Sorted)");
        System.out.println("  " + utility.UIUtils.GREEN + utility.UIUtils.BOLD + "6." + utility.UIUtils.RESET + " View Room Availability Status");

        utility.UIUtils.printSectionHeader("BILLING QUERY", utility.UIUtils.GREEN);
        System.out.println("  " + utility.UIUtils.GREEN + utility.UIUtils.BOLD + "7." + utility.UIUtils.RESET + " Query Billing Details ");
        System.out.println("  " + utility.UIUtils.GREEN + utility.UIUtils.BOLD + "8." + utility.UIUtils.RESET + " Record Payment ");
        System.out.println("  " + utility.UIUtils.GREEN + utility.UIUtils.BOLD + "9." + utility.UIUtils.RESET + " View Stored Front-Desk Files (Filter + MergeSort)");

        utility.UIUtils.printSectionHeader("NAVIGATION", utility.UIUtils.RED);
        System.out.println("  " + utility.UIUtils.RED + utility.UIUtils.BOLD + "0." + utility.UIUtils.RESET + " Back to Main Menu");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.print(utility.UIUtils.BOLD + "Enter your choice: " + utility.UIUtils.RESET);
    }

    private void searchReservation() {
        utility.UIUtils.printSubHeader("MODULE 3 > SEARCH RESERVATION", utility.UIUtils.GREEN);
        DoublyLinkedList<Reservation> reservations = controller.getAllReservationsSorted();
        if (reservations.isEmpty()) {
            System.out.println("No reservations are currently available to search.");
            return;
        }
        displayReservationChoices("ALL RESERVATIONS", reservations, true);
        String confirmNo = promptConfirmationNo();
        if (confirmNo == null) {
            return;
        }

        Reservation res = controller.searchReservation(confirmNo);
        if (res == null) {
            System.out.println("No reservation found with Confirmation No: " + confirmNo);
        } else {
            displayReservationDetails(res);
        }
    }

    private void checkInGuest() {
        utility.UIUtils.printSubHeader("MODULE 3 > CHECK-IN GUEST", utility.UIUtils.GREEN);
        DoublyLinkedList<Reservation> eligible = controller.getReservationsByStatus("CONFIRMED");
        if (eligible.isEmpty()) {
            System.out.println("No confirmed bookings are currently available for check-in.");
            return;
        }
        displayEligibleReservations("BOOKINGS AVAILABLE FOR CHECK-IN", eligible);
        String confirmNo = promptConfirmationNo();
        if (confirmNo == null) {
            return;
        }

        Reservation res = controller.searchReservation(confirmNo);
        if (res == null) {
            System.out.println("No reservation found with Confirmation No: " + confirmNo);
            return;
        }

        if (!"CONFIRMED".equals(res.getBookingStatus())) {
            System.out.println("Cannot check in. Current status: " + res.getBookingStatus());
            System.out.println("Only CONFIRMED reservations can be checked in.");
            return;
        }

        if (!controller.isCheckInDateReached(res.getCheckInDate())) {
            System.out.println("Cannot check in before the booked check-in date: " + res.getCheckInDate());
            return;
        }

        Room assignedRoom = controller.findRoom(res.getAssignedRoomNo());
        if (assignedRoom == null) {
            System.out.println("Cannot check in. No room has been assigned to this reservation.");
            return;
        }
        if ("MAINTENANCE".equals(assignedRoom.getStatus())) {
            System.out.println("Cannot check in. Room " + assignedRoom.getRoomNo() + " is under maintenance.");
            return;
        }

        displayReservationDetails(res);
        System.out.print("\nConfirm check-in? (Y/N): ");
        String confirm = utility.UIUtils.safeReadLine(scanner).toUpperCase();

        if ("Y".equals(confirm)) {
            boolean success = controller.checkIn(confirmNo);
            if (success) {
                System.out.println("\n*** CHECK-IN SUCCESSFUL ***");
                System.out.println("Guest has been checked in.");
            } else {
                System.out.println("Check-in failed. The reservation must be CONFIRMED,");
                System.out.println("the check-in date must have been reached, and the assigned room must be usable.");
            }
        } else {
            System.out.println("Check-in cancelled.");
        }
    }

    private void checkOutGuest() {
        utility.UIUtils.printSubHeader("MODULE 3 > CHECK-OUT GUEST", utility.UIUtils.GREEN);
        DoublyLinkedList<Reservation> eligible = controller.getReservationsByStatus("CHECKED_IN");
        if (eligible.isEmpty()) {
            System.out.println("No checked-in bookings are currently available for check-out.");
            return;
        }
        displayEligibleReservations("ROOMS AVAILABLE FOR CHECK-OUT", eligible);
        String confirmNo = promptConfirmationNo();
        if (confirmNo == null) {
            return;
        }

        Reservation res = controller.searchReservation(confirmNo);
        if (res == null) {
            System.out.println("No reservation found with Confirmation No: " + confirmNo);
            return;
        }

        if (!"CHECKED_IN".equals(res.getBookingStatus())) {
            System.out.println("Cannot check out. Current status: " + res.getBookingStatus());
            System.out.println("Only CHECKED_IN guests can be checked out.");
            return;
        }

        displayReservationDetails(res);
        System.out.print("\nConfirm check-out? (Y/N): ");
        String confirm = utility.UIUtils.safeReadLine(scanner).toUpperCase();

        if ("Y".equals(confirm)) {
            System.out.print("Has payment been received? (Y = PAID / N = UNPAID): ");
            String paymentInput = utility.UIUtils.safeReadLine(scanner).toUpperCase();
            boolean paymentReceived = "Y".equals(paymentInput);
            Reservation checkedOut = controller.checkOut(confirmNo, paymentReceived);
            if (checkedOut != null) {
                System.out.println("\n*** CHECK-OUT SUCCESSFUL ***");
                System.out.println("Room " + checkedOut.getAssignedRoomNo() + " is now AVAILABLE.");
                System.out.println("Loyalty points accrued to Guest ID: " + checkedOut.getGuestId());
                System.out.println("Payment Status: " + (paymentReceived ? "PAID" : "UNPAID"));
                FrontDeskController.BillingDetails bill = controller.queryBillingDetails(confirmNo);
                if (bill != null) {
                    displayBillingDetails(bill);
                }
            } else {
                System.out.println("Check-out failed.");
            }
        } else {
            System.out.println("Check-out cancelled.");
        }
    }

    private void cancelReservation() {
        utility.UIUtils.printSubHeader("MODULE 3 > CANCEL RESERVATION", utility.UIUtils.GREEN);
        DoublyLinkedList<Reservation> eligible = controller.getCancellableReservations();
        if (eligible.isEmpty()) {
            System.out.println("No allocated bookings are currently available for cancellation.");
            return;
        }
        displayReservationChoices("BOOKINGS AVAILABLE FOR CANCELLATION", eligible, true);
        String confirmNo = promptConfirmationNo();
        if (confirmNo == null) {
            return;
        }

        Reservation res = controller.searchReservation(confirmNo);
        if (res == null) {
            System.out.println("No reservation found with Confirmation No: " + confirmNo);
            return;
        }

        if (!"PENDING".equals(res.getBookingStatus())
                && !"CONFIRMED".equals(res.getBookingStatus())) {
            System.out.println("Cannot cancel. Current status: " + res.getBookingStatus());
            return;
        }
        if ("PENDING".equals(res.getBookingStatus())
                && (res.getAssignedRoomNo() == null || res.getAssignedRoomNo().trim().isEmpty())) {
            System.out.println("Cannot cancel. This pending booking has not been allocated a room yet.");
            return;
        }

        displayReservationDetails(res);
        System.out.print("\nConfirm cancellation? (Y/N): ");
        String confirm = utility.UIUtils.safeReadLine(scanner).toUpperCase();

        if ("Y".equals(confirm)) {
            boolean success = controller.cancelReservation(confirmNo);
            if (success) {
                System.out.println("\n*** RESERVATION CANCELLED ***");
            } else {
                System.out.println("Cancellation failed.");
            }
        } else {
            System.out.println("Cancellation aborted.");
        }
    }

    private void viewAllReservations() {
        utility.UIUtils.printSubHeader("MODULE 3 > VIEW ALL RESERVATIONS (BST SORTED)", utility.UIUtils.GREEN);
        System.out.println("Total Reservations: " + controller.getReservationCount());

        DoublyLinkedList<Reservation> reservations = controller.getAllReservationsSorted();
        if (reservations.isEmpty()) {
            System.out.println("No reservations in the system.");
            return;
        }

        System.out.println("+-----+----------------+----------------+--------+-----------+-------------+---------+");
        System.out.println("| No. | Confirmation   | Guest ID       | Room   | Type      | Status      | Payment |");
        System.out.println("+-----+----------------+----------------+--------+-----------+-------------+---------+");

        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation res = reservations.getEntry(i);
            String roomNo = (res.getAssignedRoomNo() != null) ? res.getAssignedRoomNo() : "---";
            String payment = res.getPaymentStatus() == null ? "UNPAID" : res.getPaymentStatus();
            System.out.printf("| %-3d | %-14s | %-14s | %-6s | %-9s | %-11s | %-7s |%n",
                    i, res.getConfirmationNo(), res.getGuestId(),
                    roomNo, res.getRoomType(), res.getBookingStatus(), payment);
        }
        System.out.println("+-----+----------------+----------------+--------+-----------+-------------+---------+");
    }

    private void viewRoomStatus() {
        utility.UIUtils.printSubHeader("MODULE 3 > VIEW ROOM AVAILABILITY STATUS", utility.UIUtils.GREEN);
        DoublyLinkedList<Room> rooms = controller.getRoomInventory();

        System.out.println("+--------+-----------+---------------+-------------+");
        System.out.println("| Room   | Type      | Nightly Rate  | Status      |");
        System.out.println("+--------+-----------+---------------+-------------+");

        int available = 0, occupied = 0, maintenance = 0;
        for (int i = 1; i <= rooms.getNumberOfEntries(); i++) {
            Room room = rooms.getEntry(i);
            System.out.printf("| %-6s | %-9s | $%-12.2f | %-11s |%n",
                    room.getRoomNo(), room.getRoomType(),
                    room.getNightlyRate(), room.getStatus());
            if ("AVAILABLE".equals(room.getStatus())) available++;
            else if ("OCCUPIED".equals(room.getStatus())) occupied++;
            else maintenance++;
        }
        System.out.println("+--------+-----------+---------------+-------------+");
        System.out.printf("Summary: %d Available | %d Occupied | %d Maintenance%n",
                available, occupied, maintenance);
    }

    private void queryBillingDetails() {
        utility.UIUtils.printSubHeader("MODULE 3 > QUERY BILLING DETAILS", utility.UIUtils.GREEN);
        DoublyLinkedList<Reservation> reservations = controller.getAllReservationsSorted();
        if (reservations.isEmpty()) {
            System.out.println("No reservations are currently available to query.");
            return;
        }
        displayReservationChoices("ALL RESERVATIONS", reservations, true);
        String confirmNo = promptConfirmationNo();
        if (confirmNo == null) {
            return;
        }

        FrontDeskController.BillingDetails bill = controller.queryBillingDetails(confirmNo);
        if (bill == null) {
            System.out.println("No reservation found with Confirmation No: " + confirmNo);
            return;
        }
        displayBillingDetails(bill);
        if ("UNPAID".equals(bill.getPaymentStatus()) && !"VOID".equals(bill.getBillStatus())) {
            System.out.print("\nRecord this bill as PAID now? (Y/N): ");
            String payNow = utility.UIUtils.safeReadLine(scanner).toUpperCase();
            if ("Y".equals(payNow)) {
                if (controller.recordPayment(confirmNo)) {
                    System.out.println("Payment recorded. Status is now PAID.");
                    FrontDeskController.BillingDetails updated = controller.queryBillingDetails(confirmNo);
                    if (updated != null) {
                        displayBillingDetails(updated);
                    }
                } else {
                    System.out.println("Unable to record payment for this confirmation number.");
                }
            }
        }
    }

    private void recordPayment() {
        utility.UIUtils.printSubHeader("MODULE 3 > RECORD PAYMENT", utility.UIUtils.GREEN);
        DoublyLinkedList<Reservation> unpaid = controller.getUnpaidReservations();
        if (unpaid.isEmpty()) {
            System.out.println("There are no unpaid bills to record.");
            return;
        }
        displayReservationChoices("UNPAID BILLS", unpaid, true);
        String confirmNo = promptConfirmationNo();
        if (confirmNo == null) {
            return;
        }

        FrontDeskController.BillingDetails bill = controller.queryBillingDetails(confirmNo);
        if (bill == null) {
            System.out.println("No reservation found with Confirmation No: " + confirmNo);
            return;
        }
        displayBillingDetails(bill);

        if ("PAID".equals(bill.getPaymentStatus())) {
            System.out.println("This bill is already PAID.");
            return;
        }
        if ("VOID".equals(bill.getBillStatus())) {
            System.out.println("Cancelled bookings have no payment due.");
            return;
        }

        System.out.print("\nConfirm payment received? (Y/N): ");
        String confirm = utility.UIUtils.safeReadLine(scanner).toUpperCase();
        if ("Y".equals(confirm)) {
            if (controller.recordPayment(confirmNo)) {
                System.out.println("\n*** PAYMENT RECORDED ***");
                System.out.println("Confirmation No " + confirmNo + " is now PAID.");
            } else {
                System.out.println("Payment could not be recorded.");
            }
        } else {
            System.out.println("Payment recording aborted.");
        }
    }

    private void viewStoredFrontDeskRecords() {
        utility.UIUtils.printSubHeader("MODULE 3 > STORED FRONT-DESK FILES", utility.UIUtils.GREEN);
        String roomType = readReportChoice("Room type (ALL | STANDARD | DELUXE | SUITE): ",
                "ALL|STANDARD|DELUXE|SUITE");
        String recordStatus = readReportChoice(
                "Status (ALL | CHECK_IN | CHECK_OUT | CANCELLED): ",
                "ALL|CHECK_IN|CHECK_OUT|CANCELLED");
        String paymentStatus = readReportChoice("Payment status (ALL | PAID | UNPAID): ",
                "ALL|PAID|UNPAID");

        FrontDeskController.StoredFrontDeskReport report =
                controller.generateStoredFrontDeskReport(roomType, recordStatus, paymentStatus);
        DoublyLinkedList<FrontDeskController.StoredFrontDeskRecord> records = report.getRecords();

        System.out.println("\n+----------------------------------------------------------------------------------------------------------+");
        System.out.println("                              STORED FRONT-DESK FILES REPORT");
        System.out.println("+----------------------------------------------------------------------------------------------------------+");
        System.out.println("  Filters: Room Type = " + report.getRoomType()
                + " | Status = " + report.getRecordStatus()
                + " | Payment = " + report.getPaymentStatus());
        System.out.println("  Search: combined stored files  |  Sort: MergeSort by status, payment, confirmation");
        System.out.println("+-----+--------------+----------------+--------+-----------+------------+---------+------------+");
        System.out.println("| No. | Confirmation | Guest          | Room   | Type      | Status     | Payment | Total      |");
        System.out.println("+-----+--------------+----------------+--------+-----------+------------+---------+------------+");
        for (int i = 1; i <= records.getNumberOfEntries(); i++) {
            FrontDeskController.StoredFrontDeskRecord record = records.getEntry(i);
            System.out.printf("| %-3d | %-12s | %-14s | %-6s | %-9s | %-10s | %-7s | $%-9.2f |%n",
                    i,
                    emptyDash(record.getConfirmationNo()),
                    truncate(record.getGuestName(), 14),
                    emptyDash(record.getRoomNo()),
                    emptyDash(record.getRoomType()),
                    emptyDash(record.getRecordStatus()),
                    emptyDash(record.getPaymentStatus()),
                    record.getGrandTotal());
        }
        if (records.isEmpty()) {
            System.out.println("|                    No stored front-desk records match the selected filters.                   |");
        }
        System.out.println("+-----+--------------+----------------+--------+-----------+------------+---------+------------+");
        System.out.println("  Total Records : " + report.getTotalRecords()
                + " | Check-In: " + report.getCheckInCount()
                + " | Check-Out: " + report.getCheckOutCount()
                + " | Cancelled: " + report.getCancelledCount());
        System.out.println("  Payment       : Paid=" + report.getPaidCount()
                + " | Unpaid=" + report.getUnpaidCount());
        System.out.println("+----------------------------------------------------------------------------------------------------------+");
    }

    private String readReportChoice(String prompt, String permittedChoices) {
        while (true) {
            System.out.print("  " + prompt);
            String choice = utility.UIUtils.safeReadLine(scanner).trim().toUpperCase();
            if (("|" + permittedChoices + "|").contains("|" + choice + "|")) {
                return choice;
            }
            System.out.println("  [!] Invalid option. Choose from: " + permittedChoices.replace("|", " | "));
        }
    }

    private String emptyDash(String value) {
        return (value == null || value.trim().isEmpty()) ? "---" : value;
    }

    private String truncate(String value, int max) {
        String text = emptyDash(value);
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max);
    }

    private void displayBillingDetails(FrontDeskController.BillingDetails bill) {
        System.out.println("\n+----------------------------------------------------+");
        System.out.println("                   BILLING DETAILS");
        System.out.println("+----------------------------------------------------+");
        System.out.println("  Confirmation No : " + bill.getConfirmationNo());
        System.out.println("  Bill Status     : " + bill.getBillStatus());
        System.out.println("  Payment Status  : " + bill.getPaymentStatus());
        System.out.println("  Booking Status  : " + bill.getBookingStatus());
        System.out.println("+----------------------------------------------------+");
        System.out.println("  Guest ID        : " + bill.getGuestId());
        System.out.println("  Guest Name      : " + bill.getGuestName());
        System.out.println("  Loyalty Tier    : " + bill.getLoyaltyTier());
        System.out.println("+----------------------------------------------------+");
        System.out.println("  Room Number     : " + bill.getRoomNo());
        System.out.println("  Room Type       : " + bill.getRoomType());
        System.out.println("  Check-In Date   : " + bill.getCheckInDate());
        System.out.println("  Check-Out Date  : " + bill.getCheckOutDate());
        System.out.println("  Stay Duration   : " + bill.getNights()
                + (bill.getNights() == 1 ? " night" : " nights"));
        System.out.println("+----------------------------------------------------+");
        System.out.printf("  Nightly Rate    : $%.2f%n", bill.getNightlyRate());
        System.out.printf("  Room Charges    : $%.2f  (%d x $%.2f)%n",
                bill.getRoomCharges(), bill.getNights(), bill.getNightlyRate());
        System.out.printf("  Loyalty Discount: -$%.2f  (%.0f%% %s)%n",
                bill.getDiscountAmount(), bill.getDiscountRate() * 100, bill.getLoyaltyTier());
        System.out.printf("  SST (%.0f%%)        : $%.2f%n", bill.getTaxRate() * 100, bill.getTaxAmount());
        System.out.println("+----------------------------------------------------+");
        System.out.printf("  GRAND TOTAL     : $%.2f%n", bill.getGrandTotal());
        if ("VOID".equals(bill.getBillStatus())) {
            System.out.println("  Note            : Cancelled booking — no payment due.");
        } else if ("UNPAID".equals(bill.getPaymentStatus())) {
            System.out.println("  Note            : Balance outstanding — status UNPAID.");
        } else {
            System.out.println("  Note            : Payment received — status PAID.");
        }
        System.out.println("+----------------------------------------------------+");
    }

    private void displayReservationDetails(Reservation res) {
        Guest guest = controller.findGuest(res.getGuestId());
        Room room = controller.findRoom(res.getAssignedRoomNo());

        System.out.println("\n+------------------------------------------+");
        System.out.println("         RESERVATION DETAILS");
        System.out.println("+------------------------------------------+");
        System.out.println("  Confirmation No : " + res.getConfirmationNo());
        System.out.println("  Booking Status  : " + res.getBookingStatus());
        System.out.println("  Payment Status  : " + (res.getPaymentStatus() == null ? "UNPAID" : res.getPaymentStatus()));
        System.out.println("+------------------------------------------+");
        System.out.println("  GUEST INFORMATION");
        if (guest != null) {
            System.out.println("  Guest ID        : " + guest.getGuestId());
            System.out.println("  Name            : " + guest.getName());
            System.out.println("  IC/Passport     : " + guest.getIcPassport());
            System.out.println("  Contact         : " + guest.getContactNo());
            System.out.println("  Email           : " + guest.getEmail());
            System.out.println("  Loyalty Tier    : " + guest.getLoyaltyTier());
        } else {
            System.out.println("  Guest ID        : " + res.getGuestId());
        }
        System.out.println("+------------------------------------------+");
        System.out.println("  ROOM & STAY DETAILS");
        System.out.println("  Room Type       : " + res.getRoomType());
        if (room != null) {
            System.out.println("  Room Number     : " + room.getRoomNo());
            System.out.println("  Nightly Rate    : $" + String.format("%.2f", room.getNightlyRate()));
            System.out.println("  Room Status     : " + room.getStatus());
        } else {
            System.out.println("  Room Number     : " + (res.getAssignedRoomNo() != null ? res.getAssignedRoomNo() : "Not Assigned"));
        }
        System.out.println("  Check-In Date   : " + res.getCheckInDate());
        System.out.println("  Check-Out Date  : " + res.getCheckOutDate());
        System.out.println("  Priority Score  : " + res.getPriorityScore());
        System.out.println("+------------------------------------------+");
    }

    private void displayEligibleReservations(String title,
                                              DoublyLinkedList<Reservation> reservations) {
        displayReservationChoices(title, reservations, false);
    }

    private void displayReservationChoices(String title,
                                             DoublyLinkedList<Reservation> reservations,
                                             boolean showStatus) {
        System.out.println("\n" + title);
        if (showStatus) {
            System.out.println("+-----+----------------+----------+-----------+----------------+-------------+");
            System.out.println("| No. | Confirmation ID| Room No. | Room Type | Guest ID       | Status      |");
            System.out.println("+-----+----------------+----------+-----------+----------------+-------------+");
        } else {
            System.out.println("+-----+----------------+----------+-----------+----------------+");
            System.out.println("| No. | Confirmation ID| Room No. | Room Type | Guest ID       |");
            System.out.println("+-----+----------------+----------+-----------+----------------+");
        }
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            String roomNo = reservation.getAssignedRoomNo() == null
                    ? "---" : reservation.getAssignedRoomNo();
            if (showStatus) {
                System.out.printf("| %-3d | %-14s | %-8s | %-9s | %-14s | %-11s |%n",
                        i, reservation.getConfirmationNo(), roomNo, reservation.getRoomType(),
                        reservation.getGuestId(), reservation.getBookingStatus());
            } else {
                System.out.printf("| %-3d | %-14s | %-8s | %-9s | %-14s |%n",
                        i, reservation.getConfirmationNo(), roomNo,
                        reservation.getRoomType(), reservation.getGuestId());
            }
        }
        if (showStatus) {
            System.out.println("+-----+----------------+----------+-----------+----------------+-------------+");
        } else {
            System.out.println("+-----+----------------+----------+-----------+----------------+");
        }
        System.out.println();
    }

    private String promptConfirmationNo() {
        System.out.print("Enter Confirmation No (8 digits): ");
        String confirmNo = utility.UIUtils.safeReadLine(scanner).trim();
        if (!controller.isValidConfirmationNo(confirmNo)) {
            System.out.println("Invalid confirmation number. Please enter an 8-digit number.");
            return null;
        }
        return confirmNo;
    }
}
