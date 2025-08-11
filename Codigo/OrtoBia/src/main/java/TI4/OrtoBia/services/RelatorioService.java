package TI4.OrtoBia.services;

import TI4.OrtoBia.dto.RelatorioPacienteDTO;
import TI4.OrtoBia.models.Agendamento;
import TI4.OrtoBia.models.Avaliacao;
import TI4.OrtoBia.models.Servico;
import TI4.OrtoBia.repositories.AgendamentoRepository;
import TI4.OrtoBia.repositories.AvaliacaoRepository;
import TI4.OrtoBia.repositories.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    public RelatorioPacienteDTO gerarRelatorio(String identificador) {
        List<Agendamento> agendamentos = agendamentoRepository.buscarPorIdentificador(identificador);

        if (agendamentos.isEmpty()) {
            throw new RuntimeException("Paciente não encontrado.");
        }

        Agendamento agendamento = agendamentos.get(0); // Mais recente
        Servico servico = servicoRepository.findById(agendamento.getServico().getId()).orElse(null);

        RelatorioPacienteDTO dto = new RelatorioPacienteDTO();
        dto.setNome(agendamento.getNome());
        dto.setEmail(agendamento.getEmail());
        dto.setTelefone(agendamento.getTelefone());
        dto.setData(agendamento.getDataHora().toLocalDate().toString());
        dto.setHorario(agendamento.getDataHora().toLocalTime().toString());

        if (servico != null) {
            dto.setTipoServico(servico.getTipo());
            dto.setDescricao(servico.getDescricao());
            dto.setPreco(servico.getPreco());
            dto.setDuracao(servico.getDuracao());
        }

        // Dados simulados (poderão ser substituídos depois)
        dto.setUltimaConsulta("10/01/2025");
        dto.setTratamentosAnteriores("Restauração, Aplicação de flúor");
        dto.setCirurgias("Nenhuma");
        dto.setHigiene("Escova 2x ao dia");
        dto.setObservacoes("Gengiva levemente inflamada");
        dto.setExames("Radiografia panorâmica");
        dto.setDiagnostico("Gengivite inicial");
        dto.setProcedimentosPlanejados("Limpeza e aplicação de antibiótico local");
        dto.setAlteracoesPlano("Nenhuma");
        dto.setMedicamentos("Clorexidina 0,12%");
        dto.setRestricoes("Evitar alimentos duros");
        dto.setCuidados("Escovação suave");

        return dto;
    }

    public Map<String, Object> gerarRelatorioDentista(Long dentistaId, LocalDateTime inicio, LocalDateTime fim) {
        Map<String, Object> relatorio = new HashMap<>();

        // Consultas realizadas
        List<Agendamento> consultas = agendamentoRepository.findByDentistaIdAndDataHoraBetween(dentistaId, inicio, fim);
        relatorio.put("totalConsultas", consultas.size());
        relatorio.put("consultasRealizadas", consultas.stream().filter(Agendamento::isAtendida).count());
        relatorio.put("consultasCanceladas", consultas.stream().filter(a -> !a.isAtendida() && !a.isDisponivel()).count());

        // Avaliações
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByAgendamentoDentistaId(dentistaId);
        double mediaNotas = avaliacoes.stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);
        relatorio.put("mediaAvaliacoes", mediaNotas);
        relatorio.put("totalAvaliacoes", avaliacoes.size());

        // Pacientes atendidos
        long pacientesUnicos = consultas.stream()
                .map(a -> a.getUsuario().getId())
                .distinct()
                .count();
        relatorio.put("pacientesUnicos", pacientesUnicos);

        return relatorio;
    }
}
