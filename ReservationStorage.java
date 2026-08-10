import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationStorage {

    public record LoadedData(List<Room> rooms, List<Guest> guests, List<Reservation> reservations) {}

    private static final Path DATA_FILE_PATH = Paths.get("data");
    private static final Path ROOMS_FILE_PATH = DATA_FILE_PATH.resolve("rooms.csv");
    private static final Path GUESTS_FILE_PATH = DATA_FILE_PATH.resolve("guests.csv");
    private static final Path RESERVATIONS_FILE_PATH = DATA_FILE_PATH.resolve("reservations.csv");

    public LoadedData load() throws IOException {
        List<Room> rooms = new ArrayList<>();
        List<Guest> guests = new ArrayList<>();
        List<Reservation> reservations = new ArrayList<>();
        
        if (Files.exists(ROOMS_FILE_PATH)) {
            for (String line : Files.readAllLines(ROOMS_FILE_PATH, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] f = splitCsvLine(line);
                Room room = new Room(f[0], f[1], Double.parseDouble(f[2]));
                room.setStatus(Room.RoomStatus.valueOf(f[3]));
                rooms.add(room);
            }
        }

        if (Files.exists(GUESTS_FILE_PATH)) {
            for (String line : Files.readAllLines(GUESTS_FILE_PATH, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] f = splitCsvLine(line);
                Guest guest = new Guest(f[0], f[1], f[2], f[3]);
                guests.add(guest);
            }
        }

        if (Files.exists(RESERVATIONS_FILE_PATH)) {
            for (String line : Files.readAllLines(RESERVATIONS_FILE_PATH, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] f = splitCsvLine(line);
                Room room = findByRoomNumber(rooms, f[1]);
                Guest guest = findByGuestId(guests, f[2]);
                if (room == null || guest == null) {
                    continue; 
                }
                Reservation reservation = new Reservation(f[0], room, guest, LocalDate.parse(f[3]), LocalDate.parse(f[4]));
                reservation.setStatus(Reservation.ReservationStatus.valueOf(f[5]));
                reservations.add(reservation);
            }
        }
        return new LoadedData(rooms, guests, reservations);
    }


    public void save(List<Room> rooms, List<Guest> guests, List<Reservation> reservations) throws IOException {
        Files.createDirectories(DATA_FILE_PATH);

        List<String> roomLines = new ArrayList<>();
        for (Room room : rooms) {
            roomLines.add(String.join(",",
                    csv(room.getRoomNumber()), csv(room.getType()), String.valueOf(room.getPricePerNight()), room.getStatus().name()));
        }
        Files.write(ROOMS_FILE_PATH, roomLines, StandardCharsets.UTF_8);

        List<String> guestLines = new ArrayList<>();
        for (Guest guest : guests) {
            guestLines.add(String.join(",",
                    csv(guest.getGuestId()), csv(guest.getName()), csv(guest.getPhone()), csv(guest.getEmail())));
        }
        Files.write(GUESTS_FILE_PATH, guestLines, StandardCharsets.UTF_8);

        List<String> reservationLines = new ArrayList<>();
        for (Reservation reservation : reservations) {
            reservationLines.add(String.join(",",
                    csv(reservation.getReservationId()), csv(reservation.getRoom().getRoomNumber()),
                    csv(reservation.getGuest().getGuestId()), reservation.getCheckInDate().toString(),
                    reservation.getCheckOutDate().toString(), reservation.getStatus().name()));
        }
        Files.write(RESERVATIONS_FILE_PATH, reservationLines, StandardCharsets.UTF_8);
        
    }

    private static Room findByRoomNumber(List<Room> rooms, String roomNumber) {
       for (Room room : rooms) {
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                return room;
            }
        }
        return null;
    }

    private static Guest findByGuestId(List<Guest> guests, String guestId) {
        for (Guest guest : guests) {
            if (guest.getGuestId().equalsIgnoreCase(guestId)) {
                return guest;
            }
        }
        return null;
    }

    private static String csv(String value) {
       if (value == null) {
        return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
       }
       return value;
    }

    private static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        currentField.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    currentField.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(currentField.toString());
                    currentField.setLength(0);
                } else {
                    currentField.append(c);
                }
            }
        }
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }
}