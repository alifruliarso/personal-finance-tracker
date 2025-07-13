package com.galapea.techblog.pftgriddbcloud.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.galapea.techblog.pftgriddbcloud.model.TransactionSummary;
import com.galapea.techblog.pftgriddbcloud.model.UserDTO;
import com.galapea.techblog.pftgriddbcloud.service.TableSeeder;
import com.galapea.techblog.pftgriddbcloud.service.TransactionService;
import com.galapea.techblog.pftgriddbcloud.service.UserService;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

	private final TransactionService transactionService;
	private final UserService userService;

	public DashboardController(
			final TransactionService transactionService, UserService userService) {
		this.transactionService = transactionService;
		this.userService = userService;
	}

	@RequestMapping
	public String dashboard(final Model model) {
		UserDTO currentUser = userService.getByEmail(TableSeeder.DUMMY_USER1_EMAIL);
		List<TransactionSummary> transactionSum =
				transactionService.getTransactionSummary(currentUser.getId());
		// List.of(
		// 		new TransactionSummary("2025-06-29", 3, 0),
		// 		new TransactionSummary("2025-06-30", 10, 150),
		// 		new TransactionSummary("2025-07-01", 99, 0),
		// 		new TransactionSummary("2025-07-02", 7, 0),
		// 		new TransactionSummary("2025-07-03", 72, 50));
		model.addAttribute("transactionSummary", transactionSum);
		double totalIncome =
				transactionSum.stream().mapToDouble(TransactionSummary::incomeAmount).sum();
		double totalExpenses =
				transactionSum.stream().mapToDouble(TransactionSummary::expenseAmount).sum();
		double difference = totalIncome - totalExpenses;
		model.addAttribute("totalIncome", totalIncome);
		model.addAttribute("totalExpenses", totalExpenses);
		model.addAttribute("totalDifference", difference);
		return "dashboard/index.html";
	}
}
