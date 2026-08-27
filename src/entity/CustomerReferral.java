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
        this.referralId = generateId();
        this.status = "COMPLETED";
    }

    public CustomerReferral(String partnerId, String guestId, String customerName,
                            String customerStage, String productIntroduced,
                            double dealAmount, String referralDate) {
        this.referralId = generateId();
        this.partnerId = partnerId;
        this.guestId = guestId;
        this.customerName = customerName;
        this.customerStage = customerStage;
        this.productIntroduced = productIntroduced;
        this.dealAmount = dealAmount;
        this.referralDate = referralDate;
        this.status = "COMPLETED";
    }

    private static String generateId() {
        return "REF" + (idCounter++);
    }

    public static void updateIdCounter(int nextVal) {
        if (nextVal > idCounter) {
            idCounter = nextVal;
        }
    }

    // --- Getters & Setters ---

    public String getReferralId() {
        return referralId;
    }

    public void setReferralId(String referralId) {
        this.referralId = referralId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerStage() {
        return customerStage;
    }

    public void setCustomerStage(String customerStage) {
        this.customerStage = customerStage;
    }

    public String getProductIntroduced() {
        return productIntroduced;
    }

    public void setProductIntroduced(String productIntroduced) {
        this.productIntroduced = productIntroduced;
    }

    public double getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(double dealAmount) {
        this.dealAmount = dealAmount;
    }

    public String getReferralDate() {
        return referralDate;
    }

    public void setReferralDate(String referralDate) {
        this.referralDate = referralDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CustomerReferral{id='" + referralId + "', partnerId='" + partnerId
                + "', customer='" + customerName + "', stage='" + customerStage
                + "', product='" + productIntroduced + "', dealAmount=" + dealAmount + "}";
    }
}
