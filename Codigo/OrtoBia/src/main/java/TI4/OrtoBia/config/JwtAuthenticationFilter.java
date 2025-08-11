package TI4.OrtoBia.config;

import TI4.OrtoBia.models.Usuario;
import TI4.OrtoBia.services.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UsuarioService usuarioService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        System.out.println("JWT Filter - Request URI: " + request.getRequestURI());
        System.out.println("JWT Filter - Auth Header: " + authHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("JWT Filter - Token: " + token);
            
            // Extrair o ID do usuário do token (formato: jwt-token-{id})
            if (token.startsWith("jwt-token-")) {
                try {
                    String userIdStr = token.substring(10);
                    Long userId = Long.parseLong(userIdStr);
                    System.out.println("JWT Filter - User ID: " + userId);
                    
                    Optional<Usuario> userOpt = usuarioService.getUsuarioById(userId);
                    if (userOpt.isPresent()) {
                        Usuario user = userOpt.get();
                        System.out.println("JWT Filter - User found: " + user.getEmail());
                        
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                user.getEmail(), null, new ArrayList<>());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        System.out.println("JWT Filter - Authentication set successfully");
                    } else {
                        // Usuário não encontrado - token inválido
                        System.out.println("JWT Filter - User not found for ID: " + userId);
                        // Não retorna erro aqui, deixa o Spring Security decidir
                    }
                } catch (NumberFormatException e) {
                    // Token inválido
                    System.out.println("JWT Filter - Invalid token format: " + e.getMessage());
                    // Não retorna erro aqui, deixa o Spring Security decidir
                }
            } else {
                // Token inválido (não no formato esperado)
                System.out.println("JWT Filter - Token not in expected format");
                // Não retorna erro aqui, deixa o Spring Security decidir
            }
        } else {
            System.out.println("JWT Filter - No Bearer token found, continuing...");
        }
        // Continua com o filtro para que o Spring Security processe normalmente
        
        filterChain.doFilter(request, response);
    }
}
