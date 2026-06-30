package control;

import adt.CustomBinarySearchTree;
import adt.CustomLinkedList;
import adt.CustomQueue;
import entity.Guest;

/**
 * Controller: Module 1 — Walk-In Registrations & Standard Booking Procedure.
 * Handles all CustomQueue operations for standard bookings.
 * Receives shared ADT instances from Main.java.
 */
public class StandardBookingController {

    private CustomQueue<Guest> standardQueue;
    private CustomLinkedList<Guest> masterRegistry;
    private CustomBinarySearchTree<Guest> searchTree;

    public StandardBookingController(CustomQueue<Guest> standardQueue,
                                     CustomLinkedList<Guest> masterRegistry,
                                     CustomBinarySearchTree<Guest> searchTree) {
        this.standardQueue = standardQueue;
        this.masterRegistry = masterRegistry;
        this.searchTree = searchTree;
    }

    // --- Skeleton methods (to be implemented) ---

    // public void registerBooking(Guest guest) { }
    // public Guest processNextBooking() { }
    // public Guest peekNextBooking() { }
    // public int getQueueSize() { }
}
