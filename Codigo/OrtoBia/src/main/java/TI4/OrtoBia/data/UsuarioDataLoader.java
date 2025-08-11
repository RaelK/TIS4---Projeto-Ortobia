package TI4.OrtoBia.data;

import TI4.OrtoBia.models.Usuario;
import TI4.OrtoBia.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioDataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioDataLoader(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            Usuario c1 = new Usuario();
            c1.setNome("José Silva");
            c1.setCpf("123.456.789-01");
            c1.setPassword(passwordEncoder.encode("1234"));
            c1.setUsername("jose");
            c1.setEmail("jose@gmail.com");
            c1.setTelefone("(31)99999-0001");
            usuarioRepository.save(c1);

            Usuario c2 = new Usuario();
            c2.setNome("ADMIN");
            c2.setCpf("999.999.999-00");
            c2.setPassword(passwordEncoder.encode("admin"));
            c2.setUsername("admin");
            c2.setEmail("admin@gmail.com");
            c2.setTelefone("(31)99999-0002");
            c2.setRole("SECRETARIA");
            usuarioRepository.save(c2);

            Usuario c3 = new Usuario();
            c3.setNome("Beatriz Mamede");
            c3.setCpf("999.999.998-00");
            c3.setPassword(passwordEncoder.encode("dentista"));
            c3.setUsername("dentista");
            c3.setEmail("dentista@gmail.com");
            c3.setTelefone("(31)99999-0003");
            c3.setRole("DENTISTA");
            usuarioRepository.save(c3);
        }
    }
}
