package com.AIT.Optimanage.Models.Agenda;

import com.AIT.Optimanage.Models.Enums.TipoEvento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoAgenda {
    private TipoEvento tipo;
    private TipoEvento referencia;
    private LocalDate data;
    private LocalTime hora;
    private Duration duracao;
    private Integer id;
    private String titulo;
    private String descricao;
}
