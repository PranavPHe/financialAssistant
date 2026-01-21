package src;

import java.util.Scanner;
import java.util.Currency;
import java.io.Console;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        App app = new App();
        try {
            app.run();
        } finally {
            app.close();
        }
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
        if (currencyCode == null || currencyCode.isEmpty())
            return null;
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

class App {
    private final String RESET = "\u001B[0m";
    private final String GREEN = "\u001B[1;32m";
    private final String RED = "\u001B[1;31m";
    private final String YELLOW = "\u001B[1;33m";

    private final Scanner input = new Scanner(System.in);
    private final Console console = System.console();

    private String username;
    private String preferredName;
    private String currencyCode;
    private double budgetAmount = 0.0;
    private final List<Expense> expenses = new ArrayList<>();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;

    public void run() {
        Main.printTitle(GREEN, RESET);

        int signOption = promptSignOption();

        if (signOption == 1) {
            Main.clearScreen();
            Main.printTitle(GREEN, RESET);
            registerUser();
        } else {
            System.out.println(GREEN + "Login flow not implemented yet." + RESET);
        }
    }

    private int promptSignOption() {
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
        return signOption;
    }

    public void registerUser() {
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

        this.username = username;
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
                if (confirmChars != null)
                    java.util.Arrays.fill(confirmChars, ' ');

                if (!match) {
                    System.out.println(RED + "Passwords do not match. Please try again." + RESET);
                    continue;
                }
                break;
            }

            System.out.println("Registered user: '" + username + "'");
        } finally {
            if (passwordChars != null)
                java.util.Arrays.fill(passwordChars, ' ');
        }

        Main.clearScreen();
        Main.printTitle(GREEN, RESET);

        System.out.println("Welcome, " + username + "! Let's set up your profile.\n");

        System.out.print("Please enter your preferred name: ");
        String preferredName = input.nextLine().trim();
        this.preferredName = preferredName;
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
        this.currencyCode = currency.getCurrencyCode();

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

        if (parsed) {
            System.out.println(YELLOW + "Current balance set to: " + fmt(amount) + RESET);
        } else {
            System.out.println(YELLOW + "Current balance set to: " + balance + " " + currency.getCurrencyCode() + RESET);
        }

        this.budgetAmount = parsed ? amount : 0.0;

        System.out.print("\n ");

        dashboardLoop();
    }

    private void dashboardLoop() {
        boolean running = true;
        while (running) {
            showDashboard();
            System.out.println("\nOptions:\n 1) Add expense\n 2) Update budget\n 3) View expenses\n 4) Exit");
            System.out.print("Select an option: ");
            String choice = input.nextLine().trim();
            switch (choice) {
                case "1":
                    addExpense();
                    break;
                case "2":
                    updateBudget();
                    break;
                case "3":
                    viewExpenses();
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println(RED + "Invalid option." + RESET);
            }
        }
        System.out.println(GREEN + "Exiting dashboard." + RESET);
    }

    private void showDashboard() {
        Main.clearScreen();
        Main.printTitle(GREEN, RESET);
        System.out.println("[DASHBOARD] - " + displayName());
        System.out.println("Currency: " + (currencyCode != null ? currencyCode : "N/A"));

        double totalExpenses = totalExpenses();
        double remaining = budgetAmount - totalExpenses;

        System.out.println("Budget: " + fmt(budgetAmount));
        System.out.println("Total expenses: " + fmt(totalExpenses));
        System.out.println("Remaining: " + fmt(remaining));

        printAnalytics(totalExpenses);
        printRecentExpenses(5);
    }

    private void printAnalytics(double totalExpenses) {
        double percentUsed = (budgetAmount > 0.0) ? (totalExpenses / budgetAmount) * 100.0 : 0.0;
        double avgExpense = expenses.isEmpty() ? 0.0 : totalExpenses / expenses.size();
        double highest = expenses.stream().mapToDouble(e -> e.amount).max().orElse(0.0);
        long daysTracked = daysTracked();
        double dailyBurn = (daysTracked <= 0) ? 0.0 : totalExpenses / daysTracked;

        System.out.println("\nAnalytics:");
        System.out.println(" - % of budget used: " + String.format("%.1f%%", percentUsed));
        System.out.println(" - Avg per expense: " + fmt(avgExpense));
        System.out.println(" - Daily burn (avg): " + fmt(dailyBurn) + (daysTracked > 0 ? " over " + daysTracked + " days" : ""));
        System.out.println(" - Largest expense: " + fmt(highest));
    }

    private void printRecentExpenses(int count) {
        System.out.println("\nRecent expenses:");
        if (expenses.isEmpty()) {
            System.out.println(" (no expenses yet)");
        } else {
            int show = Math.min(expenses.size(), count);
            for (int i = expenses.size() - show; i < expenses.size(); i++) {
                System.out.println(" - " + formatExpense(expenses.get(i)));
            }
        }
    }

    private void addExpense() {
        System.out.print("Expense description: ");
        String desc = input.nextLine().trim();
        double amt = promptDouble("Amount: ");
        expenses.add(new Expense(desc, amt, LocalDate.now().format(dateFmt)));
        System.out.println(YELLOW + "Expense added: " + desc + " - " + fmt(amt) + RESET);
    }

    private void updateBudget() {
        this.budgetAmount = promptDouble("Enter new budget amount: ");
        System.out.println(YELLOW + "Budget updated to: " + fmt(budgetAmount) + RESET);
    }

    private void viewExpenses() {
        System.out.println("\nAll expenses:");
        if (expenses.isEmpty()) {
            System.out.println(" (no expenses yet)");
        } else {
            for (int i = 0; i < expenses.size(); i++) {
                System.out.println((i + 1) + ") " + formatExpense(expenses.get(i)));
            }
        }
        pause();
    }

    private String displayName() {
        return (preferredName != null && !preferredName.isEmpty()) ? preferredName : username;
    }

    private double totalExpenses() {
        return expenses.stream().mapToDouble(e -> e.amount).sum();
    }

    private long daysTracked() {
        if (expenses.isEmpty()) return 0;
        LocalDate earliest = expenses.stream().map(e -> LocalDate.parse(e.date)).min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate latest = expenses.stream().map(e -> LocalDate.parse(e.date)).max(LocalDate::compareTo).orElse(LocalDate.now());
        long days = ChronoUnit.DAYS.between(earliest, latest) + 1;
        return Math.max(1, days);
    }

    private String fmt(double amount) {
        Locale loc = Main.findLocaleForCurrency(currencyCode);
        NumberFormat nf = (loc != null) ? NumberFormat.getNumberInstance(loc) : NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(amount) + " " + (currencyCode != null ? currencyCode : "");
    }

    private String formatExpense(Expense e) {
        return e.date + " | " + e.description + " : " + fmt(e.amount);
    }

    private void pause() {
        System.out.println("\nPress Enter to continue...");
        input.nextLine();
    }

    private double promptDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine().trim();
            try {
                NumberFormat parser = NumberFormat.getNumberInstance();
                Number n = parser.parse(line);
                return n.doubleValue();
            } catch (Exception ex) {
                System.out.println(RED + "Invalid number. Try again." + RESET);
            }
        }
    }

    private static class Expense {
        final String description;
        final double amount;
        final String date;

        Expense(String d, double a, String date) {
            this.description = d;
            this.amount = a;
            this.date = date;
        }
    }

    public void close() {
        try {
            input.close();
        } catch (Exception e) {
            // ignore
        }
    }
}
