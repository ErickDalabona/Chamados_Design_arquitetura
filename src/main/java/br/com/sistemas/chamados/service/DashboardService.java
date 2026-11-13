package br.com.sistemas.chamados.service;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.DashboardResumo;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;

@Service
public class DashboardService {

    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ChamadoRepository chamadoRepository;

    public DashboardService(ClienteRepository clienteRepository,
                            CategoriaRepository categoriaRepository,
                            TecnicoRepository tecnicoRepository,
                            ChamadoRepository chamadoRepository) {
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResumo resumo() {
        Map<StatusChamado, Long> porStatus = new EnumMap<>(StatusChamado.class);
        for (StatusChamado status : StatusChamado.values()) {
            porStatus.put(status, chamadoRepository.countByStatus(status));
        }
        return new DashboardResumo(
            clienteRepository.count(),
            categoriaRepository.count(),
            tecnicoRepository.count(),
            chamadoRepository.count(),
            porStatus);
    }
}