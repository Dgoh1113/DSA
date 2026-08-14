package entity;

import adt.DoublyLinkedList;

/**
 * Entity: LoyaltyAccount — Manages points balances, tier qualification, and history.
 * Pure data blueprint — no business logic.
 *
 * Primary Key / Foreign Key: memberId (→ Guest.guestId)
 * Core Attributes: totalPoints, tierStatus, pointsExpiryDate, pointHistoryList
 */
public class LoyaltyAccount {

    private String memberId;          // PK/FK → Guest.guestId
    private int totalPoints;
    private String tierStatus;        // STANDARD, SILVER, GOLD, PLATINUM, DIAMOND
    private String pointsExpiryDate;  // Date string (e.g., "2027-08-03")
    private DoublyLinkedList<String> pointHistoryList; // Audit trail of point changes

    public LoyaltyAccount() {
        this.totalPoints = 0;
        this.tierStatus = "STANDARD";
        this.pointHistoryList = new DoublyLinkedList<>();
    }

    public LoyaltyAccount(String memberId) {
        this.memberId = memberId;
        this.totalPoints = 0;
        this.tierStatus = "STANDARD";
        this.pointHistoryList = new DoublyLinkedList<>();
    }

    public LoyaltyAccount(String memberId, int totalPoints, String tierStatus, String pointsExpiryDate) {
        this.memberId = memberId;
        this.totalPoints = totalPoints;
        this.tierStatus = tierStatus;
        this.pointsExpiryDate = pointsExpiryDate;
        this.pointHistoryList = new DoublyLinkedList<>();
    }

    // --- Getters & Setters ---

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getTierStatus() {
        return tierStatus;
    }

    public void setTierStatus(String tierStatus) {
        this.tierStatus = tierStatus;
    }

    public String getPointsExpiryDate() {
        return pointsExpiryDate;
    }

    public void setPointsExpiryDate(String pointsExpiryDate) {
        this.pointsExpiryDate = pointsExpiryDate;
    }

    public DoublyLinkedList<String> getPointHistoryList() {
        return pointHistoryList;
    }

    public void setPointHistoryList(DoublyLinkedList<String> pointHistoryList) {
        this.pointHistoryList = pointHistoryList;
    }

    /**
     * Adds a history entry to the point history list.
     */
    public void addHistoryEntry(String entry) {
        pointHistoryList.add(entry);
    }

    @Override
    public String toString() {
        return "LoyaltyAccount{memberId='" + memberId + "', points=" + totalPoints
                + ", tier='" + tierStatus + "', expiry='" + pointsExpiryDate + "'}";
    }
}
