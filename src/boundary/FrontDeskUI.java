package boundary;

import control.FrontDeskController;
import java.util.Scanner;

/**
 * Boundary: Front-Desk Service UI (Module 4).
 * Search screen for guest lookup by 8-digit confirmation number.
 * All System.out / Scanner interactions live here.
 */
public class FrontDeskUI {

    private FrontDeskController controller;
    private Scanner scanner;

    public FrontDeskUI(FrontDeskController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void show() {
        // TODO: Implement search form and report display
        System.out.println("\n--- Front-Desk Service Module ---");
        System.out.println("[Module 4 UI - To be implemented]");
    }
}
