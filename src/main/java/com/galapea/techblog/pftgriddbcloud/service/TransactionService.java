package com.galapea.techblog.pftgriddbcloud.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.galapea.techblog.pftgriddbcloud.model.TransactionDTO;
import com.galapea.techblog.pftgriddbcloud.model.TransactionSummary;
import com.galapea.techblog.pftgriddbcloud.util.NotFoundException;
import com.galapea.techblog.pftgriddbcloud.util.NotImplementedException;
import com.github.f4b6a3.tsid.TsidCreator;

@Service
public class TransactionService {

	private final TransactionContainer transactionContainer;

	public TransactionService(TransactionContainer transactionContainer) {
		this.transactionContainer = transactionContainer;
	}

	public static String nextId() {
		return "trx_" + TsidCreator.getTsid().format("%S");
	}

	public List<TransactionDTO> findAll() {
		final List<TransactionRecord> transactions = transactionContainer.getAll();
		return transactions.stream()
				.map(transaction -> mapToDTO(transaction, new TransactionDTO()))
				.collect(Collectors.toList());
	}

	public TransactionDTO get(final String id) {
		return transactionContainer
				.getOne(id)
				.map(transaction -> mapToDTO(transaction, new TransactionDTO()))
				.orElseThrow(NotFoundException::new);
	}

	public String create(final TransactionDTO transactionDTO) {
		final String id = (transactionDTO.getId() != null) ? transactionDTO.getId() : nextId();
		TransactionRecord newTransaction =
				new TransactionRecord(
						id,
						transactionDTO.getCategoryId(),
						transactionDTO.getAmount(),
						transactionDTO.getTransactionDate(),
						transactionDTO.getTransactionType(),
						transactionDTO.getDescription(),
						transactionDTO.getUserId());
		transactionContainer.saveRecords(List.of(newTransaction));
		return newTransaction.id();
	}

	public void update(final String id, final TransactionDTO transactionDTO) {
		TransactionRecord newTransaction =
				new TransactionRecord(
						id,
						transactionDTO.getCategoryId(),
						transactionDTO.getAmount(),
						transactionDTO.getTransactionDate(),
						transactionDTO.getTransactionType(),
						transactionDTO.getDescription(),
						transactionDTO.getUserId());
		transactionContainer.saveRecords(List.of(newTransaction));
	}

	public void createAll(List<TransactionDTO> transactionDTOs) {
		List<TransactionRecord> transactionRecords =
				transactionDTOs.stream()
						.map(
								transactionDTO ->
										new TransactionRecord(
												(transactionDTO.getId() != null)
														? transactionDTO.getId()
														: nextId(),
												transactionDTO.getCategoryId(),
												transactionDTO.getAmount(),
												transactionDTO.getTransactionDate(),
												transactionDTO.getTransactionType(),
												transactionDTO.getDescription(),
												transactionDTO.getUserId()))
						.collect(Collectors.toList());
		if (!transactionRecords.isEmpty()) {
			transactionContainer.saveRecords(transactionRecords);
		}
	}

	public void delete(final String id) {
		throw new NotImplementedException("Delete operation is not implemented yet.");
	}

	private TransactionDTO mapToDTO(
			final TransactionRecord transaction, final TransactionDTO transactionDTO) {
		transactionDTO.setId(transaction.id());
		transactionDTO.setCategoryId(transaction.categoryId());
		transactionDTO.setAmount(transaction.amount());
		transactionDTO.setTransactionDate(transaction.transactionDate());
		transactionDTO.setTransactionType(transaction.transactionType());
		transactionDTO.setDescription(transaction.description());
		transactionDTO.setUserId(transaction.userId());
		return transactionDTO;
	}

	public boolean idExists(final String id) {
		return transactionContainer.getOne(id).isPresent();
	}

	public void createTable() {
		transactionContainer.createTable();
	}

	public List<TransactionSummary> getTransactionSummary(String userIdString) {
		return transactionContainer.getTransactionSummaryByDate(userIdString, "", "");
	}
}
