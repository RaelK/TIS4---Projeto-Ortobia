package TI4.OrtoBia.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "agendamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false, unique = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHora;

    @Column(name = "procedimento")
    private String procedimento;

    @Column(length = 500)
    private String observacoes;

    @Column(name = "disponivel")
    private boolean disponivel;

    @Column(name = "confirmada")
    private Boolean confirmada = false;

    @Column(name = "atendida")
    private Boolean atendida = false;

    @Column(name = "avaliada")
    private boolean avaliada;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({ "agendamentos" })
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "servico_id", nullable = false)
    @JsonIgnoreProperties({ "agendamentos" })
    private Servico servico;

    @ManyToOne
    @JoinColumn(name = "dentista_id", nullable = false)
    private Usuario dentista;

    @Column(length = 500)
    private String motivo;

    public boolean isReservado() {
        return !this.disponivel;
    }

    public String getNome() {
        return usuario != null ? usuario.getNome() : null;
    }

    public String getEmail() {
        return usuario != null ? usuario.getEmail() : null;
    }

    public String getTelefone() {
        return usuario != null ? usuario.getTelefone() : null;
    }

    public String getTipoServico() {
        return procedimento;
    }

    public Servico getServico() {
        return servico;
    }

    public Usuario getDentista() {
        return dentista;
    }

    public String getMotivo() {
        return motivo;
    }

    public boolean isAtendida() {
        return atendida;
    }

    public void setAtendida(boolean atendida) {
        this.atendida = atendida;
    }
}