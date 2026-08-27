package control;

import adt.BinarySearchTree;
import adt.DoublyLinkedList;
import entity.Guest;
import entity.LoyaltyAccount;
import entity.Reservation;
import entity.Room;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Controller: Module 3 — Front-Desk Service.
 * Handles BST searching, check-in/check-out, and reservation management.
 *
 * Business Rules:
 * - Front-desk agents search reservations by 8-digit confirmationNo.
 * - BST search traverses left/right based on numeric comparison — O(log n) average.
 * - In-order traversal prints sorted booking logs efficiently.
 * Handles BST searching, check-in, check-out, and report generation.
 * Receives shared ADT instances from Main.java.
 */
public class FrontDeskController {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CHECKED_IN = "CHECKED_IN";
    private static final String STATUS_CHECKED_OUT = "CHECKED_OUT";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String ROOM_AVAILABLE = "AVAILABLE";
    private static final String ROOM_OCCUPIED = "OCCUPIED";
    private static final String ROOM_MAINTENANCE = "MAINTENANCE";

    private BinarySearchTree<Reservation> searchTree;
    private DoublyLinkedList<Guest> guestRegistry;
    private DoublyLinkedList<Room> roomInventory;
    private LoyaltyController loyaltyController;
    private UndoController undoController;

    public FrontDeskController(BinarySearchTree<Reservation> searchTree,
                               DoublyLinkedList<Guest> guestRegistry,
                               DoublyLinkedList<Room> roomInventory,
                               LoyaltyController loyaltyController) {
        this.searchTree = searchTree;
        this.guestRegistry = guestRegistry;
        this.roomInventory = roomInventory;
        this.loyaltyController = loyaltyController;
    }

    public void setUndoController(UndoController undoController) {
        this.undoController = undoController;
    }

    /**
     * Creates a dummy Reservation with the given confirmationNo for comparison.
     * Average O(log n) time complexity.
     *
     * @param confirmationNo The 8-digit confirmation number to search for.
     * @return The matching Reservation, or null if not found.
     */
    public Reservation searchReservation(String confirmationNo) {
        Reservation searchKey = new Reservation();
        searchKey.setConfirmationNo(confirmationNo);
        return searchTree.search(searchKey);
    }

    /**
     * Checks in a guest: marks reservation as CHECKED_IN and room as OCCUPIED.
     *
     * @param confirmationNo The 8-digit confirmation number.
     * @return true if check-in was successful, false if reservation not found or invalid state.
     */
    public boolean checkIn(String confirmationNo) {
        Reservation reservation = searchReservation(confirmationNo);
        if (reservation == null) {
            return false;
        }

        if (!STATUS_CONFIRMED.equals(reservation.getBookingStatus())) {
            return false; // Can only check in from CONFIRMED status
        }

        if (!isCheckInDateReached(reservation.getCheckInDate())) {
            return false;
        }

        Room room = findRoom(reservation.getAssignedRoomNo());
        if (room == null || ROOM_MAINTENANCE.equals(room.getStatus())) {
            return false;
        }

        String previousRoomStatus = room.getStatus();
        reservation.setBookingStatus(STATUS_CHECKED_IN);
        room.setStatus(ROOM_OCCUPIED);

        // Record Undo Action. Confirmed bookings already hold the room, so undo
        // restores OCCUPIED rather than releasing inventory back to AVAILABLE.
        if (undoController != null) {
            undoController.recordAction(
                "CHECK_IN",
                "Module 3: Front-Desk Service",
                "Check-In Conf #" + confirmationNo + " (Room " + reservation.getAssignedRoomNo() + ")",
                () -> {
                    reservation.setBookingStatus(STATUS_CONFIRMED);
                    room.setStatus(previousRoomStatus != null ? previousRoomStatus : ROOM_OCCUPIED);
                }
            );
        }

        return true;
    }

    /**
     * Checks out a guest: marks reservation as CHECKED_OUT, frees room,
     * and triggers loyalty points accrual.
     *
     * @param confirmationNo The 8-digit confirmation number.
     * @return The check-out Reservation, or null if not found or invalid state.
     */
    public Reservation checkOut(String confirmationNo) {
        Reservation reservation = searchReservation(confirmationNo);
        if (reservation == null) {
            return null;
        }

        if (!STATUS_CHECKED_IN.equals(reservation.getBookingStatus())) {
            return null; // Can only check out from CHECKED_IN status
        }

        reservation.setBookingStatus(STATUS_CHECKED_OUT);

        Room room = findRoom(reservation.getAssignedRoomNo());
        Guest guest = findGuest(reservation.getGuestId());
        int nights = calculateNights(reservation.getCheckInDate(), reservation.getCheckOutDate());
        int pointsAwarded = 0;

        if (room != null) {
            room.setStatus(ROOM_AVAILABLE);

            if (loyaltyController != null && guest != null) {
                pointsAwarded = (int) (room.getNightlyRate() * nights);
                loyaltyController.accruePointsByContactNo(guest.getContactNo(),
                                                          room.getNightlyRate(), nights);
            }
        }

        final int pointsToReverse = pointsAwarded;
        final Guest checkedOutGuest = guest;

        if (undoController != null) {
            undoController.recordAction(
                "CHECK_OUT",
                "Module 3: Front-Desk Service",
                "Check-Out Conf #" + confirmationNo + " (Room " + reservation.getAssignedRoomNo() + ")",
                () -> {
                    reservation.setBookingStatus(STATUS_CHECKED_IN);
                    if (room != null) {
                        room.setStatus(ROOM_OCCUPIED);
                    }
                    reverseAccruedPoints(checkedOutGuest, pointsToReverse, confirmationNo);
                }
            );
        }

        return reservation;
    }

