package com.AIT.Optimanage.Models.Venda.Search;

import com.AIT.Optimanage.Models.Search;
import com.AIT.Optimanage.Models.Enums.FormaPagamento;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class VendaSearch extends Search {
    private Integer id;
    private Integer clienteId;
    private String dataInicial;
    private String dataFinal;
    private Boolean pago;
    private StatusVenda status;
    private FormaPagamento formaPagamento;
}
