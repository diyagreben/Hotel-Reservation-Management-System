import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final HotelManagementSystem system = new HotelManagementSystem();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean loadedExistingData = false;
        try {
            loadedExistingData = system.loadFromFile();
        } catch (IOException e) {
            System.out.println("Could not read saved data (" + e.getMessage() + "). Starting again.");
        } catch (RuntimeException e) {
            System.out.println("Saved data may be corrupted (" + e.getMessage() + "). Starting again.");
        }
        if (!loadedExistingData) {
            seedSampleRooms();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(Main::saveQuietly));

        System.out.println("== Welcome to the Hotel Management System! ==");
        if (loadedExistingData) {
            System.out.println("Loaded existing data.");
        } 
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": searchAvailableRooms(); break;
                    case "2": makeReservation(); break;
                    case "3": cancelReservation(); break;
                    case "4": checkIn(); break;
                    case "5": checkOut(); break;
                    case "6": manageRooms(); break;
                    case "7": viewReservationHistory(); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice. Please try again.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
        saveQuietly();
        System.out.println("Exiting the system. Goodbye!");
    }

    private static void saveQuietly() {
        try {
            system.saveToFile();
        } catch (IOException e) {
            System.out.println("Could not save data (" + e.getMessage() + ").");
        }
    }

    private static void printMenu() {
        System.out.println("Please choose an option");
        System.out.println("========================================");
        System.out.println("1. Search available rooms");
        System.out.println("2. Make a reservation");
        System.out.println("3. Cancel a reservation");
        System.out.println("4. Check-in guest");
        System.out.println("5. Check-out guest");
        System.out.println("6. Manage rooms (admin only)");
        System.out.println("7. View reservation history");
        System.out.println("0. Exit");
    }

    private static void searchAvailableRooms() {
        LocalDate checkInDate = promptDate("Enter check-in date (yyyy-MM-dd): ");
        LocalDate checkOutDate = promptDate("Enter check-out date (yyyy-MM-dd): ");
        List<Room> availableRooms = system.searchAvailableRooms(checkInDate, checkOutDate);
        if (availableRooms.isEmpty()) {
            System.out.println("No rooms available for the selected dates.");
        } else {
            System.out.println("Available rooms:");
            for (Room room : availableRooms) {
                System.out.println(" " + room);
            }
        }
    }

    private static void makeReservation() {
        LocalDate checkInDate = promptDate("Enter check-in date (yyyy-MM-dd):");
        LocalDate checkOutDate = promptDate("Enter check-out date (yyyy-MM-dd):");

        List<Room> availableRooms = system.searchAvailableRooms(checkInDate, checkOutDate);
        if (availableRooms.isEmpty()) {
            System.out.println("No rooms available for the selected dates.");
            return;
        }
        System.out.println("Available rooms:");
        for (Room room : availableRooms) {
            System.out.println(" " + room);
        }

        System.out.print("Enter room number to reserve: ");
        String roomNumber = scanner.nextLine().trim();

        System.out.print("Enter guest ID (existing or new): ");
        String guestId = scanner.nextLine().trim();
        Guest guest = system.findGuest(guestId);
        if (guest == null) {
            System.out.println("New guest. Please provide details.");
            System.out.print("Enter guest name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter guest phone: ");
            String phone = scanner.nextLine().trim();
            System.out.print("Enter guest email: ");
            String email = scanner.nextLine().trim();
            guest = system.findOrCreateGuest(guestId, name, phone, email);
        }

        Reservation reservation = system.makeReservation(roomNumber, guest, checkInDate, checkOutDate);
        System.out.println("Reservation created successfully: " + reservation);
    }

    private static void cancelReservation() {
        System.out.print("Enter reservation ID to cancel: ");
        String reservationId = scanner.nextLine().trim();
        system.cancelReservation(reservationId);
        System.out.println("Reservation canceled successfully.");
    }

    private static void checkIn() {
        System.out.print("Enter reservation ID to check-in: ");
        String reservationId = scanner.nextLine().trim();
        system.checkIn(reservationId);
        System.out.println("Reservation" + reservationId + " checked in successfully.");
    }

    private static void checkOut() {
        System.out.print("Enter reservation ID to check-out: ");
        String reservationId = scanner.nextLine().trim();
        system.checkOut(reservationId);
        System.out.println("Reservation" + reservationId + " checked out successfully.");
    }

    private static void manageRooms() {
        System.out.println(" a) Add Room");
        System.out.println(" b) Remove Room");
        System.out.println(" c) Mark Room as Under Maintenance");
        System.out.println(" d) Mark Room as Available");
        System.out.println(" e) List All Rooms");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim().toLowerCase();
        switch (choice) {
            case "a": {
                System.out.print("Enter room number: ");
                String roomNumber = scanner.nextLine().trim();
                System.out.print("Enter room type (SINGLE, DOUBLE, SUITE): ");
                String typeInput = scanner.nextLine().trim().toUpperCase();
                System.out.print("Enter room price: ");
                double price = Double.parseDouble(scanner.nextLine().trim());
                system.addRoom(new Room(roomNumber, typeInput, price));
                System.out.println("Room " + roomNumber + " added successfully.");
                break;
            }
            case "b": {
                System.out.print("Enter room number to remove: ");
                String roomNumber = scanner.nextLine().trim();
                system.removeRoom(roomNumber);
                System.out.println("Room " + roomNumber + " removed successfully.");
                break;
            }
            case "c": {
                System.out.print("Enter room number to mark as under maintenance: ");
                String roomNumber = scanner.nextLine().trim();
                system.setRoomMaintenance(roomNumber, true);
                System.out.println("Room " + roomNumber + " marked as under maintenance.");
                break;
            }
            case "d": {
                System.out.print("Enter room number to mark as available: ");
                String roomNumber = scanner.nextLine().trim();
                system.setRoomMaintenance(roomNumber, false);
                System.out.println("Room " + roomNumber + " marked as available.");
                break;
            }
            case "e": {
                for (Room room : system.getAllRooms()) {
                    System.out.println(" " + room);
                }
                break;
            }
            default:
                System.out.println("Invalid choice.");
        }  
    }

    private static void viewReservationHistory() {
        System.out.print("Enter guest ID to view reservation history: ");
        String guestId = scanner.nextLine().trim();
        List<Reservation> history = system.getReservationHistoryForGuest(guestId);
        if (history.isEmpty()) {
            System.out.println("No reservations found for guest ID: " + guestId);
        } else {
            System.out.println("Reservation history for guest ID " + guestId + ":");
            for (Reservation reservation : history) {
                System.out.println(" " + reservation);
            }
        }
    }

    private static LocalDate promptDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
    }

    private static void seedSampleRooms() {
        system.addRoom(new Room("101", "SINGLE", 100.0));
        system.addRoom(new Room("102", "SINGLE", 100.0));
        system.addRoom(new Room("201", "DOUBLE", 150.0));
        system.addRoom(new Room("202", "DOUBLE", 150.0));
        system.addRoom(new Room("301", "SUITE", 300.0));
        system.addRoom(new Room("302", "SUITE", 300.0));
    }
}
