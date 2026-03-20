package com.investalert.investalert.model;

import com.investalert.investalert.model.enums.TipoAtivo;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "ativo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String ticker;

    @Column(length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoAtivo tipo;

    @Column(length = 50)
    private String mercado;

    @OneToMany(mappedBy = "ativo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PrecoAtivo> precos;

    @OneToMany(mappedBy = "ativo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarteiraAtivo> carteiraAtivos;

    @OneToMany(mappedBy = "ativo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Alerta> alertas;
}