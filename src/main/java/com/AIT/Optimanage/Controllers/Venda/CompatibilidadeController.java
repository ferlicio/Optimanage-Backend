package com.AIT.Optimanage.Controllers.Venda;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.Compatibilidade;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.CompatibilidadeDTO;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Venda.CompatibilidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compatibilidades")
@RequiredArgsConstructor
public class CompatibilidadeController extends V1BaseController {

    private final CompatibilidadeService compatibilidadeService;

    @GetMapping("/{contexto}")
    public ResponseEntity<List<Compatibilidade>> getCompatibilidades(@AuthenticationPrincipal User loggedUser, @PathVariable String contexto) {
        List<Compatibilidade> compatibilidades = compatibilidadeService.buscarCompatibilidades(loggedUser, contexto);
        return ResponseEntity.ok(compatibilidades);
    }

    @PostMapping
    public ResponseEntity<Compatibilidade> adicionarCompatibilidade(@AuthenticationPrincipal User loggedUser, @RequestBody CompatibilidadeDTO request) {
        Compatibilidade compatibilidade = compatibilidadeService.adicionarCompatibilidade(loggedUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(compatibilidade);
    }
}
