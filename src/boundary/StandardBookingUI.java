package boundary;

import adt.DoublyLinkedList;
import control.StandardBookingController;
import entity.Guest;
import entity.Reservation;
import java.util.Scanner;

/**
 * Boundary: Standard Booking UI (Module 1).
 * Console forms for walk-in registrations and standard booking queue operations.
 * All System.out / Scanner interactions live here — no business logic.
 */
public class StandardBookingUI {

    private StandardBookingController controller;
    private Scanner scanner;

    public StandardBookingUI(StandardBookingController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void show() {
        boolean exitToMainMenu = false;
        do {
            utility.UIUtils.clearScreen();
            utility.UIUtils.printModule1Header();
            displayMenu();
            int choice = utility.UIUtils.safeReadInt(scanner);

            switch (choice) {
                case 1:
                    if (registerWalkIn()) {
                        exitToMainMenu = true;
                    } else {
                        exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    }
                    break;
                case 2:
                    processNextBooking();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 3:
                    undoLastOperationUI();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 4:
                    modifyPendingBooking();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 5:
                    cancelPendingBooking();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 6:
                    registerNewGuestUI();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 7:
                    peekNextBooking();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 8:
                    viewQueue();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 9:
                    generateRevenueReport();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 10:
                    generateQueuePerformanceReport();
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
        utility.UIUtils.printSectionHeader("REGISTRATION & PROCESSING", utility.UIUtils.CYAN);
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "1." + utility.UIUtils.RESET + " Create Standard Booking (Guest ID)");
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "2." + utility.UIUtils.RESET + " Process Next Booking (Assign Room)");
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "3." + utility.UIUtils.RESET + " Undo Last Operation (Stack ADT)");

        utility.UIUtils.printSectionHeader("REGISTRATION MANAGEMENT", utility.UIUtils.CYAN);
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "4." + utility.UIUtils.RESET + " Modify Pending Booking");
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "5." + utility.UIUtils.RESET + " Cancel Pending Booking");
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "6." + utility.UIUtils.RESET + " Register New Guest / Member");

        utility.UIUtils.printSectionHeader("QUEUE MONITORING", utility.UIUtils.CYAN);
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "7." + utility.UIUtils.RESET + " Peek Next Guest in Queue");
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "8." + utility.UIUtils.RESET + " View Entire Pending Queue");

        utility.UIUtils.printSectionHeader("REPORTS & ANALYTICS", utility.UIUtils.YELLOW);
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "9." + utility.UIUtils.RESET + " Reservations Revenue Analysis Report (MergeSort)");
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "10." + utility.UIUtils.RESET + " Standard Queue Performance & Shortage Report");

        utility.UIUtils.printSectionHeader("NAVIGATION", utility.UIUtils.RED);
        System.out.println("  " + utility.UIUtils.RED + utility.UIUtils.BOLD + "0." + utility.UIUtils.RESET + " Back to Main Menu");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.print(utility.UIUtils.BOLD + "Enter your choice: " + utility.UIUtils.RESET);
    }

    private boolean registerWalkIn() {
        utility.UIUtils.printSubHeader("MODULE 1 > CREATE STANDARD BOOKING", utility.UIUtils.CYAN);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");

        String roomType = "";
        String checkIn = "";
        String checkOut = "";


        utility.StepResult guestIdResult = utility.ValidationUtils.readValidStringStep(
                scanner, "Guest ID", "", false);
        if (guestIdResult.isQuitToMain()) return true;
        if (guestIdResult.isGoBack() || guestIdResult.isCancel()) return false;

        Guest guest = controller.findGuestById(guestIdResult.getValue());
        if (guest == null) {
            System.out.print("\n  [!] Guest ID not found. Would you like to register a new guest now? (Y/N): ");
            String regChoice = utility.UIUtils.safeReadLine(scanner).trim();
            if ("Y".equalsIgnoreCase(regChoice)) {
                guest = registerNewGuestUI();
                if (guest == null) {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (guest.isVIP()) {
            System.out.println("\n  [!] " + guest.getName() + " is VIP eligible. Create this booking through Module 2.");
            return false;
        }

        System.out.println("\n  Guest details loaded:");
        System.out.println("  Guest ID        : " + guest.getGuestId());
        System.out.println("  Name            : " + guest.getName());
        System.out.println("  IC / Passport   : " + guest.getIcPassport());
        System.out.println("  Contact Number  : " + guest.getContactNo());
        System.out.println("  Email           : " + guest.getEmail());
        System.out.println("  Loyalty Tier    : " + guest.getLoyaltyTier());
        System.out.println("\n  Only the room and stay dates are required.\n");

        printRoomCatalogueTable();

        int step = 0;
        while (step >= 0 && step <= 2) {
            switch (step) {
                case 0: {
                    utility.StepResult res = utility.ValidationUtils.readValidDateStep(scanner, "Step 1/3 - Check-In (YYYY-MM-DD) ", checkIn);
                    if (res.isGoBack()) return false;
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    String val = res.getValue();
                    try {
                        java.time.LocalDate date = java.time.LocalDate.parse(val);
                        if (date.isBefore(java.time.LocalDate.now())) {
                            System.out.println(utility.UIUtils.RED + "  [!] ERROR: Check-in date cannot be in the past (before " + java.time.LocalDate.now() + ")." + utility.UIUtils.RESET);
                            break; // Repeat Step 0
                        }
                    } catch (Exception e) {
                        System.out.println(utility.UIUtils.RED + "  [!] ERROR: Invalid date format." + utility.UIUtils.RESET);
                        break; // Repeat Step 0
                    }
                    checkIn = val;
                    step++;
                    break;
                }
                case 1: {
                    utility.StepResult res = utility.ValidationUtils.readValidCheckOutDateStep(scanner, "Step 2/3 - Check-Out Date   ", checkIn, checkOut);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    checkOut = res.getValue();
                    step++;
                    break;
                }
                case 2: {
                    System.out.println("  Room Options: STANDARD | DELUXE | SUITE");
                    utility.StepResult res = utility.ValidationUtils.readValidRoomTypeStep(scanner, "Step 3/3 - Preferred Room   ", roomType);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    roomType = res.getValue();
                    step++;
                    break;
                }
            }
        }

        if (step < 0) {
            System.out.println("\n  [!] Booking creation cancelled. No data saved.");
            return false;
        }

        // Double-booking check
        if (guest != null) {
            Reservation overlap = controller.findOverlappingReservation(guest.getGuestId(), checkIn, checkOut);
            if (overlap != null) {
                System.out.println(utility.UIUtils.RED + "\n  [!] DOUBLE-BOOKING DETECTED!" + utility.UIUtils.RESET);
                System.out.println("  Guest already has an active booking for overlapping dates:");
                System.out.println("  Confirmation No : " + overlap.getConfirmationNo());
                System.out.println("  Room Type       : " + overlap.getRoomType());
                System.out.println("  Check-In Date   : " + overlap.getCheckInDate());
                System.out.println("  Check-Out Date  : " + overlap.getCheckOutDate());
                System.out.println("  Booking Status  : " + overlap.getBookingStatus());
                System.out.print("\n  Are you sure you want to proceed with this additional booking? (Y/N): ");
                String proceed = utility.UIUtils.safeReadLine(scanner).trim();
                if (!"Y".equalsIgnoreCase(proceed)) {
                    System.out.println("\n  [!] Booking creation cancelled due to double-booking detection.");
                    return false;
                }
            }
        }

        Reservation res = controller.registerBookingForGuest(guest, roomType, checkIn, checkOut);

        System.out.println("\n+------------------------------------------+");
        System.out.println("  BOOKING REGISTERED SUCCESSFULLY!");
        System.out.println("+------------------------------------------+");
        System.out.println("  Confirmation No : " + res.getConfirmationNo());
        System.out.println("  Guest ID        : " + res.getGuestId());
        System.out.println("  Room Type       : " + res.getRoomType());
        System.out.println("  Check-In        : " + res.getCheckInDate());
        System.out.println("  Check-Out       : " + res.getCheckOutDate());
        System.out.println("  Status          : " + res.getBookingStatus());
        if ("CONFIRMED".equals(res.getBookingStatus())) {
            System.out.println("  Room Assigned   : " + res.getAssignedRoomNo());
        } else {
            System.out.println("  Queue Position  : " + controller.getQueueSize());
        }
        System.out.println("+------------------------------------------+");
        return false;
    }

    private void printRoomCatalogueTable() {
        System.out.println("\n=========================================================================================");
        System.out.println("                              TARUMT RESORTS - ROOM CATALOGUE");
        System.out.println("=========================================================================================");
        System.out.printf("| %-12s | %-12s | %-12s | %-45s |%n", "Room Type", "Price/Night", "Total Rooms", "Reserved Stay Dates (Today onwards)");
        System.out.println("+--------------+--------------+--------------+-----------------------------------------------+");
        
        printCatalogRow("STANDARD", "RM100.00");
        System.out.println("+--------------+--------------+--------------+-----------------------------------------------+");
        printCatalogRow("DELUXE", "RM200.00");
        System.out.println("+--------------+--------------+--------------+-----------------------------------------------+");
        printCatalogRow("SUITE", "RM500.00");
        System.out.println("=========================================================================================\n");
    }

    private void printCatalogRow(String roomType, String price) {
        int totalRooms = controller.getTotalRoomCount(roomType);
        adt.DoublyLinkedList<control.StandardBookingController.DateRange> list = controller.getOccupiedDateRanges(roomType);
        
        if (list.isEmpty()) {
            System.out.printf("| %-12s | %-12s | %-12d | %-45s |%n", roomType, price, totalRooms, "None (All dates fully available)");
        } else {
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                control.StandardBookingController.DateRange r = list.getEntry(i);
                String rangeStr = "* " + r.getCheckIn() + " to " + r.getCheckOut() + " (" + r.getCount() + " booked)";
                if (i == 1) {
                    System.out.printf("| %-12s | %-12s | %-12d | %-45s |%n", roomType, price, totalRooms, rangeStr);
                } else {
                    System.out.printf("| %-12s | %-12s | %-12s | %-45s |%n", "", "", "", rangeStr);
                }
            }
        }
    }

    private void processNextBooking() {
        utility.UIUtils.printSubHeader("MODULE 1 > PROCESS NEXT BOOKING (ROOM ASSIGNMENT)", utility.UIUtils.CYAN);

        Reservation peekRes = controller.peekNextBooking();
        if (peekRes == null) {
            System.out.println("  [!] The pending queue is EMPTY. No bookings to process.");
            return;
        }

        boolean isVIP = peekRes.getPriorityScore() > 0 || (peekRes.getGuest() != null && peekRes.getGuest().isVIP());
        System.out.println("  Next Pending Reservation Details:");
        System.out.println("  Queue Category  : " + (isVIP ? utility.UIUtils.YELLOW + "VIP Priority Queue (Max-Heap)" + utility.UIUtils.RESET : "Standard Queue (FIFO)"));
        System.out.println("  Confirmation No : " + peekRes.getConfirmationNo());
        System.out.println("  Guest ID        : " + peekRes.getGuestId());
        System.out.println("  Guest Name      : " + (peekRes.getGuest() != null ? peekRes.getGuest().getName() : "N/A"));
        if (isVIP && peekRes.getGuest() != null) {
            System.out.println("  Loyalty Tier    : " + peekRes.getGuest().getLoyaltyTier() + " (Priority Score: " + peekRes.getPriorityScore() + ")");
        }
        System.out.println("  Requested Room  : " + peekRes.getRoomType());
        System.out.println("  Stay Dates      : " + peekRes.getCheckInDate() + " to " + peekRes.getCheckOutDate());
        System.out.println();

        System.out.print(utility.UIUtils.YELLOW + "  Assign room for this guest now? (Y/N): " + utility.UIUtils.RESET);
        String confirm = utility.UIUtils.safeReadLine(scanner).trim();
        if (!"Y".equalsIgnoreCase(confirm)) {
            System.out.println("  [!] Room assignment operation cancelled. Guest remains in queue.");
            return;
        }

        Reservation res = controller.processNextBooking();
        if (res != null) {
            System.out.println("\n+------------------------------------------+");
            if ("CONFIRMED".equals(res.getBookingStatus())) {
                System.out.println(utility.UIUtils.GREEN + "  ROOM ASSIGNED SUCCESSFULLY!" + utility.UIUtils.RESET);
                System.out.println("+------------------------------------------+");
                System.out.println("  Confirmation No : " + res.getConfirmationNo());
                System.out.println("  Guest ID        : " + res.getGuestId());
                System.out.println("  Guest Name      : " + (res.getGuest() != null ? res.getGuest().getName() : "N/A"));
                System.out.println("  Room Assigned   : " + utility.UIUtils.GREEN + res.getAssignedRoomNo() + utility.UIUtils.RESET);
                System.out.println("  Room Type       : " + res.getRoomType());
                System.out.println("  Status          : " + res.getBookingStatus());
            } else {
                System.out.println(utility.UIUtils.RED + "  NO ROOMS AVAILABLE FOR REQUESTED DATES" + utility.UIUtils.RESET);
                System.out.println("+------------------------------------------+");
                System.out.println("  Confirmation No : " + res.getConfirmationNo());
                System.out.println("  Guest remains PENDING in queue.");
                System.out.println("  Status          : " + res.getBookingStatus());
            }
            System.out.println("+------------------------------------------+");
        }
    }

    private void undoLastOperationUI() {
        utility.UIUtils.printSubHeader("MODULE 1 > UNDO LAST OPERATION (LIFO STACK ADT)", utility.UIUtils.YELLOW);

        if (controller.getUndoController() == null || controller.getUndoController().isQueueEmpty()) {
            System.out.println("  [!] No operations available on the Undo Stack.");
            return;
        }

        entity.UndoAction action = controller.getUndoController().peekNextUndo();
        System.out.println("  Operation on Top of Stack (LIFO):");
        System.out.println("  Action Type     : " + action.getActionType());
        System.out.println("  Module Source   : " + action.getModuleName());
        System.out.println("  Description     : " + utility.UIUtils.CYAN + action.getDescription() + utility.UIUtils.RESET);
        System.out.println();

        System.out.print(utility.UIUtils.YELLOW + "  Are you sure you want to UNDO and reverse this operation? (Y/N): " + utility.UIUtils.RESET);
        String confirm = utility.UIUtils.safeReadLine(scanner).trim();
        if (!"Y".equalsIgnoreCase(confirm)) {
            System.out.println("  [!] Undo operation cancelled. Stack unchanged.");
            return;
        }

        entity.UndoAction executed = controller.getUndoController().processNextUndo();
        if (executed != null) {
            System.out.println(utility.UIUtils.GREEN + "  [+] Operation undone successfully! System state restored." + utility.UIUtils.RESET);
        } else {
            System.out.println(utility.UIUtils.RED + "  [!] Failed to execute undo operation." + utility.UIUtils.RESET);
        }
    }

    private void peekNextBooking() {
        utility.UIUtils.printSubHeader("MODULE 1 > PEEK NEXT GUEST IN QUEUE", utility.UIUtils.CYAN);

        Reservation res = controller.peekNextBooking();
        if (res == null) {
            System.out.println("  [!] The pending queue is EMPTY.");
        } else {
            boolean isVIP = res.getPriorityScore() > 0 || (res.getGuest() != null && res.getGuest().isVIP());
            System.out.println("  Queue Category  : " + (isVIP ? utility.UIUtils.YELLOW + "VIP Priority Queue (Max-Heap)" + utility.UIUtils.RESET : "Standard Queue (FIFO)"));
            System.out.println("  Next Guest      : " + (res.getGuest() != null ? res.getGuest().getName() : res.getGuestId()));
            if (isVIP && res.getGuest() != null) {
                System.out.println("  Loyalty Tier    : " + res.getGuest().getLoyaltyTier() + " (Priority Score: " + res.getPriorityScore() + ")");
            }
            System.out.println("  Confirmation No : " + res.getConfirmationNo());
            System.out.println("  Room Type       : " + res.getRoomType());
            System.out.println("  Stay Dates      : " + res.getCheckInDate() + " to " + res.getCheckOutDate());
            System.out.println("  Status          : " + res.getBookingStatus());
        }
    }

    private void viewQueue() {
        utility.UIUtils.printSubHeader("MODULE 1 > VIEW ALL PENDING BOOKING QUEUES", utility.UIUtils.CYAN);
        DoublyLinkedList<Reservation> list = controller.getAllPendingReservations();
        System.out.println("Total Pending Queue Size: " + (list != null ? list.getNumberOfEntries() : 0));

        if (list == null || list.isEmpty()) {
            System.out.println("The pending queue is empty.");
            return;
        }

        System.out.println("+-----+----------------+----------------+----------------------+-----------+------------+------------+");
        System.out.println("| No. | Confirmation   | Guest ID       | Guest Name           | Room Type | Priority   | Status     |");
        System.out.println("+-----+----------------+----------------+----------------------+-----------+------------+------------+");

        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            Reservation res = list.getEntry(i);
            String guestName = res.getGuest() != null ? res.getGuest().getName() : res.getGuestId();
            String priorityStr = res.getPriorityScore() > 0 ? String.valueOf(res.getPriorityScore()) : "0 (FIFO)";
            System.out.printf("| %-3d | %-14s | %-14s | %-20s | %-9s | %-10s | %-10s |%n",
                    i, res.getConfirmationNo(), res.getGuestId(), guestName,
                    res.getRoomType(), priorityStr, res.getBookingStatus());
        }
        System.out.println("+-----+----------------+----------------+----------------------+-----------+------------+------------+");
    }

    private void modifyPendingBooking() {
        utility.UIUtils.printSubHeader("MODULE 1 > MODIFY PENDING BOOKING", utility.UIUtils.CYAN);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type '0' or 'b' or 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");
        displayPendingReservationsList();
        
        Reservation res = null;
        String confNo = "";
        
        while (true) {
            System.out.print("Enter Confirmation Number: ");
            confNo = utility.UIUtils.safeReadLine(scanner).trim();
            
            if ("0".equals(confNo) || "b".equalsIgnoreCase(confNo) || "back".equalsIgnoreCase(confNo) || "cancel".equalsIgnoreCase(confNo)) {
                System.out.println("  Operation cancelled.");
                return;
            }
            
            if (confNo.isEmpty()) {
                continue;
            }
            
            res = controller.findReservationByConfNo(confNo);
            if (res == null) {
                System.out.println(utility.UIUtils.RED + "  [!] ERROR: Reservation with Confirmation No " + confNo + " not found. Please try again." + utility.UIUtils.RESET);
                continue;
            }
            
            if (!"PENDING".equals(res.getBookingStatus())) {
                System.out.println(utility.UIUtils.RED + "  [!] ERROR: Only PENDING reservations can be modified (Status is currently: " + res.getBookingStatus() + "). Please try again." + utility.UIUtils.RESET);
                continue;
            }
            
            break;
        }
        
        System.out.println("\n  Current Details:");
        System.out.println("  Guest ID        : " + res.getGuestId());
        System.out.println("  Guest Name      : " + (res.getGuest() != null ? res.getGuest().getName() : "N/A"));
        System.out.println("  Room Type       : " + res.getRoomType());
        System.out.println("  Check-In        : " + res.getCheckInDate());
        System.out.println("  Check-Out       : " + res.getCheckOutDate());
        System.out.println();
        
        System.out.println("  Enter new details (leave blank to keep current):");
        
        // 1. Room type modification
        System.out.println("  Room Options: STANDARD | DELUXE | SUITE");
        System.out.print("  New Room Type [" + res.getRoomType() + "]: ");
        String roomType = utility.UIUtils.safeReadLine(scanner).toUpperCase().trim();
        if ("0".equals(roomType) || "CANCEL".equals(roomType) || "B".equals(roomType) || "BACK".equals(roomType)) {
            System.out.println("  Modification cancelled.");
            return;
        }
        if (roomType.isEmpty()) {
            roomType = res.getRoomType();
        } else {
            while (!roomType.equals("STANDARD") && !roomType.equals("DELUXE") && !roomType.equals("SUITE")) {
                System.out.println("  [!] ERROR: Invalid room type. Choose STANDARD | DELUXE | SUITE.");
                System.out.print("  New Room Type [" + res.getRoomType() + "]: ");
                roomType = utility.UIUtils.safeReadLine(scanner).toUpperCase().trim();
                if ("0".equals(roomType) || "CANCEL".equals(roomType) || "B".equals(roomType) || "BACK".equals(roomType)) {
                    System.out.println("  Modification cancelled.");
                    return;
                }
                if (roomType.isEmpty()) {
                    roomType = res.getRoomType();
                    break;
                }
            }
        }
        
        // 2. Check-In Date
        System.out.print("  New Check-In (YYYY-MM-DD) [" + res.getCheckInDate() + "]: ");
        String checkIn = utility.UIUtils.safeReadLine(scanner).trim();
        if ("0".equals(checkIn) || "cancel".equalsIgnoreCase(checkIn) || "b".equalsIgnoreCase(checkIn) || "back".equalsIgnoreCase(checkIn)) {
            System.out.println("  Modification cancelled.");
            return;
        }
        if (checkIn.isEmpty()) {
            checkIn = res.getCheckInDate();
        } else {
            while (!utility.ValidationUtils.isValidDate(checkIn)) {
                System.out.println("  [!] ERROR: Invalid date format. Enter in YYYY-MM-DD format.");
                System.out.print("  New Check-In (YYYY-MM-DD) [" + res.getCheckInDate() + "]: ");
                checkIn = utility.UIUtils.safeReadLine(scanner).trim();
                if ("0".equals(checkIn) || "cancel".equalsIgnoreCase(checkIn) || "b".equalsIgnoreCase(checkIn) || "back".equalsIgnoreCase(checkIn)) {
                    System.out.println("  Modification cancelled.");
                    return;
                }
                if (checkIn.isEmpty()) {
                    checkIn = res.getCheckInDate();
                    break;
                }
            }
        }
        
        // 3. Check-Out Date
        System.out.print("  New Check-Out Date (YYYY-MM-DD) [" + res.getCheckOutDate() + "]: ");
        String checkOut = utility.UIUtils.safeReadLine(scanner).trim();
        if ("0".equals(checkOut) || "cancel".equalsIgnoreCase(checkOut) || "b".equalsIgnoreCase(checkOut) || "back".equalsIgnoreCase(checkOut)) {
            System.out.println("  Modification cancelled.");
            return;
        }
        if (checkOut.isEmpty()) {
            checkOut = res.getCheckOutDate();
        } else {
            while (true) {
                if (!utility.ValidationUtils.isValidDate(checkOut)) {
                    System.out.println("  [!] ERROR: Invalid date format. Enter in YYYY-MM-DD format.");
                } else if (!java.time.LocalDate.parse(checkOut).isAfter(java.time.LocalDate.parse(checkIn))) {
                    System.out.println("  [!] ERROR: Check-out date must be strictly after check-in date (" + checkIn + ").");
                } else {
                    break;
                }
                System.out.print("  New Check-Out Date (YYYY-MM-DD) [" + res.getCheckOutDate() + "]: ");
                checkOut = utility.UIUtils.safeReadLine(scanner).trim();
                if ("0".equals(checkOut) || "cancel".equalsIgnoreCase(checkOut) || "b".equalsIgnoreCase(checkOut) || "back".equalsIgnoreCase(checkOut)) {
                    System.out.println("  Modification cancelled.");
                    return;
                }
                if (checkOut.isEmpty()) {
                    checkOut = res.getCheckOutDate();
                    break;
                }
            }
        }
        
        // Check for double booking conflict if stay dates are changed
        Reservation overlap = controller.findOverlappingReservation(res.getGuestId(), checkIn, checkOut);
        if (overlap != null && !overlap.getConfirmationNo().equals(res.getConfirmationNo())) {
            System.out.println(utility.UIUtils.RED + "  [!] ERROR: Cannot modify. Overlap detected with existing Conf #" + overlap.getConfirmationNo() + "." + utility.UIUtils.RESET);
            return;
        }
        
        System.out.print("\n  Confirm modification? (Y/N): ");
        if ("Y".equalsIgnoreCase(utility.UIUtils.safeReadLine(scanner))) {
            boolean success = controller.modifyPendingReservation(confNo, roomType, checkIn, checkOut);
            if (success) {
                System.out.println(utility.UIUtils.GREEN + "  [+] Reservation modified successfully!" + utility.UIUtils.RESET);
            } else {
                System.out.println(utility.UIUtils.RED + "  [!] Modification failed." + utility.UIUtils.RESET);
            }
        } else {
            System.out.println("  Modification cancelled.");
        }
    }

    private void cancelPendingBooking() {
        utility.UIUtils.printSubHeader("MODULE 1 > CANCEL PENDING BOOKING", utility.UIUtils.CYAN);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type '0' or 'b' or 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");
        displayPendingReservationsList();
        
        Reservation res = null;
        String confNo = "";
        
        while (true) {
            System.out.print("Enter Confirmation Number: ");
            confNo = utility.UIUtils.safeReadLine(scanner).trim();
            
            if ("0".equals(confNo) || "b".equalsIgnoreCase(confNo) || "back".equalsIgnoreCase(confNo) || "cancel".equalsIgnoreCase(confNo)) {
                System.out.println("  Operation cancelled.");
                return;
            }
            
            if (confNo.isEmpty()) {
                continue;
            }
            
            res = controller.findReservationByConfNo(confNo);
            if (res == null) {
                System.out.println(utility.UIUtils.RED + "  [!] ERROR: Reservation with Confirmation No " + confNo + " not found. Please try again." + utility.UIUtils.RESET);
                continue;
            }
            
            if (!"PENDING".equals(res.getBookingStatus())) {
                System.out.println(utility.UIUtils.RED + "  [!] ERROR: Only PENDING reservations can be cancelled here (Status is currently: " + res.getBookingStatus() + "). Please try again." + utility.UIUtils.RESET);
                continue;
            }
            
            break;
        }
        
        System.out.println("\n  Reservation Details:");
        System.out.println("  Guest ID        : " + res.getGuestId());
        System.out.println("  Guest Name      : " + (res.getGuest() != null ? res.getGuest().getName() : "N/A"));
        System.out.println("  Room Type       : " + res.getRoomType());
        System.out.println("  Stay Dates      : " + res.getCheckInDate() + " to " + res.getCheckOutDate());
        System.out.println();
        
        System.out.print("  Are you sure you want to CANCEL this reservation? (Y/N): ");
        if ("Y".equalsIgnoreCase(utility.UIUtils.safeReadLine(scanner))) {
            boolean success = controller.cancelPendingReservation(confNo);
            if (success) {
                System.out.println(utility.UIUtils.GREEN + "  [+] Reservation cancelled successfully." + utility.UIUtils.RESET);
            } else {
                System.out.println(utility.UIUtils.RED + "  [!] Cancellation failed." + utility.UIUtils.RESET);
            }
        } else {
            System.out.println("  Cancellation aborted.");
        }
    }

    private void generateRevenueReport() {
        utility.UIUtils.printSubHeader("REVENUE ANALYSIS REPORT", utility.UIUtils.YELLOW);
        
        DoublyLinkedList<String> roomFilters = promptRoomFilters();
        
        System.out.println("  Select Booking Status(es) to filter by:");
        System.out.println("    1. PENDING");
        System.out.println("    2. CONFIRMED");
        System.out.println("    3. CANCELLED");
        System.out.println("    4. CHECKED_IN");
        System.out.println("    5. CHECKED_OUT");
        System.out.println("    6. ALL");
        System.out.print("  Enter choice(s) using numbers separated by commas (e.g., 1,2 or 6 for ALL): ");
        String statusInput = utility.UIUtils.safeReadLine(scanner).trim();
        
        DoublyLinkedList<String> statusFilters = new DoublyLinkedList<>();
        if (statusInput.isEmpty()) {
            statusFilters.add("ALL");
        } else {
            String[] parts = statusInput.split(",");
            for (String part : parts) {
                String choice = part.trim();
                switch (choice) {
                    case "1":
                        statusFilters.add("PENDING");
                        break;
                    case "2":
                        statusFilters.add("CONFIRMED");
                        break;
                    case "3":
                        statusFilters.add("CANCELLED");
                        break;
                    case "4":
                        statusFilters.add("CHECKED_IN");
                        break;
                    case "5":
                        statusFilters.add("CHECKED_OUT");
                        break;
                    case "6":
                    default:
                        if (choice.equals("6")) {
                            statusFilters.add("ALL");
                        }
                        break;
                }
            }
        }
        if (statusFilters.isEmpty()) {
            statusFilters.add("ALL");
        }
        
        System.out.print("  Filter by Minimum Duration of Stay (days): ");
        int minDuration = 0;
        try {
            String input = utility.UIUtils.safeReadLine(scanner).trim();
            if (!input.isEmpty()) {
                minDuration = Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid number. Minimum duration set to 0.");
        }
        
        StandardBookingController.StandardRevenueReport report = controller.generateRevenueReport(roomFilters, statusFilters, minDuration);
        
        System.out.println("\n============================================================================================");
        System.out.println("                         RESERVATIONS REVENUE ANALYSIS REPORT");
        System.out.println("============================================================================================");
        System.out.println("  Filters Applied:");
        System.out.println("  - Room Type      : " + report.getRoomTypeFilter());
        System.out.println("  - Booking Status : " + report.getStatusFilter());
        System.out.println("  - Min Duration   : " + report.getMinDurationFilter() + " nights");
        System.out.println("--------------------------------------------------------------------------------------------");
        
        DoublyLinkedList<Reservation> list = report.getReservations();
        if (list.isEmpty()) {
            System.out.println("  No reservations matched the filters.");
        } else {
            System.out.println("+-----+--------------+----------------------+-----------+------------+------------+---------+");
            System.out.println("| No. | Confirmation | Guest Name           | Room Type | Check-In   | Status     | Revenue |");
            System.out.println("+-----+--------------+----------------------+-----------+------------+------------+---------+");
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                Reservation res = list.getEntry(i);
                try {
                    long days = StandardBookingController.calculateStayDuration(res.getCheckInDate(), res.getCheckOutDate());
                    double revenue = StandardBookingController.calculateRevenue(res);
                    String guestName = res.getGuest() != null ? res.getGuest().getName() : res.getGuestId();
                    if (guestName.length() > 20) guestName = guestName.substring(0, 17) + "...";
                    
                    System.out.printf("| %-3d | %-12s | %-20s | %-9s | %-10s | %-10s | RM%6.2f |%n",
                            i, res.getConfirmationNo(), guestName, res.getRoomType(), res.getCheckInDate(), res.getBookingStatus(), revenue);
                } catch (Exception e) {
                    // Ignore
                }
            }
            System.out.println("+-----+--------------+----------------------+-----------+------------+------------+---------+");
        }
        
        System.out.println("============================================================================================");
        System.out.println("  SUMMARY STATISTICS");
        System.out.println("============================================================================================");
        System.out.println("  Total Bookings matched  : " + report.getTotalBookings());
        System.out.println("  Total Room Nights Sold  : " + report.getTotalDays());
        System.out.printf("  Total Projected Revenue : RM%.2f%n", report.getTotalRevenue());
        System.out.printf("  Avg. Revenue / Booking  : RM%.2f%n", report.getAverageRevenuePerBooking());
        System.out.println("============================================================================================");
    }

    private void generateQueuePerformanceReport() {
        utility.UIUtils.printSubHeader("QUEUE PERFORMANCE REPORT", utility.UIUtils.YELLOW);
        
        DoublyLinkedList<String> roomFilters = promptRoomFilters();
        
        StandardBookingController.QueuePerformanceReport report = controller.generateQueuePerformanceReport(roomFilters);
        
        System.out.println("\n=============================================================================");
        System.out.println("                    STANDARD QUEUE PERFORMANCE & ROOM SHORTAGE REPORT");
        System.out.println("=============================================================================");
        System.out.println("  Room Type Filter: " + report.getRoomTypeFilter());
        System.out.println("-----------------------------------------------------------------------------");
        
        DoublyLinkedList<Reservation> list = report.getPendingReservations();
        if (list.isEmpty()) {
            System.out.println("  No guests currently waiting in the standard queue for this room type.");
        } else {
            System.out.println("+-----+--------------+----------------------+-----------+------------+------------+-----------------+");
            System.out.println("| Pos | Confirmation | Guest Name           | Room Type | Check-In   | Check-Out  | Waiting Time    |");
            System.out.println("+-----+--------------+----------------------+-----------+------------+------------+-----------------+");
            long now = System.currentTimeMillis();
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                Reservation res = list.getEntry(i);
                String guestName = res.getGuest() != null ? res.getGuest().getName() : res.getGuestId();
                if (guestName.length() > 20) guestName = guestName.substring(0, 17) + "...";
                
                long waitMillis = now - res.getTimestamp();
                long waitMinutes = waitMillis / (60 * 1000);
                String waitStr = waitMinutes + " min ago";
                if (waitMinutes > 60) {
                    waitStr = (waitMinutes / 60) + " hrs ago";
                }
                
                System.out.printf("| %-3d | %-12s | %-20s | %-9s | %-10s | %-10s | %-15s |%n",
                        i, res.getConfirmationNo(), guestName, res.getRoomType(), res.getCheckInDate(), res.getCheckOutDate(), waitStr);
            }
            System.out.println("+-----+--------------+----------------------+-----------+------------+------------+-----------------+");
        }
        
        System.out.println("=============================================================================");
        System.out.println("  RESOURCE OPTIMIZATION & INVENTORY SHORTAGE ANALYSIS");
        System.out.println("=============================================================================");
        System.out.println("  Total Guests Waiting     : " + report.getTotalPending());
        System.out.println("  Available Rooms in Stock : " + report.getAvailableRooms());
        if (report.getShortage() > 0) {
            System.out.println("  Inventory Shortage       : " + utility.UIUtils.RED + report.getShortage() + " room(s) missing" + utility.UIUtils.RESET);
            System.out.println("  Action Recommendation    : Housekeeping should expedite cleaning, or offer upgrade/downgrade options.");
        } else {
            System.out.println("  Inventory Shortage       : " + utility.UIUtils.GREEN + "0 room(s) missing (Adequate supply)" + utility.UIUtils.RESET);
            System.out.println("  Action Recommendation    : Assign rooms to waiting guests immediately.");
        }
        System.out.println("=============================================================================");
    }

    private Guest registerNewGuestUI() {
        utility.UIUtils.printSubHeader("MODULE 1 > REGISTER NEW GUEST", utility.UIUtils.CYAN);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");

        String name = "";
        String icPassport = "";
        String contactNo = "";
        String email = "";

        int step = 0;
        while (step >= 0 && step <= 3) {
            switch (step) {
                case 0: {
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 1/4 - Guest Name       ", name, false);
                    if (res.isGoBack()) return null;
                    if (res.isQuitToMain() || res.isCancel()) return null;
                    name = res.getValue();
                    step++;
                    break;
                }
                case 1: {
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 2/4 - IC / Passport     ", icPassport, false);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain() || res.isCancel()) return null;
                    icPassport = res.getValue();
                    step++;
                    break;
                }
                case 2: {
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 3/4 - Contact Number    ", contactNo, false);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain() || res.isCancel()) return null;
                    contactNo = res.getValue();
                    step++;
                    break;
                }
                case 3: {
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 4/4 - Email Address     ", email, false);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain() || res.isCancel()) return null;
                    email = res.getValue();
                    step++;
                    break;
                }
            }
        }

        if (step < 0) return null;

        Guest newGuest = controller.registerNewMember(name, icPassport, contactNo, email);
        if (newGuest != null) {
            System.out.println("\n  [+] Guest registered successfully!");
            System.out.println("      Guest ID  : " + newGuest.getGuestId());
            System.out.println("      Name      : " + newGuest.getName());
            System.out.println("      Tier      : " + newGuest.getLoyaltyTier());
        } else {
            System.out.println("\n  [!] Guest registration failed.");
        }
        return newGuest;
    }

    private int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }

    private void displayPendingReservationsList() {
        DoublyLinkedList<Reservation> pendingList = controller.getAllPendingReservations();
        if (pendingList == null || pendingList.isEmpty()) {
            System.out.println("  [No pending reservations available]\n");
            return;
        }
        System.out.println("  Pending Reservations:");
        for (int i = 1; i <= pendingList.getNumberOfEntries(); i++) {
            Reservation pendingRes = pendingList.getEntry(i);
            if (pendingRes != null) {
                String guestName = pendingRes.getGuest() != null ? pendingRes.getGuest().getName() : pendingRes.getGuestId();
                System.out.println("  " + pendingRes.getConfirmationNo() + ", " 
                        + pendingRes.getRoomType() + ", "
                        + pendingRes.getCheckInDate() + " to " + pendingRes.getCheckOutDate() + ", " 
                        + guestName);
            }
        }
        System.out.println();
    }

    private DoublyLinkedList<String> promptRoomFilters() {
        System.out.println("  Select Room Type(s) to filter by:");
        System.out.println("    1. STANDARD");
        System.out.println("    2. DELUXE");
        System.out.println("    3. SUITE");
        System.out.println("    4. ALL");
        System.out.print("  Enter choice(s) using numbers separated by commas (e.g., 1,2 or 4 for ALL): ");
        String roomInput = utility.UIUtils.safeReadLine(scanner).trim();
        
        DoublyLinkedList<String> roomFilters = new DoublyLinkedList<>();
        if (roomInput.isEmpty()) {
            roomFilters.add("ALL");
        } else {
            String[] parts = roomInput.split(",");
            for (String part : parts) {
                String choice = part.trim();
                switch (choice) {
                    case "1":
                        roomFilters.add("STANDARD");
                        break;
                    case "2":
                        roomFilters.add("DELUXE");
                        break;
                    case "3":
                        roomFilters.add("SUITE");
                        break;
                    case "4":
                    default:
                        if (choice.equals("4")) {
                            roomFilters.add("ALL");
                        }
                        break;
                }
            }
        }
        if (roomFilters.isEmpty()) {
            roomFilters.add("ALL");
        }
        return roomFilters;
    }
}