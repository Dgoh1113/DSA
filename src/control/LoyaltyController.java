package control;

import adt.DoublyLinkedList;
import adt.BinarySearchTree;
import adt.SortAlgorithms;
import entity.Guest;
import entity.LoyaltyAccount;
import entity.RedemptionTransaction;
import entity.Reservation;
import entity.Room;

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
    private DoublyLinkedList<Room> roomInventory;
    private BinarySearchTree<Reservation> reservationRegistry;

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

    /** Supplies the shared reservation and room records needed for room upgrades. */
    public void setReservationResources(DoublyLinkedList<Room> roomInventory,
                                        BinarySearchTree<Reservation> reservationRegistry) {
        this.roomInventory = roomInventory;
        this.reservationRegistry = reservationRegistry;
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
        if (findGuest(memberId) == null || account == null || account.getTotalPoints() < pointsCost) {
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
     * Returns a member's active bookings that can be considered for a room
     * upgrade. Suite bookings are intentionally included so the UI can explain
     * that they cannot be upgraded further.
     */
    public DoublyLinkedList<Reservation> getRoomUpgradeBookings(String memberId) {
        DoublyLinkedList<Reservation> bookings = new DoublyLinkedList<>();
        if (reservationRegistry == null || memberId == null || memberId.trim().isEmpty()) return bookings;

        String normalizedMemberId = memberId.trim();

        DoublyLinkedList<Reservation> reservations = reservationRegistry.inOrderTraversal();
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            if (reservation != null
                    && normalizedMemberId.equalsIgnoreCase(reservation.getGuestId())
                    && ("PENDING".equals(reservation.getBookingStatus())
                        || "CONFIRMED".equals(reservation.getBookingStatus()))) {
                bookings.add(reservation);
            }
        }
        return bookings;
    }

    /**
     * Applies a one-level room upgrade to an active reservation:
     * STANDARD -> DELUXE or DELUXE -> SUITE. Points are deducted only after a
     * suitable higher-tier room has been found and the booking is updated.
     */
    public RoomUpgradeResult redeemRoomUpgrade(String memberId, String confirmationNo,
                                                int pointsCost) {
        LoyaltyAccount account = findAccount(memberId);
        if (account == null) {
            return RoomUpgradeResult.failure("No loyalty account was found for this member.");
        }
        if (roomInventory == null || reservationRegistry == null) {
            return RoomUpgradeResult.failure("Room upgrade service is not configured.");
        }
        if (account.getTotalPoints() < pointsCost) {
            return RoomUpgradeResult.failure("Insufficient points. You need " + pointsCost
                    + " points but have " + account.getTotalPoints() + ".");
        }

        Reservation reservation = findReservation(confirmationNo);
        if (reservation == null) {
            return RoomUpgradeResult.failure("No booking was found for that confirmation number.");
        }
        if (!belongsToMember(reservation, memberId)) {
            return RoomUpgradeResult.failure("This booking belongs to another guest. Enter a confirmation number for Guest ID "
                    + account.getMemberId() + ".");
        }
        if (!isRoomUpgradeEligible(reservation)) {
            return RoomUpgradeResult.failure("This booking is not active and cannot be upgraded.");
        }

        String currentType = reservation.getRoomType();
        if ("SUITE".equals(currentType)) {
            return RoomUpgradeResult.failure("This booking is already a SUITE and cannot be upgraded further.");
        }

        String upgradedType;
        if ("STANDARD".equals(currentType)) {
            upgradedType = "DELUXE";
        } else if ("DELUXE".equals(currentType)) {
            upgradedType = "SUITE";
        } else {
            return RoomUpgradeResult.failure("This booking has an unsupported room type: " + currentType + ".");
        }

        Room currentRoom = findRoom(reservation.getAssignedRoomNo());
        if ("CONFIRMED".equals(reservation.getBookingStatus()) && currentRoom == null) {
            return RoomUpgradeResult.failure("The booked room could not be found. The upgrade was not applied.");
        }
        Room upgradedRoom = findAvailableRoom(upgradedType);
        if (upgradedRoom == null) {
            return RoomUpgradeResult.failure("No " + upgradedType
                    + " room is currently available. The booking was not changed.");
        }

        String oldRoomNo = reservation.getAssignedRoomNo();
        String oldBookingStatus = reservation.getBookingStatus();
        reservationRegistry.delete(reservation);
        if (currentRoom != null) currentRoom.setStatus("AVAILABLE");
        upgradedRoom.setStatus("OCCUPIED");
        reservation.setRoomType(upgradedType);
        reservation.setAssignedRoomNo(upgradedRoom.getRoomNo());
        reservation.setBookingStatus("CONFIRMED");
        reservationRegistry.insert(reservation);

        account.setTotalPoints(account.getTotalPoints() - pointsCost);
        String reward = "Room Upgrade (" + currentType + " -> " + upgradedType + ")";
        account.addHistoryEntry("-" + pointsCost + " pts (Redeemed: " + reward + ")");
        RedemptionTransaction transaction = new RedemptionTransaction(
                memberId, reward, pointsCost, getCurrentDate());
        redemptionLog.add(transaction);
        checkAndUpgradeTier(memberId);

        if (undoController != null) {
            undoController.recordAction(
                "ROOM_UPGRADE_REDEMPTION",
                "Module 4: Loyalty & Rewards",
                "Upgraded Conf #" + confirmationNo + " from " + currentType + " to " + upgradedType,
                () -> {
                    reservationRegistry.delete(reservation);
                    upgradedRoom.setStatus("AVAILABLE");
                    if (currentRoom != null) currentRoom.setStatus("OCCUPIED");
                    reservation.setRoomType(currentType);
                    reservation.setAssignedRoomNo(oldRoomNo);
                    reservation.setBookingStatus(oldBookingStatus);
                    reservationRegistry.insert(reservation);
                    account.setTotalPoints(account.getTotalPoints() + pointsCost);
                    account.addHistoryEntry("+" + pointsCost + " pts (REFUND: Undone " + reward + ")");
                    removeTransaction(transaction);
                    checkAndUpgradeTier(memberId);
                }
            );
        }

        return RoomUpgradeResult.success(transaction, reservation, currentType, upgradedType);
    }

    /**
     * Returns bookings whose check-out date can still be extended. A guest may
     * redeem late checkout before arrival, after confirmation, or while checked
     * in, but never after the stay has been checked out or cancelled.
     */
    public DoublyLinkedList<Reservation> getLateCheckoutBookings(String memberId) {
        DoublyLinkedList<Reservation> bookings = new DoublyLinkedList<>();
        if (reservationRegistry == null || memberId == null || memberId.trim().isEmpty()) return bookings;

        String normalizedMemberId = memberId.trim();

        DoublyLinkedList<Reservation> reservations = reservationRegistry.inOrderTraversal();
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            if (reservation != null && normalizedMemberId.equalsIgnoreCase(reservation.getGuestId())
                    && isLateCheckoutEligible(reservation)) {
                bookings.add(reservation);
            }
        }
        return bookings;
    }

    /**
     * Redeems a late checkout for one booking. The reward extends the recorded
     * checkout date by exactly one calendar day, so billing and the eventual
     * checkout log use the new stay length.
     */
    public LateCheckoutResult redeemLateCheckout(String memberId, String confirmationNo,
                                                  int pointsCost) {
        LoyaltyAccount account = findAccount(memberId);
        if (account == null) {
            return LateCheckoutResult.failure("No loyalty account was found for this member.");
        }
        if (reservationRegistry == null) {
            return LateCheckoutResult.failure("Late checkout service is not configured.");
        }
        if (account.getTotalPoints() < pointsCost) {
            return LateCheckoutResult.failure("Insufficient points. You need " + pointsCost
                    + " points but have " + account.getTotalPoints() + ".");
        }

        Reservation reservation = findReservation(confirmationNo);
        if (reservation == null) {
            return LateCheckoutResult.failure("No booking was found for that confirmation number.");
        }
        if (!belongsToMember(reservation, memberId)) {
            return LateCheckoutResult.failure("This booking belongs to another guest. Enter a confirmation number for Guest ID "
                    + account.getMemberId() + ".");
        }
        if (!isLateCheckoutStatusEligible(reservation)) {
            return LateCheckoutResult.failure("Late checkout is unavailable for a cancelled or checked-out booking.");
        }
        if (!hasAssignedRoom(reservation)) {
            return LateCheckoutResult.failure("Late checkout requires a room to be assigned to this booking first.");
        }

        String previousCheckOutDate = reservation.getCheckOutDate();
        final String extendedCheckOutDate;
        try {
            extendedCheckOutDate = java.time.LocalDate.parse(previousCheckOutDate)
                    .plusDays(1).toString();
        } catch (Exception e) {
            return LateCheckoutResult.failure("This booking has an invalid check-out date and cannot be extended.");
        }

        reservation.setCheckOutDate(extendedCheckOutDate);
        account.setTotalPoints(account.getTotalPoints() - pointsCost);
        String reward = "Late Checkout (Conf #" + reservation.getConfirmationNo() + ": "
                + previousCheckOutDate + " -> " + extendedCheckOutDate + ")";
        account.addHistoryEntry("-" + pointsCost + " pts (Redeemed: " + reward + ")");
        RedemptionTransaction transaction = new RedemptionTransaction(
                memberId, reward, pointsCost, getCurrentDate());
        redemptionLog.add(transaction);
        checkAndUpgradeTier(memberId);

        if (undoController != null) {
            undoController.recordAction(
                "LATE_CHECKOUT_REDEMPTION",
                "Module 4: Loyalty & Rewards",
                "Extended Conf #" + confirmationNo + " check-out from "
                        + previousCheckOutDate + " to " + extendedCheckOutDate,
                () -> {
                    reservation.setCheckOutDate(previousCheckOutDate);
                    account.setTotalPoints(account.getTotalPoints() + pointsCost);
                    account.addHistoryEntry("+" + pointsCost
                            + " pts (REFUND: Undone " + reward + ")");
                    removeTransaction(transaction);
                    checkAndUpgradeTier(memberId);
                }
            );
        }

        return LateCheckoutResult.success(transaction, reservation,
                previousCheckOutDate, extendedCheckOutDate);
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
    public DoublyLinkedList<LoyaltyAccount> generateTierReport(DoublyLinkedList<String> tiers) {
        ensureAllGuestsHaveAccounts();
        DoublyLinkedList<LoyaltyAccount> filtered = new DoublyLinkedList<>();
        
        boolean allowAllTiers = tiers.isEmpty();
        for (int i = 1; i <= tiers.getNumberOfEntries(); i++) {
            if ("ALL".equalsIgnoreCase(tiers.getEntry(i))) {
                allowAllTiers = true;
                break;
            }
        }

        for (int i = 1; i <= loyaltyAccounts.getNumberOfEntries(); i++) {
            LoyaltyAccount account = loyaltyAccounts.getEntry(i);
            boolean matched = allowAllTiers;
            if (!allowAllTiers) {
                for (int j = 1; j <= tiers.getNumberOfEntries(); j++) {
                    if (tiers.getEntry(j).equalsIgnoreCase(account.getTierStatus())) {
                        matched = true;
                        break;
                    }
                }
            }
            if (matched) {
                filtered.add(account);
            }
        }
        return SortAlgorithms.mergeSort(filtered,
                (a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints()));
    }

    /**
     * Builds a management-ready loyalty report by searching member records,
     * applying all selected filters, then ranking the matches by points.
     *
     * @param tierFilters      Loyalty tiers, or ALL for every tier.
     * @param minimumPoints    Minimum current points required.
     * @param expiryWithinDays Positive value filters to points expiring in that
     *                         period; zero disables the expiry filter.
     * @param memberSearchTerm Optional member ID or guest-name search text.
     */
    public DoublyLinkedList<ManagementReportEntry> generateManagementReport(
            DoublyLinkedList<String> tierFilters, int minimumPoints, int expiryWithinDays,
            String memberSearchTerm) {
        ensureAllGuestsHaveAccounts();
        DoublyLinkedList<ManagementReportEntry> matches = new DoublyLinkedList<>();
        
        boolean allowAllTiers = tierFilters.isEmpty();
        for (int i = 1; i <= tierFilters.getNumberOfEntries(); i++) {
            if ("ALL".equalsIgnoreCase(tierFilters.getEntry(i))) {
                allowAllTiers = true;
                break;
            }
        }
        
        String normalizedSearch = memberSearchTerm == null
                ? "" : memberSearchTerm.trim().toLowerCase();
        int pointsThreshold = Math.max(0, minimumPoints);

        for (int i = 1; i <= loyaltyAccounts.getNumberOfEntries(); i++) {
            LoyaltyAccount account = loyaltyAccounts.getEntry(i);
            Guest guest = findGuest(account.getMemberId()); // member-record search
            String guestName = guest == null ? "Unknown" : guest.getName();

            boolean matchesTier = allowAllTiers;
            if (!allowAllTiers) {
                for (int j = 1; j <= tierFilters.getNumberOfEntries(); j++) {
                    if (tierFilters.getEntry(j).equalsIgnoreCase(account.getTierStatus())) {
                        matchesTier = true;
                        break;
                    }
                }
            }
            
            boolean matchesPoints = account.getTotalPoints() >= pointsThreshold;
            boolean matchesExpiry = expiryWithinDays <= 0
                    || hasExpiryWithinDays(account.getPointsExpiryDate(), expiryWithinDays);
            boolean matchesSearch = normalizedSearch.isEmpty()
                    || account.getMemberId().toLowerCase().contains(normalizedSearch)
                    || guestName.toLowerCase().contains(normalizedSearch);

            if (matchesTier && matchesPoints && matchesExpiry && matchesSearch) {
                matches.add(new ManagementReportEntry(
                        account.getMemberId(), guestName, account.getTierStatus(),
                        account.getTotalPoints(), account.getPointsExpiryDate(),
                        countRedemptions(account.getMemberId())));
            }
        }

        // Stable MergeSort creates a management ranking: highest points first,
        // then member ID for consistent ordering when points are equal.
        return SortAlgorithms.mergeSort(matches, (first, second) -> {
            int pointComparison = Integer.compare(second.getTotalPoints(), first.getTotalPoints());
            return pointComparison != 0 ? pointComparison
                    : first.getMemberId().compareTo(second.getMemberId());
        });
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

    /** Value object used by the multi-filter loyalty management report. */
    public static class ManagementReportEntry {
        private final String memberId;
        private final String memberName;
        private final String tier;
        private final int totalPoints;
        private final String expiryDate;
        private final int redemptionCount;

        public ManagementReportEntry(String memberId, String memberName, String tier,
                                     int totalPoints, String expiryDate, int redemptionCount) {
            this.memberId = memberId;
            this.memberName = memberName;
            this.tier = tier;
            this.totalPoints = totalPoints;
            this.expiryDate = expiryDate;
            this.redemptionCount = redemptionCount;
        }

        public String getMemberId() { return memberId; }
        public String getMemberName() { return memberName; }
        public String getTier() { return tier; }
        public int getTotalPoints() { return totalPoints; }
        public String getExpiryDate() { return expiryDate; }
        public int getRedemptionCount() { return redemptionCount; }
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

    private Reservation findActiveBooking(String memberId, String confirmationNo) {
        if (confirmationNo == null) return null;
        DoublyLinkedList<Reservation> bookings = getRoomUpgradeBookings(memberId);
        for (int i = 1; i <= bookings.getNumberOfEntries(); i++) {
            Reservation booking = bookings.getEntry(i);
            if (confirmationNo.trim().equals(booking.getConfirmationNo())) {
                return booking;
            }
        }
        return null;
    }

    /** Looks up a reservation by confirmation number without bypassing ownership checks. */
    private Reservation findReservation(String confirmationNo) {
        if (reservationRegistry == null || confirmationNo == null || confirmationNo.trim().isEmpty()) {
            return null;
        }
        return reservationRegistry.search(Reservation.lookupKey(confirmationNo.trim()));
    }

    private boolean belongsToMember(Reservation reservation, String memberId) {
        return reservation != null && reservation.getGuestId() != null && memberId != null
                && reservation.getGuestId().equalsIgnoreCase(memberId.trim());
    }

    private boolean isRoomUpgradeEligible(Reservation reservation) {
        String status = reservation.getBookingStatus();
        return "PENDING".equals(status) || "CONFIRMED".equals(status);
    }

    private Reservation findLateCheckoutBooking(String memberId, String confirmationNo) {
        if (confirmationNo == null) return null;
        DoublyLinkedList<Reservation> bookings = getLateCheckoutBookings(memberId);
        for (int i = 1; i <= bookings.getNumberOfEntries(); i++) {
            Reservation booking = bookings.getEntry(i);
            if (confirmationNo.trim().equals(booking.getConfirmationNo())) {
                return booking;
            }
        }
        return null;
    }

    private boolean isLateCheckoutEligible(Reservation reservation) {
        return hasAssignedRoom(reservation) && isLateCheckoutStatusEligible(reservation);
    }

    private boolean isLateCheckoutStatusEligible(Reservation reservation) {
        String status = reservation.getBookingStatus();
        return "PENDING".equals(status) || "CONFIRMED".equals(status)
                || "CHECKED_IN".equals(status);
    }

    private boolean hasAssignedRoom(Reservation reservation) {
        if (reservation == null || reservation.getAssignedRoomNo() == null
                || reservation.getAssignedRoomNo().trim().isEmpty()) {
            return false;
        }
        return roomInventory != null && findRoom(reservation.getAssignedRoomNo()) != null;
    }

    private Room findRoom(String roomNo) {
        if (roomNo == null) return null;
        for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
            Room room = roomInventory.getEntry(i);
            if (room != null && roomNo.equals(room.getRoomNo())) return room;
        }
        return null;
    }

    private Room findAvailableRoom(String roomType) {
        for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
            Room room = roomInventory.getEntry(i);
            if (room != null && roomType.equals(room.getRoomType())
                    && "AVAILABLE".equals(room.getStatus())) {
                return room;
            }
        }
        return null;
    }

    private int countRedemptions(String memberId) {
        int count = 0;
        for (int i = 1; i <= redemptionLog.getNumberOfEntries(); i++) {
            RedemptionTransaction transaction = redemptionLog.getEntry(i);
            if (transaction != null && memberId.equalsIgnoreCase(transaction.getMemberId())) {
                count++;
            }
        }
        return count;
    }

    private boolean hasExpiryWithinDays(String expiryDate, int days) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) return false;
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate expiry = java.time.LocalDate.parse(expiryDate);
            return !expiry.isBefore(today) && !expiry.isAfter(today.plusDays(days));
        } catch (Exception e) {
            return false;
        }
    }

    private void removeTransaction(RedemptionTransaction transaction) {
        for (int i = 1; i <= redemptionLog.getNumberOfEntries(); i++) {
            if (redemptionLog.getEntry(i).equals(transaction)) {
                redemptionLog.remove(i);
                return;
            }
        }
    }

    /** Result returned by a room-upgrade redemption, including an actionable error message. */
    public static class RoomUpgradeResult {
        private final RedemptionTransaction transaction;
        private final Reservation reservation;
        private final String previousRoomType;
        private final String upgradedRoomType;
        private final String errorMessage;

        private RoomUpgradeResult(RedemptionTransaction transaction, Reservation reservation,
                                  String previousRoomType, String upgradedRoomType,
                                  String errorMessage) {
            this.transaction = transaction;
            this.reservation = reservation;
            this.previousRoomType = previousRoomType;
            this.upgradedRoomType = upgradedRoomType;
            this.errorMessage = errorMessage;
        }

        public static RoomUpgradeResult success(RedemptionTransaction transaction,
                                                Reservation reservation, String previousRoomType,
                                                String upgradedRoomType) {
            return new RoomUpgradeResult(transaction, reservation, previousRoomType,
                    upgradedRoomType, null);
        }

        public static RoomUpgradeResult failure(String errorMessage) {
            return new RoomUpgradeResult(null, null, null, null, errorMessage);
        }

        public boolean isSuccessful() { return transaction != null; }
        public RedemptionTransaction getTransaction() { return transaction; }
        public Reservation getReservation() { return reservation; }
        public String getPreviousRoomType() { return previousRoomType; }
        public String getUpgradedRoomType() { return upgradedRoomType; }
        public String getErrorMessage() { return errorMessage; }
    }

    /** Result returned by a late-checkout redemption. */
    public static class LateCheckoutResult {
        private final RedemptionTransaction transaction;
        private final Reservation reservation;
        private final String previousCheckOutDate;
        private final String extendedCheckOutDate;
        private final String errorMessage;

        private LateCheckoutResult(RedemptionTransaction transaction, Reservation reservation,
                                   String previousCheckOutDate, String extendedCheckOutDate,
                                   String errorMessage) {
            this.transaction = transaction;
            this.reservation = reservation;
            this.previousCheckOutDate = previousCheckOutDate;
            this.extendedCheckOutDate = extendedCheckOutDate;
            this.errorMessage = errorMessage;
        }

        public static LateCheckoutResult success(RedemptionTransaction transaction,
                                                  Reservation reservation,
                                                  String previousCheckOutDate,
                                                  String extendedCheckOutDate) {
            return new LateCheckoutResult(transaction, reservation, previousCheckOutDate,
                    extendedCheckOutDate, null);
        }

        public static LateCheckoutResult failure(String errorMessage) {
            return new LateCheckoutResult(null, null, null, null, errorMessage);
        }

        public boolean isSuccessful() { return transaction != null; }
        public RedemptionTransaction getTransaction() { return transaction; }
        public Reservation getReservation() { return reservation; }
        public String getPreviousCheckOutDate() { return previousCheckOutDate; }
        public String getExtendedCheckOutDate() { return extendedCheckOutDate; }
        public String getErrorMessage() { return errorMessage; }
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
