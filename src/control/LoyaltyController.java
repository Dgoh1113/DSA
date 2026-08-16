package control;

import adt.DoublyLinkedList;
import adt.SortAlgorithms;
import entity.Guest;
import entity.LoyaltyAccount;
import entity.RedemptionTransaction;

/**
 * Controller: Module 4 — Loyalty and Rewards Service.
 * Handles points accumulation, tier progressions, redemptions, and analytical reports.
 *
 * Business Rules:
 * - Points = nightlyRate × numberOfNights (1 point per dollar).
 * - Tier thresholds: STANDARD(0), SILVER(500), GOLD(2000), PLATINUM(5000), DIAMOND(10000).
 * - Tier upgrades are automatic when accumulated points exceed thresholds.
 * - MergeSort / QuickSort (O(n log n)) used for management report generation.
 * - Supports multi-criteria filtering (by tier, expiry date, etc.).
 *
 * Data Structures:
 * - Doubly Linked List for loyalty accounts and transaction records.
 * - MergeSort for ranking top point earners.
 * - QuickSort for listing members with points expiring soon.
 */
public class LoyaltyController {

    // Tier thresholds
    public static final int SILVER_THRESHOLD = 500;
    public static final int GOLD_THRESHOLD = 2000;
    public static final int PLATINUM_THRESHOLD = 5000;
    public static final int DIAMOND_THRESHOLD = 10000;

    private DoublyLinkedList<Guest> guestRegistry;
    private DoublyLinkedList<LoyaltyAccount> loyaltyAccounts;
    private DoublyLinkedList<RedemptionTransaction> redemptionLog;
    private UndoController undoController;

    public LoyaltyController(DoublyLinkedList<Guest> guestRegistry,
                             DoublyLinkedList<LoyaltyAccount> loyaltyAccounts,
                             DoublyLinkedList<RedemptionTransaction> redemptionLog) {
        this.guestRegistry = guestRegistry;
        this.loyaltyAccounts = loyaltyAccounts;
        this.redemptionLog = redemptionLog;
    }

    public void setUndoController(UndoController undoController) {
        this.undoController = undoController;
    }

    /**
     * Accrues loyalty points after check-out.
     * Points = nightlyRate × numberOfNights (1 point per dollar).
     * Automatically checks and upgrades tier if threshold exceeded.
     */
    public void accruePoints(String guestId, double nightlyRate, int nights) {
        LoyaltyAccount account = findOrCreateAccount(guestId);
        int pointsEarned = (int) (nightlyRate * nights);

        account.setTotalPoints(account.getTotalPoints() + pointsEarned);
        account.addHistoryEntry("+" + pointsEarned + " pts (Stay: " + nights
                + " nights @ $" + nightlyRate + "/night)");

        // Set expiry date to 1 year from now (simplified)
        account.setPointsExpiryDate(getExpiryDate());

        // Check for tier upgrade
        checkAndUpgradeTier(guestId);
    }

    /** Credits the loyalty account belonging to a registered phone number. */
    public void accruePointsByContactNo(String contactNo, double nightlyRate, int nights) {
        Guest guest = findGuestByContactNo(contactNo);
        if (guest != null) {
            accruePoints(guest.getGuestId(), nightlyRate, nights);
        }
    }

    /**
     * Evaluates the guest's total points against tier thresholds
     * and upgrades their tier if qualified.
     * Updates both the LoyaltyAccount and the Guest entity.
     */
    public void checkAndUpgradeTier(String guestId) {
        LoyaltyAccount account = findAccount(guestId);
        if (account == null) return;

        int points = account.getTotalPoints();
        String newTier;

        if (points >= DIAMOND_THRESHOLD) {
            newTier = "DIAMOND";
        } else if (points >= PLATINUM_THRESHOLD) {
            newTier = "PLATINUM";
        } else if (points >= GOLD_THRESHOLD) {
            newTier = "GOLD";
        } else if (points >= SILVER_THRESHOLD) {
            newTier = "SILVER";
        } else {
            newTier = "STANDARD";
        }

        String oldTier = account.getTierStatus();
        if (!oldTier.equals(newTier)) {
            account.setTierStatus(newTier);
            account.addHistoryEntry("Tier upgrade: " + oldTier + " -> " + newTier);

            // Update guest entity tier as well
            Guest guest = findGuest(guestId);
            if (guest != null) {
                guest.setLoyaltyTier(newTier);
            }
        }
    }

