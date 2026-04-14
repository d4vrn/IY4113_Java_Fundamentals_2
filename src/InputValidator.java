import java.util.Scanner;

public class InputValidator {

    private final Scanner input;

    public InputValidator(Scanner input) {
        this.input = input;
    }

    public String getValidDate(String prompt) {
        String result = null;
        while (result == null) {
            System.out.print(prompt);
            String txt = input.nextLine().trim();
            if (txt.isBlank()) {
                System.out.println("INPUT ERROR: Date is required. Use DD/MM/YYYY (e.g., 28/02/2026).");
                continue;
            }
            String[] parts = txt.split("/");
            if (parts.length != 3) {
                System.out.println("INPUT ERROR: Invalid date format. Use DD/MM/YYYY (e.g., 28/02/2026).");
                continue;
            }
            int day, month, year;
            try {
                day   = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                year  = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                System.out.println("INPUT ERROR: Day/Month/Year must be whole numbers.");
                continue;
            }
            if (day < 1 || day > 31)       { System.out.println("INPUT ERROR: Day must be 1-31.");           continue; }
            if (month < 1 || month > 12)   { System.out.println("INPUT ERROR: Month must be 1-12.");         continue; }
            if (year < 2026 || year > 2028){ System.out.println("INPUT ERROR: Year must be 2026-2028.");     continue; }
            result = String.format("%02d/%02d/%04d", day, month, year);
        }
        return result;
    }

    public String getValidTime(String prompt) {
        String result = null;
        while (result == null) {
            System.out.print(prompt);
            String txt = input.nextLine().trim();
            if (txt.isBlank()) {
                System.out.println("INPUT ERROR: Time is required. Use HH:MM (e.g., 14:59).");
                continue;
            }
            String[] parts = txt.split(":");
            if (!txt.contains(":") || parts.length != 2) {
                System.out.println("INPUT ERROR: Invalid time format. Use HH:MM (e.g., 14:59).");
                continue;
            }
            int hour, minute;
            try {
                hour   = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                System.out.println("INPUT ERROR: HOUR:MINUTE must be whole numbers.");
                continue;
            }
            if (hour < 0 || hour > 23)     { System.out.println("INPUT ERROR: Hour must be 00-23.");     continue; }
            if (minute < 0 || minute > 59) { System.out.println("INPUT ERROR: Minute must be 00-59.");   continue; }
            result = String.format("%02d:%02d", hour, minute);
        }
        return result;
    }

    public int getValidZone(String prompt) {
        Integer zone = null;
        while (zone == null) {
            System.out.print(prompt);
            String txt = input.nextLine().trim();
            if (txt.isBlank()) {
                System.out.println("INPUT ERROR: Zone is required.");
                continue;
            }
            int z;
            try {
                z = Integer.parseInt(txt);
            } catch (NumberFormatException e) {
                System.out.println("INPUT ERROR: Zone must be a whole number.");
                continue;
            }
            if (z < CityRideDataset.MIN_ZONE || z > CityRideDataset.MAX_ZONE) {
                System.out.println("INPUT ERROR: Zone must be " + CityRideDataset.MIN_ZONE + "-" + CityRideDataset.MAX_ZONE + ".");
                continue;
            }
            zone = z;
        }
        return zone;
    }

    public CityRideDataset.TimeBand getValidTimeBand(String prompt) {
        CityRideDataset.TimeBand result = null;
        while (result == null) {
            System.out.print(prompt);
            String txt = input.nextLine().trim();
            if (txt.isBlank()) {
                System.out.println("INPUT ERROR: Time Band is required.");
                continue;
            }
            if (CityRideDataset.TimeBand.PEAK.toString().equalsIgnoreCase(txt)) {
                result = CityRideDataset.TimeBand.PEAK;
            } else if (CityRideDataset.TimeBand.OFF_PEAK.toString().equalsIgnoreCase(txt)) {
                result = CityRideDataset.TimeBand.OFF_PEAK;
            } else {
                System.out.println("INPUT ERROR: Allowed values: PEAK, OFF_PEAK.");
            }
        }
        return result;
    }

    public CityRideDataset.PassengerType getValidPassengerType(String prompt) {
        CityRideDataset.PassengerType result = null;
        while (result == null) {
            System.out.print(prompt);
            String txt = input.nextLine().trim();
            if (txt.isBlank()) {
                System.out.println("INPUT ERROR: Passenger Type is required.");
                continue;
            }
            for (CityRideDataset.PassengerType p : CityRideDataset.PassengerType.values()) {
                if (p.toString().equalsIgnoreCase(txt)) {
                    result = p;
                    break;
                }
            }
            if (result == null) {
                System.out.println("INPUT ERROR: Allowed values: ADULT, STUDENT, CHILD, SENIOR_CITIZEN.");
            }
        }
        return result;
    }

    // Returns null if input is blank or not a number — caller decides what to do.
    public Integer getMenuChoice() {
        String txt = input.nextLine().trim();
        if (txt.isBlank()) return null;
        try {
            return Integer.parseInt(txt);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Used for reading journey IDs — returns any whole number, caller validates range.
    public int getValidInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String txt = input.nextLine().trim();
            if (txt.isBlank()) {
                System.out.println("INPUT ERROR: A value is required.");
                continue;
            }
            try {
                return Integer.parseInt(txt);
            } catch (NumberFormatException e) {
                System.out.println("INPUT ERROR: Please enter a whole number.");
            }
        }
    }

    // Used for confirmation prompts like delete/reset.
    public boolean getConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt);
            String txt = input.nextLine().trim();
            if (txt.equalsIgnoreCase("y")) return true;
            if (txt.equalsIgnoreCase("n")) return false;
            System.out.println("INPUT ERROR: Please enter 'y' or 'n'.");
        }
    }
}
