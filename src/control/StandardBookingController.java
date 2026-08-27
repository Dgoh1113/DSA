// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package control;

import adt.BinarySearchTree;
import adt.DoublyLinkedList;
import adt.LinkedQueue;
import entity.Guest;
import entity.LoyaltyAccount;
import entity.Reservation;
import entity.Room;

public class StandardBookingController {
   private LinkedQueue<Reservation> standardQueue;
   private DoublyLinkedList<Guest> guestRegistry;
   private DoublyLinkedList<Room> roomInventory;
   private BinarySearchTree<Reservation> searchTree;
   private UndoController undoController;
   private LoyaltyController loyaltyController;
   private static int arrivalCounter = 0;

   public StandardBookingController(LinkedQueue<Reservation> var1, DoublyLinkedList<Guest> var2, DoublyLinkedList<Room> var3, BinarySearchTree<Reservation> var4) {
      this.standardQueue = var1;
      this.guestRegistry = var2;
      this.roomInventory = var3;
      this.searchTree = var4;
   }

   public void setUndoController(UndoController var1) {
      this.undoController = var1;
   }

   public void setLoyaltyController(LoyaltyController var1) {
      this.loyaltyController = var1;
   }

   public Reservation registerWalkIn(String var1, String var2, String var3, String var4, String var5, String var6, String var7) {
      Guest var8 = this.findGuestByContactNo(var3);
      if (var8 == null) {
         var8 = new Guest(var1, var2, var3, var4);
         this.guestRegistry.add(var8);
      }

      Reservation var9 = new Reservation(var8.getGuestId(), var5, var6, var7);
      var9.setGuest(var8);
      var9.setBookingStatus("PENDING");
      this.standardQueue.enqueue(var9);
      this.searchTree.insert(var9);
      if (this.undoController != null) {
         this.undoController.recordAction("WALK_IN_REGISTRATION", "Module 1: Standard Booking", "Register Walk-In: " + var1 + " (Conf #" + var9.getConfirmationNo() + ")", () -> {
            var9.setBookingStatus("CANCELLED");
            this.searchTree.delete(var9);
         });
      }

      return var9;
   }

   public Reservation processNextBooking() {
      if (this.standardQueue.isEmpty()) {
         return null;
      } else {
         Reservation var1 = this.dequeueNextActiveReservation();
         if (var1 == null) {
            return null;
         } else {
            Room var2 = this.findAvailableRoom(var1.getRoomType());
            if (var2 != null) {
               var2.setStatus("OCCUPIED");
               var1.setAssignedRoomNo(var2.getRoomNo());
               var1.setBookingStatus("CONFIRMED");
               this.searchTree.delete(var1);
               this.searchTree.insert(var1);
               if (this.undoController != null) {
                  this.undoController.recordAction("PROCESS_BOOKING", "Module 1: Standard Booking", "Assigned Room " + var2.getRoomNo() + " to Conf #" + var1.getConfirmationNo(), () -> {
                     var2.setStatus("AVAILABLE");
                     var1.setAssignedRoomNo((String)null);
                     var1.setBookingStatus("PENDING");
                     this.standardQueue.enqueue(var1);
                     this.searchTree.delete(var1);
                     this.searchTree.insert(var1);
                  });
               }
            } else {
               var1.setBookingStatus("PENDING");
               this.standardQueue.enqueue(var1);
            }

            return var1;
         }
      }
   }

   public Guest findGuestByContactNo(String var1) {
      String var2 = this.normalizeContactNo(var1);
      if (var2.isEmpty()) {
         return null;
      } else {
         for(int var3 = 1; var3 <= this.guestRegistry.getNumberOfEntries(); ++var3) {
            Guest var4 = (Guest)this.guestRegistry.getEntry(var3);
            if (var4 != null && var2.equals(this.normalizeContactNo(var4.getContactNo()))) {
               return var4;
            }
         }

         return null;
      }
   }

   public LoyaltyAccount findLoyaltyAccountByContactNo(String var1) {
      Guest var2 = this.findGuestByContactNo(var1);
      return var2 != null && this.loyaltyController != null ? this.loyaltyController.viewMemberProfile(var2.getGuestId()) : null;
   }

   public Guest registerNewMember(String var1, String var2, String var3, String var4) {
      Guest var5 = this.findGuestByContactNo(var3);
      if (var5 != null) {
         return var5;
      } else {
         Guest var6 = new Guest(var1, var2, var3, var4);
         this.guestRegistry.add(var6);
         if (this.loyaltyController != null) {
            this.loyaltyController.viewMemberProfile(var6.getGuestId());
         }

         return var6;
      }
   }

   private String normalizeContactNo(String var1) {
      if (var1 == null) {
         return "";
      } else {
         String var2 = var1.replaceAll("[^0-9]", "");
         if (var2.startsWith("0060")) {
            var2 = var2.substring(2);
         }

         if (var2.startsWith("60")) {
            var2 = "0" + var2.substring(2);
         }

         return var2;
      }
   }

   private Reservation dequeueNextActiveReservation() {
      Reservation var1;
      for(var1 = (Reservation)this.standardQueue.dequeue(); var1 != null && !"PENDING".equals(var1.getBookingStatus()); var1 = (Reservation)this.standardQueue.dequeue()) {
      }

      return var1;
   }

   public Reservation peekNextBooking() {
      DoublyLinkedList var1 = this.standardQueue.toList();

      for(int var2 = 1; var2 <= var1.getNumberOfEntries(); ++var2) {
         Reservation var3 = (Reservation)var1.getEntry(var2);
         if (var3 != null && "PENDING".equals(var3.getBookingStatus())) {
            return var3;
         }
      }

      return null;
   }

   public int getQueueSize() {
      return this.getQueueList().getNumberOfEntries();
   }

   public DoublyLinkedList<Reservation> getQueueList() {
      DoublyLinkedList var1 = new DoublyLinkedList();
      DoublyLinkedList var2 = this.standardQueue.toList();

      for(int var3 = 1; var3 <= var2.getNumberOfEntries(); ++var3) {
         Reservation var4 = (Reservation)var2.getEntry(var3);
         if (var4 != null && "PENDING".equals(var4.getBookingStatus())) {
            var1.add(var4);
         }
      }

      return var1;
   }

   public boolean isQueueEmpty() {
      return this.peekNextBooking() == null;
   }

   private Room findAvailableRoom(String var1) {
      for(int var2 = 1; var2 <= this.roomInventory.getNumberOfEntries(); ++var2) {
         Room var3 = (Room)this.roomInventory.getEntry(var2);
         if (var3.getStatus().equals("AVAILABLE") && var3.getRoomType().equals(var1)) {
            return var3;
         }
      }

      for(int var4 = 1; var4 <= this.roomInventory.getNumberOfEntries(); ++var4) {
         Room var5 = (Room)this.roomInventory.getEntry(var4);
         if (var5.getStatus().equals("AVAILABLE")) {
            return var5;
         }
      }

      return null;
   }

   public static int getNextArrivalIndex() {
      return arrivalCounter++;
   }
}
