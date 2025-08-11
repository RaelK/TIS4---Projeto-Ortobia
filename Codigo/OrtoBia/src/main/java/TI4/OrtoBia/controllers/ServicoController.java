package TI4.OrtoBia.controllers;

import TI4.OrtoBia.models.Servico;
import TI4.OrtoBia.repositories.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServicoController {

    @Autowired
    private ServicoRepository servicoRepository;

    // Endpoint para listar todos os serviços
    @GetMapping
    public ResponseEntity<List<Servico>> listarTodos() {
        return ResponseEntity.ok(servicoRepository.findAll());
    }

    // ✅ Novo endpoint para criar um serviço
    @PostMapping
    public ResponseEntity<Servico> criarServico(@RequestBody Servico servico) {
        Servico novoServico = servicoRepository.save(servico);
        return new ResponseEntity<>(novoServico, HttpStatus.CREATED);
    }

    // (Opcional) Endpoint para deletar um serviço
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarServico(@PathVariable Long id) {
        if (servicoRepository.existsById(id)) {
            servicoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}