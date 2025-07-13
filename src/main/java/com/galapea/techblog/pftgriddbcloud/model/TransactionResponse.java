package com.galapea.techblog.pftgriddbcloud.model;

import java.time.LocalDateTime;

public record TransactionResponse(
		String id,
		CategoryResponse category,
		Double amount,
		LocalDateTime transactionDate,
		String transactionType,
		String description,
		UserResponse user) {}