    /**
     * Cancels a reservation.
     *
     * @param confirmationNo The confirmation number to cancel.
     * @return true if cancellation was successful.
     */
    public boolean cancelReservation(String confirmationNo) {
        Reservation reservation = searchReservation(confirmationNo);
        if (reservation == null) {
            return false;
        }

        if (!STATUS_PENDING.equals(reservation.getBookingStatus())
                && !STATUS_CONFIRMED.equals(reservation.getBookingStatus())) {
            return false;
        }

        String prevStatus = reservation.getBookingStatus();
        reservation.setBookingStatus(STATUS_CANCELLED);

        Room room = findRoom(reservation.getAssignedRoomNo());
        String previousRoomStatus = room != null ? room.getStatus() : null;
        if (room != null && STATUS_CONFIRMED.equals(prevStatus)) {
            room.setStatus(ROOM_AVAILABLE);
        }

        if (undoController != null) {
            undoController.recordAction(
                "CANCEL_RESERVATION",
                "Module 3: Front-Desk Service",
                "Cancelled Conf #" + confirmationNo,
                () -> {
                    reservation.setBookingStatus(prevStatus);
                    if (room != null && STATUS_CONFIRMED.equals(prevStatus)
                            && ROOM_AVAILABLE.equals(room.getStatus())) {
                        room.setStatus(previousRoomStatus != null ? previousRoomStatus : ROOM_OCCUPIED);
                    }
                }
            );
        }

        return true;
    }

    /**
     * Returns all reservations in sorted order via BST in-order traversal.
     * Used for printing sorted booking logs.
     */
    public DoublyLinkedList<Reservation> getAllReservationsSorted() {
        return searchTree.inOrderTraversal();
    }

    /** Returns reservations matching a booking status, sorted by confirmation number. */
    public DoublyLinkedList<Reservation> getReservationsByStatus(String bookingStatus) {
        DoublyLinkedList<Reservation> matches = new DoublyLinkedList<>();
        if (bookingStatus == null) return matches;

        DoublyLinkedList<Reservation> reservations = searchTree.inOrderTraversal();
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            if (reservation != null
                    && bookingStatus.equalsIgnoreCase(reservation.getBookingStatus())) {
                matches.add(reservation);
            }
        }
        return matches;
    }

    /** Returns bookings that have not started and can still be cancelled. */
    public DoublyLinkedList<Reservation> getCancellableReservations() {
        DoublyLinkedList<Reservation> matches = new DoublyLinkedList<>();
        DoublyLinkedList<Reservation> reservations = searchTree.inOrderTraversal();
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            if (reservation != null
                    && (STATUS_PENDING.equals(reservation.getBookingStatus())
                    || STATUS_CONFIRMED.equals(reservation.getBookingStatus()))) {
                matches.add(reservation);
            }
        }
        return matches;
    }

    /**
     * Returns the total number of reservations in the BST.
     */
    public int getReservationCount() {
        return searchTree.size();
    }

    /**
     * Finds a guest by guestId in the guest registry.
     */
    public Guest findGuest(String guestId) {
        for (int i = 1; i <= guestRegistry.getNumberOfEntries(); i++) {
            Guest guest = guestRegistry.getEntry(i);
            if (guest.getGuestId().equals(guestId)) {
                return guest;
            }
        }
        return null;
    }

    /**
     * Finds a room by room number in the inventory.
     */
    public Room findRoom(String roomNo) {
        if (roomNo == null) return null;
        for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
            Room room = roomInventory.getEntry(i);
            if (room.getRoomNo().equals(roomNo)) {
                return room;
            }
        }
        return null;
    }

    /**
     * Gets all rooms and their current status.
     */
    public DoublyLinkedList<Room> getRoomInventory() {
        return roomInventory;
    }

    /** Returns true when the confirmation number is an 8-digit value. */
    public boolean isValidConfirmationNo(String confirmationNo) {
        return confirmationNo != null && confirmationNo.matches("\\d{8}");
    }

    /** Returns true when today's date is on or after the booked check-in date. */
    public boolean isCheckInDateReached(String checkInDate) {
        LocalDate parsed = parseDate(checkInDate);
        if (parsed == null) {
            return true;
        }
        return !LocalDate.now().isBefore(parsed);
    }

    /**
     * Night calculation from date strings (format: YYYY-MM-DD).
     * Uses calendar dates so month and year boundaries are counted correctly.
     */
    public int calculateNights(String checkIn, String checkOut) {
        LocalDate inDate = parseDate(checkIn);
        LocalDate outDate = parseDate(checkOut);
        if (inDate == null || outDate == null) {
            return 1;
        }
        long nights = ChronoUnit.DAYS.between(inDate, outDate);
        return (nights > 0) ? (int) nights : 1;
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(date.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Reverses points credited at check-out using LoyaltyController public APIs
     * without changing Module 4 source.
     */
    private void reverseAccruedPoints(Guest guest, int pointsAwarded, String confirmationNo) {
        if (loyaltyController == null || guest == null || pointsAwarded <= 0) {
            return;
        }
        LoyaltyAccount account = loyaltyController.findAccount(guest.getGuestId());
        if (account == null) {
            return;
        }
        int restored = Math.max(0, account.getTotalPoints() - pointsAwarded);
        account.setTotalPoints(restored);
        account.addHistoryEntry("-" + pointsAwarded + " pts (Undo check-out Conf #" + confirmationNo + ")");
        loyaltyController.checkAndUpgradeTier(guest.getGuestId());
    }
}
