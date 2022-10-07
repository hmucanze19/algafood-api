package com.mucanze.algafood.domain.service;

import java.io.InputStream;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

public interface FotoStorageService {
	
	public void armazenar(NovaFoto novaFoto);
	
	public default String gerarNomeArquivo(String nome) {
		return UUID.randomUUID().toString() + "-" + nome;
	}
	
	@Builder
	@Getter
	public class NovaFoto {
		private String nomeArquivo;
		private InputStream inputStream;
	}

}