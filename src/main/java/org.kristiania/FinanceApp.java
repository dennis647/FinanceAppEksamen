package org.kristiania;

import java.sql.*;
import java.text.DateFormatSymbols;
import java.util.*;



class Expense {
    private String category;
    private double amount;

    public Expense(String category, double amount){
        this.category = category;
        this.amount = amount;
    }
}

class Expenses {
    private Map<String, Double> categories;

    public Expenses() {
        categories = new HashMap<>();
    }

    public void addExpense(String category, double amount) {
        Expense expense = new Expense(category, amount);
        categories.put(category, categories.getOrDefault(category, 0.0) + amount);
    }

    public double getTotalExpenses() {
        return categories.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}



public class FinanceApp {

    public static void main(String[] args) throws SQLException {

        // Connect to the database
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/financedb", "root", "toor");
            Statement statement = connection.createStatement();

            Scanner scanner = new Scanner(System.in);

            // Scanner for getting all the values and information needed

            System.out.println("Hello! Welcome to your financial helper application! \n");

            System.out.println("Are you a new user? (yes/no)");
            String isNewUser = scanner.nextLine();

            // Create a new user
            if (isNewUser.equalsIgnoreCase("yes")) {
                System.out.println("Creating new user...");
                System.out.println("Enter your first and last name");
                userName = scanner.nextLine();
                System.out.println("Enter your yearly savings goal");
                double newSavingsGoal = scanner.nextDouble();

                //Add new user to the database
                int affectedRows = statement.executeUpdate("INSERT INTO users (fullname, savings_goal) VALUES ('" + userName + "' , " + newSavingsGoal + ")", Statement.RETURN_GENERATED_KEYS);
                if (affectedRows > 0) {
                    ResultSet generatedKeys = statement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        selectedUserId = generatedKeys.getInt(1);
                    }
                }

                System.out.println("Welcome, " + userName + "!");

                // Enter month selection
                getMonths(connection, statement, scanner);


                // If the user already exists
            } else if (isNewUser.equalsIgnoreCase("no")) {

                // Display existing users
                try {

                    ResultSet existingUsers = statement.executeQuery("SELECT * FROM users");

                    System.out.println("--- Existing users ---");
                    while (existingUsers.next()) {
                        System.out.println(existingUsers.getInt("user_id") + " : " + existingUsers.getString("fullname"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Choose the existing user ID
                System.out.println("Please enter the ID number in front of your name");
                selectedUserId = scanner.nextInt();


                // Fetch user details with that name and use it further in the application
                ResultSet userResultSet = statement.executeQuery("SELECT * FROM users WHERE user_id = " + selectedUserId);

                // Check if the user has a yearly savings goal set
                if (userResultSet.next()) {
                    savingsGoal = userResultSet.getDouble("savings_goal");
                    userName = userResultSet.getString("fullname");


                    if (savingsGoal <= 0) {
                        System.out.println("Hello, " + userName + "! You haven't set a yearly savings goal yet.");
                        System.out.println("Set your yearly savings goal now:");
                        yearlySavingsGoal = scanner.nextDouble();

                        // Update the users savings goal in the db
                        String checkSavingsGoalQuery = "UPDATE users SET savings_goal = ? WHERE user_id = ?";
                        PreparedStatement updateStatement = connection.prepareStatement(checkSavingsGoalQuery);
                        updateStatement.setDouble(1, yearlySavingsGoal);
                        updateStatement.setInt(2, selectedUserId);
                        updateStatement.executeUpdate();

                        System.out.println("Yearly savings goal set successfully!");

                        getMonths(connection, statement, scanner);

                    } else {

                        yearlySavingsGoal = savingsGoal;
                        System.out.println("Welcome back, " + userName + "! \nYour set yearly savings goal is: kr " + yearlySavingsGoal + ",- (Enter anything to continue)");
                        scanner.next();

                        getMonths(connection, statement, scanner);
                    }
                } else {
                    System.out.println("User not found");
                    main(args);
                }
            }

            statement.close();
            connection.close();
            scanner.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static String userName;
    private static double workIncome = 0.0;
    private static double extraIncome = 0.0;
    private static double savingsGoal;
    private static int selectedUserId;
    private static int selectedMonthNumber = 0;
    private static double incomeLeft = 0.0;
    private static double selectedMonthSave = 0.0;
    private static double yearlySavingsGoal;


    private static void getMonths(Connection connection, Statement statement, Scanner scanner) {
        try {

            Map<Integer, String> filledMonths = getFilledMonths(connection, selectedUserId);
            displayAvailableMonths(filledMonths);

            System.out.println("\n15. Overview of the entire year");
            System.out.println("\nSelect a month (1-12) or select (15):");
            selectedMonthNumber = scanner.nextInt();

            // Check if data is already filled in for the selected month
            boolean isFilledIncome = filledMonths.containsKey(selectedMonthNumber) && filledMonths.get(selectedMonthNumber).equals("Filled in");
            boolean isFilledExpenses = filledMonths.containsKey(selectedMonthNumber) && filledMonths.get(selectedMonthNumber).equals("Expenses filled");

            if (selectedMonthNumber == 15) {

                getYearlyOverview(connection, statement, scanner);

            } else if (!isFilledIncome && !isFilledExpenses && selectedMonthNumber <= 12) {
                System.out.println("Please enter your work income:");
                workIncome = scanner.nextDouble();

                System.out.println("Enter your extra income:");
                extraIncome = scanner.nextDouble();

                // Insert the written data into income table
                insertIncomeData(connection, selectedUserId, selectedMonthNumber, workIncome, extraIncome);
                insertExpensesForNewUser(connection, selectedUserId, selectedMonthNumber);

                System.out.println("Enter anything to continue");
                scanner.next();

                getMonths(connection, statement, scanner);

            } else {
                // Display existing income data for the selected month
                ResultSet incomeResultSet = statement.executeQuery("SELECT * FROM income WHERE user_id = " + selectedUserId + " AND MONTH(month) = " + selectedMonthNumber +
                        " UNION SELECT * FROM expenses WHERE user_id = " + selectedUserId + " AND MONTH(month) = " + selectedMonthNumber);

                if (incomeResultSet.next()) {
                    double workIncome = incomeResultSet.getDouble("work_income");
                    double extraIncome = incomeResultSet.getDouble("extra_income");
                    double totalExpenses = 0.0;
                    double monthSavings = 0.0;

                    System.out.println("--- Existing income data for the selected month ---");
                    System.out.println("Work Income: kr " + workIncome + ",-");
                    System.out.println("Extra Income: kr " + extraIncome + ",-");

                    //Get total expenses for that month
                    ResultSet expensesResultSet = statement.executeQuery("SELECT SUM(amount) AS total_expenses FROM expenses WHERE user_id = " + selectedUserId +
                            " AND MONTH(month) = " + selectedMonthNumber);

                    if (expensesResultSet.next()) {
                        totalExpenses = expensesResultSet.getDouble("total_expenses");
                        System.out.println("Total Expenses: kr " + totalExpenses + ",-");
                    }

                    ResultSet savingsResultSet = statement.executeQuery("SELECT * FROM user_savings WHERE user_id = " + selectedUserId + " AND MONTH(month) = "
                            + selectedMonthNumber);

                    if (savingsResultSet.next()) {
                        monthSavings = savingsResultSet.getDouble("amount_saved");
                        System.out.println("Saved this month: kr " + monthSavings + ",-");
                    }

                    incomeLeft = ((workIncome + extraIncome) - totalExpenses) - monthSavings;
                    System.out.println("Money left: kr " + incomeLeft + ",-");
                    System.out.println("\n--- Saved so far ---");
                    System.out.println("So far you have saved kr " + monthSavings + ",- out of your goal: kr " + yearlySavingsGoal + ",-");
                    scanner.next();

                    System.out.println("Do you want to view a detailed overview of expenses for this month? (yes/no)");
                    String viewExpensesOption = scanner.next();

                    // If the user wants a more detailed overview
                    if (viewExpensesOption.equalsIgnoreCase("yes")) {
                        ResultSet detailedExpensesResultSet = statement.executeQuery("SELECT * FROM expenses WHERE user_id = " + selectedUserId +
                                " AND MONTH(month) = " + selectedMonthNumber);
                        while (detailedExpensesResultSet.next()) {
                            String expenseCategory = detailedExpensesResultSet.getString("category");
                            double expenseAmount = detailedExpensesResultSet.getDouble("amount");

                            System.out.println("Description: " + expenseCategory + ", Amount: kr " + expenseAmount + ",-");
                        }

                        System.out.println("\nDo you want some financial advice? (yes/no) (no to go back to overview)");
                        String savingsAdv = scanner.next();

                        // Gives the financial advice
                        if (savingsAdv.equalsIgnoreCase("yes")) {
                            ResultSet FinanceAdvResultSet = statement.executeQuery("SELECT * FROM expenses WHERE user_id = " + selectedUserId +
                                    " AND MONTH(month) = " + selectedMonthNumber);
                            FinancialAdviceService.provideAdvice(FinanceAdvResultSet);

                            System.out.println("\nType anything to return to monthly overview");
                            scanner.next();
                            getMonths(connection, statement, scanner);

                        } else if (savingsAdv.equalsIgnoreCase("no")) {
                            getMonths(connection, statement, scanner);

                        } else if (viewExpensesOption.equalsIgnoreCase("no")) {
                            getMonths(connection, statement, scanner);

                        }

                    } else if (viewExpensesOption.equalsIgnoreCase("no")) {
                        getMonths(connection, statement, scanner);
                    }
                } else {
                    System.out.println("Error: No income data found for the selected month.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Displays the yearly overview - but savings doesn't work.
    private static void getYearlyOverview(Connection connection, Statement statement, Scanner scanner) throws SQLException {
        double totalIncome = 0.0;
        double totalExpenses = 0.0;
        double totalSavings = 0.0;

        try {
            for (int i = 1; i <= 12; i++) {
                ResultSet overviewResultSet = statement.executeQuery(
                        "SELECT work_income, extra_income, NULL AS amount, NULL AS amount_saved FROM income WHERE user_id = " + selectedUserId + " AND MONTH(month) = " + i +
                                " UNION ALL " +
                                "SELECT NULL, NULL, amount, NULL FROM expenses WHERE user_id = " + selectedUserId + " AND MONTH(month) = " + i +
                                " UNION ALL " +
                                "SELECT NULL, NULL, NULL, amount_saved FROM user_savings WHERE user_id = " + selectedUserId + " AND MONTH(month) = " + i);


                double monthIncome = 0.0;
                double monthExpenses = 0.0;
                double monthSavings = 0.0;

                if (overviewResultSet.next()) {
                    monthIncome += overviewResultSet.getDouble("work_income") + overviewResultSet.getDouble("extra_income");
                }

                if (overviewResultSet.next()) {
                    monthExpenses += overviewResultSet.getDouble("amount");
                }

                if (overviewResultSet.next()) {
                    monthSavings += overviewResultSet.getDouble("amount_saved");
                    monthIncome += 1;
                }

                totalIncome += monthIncome;
                totalExpenses += monthExpenses;
                totalSavings += monthSavings;

                System.out.println("Month " + i + " - Income: " + monthIncome + ", Expenses: " + monthExpenses + ", Savings: " + monthSavings);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("\nTotal Income for the year: " + totalIncome);
        System.out.println("Total Expenses for the year: " + totalExpenses);
        System.out.println("Total Savings for the year: " + totalSavings);
        System.out.println("\nEnter anything to return");
        scanner.next();
        getMonths(connection, statement, scanner);
    }

    // For inserting income data into the tables

    private static void insertIncomeData(Connection connection, int selectedUserId, int selectedMonthNumber, double workIncome, double extraIncome) throws SQLException {
        String insertQuery = "INSERT INTO income (user_id, month, work_income, extra_income) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setInt(1, selectedUserId);
        preparedStatement.setString(2, "2023-" + String.format("%02d", selectedMonthNumber) + "-01"); // Here we are starting by just doing 1 year (2023)
        preparedStatement.setDouble(3, workIncome);
        preparedStatement.setDouble(4, extraIncome);

        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Income data has successfully been added!");
        } else {
            System.out.println("Error: Failed to add income");
        }
    }

    // Add expenses into the database
    private static void insertExpensesForNewUser(Connection connection, int selectedUserId, int selectedMonthNumber) throws SQLException {
        Expenses expenses = new Expenses();
        ExpenseCategoryService categorizationServices = new ExpenseCategoryService();
        Scanner scanner = new Scanner(System.in);

        String getQuery = "SELECT SUM(work_income + extra_income) AS total_income " +
                "FROM income " +
                "WHERE user_id = " + selectedUserId + " AND MONTH(month) = " + selectedMonthNumber;

        double totalIncome = 0.0;
        double incomeLeft = 0.0;

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(getQuery);
            if (resultSet.next()) {
                totalIncome = resultSet.getDouble("total_income");
            }
        }

        while (true) {
            System.out.println("Enter an expense description (Type done to break):");
            String description = scanner.next();
            if (description.equalsIgnoreCase("done")) break;

            System.out.println("Enter the expense amount:");
            double amount = scanner.nextDouble();

            String category = categorizationServices.categoryExpenses(description);
            expenses.addExpense(category, amount);

            // Insert the expenses into the database
            insertExpenseData(connection, selectedUserId, selectedMonthNumber, category, amount);

            // Insert monthly savings into the database

            incomeLeft -= amount;
        }
        incomeLeft = incomeLeft + totalIncome;

        System.out.println("Left of income after expenses: kr " + incomeLeft + ",-");
        System.out.println("How much would you like to put towards your savings goal?");
        selectedMonthSave = scanner.nextDouble();

        insertMonthlySavings(connection, selectedUserId, selectedMonthNumber, selectedMonthSave);

    }

    // For showing all the months
    private static Map<Integer, String> getFilledMonths(Connection connection, int selectedUserId) throws SQLException {
        Map<Integer, String> filledMonths = new HashMap<>();

        ResultSet filledMonthsResultSet = connection.createStatement().executeQuery(
                "SELECT DISTINCT MONTH(month) AS month_number FROM income WHERE user_id = " + selectedUserId +
                        " UNION SELECT DISTINCT MONTH(month) AS month_number FROM expenses WHERE user_id = " + selectedUserId);

        while (filledMonthsResultSet.next()) {
            filledMonths.put(filledMonthsResultSet.getInt("month_number"), "Filled in");
        }
        return filledMonths;
    }

    // If a user hasn't added expenses
    private static void insertExpenseData(Connection connection, int selectedUserId, int selectedMonthNumber, String category, double amount) throws SQLException {
        String insertQuery = "INSERT INTO expenses (user_id, month, category, amount) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setInt(1, selectedUserId);
        preparedStatement.setString(2, "2023-" + String.format("%02d", selectedMonthNumber) + "-01");
        preparedStatement.setString(3, category);
        preparedStatement.setDouble(4, amount);

        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Expense data has successfully been added!");
        } else {
            System.out.println("Error: Failed to add expense");
        }
    }

    // Inserts monthly savings into the database
    private static void insertMonthlySavings(Connection connection, int selectedUserId, int selectedMonthNumber, double amountSaved) throws SQLException {
        String insertQuery = "INSERT INTO user_savings (user_id, month, amount_saved) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setInt(1, selectedUserId);
        preparedStatement.setString(2, "2023-" + String.format("%02d", selectedMonthNumber) + "-01");
        preparedStatement.setDouble(3, amountSaved);

        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Savings data has successfully been added!");

        } else {
            System.out.println("Error: Failed to add savings data");
        }
    }

    // Displays the 12 months
    private static void displayAvailableMonths(Map<Integer, String> filledMonths) {
        System.out.println("--- Available months ---");
        for (int i = 1; i <= 12; i++) {
            String monthName = getMonthName(i);
            String status = filledMonths.containsKey(i) ? filledMonths.get(i) : "Not filled in";
            System.out.println(i + ". " + monthName + " - " + status);
        }
    }

    // To get all the months
    private static String getMonthName(int monthNumber) {
        return new DateFormatSymbols().getMonths()[monthNumber -1 ];
    }
}