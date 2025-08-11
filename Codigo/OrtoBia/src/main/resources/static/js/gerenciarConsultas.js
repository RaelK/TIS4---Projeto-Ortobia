document.addEventListener("DOMContentLoaded", () => {
  carregarAgendamentos();
  carregarUsuarios();
});

// Carregar agendamento
async function carregarAgendamentos() {
  const container = document.getElementById("consultas-container");
  container.innerHTML = "";

  try {
    const response = await fetch("http://localhost:8080/agendamentos");
    const agendamentos = await response.json();

    agendamentos.forEach(ag => {
      const card = document.createElement("div");
      card.classList.add("services-list");
      card.setAttribute("data-id", ag.id);

      card.innerHTML = `
        <p><strong>Paciente:</strong> ${ag.usuario.nome_completo}</p>
        <p><strong>Horário:</strong> <span class="data-hora">${ag.dataHora.replace('T', ' ').slice(0, 16)}</span></p>
        <p><strong>Descrição:</strong> <span class="obs">${ag.observacoes || "Sem observações."}</span></p>
        <button class="btn-edit" onclick="alterarAgendamento(${ag.id})">Alterar</button>
        <button class="btn-delete" onclick="removerAgendamento(${ag.id})">Cancelar</button>
      `;

      container.appendChild(card);
    });
  } catch (error) {
    console.error("Erro ao carregar agendamentos:", error);
  }
}

// Remover agendameto
async function removerAgendamento(id) {
  if (confirm("Deseja cancelar este agendamento?")) {
    await fetch(`http://localhost:8080/agendamentos/${id}`, { method: "DELETE" });
    alert("Agendamento cancelado.");
    carregarAgendamentos();
  }
}

// Alterar agendamento
function alterarAgendamento(id) {
  const card = document.querySelector(`.services-list[data-id='${id}']`);
  const dataHoraEl = card.querySelector(".data-hora");
  const observacoesEl = card.querySelector(".obs");

  const currentHora = dataHoraEl.textContent.trim();
  const currentObs = observacoesEl.textContent.trim();

  dataHoraEl.innerHTML = `<input type="datetime-local" value="${currentHora.replace(' ', 'T')}" class="edit-hora">`;
  observacoesEl.innerHTML = `<textarea class="edit-obs">${currentObs}</textarea>`;

  const btnAlterar = card.querySelector(".btn-edit");
  btnAlterar.textContent = "Salvar";
  btnAlterar.onclick = () => salvarAlteracoes(id);
}

// Salvar alterações
async function salvarAlteracoes(id) {
  const card = document.querySelector(`.services-list[data-id='${id}']`);
  const novaHora = card.querySelector(".edit-hora").value;
  const novaObs = card.querySelector(".edit-obs").value;

  try {
    await fetch(`http://localhost:8080/agendamentos/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ dataHora: novaHora, observacoes: novaObs })
    });

    alert("Consulta atualizada com sucesso!");
    carregarAgendamentos();
  } catch (error) {
    alert("Erro ao atualizar consulta.");
    console.error(error);
  }
}

// Abrir popup de nova consulta
document.addEventListener("DOMContentLoaded", () => {
  const btn = document.getElementById("nova-consulta-btn");

  btn.addEventListener("click", async () => {
    const overlay = document.createElement("div");
    overlay.classList.add("modal-overlay");
    overlay.id = "modal-overlay";

    const modal = document.createElement("div");
    modal.classList.add("modal");

    modal.innerHTML = `
  <h2>Nova Consulta</h2>
  <form id="form-consulta">
    <label for="usuarioId">Paciente:</label>
    <select id="usuarioId" required></select><br><br>

    <label for="email">Email:</label>
    <input type="email" id="email" required><br><br>

    <label for="telefone">Telefone:</label>
    <input type="tel" id="telefone" required><br><br>

    <label for="dataHora">Data e Hora:</label>
    <input type="datetime-local" id="dataHora" required><br><br>

    <label for="descricao">Descrição da consulta:</label><br>
    <textarea id="descricao" rows="3"></textarea><br><br>

    <div style="display: flex; justify-content: flex-end; gap: 10px;">
      <button type="submit" class="btn-edit">Salvar</button>
      <button type="button" class="btn-delete" onclick="fecharModal()">Cancelar</button>
    </div>
  </form>
`;



    overlay.appendChild(modal);
    document.body.appendChild(overlay);
    overlay.style.display = "flex";

    const selectElement = modal.querySelector("#usuarioId");
    await carregarUsuariosParam(selectElement);

    modal.querySelector("#form-consulta").addEventListener("submit", async (e) => {
      e.preventDefault();
    
      const pacienteId = modal.querySelector("#usuarioId").value;
      const email = modal.querySelector("#email").value;
      const telefone = modal.querySelector("#telefone").value;
      const nome = modal.querySelector("#usuarioId").selectedOptions[0].textContent;
      const dataHoraCompleta = modal.querySelector("#dataHora").value;
    
      if (!pacienteId || !email || !telefone || !dataHoraCompleta) {
        alert("Preencha todos os campos obrigatórios.");
        return;
      }
    
     
      const [data, horario] = dataHoraCompleta.split("T");
    
      const dto = {
        data,
        horario,
        nome,
        email,
        telefone
      };
    
      console.log("Enviando AgendamentoDTO:", dto);
    
      try {
        const response = await fetch("http://localhost:8080/agendamentos", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(dto)
        });
    
        if (!response.ok) throw new Error("Erro no servidor");
    
        alert("Consulta cadastrada com sucesso!");
        fecharModal();
        carregarAgendamentos();
    
      } catch (error) {
        alert("Erro ao cadastrar consulta.");
        console.error("Erro:", error);
      }
    });
  });
});

// Fechar popup
function fecharModal() {
  const overlay = document.getElementById("modal-overlay");
  if (overlay) overlay.remove();
}

// Carregar usuarios do popup 
async function carregarUsuariosParam(selectElement) {
  if (!selectElement) return;

  try {
    const response = await fetch("http://localhost:8080/usuarios");
    if (!response.ok) throw new Error("Erro ao buscar usuários.");

    const clientes = await response.json();
    selectElement.innerHTML = '<option value="" disabled selected hidden>Selecione um paciente</option>';

    clientes.forEach(cliente => {
      const option = document.createElement("option");
      option.value = cliente.id;
      option.textContent = cliente.nome_completo;
      selectElement.appendChild(option);
    });
  } catch (error) {
    console.error("Erro:", error);
    alert("Erro ao carregar pacientes.");
  }
}

// Carregar usurios fora do popup 
async function carregarUsuarios() {
  try {
    const response = await fetch("http://localhost:8080/usuarios");
    if (!response.ok) throw new Error("Erro ao buscar usuários.");

    const clientes = await response.json();
    const select = document.getElementById("usuarioId");
    if (!select) return;

    clientes.forEach(cliente => {
      const option = document.createElement("option");
      option.value = cliente.id;
      option.textContent = cliente.nome_completo;
      select.appendChild(option);
    });
  } catch (error) {
    console.error("Erro:", error);
    alert("Erro ao carregar clientes.");
  }
}
