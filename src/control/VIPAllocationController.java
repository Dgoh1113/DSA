package control;

import adt.BinaryMaxHeap;
import adt.BinarySearchTree;
import adt.DoublyLinkedList;
import adt.SortAlgorithms;
import entity.Guest;
import entity.LoyaltyAccount;
import entity.Reservation;
import entity.Room;

/**
 * Controller: Module 2 — VIP & Loyalty Tier Priority Room Allocation.
 * Handles all Binary Max-Heap operations for VIP priority scheduling.
 *
 * Business Rules:
 * - High-tier members (SILVER, GOLD, PLATINUM, DIAMOND) bypass standard waitlists.
 * - Priority Score = (Tier Weight × 10000) + Urgency Factor − Arrival Order Index
 * - Binary Max-Heap guarantees highest priority guest at root — O(1) peek.
 * - Insertion (sift-up) and extraction (sift-down) in O(log n).
 * - When rooms become available, extractMax() removes the highest-ranked VIP first.
 *
 * A wrapper class VIPReservation implements Comparable based on priorityScore
 * so the Max-Heap can order reservations by VIP priority.
 */
public class VIPAllocationController {

    private BinaryMaxHeap<VIPReservation> vipQueue;
    private DoublyLinkedList<Guest> guestRegistry;
    private DoublyLinkedList<LoyaltyAccount> loyaltyAccounts;
    private DoublyLinkedList<Room> roomInventory;
    private BinarySearchTree<Reservation> searchTree;
    private UndoController undoController;

    public VIPAllocationController(BinaryMaxHeap<VIPReservation> vipQueue,
                                   DoublyLinkedList<Guest> guestRegistry,
                                   DoublyLinkedList<LoyaltyAccount> loyaltyAccounts,
                                   DoublyLinkedList<Room> roomInventory,
                                   BinarySearchTree<Reservation> searchTree) {
        this.vipQueue = vipQueue;
        this.guestRegistry = guestRegistry;
        this.loyaltyAccounts = loyaltyAccounts;
        this.roomInventory = roomInventory;
        this.searchTree = searchTree;
    }

    public void setUndoController(UndoController undoController) {
        this.undoController = undoController;
    }

    /** Finds a registered guest by ID, ignoring letter case and surrounding whitespace. */
    public Guest findGuestById(String guestId) {
        if (guestId == null || guestId.trim().isEmpty()) return null;
        String normalizedId = guestId.trim();
        for (int i = 1; i <= guestRegistry.getNumberOfEntries(); i++) {
            Guest guest = guestRegistry.getEntry(i);
            if (guest != null && guest.getGuestId().equalsIgnoreCase(normalizedId)) {
                return guest;
            }
        }
        return null;
    }

    /** Returns the current loyalty points for a guest, or null if no loyalty account exists. */
    public Integer getCurrentPointsByGuestId(String guestId) {
        if (guestId == null || guestId.trim().isEmpty() || loyaltyAccounts == null) return null;
        String normalizedId = guestId.trim();
        for (int i = 1; i <= loyaltyAccounts.getNumberOfEntries(); i++) {
            LoyaltyAccount account = loyaltyAccounts.getEntry(i);
            if (account != null && account.getMemberId() != null
                    && account.getMemberId().equalsIgnoreCase(normalizedId)) {
                return account.getTotalPoints();
            }
        }
        return null;
    }

    /**
     * Registers a walk-in VIP booking from guest details entered at the desk.
     * Reuses a registered guest when the contact number already exists.
     */
    public Reservation addVIPBooking(String name, String icPassport, String contactNo,
                                     String email, String tier, String roomType,
                                     String checkInDate, String checkOutDate) {
        if (tier == null || "STANDARD".equalsIgnoreCase(tier.trim())) {
            return null;
        }
        String normalizedTier = tier.trim().toUpperCase();

        Guest guest = findGuestByContactNo(contactNo);
        if (guest == null) {
            guest = new Guest(name, icPassport, contactNo, email, normalizedTier);
            guestRegistry.add(guest);
            ensureLoyaltyAccount(guest, normalizedTier);
        } else if (!guest.isVIP()) {
            return null;
        }

        return createVIPReservation(guest, roomType, checkInDate, checkOutDate);
    }

