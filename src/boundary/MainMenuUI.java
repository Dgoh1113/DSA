package boundary;

import control.FrontDeskController;
import control.LoyaltyController;
import control.PartnerController;
import control.StandardBookingController;
import control.VIPAllocationController;
import entity.Guest;
import entity.LoyaltyAccount;
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
    private Scanner scanner;
    private StandardBookingController bookingController;
    private LoyaltyController loyaltyController;
    private Guest currentMember;

    public MainMenuUI(StandardBookingController mod1,
                      VIPAllocationController mod2,
                      FrontDeskController mod3,
                      LoyaltyController mod4,
                      PartnerController mod5) {
        this.scanner = new Scanner(System.in);
        this.bookingController = mod1;
        this.loyaltyController = mod4;
        this.standardBookingUI = new StandardBookingUI(mod1, scanner);
        this.vipAllocationUI = new VIPAllocationUI(mod2, scanner);
        this.frontDeskUI = new FrontDeskUI(mod3, scanner);
        this.loyaltyUI = new LoyaltyUI(mod4, scanner);
        this.partnerUI = new PartnerUI(mod5, scanner);
    }

    public void start() {
        currentMember = authenticateMember();
        if (currentMember == null) return;
        standardBookingUI.setAuthenticatedGuest(currentMember);
        vipAllocationUI.setAuthenticatedGuest(currentMember);

        int choice = 0;
        do {
            utility.UIUtils.clearScreen();
            utility.UIUtils.printMainTitleHeader();
            displayMenu();
            choice = utility.UIUtils.safeReadInt(scanner);

            switch (choice) {
                case 1:
                    if (isVIPEligible()) {
                        showUnavailableOption("VIP members must book through Module 2.");
                    } else {
                        standardBookingUI.show();
                    }
                    break;
                case 2:
                    if (isVIPEligible()) {
                        vipAllocationUI.show();
                    } else {
                        showUnavailableOption("Module 2 is available only to VIP members.");
                    }
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
        System.out.println("  Signed in as     : " + currentMember.getName());
        System.out.println("  Member ID        : " + currentMember.getGuestId());
        System.out.println("  Loyalty Tier     : " + currentMember.getLoyaltyTier());
        LoyaltyAccount loyaltyAccount =
                loyaltyController.viewMemberProfile(currentMember.getGuestId());
        System.out.println("  Loyalty Points   : "
                + (loyaltyAccount == null ? 0 : loyaltyAccount.getTotalPoints()));
        if (loyaltyAccount != null
                && loyaltyController.hasPointsExpiringWithinDays(
                        currentMember.getGuestId(), 30)) {
            System.out.println(utility.UIUtils.YELLOW + utility.UIUtils.BOLD
                    + "  [!] POINTS EXPIRY ALERT: Your points expire on "
                    + loyaltyAccount.getPointsExpiryDate() + " (within 30 days)."
                    + utility.UIUtils.RESET);
        }
        utility.UIUtils.printSectionHeader("CORE SYSTEM MODULES", utility.UIUtils.CYAN);
        if (isVIPEligible()) {
            System.out.println("  " + utility.UIUtils.YELLOW + utility.UIUtils.BOLD + "2." + utility.UIUtils.RESET + " VIP Priority Allocation     " + utility.UIUtils.YELLOW + "[ Max-Heap ADT ]" + utility.UIUtils.RESET);
        } else {
            System.out.println("  " + utility.UIUtils.CYAN + utility.UIUtils.BOLD + "1." + utility.UIUtils.RESET + " Walk-In & Standard Booking  " + utility.UIUtils.CYAN + "[ Queue ADT ]" + utility.UIUtils.RESET);
        }
        System.out.println("  " + utility.UIUtils.GREEN + utility.UIUtils.BOLD + "3." + utility.UIUtils.RESET + " Front-Desk Service          " + utility.UIUtils.GREEN + "[ BST Search ADT ]" + utility.UIUtils.RESET);
        System.out.println("  " + utility.UIUtils.MAGENTA + utility.UIUtils.BOLD + "4." + utility.UIUtils.RESET + " Loyalty & Rewards Program   " + utility.UIUtils.MAGENTA + "[ Doubly-Linked List + Sorting ]" + utility.UIUtils.RESET);
        System.out.println("  " + utility.UIUtils.BLUE + utility.UIUtils.BOLD + "5." + utility.UIUtils.RESET + " Strategic Partners & Refer  " + utility.UIUtils.BLUE + "[ Strategic Partner Network ]" + utility.UIUtils.RESET);

        utility.UIUtils.printSectionHeader("SYSTEM CONTROL", utility.UIUtils.RED);
        System.out.println("  " + utility.UIUtils.RED + utility.UIUtils.BOLD + "0." + utility.UIUtils.RESET + " Exit Application");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.print(utility.UIUtils.BOLD + "Enter your choice: " + utility.UIUtils.RESET);
    }

    private Guest authenticateMember() {
        while (true) {
            utility.UIUtils.clearScreen();
            utility.UIUtils.printMainTitleHeader();
            utility.UIUtils.printSectionHeader("IS MEMBER?", utility.UIUtils.CYAN);
            System.out.println("  1. Enter Phone Number");
            System.out.println("  2. Register New Member");
            System.out.println("  0. Exit Application");
            System.out.print("Enter your choice: ");
            int choice = utility.UIUtils.safeReadInt(scanner);

            if (choice == 0) return null;
            if (choice == 1) {
                String phone = utility.ValidationUtils.getValidContactNo(
                        scanner, "Enter registered phone number: ");
                Guest member = bookingController.findGuestByContactNo(phone);
                if (member != null) {
                    System.out.println("\nMember found: " + member.getName()
                            + " | Tier: " + member.getLoyaltyTier());
                    return member;
                }
                System.out.println("\nNo member is registered with that phone number.");
                utility.UIUtils.pressEnterToContinue(scanner);
            } else if (choice == 2) {
                String phone = utility.ValidationUtils.getValidContactNo(
                        scanner, "Enter phone number: ");
                Guest existing = bookingController.findGuestByContactNo(phone);
                if (existing != null) {
                    System.out.println("\nThis phone number is already registered to "
                            + existing.getName() + ". Signing in to the existing account.");
                    return existing;
                }

                String name = utility.ValidationUtils.getValidName(scanner, "Enter guest name: ");
                String icPassport = utility.ValidationUtils.getValidIcPassport(
                        scanner, "Enter IC / Passport No: ");
                String email = utility.ValidationUtils.getValidEmail(scanner, "Enter email address: ");
                Guest member = bookingController.registerNewMember(name, icPassport, phone, email);
                System.out.println("\nMember registered successfully. Member ID: "
                        + member.getGuestId() + " | Tier: " + member.getLoyaltyTier());
                return member;
            } else {
                System.out.println("Invalid option. Please choose 1, 2, or 0.");
                utility.UIUtils.pressEnterToContinue(scanner);
            }
        }
    }

    private boolean isVIPEligible() {
        return currentMember != null && currentMember.isVIP();
    }

    private void showUnavailableOption(String message) {
        System.out.println(message);
        utility.UIUtils.pressEnterToContinue(scanner);
    }

    private int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }
}

