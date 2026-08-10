Hotel Reservation Management System

A Java command-line application for hotel staff to search room availability, manage reservations, check guests in and out, and maintain the room inventory.

The app is a single Java project made up of six classes: three entity classes (Room, Guest, Reservation), one persistence class (ReservationStorage), one service class (HotelManagementSystem) that holds all business rules, and the CLI entry point (Main).

Prerequisites
Java Development Kit (JDK) 17 or newer.

No external libraries or build tool are required. The entire application only uses the Java standard library, so there is nothing to install beyond the JDK itself.

Install and Run
All six .java files live in the same folder. From inside that folder:

javac *.java
java Main

On Windows, the same two commands work from PowerShell or Command Prompt.

What Happens on First Run
No saved data exists yet, so the system seeds six sample rooms so you can try the app right away:

101, 102 — SINGLE — $100.00/night
201, 202 — DOUBLE — $150.00/night
301, 302 — SUITE — $300.00/night

From then on, everything you do is saved automatically (see Data Persistence below), so the seeded rooms only appear the very first time.

Features
The CLI presents a numbered menu with the following actions:

Search available rooms — enter a check-in and check-out date; returns every room that is not under maintenance and has no conflicting reservation.
Make a reservation — searches availability, then books a room to a guest. You can reuse an existing guest ID or enter details for a new guest on the spot.
Cancel a reservation — only allowed while the reservation is still in RESERVED status.
Check in a guest — only allowed while RESERVED; moves the reservation to CHECKED_IN.
Check out a guest — only allowed while CHECKED_IN; moves the reservation to CHECKED_OUT.
Manage rooms (admin) — add a room, remove a room (blocked if it has active reservations), mark a room under maintenance or available again, or list every room.
View reservation history — enter a guest ID to see all of that guest's reservations, regardless of status.

Data Persistence
Data lives in memory while the program runs and is mirrored to three CSV files in a data folder created next to wherever you run java Main:

data/rooms.csv
data/guests.csv
data/reservations.csv

Saved data is loaded automatically on startup. It's saved automatically when you exit cleanly from the menu, and also on an abrupt exit (Ctrl+C or closing the terminal) via a shutdown hook, so an in-progress session isn't lost.

To reset the app back to a clean first-run state, delete the data folder.

Validation Rules
Check-in date cannot be in the past, and check-out date must be after check-in date.
A room can only be booked if it is marked AVAILABLE and has no other RESERVED or CHECKED_IN reservation overlapping the requested dates.
Guest email must match a basic name@domain.tld pattern; guest ID and name cannot be blank.
Room number cannot be blank, and price cannot be negative.
A reservation can only move from RESERVED to CHECKED_IN, or from CHECKED_IN to CHECKED_OUT — one step at a time, no skipping.
A room cannot be removed from inventory while it has active or upcoming reservations.

Project Structure
Room.java — room entity, field validation, nests the RoomStatus enum
Guest.java — guest entity and field validation
Reservation.java — reservation entity, date/overlap logic, nests the ReservationStatus enum
HotelManagementSystem.java — all business rules and validation; the single entry point the CLI talks to
ReservationStorage.java — reads and writes the three CSV files
Main.java — CLI menu and user input handling
