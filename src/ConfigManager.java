import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private String peakMorningStart;
    private String peakMorningEnd;
    private String peakEveningStart;
    private String peakEveningEnd;

    private Map<String, String> baseFares;

    private Map<String, String> discountRates;

    private Map<String, String> dailyCaps;

    private static final String DEFAULT_PEAK_MORNING_START = "07:00";
    private static final String DEFAULT_PEAK_MORNING_END   = "09:30";
    private static final String DEFAULT_PEAK_EVENING_START = "16:00";
    private static final String DEFAULT_PEAK_EVENING_END   = "19:00";

    private ConfigManager() {}

    public static ConfigManager load() {
        if (FileManager.fileExists(FileManager.CONFIG_FILE)) {
            ConfigManager config = FileManager.readJson(FileManager.CONFIG_FILE, ConfigManager.class);
            if (config != null) {
                System.out.println("CONFIG: Loaded from " + FileManager.CONFIG_FILE);
                return config;
            }
        }

        System.out.println("CONFIG: No config file found — loading defaults from CityRideDataset.");
        ConfigManager config = buildFromDefaults();
        config.save(); // save defaults so config.json exists for next run
        return config;
    }


    public boolean save() {
        boolean saved = FileManager.writeJson(FileManager.CONFIG_FILE, this);
        if (saved) {
            System.out.println("CONFIG SAVED: " + FileManager.CONFIG_FILE);
        }
        return saved;
    }


    private static ConfigManager buildFromDefaults() {
        ConfigManager config = new ConfigManager();

        config.peakMorningStart = DEFAULT_PEAK_MORNING_START;
        config.peakMorningEnd   = DEFAULT_PEAK_MORNING_END;
        config.peakEveningStart = DEFAULT_PEAK_EVENING_START;
        config.peakEveningEnd   = DEFAULT_PEAK_EVENING_END;

        config.baseFares = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : CityRideDataset.BASE_FARE.entrySet()) {
            config.baseFares.put(entry.getKey(), entry.getValue().toPlainString());
        }

        config.discountRates = new HashMap<>();
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            config.discountRates.put(
                    type.name(),
                    CityRideDataset.DISCOUNT_RATE.get(type).toPlainString()
            );
        }

        config.dailyCaps = new HashMap<>();
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            config.dailyCaps.put(
                    type.name(),
                    CityRideDataset.DAILY_CAP.get(type).toPlainString()
            );
        }

        return config;
    }

    public boolean isPeak(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }

        LocalTime time          = dateTime.toLocalTime();
        LocalTime morningStart  = LocalTime.parse(peakMorningStart);
        LocalTime morningEnd    = LocalTime.parse(peakMorningEnd);
        LocalTime eveningStart  = LocalTime.parse(peakEveningStart);
        LocalTime eveningEnd    = LocalTime.parse(peakEveningEnd);

        boolean inMorningPeak = !time.isBefore(morningStart) && !time.isAfter(morningEnd);
        boolean inEveningPeak = !time.isBefore(eveningStart) && !time.isAfter(eveningEnd);

        return inMorningPeak || inEveningPeak;
    }


    public BigDecimal getBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand timeBand) {
        String key   = CityRideDataset.key(fromZone, toZone, timeBand);
        String value = baseFares.get(key);
        return value != null ? new BigDecimal(value).setScale(2, RoundingMode.HALF_UP) : null;
    }


    public BigDecimal getDiscountRate(CityRideDataset.PassengerType type) {
        String value = discountRates.get(type.name());
        return value != null ? new BigDecimal(value) : BigDecimal.ZERO;
    }


    public BigDecimal getDailyCap(CityRideDataset.PassengerType type) {
        String value = dailyCaps.get(type.name());
        return value != null ? new BigDecimal(value).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }


    public boolean updateBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand timeBand, BigDecimal fare) {
        if (!isValidFare(fare)) {
            System.out.println("VALIDATION ERROR: Fare must be greater than zero.");
            return false;
        }
        if (!isValidZone(fromZone) || !isValidZone(toZone)) {
            System.out.println("VALIDATION ERROR: Zones must be between " +
                    CityRideDataset.MIN_ZONE + " and " + CityRideDataset.MAX_ZONE + ".");
            return false;
        }
        String key = CityRideDataset.key(fromZone, toZone, timeBand);
        baseFares.put(key, fare.setScale(2, RoundingMode.HALF_UP).toPlainString());
        System.out.println("BASE FARE UPDATED: " + key + " → £" + fare);
        return save();
    }


    public boolean updateDiscountRate(CityRideDataset.PassengerType type, BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            System.out.println("VALIDATION ERROR: Discount rate must be between 0.00 and 1.00.");
            return false;
        }
        discountRates.put(type.name(), rate.toPlainString());
        System.out.println("DISCOUNT UPDATED: " + type.name() + " → " + rate);
        return save();
    }


    public boolean updateDailyCap(CityRideDataset.PassengerType type, BigDecimal cap) {
        if (!isValidFare(cap)) {
            System.out.println("VALIDATION ERROR: Daily cap must be greater than zero.");
            return false;
        }
        dailyCaps.put(type.name(), cap.setScale(2, RoundingMode.HALF_UP).toPlainString());
        System.out.println("DAILY CAP UPDATED: " + type.name() + " → £" + cap);
        return save();
    }

    public boolean updatePeakWindows(String morningStart, String morningEnd,
                                     String eveningStart, String eveningEnd) {
        if (!isValidTime(morningStart) || !isValidTime(morningEnd) ||
                !isValidTime(eveningStart) || !isValidTime(eveningEnd)) {
            System.out.println("VALIDATION ERROR: Times must be in HH:mm format e.g. 07:00.");
            return false;
        }

        LocalTime mStart = LocalTime.parse(morningStart);
        LocalTime mEnd   = LocalTime.parse(morningEnd);
        LocalTime eStart = LocalTime.parse(eveningStart);
        LocalTime eEnd   = LocalTime.parse(eveningEnd);

        if (!mStart.isBefore(mEnd)) {
            System.out.println("VALIDATION ERROR: Morning start must be before morning end.");
            return false;
        }
        if (!eStart.isBefore(eEnd)) {
            System.out.println("VALIDATION ERROR: Evening start must be before evening end.");
            return false;
        }
        if (!mEnd.isBefore(eStart)) {
            System.out.println("VALIDATION ERROR: Morning window must end before evening window starts.");
            return false;
        }

        this.peakMorningStart = morningStart;
        this.peakMorningEnd   = morningEnd;
        this.peakEveningStart = eveningStart;
        this.peakEveningEnd   = eveningEnd;

        System.out.println("PEAK WINDOWS UPDATED: Morning " + morningStart + "-" + morningEnd +
                " / Evening " + eveningStart + "-" + eveningEnd);
        return save();
    }


    public void displayConfig() {
        System.out.println("\n========================================");
        System.out.println("        ACTIVE SYSTEM CONFIGURATION      ");
        System.out.println("========================================");

        System.out.println("\n--- Peak Windows ---");
        System.out.println("Morning : " + peakMorningStart + " - " + peakMorningEnd);
        System.out.println("Evening : " + peakEveningStart + " - " + peakEveningEnd);
        System.out.println("Peak days: Monday - Friday (weekends always off-peak)");

        System.out.println("\n--- Daily Caps ---");
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            System.out.printf("%-15s £%s%n", type.name(), dailyCaps.get(type.name()));
        }

        System.out.println("\n--- Discount Rates ---");
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            BigDecimal rate = new BigDecimal(discountRates.get(type.name()))
                    .multiply(new BigDecimal("100"));
            System.out.printf("%-15s %s%%%n", type.name(), rate.stripTrailingZeros().toPlainString());
        }

        System.out.println("\n--- Base Fares ---");
        System.out.printf("%-20s %-10s %-10s%n", "Route", "Peak", "Off-Peak");
        System.out.println("--------------------------------------------");
        for (int from = CityRideDataset.MIN_ZONE; from <= CityRideDataset.MAX_ZONE; from++) {
            for (int to = CityRideDataset.MIN_ZONE; to <= CityRideDataset.MAX_ZONE; to++) {
                String peakKey    = CityRideDataset.key(from, to, CityRideDataset.TimeBand.PEAK);
                String offPeakKey = CityRideDataset.key(from, to, CityRideDataset.TimeBand.OFF_PEAK);
                System.out.printf("Zone %d → Zone %d       £%-9s £%s%n",
                        from, to,
                        baseFares.getOrDefault(peakKey, "N/A"),
                        baseFares.getOrDefault(offPeakKey, "N/A"));
            }
        }
        System.out.println("========================================\n");
    }

    private boolean isValidFare(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }


    private boolean isValidZone(int zone) {
        return zone >= CityRideDataset.MIN_ZONE && zone <= CityRideDataset.MAX_ZONE;
    }


    private boolean isValidTime(String time) {
        try {
            LocalTime.parse(time);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public String getPeakMorningStart() { return peakMorningStart; }
    public String getPeakMorningEnd()   { return peakMorningEnd; }
    public String getPeakEveningStart() { return peakEveningStart; }
    public String getPeakEveningEnd()   { return peakEveningEnd; }
}