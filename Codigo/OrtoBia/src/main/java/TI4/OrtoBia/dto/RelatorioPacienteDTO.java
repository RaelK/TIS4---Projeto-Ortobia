package TI4.OrtoBia.dto;

public class RelatorioPacienteDTO {
    private String nome;
    private String email;
    private String telefone;
    private String data;
    private String horario;

    private String tipoServico;
    private String descricao;
    private double preco;
    private Integer duracao;

    private String ultimaConsulta;
    private String tratamentosAnteriores;
    private String cirurgias;
    private String higiene;

    private String observacoes;
    private String exames;
    private String diagnostico;

    private String procedimentosPlanejados;
    private String alteracoesPlano;

    private String medicamentos;
    private String restricoes;
    private String cuidados;

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getTipoServico() { return tipoServico; }
    public void setTipoServico(String tipoServico) { this.tipoServico = tipoServico; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public Integer getDuracao() { return duracao; }
    public void setDuracao(Integer duracao) { this.duracao = duracao; }

    public String getUltimaConsulta() { return ultimaConsulta; }
    public void setUltimaConsulta(String ultimaConsulta) { this.ultimaConsulta = ultimaConsulta; }

    public String getTratamentosAnteriores() { return tratamentosAnteriores; }
    public void setTratamentosAnteriores(String tratamentosAnteriores) { this.tratamentosAnteriores = tratamentosAnteriores; }

    public String getCirurgias() { return cirurgias; }
    public void setCirurgias(String cirurgias) { this.cirurgias = cirurgias; }

    public String getHigiene() { return higiene; }
    public void setHigiene(String higiene) { this.higiene = higiene; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getExames() { return exames; }
    public void setExames(String exames) { this.exames = exames; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getProcedimentosPlanejados() { return procedimentosPlanejados; }
    public void setProcedimentosPlanejados(String procedimentosPlanejados) { this.procedimentosPlanejados = procedimentosPlanejados; }

    public String getAlteracoesPlano() { return alteracoesPlano; }
    public void setAlteracoesPlano(String alteracoesPlano) { this.alteracoesPlano = alteracoesPlano; }

    public String getMedicamentos() { return medicamentos; }
    public void setMedicamentos(String medicamentos) { this.medicamentos = medicamentos; }

    public String getRestricoes() { return restricoes; }
    public void setRestricoes(String restricoes) { this.restricoes = restricoes; }

    public String getCuidados() { return cuidados; }
    public void setCuidados(String cuidados) { this.cuidados = cuidados; }
}