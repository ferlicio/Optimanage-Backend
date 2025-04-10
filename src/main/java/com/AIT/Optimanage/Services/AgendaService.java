package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Models.Agenda.AgendaSearch;
import com.AIT.Optimanage.Models.Agenda.EventoAgenda;
import com.AIT.Optimanage.Models.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgendaService {

    public List<EventoAgenda> listarEventos(User loggedUser, AgendaSearch pesquisa) {
        Sort.Direction direction = Optional.ofNullable(pesquisa.getOrder()).filter(Sort.Direction::isDescending)
                .map(order -> Sort.Direction.DESC).orElse(Sort.Direction.ASC);

        String sortBy = Optional.ofNullable(pesquisa.getSort()).orElse("id");
        Pageable pageable = PageRequest.of(pesquisa.getPage(), pesquisa.getPageSize(), Sort.by(direction, sortBy));


        return null;
    }
}
