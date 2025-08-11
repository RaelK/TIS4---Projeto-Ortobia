package TI4.OrtoBia.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @Column(nullable = false)
    private String tipo; // EMAIL, SMS, PUSH

    @Column(nullable = false)
    private String status = "PENDENTE"; // PENDENTE, ENVIADA, FALHA

    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    @Column(nullable = false)
    private LocalDateTime dataAgendamento;

    @Column(length = 500)
    private String mensagem;

    @Column(nullable = false)
    private boolean enviada = false;
} 