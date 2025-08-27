package com.AIT.Optimanage.Controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoResponse {

    private Integer id;
    private String nome;
    private Float valor;
    private Integer duracaoDias;
    private Integer qtdAcessos;
}

