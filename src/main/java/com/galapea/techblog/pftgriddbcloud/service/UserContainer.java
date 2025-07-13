package com.galapea.techblog.pftgriddbcloud.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import com.galapea.techblog.pftgriddbcloud.webapi.GridDbCloudClient;
import com.galapea.techblog.pftgriddbcloud.webapi.GridDbCloudSQLStmt;
import com.galapea.techblog.pftgriddbcloud.webapi.GridDbColumn;
import com.galapea.techblog.pftgriddbcloud.webapi.GridDbContainerDefinition;
import com.galapea.techblog.pftgriddbcloud.webapi.GridDbException;
import com.galapea.techblog.pftgriddbcloud.webapi.acquisition.AcquireRowsRequest;
import com.galapea.techblog.pftgriddbcloud.webapi.acquisition.AcquireRowsResponse;

@Component
public class UserContainer {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final GridDbCloudClient gridDbCloudClient;
	private static final String TBL_NAME = "PFTUser";

	public UserContainer(GridDbCloudClient gridDbCloudClient) {
		this.gridDbCloudClient = gridDbCloudClient;
	}

	public void createTable() {
		log.info("Creating table " + TBL_NAME + " in GridDB...");
		List<GridDbColumn> columns =
				List.of(
						new GridDbColumn("id", "STRING", Set.of("TREE")),
						new GridDbColumn("email", "STRING", Set.of("TREE")),
						new GridDbColumn("fullName", "STRING"));

		GridDbContainerDefinition containerDefinition =
				GridDbContainerDefinition.build(TBL_NAME, columns);
		this.gridDbCloudClient.createContainer(containerDefinition);
		log.info("Created table " + TBL_NAME + " with columns: {}", columns);
	}

	private void post(String uri, Object body) {
		try {
			this.gridDbCloudClient.post(uri, body);
		} catch (GridDbException e) {
			throw e;
		} catch (Exception e) {
			throw new GridDbException(
					"Failed to execute POST request",
					HttpStatusCode.valueOf(500),
					e.getMessage(),
					e);
		}
	}

	public void insert(UserRecord rec) {
		String stmt =
				"INSERT INTO "
						+ TBL_NAME
						+ "(id, email, fullName) VALUES ('"
						+ rec.id()
						+ "', '"
						+ rec.email()
						+ "', '"
						+ rec.fullName()
						+ "')";
		GridDbCloudSQLStmt insert = new GridDbCloudSQLStmt(stmt);

		post("/sql/update", List.of(insert));
	}

	public void saveRecords(List<UserRecord> cRecords) {
		// Initialize a StringBuilder to create the JSON-like array representation
		StringBuilder sb = new StringBuilder();
		sb.append("["); // Start the outer array
		for (int i = 0; i < cRecords.size(); i++) {
			UserRecord record = cRecords.get(i);
			sb.append("["); // Start the inner array (each record)
			// Append each record field in order
			sb.append("\"").append(record.id()).append("\"");
			sb.append(", ");
			sb.append("\"").append(record.email()).append("\"");
			sb.append(", ");
			sb.append("\"").append(record.fullName()).append("\"");
			sb.append("]"); // End the inner array
			// Add a comma between record, except for the last one
			if (i < cRecords.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append("]"); // End the outer array
		String result = sb.toString();
		log.info("users array: {}", result);
		this.gridDbCloudClient.registerRows(TBL_NAME, result);
	}

	public List<UserRecord> getAll() {
		AcquireRowsRequest requestBody = AcquireRowsRequest.builder().limit(50L).build();
		AcquireRowsResponse response = this.gridDbCloudClient.acquireRows(TBL_NAME, requestBody);
		if (response == null || response.getRows() == null) {
			log.error("Failed to acquire rows from GridDB");
			return List.of();
		}
		List<UserRecord> users = convertResponseToRecord(response);
		log.info("Fetched {} users from GridDB", users.size());
		return users;
	}

	public Optional<UserRecord> getOne(String id) {
		AcquireRowsRequest requestBody =
				AcquireRowsRequest.builder().limit(1L).condition("id == \'" + id + "\'").build();
		AcquireRowsResponse response = this.gridDbCloudClient.acquireRows(TBL_NAME, requestBody);
		if (response == null || response.getRows() == null) {
			log.error("Failed to acquire rows from GridDB");
			return Optional.empty();
		}
		List<UserRecord> users = convertResponseToRecord(response);
		log.info("Fetched {} users from GridDB", users.size());
		return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
	}

	public Optional<UserRecord> getOneByEmail(String email) {
		AcquireRowsRequest requestBody =
				AcquireRowsRequest.builder()
						.limit(1L)
						.condition("email == \'" + email + "\'")
						.build();
		AcquireRowsResponse response = this.gridDbCloudClient.acquireRows(TBL_NAME, requestBody);
		if (response == null || response.getRows() == null) {
			log.error("Failed to acquire rows from GridDB");
			return Optional.empty();
		}
		List<UserRecord> users = convertResponseToRecord(response);
		log.info("Fetched {} users getOneByEmail from GridDB", users.size());
		return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
	}

	private List<UserRecord> convertResponseToRecord(AcquireRowsResponse response) {
		List<UserRecord> results =
				response.getRows().stream()
						.map(
								row -> {
									try {
										var rec =
												new UserRecord(
														row.get(0).toString(),
														row.get(1).toString(),
														row.get(2).toString());
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
}
