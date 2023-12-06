package org.kristiania;

import javax.xml.transform.Result;
import java.sql.*;
import java.text.DateFormatSymbols;
import java.util.*;

class personalInfo {
    private String name;
    public personalInfo(String name) {
        this.name = name;
    }
}

class Income {
    private double workIncome;
    private double extraIncome;

    public Income(double workIncome, double extraIncome) {
        this.workIncome = workIncome;
        this.extraIncome = extraIncome;
    }

    public double getTotalIncome() {
        return workIncome + extraIncome;
    }
}

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

    public Map<String, Double> getCategories() {
        return categories;
    }

    public double getTotalExpenses() {
        return categories.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}


class Savings {
    private double savingsGoal;
    private double currentAmountSaved;

    public Savings(double savingsGoal) {
        this.savingsGoal = savingsGoal;
        this.currentAmountSaved = 0.00;
    }

    public double getCurrentAmountSaved() {
        return currentAmountSaved;
    }

    public double getSavingsGoal() {
        return savingsGoal;
    }

    public void addToSavings(double amount) {
        currentAmountSaved += amount;
    }

    public int monthsToReachGoal(double monthlySaved) {
        if (monthlySaved <= 0) return -1;
        double remaining = savingsGoal - currentAmountSaved;
        return (int) Math.ceil(remaining / monthlySaved);
    }
}


public class FinanceApp {

    private static String userName;
    private static double workIncome = 0.0;
    private static double extraIncome = 0.0;
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
                String newName = scanner.nextLine();

                //Add new user to the database
                statement.executeUpdate("INSERT INTO users (fullname) VALUES ('" + newName + "')");

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

                // Choose the existing name
                System.out.println("Please enter the ID number in front of your name");
                int selectedUserId = scanner.nextInt();

                // Fetch user details with that name and use it further in the application
                ResultSet userResultSet = statement.executeQuery("SELECT * FROM users WHERE user_id = " + selectedUserId);
                if (userResultSet.next()) {
                    userName = userResultSet.getString("fullname");
                    System.out.println("Welcome back, " + userName + "!");
                } else {
                    System.out.println("User not found");
                }

                // Let the user select which month it wants to fill in. Also get overview of what months are filled in.
                try {
                    Map<Integer, String> filledMonths = new HashMap<>();

                    ResultSet filledMonthsResultSet = statement.executeQuery("SELECT DISTINCT MONTH(month) as month_number FROM income WHERE user_id = " + selectedUserId +
                            " UNION SELECT DISTINCT MONTH(month) as month_number FROM expenses WHERE user_id = " + selectedUserId);

                    while (filledMonthsResultSet.next()) {
                        filledMonths.put(filledMonthsResultSet.getInt("month_number"), "Filled in");
                    }

                    System.out.println("Available months:");
                    for (int i = 1; i <= 12; i++) {
                        String monthName = getMonthName(i);
                        String status = filledMonths.containsKey(i) ? filledMonths.get(i) : "Not filled in";
                        System.out.println(i + ". " + monthName + " - " + status);
                    }
                    System.out.println("Select a month (1-12):");
                    int selectedMonthNumber = scanner.nextInt();

                    // Check if data is already filled in for the selected month
                    boolean isFilled = filledMonths.containsKey(selectedMonthNumber) && filledMonths.get(selectedMonthNumber).equals("Filled in");


                    if (!isFilled) {
                        System.out.println("Please enter your work income:");
                        workIncome = scanner.nextDouble();

                        System.out.println("Enter your extra income:");
                        extraIncome = scanner.nextDouble();

                        // Insert the written data into income table
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
                            System.out.println("Failed to add income");
                        }
                    } else {
                        System.out.println("Income data for the selected month already exists.");
                        // Display existing income data for the selected month
                        ResultSet incomeResultSet = statement.executeQuery("SELECT * FROM income WHERE user_id = " + selectedUserId + " AND MONTH(month) = " + selectedMonthNumber);
                        if (incomeResultSet.next()) {
                            double workIncome = incomeResultSet.getDouble("work_income");
                            double extraIncome = incomeResultSet.getDouble("extra_income");

                            System.out.println("Existing income data for the selected month:");
                            System.out.println("Work Income: " + workIncome);
                            System.out.println("Extra Income: " + extraIncome);
                        } else {
                            System.out.println("No income data found for the selected month.");
                        }
                    }

                        ResultSet incomeResultSet = statement.executeQuery("SELECT * FROM income WHERE user_id = " + selectedUserId + " AND MONTH(month) = " + selectedMonthNumber);

                    } catch(SQLException e){
                        e.printStackTrace();
                    }

            }


            Income income = new Income(workIncome, extraIncome);

            System.out.println("Set your savings goal:");
            double savingsGoal = scanner.nextDouble();

            Savings savings = new Savings(savingsGoal);

            Expenses expenses = new Expenses();

            ExpenseCategoryService categorizationServices = new ExpenseCategoryService();

            while (true) {
                System.out.println("Enter an expense description (Type done to break):");
                String description = scanner.next();
                if (description.equalsIgnoreCase("done")) break;

                System.out.println("Enter the expense amount:");
                double amount = scanner.nextDouble();

                String category = categorizationServices.categoryExpenses(description);

                expenses.addExpense(category, amount);
            }


            double totalIncome = income.getTotalIncome();
            double totalExpenses = expenses.getTotalExpenses();
            double leftEachMonth = totalIncome - totalExpenses;

            System.out.println("\n--- Financial Helper Application ---");
            System.out.println("Hello, " + userName + "!");
            System.out.println("\n--- Summary ---");
            System.out.println("Total Income: kr " + totalIncome + ",-");
            System.out.println("Total Expenses: kr " + totalExpenses + ",-");
            System.out.println("Amount Left Each Month: kr " + leftEachMonth + ",-");

            System.out.println("\n--- Expense Summary ---");
            for (Map.Entry<String, Double> entry : expenses.getCategories().entrySet()) {
                System.out.println("Category: " + entry.getKey() + ", Total Amount: kr " + entry.getValue() + ",-");
            }

            System.out.println("\n--- Savings ---");
            System.out.println("Current Savings: kr " + savings.getCurrentAmountSaved() + ",-");
            System.out.println("Savings Goal: kr " + savings.getSavingsGoal() + ",-");

            System.out.println("\n--- Savings Plan ---");
            System.out.println("You have " + leftEachMonth + "kr left after all expenses");
            System.out.println("Enter the amount you want to save each month:");
            double monthlySaving = scanner.nextDouble();

            int monthsToReachGoal = savings.monthsToReachGoal(monthlySaving);

            if (monthsToReachGoal > 0) {
                System.out.println("If you save kr " + monthlySaving + ",- every month, it will take you approximately " + monthsToReachGoal + " months to reach your savings goal of kr" + savingsGoal + ",-");
            } else {
                System.out.println("You won't reach your savings goal with the given savings amount.");
            }

            statement.close();
            connection.close();
            scanner.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getMonthName(int monthNumber) {
        return new DateFormatSymbols().getMonths()[monthNumber -1 ];
    }
}