    /**
     * Creates a VIP booking only for an existing, VIP-eligible guest.
     * The registry copy is used so a caller cannot supply an unregistered profile
     * or a manually altered loyalty tier.
     */
    public Reservation addVIPBookingForGuest(Guest guest, String roomType,
                                              String checkInDate, String checkOutDate) {
        if (guest == null) return null;
        Guest registeredGuest = findGuestById(guest.getGuestId());
        if (registeredGuest == null || !registeredGuest.isVIP()) return null;
        return createVIPReservation(registeredGuest, roomType, checkInDate, checkOutDate);
    }

    private Reservation createVIPReservation(Guest guest, String roomType,
                                               String checkInDate, String checkOutDate) {
        // Create reservation
        Reservation reservation = new Reservation(guest.getGuestId(), roomType, checkInDate, checkOutDate);
        reservation.setGuest(guest);
        reservation.setBookingStatus("PENDING");

        // Calculate priority score
        int arrivalIndex = StandardBookingController.getNextArrivalIndex();
        int priorityScore = calculatePriorityScore(guest.getTierWeight(), arrivalIndex);
        reservation.setPriorityScore(priorityScore);

        // Wrap and insert into Max-Heap
        VIPReservation vipRes = new VIPReservation(reservation);
        vipQueue.enqueue(vipRes);

        // Insert into BST for front-desk search
        searchTree.insert(reservation);

        // Record Undo Action
        if (undoController != null) {
            undoController.recordAction(
                "ADD_VIP_BOOKING",
                "Module 2: VIP Priority Allocation",
                "Added VIP Booking: " + guest.getName() + " (" + guest.getLoyaltyTier()
                        + ", Conf #" + reservation.getConfirmationNo() + ")",
                () -> {
                    reservation.setBookingStatus("CANCELLED");
                    searchTree.delete(reservation);
                }
            );
        }

        return reservation;
    }

    private Guest findGuestByContactNo(String contactNo) {
        if (contactNo == null) {
            return null;
        }
        String needle = contactNo.replaceAll("[^0-9]", "");
        if (needle.isEmpty()) {
            return null;
        }
        for (int i = 1; i <= guestRegistry.getNumberOfEntries(); i++) {
            Guest guest = guestRegistry.getEntry(i);
            if (guest == null || guest.getContactNo() == null) {
                continue;
            }
            String existing = guest.getContactNo().replaceAll("[^0-9]", "");
            if (needle.equals(existing)) {
                return guest;
            }
        }
        return null;
    }

    private void ensureLoyaltyAccount(Guest guest, String tier) {
        if (loyaltyAccounts == null || guest == null) {
            return;
        }
        if (getCurrentPointsByGuestId(guest.getGuestId()) != null) {
            return;
        }
        LoyaltyAccount account = new LoyaltyAccount(guest.getGuestId());
        account.setTierStatus(tier);
        account.addHistoryEntry("VIP walk-in registration (" + tier + ")");
        loyaltyAccounts.add(account);
    }

    /**
     * Allocates a room to the highest-priority VIP guest.
     * Extracts the max element from the heap and assigns an available room.
     *
     * @return The processed Reservation, or null if VIP queue is empty.
     */
    public Reservation allocateNextVIP() {
        if (vipQueue.isEmpty()) {
            return null;
        }

        VIPReservation vipRes = vipQueue.dequeue();
        while (vipRes != null
                && !"PENDING".equals(vipRes.getReservation().getBookingStatus())) {
            vipRes = vipQueue.dequeue();
        }
        if (vipRes == null) return null;
        Reservation reservation = vipRes.getReservation();

        // Find an available room
        Room assignedRoom = findAvailableRoom(reservation.getRoomType());
        if (assignedRoom != null) {
            assignedRoom.setStatus("OCCUPIED");
            reservation.setAssignedRoomNo(assignedRoom.getRoomNo());
            reservation.setBookingStatus("CONFIRMED");

            // Update BST entry
            searchTree.delete(reservation);
            searchTree.insert(reservation);

            // Record Undo Action
            if (undoController != null) {
                undoController.recordAction(
                    "ALLOCATE_VIP_ROOM",
                    "Module 2: VIP Priority Allocation",
                    "Allocated Room " + assignedRoom.getRoomNo() + " to VIP Conf #" + reservation.getConfirmationNo(),
                    () -> {
                        assignedRoom.setStatus("AVAILABLE");
                        reservation.setAssignedRoomNo(null);
                        reservation.setBookingStatus("PENDING");
                        vipQueue.enqueue(new VIPReservation(reservation));
                        searchTree.delete(reservation);
                        searchTree.insert(reservation);
                    }
                );
            }
        } else {
            reservation.setBookingStatus("PENDING");
            vipQueue.enqueue(vipRes);
        }

        return reservation;
    }

