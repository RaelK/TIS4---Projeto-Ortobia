package TI4.OrtoBia.controllers;

import TI4.OrtoBia.models.Usuario;
import TI4.OrtoBia.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        try {
            // Criptografar a senha
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            // Criar o usuário
            Usuario novoUsuario = usuarioService.createUsuario(usuario);
            return ResponseEntity.ok(novoUsuario);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String senha = loginRequest.get("senha");

            // Buscar usuário por email
            Usuario usuario = usuarioService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Verificar senha
            if (!passwordEncoder.matches(senha, usuario.getPassword())) {
                throw new RuntimeException("Senha incorreta");
            }

            // Gerar token JWT (simulado por enquanto)
            String token = "jwt-token-" + usuario.getId();

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("id", usuario.getId());
            response.put("nome", usuario.getNome());
            response.put("role", usuario.getRole());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 