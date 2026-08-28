package control;

import adt.BinarySearchTree;
import adt.DoublyLinkedList;
import adt.LinkedQueue;
import adt.SortAlgorithms;
import entity.Guest;
import entity.LoyaltyAccount;
import entity.Reservation;
import entity.Room;
import java.time.LocalDate;

/**
 * Controller: Module 1 — Walk-In Registrations & Standard Booking Procedure.
 * Handles all LinkedQueue (FIFO) operations for standard (non-VIP) bookings.
 *
 * Business Rules:
 * - Standard walk-in guests are served strictly in order of arrival.
 * - enqueue() adds a guest to the waiting list in O(1).
 * - dequeue() assigns the next available standard room in O(1).
 * - When a room becomes available and the VIP heap is empty, the system
 *   falls back here to serve the longest-waiting standard guest.
 */
public class StandardBookingController {

    private LinkedQueue<Reservation> standardQueue;
    private DoublyLinkedList<Guest> guestRegistry;
    private DoublyLinkedList<Room> roomInventory;
    private BinarySearchTree<Reservation> searchTree;

    private UndoController undoController;
    private LoyaltyController loyaltyController;

    private static int arrivalCounter = 0; // Tracks arrival order across modules

    public StandardBookingController(LinkedQueue<Reservation> standardQueue,
                                     DoublyLinkedList<Guest> guestRegistry,
                                     DoublyLinkedList<Room> roomInventory,
                                     BinarySearchTree<Reservation> searchTree) {
        this.standardQueue = standardQueue;
        this.guestRegistry = guestRegistry;
        this.roomInventory = roomInventory;
        this.searchTree = searchTree;
    }

    public void setUndoController(UndoController undoController) {
        this.undoController = undoController;
    }

