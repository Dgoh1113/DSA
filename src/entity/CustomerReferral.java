package entity;

/**
 * Entity: CustomerReferral — Records partner-introduced products/services to customers.
 * Tracks customer stage (PURCHASING, RENOVATING, UPGRADING) and transaction details.
 * Pure data blueprint — no business logic.
 */
public class CustomerReferral {

    private static int idCounter = 1000;

    private String referralId;          // PK, auto-generated (e.g. "REF1001")
    private String partnerId;           // FK referencing Partner
    private String guestId;             // FK referencing Guest (if registered)
    private String customerName;
    private String customerStage;       // PURCHASING, RENOVATING, UPGRADING
    private String productIntroduced;   // Description of product/service recommended
    private double dealAmount;          // Dollar amount of transaction
    private String referralDate;        // Date of referral (e.g. "2026-08-13")
    private String status;              // PENDING, COMPLETED, CANCELLED

    public CustomerReferral() {
      this.status = "COMPLETED";
   }

   public CustomerReferral(String var1, String var2, String var3, String var4, String var5, double var6, String var8) {
      this.partnerId = var1;
      this.guestId = var2;
      this.customerName = var3;
      this.customerStage = var4;
      this.productIntroduced = var5;
      this.dealAmount = var6;
      this.referralDate = var8;
      this.status = "COMPLETED";
   }

   private static String generateId() {
      int var10000 = idCounter++;
      return "REF" + var10000;
   }

   public static void updateIdCounter(int var0) {
      if (var0 > idCounter) {
         idCounter = var0;
      }

   }

   public String getReferralId() {
      return this.referralId;
   }

   public void setReferralId(String var1) {
      this.referralId = var1;
   }

   public String getPartnerId() {
      return this.partnerId;
   }

   public void setPartnerId(String var1) {
      this.partnerId = var1;
   }

   public String getGuestId() {
      return this.guestId;
   }

   public void setGuestId(String var1) {
      this.guestId = var1;
   }

   public String getCustomerName() {
      return this.customerName;
   }

   public void setCustomerName(String var1) {
      this.customerName = var1;
   }

   public String getCustomerStage() {
      return this.customerStage;
   }

   public void setCustomerStage(String var1) {
      this.customerStage = var1;
   }

   public String getProductIntroduced() {
      return this.productIntroduced;
   }

   public void setProductIntroduced(String var1) {
      this.productIntroduced = var1;
   }

   public double getDealAmount() {
      return this.dealAmount;
   }

   public void setDealAmount(double var1) {
      this.dealAmount = var1;
   }

   public String getReferralDate() {
      return this.referralDate;
   }

   public void setReferralDate(String var1) {
      this.referralDate = var1;
   }

   public String getStatus() {
      return this.status;
   }

   public void setStatus(String var1) {
      this.status = var1;
   }

   public String toString() {
      return "CustomerReferral{id='" + this.referralId + "', partnerId='" + this.partnerId + "', customer='" + this.customerName + "', stage='" + this.customerStage + "', product='" + this.productIntroduced + "', dealAmount=" + this.dealAmount + "}";
   }
}
