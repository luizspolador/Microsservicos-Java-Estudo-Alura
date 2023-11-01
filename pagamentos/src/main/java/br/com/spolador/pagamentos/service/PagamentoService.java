package br.com.spolador.pagamentos.service;

import br.com.spolador.pagamentos.client.PedidoClient;
import br.com.spolador.pagamentos.dto.PagamentoDto;
import br.com.spolador.pagamentos.model.Pagamento;
import br.com.spolador.pagamentos.model.enums.Status;
import br.com.spolador.pagamentos.repository.PagamentoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class PagamentoService {
    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private ModelMapper modelMapper; // permite transferir dados entre entidade e DTO

    @Autowired
    private PedidoClient pedidoClient;





    // Método para obter todos os pagamentos de forma paginada e mapeá-los para PagamentoDto
    public Page<PagamentoDto> obterTodos(Pageable paginacao) {
        Page<Pagamento> pagamentos = pagamentoRepository.findAll(paginacao); // Recupera uma página de pagamentos do repositório com base nas informações de paginação
        return pagamentos.map((p -> modelMapper.map(p, PagamentoDto.class))); // Mapeia cada Pagamento para um PagamentoDto usando ModelMapper e retorna a página resultante
    }

    // Método para obter um pagamento pelo seu ID
    public PagamentoDto obterPorId(Long id) {
        Pagamento pagamento = pagamentoRepository.findById(id) // Tenta encontrar um pagamento no repositório com base no ID fornecido
                .orElseThrow(EntityNotFoundException::new); // Lança uma exceção se o pagamento não for encontrado

        PagamentoDto dto = modelMapper.map(pagamento, PagamentoDto.class);
        dto.setItens(pedidoClient.obterItensDoPedido(pagamento.getPedidoId()).getItens());
        return dto;
    }

    public PagamentoDto criarPagamento(PagamentoDto dto){
        Pagamento pagamento = modelMapper.map(dto, Pagamento.class); // muda de dto para Pagamento
        pagamento.setStatus(Status.CRIADO); // altera o status
        pagamentoRepository.save(pagamento); // salva no banco

        return modelMapper.map(pagamento, PagamentoDto.class); // Pagamento é novamente transformado em dto
    }

    // Método para atualizar um pagamento existente com base no ID e nos novos dados.
    public PagamentoDto atualizarPagamento(Long id, PagamentoDto dto) {
        Pagamento pagamento = modelMapper.map(dto, Pagamento.class);   // Converte o objeto PagamentoDto em um objeto Pagamento usando ModelMapper.
        pagamento.setId(id); // Define o ID do pagamento com o valor fornecido no parâmetro id.
        pagamento = pagamentoRepository.save(pagamento); // Salva o pagamento atualizado no repositório - bd

        return modelMapper.map(pagamento, PagamentoDto.class); // Converte o pagamento atualizado de volta para PagamentoDto e o retorna.
    }

    // Método para excluir um pagamento com base no ID.
    public void excluirPagamento(Long id) {
        pagamentoRepository.deleteById(id); // Remove o pagamento com o ID especificado do repositório (provavelmente, um banco de dados).
    }

    public void confirmarPagamento(Long id){
        Optional<Pagamento> pagamento = pagamentoRepository.findById(id); // Recupera o pagamento do bd

        if (!pagamento.isPresent()) {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO);  // altera o status para confirmado
        pagamentoRepository.save(pagamento.get()); // salva o pagamento
        pedidoClient.atualizaPagamento(pagamento.get().getPedidoId());
    }

    public void alteraStatus(Long id) {
        Optional<Pagamento> pagamento = pagamentoRepository.findById(id); // Recupera o pagamento do bd

        if (!pagamento.isPresent()) {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);  // altera o status para CONFIRMADO_SEM_INTEGRACAO
        pagamentoRepository.save(pagamento.get()); // salva o pagamento
    }
}
