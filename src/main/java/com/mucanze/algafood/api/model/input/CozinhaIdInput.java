package com.mucanze.algafood.api.model.input;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CozinhaIdInput {
	
	@NotNull
	private Long cozinhaId;

}
