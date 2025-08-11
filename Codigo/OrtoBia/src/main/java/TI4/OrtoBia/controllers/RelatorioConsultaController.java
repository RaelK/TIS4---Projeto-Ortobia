package TI4.OrtoBia.controllers;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;

import TI4.OrtoBia.models.Agendamento;
import TI4.OrtoBia.models.Relatorio;
import TI4.OrtoBia.models.Usuario;
import TI4.OrtoBia.services.AgendamentoService;
import TI4.OrtoBia.services.RelatorioConsultaService;
import TI4.OrtoBia.services.UsuarioService;

@RestController
@RequestMapping("/consulta")
public class RelatorioConsultaController {

    private final RelatorioConsultaService relatorioConsultaService;
    private final AgendamentoService agendamentoService;
    private final UsuarioService usuarioService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public RelatorioConsultaController(RelatorioConsultaService relatorioConsultaService,
            AgendamentoService agendamentoService,
            UsuarioService usuarioService) {
        this.relatorioConsultaService = relatorioConsultaService;
        this.agendamentoService = agendamentoService;
        this.usuarioService = usuarioService;
    }

    /**
     * Método privado para adicionar o logo da empresa no início do PDF
     */
    private void adicionarLogoPdf(Document document) {
        try {
            // Caminho para a imagem do logo - usando ClassLoader para acessar recursos
            String logoPath = "static/images/Principal (fundo escuro) 1.png";
            var logoStream = getClass().getClassLoader().getResourceAsStream(logoPath);
            
            if (logoStream != null) {
                // Cria a imagem a partir do InputStream
                Image logo = new Image(ImageDataFactory.create(logoStream.readAllBytes()));
                
                // Redimensiona a imagem para um tamanho apropriado
                logo.scaleToFit(150, 100);
                
                // Adiciona a imagem ao documento
                document.add(logo);
                
                // Adiciona um espaço após o logo
                document.add(new Paragraph(" "));
                
                logoStream.close();
            }
            
        } catch (Exception e) {
            // Se não conseguir carregar a imagem, continua sem ela
            System.err.println("Erro ao carregar logo: " + e.getMessage());
        }
    }

