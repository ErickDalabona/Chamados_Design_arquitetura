package br.com.sistemas.chamados.controller.web;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.ChamadoForm;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.StatusRequest;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.ComentarioService;
import br.com.sistemas.chamados.service.TecnicoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/app/chamados")
public class ChamadoWebController {

    private static final String LISTA = "redirect:/app/chamados";

    private final ChamadoService service;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;
    private final ComentarioService comentarioService;

    public ChamadoWebController(ChamadoService service,
                                ClienteService clienteService,
                                CategoriaService categoriaService,
                                TecnicoService tecnicoService,
                                ComentarioService comentarioService) {
        this.service = service;
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
        this.comentarioService = comentarioService;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {
        var pageable = PageRequest.of(pagina, 10,
            Sort.by(Sort.Direction.DESC, "dataAbertura"));
        var resultado = service.listar(status, prioridade, clienteId, pageable);
        model.addAttribute("chamados", resultado.conteudo());
        model.addAttribute("totalPaginas", resultado.totalPaginas());
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("statusFiltro", status);
        model.addAttribute("prioridadeFiltro", prioridade);
        model.addAttribute("clienteIdFiltro", clienteId);
        model.addAttribute("statusOpcoes", StatusChamado.values());
        model.addAttribute("prioridadeOpcoes", Prioridade.values());
        model.addAttribute("clientes", clienteService.listar());
        return "chamados/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("chamado", new ChamadoForm());
        return formulario(model, "Novo chamado", "/app/chamados");
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("chamado") ChamadoForm form,
                        BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formulario(model, "Novo chamado", "/app/chamados");
        }
        try {
            service.criar(paraRequest(form));
        } catch (RegraNegocioException e) {
            model.addAttribute("erroGeral", e.getMessage());
            return formulario(model, "Novo chamado", "/app/chamados");
        }
        redirect.addFlashAttribute("mensagem", "Chamado aberto com sucesso.");
        return LISTA;
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        ChamadoResponse c = service.buscar(id);
        model.addAttribute("chamado", c);
        model.addAttribute("comentarios", comentarioService.listar(id));
        model.addAttribute("tecnicos", tecnicoService.listar());
        model.addAttribute("statusOpcoes", StatusChamado.values());
        model.addAttribute("comentarioForm", new ComentarioRequest("", ""));
        return "chamados/detalhe";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id,
                                @RequestParam StatusChamado novoStatus,
                                RedirectAttributes redirect) {
        try {
            service.alterarStatus(id, new StatusRequest(novoStatus));
            redirect.addFlashAttribute("mensagem", "Status alterado para " + novoStatus + ".");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/app/chamados/" + id;
    }

    @PostMapping("/{id}/atribuir")
    public String atribuir(@PathVariable Long id,
                           @RequestParam Long tecnicoId,
                           RedirectAttributes redirect) {
        try {
            service.atribuir(id,
                new br.com.sistemas.chamados.dto.AtribuirTecnicoRequest(tecnicoId));
            redirect.addFlashAttribute("mensagem", "Técnico atribuído com sucesso.");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/app/chamados/" + id;
    }

    @PostMapping("/{id}/comentarios")
    public String comentar(@PathVariable Long id,
                           @RequestParam String texto,
                           @RequestParam String autor,
                           RedirectAttributes redirect) {
        if (texto == null || texto.isBlank() || autor == null || autor.isBlank()) {
            redirect.addFlashAttribute("erro", "Texto e autor são obrigatórios.");
        } else {
            comentarioService.criar(id, new ComentarioRequest(texto, autor));
            redirect.addFlashAttribute("mensagem", "Comentário registrado.");
        }
        return "redirect:/app/chamados/" + id;
    }

    private String formulario(Model model, String titulo, String acao) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("acao", acao);
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("tecnicos", tecnicoService.listar());
        return "chamados/form";
    }

    private ChamadoRequest paraRequest(ChamadoForm form) {
        return new ChamadoRequest(
            form.getTitulo(), form.getDescricao(), form.getPrioridade(),
            form.getClienteId(), form.getCategoriaId(), form.getTecnicoId());
    }
}