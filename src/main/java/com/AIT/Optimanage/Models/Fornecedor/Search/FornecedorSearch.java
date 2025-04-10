package com.AIT.Optimanage.Models.Fornecedor.Search;

import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.Search;
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
public class FornecedorSearch extends Search {
    private String nome;
    private String cpfOuCnpj;
    private String estado;
    private Integer atividade;
    private TipoPessoa tipoPessoa;
    private Boolean ativo;
    private Integer id;
}
