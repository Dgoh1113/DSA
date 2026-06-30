package control;

import adt.CustomBinarySearchTree;
import adt.CustomLinkedList;
import adt.CustomPriorityQueue;
import entity.Guest;

/**
 * Controller: Module 2 — VIP & Loyalty Tier Priority Room Allocation.
 * Handles all CustomPriorityQueue sorting operations.
 * Receives shared ADT instances from Main.java.
 */
public class VIPAllocationController {

    private CustomPriorityQueue<Guest> vipQueue;
    private CustomLinkedList<Guest> masterRegistry;
    private CustomBinarySearchTree<Guest> searchTree;

    public VIPAllocationController(CustomPriorityQueue<Guest> vipQueue,
                                   CustomLinkedList<Guest> masterRegistry,
                                   CustomBinarySearchTree<Guest> searchTree) {
        this.vipQueue = vipQueue;
        this.masterRegistry = masterRegistry;
        this.searchTree = searchTree;
    }

    // --- Skeleton methods (to be implemented) ---

    // public void addVIPGuest(Guest guest) { }
    // public Guest allocateNextVIP() { }
    // public Guest peekNextVIP() { }
}