    /**
     * Redeems points for a reward item.
     * Creates a RedemptionTransaction audit record.
     *
     * @return The RedemptionTransaction if successful, null if insufficient points.
     */
    public RedemptionTransaction redeemPoints(String memberId, String rewardItem, int pointsCost) {
        LoyaltyAccount account = findAccount(memberId);
        if (account == null || account.getTotalPoints() < pointsCost) {
            return null; // Insufficient points or account not found
        }

        // Deduct points
        account.setTotalPoints(account.getTotalPoints() - pointsCost);
        account.addHistoryEntry("-" + pointsCost + " pts (Redeemed: " + rewardItem + ")");

        // Create transaction record
        RedemptionTransaction txn = new RedemptionTransaction(
                memberId, rewardItem, pointsCost, getCurrentDate());
        redemptionLog.add(txn);

        // Re-evaluate tier after point deduction
        checkAndUpgradeTier(memberId);

        // Record Undo Action
        if (undoController != null) {
            undoController.recordAction(
                "REDEEM_POINTS",
                "Module 4: Loyalty & Rewards",
                "Redeemed " + pointsCost + " pts for '" + rewardItem + "' (Member " + memberId + ")",
                () -> {
                    account.setTotalPoints(account.getTotalPoints() + pointsCost);
                    account.addHistoryEntry("+" + pointsCost + " pts (REFUND: Undone " + rewardItem + ")");
                    // Remove transaction from redemption log
                    for (int i = 1; i <= redemptionLog.getNumberOfEntries(); i++) {
                        if (redemptionLog.getEntry(i).equals(txn)) {
                            redemptionLog.remove(i);
                            break;
                        }
                    }
                    checkAndUpgradeTier(memberId);
                }
            );
        }

        return txn;
    }

    /**
     * Generates a report of top point earners, sorted by totalPoints descending.
     * Uses MergeSort — O(n log n) stable sort.
     *
     * @return A new DoublyLinkedList sorted by points (highest first).
     */
    public DoublyLinkedList<LoyaltyAccount> generateTopEarnersReport() {
        ensureAllGuestsHaveAccounts();
        return SortAlgorithms.mergeSort(loyaltyAccounts,
                (a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints()));
    }

    /**
     * Generates a report of members with points expiring within the given threshold.
     * Uses QuickSort — O(n log n) average case.
     * Filters by expiry date, then sorts by points descending.
     *
     * @param daysThreshold Number of days from now to check for expiry.
     * @return Filtered and sorted list of accounts with expiring points.
     */
    public DoublyLinkedList<LoyaltyAccount> generateExpiringPointsReport(int daysThreshold) {
        // Filter accounts with expiring points
        DoublyLinkedList<LoyaltyAccount> filtered = new DoublyLinkedList<>();
        for (int i = 1; i <= loyaltyAccounts.getNumberOfEntries(); i++) {
            LoyaltyAccount account = loyaltyAccounts.getEntry(i);
            if (account.getPointsExpiryDate() != null && account.getTotalPoints() > 0) {
                if (isExpiringWithinDays(account.getPointsExpiryDate(), daysThreshold)) {
                    filtered.add(account);
                }
            }
        }

        // Sort by points descending using QuickSort
        return SortAlgorithms.quickSort(filtered,
                (a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints()));
    }

    /**
     * Generates a report filtered by a specific tier.
     * Uses MergeSort to order by points within the tier.
     *
     * @param tier The loyalty tier to filter by.
     * @return Sorted list of accounts matching the tier.
     */
    public DoublyLinkedList<LoyaltyAccount> generateTierReport(String tier) {
        ensureAllGuestsHaveAccounts();
        DoublyLinkedList<LoyaltyAccount> filtered = new DoublyLinkedList<>();
        for (int i = 1; i <= loyaltyAccounts.getNumberOfEntries(); i++) {
            LoyaltyAccount account = loyaltyAccounts.getEntry(i);
            if (account.getTierStatus().equals(tier)) {
                filtered.add(account);
            }
        }
        return SortAlgorithms.mergeSort(filtered,
                (a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints()));
    }

    /**
     * Retrieves the transaction history for a specific member.
     */
    public DoublyLinkedList<RedemptionTransaction> getTransactionHistory(String memberId) {
        DoublyLinkedList<RedemptionTransaction> history = new DoublyLinkedList<>();
        for (int i = 1; i <= redemptionLog.getNumberOfEntries(); i++) {
            RedemptionTransaction txn = redemptionLog.getEntry(i);
            if (txn.getMemberId().equals(memberId)) {
                history.add(txn);
            }
        }
        return history;
    }

    /**
     * Views a specific member's loyalty profile.
     */
    public LoyaltyAccount viewMemberProfile(String memberId) {
        LoyaltyAccount account = findAccount(memberId);
        if (account != null) return account;

        Guest guest = findGuest(memberId);
        if (guest == null) return null;

        account = new LoyaltyAccount(guest.getGuestId());
        account.setTierStatus(guest.getLoyaltyTier());
        loyaltyAccounts.add(account);
        return account;
    }

