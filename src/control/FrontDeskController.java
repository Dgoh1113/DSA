package control;

import adt.CustomBinarySearchTree;
import adt.CustomLinkedList;
import entity.Guest;

/**
 * Controller: Module 4 — Front-Desk Service.
 * Handles BST searching and report generation.
 * Receives shared ADT instances from Main.java.
 */
public class FrontDeskController {

    private CustomBinarySearchTree<Guest> searchTree;
    private CustomLinkedList<Guest> masterRegistry;

    public FrontDeskController(CustomBinarySearchTree<Guest> searchTree,
                               CustomLinkedList<Guest> masterRegistry) {
        this.searchTree = searchTree;
        this.masterRegistry = masterRegistry;
    }

    // --- Skeleton methods (to be implemented) ---

    // public Guest searchGuest(String confirmationNumber) { }
    // public void generateReport1() { }
    // public void generateReport2() { }
}
