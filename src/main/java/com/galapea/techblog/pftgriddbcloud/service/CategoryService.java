package com.galapea.techblog.pftgriddbcloud.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.galapea.techblog.pftgriddbcloud.model.CategoryDTO;
import com.galapea.techblog.pftgriddbcloud.util.NotFoundException;
import com.galapea.techblog.pftgriddbcloud.util.NotImplementedException;
import com.github.f4b6a3.tsid.TsidCreator;

@Service
public class CategoryService {

	private final CategoryContainer categoryContainer;

	public CategoryService(CategoryContainer categoryContainer) {
		this.categoryContainer = categoryContainer;
	}

	public static String nextId() {
		return "cat_" + TsidCreator.getTsid().format("%S");
	}

	public List<CategoryDTO> findAll() {
		final List<CategoryRecord> categories = categoryContainer.getAll();
		return categories.stream()
				.map(category -> mapToDTO(category, new CategoryDTO()))
				.collect(Collectors.toList());
	}

	public CategoryDTO get(final String id) {
		return categoryContainer
				.getOne(id)
				.map(category -> mapToDTO(category, new CategoryDTO()))
				.orElseThrow(NotFoundException::new);
	}

	public String create(final CategoryDTO categoryDTO) {
		String id = (categoryDTO.getId() != null) ? categoryDTO.getId() : nextId();
		CategoryRecord newCategory = new CategoryRecord(id, categoryDTO.getName());
		categoryContainer.saveRecords(List.of(newCategory));
		return id;
	}

	public void createAll(List<CategoryDTO> categoryDTOs) {
		List<CategoryRecord> categoryRecords =
				categoryDTOs.stream()
						.map(
								categoryDTO ->
										new CategoryRecord(
												(categoryDTO.getId() != null)
														? categoryDTO.getId()
														: nextId(),
												categoryDTO.getName()))
						.collect(Collectors.toList());
		if (!categoryRecords.isEmpty()) {
			categoryContainer.saveRecords(categoryRecords);
		}
	}

	public void update(final String id, final CategoryDTO categoryDTO) {
		CategoryRecord updatedCategory = new CategoryRecord(id, categoryDTO.getName());
		categoryContainer.saveRecords(List.of(updatedCategory));
	}

	public void delete(final String id) {
		throw new NotImplementedException("Delete operation is not implemented yet.");
	}

	private CategoryDTO mapToDTO(final CategoryRecord category, final CategoryDTO categoryDTO) {
		categoryDTO.setId(category.id());
		categoryDTO.setName(category.name());
		return categoryDTO;
	}

	public boolean idExists(final String id) {
		return categoryContainer.getOne(id).isPresent();
	}

	public boolean nameExists(final String name) {
		return categoryContainer.getOneByName(name).isPresent();
	}

	public void createTable() {
		categoryContainer.createTable();
	}
}
