package TI4.OrtoBia.services;

import TI4.OrtoBia.models.Agendamento;
import TI4.OrtoBia.models.Usuario;
import TI4.OrtoBia.repositories.AgendamentoRepository;
import TI4.OrtoBia.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private AgendamentoRepository agendamentoRepository;

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> buscarPorId(Long id){
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorNomeCompleto(String nomeCompleto) {
        return usuarioRepository.findByNomeCompleto(nomeCompleto);
    }

    public List<Usuario> getUsuariosByRole(String role) {
        return usuarioRepository.findByRole(role);
    }

    public Usuario createUsuario(Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new RuntimeException("Nome de usuário já está em uso.");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado.");
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario updateUsuario(Long id, Usuario usuarioDetails) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNome(usuarioDetails.getNome());
            usuario.setEmail(usuarioDetails.getEmail());
            usuario.setUsername(usuarioDetails.getUsername());
            usuario.setPassword(usuarioDetails.getPassword());
            return usuarioRepository.save(usuario);
        }).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public List<Agendamento> getAgendamentosDoUsuario(Long usuarioId) {
        return agendamentoRepository.findByUsuarioId(usuarioId);
    }

    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}
