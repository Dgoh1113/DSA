package entity;

/**
 * Entity: Stores guest information.
 * Pure data blueprint — no business logic.
 * Implements Comparable so it can be used in BST and PriorityQueue.
 */
public class Guest implements Comparable<Guest> {

    private String name;
    private String icNumber;
    private String phoneNumber;
    private String confirmationNumber; // 8-digit confirmation number
    private LoyaltyProfile loyaltyProfile;

    public Guest() {
        this.loyaltyProfile = new LoyaltyProfile();
    }

    public Guest(String name, String icNumber, String phoneNumber, String confirmationNumber) {
        this.name = name;
        this.icNumber = icNumber;
        this.phoneNumber = phoneNumber;
        this.confirmationNumber = confirmationNumber;
        this.loyaltyProfile = new LoyaltyProfile();
    }

    // --- Getters & Setters ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcNumber() {
        return icNumber;
    }

    public void setIcNumber(String icNumber) {
        this.icNumber = icNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public LoyaltyProfile getLoyaltyProfile() {
        return loyaltyProfile;
    }

    public void setLoyaltyProfile(LoyaltyProfile loyaltyProfile) {
        this.loyaltyProfile = loyaltyProfile;
    }

    /**
     * Compares guests by confirmation number.
     * This ordering is used by the BST for front-desk searching
     * and by the PriorityQueue for VIP sorting.
     */
    @Override
    public int compareTo(Guest other) {
        return this.confirmationNumber.compareTo(other.confirmationNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Guest other = (Guest) obj;
        return confirmationNumber != null && confirmationNumber.equals(other.confirmationNumber);
    }

    @Override
    public String toString() {
        return "Guest{name='" + name + "', ic='" + icNumber
                + "', phone='" + phoneNumber
                + "', confirm='" + confirmationNumber
                + "', loyalty=" + loyaltyProfile + "}";
    }
}
