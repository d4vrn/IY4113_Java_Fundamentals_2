import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DailySummary {

    private final String riderName;
    private final String date;
    private final ConfigManager config;

    private int totalJourneys;
    private BigDecimal totalCharged;
    private BigDecimal averageCost;
    private BigDecimal totalSavings;
    private Journey mostExpensiveJourney;

    private Map<CityRideDataset.PassengerType, TypeTotals> totalsByType;
    private long peakCount;
    private long offPeakCount;
    private Map<String, Integer> zonePairCounts;

    public DailySummary(List<Journey> journeys, String date, String riderName, ConfigManager config) {
        this.date       = date;
        this.riderName  = riderName;
        this.config = config;
        compute(journeys);
    }

    private void compute(List<Journey> journeys) {
        List<Journey> dayJourneys = journeys.stream()
                .filter(j -> j.getDate().equals(date))
                .toList();

        totalJourneys = dayJourneys.size();

        if (totalJourneys == 0) {
            totalCharged        = BigDecimal.ZERO;
            averageCost         = BigDecimal.ZERO;
            totalSavings        = BigDecimal.ZERO;
            mostExpensiveJourney = null;
            totalsByType        = new EnumMap<>(CityRideDataset.PassengerType.class);
            peakCount           = 0;
            offPeakCount        = 0;
            zonePairCounts      = new LinkedHashMap<>();
            return;
        }

        totalCharged = dayJourneys.stream()
                .map(Journey::getChargedFare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        averageCost = totalCharged
                .divide(BigDecimal.valueOf(totalJourneys), 2, RoundingMode.HALF_UP);

        mostExpensiveJourney = dayJourneys.stream()
                .max((a, b) -> a.getChargedFare().compareTo(b.getChargedFare()))
                .orElse(null);

        BigDecimal totalBase = dayJourneys.stream()
                .map(Journey::getBaseFare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalSavings = totalBase.subtract(totalCharged)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        totalsByType = new EnumMap<>(CityRideDataset.PassengerType.class);
        for (Journey j : dayJourneys) {
            totalsByType.putIfAbsent(j.getPassengerType(), new TypeTotals());
            totalsByType.get(j.getPassengerType()).add(j);
        }

        peakCount    = dayJourneys.stream()
                .filter(j -> j.getTimeBand() == CityRideDataset.TimeBand.PEAK)
                .count();
        offPeakCount = dayJourneys.stream()
                .filter(j -> j.getTimeBand() == CityRideDataset.TimeBand.OFF_PEAK)
                .count();

        zonePairCounts = new LinkedHashMap<>();
        for (Journey j : dayJourneys) {
            String key = j.getFromZone() + "->" + j.getToZone();
            zonePairCounts.put(key, zonePairCounts.getOrDefault(key, 0) + 1);
        }
    }

    public void print() {
        System.out.println(buildSummaryText());
    }

    public String buildSummaryText() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n========================================\n");
        sb.append("       CITYRIDE LITE — DAILY SUMMARY    \n");
        sb.append("========================================\n");
        sb.append("Rider : ").append(riderName).append("\n");
        sb.append("Date  : ").append(date).append("\n");

        if (totalJourneys == 0) {
            sb.append("\nNo journeys recorded for this date.\n");
            return sb.toString();
        }

        sb.append("\n--- Overview ---\n");
        sb.append("Total journeys       : ").append(totalJourneys).append("\n");
        sb.append("Total cost charged   : £").append(money(totalCharged)).append("\n");
        sb.append("Average cost         : £").append(money(averageCost)).append("\n");
        sb.append("Total savings (caps) : £").append(money(totalSavings)).append("\n");

        if (mostExpensiveJourney != null) {
            sb.append("Most expensive       : ID ").append(mostExpensiveJourney.getId())
                    .append(" (£").append(money(mostExpensiveJourney.getChargedFare())).append(")\n");
        }

        sb.append("\n--- Totals by Passenger Type ---\n");
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            TypeTotals t = totalsByType.get(type);
            if (t == null) continue;

            BigDecimal cap        = config.getDailyCap(type);
            boolean capReached    = t.chargedAfterCapSum.compareTo(cap) >= 0;

            sb.append(type)
                    .append(" | count=").append(t.count)
                    .append(" | pre-discount=£").append(money(t.preDiscountSum))
                    .append(" | discounted=£").append(money(t.discountedSumBeforeCap))
                    .append(" | charged=£").append(money(t.chargedAfterCapSum))
                    .append(" | cap reached=").append(capReached ? "YES" : "NO")
                    .append("\n");
        }

        sb.append("\n--- Category Counts ---\n");
        sb.append("Peak journeys    : ").append(peakCount).append("\n");
        sb.append("Off-peak journeys: ").append(offPeakCount).append("\n");

        sb.append("\nZone pair counts:\n");
        for (Map.Entry<String, Integer> e : zonePairCounts.entrySet()) {
            sb.append(e.getKey()).append(" : ").append(e.getValue()).append("\n");
        }

        sb.append("========================================\n");
        return sb.toString();
    }

    public ConfigManager getConfig()                                       { return config; }
    public String getRiderName()                                          { return riderName; }
    public String getDate()                                               { return date; }
    public int getTotalJourneys()                                         { return totalJourneys; }
    public BigDecimal getTotalCharged()                                   { return totalCharged; }
    public BigDecimal getAverageCost()                                    { return averageCost; }
    public BigDecimal getTotalSavings()                                   { return totalSavings; }
    public Journey getMostExpensiveJourney()                              { return mostExpensiveJourney; }
    public Map<CityRideDataset.PassengerType, TypeTotals> getTotalsByType() { return totalsByType; }
    public long getPeakCount()                                            { return peakCount; }
    public long getOffPeakCount()                                         { return offPeakCount; }
    public Map<String, Integer> getZonePairCounts()                       { return zonePairCounts; }

    private static String money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public static class TypeTotals {
        public int count                          = 0;
        public BigDecimal preDiscountSum          = BigDecimal.ZERO;
        public BigDecimal discountedSumBeforeCap  = BigDecimal.ZERO;
        public BigDecimal chargedAfterCapSum      = BigDecimal.ZERO;

        void add(Journey j) {
            count++;
            BigDecimal base       = j.getBaseFare();
            BigDecimal discount   = j.getDiscountApplied();
            BigDecimal discounted = (BigDecimal.ONE.subtract(discount))
                    .multiply(base)
                    .setScale(2, RoundingMode.HALF_UP);
            preDiscountSum         = preDiscountSum.add(base);
            discountedSumBeforeCap = discountedSumBeforeCap.add(discounted);
            chargedAfterCapSum     = chargedAfterCapSum.add(j.getChargedFare());
        }
    }
}