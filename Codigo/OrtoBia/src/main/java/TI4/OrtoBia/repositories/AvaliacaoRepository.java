package TI4.OrtoBia.repositories;

import TI4.OrtoBia.models.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByAgendamentoDentistaId(Long dentistaId);
    List<Avaliacao> findByAgendamentoId(Long agendamentoId);
    List<Avaliacao> findByPacienteId(Long pacienteId);
}
