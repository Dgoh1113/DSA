// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package control;

import adt.DoublyLinkedList;
import adt.SortAlgorithms;
import entity.CustomerReferral;
import entity.Guest;
import entity.Partner;

public class PartnerController {
   private DoublyLinkedList<Partner> partnerRegistry;
   private DoublyLinkedList<CustomerReferral> referralLog;
   private DoublyLinkedList<Guest> guestRegistry;
   private UndoController undoController;

   public PartnerController(DoublyLinkedList<Partner> var1, DoublyLinkedList<CustomerReferral> var2, DoublyLinkedList<Guest> var3) {
      this.partnerRegistry = var1;
      this.referralLog = var2;
      this.guestRegistry = var3;
   }

   public void setUndoController(UndoController var1) {
      this.undoController = var1;
   }

   public Partner registerPartner(String var1, String var2, String var3, String var4, String var5, String var6) {
      Partner var7 = new Partner(var1, var2.toUpperCase(), var3, var4, var5, var6);
      this.partnerRegistry.add(var7);
      if (this.undoController != null) {
         this.undoController.recordAction("REGISTER_PARTNER", "Module 5: Strategic Partners", "Registered Strategic Partner: " + var1 + " (" + var2 + ")", () -> {
            for(int var2 = 1; var2 <= this.partnerRegistry.getNumberOfEntries(); ++var2) {
               if (((Partner)this.partnerRegistry.getEntry(var2)).equals(var7)) {
                  this.partnerRegistry.remove(var2);
                  break;
               }
            }

         });
      }

      return var7;
   }

   public Partner findPartnerById(String var1) {
      for(int var2 = 1; var2 <= this.partnerRegistry.getNumberOfEntries(); ++var2) {
         Partner var3 = (Partner)this.partnerRegistry.getEntry(var2);
         if (var3 != null && var3.getPartnerId().equalsIgnoreCase(var1)) {
            return var3;
         }
      }

      return null;
   }

   public DoublyLinkedList<Partner> getAllPartners() {
      return this.partnerRegistry;
   }

   public DoublyLinkedList<Partner> getPartnersByCategory(String var1) {
      DoublyLinkedList var2 = new DoublyLinkedList();

      for(int var3 = 1; var3 <= this.partnerRegistry.getNumberOfEntries(); ++var3) {
         Partner var4 = (Partner)this.partnerRegistry.getEntry(var3);
         if (var4 != null && var4.getPartnerCategory().equalsIgnoreCase(var1)) {
            var2.add(var4);
         }
      }

      return var2;
   }

   public CustomerReferral recordReferral(String var1, String var2, String var3, String var4, String var5, double var6, String var8) {
      Partner var9 = this.findPartnerById(var1);
      if (var9 == null) {
         return null;
      } else {
         if (var2 != null && !var2.trim().isEmpty() && (var3 == null || var3.trim().isEmpty())) {
            Guest var10 = this.findGuestById(var2);
            if (var10 != null) {
               var3 = var10.getName();
            }
         }

         CustomerReferral var11 = new CustomerReferral(var1, var2, var3, var4.toUpperCase(), var5, var6, var8);
         this.referralLog.add(var11);
         var9.incrementReferrals(var6);
         if (this.undoController != null) {
            this.undoController.recordAction("RECORD_REFERRAL", "Module 5: Strategic Partners", "Logged Referral for " + var9.getCompanyName() + ": " + var5 + " ($" + String.format("%.2f", var6) + ")", () -> {
               for(int var5 = 1; var5 <= this.referralLog.getNumberOfEntries(); ++var5) {
                  if (((CustomerReferral)this.referralLog.getEntry(var5)).equals(var11)) {
                     this.referralLog.remove(var5);
                     break;
                  }
               }

               var9.setTotalReferralsCount(var9.getTotalReferralsCount() - 1);
               var9.setTotalRevenueGenerated(var9.getTotalRevenueGenerated() - var6);
            });
         }

         return var11;
      }
   }

   public DoublyLinkedList<CustomerReferral> getAllReferrals() {
      return this.referralLog;
   }

   public DoublyLinkedList<CustomerReferral> getReferralsByPartner(String var1) {
      DoublyLinkedList var2 = new DoublyLinkedList();

      for(int var3 = 1; var3 <= this.referralLog.getNumberOfEntries(); ++var3) {
         CustomerReferral var4 = (CustomerReferral)this.referralLog.getEntry(var3);
         if (var4 != null && var4.getPartnerId().equalsIgnoreCase(var1)) {
            var2.add(var4);
         }
      }

      return var2;
   }

   public DoublyLinkedList<Partner> getRecommendedPartnersForStage(String var1) {
      DoublyLinkedList var2 = new DoublyLinkedList();
      String var3 = var1.toUpperCase().trim();

      for(int var4 = 1; var4 <= this.partnerRegistry.getNumberOfEntries(); ++var4) {
         Partner var5 = (Partner)this.partnerRegistry.getEntry(var4);
         if (var5 != null) {
            String var6 = var5.getPartnerCategory();
            if ("PURCHASING".equals(var3)) {
               if ("PROPERTY_DEVELOPER".equals(var6)) {
                  var2.add(var5);
               }
            } else if ("RENOVATING".equals(var3)) {
               if ("RENOVATION_CONTRACTOR".equals(var6) || "ELECTRICAL_CONTRACTOR".equals(var6)) {
                  var2.add(var5);
               }
            } else if ("UPGRADING".equals(var3) && ("INTERIOR_DESIGN_FIRM".equals(var6) || "RENOVATION_CONTRACTOR".equals(var6))) {
               var2.add(var5);
            }
         }
      }

      return var2;
   }

   public DoublyLinkedList<Partner> getTopPartnersReportByReferrals() {
      return SortAlgorithms.mergeSort(this.partnerRegistry, (var0, var1) -> Integer.compare(var1.getTotalReferralsCount(), var0.getTotalReferralsCount()));
   }

   public DoublyLinkedList<Partner> getTopPartnersReportByRevenue() {
      return SortAlgorithms.quickSort(this.partnerRegistry, (var0, var1) -> Double.compare(var1.getTotalRevenueGenerated(), var0.getTotalRevenueGenerated()));
   }

   private Guest findGuestById(String var1) {
      for(int var2 = 1; var2 <= this.guestRegistry.getNumberOfEntries(); ++var2) {
         Guest var3 = (Guest)this.guestRegistry.getEntry(var2);
         if (var3 != null && var3.getGuestId().equalsIgnoreCase(var1)) {
            return var3;
         }
      }

      return null;
   }
}
