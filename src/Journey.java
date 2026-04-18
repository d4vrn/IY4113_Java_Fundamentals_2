import java.math.BigDecimal;
import java.math.RoundingMode;

public class Journey {
    private final int sessionId;
    private final String date;
    private final String time;
    private final int fromZone;
    private final int toZone;
    private final CityRideDataset.TimeBand timeBand;
    private final CityRideDataset.PassengerType passengerType;
    private final BigDecimal baseFare;
    private final BigDecimal discountApplied;
    private final BigDecimal chargedFare;

    public Journey(int sessionId, String date, String time, int fromZone, int toZone,
                   CityRideDataset.TimeBand timeBand, CityRideDataset.PassengerType passengerType,
                   BigDecimal baseFare, BigDecimal chargedFare, BigDecimal discountApplied) {
        this.sessionId = sessionId;
        this.date = date;
        this.time = time;
        this.fromZone = fromZone;
        this.toZone = toZone;
        this.timeBand = timeBand;
        this.passengerType = passengerType;
        this.baseFare = baseFare;
        this.chargedFare = chargedFare;
        this.discountApplied = discountApplied;
    }

    public int getId() { return sessionId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getFromZone() { return fromZone; }
    public int getToZone() { return toZone; }
    public CityRideDataset.TimeBand getTimeBand() { return timeBand; }
    public CityRideDataset.PassengerType getPassengerType() { return passengerType; }
    public BigDecimal getChargedFare() { return chargedFare; }
    public BigDecimal getBaseFare() { return baseFare; }
    public BigDecimal getDiscountApplied() { return discountApplied; }

    public BigDecimal getDiscountedFare() {
        return baseFare.multiply(BigDecimal.ONE.subtract(discountApplied))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public int getZonesCrossed() {
        return Math.abs(toZone - fromZone) + 1;
    }

    public void displayJourneyDetails() {
        System.out.println(
                "\nID: " + sessionId +
                        "\nDate: " + date +
                        "\nTime: " + time +
                        "\nDeparture Zone: " + fromZone +
                        "\nArrival Zone: " + toZone +
                        "\nTime Band: " + timeBand +
                        "\nPassenger Type: " + passengerType +
                        "\nBase fare: £" + baseFare +
                        "\nDiscount: " + discountApplied.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP) + "%" +
                        "\nCharged fare: £" + chargedFare
        );
    }
}
