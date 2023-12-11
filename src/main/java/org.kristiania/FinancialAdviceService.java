package org.kristiania;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class FinancialAdviceService {

    public static void provideAdvice(ResultSet detailedExpensesResultSet) throws SQLException {
        boolean isBadPurchase = false;
        Map<String, Double> categoryExpenses = new HashMap<>();

        while (detailedExpensesResultSet.next()) {
            String expenseCategory = detailedExpensesResultSet.getString("category");
            double expenseAmount = detailedExpensesResultSet.getDouble("amount");

            categoryExpenses.put(expenseCategory, categoryExpenses.getOrDefault(expenseCategory, 0.0) + expenseAmount);

        }
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("snus") && entry.getValue() > 500) {
                System.out.println("Spending a lot on snus may not be the best use of your funds. Amount: kr " + entry.getValue());
                isBadPurchase = true;
            }
            if (entry.getKey().equalsIgnoreCase("cigarettes") && entry.getValue() > 500) {
                System.out.println("Spending a lot on cigarettes may not be the best use of your funds. Amount: kr " + entry.getValue());
                isBadPurchase = true;
            }
        }

            if (!isBadPurchase) {
                System.out.println("No specific advice for your expenses this month.");
            }
        }
    }
