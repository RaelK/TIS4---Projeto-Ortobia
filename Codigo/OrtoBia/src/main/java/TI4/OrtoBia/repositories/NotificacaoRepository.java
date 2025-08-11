package TI4.OrtoBia.repositories;

import TI4.OrtoBia.models.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByAgendamentoId(Long agendamentoId);
    List<Notificacao> findByStatusAndDataEnvioBefore(String status, LocalDateTime data);
} 