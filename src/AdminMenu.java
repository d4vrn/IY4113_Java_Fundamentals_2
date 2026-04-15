import java.util.Scanner;

public class AdminMenu {
    private static final String ADMIN_PASSWORD = "admin321";
    private Scanner input;
    private InputValidator reader;

    public AdminMenu() {
        input = new Scanner(System.in);
        reader = new InputValidator(input);
    }

    public void run() {
        int attempts = 0;
        int maxAttempts = 3;
        int menuChoice = 0;

        while (attempts < maxAttempts) {
            System.out.println("\nEnter password: ");
            System.out.print("> ");
            String passwordInput =  input.nextLine().trim();

            if (passwordInput.equals(ADMIN_PASSWORD)) {
                break;
            }

            attempts++;
            int remaining = maxAttempts - attempts;
            String word = remaining == 1 ? "attempt" : "attempts";
            System.out.println("Incorrect password. " + remaining + " " + word + " remaining.");
        }
        if (attempts == maxAttempts) {
            System.out.println("\nToo many failed attempts. Returning to main menu...");
            return;
        }

        System.out.println("\nWelcome, Admin!");

        do {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1.View config");
            System.out.println("2.Manage base fares");
            System.out.println("3.Manage discounts");
            System.out.println("4.Manage daily caps");
            System.out.println("5.Manage peak windows");
            System.out.println("6.Exit");

            System.out.print("\n> ");
            String choiceText = input.nextLine().trim();
            try {
                menuChoice = Integer.parseInt(choiceText);
            } catch (NumberFormatException e) {
                System.out.println("INPUT ERROR: Choose a number from 1 to 6.");
                continue;
            }

            switch (menuChoice) {
                case 1 -> System.out.println("You reached 'View config'");
                case 2 -> System.out.println("You reached 'Manage base fare'");
                case 3 -> System.out.println("You reached 'Manage discount'");
                case 4 -> System.out.println("You reached 'Manage daily caps'");
                case 5 -> System.out.println("You reached 'Manage peak windows'");
                case 6 -> System.out.println("\nReturning to the main menu...");
                default -> System.out.println("INPUT ERROR: Choose a number from 1 to 6.");
            }
        } while (menuChoice != 6);
        // removing input close. because, it is crashing. and closing only when exiting.
//        input.close();
    }
}
