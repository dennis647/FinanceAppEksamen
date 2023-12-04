package org.kristiania;

public class ExpenseCategoryService {
    public String categoryExpenses(String description) {
        // Simple rules for expense categorization
        if (description.toLowerCase().contains("food") || description.toLowerCase().contains("mat")) {
            return "Food";
        } else if (description.toLowerCase().contains("rent") || description.toLowerCase().contains("housing")) {
            return "Rent";
        } else if (description.toLowerCase().contains("transport") || description.toLowerCase().contains("car")) {
            return "Transportation";
        } else if (description.toLowerCase().contains("bills") || description.toLowerCase().contains("electricity")) {
            return "Bills";
        } else {
            return "Other";
        }
    }
}
