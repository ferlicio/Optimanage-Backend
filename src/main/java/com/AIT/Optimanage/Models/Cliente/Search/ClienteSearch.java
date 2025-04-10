package com.AIT.Optimanage.Models.Cliente.Search;

import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.Search;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ClienteSearch extends Search {
    private String nome;
    private String cpfOuCnpj;
    private String estado;
    private Integer atividade;
    private TipoPessoa tipoPessoa;
    private Boolean ativo;
    private Integer id;
}
