package br.com.sistemas.chamados.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.ClienteForm;
import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ClienteService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/app/clientes")
public class ClienteWebController {

    private static final String LISTA = "redirect:/app/clientes";

    private final ClienteService service;

    public ClienteWebController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", service.listar());
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new ClienteForm());
        return formulario(model, "Novo cliente", "/app/clientes");
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("cliente") ClienteForm form,
                        BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formulario(model, "Novo cliente", "/app/clientes");
        }
        try {
            service.criar(paraRequest(form));
        } catch (RegraNegocioException e) {
            result.rejectValue("email", "duplicado", e.getMessage());
            return formulario(model, "Novo cliente", "/app/clientes");
        }
        redirect.addFlashAttribute("mensagem", "Cliente cadastrado com sucesso.");
        return LISTA;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        ClienteResponse c = service.buscar(id);
        ClienteForm form = new ClienteForm();
        form.setNome(c.nome());
        form.setEmail(c.email());
        form.setTelefone(c.telefone());
        model.addAttribute("cliente", form);
        return formulario(model, "Editar cliente", "/app/clientes/" + id);
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("cliente") ClienteForm form,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formulario(model, "Editar cliente", "/app/clientes/" + id);
        }
        try {
            service.atualizar(id, paraRequest(form));
        } catch (RegraNegocioException e) {
            result.rejectValue("email", "duplicado", e.getMessage());
            return formulario(model, "Editar cliente", "/app/clientes/" + id);
        }
        redirect.addFlashAttribute("mensagem", "Cliente atualizado com sucesso.");
        return LISTA;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.excluir(id);
            redirect.addFlashAttribute("mensagem", "Cliente excluído.");
        } catch (RecursoNaoEncontradoException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erro",
                "Não foi possível excluir o cliente. Verifique se ele possui chamados.");
        }
        return LISTA;
    }

    private String formulario(Model model, String titulo, String acao) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("acao", acao);
        return "clientes/form";
    }

    private ClienteRequest paraRequest(ClienteForm form) {
        return new ClienteRequest(form.getNome(), form.getEmail(), form.getTelefone());
    }
}