    /** Returns true when a member has positive points expiring within the given period. */
    public boolean hasPointsExpiringWithinDays(String memberId, int daysThreshold) {
        LoyaltyAccount account = findAccount(memberId);
        if (account == null || account.getTotalPoints() <= 0
                || account.getPointsExpiryDate() == null) {
            return false;
        }
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate expiry = java.time.LocalDate.parse(account.getPointsExpiryDate());
            java.time.LocalDate threshold = today.plusDays(daysThreshold);
            return !expiry.isBefore(today) && !expiry.isAfter(threshold);
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns the guest details linked to a loyalty member ID. */
    public Guest viewMemberGuest(String memberId) {
        return findGuest(memberId);
    }

    /**
     * Returns all loyalty accounts.
     */
    public DoublyLinkedList<LoyaltyAccount> getAllAccounts() {
        ensureAllGuestsHaveAccounts();
        return loyaltyAccounts;
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    /**
     * Finds an existing loyalty account by member/guest ID.
     */
    public LoyaltyAccount findAccount(String memberId) {
        if (memberId == null) return null;
        String normalizedId = memberId.trim();
        for (int i = 1; i <= loyaltyAccounts.getNumberOfEntries(); i++) {
            LoyaltyAccount account = loyaltyAccounts.getEntry(i);
            if (account.getMemberId().equalsIgnoreCase(normalizedId)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Finds or creates a loyalty account for a guest.
     */
    private LoyaltyAccount findOrCreateAccount(String guestId) {
        LoyaltyAccount account = findAccount(guestId);
        if (account == null) {
            account = new LoyaltyAccount(guestId);
            Guest guest = findGuest(guestId);
            if (guest != null) {
                account.setTierStatus(guest.getLoyaltyTier());
            }
            loyaltyAccounts.add(account);
        }
        return account;
    }

    /**
     * Finds a guest by guestId.
     */
    private Guest findGuest(String guestId) {
        if (guestId == null) return null;
        String normalizedId = guestId.trim();
        for (int i = 1; i <= guestRegistry.getNumberOfEntries(); i++) {
            Guest guest = guestRegistry.getEntry(i);
            if (guest.getGuestId().equalsIgnoreCase(normalizedId)) {
                return guest;
            }
        }
        return null;
    }

    private Guest findGuestByContactNo(String contactNo) {
        String normalizedContact = normalizeContactNo(contactNo);
        if (normalizedContact.isEmpty()) return null;
        for (int i = 1; i <= guestRegistry.getNumberOfEntries(); i++) {
            Guest guest = guestRegistry.getEntry(i);
            if (guest != null
                    && normalizedContact.equals(normalizeContactNo(guest.getContactNo()))) {
                return guest;
            }
        }
        return null;
    }

    private String normalizeContactNo(String contactNo) {
        if (contactNo == null) return "";
        String digits = contactNo.replaceAll("[^0-9]", "");
        if (digits.startsWith("0060")) digits = digits.substring(2);
        if (digits.startsWith("60")) digits = "0" + digits.substring(2);
        return digits;
    }

    /** Enrols newly registered guests so they appear in loyalty member listings immediately. */
    private void ensureAllGuestsHaveAccounts() {
        for (int i = 1; i <= guestRegistry.getNumberOfEntries(); i++) {
            Guest guest = guestRegistry.getEntry(i);
            if (guest != null && findAccount(guest.getGuestId()) == null) {
                LoyaltyAccount account = new LoyaltyAccount(guest.getGuestId());
                account.setTierStatus(guest.getLoyaltyTier());
                loyaltyAccounts.add(account);
            }
        }
    }

    /**
     * Returns a date string 1 year from now (simplified format).
     */
    private String getExpiryDate() {
        // Simplified: just add 1 to the year
        java.time.LocalDate now = java.time.LocalDate.now();
        return now.plusYears(1).toString();
    }

    /**
     * Returns the current date as a string.
     */
    private String getCurrentDate() {
        return java.time.LocalDate.now().toString();
    }

    /**
     * Checks if a date string (YYYY-MM-DD) is within the given number of days from today.
     */
    private boolean isExpiringWithinDays(String expiryDateStr, int daysThreshold) {
        try {
            java.time.LocalDate expiryDate = java.time.LocalDate.parse(expiryDateStr);
            java.time.LocalDate threshold = java.time.LocalDate.now().plusDays(daysThreshold);
            return !expiryDate.isAfter(threshold);
        } catch (Exception e) {
            return false;
        }
    }
}
