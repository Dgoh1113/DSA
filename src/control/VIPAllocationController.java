package control;

import adt.BinaryMaxHeap;
import adt.BinarySearchTree;
import adt.DoublyLinkedList;
import entity.Guest;
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
    private DoublyLinkedList<Room> roomInventory;
    private BinarySearchTree<Reservation> searchTree;
    private UndoController undoController;

    public VIPAllocationController(BinaryMaxHeap<VIPReservation> vipQueue,
                                   DoublyLinkedList<Guest> guestRegistry,
                                   DoublyLinkedList<Room> roomInventory,
                                   BinarySearchTree<Reservation> searchTree) {
        this.vipQueue = vipQueue;
        this.guestRegistry = guestRegistry;
        this.roomInventory = roomInventory;
        this.searchTree = searchTree;
    }

    public void setUndoController(UndoController undoController) {
        this.undoController = undoController;
    }

    /**
     * Adds a VIP guest booking into the priority queue.
     * Calculates the priority score and wraps the reservation in a VIPReservation
     * for Max-Heap ordering.
     *
     * @return The created Reservation.
     */
    public Reservation addVIPBooking(String name, String icPassport, String contactNo,
                                      String email, String loyaltyTier, String roomType,
                                      String checkInDate, String checkOutDate) {
        // Create VIP guest
        Guest guest = new Guest(name, icPassport, contactNo, email, loyaltyTier);
        guestRegistry.add(guest);

        return createVIPReservation(guest, roomType, checkInDate, checkOutDate);
    }

    /** Creates a VIP booking using an authenticated loyalty member profile. */
    public Reservation addVIPBookingForGuest(Guest guest, String roomType,
                                              String checkInDate, String checkOutDate) {
        if (guest == null || !guest.isVIP()) return null;
        return createVIPReservation(guest, roomType, checkInDate, checkOutDate);
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
                && "CANCELLED".equals(vipRes.getReservation().getBookingStatus())) {
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
        VIPReservation vipRes = vipQueue.peek();
        return (vipRes != null) ? vipRes.getReservation() : null;
    }

    /**
     * Returns the size of the VIP queue.
     */
    public int getVIPQueueSize() {
        return vipQueue.size();
    }

    /**
     * Returns all VIP reservations currently in the heap.
     */
    public DoublyLinkedList<VIPReservation> getVIPQueueList() {
        return vipQueue.toList();
    }

    /**
     * Checks if the VIP queue is empty.
     */
    public boolean isVIPQueueEmpty() {
        return vipQueue.isEmpty();
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
