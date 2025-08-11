package TI4.OrtoBia.services;

import TI4.OrtoBia.models.Notificacao;
import TI4.OrtoBia.models.Agendamento;
import TI4.OrtoBia.repositories.NotificacaoRepository;
import TI4.OrtoBia.repositories.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificacaoService {
    @Autowired
    private NotificacaoRepository notificacaoRepository;
    
    @Autowired
    private AgendamentoRepository agendamentoRepository;
    
    @Autowired
    private JavaMailSender emailSender;

    public Notificacao criarNotificacao(Notificacao notificacao) {
        // Validar se o agendamento existe
        agendamentoRepository.findById(notificacao.getAgendamento().getId())
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
            
        notificacao.setStatus("PENDENTE");
        return notificacaoRepository.save(notificacao);
    }

    @Scheduled(fixedRate = 300000) // Executa a cada 5 minutos
    public void enviarNotificacoesPendentes() {
        List<Notificacao> notificacoesPendentes = notificacaoRepository
            .findByStatusAndDataEnvioBefore("PENDENTE", LocalDateTime.now());

        for (Notificacao notificacao : notificacoesPendentes) {
            try {
                enviarEmail(notificacao);
                notificacao.setStatus("ENVIADA");
                notificacao.setDataEnvio(LocalDateTime.now());
            } catch (Exception e) {
                notificacao.setStatus("FALHA");
            }
            notificacaoRepository.save(notificacao);
        }
    }

    private void enviarEmail(Notificacao notificacao) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notificacao.getAgendamento().getUsuario().getEmail());
        message.setSubject("Lembrete de Consulta - Clínica Odontológica");
        message.setText(notificacao.getMensagem());
        emailSender.send(message);
    }

    public List<Notificacao> getNotificacoesPorAgendamento(Long agendamentoId) {
        return notificacaoRepository.findByAgendamentoId(agendamentoId);
    }

    public void criarNotificacaoAgendamento(Agendamento agendamento) {
        Notificacao notificacao = new Notificacao();
        notificacao.setAgendamento(agendamento);
        notificacao.setDataAgendamento(agendamento.getDataHora());
        
        String mensagemDetalhada = String.format(
            "<strong>Confirmação de Agendamento</strong><br><br>" +
            "Sua consulta foi agendada com sucesso!<br><br>" +
            "<strong>📅 Data:</strong> %s<br>" +
            "<strong>⏰ Horário:</strong> %s<br>" +
            "<strong>👨‍⚕️ Profissional:</strong> Dr(a). %s<br>" +
            "<strong>🦷 Procedimento:</strong> %s<br><br>" +
            "<em>Por favor, chegue com 15 minutos de antecedência.</em>",
            agendamento.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            agendamento.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm")),
            agendamento.getDentista().getNome(),
            agendamento.getProcedimento() != null ? agendamento.getProcedimento() : "Consulta"
        );
        
        notificacao.setMensagem(mensagemDetalhada);
        notificacao.setEnviada(false);
        notificacao.setTipo("AGENDAMENTO");
        notificacao.setStatus("PENDENTE"); 
        notificacao.setDataEnvio(LocalDateTime.now());
        notificacaoRepository.save(notificacao);

        // Envia email de confirmação
        enviarEmailNotificacao(
            agendamento.getUsuario().getEmail(),
            "✅ Confirmação de Agendamento - OrtoBia",
            mensagemDetalhada
        );
        
        System.out.println("Notificação salva no banco e email enviado");
    }

    public void criarNotificacaoCancelamento(Agendamento agendamento) {
        Notificacao notificacao = new Notificacao();
        notificacao.setAgendamento(agendamento);
        notificacao.setDataAgendamento(agendamento.getDataHora());
        
        String mensagemDetalhada = String.format(
            "<strong>❌ Cancelamento de Consulta</strong><br><br>" +
            "Informamos que sua consulta foi cancelada.<br><br>" +
            "<strong>📅 Data:</strong> %s<br>" +
            "<strong>⏰ Horário:</strong> %s<br>" +
            "<strong>👨‍⚕️ Profissional:</strong> Dr(a). %s<br><br>" +
            "Se desejar reagendar, entre em contato conosco através dos nossos canais de atendimento.<br><br>" +
            "<em>Pedimos desculpas por qualquer inconveniente causado.</em>",
            agendamento.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            agendamento.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm")),
            agendamento.getDentista().getNome()
        );
        
        notificacao.setMensagem(mensagemDetalhada);
        notificacao.setEnviada(false);
        notificacaoRepository.save(notificacao);

        // Envia email de cancelamento
        enviarEmailNotificacao(
            agendamento.getUsuario().getEmail(),
            "❌ Cancelamento de Consulta - OrtoBia",
            mensagemDetalhada
        );
    }

    public void criarNotificacaoFinalizacao(Agendamento agendamento) {
        Notificacao notificacao = new Notificacao();
        notificacao.setAgendamento(agendamento);
        notificacao.setDataAgendamento(agendamento.getDataHora());
        
        String mensagemDetalhada = String.format(
            "<strong>✅ Consulta Finalizada</strong><br><br>" +
            "Sua consulta foi finalizada com sucesso!<br><br>" +
            "<strong>📅 Data do Atendimento:</strong> %s<br>" +
            "<strong>👨‍⚕️ Profissional:</strong> Dr(a). %s<br>" +
            "<strong>🦷 Procedimento Realizado:</strong> %s<br><br>" +
            "Esperamos que tenha tido uma excelente experiência em nossa clínica.<br><br>" +
            "<strong>Sua opinião é importante!</strong><br>" +
            "Por favor, avalie nosso atendimento e compartilhe sua experiência conosco.<br><br>" +
            "<em>Lembre-se de seguir as orientações pós-procedimento fornecidas pelo dentista.</em>",
            agendamento.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            agendamento.getDentista().getNome(),
            agendamento.getProcedimento() != null ? agendamento.getProcedimento() : "Consulta"
        );
        
        notificacao.setMensagem(mensagemDetalhada);
        notificacao.setEnviada(false);
        notificacaoRepository.save(notificacao);

        // Envia email de finalização
        enviarEmailNotificacao(
            agendamento.getUsuario().getEmail(),
            "✅ Consulta Finalizada - OrtoBia",
            mensagemDetalhada
        );
    }

    private void enviarEmailNotificacao(String destinatario, String assunto, String mensagem) {
        try {
            System.out.println("=== ENVIANDO EMAIL ===");
            System.out.println("Destinatário: " + destinatario);
            System.out.println("Assunto: " + assunto);
            
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setFrom("ti4ortobia@gmail.com");
            
            // Criar HTML simples e funcional
            String htmlSeguro = criarHtmlSeguro(mensagem, assunto);
            String conteudoTexto = criarConteudoTexto(mensagem);
            
            // Configurar tanto HTML quanto texto (multipart)
            helper.setText(conteudoTexto, htmlSeguro);
            
            emailSender.send(mimeMessage);
            System.out.println("Email HTML enviado com sucesso para: " + destinatario);
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar email para " + destinatario + ": " + e.getMessage());
            System.err.println("Tipo da exceção: " + e.getClass().getSimpleName());
            e.printStackTrace();
            
            // Fallback: tentar apenas texto simples
            try {
                SimpleMailMessage simpleMessage = new SimpleMailMessage();
                simpleMessage.setTo(destinatario);
                simpleMessage.setSubject(assunto);
                simpleMessage.setFrom("ti4ortobia@gmail.com");
                simpleMessage.setText(criarConteudoTexto(mensagem));
                
                emailSender.send(simpleMessage);
                System.out.println("Email de texto simples enviado como fallback para: " + destinatario);
            } catch (Exception fallbackError) {
                System.err.println("Erro crítico ao enviar email: " + fallbackError.getMessage());
                fallbackError.printStackTrace();
            }
        }
    }
    
    @Scheduled(cron = "0 0 8 * * ?") // Executa todos os dias às 8h
    public void criarNotificacoesDiarias() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0);
        LocalDateTime fim = inicio.plusDays(1).withHour(23).withMinute(59);
        
        List<Agendamento> agendamentos = agendamentoRepository.findByDataHoraBetween(inicio, fim);
        
        for (Agendamento agendamento : agendamentos) {
            // Verifica se já existe notificação para este agendamento
            if (notificacaoRepository.findByAgendamentoId(agendamento.getId()).isEmpty()) {
                Notificacao notificacao = new Notificacao();
                notificacao.setAgendamento(agendamento);
                
                String mensagemDetalhada = String.format(
                    "<strong>🔔 Lembrete de Consulta</strong><br><br>" +
                    "Você tem uma consulta agendada para <strong>amanhã</strong>!<br><br>" +
                    "<strong>📅 Data:</strong> %s<br>" +
                    "<strong>⏰ Horário:</strong> %s<br>" +
                    "<strong>👨‍⚕️ Profissional:</strong> Dr(a). %s<br>" +
                    "<strong>🦷 Procedimento:</strong> %s<br><br>" +
                    "<strong>Importante:</strong><br>" +
                    "• Chegue com 15 minutos de antecedência<br>" +
                    "• Traga um documento de identidade<br>" +
                    "• Em caso de imprevisto, entre em contato conosco<br><br>" +
                    "<em>Estamos ansiosos para atendê-lo(a)!</em>",
                    agendamento.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    agendamento.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm")),
                    agendamento.getDentista().getNome(),
                    agendamento.getProcedimento() != null ? agendamento.getProcedimento() : "Consulta"
                );
                
                notificacao.setMensagem(mensagemDetalhada);
                notificacao.setEnviada(false);
                notificacaoRepository.save(notificacao);
                
                // Envia o email
                enviarEmailNotificacao(
                    agendamento.getUsuario().getEmail(),
                    "🔔 Lembrete: Consulta Amanhã - OrtoBia",
                    mensagemDetalhada
                );
            }
        }
    }
    
    private String criarConteudoTexto(String mensagem) {
        // Remover tags HTML da mensagem para criar versão texto mais limpa
        String textoLimpo = mensagem
            .replaceAll("<br>", "\n")
            .replaceAll("<br/>", "\n")
            .replaceAll("<strong>", "**")
            .replaceAll("</strong>", "**")
            .replaceAll("<em>", "*")
            .replaceAll("</em>", "*")
            .replaceAll("<[^>]*>", ""); // Remove qualquer tag HTML restante
            
        return "=========================================\n" +
               "ORTOBIA - CLÍNICA ODONTOLÓGICA\n" +
               "=========================================\n\n" +
               "Prezado(a) paciente,\n\n" +
               textoLimpo + "\n\n" +
               "Para qualquer dúvida ou necessidade de reagendamento, " +
               "entre em contato conosco através dos canais abaixo.\n\n" +
               "Agradecemos pela confiança em nossos serviços!\n\n" +
               "Atenciosamente,\n" +
               "**Equipe OrtoBia**\n\n" +
               "-----------------------------------------\n" +
               "CONTATOS:\n" +
               "📧 E-mail: ti4ortobia@gmail.com\n" +
               "📱 WhatsApp: (31) 98366-8435\n" +
               "📍 Endereço: Rua Cândido Nogueira, 35\n" +
               "   Grajaú, Belo Horizonte - MG\n" +
               "-----------------------------------------\n\n" +
               "Este é um e-mail automático, por favor não responda.";
    }

    private String criarHtmlSeguro(String mensagem, String assunto) {
        // HTML profissional sem logo, igual funcionava antes
        String textoLimpo = mensagem
            .replaceAll("<br>", "<br/>")
            .replaceAll("<strong>", "<b>")
            .replaceAll("</strong>", "</b>")
            .replaceAll("<em>", "<i>")
            .replaceAll("</em>", "</i>");
            
        return "<!DOCTYPE html>" +
               "<html lang='pt-BR'>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<title>" + assunto + "</title>" +
               "</head>" +
               "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f4f4f4;'>" +
               "<div style='background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);'>" +
               
               // Header sem logo
               "<div style='text-align: center; border-bottom: 2px solid #0066cc; padding-bottom: 20px; margin-bottom: 30px;'>" +
               "<h1 style='color: #0066cc; font-size: 24px; font-weight: bold; margin: 0;'>OrtoBia - Clínica Odontológica</h1>" +
               "</div>" +
               
               // Conteúdo principal
               "<div style='margin: 20px 0; font-size: 16px;'>" +
               "<p>Prezado(a) paciente,</p>" +
               "<div style='background-color: #f8f9fa; border-left: 4px solid #0066cc; padding: 15px; margin: 20px 0; border-radius: 4px;'>" +
               textoLimpo +
               "</div>" +
               "<p>Para qualquer dúvida ou necessidade de reagendamento, entre em contato conosco através dos canais abaixo.</p>" +
               "<p>Agradecemos pela confiança em nossos serviços!</p>" +
               "<p>Atenciosamente,<br/>" +
               "<span style='color: #0066cc; font-weight: bold;'>Equipe OrtoBia</span></p>" +
               "</div>" +
               
               // Footer
               "<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; text-align: center; color: #666; font-size: 14px;'>" +
               "<div>" +
               "<b>📧 E-mail:</b> ti4ortobia@gmail.com<br/>" +
               "<b>📱 WhatsApp:</b> (31) 98366-8435<br/>" +
               "<b>📍 Endereço:</b> Rua Cândido Nogueira, 35 - Grajaú, Belo Horizonte - MG" +
               "</div>" +
               "<p style='font-style: italic; margin-top: 15px; font-size: 12px;'>Este é um e-mail automático, por favor não responda.</p>" +
               "</div>" +
               
               "</div>" +
               "</body>" +
               "</html>";
    }
}