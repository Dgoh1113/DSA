package entity;

/**
 * Entity: Stores a guest's loyalty profile data.
 * Pure data blueprint — no business logic.
 */
public class LoyaltyProfile {

    private int points;
    private String tier; // "Silver", "Gold", "Diamond"

    public LoyaltyProfile() {
        this.points = 0;
        this.tier = "Silver";
    }

    public LoyaltyProfile(int points, String tier) {
        this.points = points;
        this.tier = tier;
    }

    // --- Getters & Setters ---

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    @Override
    public String toString() {
        return "LoyaltyProfile{points=" + points + ", tier='" + tier + "'}";
    }
}
