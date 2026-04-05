package com.zorvyn.finance.dto;

import java.math.BigDecimal;

public class CategoryTotal {

    private String category;
    private String type;
    private BigDecimal total;

    public CategoryTotal() {
    }

    public CategoryTotal(String category, String type, BigDecimal total) {
        this.category = category;
        this.type = type;
        this.total = total;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}

