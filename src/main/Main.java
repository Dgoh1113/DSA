package main;

import adt.CustomBinarySearchTree;
import adt.CustomLinkedList;
import adt.CustomPriorityQueue;
import adt.CustomQueue;
import boundary.MainMenuUI;
import control.FrontDeskController;
import control.LoyaltyController;
import control.StandardBookingController;
import control.VIPAllocationController;
import entity.Guest;

/**
 * Main entry point for the TARUMT Resorts System.
 *
 * 1. Creates the shared ADT data structures (Master Registry, queues, tree).
 * 2. Injects them into the Controllers (Control layer).
 * 3. Passes the Controllers into the MainMenuUI (Boundary layer).
 * 4. Starts the application loop.
 */
public class Main {

    public static void main(String[] args) {

        // ============================
        // 1. Create shared ADT instances
        // ============================
        CustomLinkedList<Guest> masterRegistry       = new CustomLinkedList<>();
        CustomQueue<Guest> standardQueue             = new CustomQueue<>();
        CustomPriorityQueue<Guest> vipQueue          = new CustomPriorityQueue<>();
        CustomBinarySearchTree<Guest> searchTree     = new CustomBinarySearchTree<>();

        // ============================
        // 2. Create Controllers (Control layer)
        //    — inject the shared data structures
        // ============================
        StandardBookingController mod1 = new StandardBookingController(standardQueue, masterRegistry, searchTree);
        VIPAllocationController   mod2 = new VIPAllocationController(vipQueue, masterRegistry, searchTree);
        FrontDeskController       mod4 = new FrontDeskController(searchTree, masterRegistry);
        LoyaltyController         mod5 = new LoyaltyController(masterRegistry);

        // ============================
        // 3. Create Boundary (UI layer)
        //    — inject the controllers
        // ============================
        MainMenuUI mainMenu = new MainMenuUI(mod1, mod2, mod4, mod5);

        // ============================
        // 4. Start the application
        // ============================
        mainMenu.start();
    }
}
