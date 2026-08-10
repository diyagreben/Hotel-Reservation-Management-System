import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {

    public enum ReservationStatus {
        RESERVED,
        CHECKED_IN,
        CHECKED_OUT,
        CANCELED
    }

    private final String reservationId;
    private final Room room;
    private final Guest guest;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private ReservationStatus status;
    private final LocalDateTime createdAt;

    public Reservation(String reservationId, Room room, Guest guest, LocalDate checkInDate, LocalDate checkOutDate) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Reservation ID cannot be empty.");
        }
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }
        if (guest == null) {
            throw new IllegalArgumentException("Guest cannot be null.");
        }
        if (checkInDate == null || checkOutDate == null || !checkInDate.isBefore(checkOutDate)) {
            throw new IllegalArgumentException("Invalid check-in and check-out dates.");
        }
        this.reservationId = reservationId.trim();
        this.room = room;
        this.guest = guest;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = LocalDateTime.now();
    }

    public String getReservationId() {
        return reservationId;
    }

    public Room getRoom() {
        return room;
    }
    
    public Guest getGuest() {
        return guest;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean overlaps(LocalDate checkIn, LocalDate checkOut) {
        return !(checkOut.isBefore(checkInDate) || checkIn.isAfter(checkOutDate));
    }

    @Override
    public String toString() {
        return String.format("Reservation %-6s | Room %-6s | %-20s | %s to %s | %s",
                reservationId, room.getRoomNumber(), guest.getName(), checkInDate, checkOutDate, status);
    }            
}