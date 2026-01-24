package src;

import java.util.*;
import java.io.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Main entry point for the Finance Assistant application.
 * This class contains utility methods for terminal operations and
 * delegates the main application logic to the {@link App} class.
 */
public class Main {
    
    /**
     * Application entry point. Creates and runs the Finance Assistant app.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    /**
     * Clears the terminal screen using ANSI escape codes.
     * Works on both Unix and Windows terminals that support ANSI sequences.
     * Uses escape sequence \033[H to move cursor to home position and
     * \033[2J to clear the entire screen.
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Prints the ASCII art title banner with specified colors.
     * 
     * @param COLOR the ANSI color code to apply to the title
     * @param RESET the ANSI reset code to restore default terminal colors
     */
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

    /**
     * Finds a Locale that uses the specified currency code.
     * Iterates through all available locales to find one that matches
     * the given currency code (case-insensitive comparison).
     * 
     * @param currencyCode the ISO 4217 currency code (e.g., "USD", "EUR")
     * @return a Locale that uses the specified currency, or null if not found
     *         or if currencyCode is null/empty
     */
    public static Locale findLocaleForCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isEmpty())
            return null;
        
        // Iterate through all system locales to find a matching currency
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                Currency c = Currency.getInstance(locale);
                if (c != null && currencyCode.equalsIgnoreCase(c.getCurrencyCode())) {
                    return locale;
                }
            } catch (Exception e) {
                // Some locales don't have associated currencies - skip them
            }
        }
        return null;
    }
}

/**
 * Core application class for the Finance Assistant.
 * Handles user authentication, expense tracking, budget management,
 * and data persistence. User data is stored in text files in the data directory.
 */
class App {
    /** Directory where user data files are stored */
    private static final String DATA_DIR = "data";
    
    // ANSI color codes for terminal output formatting
    private final String RESET = "\u001B[0m";
    private final String GREEN = "\u001B[1;32m";
    private final String RED = "\u001B[1;31m";
    private final String YELLOW = "\u001B[1;33m";

    private final Scanner input = new Scanner(System.in);
    private final Console console = System.console();

    // User profile data
    private String username;
    private String password;
    private String preferredName;
    private String currencyCode;
    private double budgetAmount = 0.0;
    
    // Expense tracking
    private final List<Expense> expenses = new ArrayList<>();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Runs the main application loop.
     * Flow: Initialize data directory -> Display title -> Prompt sign-in/register -> 
     * Authenticate user -> Enter dashboard loop.
     */
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

    /**
     * Prompts the user to select either sign-in or register option.
     * Validates input to ensure only 1 or 2 is accepted.
     * 
     * @return 1 for register, 2 for login
     */
    private int promptSignOption() {
        int signOption;
        // Loop until valid input (1 or 2) is received
        do {
            System.out.print(GREEN + "\n1. Register \n2. Login" + RESET + "\nPlease select an option: ");
            // Handle non-integer input
            while (!input.hasNextInt()) {
                System.out.println(RED + "Please enter a number (1 or 2)." + RESET);
                input.next();
                System.out.print("Please select an option: ");
            }
            signOption = input.nextInt();
            if (signOption != 1 && signOption != 2)
                System.out.println(RED + "Invalid option. Please select either 1 or 2." + RESET);
        } while (signOption != 1 && signOption != 2);
        input.nextLine(); // Consume newline left by nextInt()
        return signOption;
    }

