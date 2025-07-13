package com.galapea.techblog.pftgriddbcloud.webapi;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import com.galapea.techblog.pftgriddbcloud.webapi.acquisition.AcquireRowsRequest;
import com.galapea.techblog.pftgriddbcloud.webapi.acquisition.AcquireRowsResponse;

public class GridDbCloudClient {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final RestClient restClient;

	public GridDbCloudClient(String baseUrl, String authToken) {
		this.restClient =
				RestClient.builder()
						.baseUrl(baseUrl)
						.defaultHeader("Authorization", "Basic " + authToken)
						.defaultHeader("Content-Type", "application/json")
						.defaultHeader("Accept", "application/json")
						.defaultStatusHandler(
								HttpStatusCode::isError,
								(request, response) -> {
									log.error(
											"GridDBCloud API Error HTTP status text: {}",
											response.getStatusText());
									// String errorBody = new
									// String(response.getBody().readAllBytes());
									String errorBody =
											StreamUtils.copyToString(
													response.getBody(), StandardCharsets.UTF_8);
									String message =
											String.format(
													"%s - %s",
													response.getStatusCode().value(), errorBody);
									log.error("GridDBCloud API Error: {}", message);
									throw new GridDbException(
											"GridDBCloud API request failed",
											response.getStatusCode(),
											errorBody);
								})
						.requestInterceptor(
								(request, body, execution) -> {
									final long begin = System.currentTimeMillis();
									ClientHttpResponse response = execution.execute(request, body);
									long duration = System.currentTimeMillis() - begin;
									log.info(
											"[HttpRequestInterceptor] {} {} {} Duration: {}s",
											response.getStatusCode().value(),
											request.getMethod(),
											request.getURI(),
											TimeUnit.MILLISECONDS.toSeconds(duration));
									log.info(
											"[HttpRequestInterceptor] Headers: {}",
											request.getHeaders());
									if (body != null && body.length > 0) {
										log.info(
												"[HttpRequestInterceptor] Body: {}",
												new String(body, StandardCharsets.UTF_8));
									}
									return response;
								})
						.build();
		checkConnection();
	}

	private void checkConnection() {
		try {
			log.info("Checking connection to GridDBCloud...");
			restClient.get().uri("/checkConnection").retrieve().toBodilessEntity();
			log.info("Connection to GridDBCloud is successful.");
		} catch (Exception e) {
			throw new GridDbException(
					"Failed to connect to GridDBCloud",
					HttpStatusCode.valueOf(500),
					e.getMessage(),
					e);
		}
	}

	public void createContainer(GridDbContainerDefinition containerDefinition) {
		try {
			restClient
					.post()
					.uri("/containers")
					.body(containerDefinition)
					.retrieve()
					.toBodilessEntity();
		} catch (GridDbException e) {
			if (e.getStatusCode().value() == 409) {
				return;
			}
			throw e;
		} catch (Exception e) {
			throw new GridDbException(
					"Failed to create container", HttpStatusCode.valueOf(500), e.getMessage(), e);
		}
	}

