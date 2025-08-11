package TI4.OrtoBia.controllers;

import TI4.OrtoBia.services.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @GetMapping("/dentista")
    public ResponseEntity<Map<String, Object>> getRelatorioDentista(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        
        // Verificar se o usuário é um dentista
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DENTISTA"))) {
            return ResponseEntity.status(403).build();
        }

        Long dentistaId = Long.parseLong(authentication.getName());
        Map<String, Object> relatorio = relatorioService.gerarRelatorioDentista(dentistaId, inicio, fim);
        return ResponseEntity.ok(relatorio);
    }
}