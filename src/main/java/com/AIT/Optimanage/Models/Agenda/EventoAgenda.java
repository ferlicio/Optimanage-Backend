package com.AIT.Optimanage.Models.Agenda;

import com.AIT.Optimanage.Models.Enums.TipoEvento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoAgenda {
    private TipoEvento tipo;
    private TipoEvento referencia;
    private LocalDate data;
    private Integer id;
    private String titulo;
    private String descricao;
}
