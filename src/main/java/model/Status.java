package model;

public enum Status {
    Booked("Booked"),
    Cancelled_by_Patient("Cancelled by Patient"),
    Cancelled_by_Doctor("Cancelled by Doctor"),
    Completed("Completed"),
    Absent("Absent (No-Show)");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public static Status fromDatabase(String dbValue) {
        if (dbValue == null) return Booked;
        return switch (dbValue.trim()) {
            case "Booked" -> Booked;
            case "Cancelled", "Cancelled_by_Patient" -> Cancelled_by_Patient;
            case "Cancelled_by_Doctor" -> Cancelled_by_Doctor;
            case "Completed" -> Completed;
            case "Absent", "No-Show", "Absent (No-Show)" -> Absent; // ← دعم متعدد
            default -> Booked;
        };
    }


    public String toDatabaseValue() {
        return switch (this) {
            case Booked -> "Booked";
            case Cancelled_by_Patient -> "Cancelled_by_Patient";
            case Cancelled_by_Doctor -> "Cancelled_by_Doctor";
            case Completed -> "Completed";
            case Absent -> "Absent";
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}