    /**
     * Attempts to log in a user by verifying username and password.
     * Allows up to 3 password attempts before failing.
     * On successful login, loads all user data including expenses from file.
     * 
     * @return true if login successful, false otherwise
     */
    private boolean loginUser() {
        System.out.print("Username: ");
        String user = input.nextLine().trim().toLowerCase().replaceAll("\\s+", "");

        // Check if user file exists
        File userFile = new File(DATA_DIR, user + ".txt");
        if (!userFile.exists()) {
            System.out.println(RED + "User not found." + RESET);
            return false;
        }

        // Attempt to verify password with up to 3 tries
        for (int tries = 0; tries < 3; tries++) {
            String pass = readPassword("Password: ");
            try (BufferedReader r = new BufferedReader(new FileReader(userFile))) {
                String storedPass = r.readLine();
                if (pass.equals(storedPass)) {
                    // Password matches - load all user data from file
                    // File format: password, preferredName, currencyCode, budget, then expenses
                    this.username = user;
                    this.password = pass;
                    this.preferredName = r.readLine();
                    this.currencyCode = r.readLine();
                    this.budgetAmount = Double.parseDouble(r.readLine());
                    String line;

                    // Load expenses - each line format: date|description|amount
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

    /**
     * Registers a new user by collecting username, password, and profile information.
     * Validates username (no spaces, not empty, not already taken) and password confirmation.
     * Sets up initial profile with preferred name, currency code, and starting budget.
     * 
     * @return true if registration successful, false otherwise
     */
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

    /**
     * Reads a password from the console with hidden input when available.
     * Falls back to visible input if console is not available (e.g., in IDE).
     * 
     * @param prompt the prompt message to display to the user
     * @return the password entered by the user
     */
    private String readPassword(String prompt) {
        // Use System.console() for secure password reading when available
        if (console != null) {
            char[] chars = console.readPassword(prompt);
            return chars != null ? new String(chars) : "";
        }
        // Fallback to regular input (visible) when console is unavailable
        System.out.print(prompt);
        return input.nextLine();
    }

    /**
     * Main dashboard loop for user interaction.
     * Displays dashboard and processes user menu selections until exit.
     */
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

    /**
     * Displays the main dashboard with budget and expense summary.
     * Shows user info, budget status, remaining balance, analytics, and recent expenses.
     */
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

    /**
     * Displays analytics about expenses and budget usage.
     * Calculates and shows percentage used, average expense, daily burn rate, and highest expense.
     * 
     * @param totalExpenses the total sum of all expenses
     */
    private void printAnalytics(double totalExpenses) {
        // Calculate various analytics metrics
        double percentUsed = (budgetAmount > 0.0) ? (totalExpenses / budgetAmount) * 100.0 : 0.0;
        double avgExpense = expenses.isEmpty() ? 0.0 : totalExpenses / expenses.size();
        double highest = expenses.stream().mapToDouble(e -> e.amount).max().orElse(0.0);
        long daysTracked = daysTracked();
        // Daily burn rate = total expenses divided by number of days tracked
        double dailyBurn = (daysTracked <= 0) ? 0.0 : totalExpenses / daysTracked;

        System.out.println("\nAnalytics:");
        System.out.println(" - % of budget used: " + String.format("%.1f%%", percentUsed));
        System.out.println(" - Avg per expense: " + fmt(avgExpense));
        System.out.println(" - Daily burn (avg): " + fmt(dailyBurn) + (daysTracked > 0 ? " over " + daysTracked + " days" : ""));
        System.out.println(" - Largest expense: " + fmt(highest));
    }

    /**
     * Displays a list of recent expenses.
     * Shows the most recent N expenses in chronological order.
     * 
     * @param count the maximum number of recent expenses to display
     */
    private void printRecentExpenses(int count) {
        System.out.println("\nRecent expenses:");
        if (expenses.isEmpty()) {
            System.out.println(" (no expenses yet)");
        } else {
            // Show only the last 'count' expenses
            int show = Math.min(expenses.size(), count);
            for (int i = expenses.size() - show; i < expenses.size(); i++) {
                System.out.println(" - " + formatExpense(expenses.get(i)));
            }
        }
    }

    /**
     * Adds a new expense entry to the expense list.
     * Prompts user for description and amount, then saves to file.
     */
    private void addExpense() {
        System.out.print("Expense description: ");
        String desc = input.nextLine().trim();
        double amt = promptDouble("Amount: ");
        expenses.add(new Expense(desc, amt, LocalDate.now().format(dateFmt)));
        System.out.println(YELLOW + "Expense added: " + desc + " - " + fmt(amt) + RESET);
        saveData();
    }

    /**
     * Updates the user's budget amount.
     * Prompts for new amount and persists to file.
     */
    private void updateBudget() {
        this.budgetAmount = promptDouble("Enter new budget amount: ");
        System.out.println(YELLOW + "Budget updated to: " + fmt(budgetAmount) + RESET);
        saveData();
    }

    /**
     * Displays all recorded expenses with numbered list.
     * Waits for user input before returning to dashboard.
     */
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

    /**
     * Returns the display name for the current user.
     * Prefers the user's preferred name if set, otherwise falls back to username.
     * 
     * @return the preferred name if available, otherwise the username
     */
    private String displayName() {
        return (preferredName != null && !preferredName.isEmpty()) ? preferredName : username;
    }

    /**
     * Calculates the total amount of all expenses.
     * 
     * @return the sum of all expense amounts
     */
    private double totalExpenses() {
        return expenses.stream().mapToDouble(e -> e.amount).sum();
    }

    /**
     * Calculates the number of days expenses have been tracked.
     * Computes the span from the earliest expense date to the latest expense date (inclusive).
     * 
     * @return the number of days between first and last expense, minimum of 1
     */
    private long daysTracked() {
        if (expenses.isEmpty()) return 0;
        // Find the earliest and latest expense dates using stream operations
        LocalDate earliest = expenses.stream().map(e -> LocalDate.parse(e.date)).min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate latest = expenses.stream().map(e -> LocalDate.parse(e.date)).max(LocalDate::compareTo).orElse(LocalDate.now());
        // Add 1 to include both start and end days in the count
        long days = ChronoUnit.DAYS.between(earliest, latest) + 1;
        return Math.max(1, days);
    }

    /**
     * Formats a double amount into a currency string based on the current currency code.
     * Uses locale-specific number formatting when a matching locale is found.
     * 
     * @param amount the monetary amount to format
     * @return formatted string with amount and currency code (e.g., "1,234.56 USD")
     */
    private String fmt(double amount) {
        // Find locale for currency-specific formatting (thousands separator, decimal symbol)
        Locale loc = Main.findLocaleForCurrency(currencyCode);
        NumberFormat nf = (loc != null) ? NumberFormat.getNumberInstance(loc) : NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(amount) + " " + (currencyCode != null ? currencyCode : "");
    }

    /**
     * Formats an expense entry for display.
     * 
     * @param e the expense to format
     * @return formatted string in format "date | description : amount"
     */
    private String formatExpense(Expense e) {
        return e.date + " | " + e.description + " : " + fmt(e.amount);
    }

    /**
     * Pauses execution until the user presses Enter.
     * Used to allow user to read output before screen clears.
     */
    private void pause() {
        System.out.println("\nPress Enter to continue...");
        input.nextLine();
    }

    /**
     * Prompts the user to enter a double value with validation.
     * Loops until a valid number is entered. Handles comma-formatted input.
     * 
     * @param prompt the prompt message to display
     * @return the validated double value entered by the user
     */
    private double promptDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine().trim();
            try {
                // Remove commas to handle formatted number input (e.g., "1,234.56")
                return Double.parseDouble(line.replace(",", ""));
            } catch (Exception ex) {
                System.out.println(RED + "Invalid number." + RESET);
            }
        }
    }

    /**
     * Saves all user data to a text file.
     * File format: password, preferredName, currencyCode, budgetAmount,
     * followed by expense entries (one per line in format: date|description|amount).
     */
    private void saveData() {
        try (PrintWriter writer = new PrintWriter(new File(DATA_DIR, username + ".txt"))) {
            // Write user profile data
            writer.println(password);
            writer.println(preferredName);
            writer.println(currencyCode);
            writer.println(budgetAmount);
            // Write each expense as pipe-delimited line
            for (Expense e : expenses)
                writer.println(e.date + "|" + e.description + "|" + e.amount);
        } catch (Exception e) {
            System.out.println(RED + "Error saving data." + RESET);
        }
    }
    
    /**
     * Inner class representing a single expense entry.
     * Immutable data structure containing description, amount, and date.
     */
    private static class Expense {
        /** Description of what the expense was for */
        final String description;
        /** Monetary amount of the expense */
        final double amount;
        /** Date of the expense in ISO format (YYYY-MM-DD) */
        final String date;

        /**
         * Constructs a new Expense with the given details.
         * 
         * @param d the description of the expense
         * @param a the monetary amount
         * @param date the date of the expense in ISO format
         */
        Expense(String d, double a, String date) {
            this.description = d;
            this.amount = a;
            this.date = date;
        }
    }
}
