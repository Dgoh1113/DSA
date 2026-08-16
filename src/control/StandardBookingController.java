package control;

import adt.BinarySearchTree;
import adt.DoublyLinkedList;
import adt.LinkedQueue;
import entity.Guest;
import entity.Reservation;
import entity.Room;

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

    /**
     * Registers a walk-in guest and creates a PENDING reservation.
     * The guest is added to the guest registry and the reservation
     * is enqueued into the standard FIFO queue.
     *
     * @return The created Reservation with a generated confirmation number.
     */
    public Reservation registerWalkIn(String name, String icPassport, String contactNo,
                                       String email, String roomType,
                                       String checkInDate, String checkOutDate) {
        // Create guest
        Guest guest = new Guest(name, icPassport, contactNo, email);
        guestRegistry.add(guest);

        // Create reservation
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
                "WALK_IN_REGISTRATION",
                "Module 1: Standard Booking",
                "Register Walk-In: " + name + " (Conf #" + reservation.getConfirmationNo() + ")",
                () -> {
                    reservation.setBookingStatus("CANCELLED");
                    searchTree.delete(reservation);
                }
            );
        }

        return reservation;
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
        Room assignedRoom = findAvailableRoom(reservation.getRoomType());
        if (assignedRoom != null) {
            assignedRoom.setStatus("OCCUPIED");
            reservation.setAssignedRoomNo(assignedRoom.getRoomNo());
            reservation.setBookingStatus("CONFIRMED");

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

    private Reservation dequeueNextActiveReservation() {
        Reservation reservation = standardQueue.dequeue();
        while (reservation != null && "CANCELLED".equals(reservation.getBookingStatus())) {
            reservation = standardQueue.dequeue();
        }
        return reservation;
    }

    /**
     * Peeks at the next booking in the queue without removing it.
     */
    public Reservation peekNextBooking() {
        return standardQueue.getFront();
    }

    /**
     * Returns the current size of the standard queue.
     */
    public int getQueueSize() {
        return standardQueue.size();
    }

    /**
     * Returns all reservations currently in the standard queue.
     */
    public DoublyLinkedList<Reservation> getQueueList() {
        return standardQueue.toList();
    }

    /**
     * Checks if the standard queue is empty.
     */
    public boolean isQueueEmpty() {
        return standardQueue.isEmpty();
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
        // If no room of exact type, try any available room
        for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
            Room room = roomInventory.getEntry(i);
            if (room.getStatus().equals("AVAILABLE")) {
                return room;
            }
        }
        return null;
    }

    /**
     * Gets the global arrival counter (shared across modules for priority calculation).
     */
    public static int getNextArrivalIndex() {
        return arrivalCounter++;
    }
}
