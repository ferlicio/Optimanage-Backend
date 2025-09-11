package com.AIT.Optimanage.Mappers;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraPagamento;
import com.AIT.Optimanage.Models.Compra.CompraProduto;
import com.AIT.Optimanage.Models.Compra.CompraServico;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraPagamentoResponseDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraProdutoResponseDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraResponseDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraServicoResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompraMapper {

    @Mapping(target = "fornecedorId", source = "fornecedor.id")
    @Mapping(target = "produtos", source = "compraProdutos")
    @Mapping(target = "servicos", source = "compraServicos")
    CompraResponseDTO toResponse(Compra compra);

    @Mapping(target = "produtoId", source = "produto.id")
    CompraProdutoResponseDTO toResponse(CompraProduto compraProduto);

    @Mapping(target = "servicoId", source = "servico.id")
    CompraServicoResponseDTO toResponse(CompraServico compraServico);

    CompraPagamentoResponseDTO toResponse(CompraPagamento compraPagamento);
}
