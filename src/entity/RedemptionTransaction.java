package entity;

/**
 * Entity: RedemptionTransaction — Audit log for points redemption.
 * Pure data blueprint — no business logic.
 *
 * Primary Key: transactionId (auto-generated)
 * Foreign Key: memberId (→ LoyaltyAccount.memberId → Guest.guestId)
 * Core Attributes: rewardItem, pointsDeducted, requestDate, status
 */
public class RedemptionTransaction {

    private static int txnCounter = 1;

    private String transactionId;    // PK, auto-generated (e.g., "TXN0001")
    private String memberId;         // FK → LoyaltyAccount
    private String rewardItem;       // Description of redeemed reward
    private int pointsDeducted;
    private String requestDate;
    private String status;           // PENDING, APPROVED, REJECTED

    public RedemptionTransaction() {
        this.transactionId = generateTxnId();
        this.status = "PENDING";
    }

    public RedemptionTransaction(String memberId, String rewardItem, int pointsDeducted, String requestDate) {
        this.transactionId = generateTxnId();
        this.memberId = memberId;
        this.rewardItem = rewardItem;
        this.pointsDeducted = pointsDeducted;
        this.requestDate = requestDate;
        this.status = "APPROVED"; // Automatically approved on valid redemption
    }

    private static String generateTxnId() {
        return String.format("TXN%04d", txnCounter++);
    }

    public static void updateTxnCounter(int nextVal) {
        if (nextVal > txnCounter) {
            txnCounter = nextVal;
        }
    }

    // --- Getters & Setters ---

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getRewardItem() {
        return rewardItem;
    }

    public void setRewardItem(String rewardItem) {
        this.rewardItem = rewardItem;
    }

    public int getPointsDeducted() {
        return pointsDeducted;
    }

    public void setPointsDeducted(int pointsDeducted) {
        this.pointsDeducted = pointsDeducted;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RedemptionTransaction{id='" + transactionId + "', member='" + memberId
                + "', item='" + rewardItem + "', points=" + pointsDeducted
                + ", date='" + requestDate + "', status='" + status + "'}";
    }
}
