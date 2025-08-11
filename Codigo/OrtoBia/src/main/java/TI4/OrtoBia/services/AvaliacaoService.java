package TI4.OrtoBia.services;

import TI4.OrtoBia.models.Avaliacao;
import TI4.OrtoBia.models.Agendamento;
import TI4.OrtoBia.models.Usuario;
import TI4.OrtoBia.repositories.AvaliacaoRepository;
import TI4.OrtoBia.repositories.AgendamentoRepository;
import TI4.OrtoBia.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AvaliacaoService {
    @Autowired
    private AvaliacaoRepository avaliacaoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private AgendamentoRepository agendamentoRepository;

    public Avaliacao criarAvaliacao(Avaliacao avaliacao) {
        // Validar se o agendamento existe e está finalizado
        Agendamento agendamento = agendamentoRepository.findById(avaliacao.getAgendamento().getId())
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
            
        // Validar se o paciente existe
        Usuario paciente = usuarioRepository.findById(avaliacao.getPaciente().getId())
            .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
            
        // Validar se a nota está entre 1 e 5
        if (avaliacao.getNota() < 1 || avaliacao.getNota() > 5) {
            throw new RuntimeException("A nota deve estar entre 1 e 5");
        }

        return avaliacaoRepository.save(avaliacao);
    }

    public List<Avaliacao> getAvaliacoesPorPaciente(Long pacienteId) {
        return avaliacaoRepository.findByPacienteId(pacienteId);
    }

    public List<Avaliacao> getAvaliacoesPorAgendamento(Long agendamentoId) {
        return avaliacaoRepository.findByAgendamentoId(agendamentoId);
    }

    public Optional<Avaliacao> getAvaliacaoPorId(Long id) {
        return avaliacaoRepository.findById(id);
    }
} 