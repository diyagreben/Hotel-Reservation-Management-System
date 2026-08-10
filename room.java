public class Room {
    public enum RoomStatus {
        AVAILABLE,
        MAINTENANCE
    }

    private final String roomNumber;
    private String type;
    private double pricePerNight;
    private RoomStatus status;

    public Room(String roomNumber, String type, double pricePerNight) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number cannot be null or empty.");
        }
        if (pricePerNight < 0) {
            throw new IllegalArgumentException("Price per night cannot be negative.");
        }
        this.roomNumber = roomNumber.trim();
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.status = RoomStatus.AVAILABLE;
    }

    public String getRoomNumber() {
        return roomNumber;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        if (pricePerNight < 0) {
            throw new IllegalArgumentException("Price per night cannot be negative.");
        }
        this.pricePerNight = pricePerNight;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Room %-6s | %-8s | $%.2f/night | &s", roomNumber, type, pricePerNight, status);
    }

}