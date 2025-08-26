package com.AIT.Optimanage.Controllers.User;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Operações relacionadas a usuários")
public class UsuarioController extends V1BaseController {

//    @Autowired
//    private UsuarioService usuarioService;
//
//    @Autowired
//    private BCryptPasswordEncoder passwordEncoder;
//
//    // Criar um novo usuário
//    @PostMapping("/criar")
//    public ResponseEntity<Usuario> criarUsuario(@RequestBody Usuario usuario) {
//        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
//        usuario.setDataAssinatura(LocalDate.now());
//        usuario.setAtivo(true);
//        Usuario novoUsuario = usuarioService.salvarUsuario(usuario);
//        return ResponseEntity.ok(novoUsuario);
//    }
//
//    // Listar todos os usuários (geralmente seria restrito a admins)
//    @GetMapping("/listar")
//    public ResponseEntity<List<Usuario>> listarUsuarios() {
//        List<Usuario> usuarios = usuarioService.listarUsuarios();
//        return ResponseEntity.ok(usuarios);
//    }
//
//    // Buscar usuário por ID
//    @GetMapping("/{id}")
//    public ResponseEntity<Usuario> buscarUsuario(@PathVariable Integer id) {
//        Usuario usuario = usuarioService.buscarUsuario(id);
//        return ResponseEntity.ok(usuario);
//    }
//
//    // Atualizar plano ativo de um usuário
//    @PutMapping("/{id}/atualizar-plano")
//    public ResponseEntity<Usuario> atualizarPlanoAtivo(
//            @PathVariable Integer id,
//            @RequestParam Integer novoPlanoId) {
//        Usuario usuarioAtualizado = usuarioService.atualizarPlanoAtivo(id, novoPlanoId);
//        return ResponseEntity.ok(usuarioAtualizado);
//    }
//
//    // Desativar usuário
//    @DeleteMapping("/{id}/desativar")
//    public ResponseEntity<Void> desativarUsuario(@PathVariable Integer id) {
//        usuarioService.desativarUsuario(id);
//        return ResponseEntity.noContent().build();
//    }
}
