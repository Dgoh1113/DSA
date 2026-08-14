package entity;

/**
 * Entity: Partner — Strategic Partner company profile.
 * Represents property developers, renovation contractors, electrical contractors,
 * and interior design firms that introduce products and services to customers.
 * Pure data blueprint — no business logic.
 */
public class Partner {

    private static int idCounter = 1000;

    private String partnerId;          // PK, auto-generated (e.g. "P1001")
    private String companyName;        // e.g., "Sime Darby Property", "SP Setia", "Sunway Property"
    private String partnerCategory;    // PROPERTY_DEVELOPER, RENOVATION_CONTRACTOR, ELECTRICAL_CONTRACTOR, INTERIOR_DESIGN_FIRM
    private String contactPerson;
    private String contactPhone;
    private String email;
    private String offeredServices;    // Description of products/services introduced
    private int totalReferralsCount;   // Number of successful customer introductions
    private double totalRevenueGenerated; // Total transaction revenue generated

    public Partner() {
        this.partnerId = generateId();
        this.totalReferralsCount = 0;
        this.totalRevenueGenerated = 0.0;
    }

    public Partner(String companyName, String partnerCategory, String contactPerson,
                   String contactPhone, String email, String offeredServices) {
        this.partnerId = generateId();
        this.companyName = companyName;
        this.partnerCategory = partnerCategory;
        this.contactPerson = contactPerson;
        this.contactPhone = contactPhone;
        this.email = email;
        this.offeredServices = offeredServices;
        this.totalReferralsCount = 0;
        this.totalRevenueGenerated = 0.0;
    }

    private static String generateId() {
        return "P" + (idCounter++);
    }

    public static void updateIdCounter(int nextVal) {
        if (nextVal > idCounter) {
            idCounter = nextVal;
        }
    }

    // --- Getters & Setters ---

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPartnerCategory() {
        return partnerCategory;
    }

    public void setPartnerCategory(String partnerCategory) {
        this.partnerCategory = partnerCategory;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOfferedServices() {
        return offeredServices;
    }

    public void setOfferedServices(String offeredServices) {
        this.offeredServices = offeredServices;
    }

    public int getTotalReferralsCount() {
        return totalReferralsCount;
    }

    public void setTotalReferralsCount(int totalReferralsCount) {
        this.totalReferralsCount = totalReferralsCount;
    }

    public double getTotalRevenueGenerated() {
        return totalRevenueGenerated;
    }

    public void setTotalRevenueGenerated(double totalRevenueGenerated) {
        this.totalRevenueGenerated = totalRevenueGenerated;
    }

    public void incrementReferrals(double revenue) {
        this.totalReferralsCount++;
        this.totalRevenueGenerated += revenue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Partner other = (Partner) obj;
        return partnerId != null && partnerId.equals(other.partnerId);
    }

    @Override
    public String toString() {
        return "Partner{id='" + partnerId + "', name='" + companyName
                + "', category='" + partnerCategory + "', referrals=" + totalReferralsCount
                + ", revenue=" + totalRevenueGenerated + "}";
    }
}
