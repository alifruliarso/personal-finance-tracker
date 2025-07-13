package com.galapea.techblog.pftgriddbcloud.model;

public enum TransactionType {
	EXPENSE("Expense"),
	INCOME("Income");
	private final String label;

	TransactionType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
