import java.util.Scanner;

public class RoleSelector {

    public static void selectRole() {
    Scanner input = new Scanner(System.in);
    int menuChoice = 0;
    System.out.println("\n--- Welcome, CityRide Lite!!! ---");
    System.out.println("Your daily  travel companion\n");

    do {
        System.out.println("Select your role:");
        System.out.println("1.Rider");
        System.out.println("2.Admin");
        System.out.println("3.Exit");

        System.out.print("\n> ");
        String choiceText = input.nextLine().trim();
        try {
            menuChoice = Integer.parseInt(choiceText);
        } catch (NumberFormatException e) {
            System.out.println("INPUT ERROR: Choose a number from 1 to 3");
            continue;
        }

        switch (menuChoice) {
            case 1 -> new RiderMenu().run();
            case 2 -> new AdminMenu().run();
            case 3 -> System.out.println("\nExit.................");
            default ->
                System.out.println("INPUT ERROR: Choose a number from 1 to 3.");
        }
    } while (menuChoice != 3);
    input.close();
    }

}
