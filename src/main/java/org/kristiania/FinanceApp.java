package org.kristiania;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
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
        this.currentAmountSaved = 0.0;
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

    public int timeToReachGoal(double monthlySaved) {
        if (monthlySaved <= 0) return -1;
        double remaining = savingsGoal - currentAmountSaved;
        return (int) Math.ceil(remaining / monthlySaved);
    }
}


public class FinanceApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
    }
}