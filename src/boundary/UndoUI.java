// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package boundary;

import adt.DoublyLinkedList;
import control.UndoController;
import entity.UndoAction;
import java.util.Scanner;
import utility.UIUtils;

public class UndoUI {
   private UndoController controller;
   private Scanner scanner;

   public UndoUI(UndoController var1, Scanner var2) {
      this.controller = var1;
      this.scanner = var2;
   }

   public void show() {
      boolean var1 = false;

      do {
         UIUtils.clearScreen();
         this.printUndoHeader();
         this.displayMenu();
         int var2 = UIUtils.safeReadInt(this.scanner);
         switch (var2) {
            case 0:
               var1 = true;
               break;
            case 1:
               this.processNextUndo();
               var1 = UIUtils.promptPostOperationNavigation(this.scanner);
               break;
            case 2:
               this.peekNextUndo();
               var1 = UIUtils.promptPostOperationNavigation(this.scanner);
               break;
            case 3:
               this.viewUndoQueue();
               var1 = UIUtils.promptPostOperationNavigation(this.scanner);
               break;
            case 4:
               this.clearQueue();
               var1 = UIUtils.promptPostOperationNavigation(this.scanner);
               break;
            default:
               System.out.println("\u001b[91mInvalid option. Please try again.\u001b[0m");
               UIUtils.pressEnterToContinue(this.scanner);
         }
      } while(!var1);

   }

   private void printUndoHeader() {
      System.out.println("\u001b[95m\u001b[1m=====================================================================\u001b[0m");
      System.out.println("\u001b[95m\u001b[1m    CENTRAL TRANSACTION UNDO UTILITY  [ STACK ADT - LIFO ]\u001b[0m");
      System.out.println("\u001b[97m    Cross-Module System Utility — Reverses Most Recent Action First\u001b[0m");
      System.out.println("\u001b[95m\u001b[1m=====================================================================\u001b[0m");
      System.out.println("  \u001b[93mPending Reversible Actions on Stack: " + this.controller.getQueueSize() + "\u001b[0m");
      System.out.println();
   }

   private void displayMenu() {
      UIUtils.printSectionHeader("STACK UNDO REVERSAL CONTROLS", "\u001b[95m");
      System.out.println("  \u001b[95m\u001b[1m1.\u001b[0m Process Most Recent Undo (Pop & Execute Reversal)");
      System.out.println("  \u001b[95m\u001b[1m2.\u001b[0m Peek Top Action on Undo Stack");
      UIUtils.printSectionHeader("STACK AUDIT & MANAGEMENT", "\u001b[95m");
      System.out.println("  \u001b[95m\u001b[1m3.\u001b[0m View Entire Undo Stack");
      System.out.println("  \u001b[95m\u001b[1m4.\u001b[0m Clear Undo Stack History");
      UIUtils.printSectionHeader("NAVIGATION", "\u001b[91m");
      System.out.println("  \u001b[91m\u001b[1m0.\u001b[0m Back to Main Menu");
      System.out.println("──────────────────────────────────────────────────────────");
      System.out.print("\u001b[1mEnter your choice: \u001b[0m");
   }

