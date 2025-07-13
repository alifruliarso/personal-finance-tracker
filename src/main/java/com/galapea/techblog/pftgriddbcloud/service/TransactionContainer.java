package com.galapea.techblog.pftgriddbcloud.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import com.galapea.techblog.pftgriddbcloud.model.TransactionSummary;
import com.galapea.techblog.pftgriddbcloud.model.TransactionType;
import com.galapea.techblog.pftgriddbcloud.util.DateTimeUtil;
import com.galapea.techblog.pftgriddbcloud.webapi.GridDbCloudClient;
import com.galapea.techblog.pftgriddbcloud.webapi.GridDbCloudSQLStmt;
import com.galapea.techblog.pftgriddbcloud.webapi.GridDbColumn;
import com.galapea.techblog.pftgriddbcloud.webapi.GridDbContainerDefinition;
import com.galapea.techblog.pftgriddbcloud.webapi.GridDbException;
import com.galapea.techblog.pftgriddbcloud.webapi.SQLSelectResponse;
import com.galapea.techblog.pftgriddbcloud.webapi.SQLUpdateResponse;
import com.galapea.techblog.pftgriddbcloud.webapi.acquisition.AcquireRowsRequest;
import com.galapea.techblog.pftgriddbcloud.webapi.acquisition.AcquireRowsResponse;

@Component
public class TransactionContainer {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final GridDbCloudClient gridDbCloudClient;
	private static final String TBL_NAME = "PFTTransaction";

	public String getTblName() {
		return TBL_NAME;
	}

	public static java.time.format.DateTimeFormatter dateTimeFmt =
			java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

	public TransactionContainer(GridDbCloudClient gridDbCloudClient) {
		this.gridDbCloudClient = gridDbCloudClient;
	}

	public void createTable() {
		log.info("Creating table {} in GridDB...", TBL_NAME);
		List<GridDbColumn> columns =
				List.of(
						new GridDbColumn("id", "STRING", Set.of("TREE")),
						new GridDbColumn("categoryId", "STRING"),
						new GridDbColumn("amount", "DOUBLE"),
						new GridDbColumn("transactionDate", "TIMESTAMP"),
						new GridDbColumn("transactionType", "STRING"),
						new GridDbColumn("description", "STRING"),
						new GridDbColumn("userId", "STRING", Set.of("TREE")));

		GridDbContainerDefinition containerDefinition =
				GridDbContainerDefinition.build(TBL_NAME, columns);
		this.gridDbCloudClient.createContainer(containerDefinition);
		log.info("Created table {} with columns: {}", TBL_NAME, columns);
	}

	public void insert(TransactionRecord rec) {
		String stmt =
				"INSERT INTO \""
						+ TBL_NAME
						+ "\" (\"id\", \"categoryId\", \"amount\", \"transactionDate\", \"transactionType\", \"description\", \"userId\") VALUES (\""
						+ rec.id()
						+ "\", \""
						+ rec.categoryId()
						+ "\", "
						+ rec.amount()
						+ ", \""
						+ DateTimeUtil.formatToZoneDateTimeString(rec.transactionDate())
						+ "\", \""
						+ rec.transactionType()
						+ "\", "
						+ (rec.description() == null ? null : "\"" + rec.description() + "\"")
						+ ", \""
						+ rec.userId()
						+ "\")";

		List<GridDbCloudSQLStmt> statements = List.of(new GridDbCloudSQLStmt(stmt));
		log.info("Executing SQL statement: {}", stmt);
		SQLUpdateResponse[] responses = this.gridDbCloudClient.update(statements);
		if (responses == null || responses.length == 0) {
			log.error("Failed to insert transaction record: {}", rec);
			throw new GridDbException(
					"Failed to insert transaction record",
					HttpStatusCode.valueOf(500),
					"Insert operation returned no response",
					null);
		}
		for (SQLUpdateResponse response : responses) {
			log.info("response: {}", response);
		}
	}

