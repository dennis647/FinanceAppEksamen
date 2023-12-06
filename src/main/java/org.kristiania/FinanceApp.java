package org.kristiania;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
            }

            System.out.println("Please enter your work income:");
            double workIncome = scanner.nextDouble();

            System.out.println("Enter your freelance income:");
            double freelanceIncome = scanner.nextDouble();

            Income income = new Income(workIncome, freelanceIncome);

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
}
