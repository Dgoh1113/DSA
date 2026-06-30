package boundary;

import control.VIPAllocationController;
import java.util.Scanner;

/**
 * Boundary: VIP Allocation UI (Module 2).
 * Dashboard showing VIPs waiting for rooms.
 * All System.out / Scanner interactions live here.
 */
public class VIPAllocationUI {

    private VIPAllocationController controller;
    private Scanner scanner;

    public VIPAllocationUI(VIPAllocationController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void show() {
        // TODO: Implement sub-menu and dashboard for VIP allocation
        System.out.println("\n--- VIP Priority Allocation Module ---");
        System.out.println("[Module 2 UI - To be implemented]");
    }
}
