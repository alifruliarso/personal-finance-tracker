package com.galapea.techblog.pftgriddbcloud.service;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.galapea.techblog.pftgriddbcloud.webapi.GridDbCloudClient;
import com.galapea.techblog.pftgriddbcloud.webapi.GridDbCloudSQLStmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TransactionContainerTest {
	private GridDbCloudClient gridDbCloudClient;
	private TransactionContainer transactionContainer;

	@BeforeEach
	void setUp() {
		gridDbCloudClient = mock(GridDbCloudClient.class);
		transactionContainer = new TransactionContainer(gridDbCloudClient);
	}

	@Test
	void testSaveRecords() {
		// Arrange
		LocalDateTime transactionDate = LocalDateTime.now().minusDays(1);
		TransactionRecord record =
				new TransactionRecord(
						"id1",
						"cat1",
						100.0,
						transactionDate,
						"EXPENSE",
						"Test Description",
						"user1");

		// Act
		transactionContainer.saveRecords(List.of(record));

		// Assert
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(gridDbCloudClient)
				.registerRows(eq(transactionContainer.getTblName()), captor.capture());
		Object data = captor.getValue();
		assertNotNull(data);
		String stmt = String.valueOf(data);
		System.out.println("==============================================");
		System.out.println(stmt);
		assertTrue(stmt.contains("[["));
		assertTrue(stmt.contains("\"id1\""));
		assertTrue(stmt.contains("\"cat1\""));
		assertTrue(stmt.contains("\"100.0\""));
		assertTrue(stmt.contains("\"EXPENSE\""));
		assertTrue(stmt.contains("\"Test Description\""));
		assertTrue(stmt.contains("\"user1\""));
		assertTrue(stmt.contains("]]"));
		// Check date format
		assertTrue(stmt.matches(".*\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z.*"));
	}

	void testInsert() {
		// Arrange
		LocalDateTime transactionDate = LocalDateTime.now().minusDays(1);
		TransactionRecord record =
				new TransactionRecord(
						"id1",
						"cat1",
						100.0,
						transactionDate,
						"EXPENSE",
						"Test Description",
						"user1");

		// Act
		transactionContainer.insert(record);

		// Assert
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(gridDbCloudClient).post(eq("/sql/update"), captor.capture());
		List<GridDbCloudSQLStmt> inserts = captor.getValue();
		assertEquals(1, inserts.size());
		String stmt = inserts.get(0).statement();
		System.out.println("==============================================");
		System.out.println(stmt);
		assertTrue(stmt.contains("INSERT INTO " + transactionContainer.getTblName()));
		assertTrue(stmt.contains("'id1'"));
		assertTrue(stmt.contains("'cat1'"));
		assertTrue(stmt.contains("'100.0'"));
		assertTrue(stmt.contains("'EXPENSE'"));
		assertTrue(stmt.contains("'Test Description'"));
		assertTrue(stmt.contains("'user1'"));
		// Check date format
		assertTrue(stmt.matches(".*\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z.*"));
	}

	@Test
	void testConvertResponseToRecord() {
		// Arrange: create a mock AcquireRowsResponse with one row
		List<Object> row =
				List.of(
						"id1",
						"cat1",
						100.0,
						"2023-12-15T10:45:00.032Z",
						"EXPENSE",
						"Test Description",
						"user1");
		var response =
				mock(
						com.galapea.techblog.pftgriddbcloud.webapi.acquisition.AcquireRowsResponse
								.class);
		org.mockito.Mockito.when(response.getRows()).thenReturn(List.of(row));

		// Act
		List<TransactionRecord> records = transactionContainer.convertResponseToRecord(response);

		// Assert
		assertEquals(1, records.size());
		TransactionRecord rec = records.get(0);
		assertEquals("id1", rec.id());
		assertEquals("cat1", rec.categoryId());
		assertEquals(100.0, rec.amount());
		assertEquals(LocalDateTime.of(2023, 12, 15, 10, 45, 0, 32000000), rec.transactionDate());
		assertEquals("EXPENSE", rec.transactionType());
		assertEquals("Test Description", rec.description());
		assertEquals("user1", rec.userId());
	}
}