	public void post(String uri, Object body) {
		try {
			restClient.post().uri(uri).body(body).retrieve().toBodilessEntity();
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

	/**
	 * Registers rows of data into a specified GridDB container. For more details,
	 * refer to the <a href=
	 * "https://www.toshiba-sol.co.jp/en/pro/griddb/docs-en/v5_7/GridDB_Web_API_Reference.html#row-registration-in-a-single-container">GridDB
	 * Web API Reference</a>
	 *
	 * @param containerName
	 *            The name of the container where rows will be registered
	 * @param body
	 *            The data to be registered in the container
	 * @throws GridDbException
	 *             If there's an error during the registration process with GridDB
	 *             or if the REST request fails
	 */
	public void registerRows(String containerName, Object body) {
		try {
			ResponseEntity<String> result =
					restClient
							.put()
							.uri("/containers/" + containerName + "/rows")
							.body(body)
							.retrieve()
							.toEntity(String.class);
			log.info("Register row response:{}", result);
		} catch (GridDbException e) {
			throw e;
		} catch (Exception e) {
			throw new GridDbException(
					"Failed to execute PUT request",
					HttpStatusCode.valueOf(500),
					e.getMessage(),
					e);
		}
	}

	/**
	 * Retrieves rows from a specified GridDB container using the provided request
	 * body.
	 * <p>
	 * This method sends a POST request to the GridDB Cloud API to fetch rows from
	 * the given container according to the parameters specified in the
	 * {@link AcquireRowsRequest}. The response is mapped to an
	 * {@link AcquireRowsResponse} object containing the columns, rows, and
	 * pagination information.
	 * </p>
	 * For more details, refer to the <a href=
	 * "https://www.toshiba-sol.co.jp/en/pro/griddb/docs-en/v5_7/GridDB_Web_API_Reference.html#row-acquisition-from-a-single-container">GridDB
	 * Web API Reference</a>
	 *
	 * @param containerName
	 *            the name of the GridDB container to query
	 * @param requestBody
	 *            the request parameters for acquiring rows (offset, limit,
	 *            condition, sort, etc.)
	 * @return an {@link AcquireRowsResponse} containing the result set from the
	 *         container
	 * @throws GridDbException
	 *             if the request fails or the GridDB API returns an error
	 */
	public AcquireRowsResponse acquireRows(String containerName, AcquireRowsRequest requestBody) {
		try {
			ResponseEntity<AcquireRowsResponse> responseEntity =
					restClient
							.post()
							.uri("/containers/" + containerName + "/rows")
							.body(requestBody)
							.retrieve()
							.toEntity(AcquireRowsResponse.class);
			return responseEntity.getBody();
		} catch (GridDbException e) {
			throw e;
		} catch (Exception e) {
			throw new GridDbException(
					"Failed to execute GET request",
					HttpStatusCode.valueOf(500),
					e.getMessage(),
					e);
		}
	}

	public ContainerUpdateResult[] registerRowsMultipleContainers(Object body) {
		try {
			ResponseEntity<ContainerUpdateResult[]> result =
					restClient
							.put()
							.uri("/containers/rows")
							.body(body)
							.retrieve()
							.toEntity(ContainerUpdateResult[].class);
			log.info(
					"Registers rows in multiple containers response: {}",
					(Object) result.getBody());
			return result.getBody();
		} catch (GridDbException e) {
			throw e;
		} catch (Exception e) {
			throw new GridDbException(
					"Failed to registers rows in multiple containers",
					HttpStatusCode.valueOf(500),
					e.getMessage(),
					e);
		}
	}

	public SQLSelectResponse[] select(List<GridDbCloudSQLStmt> sqlStmts) {
		try {
			ResponseEntity<SQLSelectResponse[]> responseEntity =
					restClient
							.post()
							.uri("/sql/dml/query")
							.body(sqlStmts)
							.retrieve()
							.toEntity(SQLSelectResponse[].class);
			return responseEntity.getBody();
		} catch (GridDbException e) {
			throw e;
		} catch (Exception e) {
			throw new GridDbException(
					"Failed to execute /sql/dml/query",
					HttpStatusCode.valueOf(500),
					e.getMessage(),
					e);
		}
	}

	public SQLUpdateResponse[] update(List<GridDbCloudSQLStmt> sqlStmts) {
		try {
			ResponseEntity<SQLUpdateResponse[]> responseEntity =
					restClient
							.post()
							.uri("/sql/dml/update")
							.body(sqlStmts)
							.retrieve()
							.toEntity(SQLUpdateResponse[].class);
			return responseEntity.getBody();
		} catch (GridDbException e) {
			throw e;
		} catch (Exception e) {
			throw new GridDbException(
					"Failed to execute /sql/dml/update",
					HttpStatusCode.valueOf(500),
					e.getMessage(),
					e);
		}
	}
}
