package com.galapea.techblog.pftgriddbcloud.controller;

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
import com.galapea.techblog.pftgriddbcloud.service.CategoryService;
import com.galapea.techblog.pftgriddbcloud.util.WebUtils;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/categories")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(final CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	public String list(final Model model) {
		model.addAttribute("categories", categoryService.findAll());
		return "category/list";
	}

	@GetMapping("/add")
	public String add(@ModelAttribute("category") final CategoryDTO categoryDTO) {
		return "category/add";
	}

	@PostMapping("/add")
	public String add(
			@ModelAttribute("category") @Valid final CategoryDTO categoryDTO,
			final BindingResult bindingResult,
			final RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "category/add";
		}
		categoryService.create(categoryDTO);
		redirectAttributes.addFlashAttribute(
				WebUtils.MSG_SUCCESS, WebUtils.getMessage("category.create.success"));
		return "redirect:/categories";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable(name = "id") final String id, final Model model) {
		model.addAttribute("category", categoryService.get(id));
		return "category/edit";
	}

	@PostMapping("/edit/{id}")
	public String edit(
			@PathVariable(name = "id") final String id,
			@ModelAttribute("category") @Valid final CategoryDTO categoryDTO,
			final BindingResult bindingResult,
			final RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "category/edit";
		}
		categoryService.update(id, categoryDTO);
		redirectAttributes.addFlashAttribute(
				WebUtils.MSG_SUCCESS, WebUtils.getMessage("category.update.success"));
		return "redirect:/categories";
	}

	@PostMapping("/delete/{id}")
	public String delete(
			@PathVariable(name = "id") final String id,
			final RedirectAttributes redirectAttributes) {
		categoryService.delete(id);
		redirectAttributes.addFlashAttribute(
				WebUtils.MSG_INFO, WebUtils.getMessage("category.delete.success"));
		return "redirect:/categories";
	}
}
