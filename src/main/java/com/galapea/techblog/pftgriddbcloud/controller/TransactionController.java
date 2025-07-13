package com.galapea.techblog.pftgriddbcloud.controller;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.galapea.techblog.pftgriddbcloud.model.CategoryDTO;
import com.galapea.techblog.pftgriddbcloud.model.CategoryResponse;
import com.galapea.techblog.pftgriddbcloud.model.TransactionDTO;
import com.galapea.techblog.pftgriddbcloud.model.TransactionResponse;
import com.galapea.techblog.pftgriddbcloud.model.TransactionType;
import com.galapea.techblog.pftgriddbcloud.model.UserDTO;
import com.galapea.techblog.pftgriddbcloud.model.UserResponse;
import com.galapea.techblog.pftgriddbcloud.service.CategoryService;
import com.galapea.techblog.pftgriddbcloud.service.TableSeeder;
import com.galapea.techblog.pftgriddbcloud.service.TransactionService;
import com.galapea.techblog.pftgriddbcloud.service.UserService;
import com.galapea.techblog.pftgriddbcloud.util.WebUtils;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

	private final TransactionService transactionService;
	private final CategoryService categoryService;
	private final UserService userService;
	private final Map<String, String> transactionTypes =
			Arrays.stream(TransactionType.values())
					.collect(Collectors.toMap(Enum::name, TransactionType::getLabel));

	public TransactionController(
			final TransactionService transactionService,
			final CategoryService categoryService,
			UserService userService) {
		this.transactionService = transactionService;
		this.categoryService = categoryService;
		this.userService = userService;
	}

	@ModelAttribute
	public void prepareContext(final Model model) {
		Map<String, String> categories =
				categoryService.findAll().stream()
						.collect(
								LinkedHashMap::new, // Preserve insertion order
								(map, category) -> map.put(category.getId(), category.getName()),
								Map::putAll);
		model.addAttribute("categoryIdValues", categories);
		model.addAttribute("transactionTypeValues", transactionTypes);
	}

	@GetMapping
	public String list(final Model model) {
		List<CategoryResponse> categories =
				categoryService.findAll().stream()
						.map(category -> new CategoryResponse(category.getId(), category.getName()))
						.collect(Collectors.toList());
		List<UserResponse> users =
				userService.findAll().stream()
						.map(user -> new UserResponse(user.getId(), user.getFullName()))
						.collect(Collectors.toList());
		List<TransactionResponse> transactions =
				transactionService.findAll().stream()
						.map(
								transaction ->
										new TransactionResponse(
												transaction.getId(),
												findCategory(
														categories, transaction.getCategoryId()),
												transaction.getAmount(),
												transaction.getTransactionDate(),
												transaction.getTransactionType(),
												transaction.getDescription(),
												findUser(users, transaction.getUserId())))
						.collect(Collectors.toList());
		model.addAttribute("transactions", transactions);
		return "transaction/list";
	}

	private UserResponse findUser(List<UserResponse> users, String userId) {
		return users.stream()
				.filter(user -> user.id().equals(userId))
				.findFirst()
				.orElse(new UserResponse("N/A", "N/A"));
	}

	private CategoryResponse findCategory(List<CategoryResponse> categories, String categoryId) {
		return categories.stream()
				.filter(category -> category.id().equals(categoryId))
				.findFirst()
				.orElse(new CategoryResponse("N/A", "N/A"));
	}

	@GetMapping("/add")
	public String add(@ModelAttribute("transaction") final TransactionDTO transactionDTO) {
		return "transaction/add";
	}

	@PostMapping("/add")
	public String add(
			@ModelAttribute("transaction") @Valid final TransactionDTO transactionDTO,
			final BindingResult bindingResult,
			final RedirectAttributes redirectAttributes) {
		System.out.println("<<<<<Binding result: " + bindingResult);
		bindingResult
				.getFieldErrors()
				.forEach(
						error -> {
							System.out.println(
									">>>>>Field error: "
											+ error.getField()
											+ " - "
											+ error.getDefaultMessage());
						});
		if (bindingResult.hasErrors()) {
			return "transaction/add";
		}

		transactionDTO.setUserId(userService.findAll().get(0).getId()); // Set default userId

		transactionService.createAll(List.of(transactionDTO));
		redirectAttributes.addFlashAttribute(
				WebUtils.MSG_SUCCESS, WebUtils.getMessage("transaction.create.success"));
		return "redirect:/transactions";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable(name = "id") final String id, final Model model) {
		model.addAttribute("transaction", transactionService.get(id));
		return "transaction/edit";
	}

	@PostMapping("/edit/{id}")
	public String edit(
			@PathVariable(name = "id") final String id,
			@ModelAttribute("transaction") @Valid final TransactionDTO transactionDTO,
			final BindingResult bindingResult,
			final RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "transaction/edit";
		}
		transactionService.update(id, transactionDTO);
		redirectAttributes.addFlashAttribute(
				WebUtils.MSG_SUCCESS, WebUtils.getMessage("transaction.update.success"));
		return "redirect:/transactions";
	}

	@PostMapping("/delete/{id}")
	public String delete(
			@PathVariable(name = "id") final String id,
			final RedirectAttributes redirectAttributes) {
		transactionService.delete(id);
		redirectAttributes.addFlashAttribute(
				WebUtils.MSG_INFO, WebUtils.getMessage("transaction.delete.success"));
		return "redirect:/transactions";
	}

	@PostMapping("/generate")
	public String generateDummy() {
		List<CategoryDTO> categories = categoryService.findAll();
		UserDTO currentUser = userService.getByEmail(TableSeeder.DUMMY_USER1_EMAIL);
		if (currentUser == null) {
			currentUser = new UserDTO();
			currentUser.setEmail(TableSeeder.DUMMY_USER1_EMAIL);
			currentUser.setFullName(TableSeeder.DUMMY_USER1_FULLNAME);
			userService.create(currentUser);
		}

		// Generate 20 dummy transactions
		java.time.LocalDate today = java.time.LocalDate.now();
		java.util.Random random = new java.util.Random();
		List<TransactionDTO> dummyTransactions = new java.util.ArrayList<>();
		for (int i = 0; i < 20; i++) {
			CategoryDTO category = categories.get(random.nextInt(categories.size()));
			int daysAgo = 1 + random.nextInt(15); // 1 to 15 days ago
			java.time.LocalDate date = today.minusDays(daysAgo);
			java.time.LocalDateTime transactionDate = date.atStartOfDay();
			double amount = 10 + random.nextInt(191); // 10 to 200
			String description = "Pay " + amount + " for " + category.getName();

			TransactionDTO dto = new TransactionDTO();
			dto.setUserId(currentUser.getId());
			dto.setCategoryId(category.getId());
			dto.setTransactionDate(transactionDate);
			dto.setAmount(amount);
			dto.setDescription(description);
			dto.setTransactionType(
					com.galapea.techblog.pftgriddbcloud.model.TransactionType.EXPENSE.name());
			dummyTransactions.add(dto);
		}
		transactionService.createAll(dummyTransactions);
		return "redirect:/transactions";
	}
}
