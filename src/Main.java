package src;

import java.util.Scanner;
import java.util.Currency;
import java.io.Console;
import java.text.NumberFormat;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        // Color Palette
        final String RESET = "\u001B[0m";
        final String GREEN = "\u001B[1;32m";
        final String RED = "\u001B[1;31m";
        final String YELLOW = "\u001B[1;33m";

        printTitle(GREEN, RESET);

        Scanner input = new Scanner(System.in);
        Console console = System.console();

        // Register or Login?
        int signOption;
        do {
            System.out.print(
                    GREEN + "\n1. Register \n2. Login" + RESET +
                            "\nPlease select an option: ");
            while (!input.hasNextInt()) {
                System.out.println(RED + "Please enter a number (1 or 2)." + RESET);
                input.next();
                System.out.print("Please select an option: ");
            }
            signOption = input.nextInt();

            if (signOption != 1 && signOption != 2) {
                System.out.println(RED + "Invalid option. Please select either 1 or 2." + RESET);
            }

        } while (signOption != 1 && signOption != 2);

        input.nextLine(); 

        if (signOption == 1) {
            clearScreen();
            printTitle(GREEN, RESET);
            registerUser(input, YELLOW, GREEN, RESET, RED, console);
        } else {
        }

        input.close();
    }

    public static void registerUser(
            Scanner input,
            String YELLOW,
            String GREEN,
            String RESET,
            String RED,
            Console console) {

        printDashboard();

        // Username
        String username;
        while (true) {
            System.out.print("Please enter a username: ");
            username = input.nextLine().trim();

            if (username.isEmpty()) {
                System.out.println("Username cannot be empty. Try again.");
                continue;
            }

            if (username.contains(" ")) {
                String suggestedUser = username.replaceAll("\\s", "");
                System.out.print(
                        YELLOW + "Usernames may not contain spaces. Use '" +
                                suggestedUser + "' instead? (Y/N): " + RESET);
                String resp = input.nextLine().trim();
                if (resp.equalsIgnoreCase("Y") || resp.equalsIgnoreCase("YES")) {
                    username = suggestedUser;
                    break;
                } else {
                    continue;
                }
            }
            break;
        }

        char[] passwordChars = null;
        try {
            while (true) {
                if (console != null) {
                    passwordChars = console.readPassword("\nEnter password: ");
                } else {
                    System.out.print("\nEnter password: ");
                    passwordChars = input.nextLine().toCharArray();
                }

                if (passwordChars == null || passwordChars.length == 0) {
                    System.out.println(RED + "Password cannot be empty. Try again." + RESET);
                    continue;
                }

                char[] confirmChars;
                if (console != null) {
                    confirmChars = console.readPassword("Confirm password: ");
                } else {
                    System.out.print("Confirm password: ");
                    confirmChars = input.nextLine().toCharArray();
                }

                boolean match = java.util.Arrays.equals(passwordChars, confirmChars);
                if (confirmChars != null) java.util.Arrays.fill(confirmChars, ' ');

                if (!match) {
                    System.out.println(RED + "Passwords do not match. Please try again." + RESET);
                    continue;
                }
                break;
            }

            System.out.println("Registered user: '" + username + "'");
        } finally {
            if (passwordChars != null) java.util.Arrays.fill(passwordChars, ' ');
        }

        clearScreen();
        printTitle(GREEN, RESET);

        System.out.println("Welcome, " + username + "! Let's set up your profile.\n");

        System.out.print("Please enter your preferred name: ");
        String preferredName = input.nextLine().trim();
        System.out.println(YELLOW + "Preferred name set to: " + preferredName + RESET);

        Currency currency = null;
        String currencyType;

        while (true) {
            System.out.print("\nPlease enter your currency code (e.g., USD, EUR): ");
            currencyType = input.nextLine().trim().toUpperCase();

            if (currencyType.isEmpty()) {
                System.out.println(RED + "Currency code cannot be empty. Try again." + RESET);
                continue;
            }

            try {
                currency = Currency.getInstance(currencyType);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(RED + "Currency code '" + currencyType + "' not found. Please try again." + RESET);
            }
        }

        System.out.println(YELLOW + "Default currency set to: " + currency.getCurrencyCode() + RESET);

        System.out.print("\nPlease enter your current balance: ");
        String balance = input.nextLine().trim();

        double amount = 0d;
        boolean parsed = false;
        try {
            NumberFormat parser = NumberFormat.getNumberInstance();
            Number num = parser.parse(balance);
            amount = num.doubleValue();
            parsed = true;
        } catch (Exception e) {
            parsed = false;
        }

        Locale formatLocale = findLocaleForCurrency(currency.getCurrencyCode());

        if (parsed) {
            NumberFormat numberFormatter = NumberFormat.getNumberInstance(formatLocale != null ? formatLocale : Locale.getDefault());
            numberFormatter.setMinimumFractionDigits(2);
            numberFormatter.setMaximumFractionDigits(2);
            String formattedNumber = numberFormatter.format(amount);
            System.out.println(YELLOW + "Current balance set to: " + formattedNumber + " " + currency.getCurrencyCode() + RESET);
        } else {
            System.out.println(YELLOW + "Current balance set to: " + balance + " " + currency.getCurrencyCode() + RESET);
        }

        System.out.print("\n ");

        printDashboard();


    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printTitle(String COLOR, String RESET) {
        String title = """


                    /$$$$$$$$ /$$                                                          /$$$$$$                     /$$             /$$                           /$$
                    | $$_____/|__/                                                         /$$__  $$                   |__/            | $$                          | $$
                    | $$       /$$ /$$$$$$$   /$$$$$$  /$$$$$$$   /$$$$$$$  /$$$$$$       | $$  \\ $$  /$$$$$$$ /$$$$$$$ /$$  /$$$$$$$ /$$$$$$    /$$$$$$  /$$$$$$$  /$$$$$$
                    | $$$$$   | $$| $$__  $$ |____  $$| $$__  $$ /$$_____/ /$$__  $$      | $$$$$$$$ /$$_____//$$_____/| $$ /$$_____/|_  $$_/   |____  $$| $$__  $$|_  $$_/
                    | $$__/   | $$| $$  \\ $$  /$$$$$$$| $$  \\ $$| $$      | $$$$$$$$      | $$__  $$|  $$$$$$|  $$$$$$ | $$|  $$$$$$   | $$      /$$$$$$$| $$  \\ $$  | $$
                    | $$      | $$| $$  | $$ /$$__  $$| $$  | $$| $$      | $$_____/      | $$  | $$ \\____  $$\\____  $$| $$ \\____  $$  | $$ /$$ /$$__  $$| $$  | $$  | $$ /$$
                    | $$      | $$| $$  | $$|  $$$$$$$| $$  | $$|  $$$$$$$|  $$$$$$$      | $$  | $$ /$$$$$$$//$$$$$$$/| $$ /$$$$$$$/  |  $$$$/|  $$$$$$$| $$  | $$  |  $$$$/
                    |__/      |__/|__/  |__/ \\_______/|__/  |__/ \\_______/ \\_______/      |__/  |__/|_______/|_______/ |__/|_______/    \\___/   \\_______/|__/  |__/   \\___/


        """;

        System.out.println(COLOR + title + RESET);
    }

    public static Locale findLocaleForCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isEmpty()) return null;
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                Currency c = Currency.getInstance(locale);
                if (c != null && currencyCode.equalsIgnoreCase(c.getCurrencyCode())) {
                    return locale;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static void printDashboard() {
        System.out.println("[DASHBOARD]");
    }
}