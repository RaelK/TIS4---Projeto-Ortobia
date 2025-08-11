package TI4.OrtoBia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgendamentoDTO {
    private String dataHora;
    private Long dentistaId;
    private Long servicoId;
    private String procedimento;
    private String motivo;
    private Long usuarioId;
    
    @Override
    public String toString() {
        return "AgendamentoDTO{" +
                "dataHora='" + dataHora + '\'' +
                ", dentistaId=" + dentistaId +
                ", servicoId=" + servicoId +
                ", procedimento='" + procedimento + '\'' +
                ", motivo='" + motivo + '\'' +
                ", usuarioId=" + usuarioId +
                '}';
    }
}