    public void setLoyaltyController(LoyaltyController loyaltyController) {
        this.loyaltyController = loyaltyController;
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


    /**
     * Processes the next booking in the standard queue.
     * Dequeues the front reservation and attempts to assign an available room.
     *
     * @return The processed Reservation with assigned room, or null if queue is empty.
     */
    public Reservation processNextBooking() {
        if (standardQueue.isEmpty()) {
            return null;
        }

        Reservation reservation = dequeueNextActiveReservation();
        if (reservation == null) return null;

        // Find an available room matching the requested type
        Room assignedRoom = findAvailableRoomForDates(reservation.getRoomType(), reservation.getCheckInDate(), reservation.getCheckOutDate());
        if (assignedRoom != null) {
            reservation.setAssignedRoomNo(assignedRoom.getRoomNo());
            reservation.setBookingStatus("CONFIRMED");
            try {
                if (LocalDate.parse(reservation.getCheckInDate()).equals(LocalDate.now())) {
                    assignedRoom.setStatus("OCCUPIED");
                }
            } catch (Exception e) {}

            // Update the BST entry
            searchTree.delete(reservation);
            searchTree.insert(reservation);

            // Record Undo Action
            if (undoController != null) {
                undoController.recordAction(
                    "PROCESS_BOOKING",
                    "Module 1: Standard Booking",
                    "Assigned Room " + assignedRoom.getRoomNo() + " to Conf #" + reservation.getConfirmationNo(),
                    () -> {
                        assignedRoom.setStatus("AVAILABLE");
                        reservation.setAssignedRoomNo(null);
                        reservation.setBookingStatus("PENDING");
                        standardQueue.enqueue(reservation);
                        searchTree.delete(reservation);
                        searchTree.insert(reservation);
                    }
                );
            }
        } else {
            // No room available — booking stays PENDING but is dequeued
            reservation.setBookingStatus("PENDING");
            standardQueue.enqueue(reservation);
        }

        return reservation;
    }

    /** Finds a registered guest by phone number, ignoring common formatting characters. */
    public Guest findGuestByContactNo(String contactNo) {
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

    /** Returns the loyalty profile tied to a registered phone number. */
    public LoyaltyAccount findLoyaltyAccountByContactNo(String contactNo) {
        Guest guest = findGuestByContactNo(contactNo);
        if (guest == null || loyaltyController == null) return null;
        return loyaltyController.viewMemberProfile(guest.getGuestId());
    }

    /**
     * Registers a booking for an existing guest and inserts it into the standard FIFO waitlist.
     */
    public Reservation registerBookingForGuest(Guest guest, String roomType,
                                               String checkInDate, String checkOutDate) {
        if (guest == null) return null;
        
        Reservation reservation = new Reservation(guest.getGuestId(), roomType, checkInDate, checkOutDate);
        reservation.setGuest(guest);
        reservation.setBookingStatus("PENDING");

        // Enqueue into standard queue (FIFO)
        standardQueue.enqueue(reservation);

        // Insert into BST for front-desk search
        searchTree.insert(reservation);

        // Record Undo Action
        if (undoController != null) {
            undoController.recordAction(
                "CREATE_BOOKING",
                "Module 1: Standard Booking",
                "Create Booking for: " + guest.getName() + " (Conf #" + reservation.getConfirmationNo() + ")",
                () -> {
                    reservation.setBookingStatus("CANCELLED");
                    searchTree.delete(reservation);
                }
            );
        }

        return reservation;
    }

    /** Registers a new STANDARD member without creating a room reservation. */
    public Guest registerNewMember(String name, String icPassport, String contactNo, String email) {
        Guest existingGuest = findGuestByContactNo(contactNo);
        if (existingGuest != null) return existingGuest;

        Guest guest = new Guest(name, icPassport, contactNo, email);
        guestRegistry.add(guest);
        if (loyaltyController != null) {
            loyaltyController.viewMemberProfile(guest.getGuestId());
        }
        return guest;
    }

    private String normalizeContactNo(String contactNo) {
        if (contactNo == null) return "";
        String digits = contactNo.replaceAll("[^0-9]", "");
        if (digits.startsWith("0060")) digits = digits.substring(2);
        if (digits.startsWith("60")) digits = "0" + digits.substring(2);
        return digits;
    }

    private Reservation dequeueNextActiveReservation() {
        Reservation reservation = standardQueue.dequeue();
        while (reservation != null && !"PENDING".equals(reservation.getBookingStatus())) {
            reservation = standardQueue.dequeue();
        }
        return reservation;
    }

    /**
     * Peeks at the next booking in the queue without removing it.
     */
    public Reservation peekNextBooking() {
        DoublyLinkedList<Reservation> reservations = standardQueue.toList();
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            if (reservation != null && "PENDING".equals(reservation.getBookingStatus())) {
                return reservation;
            }
        }
        return null;
    }

    /**
     * Returns the current size of the standard queue.
     */
    public int getQueueSize() {
        return getQueueList().getNumberOfEntries();
    }

    /**
     * Returns all reservations currently in the standard queue.
     */
    public DoublyLinkedList<Reservation> getQueueList() {
        DoublyLinkedList<Reservation> pendingReservations = new DoublyLinkedList<>();
        DoublyLinkedList<Reservation> allReservations = standardQueue.toList();
        for (int i = 1; i <= allReservations.getNumberOfEntries(); i++) {
            Reservation reservation = allReservations.getEntry(i);
            if (reservation != null && "PENDING".equals(reservation.getBookingStatus())) {
                pendingReservations.add(reservation);
            }
        }
        return pendingReservations;
    }

    /**
     * Checks if the standard queue is empty.
     */
    public boolean isQueueEmpty() {
        return peekNextBooking() == null;
    }

    /**
     * Finds a room of a specific type that is not booked for the given stay dates.
     */
    public Room findAvailableRoomForDates(String roomType, String checkInStr, String checkOutStr) {
        try {
            LocalDate checkIn = LocalDate.parse(checkInStr);
            LocalDate checkOut = LocalDate.parse(checkOutStr);
            
            // Collect all candidate rooms of this type that are not in maintenance
            DoublyLinkedList<Room> candidateRooms = new DoublyLinkedList<>();
            for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
                Room r = roomInventory.getEntry(i);
                if (r != null && r.getRoomType().equalsIgnoreCase(roomType) && !"MAINTENANCE".equals(r.getStatus())) {
                    candidateRooms.add(r);
                }
            }
            
            // Filter out rooms that have overlapping confirmed bookings
            DoublyLinkedList<Reservation> allReservations = searchTree.inOrderTraversal();
            for (int i = 1; i <= allReservations.getNumberOfEntries(); i++) {
                Reservation res = allReservations.getEntry(i);
                if (res != null && res.getAssignedRoomNo() != null && res.getRoomType().equalsIgnoreCase(roomType)) {
                    String status = res.getBookingStatus();
                    if ("CONFIRMED".equals(status) || "CHECKED_IN".equals(status)) {
                        LocalDate resCheckIn = LocalDate.parse(res.getCheckInDate());
                        LocalDate resCheckOut = LocalDate.parse(res.getCheckOutDate());
                        if (checkIn.isBefore(resCheckOut) && checkOut.isAfter(resCheckIn)) {
                            for (int j = 1; j <= candidateRooms.getNumberOfEntries(); j++) {
                                if (candidateRooms.getEntry(j).getRoomNo().equals(res.getAssignedRoomNo())) {
                                    candidateRooms.remove(j);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            
            if (!candidateRooms.isEmpty()) {
                return candidateRooms.getEntry(1);
            }
        } catch (Exception e) {
            // ignore parsing errors
        }
        return null;
    }

    /**
     * Gets the global arrival counter (shared across modules for priority calculation).
     */
    public static int getNextArrivalIndex() {
        return arrivalCounter++;
    }

    /**
     * Counts how many rooms of a specific type are currently AVAILABLE.
     */
    public int getAvailableRoomCount(String roomType) {
        int count = 0;
        for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
            Room room = roomInventory.getEntry(i);
            if (room != null && "AVAILABLE".equals(room.getStatus()) && room.getRoomType().equalsIgnoreCase(roomType)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts how many rooms of a specific type are AVAILABLE during the given date range.
     * Takes into account overlapping pending, confirmed, or checked-in reservations.
     */
    public int getAvailableRoomCountForDates(String roomType, String checkInStr, String checkOutStr) {
        try {
            LocalDate checkIn = LocalDate.parse(checkInStr);
            LocalDate checkOut = LocalDate.parse(checkOutStr);
            
            // 1. Count total rooms of this type not in MAINTENANCE
            int totalOfType = 0;
            for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
                Room r = roomInventory.getEntry(i);
                if (r != null && r.getRoomType().equalsIgnoreCase(roomType)) {
                    if (!"MAINTENANCE".equals(r.getStatus())) {
                        totalOfType++;
                    }
                }
            }
            
            // 2. Count overlapping reservations of this type with active statuses
            int bookedCount = 0;
            DoublyLinkedList<Reservation> allReservations = searchTree.inOrderTraversal();
            for (int i = 1; i <= allReservations.getNumberOfEntries(); i++) {
                Reservation res = allReservations.getEntry(i);
                if (res != null && res.getRoomType().equalsIgnoreCase(roomType)) {
                    String status = res.getBookingStatus();
                    if ("PENDING".equals(status) || "CONFIRMED".equals(status) || "CHECKED_IN".equals(status)) {
                        LocalDate resCheckIn = LocalDate.parse(res.getCheckInDate());
                        LocalDate resCheckOut = LocalDate.parse(res.getCheckOutDate());
                        
                        // Check if dates overlap
                        if (checkIn.isBefore(resCheckOut) && checkOut.isAfter(resCheckIn)) {
                            bookedCount++;
                        }
                    }
                }
            }
            
            return Math.max(0, totalOfType - bookedCount);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Helper to find a Room by its room number.
     */
    private Room findRoomByNo(String roomNo) {
        for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
            Room r = roomInventory.getEntry(i);
            if (r != null && r.getRoomNo().equals(roomNo)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Finds a reservation by confirmation number using the BST.
     */
    public Reservation findReservationByConfNo(String confirmationNo) {
        Reservation key = Reservation.lookupKey(confirmationNo);
        return searchTree.search(key);
    }

    /**
     * Checks if a guest has an active reservation that overlaps with the proposed dates.
     */
    public Reservation findOverlappingReservation(String guestId, String checkInDateStr, String checkOutDateStr) {
        if (guestId == null) return null;
        try {
            LocalDate checkIn = LocalDate.parse(checkInDateStr);
            LocalDate checkOut = LocalDate.parse(checkOutDateStr);
            
            DoublyLinkedList<Reservation> allReservations = searchTree.inOrderTraversal();
            for (int i = 1; i <= allReservations.getNumberOfEntries(); i++) {
                Reservation res = allReservations.getEntry(i);
                if (res != null && guestId.equals(res.getGuestId())) {
                    String status = res.getBookingStatus();
                    if ("PENDING".equals(status) || "CONFIRMED".equals(status) || "CHECKED_IN".equals(status)) {
                        LocalDate resCheckIn = LocalDate.parse(res.getCheckInDate());
                        LocalDate resCheckOut = LocalDate.parse(res.getCheckOutDate());
                        
                        // Overlap condition
                        if (checkIn.isBefore(resCheckOut) && checkOut.isAfter(resCheckIn)) {
                            return res;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore date parsing issues
        }
        return null;
    }

    /**
     * Cancels a pending standard reservation.
     * Updates status to CANCELLED and deletes from BST.
     */
    public boolean cancelPendingReservation(String confirmationNo) {
        Reservation key = Reservation.lookupKey(confirmationNo);
        Reservation res = searchTree.search(key);
        if (res != null && "PENDING".equals(res.getBookingStatus())) {
            res.setBookingStatus("CANCELLED");
            searchTree.delete(res);
            
            // Record Undo Action
            if (undoController != null) {
                undoController.recordAction(
                    "CANCEL_STANDARD_RESERVATION",
                    "Module 1: Standard Booking",
                    "Cancelled Conf #" + confirmationNo,
                    () -> {
                        res.setBookingStatus("PENDING");
                        searchTree.insert(res);
                    }
                );
            }
            return true;
        }
        return false;
    }

    /**
     * Modifies a pending standard reservation's details.
     */
    public boolean modifyPendingReservation(String confirmationNo, String newRoomType, 
                                            String newCheckIn, String newCheckOut) {
        Reservation key = Reservation.lookupKey(confirmationNo);
        Reservation res = searchTree.search(key);
        if (res != null && "PENDING".equals(res.getBookingStatus())) {
            String oldRoomType = res.getRoomType();
            String oldCheckIn = res.getCheckInDate();
            String oldCheckOut = res.getCheckOutDate();
            
            res.setRoomType(newRoomType);
            res.setCheckInDate(newCheckIn);
            res.setCheckOutDate(newCheckOut);
            
            // Record Undo Action
            if (undoController != null) {
                undoController.recordAction(
                    "MODIFY_STANDARD_RESERVATION",
                    "Module 1: Standard Booking",
                    "Modified Conf #" + confirmationNo,
                    () -> {
                        res.setRoomType(oldRoomType);
                        res.setCheckInDate(oldCheckIn);
                        res.setCheckOutDate(oldCheckOut);
                    }
                );
            }
            return true;
        }
        return false;
    }

    /**
     * Generates a Revenue Analysis report for standard bookings.
     */
    public StandardRevenueReport generateRevenueReport(DoublyLinkedList<String> roomTypes, DoublyLinkedList<String> bookingStatuses, int minDuration) {
        DoublyLinkedList<Reservation> matches = new DoublyLinkedList<>();
        DoublyLinkedList<Reservation> allReservations = searchTree.inOrderTraversal();

        boolean allowAllRooms = roomTypes.isEmpty();
        for (int i = 1; i <= roomTypes.getNumberOfEntries(); i++) {
            if ("ALL".equalsIgnoreCase(roomTypes.getEntry(i))) {
                allowAllRooms = true;
                break;
            }
        }

        boolean allowAllStatuses = bookingStatuses.isEmpty();
        for (int i = 1; i <= bookingStatuses.getNumberOfEntries(); i++) {
            if ("ALL".equalsIgnoreCase(bookingStatuses.getEntry(i))) {
                allowAllStatuses = true;
                break;
            }
        }

        for (int i = 1; i <= allReservations.getNumberOfEntries(); i++) {
            Reservation res = allReservations.getEntry(i);
            if (res == null) {
                continue;
            }
            
            if (!allowAllRooms) {
                boolean roomMatched = false;
                for (int j = 1; j <= roomTypes.getNumberOfEntries(); j++) {
                    if (roomTypes.getEntry(j).equalsIgnoreCase(res.getRoomType())) {
                        roomMatched = true;
                        break;
                    }
                }
                if (!roomMatched) {
                    continue;
                }
            }
            
            if (!allowAllStatuses) {
                boolean statusMatched = false;
                for (int j = 1; j <= bookingStatuses.getNumberOfEntries(); j++) {
                    if (bookingStatuses.getEntry(j).equalsIgnoreCase(res.getBookingStatus())) {
                        statusMatched = true;
                        break;
                    }
                }
                if (!statusMatched) {
                    continue;
                }
            }

            try {
                long duration = LocalDate.parse(res.getCheckOutDate()).toEpochDay() - LocalDate.parse(res.getCheckInDate()).toEpochDay();
                if (duration < minDuration) {
                    continue;
                }
                matches.add(res);
            } catch (Exception e) {
                // Ignore
            }
        }

        // Sort by duration descending using MergeSort
        DoublyLinkedList<Reservation> sorted = SortAlgorithms.mergeSort(matches, (r1, r2) -> {
            try {
                long d1 = LocalDate.parse(r1.getCheckOutDate()).toEpochDay() - LocalDate.parse(r1.getCheckInDate()).toEpochDay();
                long d2 = LocalDate.parse(r2.getCheckOutDate()).toEpochDay() - LocalDate.parse(r2.getCheckInDate()).toEpochDay();
                return Long.compare(d2, d1); // Descending
            } catch (Exception e) {
                return 0;
            }
        });

        StringBuilder roomSb = new StringBuilder();
        if (allowAllRooms) {
            roomSb.append("ALL");
        } else {
            for (int i = 1; i <= roomTypes.getNumberOfEntries(); i++) {
                if (i > 1) roomSb.append(", ");
                roomSb.append(roomTypes.getEntry(i));
            }
        }

        StringBuilder statusSb = new StringBuilder();
        if (allowAllStatuses) {
            statusSb.append("ALL");
        } else {
            for (int i = 1; i <= bookingStatuses.getNumberOfEntries(); i++) {
                if (i > 1) statusSb.append(", ");
                statusSb.append(bookingStatuses.getEntry(i));
            }
        }

        return new StandardRevenueReport(roomSb.toString(), statusSb.toString(), minDuration, sorted);
    }

    /**
     * Generates a Queue Performance & Room Shortage report.
     */
    public QueuePerformanceReport generateQueuePerformanceReport(DoublyLinkedList<String> roomTypes) {
        DoublyLinkedList<Reservation> pending = new DoublyLinkedList<>();
        DoublyLinkedList<Reservation> allQueue = standardQueue.toList();

        boolean allowAllRooms = roomTypes.isEmpty();
        for (int i = 1; i <= roomTypes.getNumberOfEntries(); i++) {
            if ("ALL".equalsIgnoreCase(roomTypes.getEntry(i))) {
                allowAllRooms = true;
                break;
            }
        }

        LocalDate today = LocalDate.now();
        for (int i = 1; i <= allQueue.getNumberOfEntries(); i++) {
            Reservation res = allQueue.getEntry(i);
            if (res != null && "PENDING".equals(res.getBookingStatus())) {
                try {
                    LocalDate checkIn = LocalDate.parse(res.getCheckInDate());
                    if (checkIn.isBefore(today)) {
                        continue; // Skip expired pending bookings
                    }
                } catch (Exception e) {}
                
                boolean roomMatched = allowAllRooms;
                if (!allowAllRooms) {
                    for (int j = 1; j <= roomTypes.getNumberOfEntries(); j++) {
                        if (roomTypes.getEntry(j).equalsIgnoreCase(res.getRoomType())) {
                            roomMatched = true;
                            break;
                        }
                    }
                }
                
                if (roomMatched) {
                    pending.add(res);
                }
            }
        }

        // Sort by timestamp ascending (longest waiting first)
        DoublyLinkedList<Reservation> sorted = SortAlgorithms.mergeSort(pending, (r1, r2) -> {
            return Long.compare(r1.getTimestamp(), r2.getTimestamp());
        });

        // Count available rooms
        int availCount = 0;
        for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
            Room r = roomInventory.getEntry(i);
            if (r != null && "AVAILABLE".equals(r.getStatus())) {
                boolean roomMatched = allowAllRooms;
                if (!allowAllRooms) {
                    for (int j = 1; j <= roomTypes.getNumberOfEntries(); j++) {
                        if (roomTypes.getEntry(j).equalsIgnoreCase(r.getRoomType())) {
                            roomMatched = true;
                            break;
                        }
                    }
                }
                if (roomMatched) {
                    availCount++;
                }
            }
        }

        StringBuilder roomSb = new StringBuilder();
        if (allowAllRooms) {
            roomSb.append("ALL");
        } else {
            for (int i = 1; i <= roomTypes.getNumberOfEntries(); i++) {
                if (i > 1) roomSb.append(", ");
                roomSb.append(roomTypes.getEntry(i));
            }
        }

        return new QueuePerformanceReport(roomSb.toString(), sorted, availCount);
    }

    // ========================================================================
    // Report Value Objects (Standard Booking & Registration Module)
    // ========================================================================

    public static class StandardRevenueReport {
        private final String roomTypeFilter;
        private final String statusFilter;
        private final int minDurationFilter;
        private final DoublyLinkedList<Reservation> reservations;
        private int totalBookings;
        private long totalDays;
        private double totalRevenue;

        public StandardRevenueReport(String roomTypeFilter, String statusFilter, int minDurationFilter,
                                     DoublyLinkedList<Reservation> reservations) {
            this.roomTypeFilter = roomTypeFilter;
            this.statusFilter = statusFilter;
            this.minDurationFilter = minDurationFilter;
            this.reservations = reservations;
            calculateSummaries();
        }

        private void calculateSummaries() {
            totalBookings = reservations.getNumberOfEntries();
            totalDays = 0;
            totalRevenue = 0;
            for (int i = 1; i <= totalBookings; i++) {
                Reservation res = reservations.getEntry(i);
                try {
                    long days = LocalDate.parse(res.getCheckOutDate()).toEpochDay() - LocalDate.parse(res.getCheckInDate()).toEpochDay();
                    double rate = 100.0;
                    if ("DELUXE".equals(res.getRoomType())) rate = 200.0;
                    else if ("SUITE".equals(res.getRoomType())) rate = 500.0;
                    totalDays += days;
                    totalRevenue += (days * rate);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        public String getRoomTypeFilter() { return roomTypeFilter; }
        public String getStatusFilter() { return statusFilter; }
        public int getMinDurationFilter() { return minDurationFilter; }
        public DoublyLinkedList<Reservation> getReservations() { return reservations; }
        public int getTotalBookings() { return totalBookings; }
        public long getTotalDays() { return totalDays; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getAverageRevenuePerBooking() {
            return totalBookings == 0 ? 0.0 : totalRevenue / totalBookings;
        }
    }

    public static class QueuePerformanceReport {
        private final String roomTypeFilter;
        private final DoublyLinkedList<Reservation> pendingReservations;
        private int totalPending;
        private int availableRooms;
        private int shortage;

        public QueuePerformanceReport(String roomTypeFilter, DoublyLinkedList<Reservation> pendingReservations, int availableRooms) {
            this.roomTypeFilter = roomTypeFilter;
            this.pendingReservations = pendingReservations;
            this.totalPending = pendingReservations.getNumberOfEntries();
            this.availableRooms = availableRooms;
            this.shortage = Math.max(0, totalPending - availableRooms);
        }

        public String getRoomTypeFilter() { return roomTypeFilter; }
        public DoublyLinkedList<Reservation> getPendingReservations() { return pendingReservations; }
        public int getTotalPending() { return totalPending; }
        public int getAvailableRooms() { return availableRooms; }
        public int getShortage() { return shortage; }
    }

    /**
     * Counts the total number of rooms of a specific type in the system.
     */
    public int getTotalRoomCount(String roomType) {
        int count = 0;
        for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
            Room r = roomInventory.getEntry(i);
            if (r != null && r.getRoomType().equalsIgnoreCase(roomType)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Retrieves all booked/occupied date ranges for standard bookings of a specific type.
     */
    public DoublyLinkedList<DateRange> getOccupiedDateRanges(String roomType) {
        DoublyLinkedList<DateRange> ranges = new DoublyLinkedList<>();
        DoublyLinkedList<Reservation> allReservations = searchTree.inOrderTraversal();
        LocalDate today = LocalDate.now();
        
        for (int i = 1; i <= allReservations.getNumberOfEntries(); i++) {
            Reservation res = allReservations.getEntry(i);
            if (res != null) {
                String status = res.getBookingStatus();
                if ("PENDING".equals(status) || "CONFIRMED".equals(status) || "CHECKED_IN".equals(status)) {
                    if (res.getRoomType().equalsIgnoreCase(roomType)) {
                        try {
                            LocalDate checkOut = LocalDate.parse(res.getCheckOutDate());
                            if (checkOut.isBefore(today)) {
                                continue; // skip historical reservations
                            }
                        } catch (Exception e) {
                            // ignore parsing errors
                        }
                        
                        boolean found = false;
                        for (int j = 1; j <= ranges.getNumberOfEntries(); j++) {
                            DateRange range = ranges.getEntry(j);
                            if (range.getCheckIn().equals(res.getCheckInDate()) && range.getCheckOut().equals(res.getCheckOutDate())) {
                                range.increment();
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            ranges.add(new DateRange(res.getCheckInDate(), res.getCheckOutDate(), 1));
                        }
                    }
                }
            }
        }
        return ranges;
    }

    /**
     * Simple DateRange value class representing booked dates.
     */
    public static class DateRange {
        private final String checkIn;
        private final String checkOut;
        private int count;

        public DateRange(String checkIn, String checkOut, int count) {
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.count = count;
        }

        public String getCheckIn() { return checkIn; }
        public String getCheckOut() { return checkOut; }
        public int getCount() { return count; }
        public void increment() { count++; }
    }
}
