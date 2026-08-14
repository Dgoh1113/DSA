package utility;

import java.util.Scanner;

/**
 * Utility: UIUtils — Rich Console Aesthetics & Dynamic Screen Management.
 * Features:
 * - Dynamic Sub-Headers that automatically match the current working screen
 * - Unique ANSI color palettes per module
 * - Robust line and integer reading (prevents NoSuchElementException)
 * - Automatic screen clearing across view transitions
 * - "Press Enter to continue" prompts
 */
public class UIUtils {

    // ANSI Color Codes
    public static final String RESET          = "\u001B[0m";
    public static final String BOLD           = "\u001B[1m";

    // Text Colors
    public static final String RED            = "\u001B[91m";
    public static final String GREEN          = "\u001B[92m";
    public static final String YELLOW         = "\u001B[93m";
    public static final String BLUE           = "\u001B[94m";
    public static final String MAGENTA        = "\u001B[95m";
    public static final String PURPLE         = "\u001B[95m";
    public static final String CYAN           = "\u001B[96m";
    public static final String WHITE          = "\u001B[97m";

    /**
     * Clears the console terminal screen.
     */
    public static void clearScreen() {
        try {
            System.out.print("\033[H\033[2J");
            System.out.flush();

            String os = System.getProperty("os.name");
            if (os != null && os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }

    /**
     * Safely reads a line of text from Scanner without throwing exceptions.
     */
    public static String safeReadLine(Scanner scanner) {
        if (scanner != null && scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        if (scanner != null && !scanner.hasNextLine()) {
            return "cancel";
        }
        return "";
    }

    /**
     * Safely reads an integer from Scanner and clears the line buffer.
     */
    public static int safeReadInt(Scanner scanner) {
        while (true) {
            if (scanner == null || !scanner.hasNext()) {
                return 0;
            }
            if (scanner.hasNextInt()) {
                int val = scanner.nextInt();
                if (scanner.hasNextLine()) {
                    scanner.nextLine(); // consume trailing newline
                }
                return val;
            }
            System.out.print(RED + "Please enter a valid whole number: " + RESET);
            scanner.next(); // consume invalid token
        }
    }

    /**
     * Pauses and waits for user to press ENTER before returning/clearing screen.
     */
    public static void pressEnterToContinue(Scanner scanner) {
        System.out.println("\n" + YELLOW + "Press [ENTER] to return..." + RESET);
        safeReadLine(scanner);
    }

    /**
     * Post-operation navigation prompt.
     * Prevents looping back into data entry forms after finishing details.
     * Returns true if user selected [0] to Quit to Main Menu right away,
     * or false if user selected [1] or [ENTER] to return to Sub-Module Menu.
     */
    public static boolean promptPostOperationNavigation(Scanner scanner) {
        System.out.println("\n" + CYAN + "──────────────────────────────────────────────────────────" + RESET);
        System.out.println("  " + BOLD + "[1]" + RESET + " Return to Sub-Module Menu");
        System.out.println("  " + BOLD + "[0]" + RESET + " Quit to Main Menu Right Away");
        System.out.println(CYAN + "──────────────────────────────────────────────────────────" + RESET);
        System.out.print(BOLD + "Enter choice (Press [ENTER] for Sub-Module Menu or '0' for Main Menu): " + RESET);

        String input = safeReadLine(scanner).trim();
        return "0".equals(input);
    }

    /**
     * Main Menu Title Header (Golden Yellow & White)
     */
    public static void printMainTitleHeader() {
        System.out.println(YELLOW + BOLD + "╔═════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(YELLOW + BOLD + "║                " + WHITE + "TARUMT RESORTS MANAGEMENT SYSTEM" + YELLOW + "                     ║" + RESET);
        System.out.println(YELLOW + BOLD + "║             " + CYAN + "Hotel Reservation & Strategic Partners System" + YELLOW + "           ║" + RESET);
        System.out.println(YELLOW + BOLD + "╚═════════════════════════════════════════════════════════════════════╝" + RESET);
    }

    /**
     * Module 1 Title Banner (Bright Cyan - Standard Booking)
     */
    public static void printModule1Header() {
        System.out.println(CYAN + BOLD + "╔═════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + BOLD + "║     MODULE 1: WALK-IN & STANDARD BOOKING QUEUE [ Queue ADT ]       ║" + RESET);
        System.out.println(CYAN + BOLD + "╚═════════════════════════════════════════════════════════════════════╝" + RESET);
    }

    /**
     * Module 2 Title Banner (Bright Gold / Yellow - VIP Priority Heap)
     */
    public static void printModule2Header() {
        System.out.println(YELLOW + BOLD + "╔═════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(YELLOW + BOLD + "║     MODULE 2: VIP PRIORITY ROOM ALLOCATION     [ Max-Heap ADT ]    ║" + RESET);
        System.out.println(YELLOW + BOLD + "╚═════════════════════════════════════════════════════════════════════╝" + RESET);
    }

    /**
     * Module 3 Title Banner (Bright Emerald Green - Front-Desk BST)
     */
    public static void printModule3Header() {
        System.out.println(GREEN + BOLD + "╔═════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(GREEN + BOLD + "║     MODULE 3: FRONT-DESK SERVICE & BST LOOKUP  [ BST ADT ]         ║" + RESET);
        System.out.println(GREEN + BOLD + "╚═════════════════════════════════════════════════════════════════════╝" + RESET);
    }

    /**
     * Module 4 Title Banner (Bright Magenta / Purple - Loyalty DLL & Sort)
     */
    public static void printModule4Header() {
        System.out.println(MAGENTA + BOLD + "╔═════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(MAGENTA + BOLD + "║     MODULE 4: LOYALTY & REWARDS PROGRAM        [ DLL + Sorting ]   ║" + RESET);
        System.out.println(MAGENTA + BOLD + "╚═════════════════════════════════════════════════════════════════════╝" + RESET);
    }

    /**
     * Module 5 Title Banner (Bright Royal Blue - Strategic Partners)
     */
    public static void printModule5Header() {
        System.out.println(BLUE + BOLD + "╔═════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(BLUE + BOLD + "║     MODULE 5: STRATEGIC PARTNERS & REFERRALS   [ Customer Referrals ] ║" + RESET);
        System.out.println(BLUE + BOLD + "╚═════════════════════════════════════════════════════════════════════╝" + RESET);
    }

    /**
     * Dynamic Sub-Header — Auto-matches the active working screen!
     */
    public static void printSubHeader(String screenTitle, String color) {
        clearScreen();
        System.out.println(color + BOLD + "╔═════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(color + BOLD + "  CURRENT SCREEN: " + WHITE + screenTitle.toUpperCase() + RESET);
        System.out.println(color + BOLD + "╚═════════════════════════════════════════════════════════════════════╝\n" + RESET);
    }

    /**
     * Categorized Sub-Section Badge Header
     */
    public static void printSectionHeader(String sectionName, String color) {
        System.out.println("\n" + color + BOLD + "─── [ " + sectionName.toUpperCase() + " ] ─────────────────────────────────────────" + RESET);
    }
}
