package TI4.OrtoBia.data;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import TI4.OrtoBia.models.Servico;
import TI4.OrtoBia.repositories.ServicoRepository;

@Component
public class ServicoDataLoader implements CommandLineRunner {

    private final ServicoRepository servicoRepository;

    public ServicoDataLoader(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (servicoRepository.count() == 0) {
            Servico s1 = new Servico();
            s1.setTipo("Limpeza");
            s1.setDescricao("Limpeza dental completa");
            s1.setPreco(150.00);
            s1.setDuracao(30);
            servicoRepository.save(s1);

            Servico s2 = new Servico();
            s2.setTipo("Restauração");
            s2.setDescricao("Restauração de cárie");
            s2.setPreco(200.00);
            s2.setDuracao(45);
            servicoRepository.save(s2);

            Servico s3 = new Servico();
            s3.setTipo("Ortodontia");
            s3.setDescricao("Tratamento ortodontia com aparelho");
            s3.setPreco(3000.00);
            s3.setDuracao(60);
            servicoRepository.save(s3);

            Servico s4 = new Servico();
            s4.setTipo("Clareamento");
            s4.setDescricao("Clareamento dental a laser");
            s4.setPreco(800.00);
            s4.setDuracao(90);
            servicoRepository.save(s4);

            Servico s5 = new Servico();
            s5.setTipo("Extração");
            s5.setDescricao("Extração de dente siso");
            s5.setPreco(250.00);
            s5.setDuracao(30);
            servicoRepository.save(s5);

            Servico s6 = new Servico();
            s6.setTipo("Implante");
            s6.setDescricao("Implante dentário");
            s6.setPreco(5000.00);
            s6.setDuracao(120);
            servicoRepository.save(s6);

            Servico s7 = new Servico();
            s7.setTipo("Periodontia");
            s7.setDescricao("Tratamento de gengivite e periodontite");
            s7.setPreco(400.00);
            s7.setDuracao(60);
            servicoRepository.save(s7);
        }
    }
    
}
