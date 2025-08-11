package TI4.OrtoBia.controllers;

import TI4.OrtoBia.models.Notificacao;
import TI4.OrtoBia.services.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {
    @Autowired
    private NotificacaoService notificacaoService;

    @PostMapping
    public ResponseEntity<?> criarNotificacao(@RequestBody Notificacao notificacao) {
        try {
            Notificacao novaNotificacao = notificacaoService.criarNotificacao(notificacao);
            return ResponseEntity.ok(novaNotificacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/agendamento/{agendamentoId}")
    public ResponseEntity<List<Notificacao>> getNotificacoesPorAgendamento(@PathVariable Long agendamentoId) {
        return ResponseEntity.ok(notificacaoService.getNotificacoesPorAgendamento(agendamentoId));
    }
} 