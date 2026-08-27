// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package utility;

import java.util.Scanner;

public class UIUtils {
   public static final String RESET = "\u001b[0m";
   public static final String BOLD = "\u001b[1m";
   public static final String RED = "\u001b[91m";
   public static final String GREEN = "\u001b[92m";
   public static final String YELLOW = "\u001b[93m";
   public static final String BLUE = "\u001b[94m";
   public static final String MAGENTA = "\u001b[95m";
   public static final String PURPLE = "\u001b[95m";
   public static final String CYAN = "\u001b[96m";
   public static final String WHITE = "\u001b[97m";

   public UIUtils() {
   }

   public static void clearScreen() {
      try {
         System.out.print("\u001b[H\u001b[2J");
         System.out.flush();
         String var0 = System.getProperty("os.name");
         if (var0 != null && var0.contains("Windows")) {
            (new ProcessBuilder(new String[]{"cmd", "/c", "cls"})).inheritIO().start().waitFor();
         }
      } catch (Exception var2) {
         for(int var1 = 0; var1 < 50; ++var1) {
            System.out.println();
         }
      }

   }

   public static String safeReadLine(Scanner var0) {
      if (var0 != null && var0.hasNextLine()) {
         return var0.nextLine().trim();
      } else {
         return var0 != null && !var0.hasNextLine() ? "cancel" : "";
      }
   }

   public static int safeReadInt(Scanner var0) {
      while(var0 != null && var0.hasNext()) {
         if (var0.hasNextInt()) {
            int var1 = var0.nextInt();
            if (var0.hasNextLine()) {
               var0.nextLine();
            }

            return var1;
         }

         System.out.print("\u001b[91mPlease enter a valid whole number: \u001b[0m");
         var0.next();
      }

      return 0;
   }

   public static void pressEnterToContinue(Scanner var0) {
      System.out.println("\n\u001b[93mPress [ENTER] to return...\u001b[0m");
      safeReadLine(var0);
   }

   public static boolean promptPostOperationNavigation(Scanner var0) {
      System.out.println("\n\u001b[96m──────────────────────────────────────────────────────────\u001b[0m");
      System.out.println("  \u001b[1m[1]\u001b[0m Return to Sub-Module Menu");
      System.out.println("  \u001b[1m[0]\u001b[0m Quit to Main Menu Right Away");
      System.out.println("\u001b[96m──────────────────────────────────────────────────────────\u001b[0m");
      System.out.print("\u001b[1mEnter choice (Press [ENTER] for Sub-Module Menu or '0' for Main Menu): \u001b[0m");
      String var1 = safeReadLine(var0).trim();
      return "0".equals(var1);
   }

   public static void printMainTitleHeader() {
      System.out.println("\u001b[93m\u001b[1m╔═════════════════════════════════════════════════════════════════════╗\u001b[0m");
      System.out.println("\u001b[93m\u001b[1m║                \u001b[97mTARUMT RESORTS MANAGEMENT SYSTEM\u001b[93m                     ║\u001b[0m");
      System.out.println("\u001b[93m\u001b[1m║             \u001b[96mHotel Reservation & Strategic Partners System\u001b[93m           ║\u001b[0m");
      System.out.println("\u001b[93m\u001b[1m╚═════════════════════════════════════════════════════════════════════╝\u001b[0m");
   }

   public static void printModule1Header() {
      System.out.println("\u001b[96m\u001b[1m╔═════════════════════════════════════════════════════════════════════╗\u001b[0m");
      System.out.println("\u001b[96m\u001b[1m║     MODULE 1: WALK-IN & STANDARD BOOKING QUEUE [ Queue ADT ]       ║\u001b[0m");
      System.out.println("\u001b[96m\u001b[1m╚═════════════════════════════════════════════════════════════════════╝\u001b[0m");
   }

   public static void printModule2Header() {
      System.out.println("\u001b[93m\u001b[1m╔═════════════════════════════════════════════════════════════════════╗\u001b[0m");
      System.out.println("\u001b[93m\u001b[1m║     MODULE 2: VIP PRIORITY ROOM ALLOCATION     [ Max-Heap ADT ]    ║\u001b[0m");
      System.out.println("\u001b[93m\u001b[1m╚═════════════════════════════════════════════════════════════════════╝\u001b[0m");
   }

   public static void printModule3Header() {
      System.out.println("\u001b[92m\u001b[1m╔═════════════════════════════════════════════════════════════════════╗\u001b[0m");
      System.out.println("\u001b[92m\u001b[1m║     MODULE 3: FRONT-DESK SERVICE & BST LOOKUP  [ BST ADT ]         ║\u001b[0m");
      System.out.println("\u001b[92m\u001b[1m╚═════════════════════════════════════════════════════════════════════╝\u001b[0m");
   }

   public static void printModule4Header() {
      System.out.println("\u001b[95m\u001b[1m╔═════════════════════════════════════════════════════════════════════╗\u001b[0m");
      System.out.println("\u001b[95m\u001b[1m║     MODULE 4: LOYALTY & REWARDS PROGRAM        [ DLL + Sorting ]   ║\u001b[0m");
      System.out.println("\u001b[95m\u001b[1m╚═════════════════════════════════════════════════════════════════════╝\u001b[0m");
   }

   public static void printModule5Header() {
      System.out.println("\u001b[94m\u001b[1m╔═════════════════════════════════════════════════════════════════════╗\u001b[0m");
      System.out.println("\u001b[94m\u001b[1m║     MODULE 5: STRATEGIC PARTNERS & REFERRALS   [ Customer Referrals ] ║\u001b[0m");
      System.out.println("\u001b[94m\u001b[1m╚═════════════════════════════════════════════════════════════════════╝\u001b[0m");
   }

   public static void printSubHeader(String var0, String var1) {
      clearScreen();
      System.out.println(var1 + "\u001b[1m╔═════════════════════════════════════════════════════════════════════╗\u001b[0m");
      System.out.println(var1 + "\u001b[1m  CURRENT SCREEN: \u001b[97m" + var0.toUpperCase() + "\u001b[0m");
      System.out.println(var1 + "\u001b[1m╚═════════════════════════════════════════════════════════════════════╝\n\u001b[0m");
   }

   public static void printSectionHeader(String var0, String var1) {
      System.out.println("\n" + var1 + "\u001b[1m─── [ " + var0.toUpperCase() + " ] ─────────────────────────────────────────\u001b[0m");
   }
}