    /**
     * Peeks at the highest-priority VIP reservation without removing it.
     */
    public Reservation peekNextVIP() {
        VIPReservation nextVIP = null;
        DoublyLinkedList<VIPReservation> reservations = vipQueue.toList();
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            VIPReservation candidate = reservations.getEntry(i);
            if (candidate == null || candidate.getReservation() == null
                    || !"PENDING".equals(candidate.getReservation().getBookingStatus())) {
                continue;
            }
            if (nextVIP == null || candidate.compareTo(nextVIP) > 0) {
                nextVIP = candidate;
            }
        }
        return nextVIP == null ? null : nextVIP.getReservation();
    }

    /**
     * Returns the size of the VIP queue.
     */
    public int getVIPQueueSize() {
        return getVIPQueueList().getNumberOfEntries();
    }

    /**
     * Returns all VIP reservations currently in the heap.
     */
    public DoublyLinkedList<VIPReservation> getVIPQueueList() {
        DoublyLinkedList<VIPReservation> pendingReservations = new DoublyLinkedList<>();
        DoublyLinkedList<VIPReservation> allReservations = vipQueue.toList();
        for (int i = 1; i <= allReservations.getNumberOfEntries(); i++) {
            VIPReservation reservation = allReservations.getEntry(i);
            if (reservation != null && reservation.getReservation() != null
                    && "PENDING".equals(reservation.getReservation().getBookingStatus())) {
                pendingReservations.add(reservation);
            }
        }
        return pendingReservations;
    }

    /**
     * Checks if the VIP queue is empty.
     */
    public boolean isVIPQueueEmpty() {
        return peekNextVIP() == null;
    }

    public PriorityQueueSummary generatePriorityQueueSummary() {
        PriorityQueueSummary summary = new PriorityQueueSummary();
        DoublyLinkedList<VIPReservation> reservations = getVIPQueueList();
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i).getReservation();
            summary.add(reservation);
        }
        int availableStandardRooms = 0;
        int availableDeluxeRooms = 0;
        int availableSuiteRooms = 0;
        for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
            Room room = roomInventory.getEntry(i);
            if (!"AVAILABLE".equals(room.getStatus())) continue;
            if ("STANDARD".equals(room.getRoomType())) availableStandardRooms++;
            else if ("DELUXE".equals(room.getRoomType())) availableDeluxeRooms++;
            else if ("SUITE".equals(room.getRoomType())) availableSuiteRooms++;
        }
        summary.setAvailableRoomCounts(availableStandardRooms, availableDeluxeRooms,
                availableSuiteRooms);
        return summary;
    }

    public AllocationPerformanceReport generateAllocationPerformanceReport() {
        AllocationPerformanceReport report = new AllocationPerformanceReport();
        DoublyLinkedList<Reservation> reservations = searchTree.inOrderTraversal();
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            if (reservation != null && reservation.getPriorityScore() > 0) {
                report.add(reservation);
            }
        }
        return report;
    }

    /**
     * Produces a management report for VIP allocation demand. The method
     * searches the reservation registry using all three supplied criteria,
     * then uses MergeSort to rank the matching VIP reservations by priority.
     *
     * @param minimumTier lowest tier to include (SILVER through DIAMOND)
     * @param roomType requested room type, or ALL
     * @param bookingStatus reservation status, or ALL
     */
    public VIPAllocationDemandReport generateVIPAllocationDemandReport(
            String minimumTier, String roomType, String bookingStatus) {
        int minimumTierWeight = getTierWeight(minimumTier);
        DoublyLinkedList<Reservation> matches = new DoublyLinkedList<>();
        DoublyLinkedList<Reservation> reservations = searchTree.inOrderTraversal();

        // Search/filter all VIP reservations by tier, room type, and status.
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            Guest guest = reservation == null ? null : reservation.getGuest();
            if (guest == null || guest.getTierWeight() < minimumTierWeight) {
                continue;
            }
            if (!"ALL".equals(roomType) && !roomType.equals(reservation.getRoomType())) {
                continue;
            }
            if (!"ALL".equals(bookingStatus) && !bookingStatus.equals(reservation.getBookingStatus())) {
                continue;
            }
            matches.add(reservation);
        }

        // Sort filtered results from highest to lowest room-assignment priority.
        DoublyLinkedList<Reservation> sortedMatches = SortAlgorithms.mergeSort(matches,
                (first, second) -> {
                    int priorityComparison = Integer.compare(
                            second.getPriorityScore(), first.getPriorityScore());
                    return priorityComparison != 0
                            ? priorityComparison
                            : first.getConfirmationNo().compareTo(second.getConfirmationNo());
                });
        return new VIPAllocationDemandReport(minimumTier, roomType, bookingStatus, sortedMatches);
    }

    private int getTierWeight(String tier) {
        if ("DIAMOND".equals(tier)) return 4;
        if ("PLATINUM".equals(tier)) return 3;
        if ("GOLD".equals(tier)) return 2;
        return 1; // SILVER is the lowest VIP tier.
    }

    /**
     * Calculates the priority score using the formula:
     * Priority Score = (Tier Weight × 10000) + Urgency Factor − Arrival Order Index
     *
     * Urgency Factor is set to 5000 as a baseline (can be adjusted based on
     * check-in date proximity in future enhancements).
     */
    public int calculatePriorityScore(int tierWeight, int arrivalIndex) {
        int urgencyFactor = 5000; // Baseline urgency
        return (tierWeight * 10000) + urgencyFactor - arrivalIndex;
    }

    public static class PriorityQueueSummary {
        private int pendingCount;
        private int highestPriorityScore;
        private int totalPriorityScore;
        private int silverCount;
        private int goldCount;
        private int platinumCount;
        private int diamondCount;
        private int standardRoomCount;
        private int deluxeRoomCount;
        private int suiteRoomCount;
        private int availableStandardRooms;
        private int availableDeluxeRooms;
        private int availableSuiteRooms;

        private void add(Reservation reservation) {
            pendingCount++;
            highestPriorityScore = Math.max(highestPriorityScore, reservation.getPriorityScore());
            totalPriorityScore += reservation.getPriorityScore();
            String tier = reservation.getGuest() == null ? "" : reservation.getGuest().getLoyaltyTier();
            if ("SILVER".equals(tier)) silverCount++;
            else if ("GOLD".equals(tier)) goldCount++;
            else if ("PLATINUM".equals(tier)) platinumCount++;
            else if ("DIAMOND".equals(tier)) diamondCount++;

            if ("STANDARD".equals(reservation.getRoomType())) standardRoomCount++;
            else if ("DELUXE".equals(reservation.getRoomType())) deluxeRoomCount++;
            else if ("SUITE".equals(reservation.getRoomType())) suiteRoomCount++;
        }

        public int getPendingCount() { return pendingCount; }
        public int getHighestPriorityScore() { return pendingCount == 0 ? 0 : highestPriorityScore; }
        public double getAveragePriorityScore() {
            return pendingCount == 0 ? 0.0 : totalPriorityScore * 1.0 / pendingCount;
        }
        public int getSilverCount() { return silverCount; }
        public int getGoldCount() { return goldCount; }
        public int getPlatinumCount() { return platinumCount; }
        public int getDiamondCount() { return diamondCount; }
        public int getStandardRoomCount() { return standardRoomCount; }
        public int getDeluxeRoomCount() { return deluxeRoomCount; }
        public int getSuiteRoomCount() { return suiteRoomCount; }
        public int getAvailableStandardRooms() { return availableStandardRooms; }
        public int getAvailableDeluxeRooms() { return availableDeluxeRooms; }
        public int getAvailableSuiteRooms() { return availableSuiteRooms; }
        public int getStandardRoomShortage() { return Math.max(0, standardRoomCount - availableStandardRooms); }
        public int getDeluxeRoomShortage() { return Math.max(0, deluxeRoomCount - availableDeluxeRooms); }
        public int getSuiteRoomShortage() { return Math.max(0, suiteRoomCount - availableSuiteRooms); }

        private void setAvailableRoomCounts(int standard, int deluxe, int suite) {
            availableStandardRooms = standard;
            availableDeluxeRooms = deluxe;
            availableSuiteRooms = suite;
        }
    }

    public static class AllocationPerformanceReport {
        private int totalBookings;
        private int allocatedBookings;
        private int pendingBookings;
        private int cancelledBookings;

        private void add(Reservation reservation) {
            totalBookings++;
            if (reservation.getAssignedRoomNo() != null
                    || "CONFIRMED".equals(reservation.getBookingStatus())
                    || "CHECKED_IN".equals(reservation.getBookingStatus())
                    || "CHECKED_OUT".equals(reservation.getBookingStatus())) {
                allocatedBookings++;
            } else if ("CANCELLED".equals(reservation.getBookingStatus())) {
                cancelledBookings++;
            } else if ("PENDING".equals(reservation.getBookingStatus())) {
                pendingBookings++;
            }
        }

        public int getTotalBookings() { return totalBookings; }
        public int getAllocatedBookings() { return allocatedBookings; }
        public int getPendingBookings() { return pendingBookings; }
        public int getCancelledBookings() { return cancelledBookings; }
        public double getAllocationRate() {
            return totalBookings == 0 ? 0.0 : allocatedBookings * 100.0 / totalBookings;
        }
    }

    /** Management-ready result for the multi-criteria VIP demand report. */
    public static class VIPAllocationDemandReport {
        private final String minimumTier;
        private final String roomType;
        private final String bookingStatus;
        private final DoublyLinkedList<Reservation> reservations;
        private int pendingCount;
        private int allocatedCount;
        private int silverCount;
        private int goldCount;
        private int platinumCount;
        private int diamondCount;

        private VIPAllocationDemandReport(String minimumTier, String roomType,
                                          String bookingStatus,
                                          DoublyLinkedList<Reservation> reservations) {
            this.minimumTier = minimumTier;
            this.roomType = roomType;
            this.bookingStatus = bookingStatus;
            this.reservations = reservations;
            summarise();
        }

        private void summarise() {
            for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
                Reservation reservation = reservations.getEntry(i);
                String status = reservation.getBookingStatus();
                String tier = reservation.getGuest().getLoyaltyTier();
                if ("PENDING".equals(status)) pendingCount++;
                if ("CONFIRMED".equals(status) || "CHECKED_IN".equals(status)
                        || "CHECKED_OUT".equals(status)) allocatedCount++;
                if ("SILVER".equals(tier)) silverCount++;
                else if ("GOLD".equals(tier)) goldCount++;
                else if ("PLATINUM".equals(tier)) platinumCount++;
                else if ("DIAMOND".equals(tier)) diamondCount++;
            }
        }

        public String getMinimumTier() { return minimumTier; }
        public String getRoomType() { return roomType; }
        public String getBookingStatus() { return bookingStatus; }
        public DoublyLinkedList<Reservation> getReservations() { return reservations; }
        public int getTotalRequests() { return reservations.getNumberOfEntries(); }
        public int getPendingCount() { return pendingCount; }
        public int getAllocatedCount() { return allocatedCount; }
        public int getSilverCount() { return silverCount; }
        public int getGoldCount() { return goldCount; }
        public int getPlatinumCount() { return platinumCount; }
        public int getDiamondCount() { return diamondCount; }
        public double getAllocationRate() {
            return getTotalRequests() == 0 ? 0.0 : allocatedCount * 100.0 / getTotalRequests();
        }
    }

    /**
     * Finds the first available room matching the requested type.
     */
    private Room findAvailableRoom(String roomType) {
        for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
            Room room = roomInventory.getEntry(i);
            if (room.getStatus().equals("AVAILABLE") && room.getRoomType().equals(roomType)) {
                return room;
            }
        }
        // Fallback: any available room
        for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
            Room room = roomInventory.getEntry(i);
            if (room.getStatus().equals("AVAILABLE")) {
                return room;
            }
        }
        return null;
    }

    // ========================================================================
    // VIPReservation — Comparable wrapper for Max-Heap ordering by priorityScore
    // ========================================================================

    /**
     * Wrapper class that makes Reservation comparable by priorityScore
     * for use in the Binary Max-Heap (CustomPriorityQueue).
     *
     * Higher priorityScore = higher priority (Max-Heap root).
     */
    public static class VIPReservation implements Comparable<VIPReservation> {

        private Reservation reservation;

        public VIPReservation(Reservation reservation) {
            this.reservation = reservation;
        }

        public Reservation getReservation() {
            return reservation;
        }

        @Override
        public int compareTo(VIPReservation other) {
            // Higher priority score should be at the top of the Max-Heap
            return Integer.compare(this.reservation.getPriorityScore(),
                                   other.reservation.getPriorityScore());
        }

        @Override
        public String toString() {
            return reservation.toString();
        }
    }
}
