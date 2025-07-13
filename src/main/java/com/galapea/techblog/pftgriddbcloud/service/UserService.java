package com.galapea.techblog.pftgriddbcloud.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.galapea.techblog.pftgriddbcloud.model.UserDTO;
import com.galapea.techblog.pftgriddbcloud.util.NotFoundException;
import com.galapea.techblog.pftgriddbcloud.util.NotImplementedException;
import com.github.f4b6a3.tsid.TsidCreator;

@Service
public class UserService {

	private final UserContainer userContainer;

	public UserService(final UserContainer userContainer) {
		this.userContainer = userContainer;
	}

	public static String nextId() {
		return "usr_" + TsidCreator.getTsid().format("%S");
	}

	public List<UserDTO> findAll() {
		final List<UserRecord> users = userContainer.getAll();
		return users.stream()
				.map(user -> mapToDTO(user, new UserDTO()))
				.collect(Collectors.toList());
	}

	public UserDTO get(final String id) {
		return userContainer
				.getOne(id)
				.map(user -> mapToDTO(user, new UserDTO()))
				.orElseThrow(NotFoundException::new);
	}

	public UserDTO getByEmail(final String email) {
		return userContainer
				.getOneByEmail(email)
				.map(user -> mapToDTO(user, new UserDTO()))
				.orElseThrow(NotFoundException::new);
	}

	public String create(final UserDTO userDTO) {
		final UserRecord user =
				new UserRecord(
						userDTO.getId() != null ? userDTO.getId() : nextId(),
						userDTO.getEmail(),
						userDTO.getFullName());
		userContainer.saveRecords(List.of(user));
		return user.id();
	}

	public void update(final String id, final UserDTO userDTO) {
		final UserRecord user = new UserRecord(id, userDTO.getEmail(), userDTO.getFullName());
		userContainer.saveRecords(List.of(user));
	}

	public void delete(final String id) {
		throw new NotImplementedException("Delete operation is not implemented yet.");
	}

	private UserDTO mapToDTO(final UserRecord user, final UserDTO userDTO) {
		userDTO.setId(user.id());
		userDTO.setEmail(user.email());
		userDTO.setFullName(user.fullName());
		return userDTO;
	}

	public boolean idExists(final String id) {
		return userContainer.getOne(id).isPresent();
	}

	public boolean emailExists(final String email) {
		return userContainer.getOneByEmail(email).isPresent();
	}

	public void createTable() {
		userContainer.createTable();
	}
}
