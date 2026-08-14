package entity;

/**
 * Entity: Guest — Master customer profile.
 * Pure data blueprint — no business logic.
 *
 * Primary Key: guestId (auto-generated)
 * Core Attributes: name, icPassport, contactNo, email, loyaltyTier
 * Tracks membership tier level (STANDARD, SILVER, GOLD, PLATINUM, DIAMOND).
 */
public class Guest {

    private static int idCounter = 1000;

    private String guestId;       // PK, auto-generated (e.g., "G1001")
    private String name;
    private String icPassport;
    private String contactNo;
    private String email;
    private String loyaltyTier;   // STANDARD, SILVER, GOLD, PLATINUM, DIAMOND

    public Guest() {
        this.guestId = generateId();
        this.loyaltyTier = "STANDARD";
    }

    public Guest(String name, String icPassport, String contactNo, String email) {
        this.guestId = generateId();
        this.name = name;
        this.icPassport = icPassport;
        this.contactNo = contactNo;
        this.email = email;
        this.loyaltyTier = "STANDARD";
    }

    public Guest(String name, String icPassport, String contactNo, String email, String loyaltyTier) {
        this.guestId = generateId();
        this.name = name;
        this.icPassport = icPassport;
        this.contactNo = contactNo;
        this.email = email;
        this.loyaltyTier = loyaltyTier;
    }

    private static String generateId() {
        return "G" + (idCounter++);
    }

    public static void updateIdCounter(int nextVal) {
        if (nextVal > idCounter) {
            idCounter = nextVal;
        }
    }

    // --- Getters & Setters ---

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcPassport() {
        return icPassport;
    }

    public void setIcPassport(String icPassport) {
        this.icPassport = icPassport;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoyaltyTier() {
        return loyaltyTier;
    }

    public void setLoyaltyTier(String loyaltyTier) {
        this.loyaltyTier = loyaltyTier;
    }

    /**
     * Returns the tier weight used in priority score calculations.
     * STANDARD=0, SILVER=1, GOLD=2, PLATINUM=3, DIAMOND=4
     */
    public int getTierWeight() {
        switch (loyaltyTier) {
            case "DIAMOND":  return 4;
            case "PLATINUM": return 3;
            case "GOLD":     return 2;
            case "SILVER":   return 1;
            default:         return 0;
        }
    }

    /**
     * Checks if this guest qualifies for VIP priority (non-STANDARD tier).
     */
    public boolean isVIP() {
        return !"STANDARD".equals(loyaltyTier);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Guest other = (Guest) obj;
        return guestId != null && guestId.equals(other.guestId);
    }

    @Override
    public String toString() {
        return "Guest{id='" + guestId + "', name='" + name
                + "', ic='" + icPassport + "', phone='" + contactNo
                + "', email='" + email + "', tier=" + loyaltyTier + "}";
    }
}
