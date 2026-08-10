public class Guest{
    private final String guestId;
    private String name;
    private String phone;
    private String email;

    public Guest(String guestId, String name, String phone, String email) {
        if (guestId == null || guestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Guest ID cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Guest name cannot be empty.");
        }
        if (email == null ||  !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.guestId = guestId.trim();
        this.name = name.trim();
        this.phone = phone == null ? "" : phone.trim();
        this.email = email.trim();
    }

    public String getGuestId() {
        return guestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return String.format("Guest %-6s | %-20s | %-15s | %s", guestId, name, phone, email);
    }
}