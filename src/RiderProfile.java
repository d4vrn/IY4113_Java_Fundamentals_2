public class RiderProfile {

    private String name;
    private CityRideDataset.PassengerType passengerType;
    private String defaultPayment;

    public static final String[] VALID_PAYMENT_OPTIONS = {"Cash", "Card", "Contactless"};

    public RiderProfile() {}

    public RiderProfile(String name, CityRideDataset.PassengerType passengerType, String defaultPayment) {
        this.name = name;
        this.passengerType = passengerType;
        this.defaultPayment = defaultPayment;
    }

    public String getName() {
        return name;
    }

    public CityRideDataset.PassengerType getPassengerType() {
        return passengerType;
    }

    public String getDefaultPayment() {
        return defaultPayment;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassengerType(CityRideDataset.PassengerType passengerType) {
        this.passengerType = passengerType;
    }

    public void setDefaultPayment(String defaultPayment) {
        this.defaultPayment = defaultPayment;
    }

    public boolean save() {
        String path = FileManager.PROFILES_DIR + toSafeFileName(this.name) + ".json";
        boolean saved = FileManager.writeJson(path, this);
        if (saved) {
            System.out.println("PROFILE SAVED: " + path);
        }
        return saved;
    }

    public static RiderProfile load(String name) {
        String path = FileManager.PROFILES_DIR + toSafeFileName(name) + ".json";
        if (!FileManager.fileExists(path)) {
            System.out.println("PROFILE NOT FOUND: No profile exists for '" + name + "'.");
            return null;
        }
        RiderProfile profile = FileManager.readJson(path, RiderProfile.class);
        if (profile != null) {
            System.out.println("PROFILE LOADED: Welcome back, " + profile.getName() + "!");
        }
        return profile;
    }

    public static boolean isValidPassengerType(String type) {
        try {
            CityRideDataset.PassengerType.valueOf(type.toUpperCase().replace(" ", "_"));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static CityRideDataset.PassengerType toPassengerType(String type) {
        return CityRideDataset.PassengerType.valueOf(type.toUpperCase().replace(" ", "_"));
    }

    public static boolean isValidPaymentOption(String payment) {
        for (String valid : VALID_PAYMENT_OPTIONS) {
            if (valid.equalsIgnoreCase(payment)) {
                return true;
            }
        }
        return false;
    }

    private static String toSafeFileName(String name) {
        return name.trim().toLowerCase().replace(" ", "_");
    }

    public String toString() {
        return  "Name           : " + name +
                "\nPassenger Type : " + passengerType +
                "\nDefault Payment: " + defaultPayment;
    }
}
