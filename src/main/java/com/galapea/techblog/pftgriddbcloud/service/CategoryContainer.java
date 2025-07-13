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
public class CategoryContainer {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final GridDbCloudClient gridDbCloudClient;
	private static final String TBL_NAME = "PFTCategory";

	public CategoryContainer(GridDbCloudClient gridDbCloudClient) {
		this.gridDbCloudClient = gridDbCloudClient;
	}

	public void createTable() {
		log.info("Creating table " + TBL_NAME + " in GridDB...");
		List<GridDbColumn> columns =
				List.of(
						new GridDbColumn("id", "STRING", Set.of("TREE")),
						new GridDbColumn("name", "STRING", Set.of("TREE")));

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

	public void insert(CategoryRecord cat) {
		String stmt =
				"INSERT INTO "
						+ TBL_NAME
						+ "(id, name) VALUES ('"
						+ cat.id()
						+ "', '"
						+ cat.name()
						+ "')";
		GridDbCloudSQLStmt insert = new GridDbCloudSQLStmt(stmt);

		post("/sql/update", List.of(insert));
	}

	public void saveRecords(List<CategoryRecord> cRecords) {
		// Initialize a StringBuilder to create the JSON-like array representation
		StringBuilder sb = new StringBuilder();
		sb.append("["); // Start the outer array
		for (int i = 0; i < cRecords.size(); i++) {
			CategoryRecord record = cRecords.get(i);
			sb.append("["); // Start the inner array (each record)
			// Append each record field in order
			sb.append("\"").append(record.id()).append("\"");
			sb.append(", ");
			sb.append("\"").append(record.name()).append("\"");
			sb.append("]"); // End the inner array
			// Add a comma between record, except for the last one
			if (i < cRecords.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append("]"); // End the outer array
		String result = sb.toString();
		log.info("Categories array: {}", result);
		this.gridDbCloudClient.registerRows(TBL_NAME, result);
	}

	public List<CategoryRecord> getAll() {
		AcquireRowsRequest requestBody =
				AcquireRowsRequest.builder().limit(50L).sort("id ASC").build();
		AcquireRowsResponse response = this.gridDbCloudClient.acquireRows(TBL_NAME, requestBody);
		if (response == null || response.getRows() == null) {
			log.error("Failed to acquire rows from GridDB");
			return List.of();
		}
		List<CategoryRecord> categories = convertResponseToRecord(response);
		log.info("Fetched {} categories from GridDB", categories.size());
		return categories;
	}

	public Optional<CategoryRecord> getOne(String id) {
		AcquireRowsRequest requestBody =
				AcquireRowsRequest.builder().limit(1L).condition("id == \'" + id + "\'").build();
		AcquireRowsResponse response = this.gridDbCloudClient.acquireRows(TBL_NAME, requestBody);
		if (response == null || response.getRows() == null) {
			log.error("Failed to acquire rows from GridDB");
			return Optional.empty();
		}
		List<CategoryRecord> categories = convertResponseToRecord(response);
		log.info("Fetched {} categories from GridDB", categories.size());
		return categories.isEmpty() ? Optional.empty() : Optional.of(categories.get(0));
	}

	public Optional<CategoryRecord> getOneByName(String name) {
		AcquireRowsRequest requestBody =
				AcquireRowsRequest.builder()
						.limit(1L)
						.condition("name == \'" + name + "\'")
						.build();
		AcquireRowsResponse response = this.gridDbCloudClient.acquireRows(TBL_NAME, requestBody);
		if (response == null || response.getRows() == null) {
			log.error("Failed to acquire rows from GridDB");
			return Optional.empty();
		}
		List<CategoryRecord> categories = convertResponseToRecord(response);
		log.info("Fetched {} categories from GridDB", categories.size());
		return categories.isEmpty() ? Optional.empty() : Optional.of(categories.get(0));
	}

	private List<CategoryRecord> convertResponseToRecord(AcquireRowsResponse response) {
		List<CategoryRecord> results =
				response.getRows().stream()
						.map(
								row -> {
									try {
										var cat =
												new CategoryRecord(
														row.get(0).toString(),
														row.get(1).toString());
										return cat;
									} catch (Exception e) {
										log.error(
												"Error parsing category row: {}. Error: {}",
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
