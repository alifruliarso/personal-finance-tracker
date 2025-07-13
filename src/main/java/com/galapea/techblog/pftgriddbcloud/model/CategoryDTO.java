package com.galapea.techblog.pftgriddbcloud.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoryDTO {

	@Size(max = 255)
	@CategoryIdValid
	private String id;

	@NotNull
	@Size(max = 255)
	@CategoryNameUnique
	private String name;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String id;
		private String name;

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public CategoryDTO build() {
			CategoryDTO dto = new CategoryDTO();
			dto.setId(this.id);
			dto.setName(this.name);
			return dto;
		}
	}
}