    @PostMapping("/relatorio/{usuarioId}")
    public ResponseEntity<Void> relatorio(@PathVariable Long usuarioId, @RequestBody String text) {
        relatorioConsultaService.registrarRelatorio(text, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/relatorio/{usuarioId}")
    public ResponseEntity<List<Relatorio>> relatorio(@PathVariable Long usuarioId) {
        var relatorios = relatorioConsultaService.BuscarRelatorioPorUsuarioId(usuarioId);
        if (relatorios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(relatorios);
    }

    // Método que recebe o nome completo do usuario e retorna um PDF com as
    // consultas relacionadas a este nome
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> gerarAgendamentoPdfPorNome(@RequestParam String nomeCompleto) {
        // Busca o usuário pelo nome completo
        var usuarioOpt = usuarioService.buscarPorNomeCompleto(nomeCompleto);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Retorna 404 se o usuário não for encontrado
        }

        Usuario usuario = usuarioOpt.get();
        List<Agendamento> agendamentos = agendamentoService.buscarPorClienteId(usuario.getId());

        if (agendamentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

            // Adiciona o logo no início do PDF
            adicionarLogoPdf(document);

            // Adiciona título e informações do usuário no PDF
            document.add(new Paragraph("Relatório do aplicativo OrtoBia"));
            document.add(new Paragraph("Relatório de Agendamentos"));
            document.add(new Paragraph("Cliente: " + usuario.getNome()));
            document.add(new Paragraph("Telefone: " + usuario.getTelefone()));
            document.add(new Paragraph("Email: " + usuario.getEmail()));
            document.add(new Paragraph("------"));

            // Adiciona o conteúdo de cada agendamento
            for (Agendamento agendamento : agendamentos) {
                document.add(new Paragraph("Agendamento ID: " + agendamento.getId()));
                document.add(new Paragraph("Nome: " + agendamento.getNome()));
                if (agendamento.getProcedimento() != null) {
                    document.add(new Paragraph("Procedimento: " + agendamento.getProcedimento()));
                } else {
                    document.add(new Paragraph("Procedimento: Não especificado"));
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                document.add(new Paragraph("Data: " + agendamento.getDataHora().format(formatter)));
                document.add(new Paragraph("------"));
            }

            document.add(new Paragraph("Total de Consultas: " + agendamentos.size()));
            document.close();

            // Configura o ResponseEntity para retornar o PDF como um arquivo de download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "agendamento_cliente_" + usuario.getId() + ".pdf");

            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Método que recebe o ID do usuario e retorna um PDF com as consultas
    // relacionadas a este ID
    @GetMapping("/{usuarioId}/pdf")
    public ResponseEntity<byte[]> gerarAgendamentoPdf(@PathVariable Long usuarioId) {
        List<Agendamento> agendamentos = agendamentoService.buscarPorClienteId(usuarioId);

        if (agendamentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

            // Adiciona o logo no início do PDF
            adicionarLogoPdf(document);

            document.add(new Paragraph("Relatório do aplicativo OrtoBia"));

            // Adiciona título ao PDF
            document.add(new Paragraph("Agendamentos do Cliente ID: " + usuarioId));

            // Adiciona o conteúdo de cada agendamento
            for (Agendamento agendamento : agendamentos) {
                document.add(new Paragraph("Agendamento ID: " + agendamento.getId()));
                document.add(new Paragraph("Nome: " + agendamento.getNome()));
                if (agendamento.getProcedimento() != null) {
                    document.add(new Paragraph("Procedimento: " + agendamento.getProcedimento()));
                } else {
                    document.add(new Paragraph("Procedimento: Não especificado"));
                }
                document.add(new Paragraph("Data: " + agendamento.getDataHora().format(formatter)));
                document.add(new Paragraph("------"));
            }

            document.add(new Paragraph("Total de Consultas: " + agendamentos.size()));

            document.close();

            // Configura o ResponseEntity para retornar o PDF como um arquivo de download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "agendamento_cliente_" + usuarioId + ".pdf");

            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/periodo/pdf")
    public ResponseEntity<byte[]> gerarAgendamentoPdfPorPeriodo(
            @RequestParam String dataInicio,
            @RequestParam String dataFim) {

        // Converte as strings para LocalDate
        LocalDate inicio = LocalDate.parse(dataInicio);
        LocalDate fim = LocalDate.parse(dataFim);

        // Busca os agendamentos no período
        List<Agendamento> agendamentos = agendamentoService.buscarPorPeriodo(inicio, fim);

        if (agendamentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

            // Adiciona o logo no início do PDF
            adicionarLogoPdf(document);

            // Adiciona título e informações no PDF
            document.add(new Paragraph("Relatório do aplicativo OrtoBia"));
            document.add(new Paragraph("Relatório de Agendamentos no Período"));
            document.add(new Paragraph("Período: " + dataInicio + " até " + dataFim));
            document.add(new Paragraph("------"));

            // Adiciona o conteúdo de cada agendamento
            for (Agendamento agendamento : agendamentos) {
                document.add(new Paragraph("Agendamento ID: " + agendamento.getId()));
                document.add(new Paragraph("Nome: " + agendamento.getNome()));
                document.add(new Paragraph("Data: " + agendamento.getDataHora().format(formatter)));
                if (agendamento.getProcedimento() != null) {
                    document.add(new Paragraph("Procedimento: " + agendamento.getProcedimento()));
                } else {
                    document.add(new Paragraph("Procedimento: Não especificado"));
                }
                document.add(new Paragraph("------"));
            }

            document.add(new Paragraph("Total de Consultas: " + agendamentos.size()));
            document.close();

            // Configura o ResponseEntity para retornar o PDF como um arquivo de download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(
                    "filename",
                    "relatorio_periodo_" + dataInicio + "_a_" + dataFim + ".pdf");

            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint para gerar um PDF com todos os agendamentos
    @GetMapping("/todos/pdf")
    public ResponseEntity<byte[]> gerarTodosAgendamentosPdf() {
        // Busca todos os agendamentos no banco de dados
        List<Agendamento> agendamentos = agendamentoService.listarTodos();

        if (agendamentos.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retorna 204 se não houver agendamentos
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

            // Adiciona o logo no início do PDF
            adicionarLogoPdf(document);

            // Adiciona título ao PDF
            document.add(new Paragraph("Relatório do aplicativo OrtoBia"));
            document.add(new Paragraph("Relatório de Todos os Agendamentos"));
            document.add(new Paragraph("------"));

            // Adiciona o conteúdo de cada agendamento
            for (Agendamento agendamento : agendamentos) {
                document.add(new Paragraph("Agendamento ID: " + agendamento.getId()));
                document.add(new Paragraph("Nome: " + agendamento.getNome()));
                if (agendamento.getProcedimento() != null) {
                    document.add(new Paragraph("Procedimento: " + agendamento.getProcedimento()));
                } else {
                    document.add(new Paragraph("Procedimento: Não especificado"));
                }
                document.add(new Paragraph("Data: " + agendamento.getDataHora().format(formatter)));
                document.add(new Paragraph("------"));
            }

            document.add(new Paragraph("Total de Agendamentos: " + agendamentos.size()));
            document.close();

            // Configura o ResponseEntity para retornar o PDF como um arquivo de download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "todos_agendamentos.pdf");

            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
