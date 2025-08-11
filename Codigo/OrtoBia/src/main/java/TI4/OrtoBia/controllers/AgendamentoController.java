package TI4.OrtoBia.controllers;

import TI4.OrtoBia.dto.AgendamentoDTO;
import TI4.OrtoBia.models.Agendamento;
import TI4.OrtoBia.models.Servico;
import TI4.OrtoBia.models.Usuario;
import TI4.OrtoBia.repositories.AgendamentoRepository;
import TI4.OrtoBia.repositories.AvaliacaoRepository;
import TI4.OrtoBia.repositories.NotificacaoRepository;
import TI4.OrtoBia.repositories.ServicoRepository;
import TI4.OrtoBia.repositories.UsuarioRepository;
import TI4.OrtoBia.services.AgendamentoService;
import TI4.OrtoBia.services.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @GetMapping
    public ResponseEntity<List<Agendamento>> getAllAgendamentos() {
        return ResponseEntity.ok(agendamentoService.getAllAgendamentos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> getAgendamentoById(@PathVariable Long id) {
        return agendamentoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criarAgendamento(@RequestBody AgendamentoDTO dto, Authentication authentication) {
        try {
            System.out.println("=== CRIANDO AGENDAMENTO ===");
            System.out.println("DTO recebido: " + dto);
            System.out.println("Authentication: " + authentication);
            System.out.println("Authentication name: " + (authentication != null ? authentication.getName() : "null"));
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Usuario dentista = usuarioRepository.findById(dto.getDentistaId())
                .orElseThrow(() -> new RuntimeException("Dentista não encontrado"));

        Servico servico = servicoRepository.findById(dto.getServicoId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        LocalDateTime dataHora = LocalDateTime.parse(dto.getDataHora());
        String motivo = dto.getMotivo();

        boolean conflito = !agendamentoRepository.findByDataHoraBetween(
                dataHora.minusMinutes(30),
                dataHora.plusMinutes(30)).isEmpty();
        if (conflito) {
            System.out.println("Conflito de horário detectado");
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Já existe uma consulta agendada nesse horário.");
        }

        Agendamento agendamento = new Agendamento();
        agendamento.setDataHora(dataHora);
        agendamento.setUsuario(usuario);
        agendamento.setDentista(dentista);
        agendamento.setServico(servico);
        agendamento.setProcedimento(dto.getProcedimento());
        agendamento.setMotivo(motivo);
        agendamento.setDisponivel(false);
        agendamento.setAvaliada(false);

        agendamento = agendamentoRepository.save(agendamento);

        try {
            notificacaoService.criarNotificacaoAgendamento(agendamento);
            System.out.println("Notificação criada com sucesso");
        } catch (Exception e) {
            System.err.println("Erro ao criar notificação: " + e.getMessage());
            // Não propaga o erro para não afetar o agendamento
        }

        System.out.println("Agendamento criado com sucesso: " + agendamento.getId());
        ResponseEntity<?> response = ResponseEntity.ok(agendamento);
        System.out.println("Retornando resposta: " + response.getStatusCode());
        return response;
    } catch (RuntimeException e) {
        System.out.println("Erro RuntimeException: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
        System.out.println("Erro Exception: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno: " + e.getMessage());
    }
}


    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> updateAgendamento(@PathVariable Long id, @RequestBody Agendamento dados) {
        try {
            Agendamento atualizado = agendamentoService.updateAgendamento(id, dados);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/remarcar")
    public ResponseEntity<Agendamento> remarcarAgendamento(@PathVariable Long id, @RequestBody Map<String, Object> dados, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Verifica se o usuário é o dono do agendamento
        if (!agendamento.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            String dataHoraStr = (String) dados.get("dataHora");
            LocalDateTime novaDataHora = LocalDateTime.parse(dataHoraStr);
            
            agendamento.setDataHora(novaDataHora);
            agendamento.setConfirmada(false); // Reset confirmação após remarcação
            
            Agendamento atualizado = agendamentoRepository.save(agendamento);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/remarcar/{clienteId}")
    public ResponseEntity<Agendamento> remarcarAgendamentoPorClienteId(@PathVariable Long id, @PathVariable Long clienteId, @RequestBody Map<String, Object> dados) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Verifica se o cliente é o dono do agendamento
        if (!agendamento.getUsuario().getId().equals(clienteId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            String dataHoraStr = (String) dados.get("dataHora");
            LocalDateTime novaDataHora = LocalDateTime.parse(dataHoraStr);
            
            agendamento.setDataHora(novaDataHora);
            agendamento.setConfirmada(false); // Reset confirmação após remarcação
            
            Agendamento atualizado = agendamentoRepository.save(agendamento);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgendamento(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        Usuario dentista = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Dentista não encontrado"));

        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Verifica se o dentista é o responsável pelo agendamento
        if (!agendamento.getDentista().getId().equals(dentista.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Cria notificação de cancelamento
        notificacaoService.criarNotificacaoCancelamento(agendamento);

        agendamentoRepository.delete(agendamento);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarAgendamentoPorCliente(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Verifica se o usuário é o dono do agendamento
        if (!agendamento.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Cria notificação de cancelamento
        notificacaoService.criarNotificacaoCancelamento(agendamento);

        agendamentoRepository.delete(agendamento);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/cancelar/{clienteId}")
    public ResponseEntity<Void> cancelarAgendamentoPorClienteId(@PathVariable Long id, @PathVariable Long clienteId) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Verifica se o cliente é o dono do agendamento
        if (!agendamento.getUsuario().getId().equals(clienteId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Cria notificação de cancelamento
        notificacaoService.criarNotificacaoCancelamento(agendamento);

        agendamentoRepository.delete(agendamento);
        return ResponseEntity.noContent().build();
    }

    // Rota temporária para teste - sem autenticação
    @DeleteMapping("/test/cancelar/{id}/{clienteId}")
    public ResponseEntity<Void> testCancelarAgendamento(@PathVariable Long id, @PathVariable Long clienteId) {
        try {
            Agendamento agendamento = agendamentoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

            // Verifica se o cliente é o dono do agendamento
            if (!agendamento.getUsuario().getId().equals(clienteId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            agendamentoRepository.delete(agendamento);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/simples/{id}")
    @Transactional
    public ResponseEntity<Void> cancelarAgendamentoSimples(@PathVariable Long id) {
        try {
            System.out.println("Tentando cancelar agendamento ID: " + id);
            
            Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
            if (!agendamentoOpt.isPresent()) {
                System.out.println("Agendamento não encontrado com ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            Agendamento agendamento = agendamentoOpt.get();
            System.out.println("Agendamento encontrado: " + agendamento.getId());
            
            // Delete entidades relacionadas primeiro para evitar violação de FK
            try {
                // Buscar e deletar notificações relacionadas
                var notificacoes = notificacaoService.getNotificacoesPorAgendamento(id);
                for (var notificacao : notificacoes) {
                    notificacaoRepository.deleteById(notificacao.getId());
                }
                System.out.println("Notificações relacionadas deletadas: " + notificacoes.size());
            } catch (Exception e) {
                System.out.println("Erro ao deletar notificações: " + e.getMessage());
            }
            
            try {
                // Buscar e deletar avaliações relacionadas
                var avaliacoes = avaliacaoRepository.findByAgendamentoId(id);
                for (var avaliacao : avaliacoes) {
                    avaliacaoRepository.deleteById(avaliacao.getId());
                }
                System.out.println("Avaliações relacionadas deletadas: " + avaliacoes.size());
            } catch (Exception e) {
                System.out.println("Erro ao deletar avaliações: " + e.getMessage());
            }
            
            // Agora deletar o agendamento
            agendamentoRepository.delete(agendamento);
            System.out.println("Agendamento deletado com sucesso");
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Erro ao cancelar agendamento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/simples/{id}/remarcar")
    public ResponseEntity<Agendamento> remarcarAgendamentoSimples(@PathVariable Long id, @RequestBody Map<String, Object> dados) {
        try {
            System.out.println("Tentando remarcar agendamento ID: " + id);
            System.out.println("Dados recebidos: " + dados);
            
            Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
            if (!agendamentoOpt.isPresent()) {
                System.out.println("Agendamento não encontrado com ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            Agendamento agendamento = agendamentoOpt.get();
            System.out.println("Agendamento encontrado: " + agendamento.getId());

            String dataHoraStr = (String) dados.get("dataHora");
            if (dataHoraStr == null || dataHoraStr.isEmpty()) {
                System.out.println("Data/hora não fornecida ou vazia");
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("Nova data/hora: " + dataHoraStr);
            LocalDateTime novaDataHora = LocalDateTime.parse(dataHoraStr);
            
            agendamento.setDataHora(novaDataHora);
            agendamento.setConfirmada(false); // Reset confirmação após remarcação
            
            Agendamento atualizado = agendamentoRepository.save(agendamento);
            System.out.println("Agendamento remarcado com sucesso");
            
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            System.err.println("Erro ao remarcar agendamento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/usuario/{clienteId}")
    public ResponseEntity<List<Agendamento>> listarPorClienteId(@PathVariable Long clienteId) {
        List<Agendamento> agendamentos = agendamentoRepository.findByUsuarioId(clienteId);

        // Ordena a lista pelo campo "dataHora" em ordem crescente
        agendamentos.sort(Comparator.comparing(Agendamento::getDataHora));

        if (agendamentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(agendamentos);
    }

    // Removido método duplicado confirmarAgendamento(Long id)

    @GetMapping("/usuario")
    public ResponseEntity<List<Agendamento>> getAgendamentosUsuario(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Agendamento> agendamentos = agendamentoRepository.findByUsuarioId(usuario.getId());
        return ResponseEntity.ok(agendamentos);
    }

    /**
     * Retorna todos os agendamentos no horário de expediente (segunda a sábado),
     * entre 07:00 e 18:30, para que o front-end exiba corretamente como
     * "disponível" (verde) ou "reservado" (vermelho)
     */
    @GetMapping("/disponiveis")
    public ResponseEntity<List<Agendamento>> getHorariosDisponiveis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<Agendamento> agendamentos = agendamentoRepository.findByDataHoraBetween(inicio, fim);
        return ResponseEntity.ok(agendamentos);
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<Agendamento> confirmarAgendamento(@PathVariable Long id) {
        Optional<Agendamento> agendamentoExistente = agendamentoService.buscarPorId(id);
        if (agendamentoExistente.isPresent()) {
            Agendamento agendamento = agendamentoExistente.get();
            agendamento.setConfirmada(true);
            Agendamento atualizado = agendamentoService.createAgendamento(agendamento);
            return ResponseEntity.ok(atualizado);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<Agendamento> finalizarAgendamento(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        Usuario dentista = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Dentista não encontrado"));

        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Verifica se o dentista é o responsável pelo agendamento
        if (!agendamento.getDentista().getId().equals(dentista.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        agendamento.setAtendida(true);
        agendamento = agendamentoRepository.save(agendamento);

        // Cria notificação de finalização
        notificacaoService.criarNotificacaoFinalizacao(agendamento);

        return ResponseEntity.ok(agendamento);
    }

    @GetMapping("/dentista")
    public ResponseEntity<List<Agendamento>> getAgendamentosDentista(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Authentication authentication) {

        String email = authentication.getName();
        Usuario dentista = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Dentista não encontrado"));

        List<Agendamento> agendamentos = agendamentoRepository.findByDentistaIdAndDataHoraBetween(
                dentista.getId(), inicio, fim);
        return ResponseEntity.ok(agendamentos);
    }

    @PutMapping("/simples/{id}/cancelar")
    public ResponseEntity<Agendamento> cancelarAgendamentoSimplesAlt(@PathVariable Long id) {
        try {
            System.out.println("Tentando cancelar agendamento ID (alternativo): " + id);
            
            Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
            if (!agendamentoOpt.isPresent()) {
                System.out.println("Agendamento não encontrado com ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            Agendamento agendamento = agendamentoOpt.get();
            System.out.println("Agendamento encontrado: " + agendamento.getId());
            
            // Marca como cancelado ao invés de deletar
            agendamento.setConfirmada(false);
            agendamento.setAtendida(true); // Marca como atendida para remover da lista
            agendamento.setObservacoes("CANCELADO PELO CLIENTE");
            
            Agendamento atualizado = agendamentoRepository.save(agendamento);
            System.out.println("Agendamento marcado como cancelado");
            
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            System.err.println("Erro ao cancelar agendamento (alternativo): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/test/info/{id}")
    public ResponseEntity<Agendamento> testarAgendamento(@PathVariable Long id) {
        try {
            System.out.println("Testando acesso ao agendamento ID: " + id);
            
            Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
            if (!agendamentoOpt.isPresent()) {
                System.out.println("Agendamento não encontrado com ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            Agendamento agendamento = agendamentoOpt.get();
            System.out.println("Agendamento encontrado: " + agendamento.getId());
            System.out.println("Usuario: " + (agendamento.getUsuario() != null ? agendamento.getUsuario().getNome() : "null"));
            System.out.println("Dentista: " + (agendamento.getDentista() != null ? agendamento.getDentista().getNome() : "null"));
            System.out.println("Servico: " + (agendamento.getServico() != null ? agendamento.getServico().getId() : "null"));
            
            return ResponseEntity.ok(agendamento);
        } catch (Exception e) {
            System.err.println("Erro ao testar agendamento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/atender")
    public ResponseEntity<?> atenderAgendamento(@PathVariable Long id, @RequestBody Map<String, Object> dados, Authentication authentication) {
        try {
            System.out.println("=== ATENDENDO AGENDAMENTO ===");
            System.out.println("ID do agendamento: " + id);
            System.out.println("Dados recebidos: " + dados);
            System.out.println("Authentication: " + (authentication != null ? authentication.getName() : "null"));
            
            if (authentication == null) {
                System.out.println("Authentication é null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
            }
            
            String email = authentication.getName();
            Usuario dentista = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Dentista não encontrado"));

            System.out.println("Dentista encontrado: " + dentista.getNome());

            Agendamento agendamento = agendamentoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

            System.out.println("Agendamento encontrado: " + agendamento.getId());
            System.out.println("Dentista do agendamento: " + (agendamento.getDentista() != null ? agendamento.getDentista().getNome() : "null"));

            // Verifica se o dentista é o responsável pelo agendamento
            if (!agendamento.getDentista().getId().equals(dentista.getId())) {
                System.out.println("Dentista não é responsável pelo agendamento");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Extrai as observações do corpo da requisição
            String observacoes = (String) dados.get("observacoes");
            System.out.println("Observações: " + observacoes);
            
            // Atualiza o agendamento
            agendamento.setAtendida(true);
            agendamento.setObservacoes(observacoes);
            agendamento = agendamentoRepository.save(agendamento);
            System.out.println("Agendamento atualizado com sucesso");

            // Cria notificação de finalização
            try {
                notificacaoService.criarNotificacaoFinalizacao(agendamento);
                System.out.println("Notificação criada com sucesso");
            } catch (Exception e) {
                System.err.println("Erro ao criar notificação: " + e.getMessage());
                // Não propaga o erro para não afetar o atendimento
            }

            // Retorna uma resposta simples ao invés do objeto completo
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("id", agendamento.getId());
            resposta.put("atendida", agendamento.isAtendida());
            resposta.put("observacoes", agendamento.getObservacoes());
            resposta.put("mensagem", "Atendimento confirmado com sucesso");
            
            System.out.println("Retornando resposta de sucesso");
            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            System.err.println("Erro ao atender agendamento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno: " + e.getMessage());
        }
    }
}
