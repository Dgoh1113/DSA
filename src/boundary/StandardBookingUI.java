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
                    peekNextBooking();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 4:
                    viewQueue();
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

        utility.UIUtils.printSectionHeader("QUEUE MONITORING", utility.UIUtils.CYAN);
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "3." + utility.UIUtils.RESET + " Peek Next Guest in Queue");
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "4." + utility.UIUtils.RESET + " View Entire Standard Queue");

        utility.UIUtils.printSectionHeader("NAVIGATION", utility.UIUtils.RED);
        System.out.println("  " + utility.UIUtils.RED + utility.UIUtils.BOLD + "0." + utility.UIUtils.RESET + " Back to Main Menu");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.print(utility.UIUtils.BOLD + "Enter your choice: " + utility.UIUtils.RESET);
    }

    private boolean registerWalkIn() {
        utility.UIUtils.printSubHeader("MODULE 1 > CREATE STANDARD BOOKING", utility.UIUtils.CYAN);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");

        utility.StepResult guestIdResult = utility.ValidationUtils.readValidStringStep(
                scanner, "Guest ID", "", false);
        if (guestIdResult.isQuitToMain()) return true;
        if (guestIdResult.isGoBack() || guestIdResult.isCancel()) return false;

        Guest guest = controller.findGuestById(guestIdResult.getValue());
        if (guest == null) {
            System.out.println("\n  [!] Guest ID not found. Please register the guest before creating a booking.");
            return false;
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

        String roomType = "";
        String checkIn = "";
        String checkOut = "";
        int step = 0;
        while (step >= 0 && step <= 2) {
            switch (step) {
                case 0: {
                    System.out.println("  Room Options: STANDARD | DELUXE | SUITE");
                    utility.StepResult res = utility.ValidationUtils.readValidRoomTypeStep(scanner, "Step 1/3 - Preferred Room   ", roomType);
                    if (res.isGoBack()) return false;
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    roomType = res.getValue();
                    step++;
                    break;
                }
                case 1: {
                    utility.StepResult res = utility.ValidationUtils.readValidDateStep(scanner, "Step 2/3 - Check-In (YYYY-MM-DD) ", checkIn);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    checkIn = res.getValue();
                    step++;
                    break;
                }
                case 2: {
                    utility.StepResult res = utility.ValidationUtils.readValidCheckOutDateStep(scanner, "Step 3/3 - Check-Out Date   ", checkIn, checkOut);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    checkOut = res.getValue();
                    step++;
                    break;
                }
            }
        }

        if (step < 0) {
            System.out.println("\n  [!] Walk-in registration cancelled. No data saved.");
            return false;
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
        System.out.println("  Queue Position  : " + controller.getQueueSize());
        System.out.println("+------------------------------------------+");
        return false;
    }

    private void processNextBooking() {
        utility.UIUtils.printSubHeader("MODULE 1 > PROCESS NEXT BOOKING", utility.UIUtils.CYAN);

        if (controller.isQueueEmpty()) {
            System.out.println("The standard queue is EMPTY. No bookings to process.");
            return;
        }

        Reservation res = controller.processNextBooking();
        if (res != null) {
            System.out.println("\n+------------------------------------------+");
            if ("CONFIRMED".equals(res.getBookingStatus())) {
                System.out.println("  ROOM ASSIGNED SUCCESSFULLY!");
                System.out.println("+------------------------------------------+");
                System.out.println("  Confirmation No : " + res.getConfirmationNo());
                System.out.println("  Guest ID        : " + res.getGuestId());
                System.out.println("  Guest Name      : " + (res.getGuest() != null ? res.getGuest().getName() : "N/A"));
                System.out.println("  Room Assigned   : " + res.getAssignedRoomNo());
                System.out.println("  Room Type       : " + res.getRoomType());
                System.out.println("  Status          : " + res.getBookingStatus());
            } else {
                System.out.println("  NO ROOMS AVAILABLE");
                System.out.println("+------------------------------------------+");
                System.out.println("  Confirmation No : " + res.getConfirmationNo());
                System.out.println("  Guest dequeued but no room could be assigned.");
                System.out.println("  Status          : " + res.getBookingStatus());
            }
            System.out.println("  Remaining in Queue: " + controller.getQueueSize());
            System.out.println("+------------------------------------------+");
        }
    }

    private void peekNextBooking() {
        utility.UIUtils.printSubHeader("MODULE 1 > PEEK NEXT GUEST IN QUEUE", utility.UIUtils.CYAN);

        Reservation res = controller.peekNextBooking();
        if (res == null) {
            System.out.println("The standard queue is EMPTY.");
        } else {
            System.out.println("  Next Guest      : " + (res.getGuest() != null ? res.getGuest().getName() : res.getGuestId()));
            System.out.println("  Confirmation No : " + res.getConfirmationNo());
            System.out.println("  Room Type       : " + res.getRoomType());
            System.out.println("  Status          : " + res.getBookingStatus());
        }
    }

    private void viewQueue() {
        utility.UIUtils.printSubHeader("MODULE 1 > VIEW STANDARD BOOKING QUEUE", utility.UIUtils.CYAN);
        System.out.println("Queue Size: " + controller.getQueueSize());

        if (controller.isQueueEmpty()) {
            System.out.println("The queue is empty.");
            return;
        }

        DoublyLinkedList<Reservation> list = controller.getQueueList();
        System.out.println("+-----+----------------+----------------+-----------+------------+");
        System.out.println("| No. | Confirmation   | Guest ID       | Room Type | Status     |");
        System.out.println("+-----+----------------+----------------+-----------+------------+");

        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            Reservation res = list.getEntry(i);
            System.out.printf("| %-3d | %-14s | %-14s | %-9s | %-10s |%n",
                    i, res.getConfirmationNo(), res.getGuestId(),
                    res.getRoomType(), res.getBookingStatus());
        }
        System.out.println("+-----+----------------+----------------+-----------+------------+");
    }

    private int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }
}
