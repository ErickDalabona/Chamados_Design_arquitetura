package br.com.sistemas.chamados.dto;

import java.util.Map;

import br.com.sistemas.chamados.entity.StatusChamado;

public record DashboardResumo(
    long clientes,
    long categorias,
    long tecnicos,
    long chamados,
    Map<StatusChamado, Long> porStatus
) { }