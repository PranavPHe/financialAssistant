package src;

import java.util.*;
import java.io.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Main {
    // All code is contained within the app
    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    // Flushes the screen on Unix and Windows terminals
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // Prints the ASCII art title with colors
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

    // Finds a Locale that uses the given currency code
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
}

class App {
    private static final String DATA_DIR = "data";
    private final String RESET = "\u001B[0m";
    private final String GREEN = "\u001B[1;32m";
    private final String RED = "\u001B[1;31m";
    private final String YELLOW = "\u001B[1;33m";

    private final Scanner input = new Scanner(System.in);
    private final Console console = System.console();

    private String username;
    private String password;
    private String preferredName;
    private String currencyCode;
    private double budgetAmount = 0.0;
    private final List<Expense> expenses = new ArrayList<>();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;

    // Runs the main application loop (Clear screen -> Title -> Sign in/register -> Dashboard)
    public void run() {
        new File(DATA_DIR).mkdirs();
        Main.printTitle(GREEN, RESET);

        int signOption = promptSignOption();

        Main.clearScreen();
        Main.printTitle(GREEN, RESET);

        if (signOption == 1) {
            if (registerUser()) dashboardLoop();
        } else {
            if (loginUser()) dashboardLoop();
        }
    }

    // Prompts the user to select sign-in or register option
    private int promptSignOption() {
        int signOption;
        do {
            System.out.print(GREEN + "\n1. Register \n2. Login" + RESET + "\nPlease select an option: ");
            while (!input.hasNextInt()) {
                System.out.println(RED + "Please enter a number (1 or 2)." + RESET);
                input.next();
                System.out.print("Please select an option: ");
            }
            signOption = input.nextInt();
            if (signOption != 1 && signOption != 2)
                System.out.println(RED + "Invalid option. Please select either 1 or 2." + RESET);
        } while (signOption != 1 && signOption != 2);
        input.nextLine();
        return signOption;
    }

    // Attempts to log in a user by verifying username and password
    private boolean loginUser() {
        System.out.print("Username: ");
        String user = input.nextLine().trim().toLowerCase().replaceAll("\\s+", "");

        // Check if user file exists
        File userFile = new File(DATA_DIR, user + ".txt");
        if (!userFile.exists()) {
            System.out.println(RED + "User not found." + RESET);
            return false;
        }


        // Attempt to verify password and load user data
        for (int tries = 0; tries < 3; tries++) {
            String pass = readPassword("Password: ");
            try (BufferedReader r = new BufferedReader(new FileReader(userFile))) {
                String storedPass = r.readLine();
                if (pass.equals(storedPass)) {
                    this.username = user;
                    this.password = pass;
                    this.preferredName = r.readLine();
                    this.currencyCode = r.readLine();
                    this.budgetAmount = Double.parseDouble(r.readLine());
                    String line;

                    // Load expenses from file
                    while ((line = r.readLine()) != null) {
                        String[] parts = line.split("\\|", 3);
                        if (parts.length == 3)
                            expenses.add(new Expense(parts[1], Double.parseDouble(parts[2]), parts[0]));
                    }
                    System.out.println(GREEN + "Welcome back, " + displayName() + "!" + RESET);
                    return true;
                }
                System.out.println(RED + "Incorrect password. " + (2 - tries) + " tries left." + RESET);
            } catch (Exception e) {
                System.out.println(RED + "Error loading data." + RESET);
                return false;
            }
        }
        System.out.println(RED + "Too many failed attempts." + RESET);
        return false;
    }

