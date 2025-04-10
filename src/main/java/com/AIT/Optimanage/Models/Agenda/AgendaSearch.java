package com.AIT.Optimanage.Models.Agenda;

import com.AIT.Optimanage.Models.Search;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Setter
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AgendaSearch extends Search {
    private LocalDate dataInicial;
    private LocalDate dataFinal;
}
