package TI4.OrtoBia.services;

import TI4.OrtoBia.models.Agendamento;
import TI4.OrtoBia.repositories.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    public List<Agendamento> getAllAgendamentos() {
        return agendamentoRepository.findAll();
    }

    public Optional<Agendamento> getAgendamentoById(Long id) {
        return agendamentoRepository.findById(id);
    }

    public Agendamento createAgendamento(Agendamento agendamento) {
        return agendamentoRepository.save(agendamento);
    }

    public Agendamento updateAgendamento(Long id, Agendamento dados) {
        return agendamentoRepository.findById(id).map(a -> {
            a.setDataHora(dados.getDataHora());
            a.setObservacoes(dados.getObservacoes());
            return agendamentoRepository.save(a);
        }).orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
    }

    public void deleteAgendamento(Long id) {
        agendamentoRepository.deleteById(id);
    }

    public List<Agendamento> getAgendamentosByUsuarioId(Long usuarioId) {
        return agendamentoRepository.findByUsuarioId(usuarioId);
    }

    public List<Agendamento> getAgendamentosDisponiveis() {
        return agendamentoRepository.findByDisponivelTrue();
    }

    public List<Agendamento> getAgendamentosDisponiveisNoExpediente() {
        return agendamentoRepository.findDisponiveisNoExpediente();
    }

    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    public Optional<Agendamento> buscarPorId(Long id) {
        return agendamentoRepository.findById(id);
    }

    public List<Agendamento> buscarPorClienteId(Long usuarioId) {
        return agendamentoRepository.findByUsuarioId(usuarioId);
    }

    public List<Agendamento> buscarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return agendamentoRepository.findByDataHoraBetween(
                inicio.atStartOfDay(),
                fim.atTime(23, 59, 59));
    }

    public Agendamento atualizarAgendamento(Long id, Agendamento agendamentoParcial) {
        Agendamento existente = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (agendamentoParcial.getAtendida() != null) {
            existente.setAtendida(agendamentoParcial.getAtendida());
        }
        if (agendamentoParcial.getProcedimento() != null) {
            existente.setProcedimento(agendamentoParcial.getProcedimento());
        }

        return agendamentoRepository.save(existente);
    }

    public List<Agendamento> getAgendamentosNoExpediente() {
        return agendamentoRepository.findAll().stream()
                .filter(ag -> {
                    int hora = ag.getDataHora().getHour();
                    return hora >= 7 && hora < 19;
                })
                .collect(Collectors.toList());
    }
    
}
