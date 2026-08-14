package boundary;

import control.FrontDeskController;
import control.LoyaltyController;
import control.PartnerController;
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
    private PartnerUI partnerUI;
    private UndoUI undoUI;
    private Scanner scanner;

    public MainMenuUI(StandardBookingController mod1,
                      VIPAllocationController mod2,
                      FrontDeskController mod3,
                      LoyaltyController mod4,
                      PartnerController mod5,
                      control.UndoController undoController) {
        this.scanner = new Scanner(System.in);
        this.standardBookingUI = new StandardBookingUI(mod1, scanner);
        this.vipAllocationUI = new VIPAllocationUI(mod2, scanner);
        this.frontDeskUI = new FrontDeskUI(mod3, scanner);
        this.loyaltyUI = new LoyaltyUI(mod4, scanner);
        this.partnerUI = new PartnerUI(mod5, scanner);
        this.undoUI = new UndoUI(undoController, scanner);
    }

    public void start() {
        int choice = 0;
        do {
            utility.UIUtils.clearScreen();
            utility.UIUtils.printMainTitleHeader();
            displayMenu();
            choice = utility.UIUtils.safeReadInt(scanner);

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
                case 5:
                    partnerUI.show();
                    break;
                case 6:
                    undoUI.show();
                    break;
                case 0:
                    utility.UIUtils.clearScreen();
                    System.out.println(utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "=====================================================================" + utility.UIUtils.RESET);
                    System.out.println(utility.UIUtils.WHITE + utility.UIUtils.BOLD + "  Thank you for using TARUMT Resorts System." + utility.UIUtils.RESET);
                    System.out.println(utility.UIUtils.CYAN + "  All session data saved. Goodbye!" + utility.UIUtils.RESET);
                    System.out.println(utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "=====================================================================" + utility.UIUtils.RESET);
                    break;
                default:
                    System.out.println(utility.UIUtils.RED + "Invalid option. Please try again." + utility.UIUtils.RESET);
                    utility.UIUtils.pressEnterToContinue(scanner);
            }
        } while (choice != 0);
    }

    private void displayMenu() {
        utility.UIUtils.printSectionHeader("CORE SYSTEM MODULES", utility.UIUtils.CYAN);
        System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "1." + utility.UIUtils.RESET + " Walk-In & Standard Booking  " + utility.UIUtils.CYAN + "[ Queue ADT ]" + utility.UIUtils.RESET);
        System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "2." + utility.UIUtils.RESET + " VIP Priority Allocation     " + utility.UIUtils.YELLOW + "[ Max-Heap ADT ]" + utility.UIUtils.RESET);
        System.out.println("  " + utility.UIUtils.GREEN + utility.UIUtils.BOLD + "3." + utility.UIUtils.RESET + " Front-Desk Service          " + utility.UIUtils.GREEN + "[ BST Search ADT ]" + utility.UIUtils.RESET);
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "4." + utility.UIUtils.RESET + " Loyalty & Rewards Program   " + utility.UIUtils.MAGENTA + "[ Doubly-Linked List + Sorting ]" + utility.UIUtils.RESET);
        System.out.println("  " + utility.UIUtils.BLUE + utility.UIUtils.BOLD + "5." + utility.UIUtils.RESET + " Strategic Partners & Refer  " + utility.UIUtils.BLUE + "[ Strategic Partner Network ]" + utility.UIUtils.RESET);

        utility.UIUtils.printSectionHeader("CROSS-MODULE SYSTEM UTILITIES", utility.UIUtils.PURPLE);
        System.out.println("  " + utility.UIUtils.PURPLE + utility.UIUtils.BOLD + "6." + utility.UIUtils.RESET + " Transaction Undo Center     " + utility.UIUtils.PURPLE + "[ System Utility — Stack ADT ]" + utility.UIUtils.RESET);

        utility.UIUtils.printSectionHeader("SYSTEM CONTROL", utility.UIUtils.RED);
        System.out.println("  " + utility.UIUtils.RED + utility.UIUtils.BOLD + "0." + utility.UIUtils.RESET + " Exit Application");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.print(utility.UIUtils.BOLD + "Enter your choice: " + utility.UIUtils.RESET);
    }

    private int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }
}

