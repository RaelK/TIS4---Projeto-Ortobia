package TI4.OrtoBia.services;

import java.util.List;

import org.springframework.stereotype.Service;

import TI4.OrtoBia.models.Relatorio;
import TI4.OrtoBia.models.Usuario;
import TI4.OrtoBia.repositories.RelatorioRepository;

@Service
public class RelatorioConsultaService {

    private final UsuarioService usuarioService;
    private final RelatorioRepository relatorioRepository;

    public RelatorioConsultaService(UsuarioService usuarioService, RelatorioRepository relatorioRepository) {
        this.usuarioService = usuarioService;
        this.relatorioRepository = relatorioRepository;
    }

    public void registrarRelatorio(String text, Long clienteId) {
        Usuario usuario = usuarioService.buscarPorId(clienteId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        var relatorio = new Relatorio();
        relatorio.setUsuario(usuario);
        relatorio.setRelatorio(text);

        relatorioRepository.save(relatorio);
    }

    public List<Relatorio> BuscarRelatorioPorUsuarioId(Long usuarioId) {
        return relatorioRepository.findByUsuarioId(usuarioId);
    }
}
