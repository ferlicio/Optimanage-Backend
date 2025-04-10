package com.AIT.Optimanage.Models.Venda.Compatibilidade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompatibilidadeDTO {
    private Integer produtoId;
    private Integer servicoId;
    private Integer contextoId;
    private Boolean compativel;
}
