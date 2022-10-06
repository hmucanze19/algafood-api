package com.mucanze.algafood.domain.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mucanze.algafood.domain.model.FotoProduto;
import com.mucanze.algafood.domain.repository.ProdutoRepository;

@Service
public class CatalogoFotoProdutoService {
	
	@Autowired
	private ProdutoRepository produtoRepository;
	
	@Transactional
	public FotoProduto salvar(FotoProduto fotoProduto) {
		Long restauranteId = fotoProduto.getRestauranteId();
		Long produtoId = fotoProduto.getProduto().getId();
		
		Optional<FotoProduto> fotoRetornada = produtoRepository.findFotoById(restauranteId, produtoId);
		
		if(fotoRetornada.isPresent()) {
			produtoRepository.delete(fotoRetornada.get());
		}
		
		return produtoRepository.save(fotoProduto);
	}

}
