package TI4.OrtoBia.controllers;

import TI4.OrtoBia.models.Agendamento;
import TI4.OrtoBia.models.Usuario;
import TI4.OrtoBia.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
        return usuario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/dentistas")
    public List<Usuario> getDentistas() {
        return usuarioService.getUsuariosByRole("DENTISTA");
    }

    @PostMapping
    public Usuario createUsuario(@RequestBody Usuario usuario) {
        return usuarioService.createUsuario(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @RequestBody Usuario usuarioDetails) {
        try {
            Usuario updatedUsuario = usuarioService.updateUsuario(id, usuarioDetails);
            return ResponseEntity.ok(updatedUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/agendamentos")
    public ResponseEntity<List<Agendamento>> getAgendamentosDoUsuario(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Agendamento> agendamentos = usuarioService.getAgendamentosDoUsuario(id);
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/buscar-por-nome")
    public ResponseEntity<Usuario> buscarUsuarioPorNome(@RequestParam String nomeCompleto) {
        Optional<Usuario> usuario = usuarioService.buscarPorNomeCompleto(nomeCompleto);
        return usuario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
