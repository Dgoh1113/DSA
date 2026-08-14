package entity;

/**
 * Entity: Reservation — Holds reservation lifecycle details.
 * Pure data blueprint — no business logic.
 *
 * Primary Key: confirmationNo (8-digit, auto-generated)
 * Foreign Keys: guestId (→ Guest), assignedRoomNo (→ Room)
 * Core Attributes: roomType, checkInDate, checkOutDate, bookingStatus,
 *                  priorityScore, timestamp
 *
 * Implements Comparable to support BST indexing by confirmationNo
 * and Max-Heap ordering by priorityScore.
 */
public class Reservation implements Comparable<Reservation> {

    private static int confirmCounter = 10000000; // starts at 10000000 for 8 digits

    private String confirmationNo;    // PK, 8-digit
    private String guestId;           // FK → Guest
    private String roomType;          // STANDARD, DELUXE, SUITE
    private String assignedRoomNo;    // FK → Room (null until assigned)
    private String checkInDate;
    private String checkOutDate;
    private String bookingStatus;     // PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
    private int priorityScore;        // Used for Max-Heap VIP ordering
    private long timestamp;           // Arrival order timestamp

    // Transient convenience reference (not a persisted FK)
    private Guest guest;

    public Reservation() {
        this.confirmationNo = generateConfirmationNo();
        this.bookingStatus = "PENDING";
        this.timestamp = System.currentTimeMillis();
    }

    public Reservation(String guestId, String roomType, String checkInDate, String checkOutDate) {
        this.confirmationNo = generateConfirmationNo();
        this.guestId = guestId;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingStatus = "PENDING";
        this.timestamp = System.currentTimeMillis();
    }

    private static String generateConfirmationNo() {
        return String.valueOf(confirmCounter++);
    }

    public static void updateConfirmCounter(int nextVal) {
        if (nextVal > confirmCounter) {
            confirmCounter = nextVal;
        }
    }

    // --- Getters & Setters ---

    public String getConfirmationNo() {
        return confirmationNo;
    }

    public void setConfirmationNo(String confirmationNo) {
        this.confirmationNo = confirmationNo;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getAssignedRoomNo() {
        return assignedRoomNo;
    }

    public void setAssignedRoomNo(String assignedRoomNo) {
        this.assignedRoomNo = assignedRoomNo;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(int priorityScore) {
        this.priorityScore = priorityScore;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    /**
     * Compares reservations by confirmationNo.
     * This ordering is used by the BST for front-desk searching (Module 3).
     */
    @Override
    public int compareTo(Reservation other) {
        return this.confirmationNo.compareTo(other.confirmationNo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reservation other = (Reservation) obj;
        return confirmationNo != null && confirmationNo.equals(other.confirmationNo);
    }

    @Override
    public String toString() {
        return "Reservation{confirm='" + confirmationNo + "', guestId='" + guestId
                + "', roomType='" + roomType + "', room='" + assignedRoomNo
                + "', status='" + bookingStatus + "', priority=" + priorityScore + "}";
    }
}
