package control;

import adt.BinarySearchTree;
import adt.DoublyLinkedList;
import entity.Guest;
import entity.Reservation;
import entity.Room;

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

        if (!"CONFIRMED".equals(reservation.getBookingStatus())) {
            return false; // Can only check in from CONFIRMED status
        }

        reservation.setBookingStatus("CHECKED_IN");

        // Mark room as occupied
        Room room = findRoom(reservation.getAssignedRoomNo());
        if (room != null) {
            room.setStatus("OCCUPIED");
        }

        // Update BST
        searchTree.delete(reservation);
        searchTree.insert(reservation);

        // Record Undo Action
        if (undoController != null) {
            undoController.recordAction(
                "CHECK_IN",
                "Module 3: Front-Desk Service",
                "Check-In Conf #" + confirmationNo + " (Room " + reservation.getAssignedRoomNo() + ")",
                () -> {
                    reservation.setBookingStatus("CONFIRMED");
                    if (room != null) {
                        room.setStatus("AVAILABLE");
                    }
                    searchTree.delete(reservation);
                    searchTree.insert(reservation);
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

        if (!"CHECKED_IN".equals(reservation.getBookingStatus())) {
            return null; // Can only check out from CHECKED_IN status
        }

        reservation.setBookingStatus("CHECKED_OUT");

        // Free the room
        Room room = findRoom(reservation.getAssignedRoomNo());
        if (room != null) {
            room.setStatus("AVAILABLE");

            // Trigger loyalty points accrual
            if (loyaltyController != null) {
                int nights = calculateNights(reservation.getCheckInDate(), reservation.getCheckOutDate());
                if (nights <= 0) nights = 1; // minimum 1 night
                loyaltyController.accruePoints(reservation.getGuestId(),
                                                room.getNightlyRate(), nights);
            }
        }

        // Update BST
        searchTree.delete(reservation);
        searchTree.insert(reservation);

        // Record Undo Action
        if (undoController != null) {
            undoController.recordAction(
                "CHECK_OUT",
                "Module 3: Front-Desk Service",
                "Check-Out Conf #" + confirmationNo + " (Room " + reservation.getAssignedRoomNo() + ")",
                () -> {
                    reservation.setBookingStatus("CHECKED_IN");
                    if (room != null) {
                        room.setStatus("OCCUPIED");
                    }
                    searchTree.delete(reservation);
                    searchTree.insert(reservation);
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

        if (!"PENDING".equals(reservation.getBookingStatus())
                && !"CONFIRMED".equals(reservation.getBookingStatus())) {
            return false;
        }

        String prevStatus = reservation.getBookingStatus();
        reservation.setBookingStatus("CANCELLED");

        // Free room if one was assigned
        Room room = findRoom(reservation.getAssignedRoomNo());
        if (room != null) {
            room.setStatus("AVAILABLE");
        }

        // Update BST
        searchTree.delete(reservation);
        searchTree.insert(reservation);

        // Record Undo Action
        if (undoController != null) {
            undoController.recordAction(
                "CANCEL_RESERVATION",
                "Module 3: Front-Desk Service",
                "Cancelled Conf #" + confirmationNo,
                () -> {
                    reservation.setBookingStatus(prevStatus);
                    if (room != null && "CONFIRMED".equals(prevStatus)) {
                        room.setStatus("OCCUPIED");
                    }
                    searchTree.delete(reservation);
                    searchTree.insert(reservation);
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
                    && ("PENDING".equals(reservation.getBookingStatus())
                    || "CONFIRMED".equals(reservation.getBookingStatus()))) {
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

    /**
     * Simple night calculation from date strings (format: "YYYY-MM-DD").
     * Returns the difference in days, or 1 if parsing fails.
     */
    private int calculateNights(String checkIn, String checkOut) {
        try {
            if (checkIn == null || checkOut == null) return 1;
            // Simple parsing for YYYY-MM-DD format
            String[] inParts = checkIn.split("-");
            String[] outParts = checkOut.split("-");
            int inDay = Integer.parseInt(inParts[2]);
            int outDay = Integer.parseInt(outParts[2]);
            int inMonth = Integer.parseInt(inParts[1]);
            int outMonth = Integer.parseInt(outParts[1]);

            int nights = (outMonth - inMonth) * 30 + (outDay - inDay);
            return (nights > 0) ? nights : 1;
        } catch (Exception e) {
            return 1;
        }
    }
}
