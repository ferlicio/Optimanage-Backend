package com.AIT.Optimanage.Controllers.Venda;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidade;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidadeDTO;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.Venda.ContextoCompatibilidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contextos")
@RequiredArgsConstructor
public class ContextoCompatibilidadeController extends V1BaseController {

    private final ContextoCompatibilidadeService contextoService;

    @GetMapping
    public ResponseEntity<ContextoCompatibilidade> listarContextos(@AuthenticationPrincipal User loggedUser) {
        return ResponseEntity.ok(contextoService.listarContextos(loggedUser));
    }

    @GetMapping("/{idContexto}")
    public ResponseEntity<ContextoCompatibilidade> listarUmContexto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idContexto) {
        return ResponseEntity.ok(contextoService.listarUmContexto(loggedUser, idContexto));
    }

    @PostMapping
    public ResponseEntity<ContextoCompatibilidade> criarContexto(@AuthenticationPrincipal User loggedUser, @RequestBody ContextoCompatibilidadeDTO request) {
        ContextoCompatibilidade contexto = contextoService.criarContexto(loggedUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(contexto);
    }

    @PutMapping("/{idContexto}")
    public ResponseEntity<ContextoCompatibilidade> editarContexto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idContexto, @RequestBody ContextoCompatibilidadeDTO request) {
        ContextoCompatibilidade contexto = contextoService.editarContexto(loggedUser, idContexto, request);
        return ResponseEntity.ok(contexto);
    }

    @DeleteMapping("/{idContexto}")
    public ResponseEntity<Void> excluirContexto(@AuthenticationPrincipal User loggedUser, @PathVariable Integer idContexto) {
        contextoService.excluirContexto(loggedUser, idContexto);
        return ResponseEntity.noContent().build();
    }
}