   private void processNextUndo() {
      UIUtils.printSubHeader("SYSTEM UTILITY > PROCESS MOST RECENT UNDO (POP)", "\u001b[95m");
      if (this.controller.isQueueEmpty()) {
         System.out.println("\u001b[93mThe Undo Stack is currently EMPTY. No actions available to undo.\u001b[0m");
      } else {
         UndoAction var1 = this.controller.peekNextUndo();
         System.out.println("Most recent action to reverse (Top of Stack):");
         System.out.println("  ID          : " + var1.getActionId());
         System.out.println("  Module      : " + var1.getModuleName());
         System.out.println("  Description : " + var1.getDescription());
         System.out.println("  Logged At   : " + var1.getTimestamp());
         System.out.print("\nConfirm execution of undo reversal? (Y/N): ");
         String var2 = UIUtils.safeReadLine(this.scanner).toUpperCase();
         if ("Y".equals(var2)) {
            UndoAction var3 = this.controller.processNextUndo();
            if (var3 != null) {
               System.out.println("\n\u001b[92m\u001b[1m+------------------------------------------+\u001b[0m");
               System.out.println("\u001b[92m\u001b[1m  TRANSACTION UNDONE SUCCESSFULLY!\u001b[0m");
               System.out.println("\u001b[92m\u001b[1m+------------------------------------------+\u001b[0m");
               System.out.println("  Action ID   : " + var3.getActionId());
               System.out.println("  Module      : " + var3.getModuleName());
               System.out.println("  Reverted    : " + var3.getDescription());
               System.out.println("  Remaining on Stack: " + this.controller.getQueueSize());
               System.out.println("+------------------------------------------+");
            } else {
               System.out.println("\u001b[91mFailed to execute undo.\u001b[0m");
            }
         } else {
            System.out.println("Undo operation cancelled.");
         }

      }
   }

   private void peekNextUndo() {
      UIUtils.printSubHeader("SYSTEM UTILITY > PEEK TOP STACK UNDO", "\u001b[95m");
      UndoAction var1 = this.controller.peekNextUndo();
      if (var1 == null) {
         System.out.println("\u001b[93mThe Undo Stack is currently EMPTY.\u001b[0m");
      } else {
         System.out.println("  Top Action on Stack:");
         System.out.println("  Action ID   : " + var1.getActionId());
         System.out.println("  Module      : " + var1.getModuleName());
         System.out.println("  Description : " + var1.getDescription());
         System.out.println("  Timestamp   : " + var1.getTimestamp());
      }

   }

   private void viewUndoQueue() {
      UIUtils.printSubHeader("SYSTEM UTILITY > VIEW UNDO STACK HISTORY", "\u001b[95m");
      System.out.println("Stack Size: " + this.controller.getQueueSize());
      if (this.controller.isQueueEmpty()) {
         System.out.println("\u001b[93mThe Undo Stack is empty.\u001b[0m");
      } else {
         DoublyLinkedList var1 = this.controller.getUndoQueueList();
         System.out.println("+-----+----------+----------+-----------------------------+----------------------------------------------+");
         System.out.println("| Pos | ID       | Time     | Originating Module          | Description                                  |");
         System.out.println("+-----+----------+----------+-----------------------------+----------------------------------------------+");

         for(int var2 = 1; var2 <= var1.getNumberOfEntries(); ++var2) {
            UndoAction var3 = (UndoAction)var1.getEntry(var2);
            System.out.printf("| %-3d | %-8s | %-8s | %-27s | %-44s |%n", var2, var3.getActionId(), var3.getTimestamp(), this.truncate(var3.getModuleName(), 27), this.truncate(var3.getDescription(), 44));
         }

         System.out.println("+-----+----------+----------+-----------------------------+----------------------------------------------+");
      }
   }

   private void clearQueue() {
      UIUtils.printSubHeader("SYSTEM UTILITY > CLEAR UNDO STACK", "\u001b[95m");
      if (this.controller.isQueueEmpty()) {
         System.out.println("The stack is already empty.");
      } else {
         System.out.print("Are you sure you want to clear all " + this.controller.getQueueSize() + " pending undo actions on the stack? (Y/N): ");
         String var1 = UIUtils.safeReadLine(this.scanner).toUpperCase();
         if ("Y".equals(var1)) {
            this.controller.clearUndoQueue();
            System.out.println("\u001b[92mUndo Stack cleared successfully.\u001b[0m");
         } else {
            System.out.println("Action cancelled.");
         }

      }
   }

   private String truncate(String var1, int var2) {
      if (var1 == null) {
         return "";
      } else if (var1.length() <= var2) {
         return var1;
      } else {
         String var10000 = var1.substring(0, var2 - 3);
         return var10000 + "...";
      }
   }
}
