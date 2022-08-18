package com.mucanze.algafood.api.model.input;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GrupoInputModel {
	
	@NotBlank
	private String nome;

}
