package com.galapea.techblog.pftgriddbcloud.service;

import java.time.LocalDateTime;

public record TransactionRecord(
		String id,
		String categoryId,
		Double amount,
		LocalDateTime transactionDate,
		String transactionType,
		String description,
		String userId) {}
