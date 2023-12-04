package org.kristiania;

import java.util.Map;

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

    }
}

class Expenses {
    private Map<String, Double> categories;

    public Expenses() {

    }
}

class Savings {
    private double savingsGoal;
    private double currentAmountSaved;

    public Savings(double savingsGoal) {

    }
}


public class FinanceApp {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}