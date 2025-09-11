package com.AIT.Optimanage.Mappers;

import com.AIT.Optimanage.Models.Venda.DTOs.VendaPagamentoResponseDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaProdutoResponseDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaResponseDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaServicoResponseDTO;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Models.Venda.VendaPagamento;
import com.AIT.Optimanage.Models.Venda.VendaProduto;
import com.AIT.Optimanage.Models.Venda.VendaServico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VendaMapper {

    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "produtos", source = "vendaProdutos")
    @Mapping(target = "servicos", source = "vendaServicos")
    @Mapping(target = "pagamentos", source = "pagamentos")
    VendaResponseDTO toResponse(Venda venda);

    @Mapping(target = "produtoId", source = "produto.id")
    VendaProdutoResponseDTO toResponse(VendaProduto vendaProduto);

    @Mapping(target = "servicoId", source = "servico.id")
    VendaServicoResponseDTO toResponse(VendaServico vendaServico);

    VendaPagamentoResponseDTO toResponse(VendaPagamento vendaPagamento);
}
