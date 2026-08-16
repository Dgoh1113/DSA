package boundary;

import adt.DoublyLinkedList;
import control.LoyaltyController;
import entity.Guest;
import entity.LoyaltyAccount;
import entity.RedemptionTransaction;
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
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "4." + utility.UIUtils.RESET + " Report: Top Point Earners (MergeSort O(n log n))");
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "5." + utility.UIUtils.RESET + " Report: Expiring Points (QuickSort O(n log n))");
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "6." + utility.UIUtils.RESET + " Report: Members by Tier");
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "7." + utility.UIUtils.RESET + " View All Members");

        utility.UIUtils.printSectionHeader("NAVIGATION", utility.UIUtils.RED);
        System.out.println("  " + utility.UIUtils.RED + utility.UIUtils.BOLD + "0." + utility.UIUtils.RESET + " Back to Main Menu");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.print(utility.UIUtils.BOLD + "Enter your choice: " + utility.UIUtils.RESET);
    }

    private void viewMemberProfile() {
        utility.UIUtils.printSubHeader("MODULE 4 > VIEW MEMBER PROFILE", utility.UIUtils.MAGENTA);
        System.out.print("Enter Member / Guest ID: ");
        String memberId = utility.UIUtils.safeReadLine(scanner);

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
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    memberId = res.getValue();
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
                    System.out.println("  1. Room Upgrade     - 500 pts");
                    System.out.println("  2. Free Breakfast   - 300 pts");
                    System.out.println("  3. Spa Voucher      - 800 pts");
                    System.out.println("  4. Late Checkout    - 200 pts");
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 2/2 - Select reward (1-4) or enter reward name", rewardItem, false);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;

                    String input = res.getValue();
                    if (input.equals("1")) { rewardItem = "Room Upgrade"; pointsCost = 500; }
                    else if (input.equals("2")) { rewardItem = "Free Breakfast"; pointsCost = 300; }
                    else if (input.equals("3")) { rewardItem = "Spa Voucher"; pointsCost = 800; }
                    else if (input.equals("4")) { rewardItem = "Late Checkout"; pointsCost = 200; }
                    else {
                        rewardItem = input;
                        pointsCost = 200;
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

    private void viewTransactionHistory() {
        utility.UIUtils.printSubHeader("MODULE 4 > VIEW TRANSACTION HISTORY", utility.UIUtils.MAGENTA);
        System.out.print("Enter Member / Guest ID: ");
        String memberId = utility.UIUtils.safeReadLine(scanner);

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
        System.out.println("Sorted using MergeSort algorithm — O(n log n)");
    }

    private void expiringPointsReport() {
        utility.UIUtils.printSubHeader("MODULE 4 > REPORT: EXPIRING POINTS (QUICKSORT)", utility.UIUtils.MAGENTA);
        System.out.print("Days threshold (e.g., 30): ");
        int days = utility.UIUtils.safeReadInt(scanner);

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
        System.out.println("Sorted using QuickSort algorithm — O(n log n)");
    }

    private void tierReport() {
        utility.UIUtils.printSubHeader("MODULE 4 > REPORT: MEMBERS BY TIER", utility.UIUtils.MAGENTA);
        System.out.println("Select Tier: STANDARD | SILVER | GOLD | PLATINUM | DIAMOND");
        String tier = utility.ValidationUtils.getValidLoyaltyTier(scanner, "Tier: ");

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

        System.out.println("+-----+----------------+-----------+----------+-------------+");
        System.out.println("| No. | Member ID      | Points    | Tier     | Expiry Date |");
        System.out.println("+-----+----------------+-----------+----------+-------------+");

        for (int i = 1; i <= all.getNumberOfEntries(); i++) {
            LoyaltyAccount acc = all.getEntry(i);
            System.out.printf("| %-3d | %-14s | %-9d | %-8s | %-11s |%n",
                    i, acc.getMemberId(), acc.getTotalPoints(),
                    acc.getTierStatus(),
                    acc.getPointsExpiryDate() != null ? acc.getPointsExpiryDate() : "N/A");
        }
        System.out.println("+-----+----------------+-----------+----------+-------------+");
    }

    private int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }
}
