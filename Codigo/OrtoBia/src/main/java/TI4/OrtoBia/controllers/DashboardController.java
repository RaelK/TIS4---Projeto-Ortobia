package TI4.OrtoBia.controllers;

import TI4.OrtoBia.models.Agendamento;
import TI4.OrtoBia.repositories.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> getEstatisticasDashboard(Authentication authentication) {
        Map<String, Object> estatisticas = new HashMap<>();
        
        // Buscar todos os agendamentos
        List<Agendamento> agendamentos = agendamentoRepository.findAll();
        
        // Estatísticas gerais
        long totalAgendamentos = agendamentos.size();
        long agendamentosAtendidos = agendamentos.stream().filter(Agendamento::isAtendida).count();
        long agendamentosPendentes = agendamentos.stream().filter(a -> !a.isAtendida() && !a.isDisponivel()).count();
        
        estatisticas.put("totalAgendamentos", totalAgendamentos);
        estatisticas.put("agendamentosAtendidos", agendamentosAtendidos);
        estatisticas.put("agendamentosPendentes", agendamentosPendentes);
        
        // Estatísticas por tipo de procedimento
        Map<String, Long> procedimentoStats = agendamentos.stream()
            .filter(a -> a.getProcedimento() != null && !a.getProcedimento().isEmpty())
            .collect(Collectors.groupingBy(
                Agendamento::getProcedimento,
                Collectors.counting()
            ));
        
        estatisticas.put("procedimentosPorTipo", procedimentoStats);
        
        // Estatísticas por dia da semana
        Map<String, Long> diaSemanaStats = agendamentos.stream()
            .collect(Collectors.groupingBy(
                agendamento -> {
                    DayOfWeek dayOfWeek = agendamento.getDataHora().getDayOfWeek();
                    return dayOfWeek.getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
                },
                Collectors.counting()
            ));
        
        estatisticas.put("agendamentosPorDiaSemana", diaSemanaStats);
        
        // Estatísticas por mês (últimos 12 meses)
        LocalDateTime dozeMesesAtras = LocalDateTime.now().minusMonths(12);
        Map<String, Long> mesStats = agendamentos.stream()
            .filter(a -> a.getDataHora().isAfter(dozeMesesAtras))
            .collect(Collectors.groupingBy(
                agendamento -> {
                    int mes = agendamento.getDataHora().getMonthValue();
                    int ano = agendamento.getDataHora().getYear();
                    return String.format("%d-%02d", ano, mes);
                },
                Collectors.counting()
            ));
        
        estatisticas.put("agendamentosPorMes", mesStats);
        
        // Horários mais procurados
        Map<String, Long> horarioStats = agendamentos.stream()
            .collect(Collectors.groupingBy(
                agendamento -> {
                    int hora = agendamento.getDataHora().getHour();
                    return String.format("%02d:00", hora);
                },
                Collectors.counting()
            ));
        
        estatisticas.put("agendamentosPorHorario", horarioStats);
        
        return ResponseEntity.ok(estatisticas);
    }
    
    @GetMapping("/consultas-recentes")
    public ResponseEntity<List<Map<String, Object>>> getConsultasRecentes() {
        List<Agendamento> consultasRecentes = agendamentoRepository.findTop10ByOrderByDataHoraDesc();
        
        List<Map<String, Object>> resultado = consultasRecentes.stream()
            .map(agendamento -> {
                Map<String, Object> consulta = new HashMap<>();
                consulta.put("id", agendamento.getId());
                consulta.put("paciente", agendamento.getUsuario().getNome());
                consulta.put("dataHora", agendamento.getDataHora());
                consulta.put("procedimento", agendamento.getProcedimento());
                consulta.put("status", agendamento.isAtendida() ? "Atendida" : "Pendente");
                return consulta;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(resultado);
    }
}
