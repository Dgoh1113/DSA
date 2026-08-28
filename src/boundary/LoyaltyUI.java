package boundary;

import adt.DoublyLinkedList;
import control.LoyaltyController;
import entity.Guest;
import entity.LoyaltyAccount;
import entity.RedemptionTransaction;
import entity.Reservation;
import java.util.Scanner;

/**
 * Boundary: Loyalty & Rewards UI (Module 4).
 * Console screens for member profiles, point redemption, and management reports.
 * All System.out / Scanner interactions live here — no business logic.
 */
public class LoyaltyUI {

    private LoyaltyController controller;
    private Scanner scanner;

    public LoyaltyUI(LoyaltyController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void show() {
        boolean exitToMainMenu = false;
        do {
            utility.UIUtils.clearScreen();
            utility.UIUtils.printModule4Header();
            displayMenu();
            int choice = utility.UIUtils.safeReadInt(scanner);

            switch (choice) {
                case 1:
                    viewMemberProfile();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 2:
                    if (redeemPoints()) {
                        exitToMainMenu = true;
                    } else {
                        exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    }
                    break;
                case 3:
                    viewTransactionHistory();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 4:
                    topEarnersReport();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 5:
                    expiringPointsReport();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 6:
                    tierReport();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 7:
                    managementReportGenerator();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 8:
                    viewAllMembers();
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
        utility.UIUtils.printSectionHeader("MEMBER PROFILES & REDEMPTION", utility.UIUtils.MAGENTA);
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "1." + utility.UIUtils.RESET + " View Member Profile");
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "2." + utility.UIUtils.RESET + " Redeem Points");
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "3." + utility.UIUtils.RESET + " View Transaction History");

        utility.UIUtils.printSectionHeader("MANAGEMENT & ANALYTICS REPORTS", utility.UIUtils.MAGENTA);
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "4." + utility.UIUtils.RESET + " Report: Top Point Earners (MergeSort)");
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "5." + utility.UIUtils.RESET + " Report: Expiring Points (QuickSort)");
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "6." + utility.UIUtils.RESET + " Report: Members by Tier");
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "7." + utility.UIUtils.RESET + " Report Generator (Search + Multi-Filter + MergeSort)");
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "8." + utility.UIUtils.RESET + " View All Members");

        utility.UIUtils.printSectionHeader("NAVIGATION", utility.UIUtils.RED);
        System.out.println("  " + utility.UIUtils.RED + utility.UIUtils.BOLD + "0." + utility.UIUtils.RESET + " Back to Main Menu");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.print(utility.UIUtils.BOLD + "Enter your choice: " + utility.UIUtils.RESET);
    }

    private void viewMemberProfile() {
        utility.UIUtils.printSubHeader("MODULE 4 > VIEW MEMBER PROFILE", utility.UIUtils.MAGENTA);
        System.out.print("Enter Member / Guest ID (or 'cancel'): ");
        String memberId = utility.UIUtils.safeReadLine(scanner);
        if (isCancelled(memberId)) {
            printCancellationMessage();
            return;
        }

        LoyaltyAccount account = controller.viewMemberProfile(memberId);
        if (account == null) {
            System.out.println("No loyalty account found for ID: " + memberId);
            return;
        }

        Guest guest = controller.viewMemberGuest(account.getMemberId());

        System.out.println("\n+------------------------------------------+");
        System.out.println("         LOYALTY MEMBER PROFILE");
        System.out.println("+------------------------------------------+");
        System.out.println("  Member ID       : " + account.getMemberId());
        System.out.println("+------------------------------------------+");
        System.out.println("  GUEST INFORMATION");
        if (guest != null) {
            System.out.println("  Name            : " + guest.getName());
            System.out.println("  IC/Passport     : " + guest.getIcPassport());
            System.out.println("  Contact Number  : " + guest.getContactNo());
            System.out.println("  Email           : " + guest.getEmail());
        } else {
            System.out.println("  Guest details are unavailable.");
        }
        System.out.println("+------------------------------------------+");
        System.out.println("  LOYALTY INFORMATION");
        System.out.println("  Total Points    : " + account.getTotalPoints());
        System.out.println("  Tier Status     : " + account.getTierStatus());
        System.out.println("  Points Expiry   : " + (account.getPointsExpiryDate() != null ? account.getPointsExpiryDate() : "N/A"));
        System.out.println("+------------------------------------------+");
        System.out.println("  TIER THRESHOLDS");
        System.out.println("  SILVER    : " + LoyaltyController.SILVER_THRESHOLD + " pts");
        System.out.println("  GOLD      : " + LoyaltyController.GOLD_THRESHOLD + " pts");
        System.out.println("  PLATINUM  : " + LoyaltyController.PLATINUM_THRESHOLD + " pts");
        System.out.println("  DIAMOND   : " + LoyaltyController.DIAMOND_THRESHOLD + " pts");
        System.out.println("+------------------------------------------+");

        // Show point history
        DoublyLinkedList<String> history = account.getPointHistoryList();
        if (history.getNumberOfEntries() > 0) {
            System.out.println("  POINT HISTORY:");
            for (int i = 1; i <= history.getNumberOfEntries(); i++) {
                System.out.println("    " + i + ". " + history.getEntry(i));
            }
            System.out.println("+------------------------------------------+");
        }
    }

    private boolean redeemPoints() {
        utility.UIUtils.printSubHeader("MODULE 4 > REDEEM POINTS", utility.UIUtils.MAGENTA);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");

        String memberId = "";
        String rewardItem = "";
        int pointsCost = 0;

        int step = 0;
        LoyaltyAccount account = null;

        while (step >= 0 && step <= 1) {
            switch (step) {
                case 0: {
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 1/2 - Enter Member / Guest ID ", memberId, false);
                    if (res.isGoBack()) {
                        System.out.println("\n  [!] Point redemption cancelled. No data saved.");
                        return false;
                    }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) {
                        System.out.println("\n  [!] Point redemption cancelled. No data saved.");
                        return false;
                    }
                    memberId = res.getValue();
                    Guest guest = controller.viewMemberGuest(memberId);
                    if (guest == null) {
                        System.out.println("  [!] ERROR: Guest ID not found: " + memberId);
                        break;
                    }
                    account = controller.viewMemberProfile(memberId);
                    if (account == null) {
                        System.out.println("  [!] ERROR: No loyalty account found for ID: " + memberId);
                        break;
                    }
                    System.out.println("  Member Found: " + account.getMemberId() + " | Tier: " + account.getTierStatus() + " | Points: " + account.getTotalPoints());
                    step++;
                    break;
                }
                case 1: {
                    System.out.println("\nAvailable Rewards:");
                    System.out.println("  1. Room Upgrade     - 1000 pts (active booking required)");
                    System.out.println("  2. Late Checkout   - 800 pts");
                    System.out.println("  3. Free Breakfast     - 350 pts");
                    System.out.println("  4. Spa Voucher      - 300 pts");
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 2/2 - Select reward (1-4) or enter reward name", rewardItem, false);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) {
                        System.out.println("\n  [!] Point redemption cancelled. No data saved.");
                        return false;
                    }

                    String input = res.getValue();
                    if (input.equals("1") || input.equalsIgnoreCase("room upgrade")) {
                        rewardItem = "Room Upgrade";
                        pointsCost = 1000;
                    } else if (input.equals("2") || input.equalsIgnoreCase("late checkout")) {
                        rewardItem = "Late Checkout";
                        pointsCost = 800;
                    } else if (input.equals("3") || input.equalsIgnoreCase("free breakfast")) {
                        rewardItem = "Free Breakfast";
                        pointsCost = 350;
                    } else if (input.equals("4") || input.equalsIgnoreCase("spa voucher")) {
                        rewardItem = "Spa Voucher";
                        pointsCost = 300;
                    } else {
                        System.out.println("  [!] ERROR: Select 1-4 or enter a listed reward name.");
                        break;
                    }
                    step++;
                    break;
                }
            }
        }

        if (step < 0) {
            System.out.println("\n  [!] Point redemption cancelled. No data saved.");
            return false;
        }

        if (account.getTotalPoints() < pointsCost) {
            System.out.println("\n[!] Insufficient points! Need " + pointsCost + ", have " + account.getTotalPoints());
            return false;
        }

        if ("Room Upgrade".equalsIgnoreCase(rewardItem)) {
            redeemRoomUpgrade(memberId, account, pointsCost);
            return false;
        }

        if ("Late Checkout".equalsIgnoreCase(rewardItem)) {
            redeemLateCheckout(memberId, account, pointsCost);
            return false;
        }

        RedemptionTransaction txn = controller.redeemPoints(memberId, rewardItem, pointsCost);
        if (txn != null) {
            System.out.println("\n*** REDEMPTION SUCCESSFUL ***");
            System.out.println("  Transaction ID  : " + txn.getTransactionId());
            System.out.println("  Reward          : " + txn.getRewardItem());
            System.out.println("  Points Deducted : " + txn.getPointsDeducted());
            System.out.println("  Remaining Points: " + account.getTotalPoints());
            System.out.println("  Current Tier    : " + account.getTierStatus());
        } else {
            System.out.println("\n[!] Redemption failed.");
        }
        return false;
    }

    private void redeemRoomUpgrade(String memberId, LoyaltyAccount account, int pointsCost) {
        DoublyLinkedList<Reservation> bookings = controller.getRoomUpgradeBookings(memberId);
        if (bookings.isEmpty()) {
            System.out.println("\n[!] Room upgrade unavailable: this member has no active booking.");
            return;
        }

        System.out.println("\nActive bookings eligible for review:");
        System.out.println("+-----+----------------+-----------+--------+------------+------------+");
        System.out.println("| No. | Confirmation   | Room Type | Room   | Check-In   | Check-Out  |");
        System.out.println("+-----+----------------+-----------+--------+------------+------------+");
        for (int i = 1; i <= bookings.getNumberOfEntries(); i++) {
            Reservation booking = bookings.getEntry(i);
            System.out.printf("| %-3d | %-14s | %-9s | %-6s | %-10s | %-10s |%n",
                    i, booking.getConfirmationNo(), booking.getRoomType(),
                    booking.getAssignedRoomNo() == null ? "---" : booking.getAssignedRoomNo(),
                    booking.getCheckInDate(), booking.getCheckOutDate());
        }
        System.out.println("+-----+----------------+-----------+--------+------------+------------+");
        System.out.println("  STANDARD upgrades to DELUXE; DELUXE upgrades to SUITE.");
        System.out.println("  SUITE bookings cannot be upgraded further.");
        System.out.print("Enter the confirmation number to upgrade (or 'cancel'): ");
        String confirmationNo = utility.UIUtils.safeReadLine(scanner);
        if ("cancel".equalsIgnoreCase(confirmationNo)) {
            System.out.println("Room upgrade cancelled. No points were deducted.");
            return;
        }

        LoyaltyController.RoomUpgradeResult result = controller.redeemRoomUpgrade(
                memberId, confirmationNo, pointsCost);
        if (!result.isSuccessful()) {
            System.out.println("\n[!] Room upgrade failed: " + result.getErrorMessage());
            return;
        }

        RedemptionTransaction transaction = result.getTransaction();
        Reservation upgradedBooking = result.getReservation();
        System.out.println("\n*** ROOM UPGRADE SUCCESSFUL ***");
        System.out.println("  Transaction ID  : " + transaction.getTransactionId());
        System.out.println("  Booking          : " + upgradedBooking.getConfirmationNo());
        System.out.println("  Room Upgrade     : " + result.getPreviousRoomType()
                + " -> " + result.getUpgradedRoomType());
        System.out.println("  New Room         : " + upgradedBooking.getAssignedRoomNo());
        System.out.println("  Points Deducted  : " + transaction.getPointsDeducted());
        System.out.println("  Remaining Points : " + account.getTotalPoints());
    }

    private void redeemLateCheckout(String memberId, LoyaltyAccount account, int pointsCost) {
        DoublyLinkedList<Reservation> bookings = controller.getLateCheckoutBookings(memberId);
        if (bookings.isEmpty()) {
            System.out.println("\n[!] Late checkout unavailable: this member has no eligible booking.");
            return;
        }

        System.out.println("\nEligible bookings (late checkout adds one calendar day):");
        System.out.println("+-----+----------------+-----------+--------+------------+------------+");
        System.out.println("| No. | Confirmation   | Room Type | Room   | Check-In   | Check-Out  |");
        System.out.println("+-----+----------------+-----------+--------+------------+------------+");
        for (int i = 1; i <= bookings.getNumberOfEntries(); i++) {
            Reservation booking = bookings.getEntry(i);
            System.out.printf("| %-3d | %-14s | %-9s | %-6s | %-10s | %-10s |%n",
                    i, booking.getConfirmationNo(), booking.getRoomType(),
                    booking.getAssignedRoomNo() == null ? "---" : booking.getAssignedRoomNo(),
                    booking.getCheckInDate(), booking.getCheckOutDate());
        }
        System.out.println("+-----+----------------+-----------+--------+------------+------------+");
        System.out.print("Enter the confirmation number to extend (or 'cancel'): ");
        String confirmationNo = utility.UIUtils.safeReadLine(scanner);
        if ("cancel".equalsIgnoreCase(confirmationNo)) {
            System.out.println("Late checkout cancelled. No points were deducted.");
            return;
        }

        LoyaltyController.LateCheckoutResult result = controller.redeemLateCheckout(
                memberId, confirmationNo, pointsCost);
        if (!result.isSuccessful()) {
            System.out.println("\n[!] Late checkout failed: " + result.getErrorMessage());
            return;
        }

        RedemptionTransaction transaction = result.getTransaction();
        System.out.println("\n*** LATE CHECKOUT REDEMPTION SUCCESSFUL ***");
        System.out.println("  Transaction ID   : " + transaction.getTransactionId());
        System.out.println("  Booking          : " + result.getReservation().getConfirmationNo());
        System.out.println("  Check-Out        : " + result.getPreviousCheckOutDate()
                + " -> " + result.getExtendedCheckOutDate());
        System.out.println("  Points Deducted  : " + transaction.getPointsDeducted());
        System.out.println("  Remaining Points : " + account.getTotalPoints());
    }

    private void viewTransactionHistory() {
        utility.UIUtils.printSubHeader("MODULE 4 > VIEW TRANSACTION HISTORY", utility.UIUtils.MAGENTA);
        System.out.print("Enter Member / Guest ID (or 'cancel'): ");
        String memberId = utility.UIUtils.safeReadLine(scanner);
        if (isCancelled(memberId)) {
            printCancellationMessage();
            return;
        }

        DoublyLinkedList<RedemptionTransaction> history = controller.getTransactionHistory(memberId);
        if (history.isEmpty()) {
            System.out.println("No redemption transactions found for ID: " + memberId);
            return;
        }

        System.out.println("\n+-----+----------+--------------------+--------+------------+----------+");
        System.out.println("| No. | Txn ID   | Reward Item        | Points | Date       | Status   |");
        System.out.println("+-----+----------+--------------------+--------+------------+----------+");

        for (int i = 1; i <= history.getNumberOfEntries(); i++) {
            RedemptionTransaction txn = history.getEntry(i);
            System.out.printf("| %-3d | %-8s | %-18s | %-6d | %-10s | %-8s |%n",
                    i, txn.getTransactionId(), txn.getRewardItem(),
                    txn.getPointsDeducted(), txn.getRequestDate(), txn.getStatus());
        }
        System.out.println("+-----+----------+--------------------+--------+------------+----------+");
    }

    private void topEarnersReport() {
        utility.UIUtils.printSubHeader("MODULE 4 > REPORT: TOP POINT EARNERS (MERGESORT)", utility.UIUtils.MAGENTA);

        DoublyLinkedList<LoyaltyAccount> sorted = controller.generateTopEarnersReport();
        if (sorted.isEmpty()) {
            System.out.println("No loyalty accounts found.");
            return;
        }

        System.out.println("+------+----------------+-----------+----------+");
        System.out.println("| Rank | Member ID      | Points    | Tier     |");
        System.out.println("+------+----------------+-----------+----------+");

        for (int i = 1; i <= sorted.getNumberOfEntries(); i++) {
            LoyaltyAccount acc = sorted.getEntry(i);
            System.out.printf("| %-4d | %-14s | %-9d | %-8s |%n",
                    i, acc.getMemberId(), acc.getTotalPoints(), acc.getTierStatus());
        }
        System.out.println("+------+----------------+-----------+----------+");
        System.out.println("Sorted using MergeSort algorithm");
    }

    private void expiringPointsReport() {
        utility.UIUtils.printSubHeader("MODULE 4 > REPORT: EXPIRING POINTS (QUICKSORT)", utility.UIUtils.MAGENTA);
        Integer days = readNonNegativeInteger("Days threshold (e.g., 30, or 'cancel'): ");
        if (days == null) {
            printCancellationMessage();
            return;
        }

        DoublyLinkedList<LoyaltyAccount> result = controller.generateExpiringPointsReport(days);
        if (result.isEmpty()) {
            System.out.println("No members with points expiring within " + days + " days.");
            return;
        }

        System.out.println("+------+----------------+-----------+----------+-------------+");
        System.out.println("| No.  | Member ID      | Points    | Tier     | Expiry Date |");
        System.out.println("+------+----------------+-----------+----------+-------------+");

        for (int i = 1; i <= result.getNumberOfEntries(); i++) {
            LoyaltyAccount acc = result.getEntry(i);
            System.out.printf("| %-4d | %-14s | %-9d | %-8s | %-11s |%n",
                    i, acc.getMemberId(), acc.getTotalPoints(),
                    acc.getTierStatus(), acc.getPointsExpiryDate());
        }
        System.out.println("+------+----------------+-----------+----------+-------------+");
        System.out.println("Sorted using QuickSort algorithm");
    }

    private void tierReport() {
        utility.UIUtils.printSubHeader("MODULE 4 > REPORT: MEMBERS BY TIER", utility.UIUtils.MAGENTA);
        System.out.println("Select Tier: STANDARD | SILVER | GOLD | PLATINUM | DIAMOND (or 'cancel')");
        utility.StepResult tierResult = utility.ValidationUtils.readValidLoyaltyTierStep(scanner, "Tier", null);
        if (tierResult.isCancel() || tierResult.isQuitToMain()) {
            printCancellationMessage();
            return;
        }
        String tier = tierResult.getValue();

        DoublyLinkedList<LoyaltyAccount> result = controller.generateTierReport(tier);
        if (result.isEmpty()) {
            System.out.println("No members found in " + tier + " tier.");
            return;
        }

        System.out.println("+------+----------------+-----------+-------------+");
        System.out.println("| No.  | Member ID      | Points    | Expiry Date |");
        System.out.println("+------+----------------+-----------+-------------+");

        for (int i = 1; i <= result.getNumberOfEntries(); i++) {
            LoyaltyAccount acc = result.getEntry(i);
            System.out.printf("| %-4d | %-14s | %-9d | %-11s |%n",
                    i, acc.getMemberId(), acc.getTotalPoints(),
                    acc.getPointsExpiryDate() != null ? acc.getPointsExpiryDate() : "N/A");
        }
        System.out.println("+------+----------------+-----------+-------------+");
    }

    private void viewAllMembers() {
        utility.UIUtils.printSubHeader("MODULE 4 > VIEW ALL LOYALTY MEMBERS", utility.UIUtils.MAGENTA);
        DoublyLinkedList<LoyaltyAccount> all = controller.getAllAccounts();

        if (all.isEmpty()) {
            System.out.println("No loyalty accounts in the system.");
            return;
        }

        System.out.println("+-----+----------------+----------------------+-----------+----------+-------------+");
        System.out.println("| No. | Member ID      | Member Name          | Points    | Tier     | Expiry Date |");
        System.out.println("+-----+----------------+----------------------+-----------+----------+-------------+");

        for (int i = 1; i <= all.getNumberOfEntries(); i++) {
            LoyaltyAccount acc = all.getEntry(i);
            Guest guest = controller.viewMemberGuest(acc.getMemberId());
            String memberName = guest != null ? guest.getName() : "N/A";
            System.out.printf("| %-3d | %-14s | %-20s | %-9d | %-8s | %-11s |%n",
                    i, acc.getMemberId(), memberName, acc.getTotalPoints(),
                    acc.getTierStatus(),
                    acc.getPointsExpiryDate() != null ? acc.getPointsExpiryDate() : "N/A");
        }
        System.out.println("+-----+----------------+----------------------+-----------+----------+-------------+");
    }

    /** Builds a multi-criteria operational summary for management review. */
    private void managementReportGenerator() {
        utility.UIUtils.printSubHeader("MODULE 4 > LOYALTY REPORT GENERATOR", utility.UIUtils.MAGENTA);
        System.out.println("Create a ranked loyalty summary using member search and multiple filters.");
        System.out.println("Type 'cancel' at any prompt to abandon this report.");

        String tierFilter;
        do {
            System.out.print("Tier filter (ALL / STANDARD / SILVER / GOLD / PLATINUM / DIAMOND): ");
            tierFilter = utility.UIUtils.safeReadLine(scanner).toUpperCase();
            if (isCancelled(tierFilter)) {
                printCancellationMessage();
                return;
            }
            if (tierFilter.isEmpty()) tierFilter = "ALL";
        } while (!"ALL".equals(tierFilter) && !"STANDARD".equals(tierFilter)
                && !"SILVER".equals(tierFilter) && !"GOLD".equals(tierFilter)
                && !"PLATINUM".equals(tierFilter) && !"DIAMOND".equals(tierFilter));

        Integer minimumPoints = readNonNegativeInteger("Minimum points (0 for no minimum, or 'cancel'): ");
        if (minimumPoints == null) {
            printCancellationMessage();
            return;
        }
        Integer expiryWindow = readNonNegativeInteger("Points expiry window in days (0 to ignore expiry, or 'cancel'): ");
        if (expiryWindow == null) {
            printCancellationMessage();
            return;
        }
        System.out.print("Search by member ID or member name (press ENTER for all, or 'cancel'): ");
        String memberSearch = utility.UIUtils.safeReadLine(scanner);
        if (isCancelled(memberSearch)) {
            printCancellationMessage();
            return;
        }

        DoublyLinkedList<LoyaltyController.ManagementReportEntry> report =
                controller.generateManagementReport(
                        tierFilter, minimumPoints, expiryWindow, memberSearch);

        System.out.println("\n+============================================================================+ ");
        System.out.println("                 LOYALTY & REWARDS MANAGEMENT REPORT");
        System.out.println("+============================================================================+");
        System.out.println("  Tier filter     : " + tierFilter);
        System.out.println("  Minimum points  : " + minimumPoints);
        System.out.println("  Expiry filter   : " + (expiryWindow == 0 ? "Not applied"
                : "Expires within " + expiryWindow + " days"));
        System.out.println("  Member search   : " + (memberSearch.isEmpty() ? "All members" : memberSearch));
        System.out.println("+-----+----------------+--------------------+----------+--------+------------+-------+");
        System.out.println("| No. | Member ID      | Member Name        | Tier     | Points | Expiry     | Txns  |");
        System.out.println("+-----+----------------+--------------------+----------+--------+------------+-------+");

        if (report.isEmpty()) {
            System.out.println("|                         No members match the selected filters.              |");
            System.out.println("+-----+----------------+--------------------+----------+--------+------------+-------+");
            return;
        }

        int totalPoints = 0;
        int totalTransactions = 0;
        for (int i = 1; i <= report.getNumberOfEntries(); i++) {
            LoyaltyController.ManagementReportEntry entry = report.getEntry(i);
            totalPoints += entry.getTotalPoints();
            totalTransactions += entry.getRedemptionCount();
            String expiry = entry.getExpiryDate() == null || entry.getExpiryDate().isEmpty()
                    ? "N/A" : entry.getExpiryDate();
            System.out.printf("| %-3d | %-14s | %-18s | %-8s | %-6d | %-10s | %-5d |%n",
                    i, entry.getMemberId(), entry.getMemberName(), entry.getTier(),
                    entry.getTotalPoints(), expiry, entry.getRedemptionCount());
        }
        System.out.println("+-----+----------------+--------------------+----------+--------+------------+-------+");
        System.out.println("  Matched members       : " + report.getNumberOfEntries());
        System.out.println("  Total points held     : " + totalPoints);
        System.out.println("  Redemption transactions: " + totalTransactions);
        System.out.println("  Method: linear member search + multi-criteria filtering + MergeSort ranking.");
    }

    private int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }

    /** Reads a whole-number filter while allowing the current operation to be cancelled. */
    private Integer readNonNegativeInteger(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = utility.UIUtils.safeReadLine(scanner);
            if (isCancelled(input)) return null;

            try {
                int value = Integer.parseInt(input);
                if (value >= 0) return value;
            } catch (NumberFormatException e) {
                // Show the same validation message for non-numeric input.
            }
            System.out.println("  [!] ERROR: Please enter a whole number that is zero or greater.");
        }
    }

    private boolean isCancelled(String input) {
        return "cancel".equalsIgnoreCase(input == null ? "" : input.trim());
    }

    private void printCancellationMessage() {
        System.out.println("\n  [!] Input cancelled. No changes were made.");
    }
}