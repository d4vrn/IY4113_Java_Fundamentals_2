import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportExporter {

    private static final String JOURNEY_CSV_HEADER =
            "ID,Date,FromZone,ToZone,TimeBand,PassengerType,ZonesCrossed," +
                    "BaseFare,DiscountApplied,DiscountedFare,ChargedFare";

    public static boolean exportJourneysCsv(List<Journey> journeys, String riderName, String date) {
        String path = buildExportPath(riderName, date, "csv");
        List<String> rows = new ArrayList<>();
        rows.add(JOURNEY_CSV_HEADER);
        for (Journey j : journeys) {
            if (j.getDate().equals(date)) {
                rows.add(journeyToCsvRow(j));
            }
        }
        boolean saved = FileManager.writeCsv(path, rows);
        if (saved) {
            System.out.println("EXPORT SUCCESS: Journeys saved to " + path);
        }
        return saved;
    }

    public static boolean exportSummaryTxt(DailySummary summary) {
        String path = buildExportPath(summary.getRiderName(), summary.getDate(), "txt");
        boolean saved = FileManager.writeText(path, summary.buildSummaryText());
        if (saved) {
            System.out.println("EXPORT SUCCESS: Summary saved to " + path);
        }
        return saved;
    }

    public static boolean exportSummaryCsv(DailySummary summary, List<Journey> journeys) {
        String path = buildExportPath(summary.getRiderName(), summary.getDate(), "summary.csv");
        List<String> rows = new ArrayList<>();

        rows.add("DAILY SUMMARY");
        rows.add("Rider," + summary.getRiderName());
        rows.add("Date," + summary.getDate());
        rows.add("Total Journeys," + summary.getTotalJourneys());
        rows.add("Total Charged,£" + summary.getTotalCharged());
        rows.add("Average Cost,£" + summary.getAverageCost());
        rows.add("Total Savings,£" + summary.getTotalSavings());

        if (summary.getMostExpensiveJourney() != null) {
            Journey me = summary.getMostExpensiveJourney();
            rows.add("Most Expensive Journey,ID " + me.getId() + " £" + me.getChargedFare());
        }

        rows.add("");
        rows.add("TOTALS BY PASSENGER TYPE");
        rows.add("Type,Count,Pre-Discount,Discounted,Charged,Cap Reached");
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            DailySummary.TypeTotals t = summary.getTotalsByType().get(type);
            if (t == null) continue;
            BigDecimal cap     = summary.getConfig().getDailyCap(type);
            boolean capReached = t.chargedAfterCapSum.compareTo(cap) >= 0;
            rows.add(type.name() + "," +
                    t.count + "," +
                    "£" + t.preDiscountSum + "," +
                    "£" + t.discountedSumBeforeCap + "," +
                    "£" + t.chargedAfterCapSum + "," +
                    (capReached ? "YES" : "NO"));
        }

        rows.add("");
        rows.add("CATEGORY COUNTS");
        rows.add("Peak Journeys," + summary.getPeakCount());
        rows.add("Off-Peak Journeys," + summary.getOffPeakCount());

        rows.add("");
        rows.add("ZONE PAIR COUNTS");
        rows.add("Zone Pair,Count");
        for (Map.Entry<String, Integer> e : summary.getZonePairCounts().entrySet()) {
            rows.add(e.getKey() + "," + e.getValue());
        }

        rows.add("");
        rows.add("JOURNEY LINE ITEMS");
        rows.add(JOURNEY_CSV_HEADER);
        for (Journey j : journeys) {
            rows.add(journeyToCsvRow(j));
        }

        boolean saved = FileManager.writeCsv(path, rows);
        if (saved) {
            System.out.println("EXPORT SUCCESS: Summary CSV saved to " + path);
        }
        return saved;
    }

    public static List<String> importJourneysCsv(String path) {
//        String path = FileManager.IMPORTS_DIR + "journeys.csv";
        List<String> allRows = FileManager.readCsv(path);
        if (allRows.isEmpty()) {
            System.out.println("IMPORT: No file found at " + path +
                    ". Place your CSV file there and try again.");
            return new ArrayList<>();
        }
        if (allRows.get(0).toLowerCase().startsWith("date")) {
            allRows.remove(0);
        }
        System.out.println("IMPORT: Found " + allRows.size() + " rows in " + path);
        return allRows;
    }

    private static String journeyToCsvRow(Journey j) {
        return j.getId() + "," +
                j.getDate() + "," +
                j.getFromZone() + "," +
                j.getToZone() + "," +
                j.getTimeBand() + "," +
                j.getPassengerType() + "," +
                j.getZonesCrossed() + "," +
                j.getBaseFare() + "," +
                j.getDiscountApplied() + "," +
                j.getDiscountedFare() + "," +
                j.getChargedFare();
    }

    private static String buildExportPath(String riderName, String date, String extension) {
        String safeName = riderName.trim().toLowerCase().replace(" ", "_");
        String safeDate = date.replace("/", "-");
        return FileManager.EXPORTS_DIR + safeDate + "_" + safeName + "." + extension;
    }
}