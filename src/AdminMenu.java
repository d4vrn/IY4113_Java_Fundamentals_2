import java.util.Scanner;

public class AdminMenu {
    private static final String ADMIN_PASSWORD = "admin321";
    private final Scanner input;
    private final InputValidator reader;
    private final ConfigManager config;

    public AdminMenu(Scanner input, ConfigManager config) {
        this.input  = input;
        this.reader = new InputValidator(input);
        this.config = config;
    }

    public void run() {
        int attempts   = 0;
        int maxAttempts = 3;
        int menuChoice = 0;

        while (attempts < maxAttempts) {
            System.out.println("\nEnter password: ");
            System.out.print("> ");
            String passwordInput = input.nextLine().trim();

            if (passwordInput.equals(ADMIN_PASSWORD)) {
                break;
            }

            attempts++;
            int remaining = maxAttempts - attempts;
            if (remaining > 0) {
                String word = remaining == 1 ? "attempt" : "attempts";
                System.out.println("Incorrect password. " + remaining + " " + word + " remaining.");
            } else {
                System.out.println("Incorrect password.");
            }
        }

        if (attempts == maxAttempts) {
            System.out.println("\nToo many failed attempts. Returning to main menu...");
            return;
        }

        System.out.println("\nWelcome, Admin!");

        do {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. View config");
            System.out.println("2. Manage base fares");
            System.out.println("3. Manage discounts");
            System.out.println("4. Manage daily caps");
            System.out.println("5. Manage peak windows");
            System.out.println("6. Exit");

            System.out.print("\n> ");
            String choiceText = input.nextLine().trim();
            try {
                menuChoice = Integer.parseInt(choiceText);
            } catch (NumberFormatException e) {
                System.out.println("INPUT ERROR: Choose a number from 1 to 6.");
                continue;
            }

            switch (menuChoice) {
                case 1 -> config.displayConfig();
                case 2 -> manageBaseFares();
                case 3 -> manageDiscounts();
                case 4 -> manageDailyCaps();
                case 5 -> managePeakWindows();
                case 6 -> System.out.println("\nReturning to main menu...");
                default -> System.out.println("INPUT ERROR: Choose a number from 1 to 6.");
            }

        } while (menuChoice != 6);
    }


    private void manageBaseFares() {
        System.out.println("\n--- Manage Base Fares ---");
        int fromZone = reader.getValidZone("Enter from zone (1-5): ");
        int toZone   = reader.getValidZone("Enter to zone (1-5): ");
        CityRideDataset.TimeBand timeBand = reader.getValidTimeBand("Enter time band (PEAK/OFF_PEAK): ");

        while (true) {
            System.out.print("Enter new fare (e.g. 3.50): ");
            String txt = input.nextLine().trim();
            if (txt.isBlank()) { System.out.println("INPUT ERROR: Enter a valid number e.g. 3.50"); continue; }
            java.math.BigDecimal fare;
            try {
                fare = new java.math.BigDecimal(txt);
            } catch (NumberFormatException e) {
                System.out.println("INPUT ERROR: Enter a valid number e.g. 3.50");
                continue;
            }
            if (config.updateBaseFare(fromZone, toZone, timeBand, fare)) break;
        }
    }

    private void manageDiscounts() {
        System.out.println("\n--- Manage Discounts ---");
        CityRideDataset.PassengerType type = reader.getValidPassengerType(
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

        while (true) {
            System.out.print("Enter new discount rate (0.00 to 1.00, e.g. 0.25 for 25%): ");
            String txt = input.nextLine().trim();
            if (txt.isBlank()) { System.out.println("INPUT ERROR: Enter a valid number e.g. 0.25"); continue; }
            java.math.BigDecimal rate;
            try {
                rate = new java.math.BigDecimal(txt);
            } catch (NumberFormatException e) {
                System.out.println("INPUT ERROR: Enter a valid number e.g. 0.25");
                continue;
            }
            if (config.updateDiscountRate(type, rate)) break;
        }
    }

    private void manageDailyCaps() {
        System.out.println("\n--- Manage Daily Caps ---");
        CityRideDataset.PassengerType type = reader.getValidPassengerType(
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

        while (true) {
            System.out.print("Enter new daily cap (e.g. 8.00): ");
            String txt = input.nextLine().trim();
            if (txt.isBlank()) { System.out.println("INPUT ERROR: Enter a valid number e.g. 8.00"); continue; }
            java.math.BigDecimal cap;
            try {
                cap = new java.math.BigDecimal(txt);
            } catch (NumberFormatException e) {
                System.out.println("INPUT ERROR: Enter a valid number e.g. 8.00");
                continue;
            }
            if (config.updateDailyCap(type, cap)) break;
        }
    }

    private void managePeakWindows() {
        System.out.println("\n--- Manage Peak Windows ---");
        System.out.println("Current windows:");
        System.out.println("Morning: " + config.getPeakMorningStart() + " - " + config.getPeakMorningEnd());
        System.out.println("Evening: " + config.getPeakEveningStart() + " - " + config.getPeakEveningEnd());

        while (true) {
            String morningStart = reader.getValidTime("Enter morning start (HH:mm e.g. 07:00): ");
            String morningEnd   = reader.getValidTime("Enter morning end (HH:mm e.g. 09:30): ");
            String eveningStart = reader.getValidTime("Enter evening start (HH:mm e.g. 16:00): ");
            String eveningEnd   = reader.getValidTime("Enter evening end (HH:mm e.g. 19:00): ");
            if (config.updatePeakWindows(morningStart, morningEnd, eveningStart, eveningEnd)) break;
            System.out.println("Please re-enter all four times.");
        }
    }
}