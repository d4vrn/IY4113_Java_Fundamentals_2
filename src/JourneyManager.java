import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JourneyManager {

    private int journeyId = 0;
    private final List<Journey> journeys = new ArrayList<>();
    private final InputValidator reader;
    private final ConfigManager config;
    private RiderProfile profile;

    public JourneyManager(Scanner input, ConfigManager config, RiderProfile profile) {
        this.reader  = new InputValidator(input);
        this.config  = config;
        this.profile = profile;
    }

    public void updateProfile(RiderProfile profile) {
        this.profile = profile;
    }

    public void addJourney() {
        String date   = reader.getValidDate("\nEnter Journey Date (DD/MM/YYYY): ");
        String time   = reader.getValidTime("\nEnter Journey Time (HH:MM, 24-hour): ");
        int fromZone  = reader.getValidZone("\nEnter Departure Zone (1-5): ");
        int toZone    = reader.getValidZone("\nEnter Arrival Zone (1-5): ");

        LocalDateTime dateTime = parseDateTime(date, time);
        CityRideDataset.TimeBand timeBand = config.isPeak(dateTime)
                ? CityRideDataset.TimeBand.PEAK
                : CityRideDataset.TimeBand.OFF_PEAK;
        System.out.println("Time band automatically set to: " + timeBand);

        CityRideDataset.PassengerType passengerType = reader.getValidPassengerType(
                "\nEnter Passenger Type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

        BigDecimal baseFare       = config.getBaseFare(fromZone, toZone, timeBand);
        BigDecimal discount       = config.getDiscountRate(passengerType);
        BigDecimal discountedFare = BigDecimal.ONE.subtract(discount)
                .multiply(baseFare)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal dailyCap       = config.getDailyCap(passengerType);
        BigDecimal totalPaidToday = journeys.stream()
                .filter(j -> j.getDate().equals(date) && j.getPassengerType() == passengerType)
                .map(Journey::getChargedFare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = dailyCap.subtract(totalPaidToday);
        BigDecimal chargedFare;
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            chargedFare = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else if (discountedFare.compareTo(remaining) > 0) {
            chargedFare = remaining.setScale(2, RoundingMode.HALF_UP);
        } else {
            chargedFare = discountedFare;
        }

        journeyId++;
        journeys.add(new Journey(journeyId, date, time, fromZone, toZone,
                timeBand, passengerType, baseFare, chargedFare, discount));
        System.out.println("\nJourney added successfully! Time band: " + timeBand);
    }

    public void getJourneys() {
        if (journeys.isEmpty()) {
            System.out.println("No journeys recorded yet. Use option 1 to add a journey.");
            return;
        }
        System.out.println("\nJourney Number: " + journeys.size());
        for (Journey j : journeys) {
            j.displayJourneyDetails();
        }
    }

    public void editJourney() {
        if (journeys.isEmpty()) {
            System.out.println("No journeys recorded yet. Use option 1 to add a journey.");
            return;
        }

        int id = reader.getValidInt("\nEnter Journey ID to edit\n> ");
        Journey existing = null;
        int index = -1;

        for (int i = 0; i < journeys.size(); i++) {
            if (journeys.get(i).getId() == id) {
                existing = journeys.get(i);
                index = i;
                break;
            }
        }

        if (existing == null) {
            System.out.println("INPUT ERROR: No journey found with ID: " + id);
            return;
        }

        System.out.println("\nCurrent journey details:");
        existing.displayJourneyDetails();

        String date    = reader.getValidDate("\nEnter Journey Date (DD/MM/YYYY): ");
        String time    = reader.getValidTime("\nEnter Journey Time (HH:MM, 24-hour): ");
        int fromZone   = reader.getValidZone("\nEnter Departure Zone (1-5): ");
        int toZone     = reader.getValidZone("\nEnter Arrival Zone (1-5): ");

        LocalDateTime dateTime = parseDateTime(date, time);
        CityRideDataset.TimeBand timeBand = config.isPeak(dateTime)
                ? CityRideDataset.TimeBand.PEAK
                : CityRideDataset.TimeBand.OFF_PEAK;
        System.out.println("Time band recalculated to: " + timeBand);

        CityRideDataset.PassengerType passengerType = reader.getValidPassengerType(
                "\nEnter Passenger Type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

        BigDecimal baseFare       = config.getBaseFare(fromZone, toZone, timeBand);
        BigDecimal discount       = config.getDiscountRate(passengerType);
        BigDecimal discountedFare = BigDecimal.ONE.subtract(discount)
                .multiply(baseFare)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal dailyCap       = config.getDailyCap(passengerType);
        BigDecimal totalPaidToday = journeys.stream()
                .filter(j -> j.getDate().equals(date)
                        && j.getPassengerType() == passengerType
                        && j.getId() != id)
                .map(Journey::getChargedFare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = dailyCap.subtract(totalPaidToday);
        BigDecimal chargedFare;
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            chargedFare = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else if (discountedFare.compareTo(remaining) > 0) {
            chargedFare = remaining.setScale(2, RoundingMode.HALF_UP);
        } else {
            chargedFare = discountedFare;
        }

        journeys.set(index, new Journey(id, date, time, fromZone, toZone,
                timeBand, passengerType, baseFare, chargedFare, discount));
        System.out.println("\nJourney updated successfully!");
    }

    public void exportJourneys() {
        if (journeys.isEmpty()) {
            System.out.println("No journeys to export yet.");
            return;
        }
        String date = reader.getValidDate("\nEnter date to export (DD/MM/YYYY): ");
        ReportExporter.exportJourneysCsv(journeys, profile.getName(), date);
    }

    public void exportSummary() {
        if (journeys.isEmpty()) {
            System.out.println("No journeys to export yet.");
            return;
        }
        String date = reader.getValidDate("\nEnter date for summary export (DD/MM/YYYY): ");
        DailySummary summary = new DailySummary(journeys, date, profile.getName());
        ReportExporter.exportSummaryTxt(summary);
        ReportExporter.exportSummaryCsv(summary, journeys);
    }

    public void importJourneys() {
        List<String> rows = ReportExporter.importJourneysCsv();
        if (rows.isEmpty()) return;

        int imported = 0;
        int skipped  = 0;

        for (String row : rows) {
            try {
                String[] parts = row.split(",");
                if (parts.length < 5) {
                    System.out.println("IMPORT SKIP: Invalid row — " + row);
                    skipped++;
                    continue;
                }

                String date     = parts[0].trim();
                int fromZone    = Integer.parseInt(parts[1].trim());
                int toZone      = Integer.parseInt(parts[2].trim());
                String time     = parts[3].trim();
                CityRideDataset.PassengerType passengerType =
                        CityRideDataset.PassengerType.valueOf(parts[4].trim().toUpperCase());

                LocalDateTime dateTime = parseDateTime(date, time);
                CityRideDataset.TimeBand timeBand = config.isPeak(dateTime)
                        ? CityRideDataset.TimeBand.PEAK
                        : CityRideDataset.TimeBand.OFF_PEAK;

                BigDecimal baseFare       = config.getBaseFare(fromZone, toZone, timeBand);
                BigDecimal discount       = config.getDiscountRate(passengerType);
                BigDecimal discountedFare = BigDecimal.ONE.subtract(discount)
                        .multiply(baseFare)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal dailyCap       = config.getDailyCap(passengerType);
                BigDecimal totalPaidToday = journeys.stream()
                        .filter(j -> j.getDate().equals(date) && j.getPassengerType() == passengerType)
                        .map(Journey::getChargedFare)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal remaining = dailyCap.subtract(totalPaidToday);
                BigDecimal chargedFare;
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                    chargedFare = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                } else if (discountedFare.compareTo(remaining) > 0) {
                    chargedFare = remaining.setScale(2, RoundingMode.HALF_UP);
                } else {
                    chargedFare = discountedFare;
                }

                journeyId++;
                journeys.add(new Journey(journeyId, date, time, fromZone, toZone,
                        timeBand, passengerType, baseFare, chargedFare, discount));
                imported++;

            } catch (Exception e) {
                System.out.println("IMPORT SKIP: Could not parse row — " + row);
                skipped++;
            }
        }
        System.out.println("\nImport complete. Imported: " + imported + " | Skipped: " + skipped);
    }

    public void saveSession() {
        String path = FileManager.PROFILES_DIR +
                profile.getName().trim().toLowerCase().replace(" ", "_") +
                "_journeys.json";
        boolean saved = FileManager.writeJson(path, journeys);
        if (saved) {
            System.out.println("Session saved successfully.");
        }
    }

    public void loadSession() {
        String path = FileManager.PROFILES_DIR +
                profile.getName().trim().toLowerCase().replace(" ", "_") +
                "_journeys.json";
        if (!FileManager.fileExists(path)) {
            System.out.println("No saved session found for " + profile.getName() + ".");
            return;
        }
        com.google.gson.reflect.TypeToken<List<Journey>> token =
                new com.google.gson.reflect.TypeToken<List<Journey>>(){};
        List<Journey> loaded = FileManager.readJson(path, token);
        if (loaded != null) {
            journeys.addAll(loaded);
            journeyId = journeys.stream().mapToInt(Journey::getId).max().orElse(0);
            System.out.println("Session loaded. " + loaded.size() + " journeys restored.");
        }
    }

    public List<Journey> getJourneyList() {
        return journeys;
    }

    private LocalDateTime parseDateTime(String date, String time) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return LocalDateTime.parse(date + " " + time, formatter);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    public void removeJourney() {
        if (journeys.isEmpty()) {
            System.out.println("No journeys recorded yet. Use option 1 to add a journey.");
            return;
        }

        boolean done = false;

        while (!done) {
            int id = reader.getValidInt("\nEnter Journey ID you want to remove\n> ");

            if (id <= 0) {
                System.out.println("INPUT ERROR: Please enter a positive whole number.");
                continue;
            }

            int removeIndex = -1;
            for (int i = 0; i < journeys.size(); i++) {
                if (journeys.get(i).getId() == id) {
                    removeIndex = i;
                    break;
                }
            }

            if (removeIndex == -1) {
                System.out.println("INPUT ERROR: No journey found with ID: " + id);
                continue;
            }

            System.out.println("\nJourney found:");
            journeys.get(removeIndex).displayJourneyDetails();

            boolean confirmed = reader.getConfirmation("\nAre you sure you want to delete this journey? (y/n): ");
            if (confirmed) {
                journeys.remove(removeIndex);
                System.out.println("Journey deleted successfully.");
            } else {
                System.out.println("Delete cancelled.");
            }
            done = true;
        }
    }

    public void reset() {
        if (journeys.isEmpty()) {
            System.out.println("Nothing to reset. No journeys recorded yet.");
            return;
        }

        boolean confirmed = reader.getConfirmation("\nAre you sure you want to reset everything? This will delete all journeys (y/n): ");
        if (!confirmed) {
            System.out.println("Reset cancelled.");
            return;
        }

        journeys.clear();
        journeyId = 0;
        System.out.println("Reset complete. All journeys removed.");
    }

    public void filterJourneys() {
        if (journeys.isEmpty()) {
            System.out.println("No journeys recorded yet. Use option 1 to add a journey.");
            return;
        }

        boolean done = false;

        while (!done) {
            System.out.println("\n--- Filter Journeys ---");
            System.out.println("1. Passenger type");
            System.out.println("2. Time band (PEAK / OFF_PEAK)");
            System.out.println("3. Zone (from or to)");
            System.out.println("4. Date (DD/MM/YYYY)");
            System.out.println("5. Back");
            System.out.print("\n> ");

            Integer choice = reader.getMenuChoice();
            if (choice == null) {
                System.out.println("INPUT ERROR: Choose a number from 1 to 5.");
                continue;
            }

            switch (choice) {
                case 1 -> filterByPassengerType();
                case 2 -> filterByTimeBand();
                case 3 -> filterByZone();
                case 4 -> filterByDate();
                case 5 -> done = true;
                default -> System.out.println("INPUT ERROR: Choose a number from 1 to 5.");
            }
        }
    }

    private void filterByPassengerType() {
        CityRideDataset.PassengerType type = reader.getValidPassengerType("\nEnter Passenger Type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");
        int count = 0;
        for (Journey j : journeys) {
            if (j.getPassengerType() == type) {
                j.displayJourneyDetails();
                count++;
            }
        }
        if (count == 0) System.out.println("No journeys matched this passenger type.");
    }

    private void filterByTimeBand() {
        CityRideDataset.TimeBand band = reader.getValidTimeBand("\nEnter Time Band (PEAK/OFF_PEAK): ");
        int count = 0;
        for (Journey j : journeys) {
            if (j.getTimeBand() == band) {
                j.displayJourneyDetails();
                count++;
            }
        }
        if (count == 0) System.out.println("No journeys matched this time band.");
    }

    private void filterByZone() {
        int zone = reader.getValidZone("\nEnter Zone (1-5): ");

        Integer which = null;
        while (which == null) {
            System.out.println("\nSearch zone in:");
            System.out.println("1. From zone");
            System.out.println("2. To zone");
            System.out.println("3. Either (from OR to)");
            System.out.print("> ");

            which = reader.getMenuChoice();
            if (which == null || which < 1 || which > 3) {
                which = null;
                System.out.println("INPUT ERROR: Choose 1, 2, or 3.");
            }
        }

        int count = 0;
        for (Journey j : journeys) {
            boolean match = switch (which) {
                case 1 -> j.getFromZone() == zone;
                case 2 -> j.getToZone() == zone;
                default -> (j.getFromZone() == zone) || (j.getToZone() == zone);
            };
            if (match) {
                j.displayJourneyDetails();
                count++;
            }
        }
        if (count == 0) System.out.println("No journeys matched this zone filter.");
    }

    private void filterByDate() {
        String date = reader.getValidDate("\nEnter Date (DD/MM/YYYY): ");
        int count = 0;
        for (Journey j : journeys) {
            if (j.getDate().equals(date)) {
                j.displayJourneyDetails();
                count++;
            }
        }
        if (count == 0) System.out.println("No journeys matched this date.");
    }

    public void dailySummary() {
        if (journeys.isEmpty()) {
            System.out.println("No journeys recorded yet. Use option 1 to add a journey.");
            return;
        }
        String date = reader.getValidDate("\nEnter date for Daily Summary (DD/MM/YYYY): ");
        DailySummary summary = new DailySummary(journeys, date, profile.getName());
        summary.print();
    }
}
