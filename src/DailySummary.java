import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DailySummary {
    public static void printDailySummaryForDate(List<Journey> journeys, String date) {
        List<Journey> dayJourneys = journeys.stream()
                .filter(j -> j.getDate().equals(date))
                .toList();

        if (dayJourneys.isEmpty()) {
            System.out.println("No journeys found for " + date + ".");
            return;
        }

        int totalJourneys = dayJourneys.size();

        BigDecimal totalCharged = dayJourneys.stream()
                .map(Journey::getChargedFare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = totalCharged
                .divide(BigDecimal.valueOf(totalJourneys), 2, RoundingMode.HALF_UP);

        Journey mostExpensive = dayJourneys.stream()
                .max((a, b) -> a.getChargedFare().compareTo(b.getChargedFare()))
                .orElse(null);

        System.out.println("\n--- Daily Summary (" + date + ") ---");
        System.out.println("Total journeys (all types): " + totalJourneys);
        System.out.println("Total cost charged: £" + money(totalCharged));
        System.out.println("Average cost per journey: £" + money(average));
        if (mostExpensive != null) {
            System.out.println("Most expensive journey: ID " + mostExpensive.getId()
                    + " (£" + money(mostExpensive.getChargedFare()) + ")");
        }

        System.out.println("\n--- Totals by Passenger Type ---");

        Map<CityRideDataset.PassengerType, TypeTotals> totalsByType =
                new EnumMap<>(CityRideDataset.PassengerType.class);

        for (Journey j : dayJourneys) {
            totalsByType.putIfAbsent(j.getPassengerType(), new TypeTotals());
            totalsByType.get(j.getPassengerType()).add(j);
        }

        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            TypeTotals t = totalsByType.get(type);
            if (t == null) continue;

            BigDecimal cap = CityRideDataset.DAILY_CAP.get(type);
            boolean capReached = t.chargedAfterCapSum.compareTo(cap) >= 0;

            System.out.println(
                    type +
                            " | count=" + t.count +
                            " | pre-discount sum=£" + money(t.preDiscountSum) +
                            " | discounted sum(before cap)=£" + money(t.discountedSumBeforeCap) +
                            " | charged sum(after cap)=£" + money(t.chargedAfterCapSum) +
                            " | cap reached=" + (capReached ? "YES" : "NO")
            );
        }

        System.out.println("\n--- Category Counts ---");

        long peakCount    = dayJourneys.stream().filter(j -> j.getTimeBand() == CityRideDataset.TimeBand.PEAK).count();
        long offPeakCount = dayJourneys.stream().filter(j -> j.getTimeBand() == CityRideDataset.TimeBand.OFF_PEAK).count();

        System.out.println("Peak journeys: " + peakCount);
        System.out.println("Off-peak journeys: " + offPeakCount);

        Map<String, Integer> zonePairCounts = new LinkedHashMap<>();
        for (Journey j : dayJourneys) {
            String key = j.getFromZone() + "->" + j.getToZone();
            zonePairCounts.put(key, zonePairCounts.getOrDefault(key, 0) + 1);
        }

        System.out.println("\nZone pair counts:");
        for (var e : zonePairCounts.entrySet()) {
            System.out.println(e.getKey() + " : " + e.getValue());
        }
    }

    private static String money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private static class TypeTotals {
        int count = 0;
        BigDecimal preDiscountSum        = BigDecimal.ZERO;
        BigDecimal discountedSumBeforeCap = BigDecimal.ZERO;
        BigDecimal chargedAfterCapSum    = BigDecimal.ZERO;

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
