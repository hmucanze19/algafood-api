package com.mucanze.algafood.api.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mucanze.algafood.api.assembler.PedidoInputModelDisassembler;
import com.mucanze.algafood.api.assembler.PedidoOutputModelAssembler;
import com.mucanze.algafood.api.assembler.PedidoOutputResumoModelAssembler;
import com.mucanze.algafood.api.model.input.PedidoInputModel;
import com.mucanze.algafood.api.model.output.PedidoOutputModel;
import com.mucanze.algafood.api.model.output.PedidoOutputResumoModel;
import com.mucanze.algafood.domain.exception.EntidadeInexistenteException;
import com.mucanze.algafood.domain.exception.NegocioException;
import com.mucanze.algafood.domain.model.Pedido;
import com.mucanze.algafood.domain.model.Usuario;
import com.mucanze.algafood.domain.repository.PedidoRepository;
import com.mucanze.algafood.domain.service.EmissaoPedidoService;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

	@Autowired
	private PedidoRepository pedidoRepository;
	
	@Autowired
	private PedidoOutputModelAssembler pedidoOutputModelAssembler;
	
	@Autowired
	private PedidoOutputResumoModelAssembler pedidoOutputResumoModelAssembler;
	
	@Autowired
	private EmissaoPedidoService emissaoPedidoService;
	
	@Autowired
	private PedidoInputModelDisassembler pedidoInputModelDisassembler;
	
	@GetMapping
	public List<PedidoOutputResumoModel> listar() {
		return pedidoOutputResumoModelAssembler.toCollectionModel(pedidoRepository.findAll());
	}
	
	@GetMapping("/{id}")
	public PedidoOutputModel buscarPorId(@PathVariable Long id) {
		return pedidoOutputModelAssembler.toModel(emissaoPedidoService.buscarPorId(id));
	}
	
	@PostMapping
	public PedidoOutputModel salvar(@RequestBody @Valid PedidoInputModel pedidoInputModel) {
		try {
			Pedido pedido = pedidoInputModelDisassembler.toDomainObject(pedidoInputModel);
			Usuario cliente = new Usuario();
			cliente.setId(1L);
			
			pedido.setCliente(cliente);
			
			pedido = emissaoPedidoService.salvar(pedido);
			
			return pedidoOutputModelAssembler.toModel(pedido);
		} catch (EntidadeInexistenteException e) {
			throw new NegocioException(e.getMessage());
		}
	}
	
}
