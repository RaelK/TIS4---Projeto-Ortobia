package TI4.OrtoBia.controllers;

import TI4.OrtoBia.models.Avaliacao;
import TI4.OrtoBia.models.Agendamento;
import TI4.OrtoBia.services.AvaliacaoService;
import TI4.OrtoBia.services.AgendamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {
    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private AgendamentoService agendamentoService;

    @PostMapping
    public ResponseEntity<?> criarAvaliacao(@RequestBody Avaliacao avaliacao) {
        try {
            Avaliacao novaAvaliacao = avaliacaoService.criarAvaliacao(avaliacao);
            
            // Marcar o agendamento como avaliado
            Agendamento agendamento = agendamentoService.buscarPorId(avaliacao.getAgendamento().getId())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
            agendamento.setAvaliada(true);
            agendamentoService.createAgendamento(agendamento);
            
            return ResponseEntity.ok(novaAvaliacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<Avaliacao>> getAvaliacoesPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(avaliacaoService.getAvaliacoesPorPaciente(pacienteId));
    }

    @GetMapping("/agendamento/{agendamentoId}")
    public ResponseEntity<List<Avaliacao>> getAvaliacoesPorAgendamento(@PathVariable Long agendamentoId) {
        return ResponseEntity.ok(avaliacaoService.getAvaliacoesPorAgendamento(agendamentoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAvaliacaoPorId(@PathVariable Long id) {
        return avaliacaoService.getAvaliacaoPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