	public void saveRecords(List<TransactionRecord> cRecords) {
		// Initialize a StringBuilder to create the JSON-like array representation
		StringBuilder sb = new StringBuilder();
		sb.append("["); // Start the outer array
		for (int i = 0; i < cRecords.size(); i++) {
			TransactionRecord record = cRecords.get(i);
			sb.append("["); // Start the inner array (each record)
			// Append each record field in order
			sb.append("\"").append(record.id()).append("\"");
			sb.append(", ");
			sb.append("\"").append(record.categoryId()).append("\"");
			sb.append(", ");
			sb.append("\"").append(record.amount()).append("\"");
			sb.append(", ");
			sb.append("\"")
					.append(DateTimeUtil.formatToZoneDateTimeString(record.transactionDate()))
					.append("\"");
			sb.append(", ");
			sb.append("\"").append(record.transactionType()).append("\"");
			sb.append(", ");
			if (record.description() == null) {
				sb.append("null");
			} else {
				sb.append("\"").append(record.description()).append("\"");
			}
			sb.append(", ");
			sb.append("\"").append(record.userId()).append("\"");
			sb.append("]"); // End the inner array
			// Add a comma between record, except for the last one
			if (i < cRecords.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append("]"); // End the outer array
		String result = sb.toString();
		log.info("transactions array: {}", result);
		this.gridDbCloudClient.registerRows(TBL_NAME, result);
	}

	public List<TransactionRecord> getAll() {
		AcquireRowsRequest requestBody =
				AcquireRowsRequest.builder().limit(50L).sort("transactionDate DESC").build();
		AcquireRowsResponse response = this.gridDbCloudClient.acquireRows(TBL_NAME, requestBody);
		if (response == null || response.getRows() == null) {
			log.error("Failed to acquire rows from GridDB");
			return List.of();
		}
		List<TransactionRecord> transactions = convertResponseToRecord(response);
		log.info("Fetched {} transactions from GridDB", transactions.size());
		return transactions;
	}

	public Optional<TransactionRecord> getOne(String id) {
		AcquireRowsRequest requestBody =
				AcquireRowsRequest.builder().limit(1L).condition("id == \'" + id + "\'").build();
		AcquireRowsResponse response = this.gridDbCloudClient.acquireRows(TBL_NAME, requestBody);
		if (response == null || response.getRows() == null) {
			log.error("Failed to acquire rows from GridDB");
			return Optional.empty();
		}
		List<TransactionRecord> transactions = convertResponseToRecord(response);
		log.info("Fetched {} transactions from GridDB", transactions.size());
		return transactions.isEmpty() ? Optional.empty() : Optional.of(transactions.get(0));
	}

	List<TransactionRecord> convertResponseToRecord(AcquireRowsResponse response) {
		List<TransactionRecord> results =
				response.getRows().stream()
						.map(
								row -> {
									try {
										var rec =
												new TransactionRecord(
														row.get(0).toString(),
														row.get(1).toString(),
														Double.valueOf(row.get(2).toString()),
														DateTimeUtil.parseToLocalDateTime(
																row.get(3).toString()),
														row.get(4).toString(),
														(row.get(5) == null
																? ""
																: row.get(5).toString()),
														row.get(6).toString());
										return rec;
									} catch (Exception e) {
										log.error(
												"Error parsing user row: {}. Error: {}",
												row.toString(),
												e.getMessage());
										return null;
									}
								})
						.filter(r -> r != null)
						.collect(Collectors.toList());
		return results;
	}

	public List<TransactionSummary> getTransactionSummaryByDate(
			String userId, String startDate, String endDate) {

		String stmt2 =
				"""
				SELECT transactionDate, \
				SUM(CASE WHEN transactionType = '%s' THEN amount ELSE 0 END) as expenseAmount, \
				SUM(CASE WHEN transactionType = '%s' THEN amount ELSE 0 END) as incomeAmount \
				FROM %s \
				WHERE userId = '%s' \
				GROUP BY transactionDate ORDER BY transactionDate"""
						.formatted(
								TransactionType.EXPENSE.name(),
								TransactionType.INCOME.name(),
								TBL_NAME,
								userId);

		List<GridDbCloudSQLStmt> statementList = List.of(new GridDbCloudSQLStmt(stmt2));
		SQLSelectResponse[] response = this.gridDbCloudClient.select(statementList);
		if (response == null || response.length != statementList.size()) {
			log.error(
					"Failed to getTransactionSummaryByDate. Response is null or size mismatch. Expected: {}, Actual: {}",
					statementList.size(),
					response != null ? response.length : 0);
			return List.of();
		}

		List<List<Object>> results = response[0].getResults();
		if (results.isEmpty()) {
			log.info(
					"No transaction summary found for userId: {}, between {} and {}",
					userId,
					startDate,
					endDate);
			return List.of();
		}
		List<TransactionSummary> summaries =
				results.stream()
						.map(
								row -> {
									try {
										String dateStr = row.get(0).toString().split("T")[0];
										double expenseAmount =
												Double.parseDouble(row.get(1).toString());
										double incomeAmount =
												Double.parseDouble(row.get(2).toString());
										return new TransactionSummary(
												dateStr, expenseAmount, incomeAmount);
									} catch (Exception e) {
										log.error(
												"Error parsing transaction summary row: {}. Error: {}",
												row.toString(),
												e.getMessage());
										return null;
									}
								})
						.filter(s -> s != null)
						.collect(Collectors.toList());
		log.info(
				"Fetched {} transaction summaries for userId: {}, between {} and {}",
				summaries.size(),
				userId,
				startDate,
				endDate);
		return summaries;
	}
}
