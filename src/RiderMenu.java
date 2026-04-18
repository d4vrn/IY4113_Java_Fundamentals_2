import java.util.Scanner;

public class RiderMenu {
    private final Scanner input;
    private final ConfigManager config;
    private RiderProfile profile;
    private JourneyManager journeyManager;

    public RiderMenu(ConfigManager config) {
        this.input  = new Scanner(System.in);
        this.config = config;
    }

    public void run() {

        profile         = handleProfileSetup();
        journeyManager  = new JourneyManager(input, config, profile);

        boolean loadSession = getConfirmation("Load previous session? (y/n): ");
        if (loadSession) {
            journeyManager.loadSession();
        }

        int menuChoice = 0;
        do {
            System.out.print("\n");
            System.out.println("1. Add Journey");
            System.out.println("2. List Journeys");
            System.out.println("3. Filter Journeys");
            System.out.println("4. Daily Summary");
            System.out.println("5. Remove Journey");
            System.out.println("6. Edit Journey");
            System.out.println("7. Import Journeys");
            System.out.println("8. Export Journeys");
            System.out.println("9. Export Summary");
            System.out.println("10. Reset");
            System.out.println("11. Exit");

            System.out.print("\n> ");
            String choiceText = input.nextLine().trim();
            try {
                menuChoice = Integer.parseInt(choiceText);
            } catch (NumberFormatException e) {
                System.out.println("INPUT ERROR: Choose a number from 1 to 11.");
                continue;
            }

            switch (menuChoice) {
                case 1  -> journeyManager.addJourney();
                case 2  -> journeyManager.getJourneys();
                case 3  -> journeyManager.filterJourneys();
                case 4  -> journeyManager.dailySummary();
                case 5  -> journeyManager.removeJourney();
                case 6  -> journeyManager.editJourney();
                case 7  -> journeyManager.importJourneys();
                case 8  -> journeyManager.exportJourneys();
                case 9  -> journeyManager.exportSummary();
                case 10 -> journeyManager.reset();
                case 11 -> {
                    boolean save = getConfirmation("\nSave session before exiting? (y/n): ");
                    if (save) {
                        if (profile.getName().equals("Guest")) {
                            boolean createProfile = getConfirmation(
                                    "You need a profile to save. Create one now? (y/n): ");
                            if (createProfile) {
                                profile = handleProfileSetup();
                                journeyManager.updateProfile(profile);
                                journeyManager.saveSession();
                                profile.save();
                            } else {
                                System.out.println("Session not saved.");
                            }
                        } else {
                            journeyManager.saveSession();
                            profile.save();
                        }
                    }
                    System.out.println("\nGoodbye!");
                }
                default -> System.out.println("INPUT ERROR: Choose a number from 1 to 11.");
            }

        } while (menuChoice != 11);
    }

    private RiderProfile handleProfileSetup() {
        System.out.println("\n--- Rider Profile ---");

        java.util.List<String> existing = FileManager.listFiles(FileManager.PROFILES_DIR);

        if (!existing.isEmpty()) {
            System.out.println("Existing profiles found:");
            for (int i = 0; i < existing.size(); i++) {
                System.out.println((i + 1) + ". " + existing.get(i));
            }
            boolean load = getConfirmation("Load an existing profile? (y/n): ");
            if (load) {
                System.out.print("Enter profile name: ");
                String name = input.nextLine().trim();
                RiderProfile loaded = RiderProfile.load(name);
                if (loaded != null) return loaded;
                System.out.println("Could not load profile — creating a new one.");
            }
        }

        System.out.println("\nCreating new profile...");
        System.out.print("Enter your name: ");
        String name = input.nextLine().trim();

        CityRideDataset.PassengerType passengerType = null;
        while (passengerType == null) {
            System.out.print("Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");
            String typeInput = input.nextLine().trim();
            if (RiderProfile.isValidPassengerType(typeInput)) {
                passengerType = RiderProfile.toPassengerType(typeInput);
            } else {
                System.out.println("INPUT ERROR: Invalid passenger type.");
            }
        }

        String payment = null;
        while (payment == null) {
            System.out.print("Enter default payment (Cash/Card/Contactless): ");
            String paymentInput = input.nextLine().trim();
            if (RiderProfile.isValidPaymentOption(paymentInput)) {
                payment = paymentInput;
            } else {
                System.out.println("INPUT ERROR: Invalid payment option.");
            }
        }

        RiderProfile newProfile = new RiderProfile(name, passengerType, payment);
        newProfile.save();
        return newProfile;
    }

    private boolean getConfirmation(String prompt) {
        System.out.print(prompt);
        String response = input.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }
}