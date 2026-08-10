import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HotelManagementSystem {
    private final List<Room> rooms = new ArrayList<>();
    private final List<Guest> guests = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final ReservationStorage storage = new ReservationStorage();

    private int nextReservationId = 1;

    // room management 
    public void addRoom(Room room) {
        if (findRoom(room.getRoomNumber()) != null) {
            throw new IllegalArgumentException("Room" + room.getRoomNumber() + "already exists.");
        }
        rooms.add(room);
    }

    public void removeRoom(String roomNumber) {
        Room room = requireRoom(roomNumber);
        boolean hasReservations = reservations.stream()
                .anyMatch(reservation -> reservation.getRoom() == room && isActive(reservation.getStatus()));
        if (hasReservations) {
            throw new IllegalStateException("Cannot remove room with active reservations.");
        }
        rooms.remove(room);    
    }

    public void setRoomMaintenance(String roomNumber, boolean underMaintenance) {
        requireRoom(roomNumber).setStatus(underMaintenance ? Room.RoomStatus.MAINTENANCE : Room.RoomStatus.AVAILABLE);
    }

    public Room findRoom(String roomNumber) {
        return rooms.stream()
                .filter(room -> room.getRoomNumber().equalsIgnoreCase(roomNumber))
                .findFirst()
                .orElse(null);
    }

    public List<Room> getAllRooms(){
        return new ArrayList<>(rooms);
    }

    // guest management
    public Guest findOrCreateGuest(String guestId, String name, String phone, String email) {
        Guest exists = findGuest(guestId);
        if (exists != null) {
            return exists;
        }  
        Guest guest = new Guest(guestId, name, phone, email);              
        guests.add(guest);
        return guest;
    }

    public Guest findGuest(String guestId) {
        return guests.stream()
                .filter(guest -> guest.getGuestId().equalsIgnoreCase(guestId))
                .findFirst()
                .orElse(null);
    }

    //search for rooms available & not under maintenance
    public List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        validateDateRange(checkIn, checkOut);
        List<Room> available = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getStatus() == Room.RoomStatus.AVAILABLE && isRoomAvailable(room, checkIn, checkOut)) {
                available.add(room);
            }
        }
        return available;
    }

    // reservation management
    public Reservation makeReservation(String roomNumber, Guest guest, LocalDate checkIn, LocalDate checkOut) {
        validateDateRange(checkIn, checkOut);
        Room room = requireRoom(roomNumber);
        if (room.getStatus() != Room.RoomStatus.AVAILABLE) {
            throw new IllegalStateException("Room is not available for reservation.");
        }
        if (!isRoomAvailable(room, checkIn, checkOut)) {
            throw new IllegalStateException("Room is already booked for the selected dates.");
        }
        String id = String.format("R%04d", nextReservationId++);
        Reservation reservation = new Reservation(id, room, guest, checkIn, checkOut);
        reservations.add(reservation);
        return reservation;
    }

    private Room requireRoom(String roomNumber) {
        Room room = findRoom(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room " + roomNumber + " does not exist.");
        }
        return room;
    }

    //cancel reservation, check in, check out
    public void cancelReservation(String reservationId) {
        transition(reservationId, Reservation.ReservationStatus.RESERVED ,Reservation.ReservationStatus.CANCELED);
    }

    public void checkIn(String reservationId) {
        transition(reservationId, Reservation.ReservationStatus.RESERVED, Reservation.ReservationStatus.CHECKED_IN);
    }

    public void checkOut(String reservationId) {
        transition(reservationId, Reservation.ReservationStatus.CHECKED_IN, Reservation.ReservationStatus.CHECKED_OUT);
    }

    private void transition(String reservationId, Reservation.ReservationStatus from, Reservation.ReservationStatus to) {
        Reservation reservation = requireReservation(reservationId);
        if (reservation.getStatus() != from) {
            throw new IllegalStateException("Reservation " + reservationId + " must be " + from + " for this action (current status: " + reservation.getStatus() + ").");
        }
        reservation.setStatus(to);
    }

    public Reservation findReservation(String reservationId) {
        return reservations.stream()
                .filter(reservation -> reservation.getReservationId().equalsIgnoreCase(reservationId))
                .findFirst()
                .orElse(null);
                            
                    
    }

    //reservation history
    public List<Reservation> getReservationHistory() {
        return new ArrayList<>(reservations);
    }

    public List<Reservation> getReservationHistoryForGuest(String guestId) {
        return reservations.stream()
                .filter(reservation -> reservation.getGuest().getGuestId().equalsIgnoreCase(guestId))
                .toList();
            

    }

    //Persistence methods

    public boolean loadFromFile() throws IOException{
        ReservationStorage.LoadedData data = storage.load();
        rooms.clear();
        rooms.addAll(data.rooms());
        guests.clear();
        guests.addAll(data.guests()); reservations.clear();
        reservations.addAll(data.reservations());
                        
                

        nextReservationId = reservations.stream()
                .mapToInt(reservation -> numericSuffix(reservation.getReservationId()))
                .max()
                .orElse(0) + 1;
        
        return !rooms.isEmpty() || !guests.isEmpty() || !reservations.isEmpty();
    }

    public void saveToFile() throws IOException {
        storage.save(rooms, guests, reservations);
    }

    private static int numericSuffix(String reservationId) {
        String digits = reservationId.replaceAll("\\D", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private static boolean isActive(Reservation.ReservationStatus status) {
        return status == Reservation.ReservationStatus.RESERVED || status == Reservation.ReservationStatus.CHECKED_IN;
    }

    private boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        return reservations.stream()
                .filter(reservation -> reservation.getRoom() == room && isActive(reservation.getStatus()))
                .noneMatch(reservation -> reservation.overlaps(checkIn, checkOut));
    }
    private Reservation requireReservation(String reservationId) {
        Reservation reservation = findReservation(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation " + reservationId + " does not exist.");
        }
        return reservation;
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
    }
}   