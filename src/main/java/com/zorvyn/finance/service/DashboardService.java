package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.DashboardSummary;
import com.zorvyn.finance.exception.ApiException;
import com.zorvyn.finance.model.RecordType;
import com.zorvyn.finance.repository.RecordRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DashboardService {

    private final RecordRepository recordRepository;

    public DashboardService(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public DashboardSummary getSummary(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ApiException(400, "fromDate cannot be after toDate");
        }

        BigDecimal totalIncome = recordRepository.calculateTotalByType(RecordType.INCOME, fromDate, toDate);
        BigDecimal totalExpense = recordRepository.calculateTotalByType(RecordType.EXPENSE, fromDate, toDate);
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        return new DashboardSummary(
                totalIncome,
                totalExpense,
                netBalance,
                recordRepository.findCategoryTotals(fromDate, toDate),
                recordRepository.findRecentActivity(fromDate, toDate, 5),
                recordRepository.findMonthlyTrends(fromDate, toDate)
        );
    }
}

