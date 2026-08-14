package utility;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Utility: ValidationUtils — Centralized Input Validation Framework.
 * Validates user inputs across all Console UI forms:
 * - Guest Name
 * - IC / Passport Number
 * - Contact / Phone Number
 * - Email Address
 * - Dates (Check-In & Check-Out format & logic)
 * - Room Types & Loyalty Tiers
 * - Monetary Amounts & Positive Numbers
 * - Step-by-Step Form Navigation ('b' for back, '0' for main menu)
 */
public class ValidationUtils {

    // Regular Expressions
    private static final String NAME_REGEX = "^[a-zA-Z\\s'.\\-\\/]{2,50}$";
    private static final String IC_PASSPORT_REGEX = "^(\\d{6}[-\\s]?\\d{2}[-\\s]?\\d{4}|\\d{12}|[A-Za-z0-9]{6,14})$";
    private static final String PHONE_REGEX = "^(\\+?6?0\\d{1,2}[-\\s]?\\d{7,8}|\\+?\\d{8,15})$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    /**
     * Prompt and validate Guest Name.
     */
    public static String getValidName(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = UIUtils.safeReadLine(scanner);
            if (isValidName(input)) {
                return input;
            }
            System.out.println("  [!] ERROR: Invalid name. Must contain only letters and spaces (min 2 characters).");
        }
    }

    public static boolean isValidName(String name) {
        return name != null && name.matches(NAME_REGEX);
    }

    /**
     * Prompt and validate IC / Passport Number.
     * Accepts Malaysian IC (e.g. 980101-14-1234 or 980101141234) or Passport (6-14 alphanumeric).
     */
    public static String getValidIcPassport(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = UIUtils.safeReadLine(scanner);
            if (isValidIcPassport(input)) {
                return input;
            }
            System.out.println("  [!] ERROR: Invalid IC/Passport. Enter 12-digit IC (e.g. 980101-14-1234) or valid Passport.");
        }
    }

    public static boolean isValidIcPassport(String icPassport) {
        return icPassport != null && icPassport.matches(IC_PASSPORT_REGEX);
    }

    /**
     * Prompt and validate Contact / Phone Number.
     */
    public static String getValidContactNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = UIUtils.safeReadLine(scanner);
            if (isValidContactNo(input)) {
                return input;
            }
            System.out.println("  [!] ERROR: Invalid phone number. Enter a valid contact number (e.g. 012-3456789).");
        }
    }

    public static boolean isValidContactNo(String contactNo) {
        return contactNo != null && contactNo.matches(PHONE_REGEX);
    }

    /**
     * Prompt and validate Email Address.
     */
    public static String getValidEmail(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = UIUtils.safeReadLine(scanner);
            if (isValidEmail(input)) {
                return input;
            }
            System.out.println("  [!] ERROR: Invalid email address. Example: name@example.com");
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    /**
     * Prompt and validate Room Type (STANDARD | DELUXE | SUITE).
     */
    public static String getValidRoomType(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = UIUtils.safeReadLine(scanner).toUpperCase();
            if (input.equals("STANDARD") || input.equals("DELUXE") || input.equals("SUITE")) {
                return input;
            }
            System.out.println("  [!] ERROR: Invalid room type. Choose from: STANDARD | DELUXE | SUITE");
        }
    }

    /**
     * Prompt and validate Loyalty Tier (SILVER | GOLD | PLATINUM | DIAMOND).
     */
    public static String getValidLoyaltyTier(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = UIUtils.safeReadLine(scanner).toUpperCase();
            if (input.equals("STANDARD") || input.equals("SILVER") || input.equals("GOLD") ||
                input.equals("PLATINUM") || input.equals("DIAMOND")) {
                return input;
            }
            System.out.println("  [!] ERROR: Invalid tier. Choose from: STANDARD | SILVER | GOLD | PLATINUM | DIAMOND");
        }
    }

    /**
     * Prompt and validate Date format (YYYY-MM-DD).
     */
    public static String getValidDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = UIUtils.safeReadLine(scanner);
            if (isValidDate(input)) {
                return input;
            }
            System.out.println("  [!] ERROR: Invalid date format or date. Please enter in YYYY-MM-DD format (e.g. 2026-08-20).");
        }
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Prompt and validate Check-Out Date to ensure it is strictly AFTER Check-In Date.
     */
    public static String getValidCheckOutDate(Scanner scanner, String prompt, String checkInDateStr) {
        LocalDate checkInDate = LocalDate.parse(checkInDateStr);
        while (true) {
            String checkOutStr = getValidDate(scanner, prompt);
            LocalDate checkOutDate = LocalDate.parse(checkOutStr);
            if (checkOutDate.isAfter(checkInDate)) {
                return checkOutStr;
            }
            System.out.println("  [!] ERROR: Check-out date must be AFTER check-in date (" + checkInDateStr + ").");
        }
    }

    /**
     * Prompt and validate integer input.
     */
    public static int getValidInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                int val = scanner.nextInt();
                scanner.nextLine(); // consume newline
                return val;
            }
            System.out.println("  [!] ERROR: Invalid input. Please enter a valid whole number.");
            scanner.next(); // consume invalid token
        }
    }

    /**
     * Prompt and validate positive double input.
     */
    public static double getValidPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextDouble()) {
                double val = scanner.nextDouble();
                scanner.nextLine(); // consume newline
                if (val >= 0) {
                    return val;
                }
            } else {
                scanner.next(); // consume invalid token
            }
            System.out.println("  [!] ERROR: Invalid input. Please enter a positive number.");
        }
    }

    // =========================================================================
    // STEP-BY-STEP FORM NAVIGATION HELPERS ('b' for Back, '0' for Main Menu)
    // =========================================================================

    public static StepResult checkControlCommands(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.equalsIgnoreCase("b") || trimmed.equalsIgnoreCase("back")) {
            return StepResult.goBack();
        }
        if (trimmed.equals("0") || trimmed.equalsIgnoreCase("main")) {
            return StepResult.quitToMain();
        }
        if (trimmed.equalsIgnoreCase("cancel") || trimmed.equalsIgnoreCase("exit")) {
            return StepResult.cancel();
        }
        return null;
    }

    public static StepResult readValidNameStep(Scanner scanner, String prompt, String defaultVal) {
        while (true) {
            String p = prompt + (defaultVal != null && !defaultVal.isEmpty() ? " [" + defaultVal + "]" : "") + " : ";
            System.out.print(p);
            String input = UIUtils.safeReadLine(scanner);
            if (input.isEmpty() && defaultVal != null && !defaultVal.isEmpty()) {
                return StepResult.success(defaultVal);
            }
            StepResult cmd = checkControlCommands(input);
            if (cmd != null) return cmd;

            if (isValidName(input)) {
                return StepResult.success(input);
            }
            System.out.println("  [!] ERROR: Invalid name. Must contain only letters and spaces (min 2 characters).");
        }
    }

    public static StepResult readValidIcPassportStep(Scanner scanner, String prompt, String defaultVal) {
        while (true) {
            String p = prompt + (defaultVal != null && !defaultVal.isEmpty() ? " [" + defaultVal + "]" : "") + " : ";
            System.out.print(p);
            String input = UIUtils.safeReadLine(scanner);
            if (input.isEmpty() && defaultVal != null && !defaultVal.isEmpty()) {
                return StepResult.success(defaultVal);
            }
            StepResult cmd = checkControlCommands(input);
            if (cmd != null) return cmd;

            if (isValidIcPassport(input)) {
                return StepResult.success(input);
            }
            System.out.println("  [!] ERROR: Invalid IC/Passport. Enter 12-digit IC (e.g. 980101-14-1234) or valid Passport.");
        }
    }

    public static StepResult readValidContactNoStep(Scanner scanner, String prompt, String defaultVal) {
        while (true) {
            String p = prompt + (defaultVal != null && !defaultVal.isEmpty() ? " [" + defaultVal + "]" : "") + " : ";
            System.out.print(p);
            String input = UIUtils.safeReadLine(scanner);
            if (input.isEmpty() && defaultVal != null && !defaultVal.isEmpty()) {
                return StepResult.success(defaultVal);
            }
            StepResult cmd = checkControlCommands(input);
            if (cmd != null) return cmd;

            if (isValidContactNo(input)) {
                return StepResult.success(input);
            }
            System.out.println("  [!] ERROR: Invalid phone number. Enter a valid contact number (e.g. 012-3456789).");
        }
    }

    public static StepResult readValidEmailStep(Scanner scanner, String prompt, String defaultVal) {
        while (true) {
            String p = prompt + (defaultVal != null && !defaultVal.isEmpty() ? " [" + defaultVal + "]" : "") + " : ";
            System.out.print(p);
            String input = UIUtils.safeReadLine(scanner);
            if (input.isEmpty() && defaultVal != null && !defaultVal.isEmpty()) {
                return StepResult.success(defaultVal);
            }
            StepResult cmd = checkControlCommands(input);
            if (cmd != null) return cmd;

            if (isValidEmail(input)) {
                return StepResult.success(input);
            }
            System.out.println("  [!] ERROR: Invalid email address. Example: name@example.com");
        }
    }

    public static StepResult readValidRoomTypeStep(Scanner scanner, String prompt, String defaultVal) {
        while (true) {
            String p = prompt + (defaultVal != null && !defaultVal.isEmpty() ? " [" + defaultVal + "]" : "") + " : ";
            System.out.print(p);
            String input = UIUtils.safeReadLine(scanner).toUpperCase();
            if (input.isEmpty() && defaultVal != null && !defaultVal.isEmpty()) {
                return StepResult.success(defaultVal);
            }
            StepResult cmd = checkControlCommands(input);
            if (cmd != null) return cmd;

            if (input.equals("STANDARD") || input.equals("DELUXE") || input.equals("SUITE")) {
                return StepResult.success(input);
            }
            System.out.println("  [!] ERROR: Invalid room type. Choose from: STANDARD | DELUXE | SUITE");
        }
    }

    public static StepResult readValidLoyaltyTierStep(Scanner scanner, String prompt, String defaultVal) {
        while (true) {
            String p = prompt + (defaultVal != null && !defaultVal.isEmpty() ? " [" + defaultVal + "]" : "") + " : ";
            System.out.print(p);
            String input = UIUtils.safeReadLine(scanner).toUpperCase();
            if (input.isEmpty() && defaultVal != null && !defaultVal.isEmpty()) {
                return StepResult.success(defaultVal);
            }
            StepResult cmd = checkControlCommands(input);
            if (cmd != null) return cmd;

            if (input.equals("STANDARD") || input.equals("SILVER") || input.equals("GOLD") ||
                input.equals("PLATINUM") || input.equals("DIAMOND")) {
                return StepResult.success(input);
            }
            System.out.println("  [!] ERROR: Invalid tier. Choose from: STANDARD | SILVER | GOLD | PLATINUM | DIAMOND");
        }
    }

    public static StepResult readValidDateStep(Scanner scanner, String prompt, String defaultVal) {
        while (true) {
            String p = prompt + (defaultVal != null && !defaultVal.isEmpty() ? " [" + defaultVal + "]" : "") + " : ";
            System.out.print(p);
            String input = UIUtils.safeReadLine(scanner);
            if (input.isEmpty() && defaultVal != null && !defaultVal.isEmpty()) {
                return StepResult.success(defaultVal);
            }
            StepResult cmd = checkControlCommands(input);
            if (cmd != null) return cmd;

            if (isValidDate(input)) {
                return StepResult.success(input);
            }
            System.out.println("  [!] ERROR: Invalid date format. Enter in YYYY-MM-DD format (e.g. 2026-08-20).");
        }
    }

    public static StepResult readValidCheckOutDateStep(Scanner scanner, String prompt, String checkInDateStr, String defaultVal) {
        if (!isValidDate(checkInDateStr)) return StepResult.goBack();
        LocalDate checkInDate = LocalDate.parse(checkInDateStr);
        while (true) {
            StepResult res = readValidDateStep(scanner, prompt, defaultVal);
            if (!res.isSuccess()) return res;

            LocalDate checkOutDate = LocalDate.parse(res.getValue());
            if (checkOutDate.isAfter(checkInDate)) {
                return res;
            }
            System.out.println("  [!] ERROR: Check-out date must be strictly AFTER check-in date (" + checkInDateStr + ").");
        }
    }

    public static StepResult readValidStringStep(Scanner scanner, String prompt, String defaultVal, boolean allowEmpty) {
        while (true) {
            String p = prompt + (defaultVal != null && !defaultVal.isEmpty() ? " [" + defaultVal + "]" : "") + " : ";
            System.out.print(p);
            String input = UIUtils.safeReadLine(scanner);
            if (input.isEmpty() && defaultVal != null && !defaultVal.isEmpty()) {
                return StepResult.success(defaultVal);
            }
            StepResult cmd = checkControlCommands(input);
            if (cmd != null) return cmd;

            if (!input.isEmpty() || allowEmpty) {
                return StepResult.success(input);
            }
            System.out.println("  [!] ERROR: Field cannot be empty.");
        }
    }

    public static StepResult readValidPositiveDoubleStep(Scanner scanner, String prompt, Double defaultVal) {
        while (true) {
            String p = prompt + (defaultVal != null ? " [" + defaultVal + "]" : "") + " : ";
            System.out.print(p);
            String input = UIUtils.safeReadLine(scanner);
            if (input.isEmpty() && defaultVal != null) {
                return StepResult.success(String.valueOf(defaultVal));
            }
            StepResult cmd = checkControlCommands(input);
            if (cmd != null) return cmd;

            try {
                double val = Double.parseDouble(input);
                if (val >= 0) {
                    return StepResult.success(String.valueOf(val));
                }
            } catch (NumberFormatException e) {
                // fall through
            }
            System.out.println("  [!] ERROR: Please enter a valid positive number.");
        }
    }
}

