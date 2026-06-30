package boundary;

import control.FrontDeskController;
import control.LoyaltyController;
import control.StandardBookingController;
import control.VIPAllocationController;
import java.util.Scanner;

/**
 * Boundary: Main Menu UI.
 * Displays the primary menu and routes to sub-module UIs.
 * All System.out / Scanner interactions live here.
 */
public class MainMenuUI {

    private StandardBookingUI standardBookingUI;
    private VIPAllocationUI vipAllocationUI;
    private FrontDeskUI frontDeskUI;
    private LoyaltyUI loyaltyUI;
    private Scanner scanner;

    public MainMenuUI(StandardBookingController mod1,
                      VIPAllocationController mod2,
                      FrontDeskController mod4,
                      LoyaltyController mod5) {
        this.scanner = new Scanner(System.in);
        this.standardBookingUI = new StandardBookingUI(mod1, scanner);
        this.vipAllocationUI = new VIPAllocationUI(mod2, scanner);
        this.frontDeskUI = new FrontDeskUI(mod4, scanner);
        this.loyaltyUI = new LoyaltyUI(mod5, scanner);
    }

    public void start() {
        int choice = 0;
        do {
            displayMenu();
            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    standardBookingUI.show();
                    break;
                case 2:
                    vipAllocationUI.show();
                    break;
                case 3:
                    frontDeskUI.show();
                    break;
                case 4:
                    loyaltyUI.show();
                    break;
                case 0:
                    System.out.println("Thank you for using TARUMT Resorts System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 0);
    }

    private void displayMenu() {
        System.out.println("\n========================================");
        System.out.println("   TARUMT RESORTS MANAGEMENT SYSTEM");
        System.out.println("========================================");
        System.out.println("1. Walk-In / Standard Booking");
        System.out.println("2. VIP Priority Allocation");
        System.out.println("3. Front-Desk Service");
        System.out.println("4. Loyalty & Rewards");
        System.out.println("0. Exit");
        System.out.println("========================================");
        System.out.print("Enter your choice: ");
    }
}
