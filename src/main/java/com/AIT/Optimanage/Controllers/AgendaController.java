package com.AIT.Optimanage.Controllers;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Agenda.AgendaSearch;
import com.AIT.Optimanage.Models.Agenda.EventoAgenda;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.AgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/agenda")
@RequiredArgsConstructor
public class AgendaController extends V1BaseController {

    private final AgendaService agendaService;

    @GetMapping
    public List<EventoAgenda> listarEventos(@AuthenticationPrincipal User loggedUser,
                                            @RequestParam(value = "data_inicial", required = false) LocalDate data_inicial,
                                            @RequestParam(value = "data_final", required = false) LocalDate data_final,
                                            @RequestParam(value = "sort", required = false) String sort,
                                            @RequestParam(value = "order", required = false) Sort.Direction order,
                                            @RequestParam(value = "page", required = true) Integer page,
                                            @RequestParam(value = "pagesize", required = true) Integer pagesize) {
        AgendaSearch pesquisa = AgendaSearch.builder()
                .dataInicial(data_inicial)
                .dataFinal(data_final)
                .page(page)
                .pageSize(pagesize)
                .sort(sort)
                .order(order)
                .build();
        return agendaService.listarEventos(loggedUser, pesquisa);
    }
}
