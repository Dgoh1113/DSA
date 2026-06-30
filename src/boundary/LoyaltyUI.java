package boundary;

import control.LoyaltyController;
import java.util.Scanner;

/**
 * Boundary: Loyalty & Rewards UI (Module 5).
 * Screen to view and redeem loyalty points.
 * All System.out / Scanner interactions live here.
 */
public class LoyaltyUI {

    private LoyaltyController controller;
    private Scanner scanner;

    public LoyaltyUI(LoyaltyController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void show() {
        // TODO: Implement loyalty points view and redemption forms
        System.out.println("\n--- Loyalty & Rewards Module ---");
        System.out.println("[Module 5 UI - To be implemented]");
    }
}
