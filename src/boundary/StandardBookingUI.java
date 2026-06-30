package boundary;

import control.StandardBookingController;
import java.util.Scanner;

/**
 * Boundary: Standard Booking UI (Module 1).
 * Form for walk-in registrations and standard booking operations.
 * All System.out / Scanner interactions live here.
 */
public class StandardBookingUI {

    private StandardBookingController controller;
    private Scanner scanner;

    public StandardBookingUI(StandardBookingController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void show() {
        // TODO: Implement sub-menu and forms for standard booking
        System.out.println("\n--- Standard Booking Module ---");
        System.out.println("[Module 1 UI - To be implemented]");
    }
}
