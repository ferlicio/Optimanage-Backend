package com.AIT.Optimanage.Models.Compra.Search;

import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Search;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CompraSearch extends Search {
    private Integer id;
    private Integer fornecedorId;
    private String dataInicial;
    private String dataFinal;
    private Boolean pago;
    private StatusCompra status;
    private FormaPagamento formaPagamento;
}
