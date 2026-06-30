package entity;

/**
 * Entity: Stores room information.
 * Pure data blueprint — no business logic.
 */
public class Room {

    private String roomNumber;
    private String roomType;   // e.g. "Standard", "Deluxe", "Suite"
    private double price;
    private String status;     // e.g. "Available", "Occupied", "Reserved"

    public Room() {
    }

    public Room(String roomNumber, String roomType, double price, String status) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.price = price;
        this.status = status;
    }

    // --- Getters & Setters ---

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Room{number='" + roomNumber + "', type='" + roomType
                + "', price=" + price + ", status='" + status + "'}";
    }
}
