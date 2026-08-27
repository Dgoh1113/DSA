// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package utility;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class ValidationUtils {
   private static final String NAME_REGEX = "^[a-zA-Z\\s'.\\-\\/]{2,50}$";
   private static final String IC_PASSPORT_REGEX = "^(\\d{6}[-\\s]?\\d{2}[-\\s]?\\d{4}|\\d{12}|[A-Za-z0-9]{6,14})$";
   private static final String PHONE_REGEX = "^(\\+?6?0\\d{1,2}[-\\s]?\\d{7,8}|\\+?\\d{8,15})$";
   private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

   public ValidationUtils() {
   }

   public static String getValidName(Scanner var0, String var1) {
      while(true) {
         System.out.print(var1);
         String var2 = UIUtils.safeReadLine(var0);
         if (isValidName(var2)) {
            return var2;
         }

         System.out.println("  [!] ERROR: Invalid name. Must contain only letters and spaces (min 2 characters).");
      }
   }

   public static boolean isValidName(String var0) {
      return var0 != null && var0.matches("^[a-zA-Z\\s'.\\-\\/]{2,50}$");
   }

   public static String getValidIcPassport(Scanner var0, String var1) {
      while(true) {
         System.out.print(var1);
         String var2 = UIUtils.safeReadLine(var0);
         if (isValidIcPassport(var2)) {
            return var2;
         }

         System.out.println("  [!] ERROR: Invalid IC/Passport. Enter 12-digit IC (e.g. 980101-14-1234) or valid Passport.");
      }
   }

   public static boolean isValidIcPassport(String var0) {
      return var0 != null && var0.matches("^(\\d{6}[-\\s]?\\d{2}[-\\s]?\\d{4}|\\d{12}|[A-Za-z0-9]{6,14})$");
   }

   public static String getValidContactNo(Scanner var0, String var1) {
      while(true) {
         System.out.print(var1);
         String var2 = UIUtils.safeReadLine(var0);
         if (isValidContactNo(var2)) {
            return var2;
         }

         System.out.println("  [!] ERROR: Invalid phone number. Enter a valid contact number (e.g. 012-3456789).");
      }
   }

   public static boolean isValidContactNo(String var0) {
      return var0 != null && var0.matches("^(\\+?6?0\\d{1,2}[-\\s]?\\d{7,8}|\\+?\\d{8,15})$");
   }

   public static String getValidEmail(Scanner var0, String var1) {
      while(true) {
         System.out.print(var1);
         String var2 = UIUtils.safeReadLine(var0);
         if (isValidEmail(var2)) {
            return var2;
         }

         System.out.println("  [!] ERROR: Invalid email address. Example: name@example.com");
      }
   }

   public static boolean isValidEmail(String var0) {
      return var0 != null && var0.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
   }

   public static String getValidRoomType(Scanner var0, String var1) {
      while(true) {
         System.out.print(var1);
         String var2 = UIUtils.safeReadLine(var0).toUpperCase();
         if (var2.equals("STANDARD") || var2.equals("DELUXE") || var2.equals("SUITE")) {
            return var2;
         }

         System.out.println("  [!] ERROR: Invalid room type. Choose from: STANDARD | DELUXE | SUITE");
      }
   }

   public static String getValidLoyaltyTier(Scanner var0, String var1) {
      while(true) {
         System.out.print(var1);
         String var2 = UIUtils.safeReadLine(var0).toUpperCase();
         if (var2.equals("STANDARD") || var2.equals("SILVER") || var2.equals("GOLD") || var2.equals("PLATINUM") || var2.equals("DIAMOND")) {
            return var2;
         }

         System.out.println("  [!] ERROR: Invalid tier. Choose from: STANDARD | SILVER | GOLD | PLATINUM | DIAMOND");
      }
   }

   public static String getValidDate(Scanner var0, String var1) {
      while(true) {
         System.out.print(var1);
         String var2 = UIUtils.safeReadLine(var0);
         if (isValidDate(var2)) {
            return var2;
         }

         System.out.println("  [!] ERROR: Invalid date format or date. Please enter in YYYY-MM-DD format (e.g. 2026-08-20).");
      }
   }

   public static boolean isValidDate(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         try {
            LocalDate.parse(var0);
            return true;
         } catch (DateTimeParseException var2) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static String getValidCheckOutDate(Scanner var0, String var1, String var2) {
      LocalDate var3 = LocalDate.parse(var2);

      while(true) {
         String var4 = getValidDate(var0, var1);
         LocalDate var5 = LocalDate.parse(var4);
         if (var5.isAfter(var3)) {
            return var4;
         }

         System.out.println("  [!] ERROR: Check-out date must be AFTER check-in date (" + var2 + ").");
      }
   }

   public static int getValidInt(Scanner var0, String var1) {
      while(true) {
         System.out.print(var1);
         if (var0.hasNextInt()) {
            int var2 = var0.nextInt();
            var0.nextLine();
            return var2;
         }

         System.out.println("  [!] ERROR: Invalid input. Please enter a valid whole number.");
         var0.next();
      }
   }

   public static double getValidPositiveDouble(Scanner var0, String var1) {
      while(true) {
         System.out.print(var1);
         if (var0.hasNextDouble()) {
            double var2 = var0.nextDouble();
            var0.nextLine();
            if (var2 >= (double)0.0F) {
               return var2;
            }
         } else {
            var0.next();
         }

         System.out.println("  [!] ERROR: Invalid input. Please enter a positive number.");
      }
   }

   public static StepResult checkControlCommands(String var0) {
      if (var0 == null) {
         return null;
      } else {
         String var1 = var0.trim();
         if (!var1.equalsIgnoreCase("b") && !var1.equalsIgnoreCase("back")) {
            if (!var1.equals("0") && !var1.equalsIgnoreCase("main")) {
               return !var1.equalsIgnoreCase("cancel") && !var1.equalsIgnoreCase("exit") ? null : StepResult.cancel();
            } else {
               return StepResult.quitToMain();
            }
         } else {
            return StepResult.goBack();
         }
      }
   }

   public static StepResult readValidNameStep(Scanner var0, String var1, String var2) {
      while(true) {
         String var3 = var1 + (var2 != null && !var2.isEmpty() ? " [" + var2 + "]" : "") + " : ";
         System.out.print(var3);
         String var4 = UIUtils.safeReadLine(var0);
         if (var4.isEmpty() && var2 != null && !var2.isEmpty()) {
            return StepResult.success(var2);
         }

         StepResult var5 = checkControlCommands(var4);
         if (var5 != null) {
            return var5;
         }

         if (isValidName(var4)) {
            return StepResult.success(var4);
         }

         System.out.println("  [!] ERROR: Invalid name. Must contain only letters and spaces (min 2 characters).");
      }
   }

   public static StepResult readValidIcPassportStep(Scanner var0, String var1, String var2) {
      while(true) {
         String var3 = var1 + (var2 != null && !var2.isEmpty() ? " [" + var2 + "]" : "") + " : ";
         System.out.print(var3);
         String var4 = UIUtils.safeReadLine(var0);
         if (var4.isEmpty() && var2 != null && !var2.isEmpty()) {
            return StepResult.success(var2);
         }

         StepResult var5 = checkControlCommands(var4);
         if (var5 != null) {
            return var5;
         }

         if (isValidIcPassport(var4)) {
            return StepResult.success(var4);
         }

         System.out.println("  [!] ERROR: Invalid IC/Passport. Enter 12-digit IC (e.g. 980101-14-1234) or valid Passport.");
      }
   }

   public static StepResult readValidContactNoStep(Scanner var0, String var1, String var2) {
      while(true) {
         String var3 = var1 + (var2 != null && !var2.isEmpty() ? " [" + var2 + "]" : "") + " : ";
         System.out.print(var3);
         String var4 = UIUtils.safeReadLine(var0);
         if (var4.isEmpty() && var2 != null && !var2.isEmpty()) {
            return StepResult.success(var2);
         }

         StepResult var5 = checkControlCommands(var4);
         if (var5 != null) {
            return var5;
         }

         if (isValidContactNo(var4)) {
            return StepResult.success(var4);
         }

         System.out.println("  [!] ERROR: Invalid phone number. Enter a valid contact number (e.g. 012-3456789).");
      }
   }

   public static StepResult readValidEmailStep(Scanner var0, String var1, String var2) {
      while(true) {
         String var3 = var1 + (var2 != null && !var2.isEmpty() ? " [" + var2 + "]" : "") + " : ";
         System.out.print(var3);
         String var4 = UIUtils.safeReadLine(var0);
         if (var4.isEmpty() && var2 != null && !var2.isEmpty()) {
            return StepResult.success(var2);
         }

         StepResult var5 = checkControlCommands(var4);
         if (var5 != null) {
            return var5;
         }

         if (isValidEmail(var4)) {
            return StepResult.success(var4);
         }

         System.out.println("  [!] ERROR: Invalid email address. Example: name@example.com");
      }
   }

   public static StepResult readValidRoomTypeStep(Scanner var0, String var1, String var2) {
      while(true) {
         String var3 = var1 + (var2 != null && !var2.isEmpty() ? " [" + var2 + "]" : "") + " : ";
         System.out.print(var3);
         String var4 = UIUtils.safeReadLine(var0).toUpperCase();
         if (var4.isEmpty() && var2 != null && !var2.isEmpty()) {
            return StepResult.success(var2);
         }

         StepResult var5 = checkControlCommands(var4);
         if (var5 != null) {
            return var5;
         }

         if (var4.equals("STANDARD") || var4.equals("DELUXE") || var4.equals("SUITE")) {
            return StepResult.success(var4);
         }

         System.out.println("  [!] ERROR: Invalid room type. Choose from: STANDARD | DELUXE | SUITE");
      }
   }

   public static StepResult readValidLoyaltyTierStep(Scanner var0, String var1, String var2) {
      while(true) {
         String var3 = var1 + (var2 != null && !var2.isEmpty() ? " [" + var2 + "]" : "") + " : ";
         System.out.print(var3);
         String var4 = UIUtils.safeReadLine(var0).toUpperCase();
         if (var4.isEmpty() && var2 != null && !var2.isEmpty()) {
            return StepResult.success(var2);
         }

         StepResult var5 = checkControlCommands(var4);
         if (var5 != null) {
            return var5;
         }

         if (var4.equals("STANDARD") || var4.equals("SILVER") || var4.equals("GOLD") || var4.equals("PLATINUM") || var4.equals("DIAMOND")) {
            return StepResult.success(var4);
         }

         System.out.println("  [!] ERROR: Invalid tier. Choose from: STANDARD | SILVER | GOLD | PLATINUM | DIAMOND");
      }
   }

   public static StepResult readValidDateStep(Scanner var0, String var1, String var2) {
      while(true) {
         String var3 = var1 + (var2 != null && !var2.isEmpty() ? " [" + var2 + "]" : "") + " : ";
         System.out.print(var3);
         String var4 = UIUtils.safeReadLine(var0);
         if (var4.isEmpty() && var2 != null && !var2.isEmpty()) {
            return StepResult.success(var2);
         }

         StepResult var5 = checkControlCommands(var4);
         if (var5 != null) {
            return var5;
         }

         if (isValidDate(var4)) {
            return StepResult.success(var4);
         }

         System.out.println("  [!] ERROR: Invalid date format. Enter in YYYY-MM-DD format (e.g. 2026-08-20).");
      }
   }

   public static StepResult readValidCheckOutDateStep(Scanner var0, String var1, String var2, String var3) {
      if (!isValidDate(var2)) {
         return StepResult.goBack();
      } else {
         LocalDate var4 = LocalDate.parse(var2);

         while(true) {
            StepResult var5 = readValidDateStep(var0, var1, var3);
            if (!var5.isSuccess()) {
               return var5;
            }

            LocalDate var6 = LocalDate.parse(var5.getValue());
            if (var6.isAfter(var4)) {
               return var5;
            }

            System.out.println("  [!] ERROR: Check-out date must be strictly AFTER check-in date (" + var2 + ").");
         }
      }
   }

   public static StepResult readValidStringStep(Scanner var0, String var1, String var2, boolean var3) {
      while(true) {
         String var4 = var1 + (var2 != null && !var2.isEmpty() ? " [" + var2 + "]" : "") + " : ";
         System.out.print(var4);
         String var5 = UIUtils.safeReadLine(var0);
         if (var5.isEmpty() && var2 != null && !var2.isEmpty()) {
            return StepResult.success(var2);
         }

         StepResult var6 = checkControlCommands(var5);
         if (var6 != null) {
            return var6;
         }

         if (!var5.isEmpty() || var3) {
            return StepResult.success(var5);
         }

         System.out.println("  [!] ERROR: Field cannot be empty.");
      }
   }

   public static StepResult readValidPositiveDoubleStep(Scanner var0, String var1, Double var2) {
      while(true) {
         String var3 = var1 + (var2 != null ? " [" + var2 + "]" : "") + " : ";
         System.out.print(var3);
         String var4 = UIUtils.safeReadLine(var0);
         if (var4.isEmpty() && var2 != null) {
            return StepResult.success(String.valueOf(var2));
         }

         StepResult var5 = checkControlCommands(var4);
         if (var5 != null) {
            return var5;
         }

         try {
            double var6 = Double.parseDouble(var4);
            if (var6 >= (double)0.0F) {
               return StepResult.success(String.valueOf(var6));
            }
         } catch (NumberFormatException var8) {
         }

         System.out.println("  [!] ERROR: Please enter a valid positive number.");
      }
   }
}
