package com.galapea.techblog.pftgriddbcloud.service;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.galapea.techblog.pftgriddbcloud.model.CategoryDTO;
import com.galapea.techblog.pftgriddbcloud.model.UserDTO;

@Component
public class TableSeeder implements CommandLineRunner {

	public static final String DUMMY_USER1_FULLNAME = "John Doe";
	public static final String DUMMY_USER1_EMAIL = "j@doe.com";
	private final CategoryService categoryService;
	private final TransactionService transactionService;
	private final UserService userService;

	public TableSeeder(
			final CategoryService categoryService,
			final TransactionService transactionService,
			final UserService userService) {
		this.categoryService = categoryService;
		this.transactionService = transactionService;
		this.userService = userService;
	}

	@Override
	public void run(final String... args) {
		categoryService.createTable();
		transactionService.createTable();
		userService.createTable();

		if (categoryService.findAll().isEmpty()) {
			List<CategoryDTO> categories =
					List.of(
							CategoryDTO.builder().name("Default").build(),
							CategoryDTO.builder().name("Food").build(),
							CategoryDTO.builder().name("Transport").build(),
							CategoryDTO.builder().name("Electricity").build(),
							CategoryDTO.builder().name("Shopping").build(),
							CategoryDTO.builder().name("Entertainment").build(),
							CategoryDTO.builder().name("Health").build(),
							CategoryDTO.builder().name("Utilities").build());
			categoryService.createAll(categories);
		}

		UserDTO user = new UserDTO();
		user.setEmail(DUMMY_USER1_EMAIL);
		user.setFullName(DUMMY_USER1_FULLNAME);
		if (!userService.emailExists(user.getEmail())) {
			userService.create(user);
		}
	}
}
