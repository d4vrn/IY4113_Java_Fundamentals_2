import java.util.Scanner;

public class RiderMenu {
        private Scanner input;
        private JourneyManager journeyManager;

        public RiderMenu() {
            input = new Scanner(System.in);
            journeyManager = new JourneyManager(input);
        }


    public void run() {
        int menuChoice = 0;

        do {
            System.out.print("\n");
            System.out.println("1.Add Journey");
            System.out.println("2.Listing Journey");
            System.out.println("3.Filter Journey");
            System.out.println("4.Daily Summary");
            System.out.println("5.Remove Journey");
            System.out.println("6.Reset");
            System.out.println("7.Exit");

            System.out.print("\n> ");
            String choiceText = input.nextLine().trim();
            try {
                menuChoice = Integer.parseInt(choiceText);
            } catch (NumberFormatException e) {
                System.out.println("INPUT ERROR: Choose a number from 1 to 7.");
                continue;
            }

            switch (menuChoice) {
                case 1: journeyManager.addJourney();    break;
                case 2: journeyManager.getJourneys();   break;
                case 3: journeyManager.filterJourneys(); break;
                case 4: journeyManager.dailySummary();  break;
                case 5: journeyManager.removeJourney(); break;
                case 6: journeyManager.reset();         break;
                case 7: System.out.print("\nExit............"); break;
                default: System.out.println("INPUT ERROR: Choose a number from 1 to 7.");
            }

        } while (menuChoice != 7);
        input.close();
    }
}