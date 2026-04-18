import java.util.Scanner;

public class AdminMenu {
    private static final String ADMIN_PASSWORD = "admin321";
    private final Scanner input;
    private final InputValidator reader;
    private final ConfigManager config;

    public AdminMenu(ConfigManager config) {
        this.input  = new Scanner(System.in);
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
            String word = remaining == 1 ? "attempt" : "attempts";
            System.out.println("Incorrect password. " + remaining + " " + word + " remaining.");
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

        System.out.print("Enter new fare (e.g. 3.50): ");
        try {
            java.math.BigDecimal fare = new java.math.BigDecimal(input.nextLine().trim());
            config.updateBaseFare(fromZone, toZone, timeBand, fare);
        } catch (NumberFormatException e) {
            System.out.println("INPUT ERROR: Enter a valid number e.g. 3.50");
        }
    }

    private void manageDiscounts() {
        System.out.println("\n--- Manage Discounts ---");
        CityRideDataset.PassengerType type = reader.getValidPassengerType(
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

        System.out.print("Enter new discount rate (0.00 to 1.00, e.g. 0.25 for 25%): ");
        try {
            java.math.BigDecimal rate = new java.math.BigDecimal(input.nextLine().trim());
            config.updateDiscountRate(type, rate);
        } catch (NumberFormatException e) {
            System.out.println("INPUT ERROR: Enter a valid number e.g. 0.25");
        }
    }

    private void manageDailyCaps() {
        System.out.println("\n--- Manage Daily Caps ---");
        CityRideDataset.PassengerType type = reader.getValidPassengerType(
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

        System.out.print("Enter new daily cap (e.g. 8.00): ");
        try {
            java.math.BigDecimal cap = new java.math.BigDecimal(input.nextLine().trim());
            config.updateDailyCap(type, cap);
        } catch (NumberFormatException e) {
            System.out.println("INPUT ERROR: Enter a valid number e.g. 8.00");
        }
    }

    private void managePeakWindows() {
        System.out.println("\n--- Manage Peak Windows ---");
        System.out.println("Current windows:");
        System.out.println("Morning: " + config.getPeakMorningStart() + " - " + config.getPeakMorningEnd());
        System.out.println("Evening: " + config.getPeakEveningStart() + " - " + config.getPeakEveningEnd());

        System.out.print("Enter morning start (HH:mm e.g. 07:00): ");
        String morningStart = input.nextLine().trim();
        System.out.print("Enter morning end (HH:mm e.g. 09:30): ");
        String morningEnd = input.nextLine().trim();
        System.out.print("Enter evening start (HH:mm e.g. 16:00): ");
        String eveningStart = input.nextLine().trim();
        System.out.print("Enter evening end (HH:mm e.g. 19:00): ");
        String eveningEnd = input.nextLine().trim();

        config.updatePeakWindows(morningStart, morningEnd, eveningStart, eveningEnd);
    }
}