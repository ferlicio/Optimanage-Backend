package com.AIT.Optimanage.Models;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Plano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;
    @Column(nullable = false, precision = 2)
    private Float valor;
    @Column(nullable = false)
    private Integer duracaoDias;
    @Column(nullable = false)
    private Integer qtdAcessos;

}
