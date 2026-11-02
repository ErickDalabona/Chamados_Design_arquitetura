package br.com.sistemas.chamados.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    private static final String JSON_VALIDO =
        "{\"nome\":\"Ana Souza\",\"email\":\"ana@exemplo.com\",\"telefone\":\"47999999999\"}";

    private static final String JSON_INVALIDO =
        "{\"nome\":\"\",\"email\":\"email-invalido\",\"telefone\":\"123\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @MockitoBean
    private ChamadoService chamadoService;

    private ClienteResponse ana() {
        return new ClienteResponse(1L, "Ana Souza", "ana@exemplo.com", "47999999999");
    }

    @Test
    void criaClienteRetorna201() throws Exception {
        when(service.criar(any(ClienteRequest.class))).thenReturn(ana());

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/clientes/1"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("Ana Souza"));
    }

    @Test
    void dadosInvalidosRetornam400() throws Exception {
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_INVALIDO))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detalhes").isNotEmpty());

        verifyNoInteractions(service);
    }

    @Test
    void emailDuplicadoRetorna409() throws Exception {
        when(service.criar(any(ClienteRequest.class)))
            .thenThrow(new RegraNegocioException("Ja existe cliente com o e-mail ana@exemplo.com"));

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void buscarIdInexistenteRetorna404() throws Exception {
        when(service.buscar(99999L))
            .thenThrow(new RecursoNaoEncontradoException("Cliente 99999 nao encontrado"));

        mockMvc.perform(get("/clientes/99999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listaClientesRetorna200() throws Exception {
        when(service.listar()).thenReturn(List.of(ana()));

        mockMvc.perform(get("/clientes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("Ana Souza"));
    }

    @Test
    void excluirRetorna204() throws Exception {
        mockMvc.perform(delete("/clientes/1"))
            .andExpect(status().isNoContent());

        verify(service).excluir(1L);
    }
}
