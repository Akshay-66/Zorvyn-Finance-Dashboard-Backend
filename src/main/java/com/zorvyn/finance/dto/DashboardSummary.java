package com.zorvyn.finance.dto;

import com.zorvyn.finance.model.FinancialRecord;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummary {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private List<CategoryTotal> categoryTotals;
    private List<FinancialRecord> recentActivity;
    private List<TrendPoint> monthlyTrends;

    public DashboardSummary(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal netBalance,
                            List<CategoryTotal> categoryTotals, List<FinancialRecord> recentActivity,
                            List<TrendPoint> monthlyTrends) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
        this.categoryTotals = categoryTotals;
        this.recentActivity = recentActivity;
        this.monthlyTrends = monthlyTrends;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public List<CategoryTotal> getCategoryTotals() {
        return categoryTotals;
    }

    public List<FinancialRecord> getRecentActivity() {
        return recentActivity;
    }

    public List<TrendPoint> getMonthlyTrends() {
        return monthlyTrends;
    }
}

