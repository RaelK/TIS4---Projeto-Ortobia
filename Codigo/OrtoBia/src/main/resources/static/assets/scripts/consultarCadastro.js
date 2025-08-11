// 1. Executa ao carregar a página para preencher a lista de clientes
document.addEventListener("DOMContentLoaded", () => {
  carregarListaDeClientes(); // Chamamos a função correta
});

// 2. Função para BUSCAR TODOS os clientes e preencher o <select>
async function carregarListaDeClientes() {
  try {
    // Acessa o endpoint que retorna a lista de todos os usuários
    const response = await fetch("http://localhost:8080/usuarios");
    if (!response.ok) {
      throw new Error("Erro ao buscar a lista de clientes.");
    }

    const clientes = await response.json();
    const select = document.getElementById("cliente");

    // Limpa opções antigas, exceto a primeira ("Selecione um cliente")
    select.innerHTML = '<option value="" disabled selected>Selecione um cliente</option>';

    // Adiciona cada cliente como uma nova <option>
    clientes.forEach((cliente) => {
      const option = document.createElement("option");
      option.value = cliente.id;
      option.textContent = cliente.nome; // Exibe o nome do cliente na lista
      select.appendChild(option);
    });
  } catch (error) {
    console.error("Erro em carregarListaDeClientes:", error);
    alert("Não foi possível carregar a lista de clientes.");
  }
}

// 3. Função chamada QUANDO UM CLIENTE É SELECIONADO na lista
async function carregarDadosCliente() {
  const select = document.getElementById("cliente");
  const clienteId = select.value;

  // Se o valor for vazio (ex: a opção "Selecione..."), não faz nada
  if (!clienteId) {
    document.getElementById("dados-cliente").innerHTML = ""; // Limpa a área de dados
    return;
  }

  try {
    // Busca os dados do cliente específico pelo ID
    const response = await fetch(`http://localhost:8080/usuarios/${clienteId}`);
    if (!response.ok) {
      throw new Error("Erro ao buscar dados do cliente.");
    }
    const cliente = await response.json();

    // Monta o formulário com os dados do cliente
    const container = document.getElementById("dados-cliente");
    container.innerHTML = `
      <h4 class="mb-4 mt-3 border-bottom pb-2">Dados do Cliente Selecionado</h4>
      <form id="form-cliente">
        <div class="row">
          <div class="col-md-6 mb-3">
            <label for="nome" class="form-label">Nome Completo</label>
            <input type="text" class="form-control" id="nome" value="${cliente.nome}" readonly>
          </div>
          <div class="col-md-6 mb-3">
            <label for="email" class="form-label">Email</label>
            <input type="email" class="form-control" id="email" value="${cliente.email}" readonly>
          </div>
        </div>
        <div class="row">
          <div class="col-md-6 mb-3">
            <label for="telefone" class="form-label">Telefone</label>
            <input type="text" class="form-control" id="telefone" value="${cliente.telefone}" readonly>
          </div>
          <div class="col-md-6 mb-3">
            <label for="cpf" class="form-label">CPF</label>
            <input type="text" class="form-control" id="cpf" value="${cliente.cpf}" readonly>
          </div>
        </div>
        <input type="hidden" id="id" value="${cliente.id}">
        <input type="hidden" id="username" value="${cliente.username}">
        <input type="hidden" id="password" value="${cliente.password}">
      </form>
      <div id="form-buttons" class="text-end mt-3">
        <button type="button" id="btnAlt" class="btn btn-warning" onclick="alterarDados()">
          <span class="iconify me-1" data-icon="mdi:pencil"></span>
          Alterar Dados
        </button>
      </div>
    `;
  } catch (error) {
    console.error("Erro em carregarDadosCliente:", error);
    alert("Erro ao carregar os dados do cliente.");
  }
}

// 4. Função para habilitar a edição (permanece a mesma)
function alterarDados() {
  document.getElementById("nome").removeAttribute("readonly");
  document.getElementById("email").removeAttribute("readonly");
  document.getElementById("telefone").removeAttribute("readonly");
  document.getElementById("cpf").removeAttribute("readonly");

  document.querySelectorAll('#form-cliente input').forEach(input => {
    if (!input.readOnly) {
      input.classList.add('editable');
    }
  });

  const btnAlterar = document.getElementById("btnAlt");
  btnAlterar.style.display = "none";

  const containerBotoes = document.getElementById("form-buttons");
  containerBotoes.innerHTML = `
    <button type="button" class="btn btn-secondary me-2" onclick="carregarDadosCliente()">
      <span class="iconify me-1" data-icon="mdi:cancel"></span>
      Cancelar
    </button>
    <button type="button" id="btnSalvar" class="btn btn-success">
      <span class="iconify me-1" data-icon="mdi:check"></span>
      Salvar Alterações
    </button>
  `;

  document.getElementById("btnSalvar").onclick = () => {
    const dadosParaSalvar = {
      id: document.getElementById("id").value,
      nome: document.getElementById("nome").value,
      email: document.getElementById("email").value,
      telefone: document.getElementById("telefone").value,
      cpf: document.getElementById("cpf").value,
      username: document.getElementById("username").value,
      password: document.getElementById("password").value,
    };

    fetch(`http://localhost:8080/usuarios/${dadosParaSalvar.id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dadosParaSalvar),
    })
      .then((response) => {
        if (response.ok) {
          alert("Dados alterados com sucesso!");
          location.reload();
        } else {
          alert("Falha ao alterar os dados. Verifique o console para mais detalhes.");
          console.error("Falha ao alterar os dados.");
        }
      })
      .catch((error) => {
        console.error("Erro na requisição:", error);
        alert("Ocorreu um erro na requisição. Verifique o console.");
      });
  };
}