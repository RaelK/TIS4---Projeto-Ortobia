package TI4.OrtoBia.repositories;

import TI4.OrtoBia.models.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    @Query("SELECT a FROM Agendamento a WHERE a.usuario.id = :usuarioId")
    List<Agendamento> findByUsuarioId(@Param("usuarioId") Long usuarioId);
    
    List<Agendamento> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Agendamento> findByDentistaIdAndDataHoraBetween(Long dentistaId, LocalDateTime inicio, LocalDateTime fim);
    List<Agendamento> findByDisponivelTrue();
    
    @Query("SELECT a FROM Agendamento a WHERE a.disponivel = true AND FUNCTION('HOUR', a.dataHora) BETWEEN 7 AND 19")
    List<Agendamento> findDisponiveisNoExpediente();

    @Query("SELECT a FROM Agendamento a WHERE a.usuario.nome LIKE %:identificador% OR a.usuario.email LIKE %:identificador% OR a.usuario.telefone LIKE %:identificador%")
    List<Agendamento> buscarPorIdentificador(@Param("identificador") String identificador);
    
    // Método para buscar as 10 consultas mais recentes ordenadas por data/hora decrescente
    List<Agendamento> findTop10ByOrderByDataHoraDesc();
}