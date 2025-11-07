package com.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	private Long id;
	private String name;
	private String email;
	public User(String name, String email) {
		this.name = name;
		this.email = email;
	}
}