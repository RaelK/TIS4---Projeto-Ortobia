// Função helper para fazer requisições autenticadas
async function fetchWithAuth(url) {
  const token = localStorage.getItem('token');
  const headers = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  return fetch(url, { headers: headers });
}

document.getElementById("form-busca-relatorio").addEventListener("submit", async function (e) {
  e.preventDefault();

  const usuarioId = document.getElementById("input-id").value.trim();
  const nomeCompleto = document.getElementById("input-nome").value.trim();
  const resultado = document.getElementById("resultado-relatorio");
  
  // Limpa o resultado anterior
  resultado.innerHTML = "<p>Buscando...</p>";

  // Esconde os botões de PDF inicialmente e reseta seus textos
  const btnPdfId = document.getElementById("btn-pdf-id");
  const btnPdfNome = document.getElementById("btn-pdf-nome");
  
  btnPdfId.style.display = "none";
  btnPdfNome.style.display = "none";
  btnPdfId.textContent = "Baixar PDF por ID";
  btnPdfNome.textContent = "Baixar PDF por Nome";

  // Verifica se o usuário está logado antes de fazer qualquer busca
  const token = localStorage.getItem('token');
  
  if (!token) {
    resultado.innerHTML = "<p style='color:red;'>Você precisa estar logado para consultar agendamentos. <a href='login.html'>Fazer login</a></p>";
    return;
  }

  // Verifica se pelo menos um campo foi preenchido
  if (!usuarioId && !nomeCompleto) {
    resultado.innerHTML = "<p>Informe o ID do usuário ou o nome completo.</p>";
    return;
  }

  // Se ambos foram preenchidos, dá prioridade ao ID
  if (usuarioId && nomeCompleto) {
    document.getElementById("input-nome").value = ""; // Limpa o nome
  }

  try {
    let agendamentos = [];
    let tipoConsulta = "";

    if (usuarioId) {
      // Busca por ID
      const response = await fetchWithAuth(`/api/agendamentos/usuario/${usuarioId}`);
      if (response.status === 204) {
        resultado.innerHTML = "<p>Nenhuma consulta encontrada para este usuário.</p>";
        return;
      }
      if (response.status === 401) {
        resultado.innerHTML = "<p style='color:red;'>Erro de autenticação. Faça login novamente.</p>";
        return;
      }
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Erro ao buscar consultas por ID. Status: ${response.status}. Detalhes: ${errorText}`);
      }
      agendamentos = await response.json();
      tipoConsulta = "id";
      
      // Mostra o botão de PDF por ID
      const btnPdfId = document.getElementById("btn-pdf-id");
      btnPdfId.style.display = "inline-block";
      btnPdfId.setAttribute("data-usuario-id", usuarioId);
      btnPdfId.textContent = `Baixar PDF - ID ${usuarioId}`;
      
    } else if (nomeCompleto) {
      // Busca por nome completo (primeiro busca o usuário, depois os agendamentos)
      const responseUsuario = await fetch(`/usuarios/buscar-por-nome?nomeCompleto=${encodeURIComponent(nomeCompleto)}`);
      if (responseUsuario.status === 404) {
        resultado.innerHTML = "<p>Usuário não encontrado com este nome.</p>";
        return;
      }
      if (!responseUsuario.ok) {
        const errorText = await responseUsuario.text();
        throw new Error(`Erro ao buscar usuário por nome. Status: ${responseUsuario.status}. Detalhes: ${errorText}`);
      }
      
      const usuario = await responseUsuario.json();
      
      // Agora busca os agendamentos do usuário encontrado
      const responseAgendamentos = await fetchWithAuth(`/api/agendamentos/usuario/${usuario.id}`);
      if (responseAgendamentos.status === 204) {
        resultado.innerHTML = "<p>Nenhuma consulta encontrada para este usuário.</p>";
        return;
      }
      if (responseAgendamentos.status === 401) {
        resultado.innerHTML = "<p style='color:red;'>Erro de autenticação. Faça login novamente.</p>";
        return;
      }
      if (!responseAgendamentos.ok) {
        const errorText = await responseAgendamentos.text();
        throw new Error(`Erro ao buscar consultas do usuário. Status: ${responseAgendamentos.status}. Detalhes: ${errorText}`);
      }
      agendamentos = await responseAgendamentos.json();
      tipoConsulta = "nome";
      
      // Mostra o botão de PDF por Nome
      const btnPdfNome = document.getElementById("btn-pdf-nome");
      btnPdfNome.style.display = "inline-block";
      btnPdfNome.setAttribute("data-nome-completo", nomeCompleto);
      btnPdfNome.textContent = `Baixar PDF - ${nomeCompleto}`;
    }

    // Exibe os agendamentos encontrados
    resultado.innerHTML = agendamentos.map(a => `
      <div>
        <h4>Consulta</h4>
        <p><strong>Data/Hora:</strong> ${a.dataHora ? new Date(a.dataHora).toLocaleString() : ""}</p>
        <p><strong>Dentista:</strong> ${a.dentista && a.dentista.nome ? a.dentista.nome : ""}</p>
        <p><strong>Serviço:</strong> 
          ${a.servico && a.servico.tipo ? a.servico.tipo : (a.tipoServico ? a.tipoServico : "")}
        </p>
        <p><strong>Motivo(Informado pelo Cliente):</strong> ${a.motivo || ""}</p>
        <p><strong>Observações:</strong> ${a.observacoes || ""}</p>
      </div>
    `).join("");
    
  } catch (err) {
    resultado.innerHTML = `<p style="color:red;">${err.message}</p>`;
  }
});

// Event listener para o botão de PDF por ID
document.getElementById("btn-pdf-id").addEventListener("click", function () {
  const usuarioId = this.getAttribute("data-usuario-id");
  if (usuarioId) {
    window.open(`/consulta/${usuarioId}/pdf`, "_blank");
  }
});

// Event listener para o botão de PDF por Nome
document.getElementById("btn-pdf-nome").addEventListener("click", function () {
  const nomeCompleto = this.getAttribute("data-nome-completo");
  if (nomeCompleto) {
    window.open(`/consulta/pdf?nomeCompleto=${encodeURIComponent(nomeCompleto)}`, "_blank");
  }
});

document.getElementById("btn-pdf-periodo").addEventListener("click", function () {
  const dataInicio = document.getElementById("input-data-inicio").value;
  const dataFim = document.getElementById("input-data-fim").value;
  if (dataInicio && dataFim) {
    window.open(`/consulta/periodo/pdf?dataInicio=${dataInicio}&dataFim=${dataFim}`, "_blank");
  } else {
    alert("Preencha as datas de início e fim.");
  }
});

document.getElementById("btn-pdf-todos").addEventListener("click", function () {
  window.open(`/consulta/todos/pdf`, "_blank");
});