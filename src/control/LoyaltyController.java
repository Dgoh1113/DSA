package control;

import adt.CustomLinkedList;
import entity.Guest;

/**
 * Controller: Module 5 — Loyalty and Rewards Service.
 * Handles point calculations, tier upgrades, and redemptions.
 * Receives the shared master registry from Main.java.
 */
public class LoyaltyController {

    private CustomLinkedList<Guest> masterRegistry;

    public LoyaltyController(CustomLinkedList<Guest> masterRegistry) {
        this.masterRegistry = masterRegistry;
    }

    // --- Skeleton methods (to be implemented) ---

    // public void updatePoints(Guest guest, int points) { }
    // public void upgradeTier(Guest guest) { }
    // public void redeemPoints(Guest guest, int points) { }
}
