// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package entity;

public class Partner {
   private static int idCounter = 1000;
   private String partnerId = generateId();
   private String companyName;
   private String partnerCategory;
   private String contactPerson;
   private String contactPhone;
   private String email;
   private String offeredServices;
   private int totalReferralsCount;
   private double totalRevenueGenerated;

   public Partner() {
      this.totalReferralsCount = 0;
      this.totalRevenueGenerated = (double)0.0F;
   }

   public Partner(String var1, String var2, String var3, String var4, String var5, String var6) {
      this.companyName = var1;
      this.partnerCategory = var2;
      this.contactPerson = var3;
      this.contactPhone = var4;
      this.email = var5;
      this.offeredServices = var6;
      this.totalReferralsCount = 0;
      this.totalRevenueGenerated = (double)0.0F;
   }

   private static String generateId() {
      int var10000 = idCounter++;
      return "P" + var10000;
   }

   public static void updateIdCounter(int var0) {
      if (var0 > idCounter) {
         idCounter = var0;
      }

   }

   public String getPartnerId() {
      return this.partnerId;
   }

   public void setPartnerId(String var1) {
      this.partnerId = var1;
   }

   public String getCompanyName() {
      return this.companyName;
   }

   public void setCompanyName(String var1) {
      this.companyName = var1;
   }

   public String getPartnerCategory() {
      return this.partnerCategory;
   }

   public void setPartnerCategory(String var1) {
      this.partnerCategory = var1;
   }

   public String getContactPerson() {
      return this.contactPerson;
   }

   public void setContactPerson(String var1) {
      this.contactPerson = var1;
   }

   public String getContactPhone() {
      return this.contactPhone;
   }

   public void setContactPhone(String var1) {
      this.contactPhone = var1;
   }

   public String getEmail() {
      return this.email;
   }

   public void setEmail(String var1) {
      this.email = var1;
   }

   public String getOfferedServices() {
      return this.offeredServices;
   }

   public void setOfferedServices(String var1) {
      this.offeredServices = var1;
   }

   public int getTotalReferralsCount() {
      return this.totalReferralsCount;
   }

   public void setTotalReferralsCount(int var1) {
      this.totalReferralsCount = var1;
   }

   public double getTotalRevenueGenerated() {
      return this.totalRevenueGenerated;
   }

   public void setTotalRevenueGenerated(double var1) {
      this.totalRevenueGenerated = var1;
   }

   public void incrementReferrals(double var1) {
      ++this.totalReferralsCount;
      this.totalRevenueGenerated += var1;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 != null && this.getClass() == var1.getClass()) {
         Partner var2 = (Partner)var1;
         return this.partnerId != null && this.partnerId.equals(var2.partnerId);
      } else {
         return false;
      }
   }

   public String toString() {
      return "Partner{id='" + this.partnerId + "', name='" + this.companyName + "', category='" + this.partnerCategory + "', referrals=" + this.totalReferralsCount + ", revenue=" + this.totalRevenueGenerated + "}";
   }
}
