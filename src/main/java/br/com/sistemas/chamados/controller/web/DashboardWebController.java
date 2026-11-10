package br.com.sistemas.chamados.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.sistemas.chamados.service.DashboardService;

@Controller
public class DashboardWebController {

    private final DashboardService service;

    public DashboardWebController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/app";
    }

    @GetMapping("/app")
    public String dashboard(Model model) {
        model.addAttribute("resumo", service.resumo());
        return "dashboard";
    }
}