    // Registers a new user by collecting username, password, and profile info
    public boolean registerUser() {
        String user;
        while (true) {
            System.out.print("Choose a username: ");
            user = input.nextLine().trim().toLowerCase();

            if (user.isEmpty()) {
                System.out.println(RED + "Username cannot be empty. Try again." + RESET);
                continue;
            }

            if (user.contains(" ")) {
                String suggested = user.replaceAll("\\s+", "");
                System.out.print(YELLOW + "Usernames may not contain spaces. Use '" + suggested + "' instead? (Y/N): " + RESET);
                String resp = input.nextLine().trim();
                if (resp.equalsIgnoreCase("Y") || resp.equalsIgnoreCase("YES")) {
                    user = suggested;
                    break;
                } else {
                    continue;
                }
            }
            break;
        }

        // Check if username already exists
        if (new File(DATA_DIR, user + ".txt").exists()) {
            System.out.println(RED + "Username already exists." + RESET);
            return false;
        }

        // Prompt for password and confirmation (not visible input for security)
        String pass = readPassword("\nChoose a password: ");
        String confirm = readPassword("Confirm password: ");
        if (!pass.equals(confirm)) {
            System.out.println(RED + "Passwords do not match." + RESET);
            return false;
        }

        this.username = user;
        this.password = pass;
        System.out.println(GREEN + "Registered: " + user + RESET);

        Main.clearScreen();
        Main.printTitle(GREEN, RESET);
        System.out.println("Welcome, " + user + "! Let's set up your profile.\n");

        System.out.print("Preferred name: ");
        this.preferredName = input.nextLine().trim();

        // Prompt for currency code and validate
        while (true) {
            System.out.print("Currency code (e.g., USD, EUR): ");
            String code = input.nextLine().trim().toUpperCase();
            try {
                Currency.getInstance(code);
                this.currencyCode = code;
                break;
            } catch (Exception e) {
                System.out.println(RED + "Invalid currency code." + RESET);
            }
        }

        this.budgetAmount = promptDouble("Starting budget: ");
        saveData();
        return true;
    }

    // Reads a password from the console
    private String readPassword(String prompt) {
        if (console != null) {
            char[] chars = console.readPassword(prompt);
            return chars != null ? new String(chars) : "";
        }
        System.out.print(prompt);
        return input.nextLine();
    }

    // Main dashboard loop for user interaction
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

    // Displays the main dashboard with budget and expense summary
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

    // Displays analytics about expenses and budget usage
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

    // Displays a list of recent expenses
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

    // Adds a new expense entry
    private void addExpense() {
        System.out.print("Expense description: ");
        String desc = input.nextLine().trim();
        double amt = promptDouble("Amount: ");
        expenses.add(new Expense(desc, amt, LocalDate.now().format(dateFmt)));
        System.out.println(YELLOW + "Expense added: " + desc + " - " + fmt(amt) + RESET);
        saveData();
    }

    // Updates the budget amount
    private void updateBudget() {
        this.budgetAmount = promptDouble("Enter new budget amount: ");
        System.out.println(YELLOW + "Budget updated to: " + fmt(budgetAmount) + RESET);
        saveData();
    }

    // Displays all recorded expenses
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

    // Returns the display name (preferred name or username)
    private String displayName() {
        return (preferredName != null && !preferredName.isEmpty()) ? preferredName : username;
    }

    // Calculates the total amount of all expenses
    private double totalExpenses() {
        return expenses.stream().mapToDouble(e -> e.amount).sum();
    }

    // Calculates the number of days expenses have been tracked
    private long daysTracked() {
        if (expenses.isEmpty()) return 0;
        LocalDate earliest = expenses.stream().map(e -> LocalDate.parse(e.date)).min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate latest = expenses.stream().map(e -> LocalDate.parse(e.date)).max(LocalDate::compareTo).orElse(LocalDate.now());
        long days = ChronoUnit.DAYS.between(earliest, latest) + 1;
        return Math.max(1, days);
    }

    // Formats a double amount into a currency string based on the current currency code
    private String fmt(double amount) {
        Locale loc = Main.findLocaleForCurrency(currencyCode);
        NumberFormat nf = (loc != null) ? NumberFormat.getNumberInstance(loc) : NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(amount) + " " + (currencyCode != null ? currencyCode : "");
    }

    // Formats an expense entry for display
    private String formatExpense(Expense e) {
        return e.date + " | " + e.description + " : " + fmt(e.amount);
    }

    // Pauses execution until the user presses Enter
    private void pause() {
        System.out.println("\nPress Enter to continue...");
        input.nextLine();
    }

    // Prompts the user to enter a double value with validation
    private double promptDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine().trim();
            try {
                return Double.parseDouble(line.replace(",", ""));
            } catch (Exception ex) {
                System.out.println(RED + "Invalid number." + RESET);
            }
        }
    }

    // Saves user data to a file
    private void saveData() {
        try (PrintWriter writer = new PrintWriter(new File(DATA_DIR, username + ".txt"))) {
            writer.println(password);
            writer.println(preferredName);
            writer.println(currencyCode);
            writer.println(budgetAmount);
            for (Expense e : expenses)
                writer.println(e.date + "|" + e.description + "|" + e.amount);
        } catch (Exception e) {
            System.out.println(RED + "Error saving data." + RESET);
        }
    }
    
    // Expense struct
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
}
