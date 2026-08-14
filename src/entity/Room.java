package entity;

/**
 * Entity: Room — Physical room inventory and operational status.
 * Pure data blueprint — no business logic.
 *
 * Primary Key: roomNo
 * Core Attributes: roomType, nightlyRate, status
 * Status values: AVAILABLE, OCCUPIED, MAINTENANCE
 * Room types: STANDARD, DELUXE, SUITE
 */
public class Room {

    private String roomNo;         // PK (e.g., "101", "202")
    private String roomType;       // STANDARD, DELUXE, SUITE
    private double nightlyRate;
    private String status;         // AVAILABLE, OCCUPIED, MAINTENANCE

    public Room() {
    }

    public Room(String roomNo, String roomType, double nightlyRate, String status) {
        this.roomNo = roomNo;
        this.roomType = roomType;
        this.nightlyRate = nightlyRate;
        this.status = status;
    }

    // --- Getters & Setters ---

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public double getNightlyRate() {
        return nightlyRate;
    }

    public void setNightlyRate(double nightlyRate) {
        this.nightlyRate = nightlyRate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Room{no='" + roomNo + "', type='" + roomType
                + "', rate=" + nightlyRate + ", status='" + status + "'}";
    }
}
