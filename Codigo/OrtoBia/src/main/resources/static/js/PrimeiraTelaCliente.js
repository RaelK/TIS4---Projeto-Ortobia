// Verificação de autenticação
if (!sessionStorage.getItem('loggedId') || !sessionStorage.getItem('nome')) {
    window.location.href = 'login.html';
}

document.addEventListener("DOMContentLoaded", async function () {
    const mensagem = document.getElementById("boasvindas");
    mensagem.innerHTML = `Olá ${sessionStorage.getItem(
      "nome"
    )}, as próximas consultas agendadas são`;
    // Função para carregar as consultas pelo Id do cliente
    const idCliente = sessionStorage.getItem("loggedId");
    const token = `jwt-token-${idCliente}`;
    const response = await fetch(
      `http://localhost:8080/api/agendamentos/usuario/${idCliente}`,
      { 
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`
        }
      }
    );
    const divconsultas = document.getElementById("prox-consultas");
    const consultasAnteriores = document.getElementById("ant-consultas");
  
    if (!response.ok) {
      console.error("Erro na resposta da requisição", response.status);
      return;
    }
  
    // Verifica se há conteúdo na resposta
    const responseText = await response.text();
    if (!responseText) {
      divconsultas.innerHTML += `<p class="h4 p-3 text-black border border-black text-center">Você não possui consultas agendadas.</p>`;
      return;
    }
  
    const consultas = JSON.parse(responseText);
    const currentDate = new Date();

    consultas.forEach((con) => {
        const dataConsulta = new Date(con.dataHora);
        const dia = dataConsulta.toLocaleDateString();
        const hora = dataConsulta.toLocaleTimeString();

        if (currentDate < dataConsulta && con.atendida == false) {
          if(con.confirmada == false){
            divconsultas.innerHTML += `
            <div class="col">
              <div class="card h-100 bg-light bg-gradient border border-warning border-5">
                <div class="card-body">
                  <h5>Consulta agendada para o dia ${dia} às ${hora} horas</h5>
                  <h5 class="text-danger text-decoration-underline">A consulta AINDA NÃO está confirmada!</h5>
                  <p class="card-text">Consulta no nome de ${con.usuario.nome}.</p>
                  <button type="button" class="btn btn-success m-2" onclick="confirmarConsulta(${con.id})">Confirmar Consulta</button>
                  <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#exampleModal" onclick="abrirModal(${con.id})">
                    Remarcar Consulta
                  </button>
                  <button type="button" class="btn btn-danger m-2" onclick="cancelarConsultaAlternativa(${con.id})">Cancelar Consulta</button>
                </div>
              </div>
            </div>`;
          }
          if(con.confirmada == true){
            divconsultas.innerHTML += `
            <div class="col">
              <div class="card h-100 bg-light bg-gradient border border-success border-5">
                <div class="card-body">
                  <h5>Consulta agendada para o dia ${dia} às ${hora} horas</h5>
                  <h5 class="card-title text-success text-decoration-underline">A consulta está confirmada!</h5>
                  <p class="card-text">Consulta no nome de ${con.usuario.nome}.</p>
                  <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#exampleModal" onclick="abrirModal(${con.id})">
                    Remarcar Consulta
                  </button>
                  <button type="button" class="btn btn-danger m-2" onclick="cancelarConsultaAlternativa(${con.id})">Cancelar Consulta</button>
                </div>
              </div>
            </div>`;
          }
        } else {
          consultasAnteriores.innerHTML += `
          <div class="col">
            <div class="card h-100 bg-dark-subtle border border-secondary border-5">
              <div class="card-body">
                <h5 class="card-title">Consulta realizada no dia ${dia} às ${hora} horas</h5>
                <p class="card-text">Consulta no nome de ${con.usuario.nome}.</p>
                ${!con.avaliada ? `
                <button type="button" class="btn btn-primary m-2" onclick="finalizarConsulta(${con.id})">Avaliar Consulta</button>
                ` : ''}
              </div>
            </div>
          </div>`;
        }
    });
});

function cancelarConsulta(id) {
    const idCliente = sessionStorage.getItem("loggedId");
    const token = `jwt-token-${idCliente}`;
    
    fetch(`http://localhost:8080/api/agendamentos/${id}/cancelar/${idCliente}`, {
      method: "DELETE",
      headers: {
        "Authorization": `Bearer ${token}`
      }
    })
      .then((response) => {
        console.log("Response status:", response.status);
        console.log("Response ok:", response.ok);
        
        if (response.ok) {
          console.log("Consulta deletada com sucesso");
          alert("Consulta cancelada com sucesso");
          location.reload();
        } else {
          console.log("Falha ao deletar a consulta");
          response.text().then(text => {
            console.log("Error response:", text);
          });
          alert("Erro ao cancelar a consulta. Tente novamente.");
        }
      })
      .catch((error) => {
        console.error("Erro na requisição:", error);
        alert("Erro de conexão. Tente novamente.");
      });
}

function remarcarConsulta(id, dataHora) {
    const idCliente = sessionStorage.getItem("loggedId");
    const token = `jwt-token-${idCliente}`;
    
    console.log("Remarcando consulta ID:", id);
    console.log("Cliente ID:", idCliente);
    console.log("Nova data/hora:", dataHora);
    
    fetch(`http://localhost:8080/api/agendamentos/${id}/remarcar/${idCliente}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({ dataHora: dataHora }),
    })
      .then((response) => {
        console.log("Response status:", response.status);
        console.log("Response ok:", response.ok);
        
        if (response.ok) {
          alert("Consulta remarcada com sucesso");
          // Fechar o modal
          const modal = document.getElementById('exampleModal');
          const modalInstance = bootstrap.Modal.getInstance(modal);
          if (modalInstance) {
            modalInstance.hide();
          }
          location.reload();
        } else {
          console.error("Falha ao remarcar a consulta");
          response.text().then(text => {
            console.log("Error response:", text);
          });
          alert("Erro ao remarcar a consulta. Tente novamente.");
        }
      })
      .catch((error) => {
        console.error("Erro na requisição:", error);
        alert("Erro de conexão. Tente novamente.");
      });
}

function confirmarConsulta(id) {
    // Mostrar caixa de diálogo de confirmação
    if (!confirm("Tem certeza que deseja confirmar esta consulta?")) {
        return; // Se o usuário cancelar, não executa a ação
    }
    
    const idCliente = sessionStorage.getItem("loggedId");
    const token = `jwt-token-${idCliente}`;
    fetch(`http://localhost:8080/api/agendamentos/${id}/confirmar`, {
      method: "PUT",
      headers: {
        "Authorization": `Bearer ${token}`
      }
    })
    .then(response => {
      if (response.ok) {
        alert("A consulta foi confirmada!");
        location.reload(true); 
      } else {
        alert("Erro ao confirmar consulta.");
      }
    })
    .catch(error => console.error('Erro:', error));
}

function finalizarConsulta(id) {
    const idCliente = sessionStorage.getItem("loggedId");
    const token = `jwt-token-${idCliente}`;
    fetch(`http://localhost:8080/api/agendamentos/${id}/finalizar`, {
      method: "PUT",
      headers: {
        "Authorization": `Bearer ${token}`
      }
    })
    .then(response => {
      if (response.ok) {
        window.location.href = `avaliacao.html?agendamentoId=${id}`;
      } else {
        alert("Erro ao finalizar consulta.");
      }
    })
    .catch(error => console.error('Erro:', error));
} 

function abrirModal(id) {
    const modal = document.getElementById("ModalCorpo");
    modal.innerHTML = `
      <form>
          <label for="data">Selecione uma data:</label>
          <input type="date" id="data" name="data">
          <br>
          <label for="hora">Selecione um horário:</label>
          <input type="time" id="hora" name="hora" min="09:00" max="21:00">
          <button type="button" class="btn btn-info" onclick="obterData(${id})">Remarcar Consulta</button>
      </form>`;

    const horarioInput = document.getElementById("hora");

    horarioInput.addEventListener("input", () => {
      const minTime = "09:00";
      const maxTime = "19:00";

      if (horarioInput.value < minTime) {
        horarioInput.value = minTime;
      } else if (horarioInput.value > maxTime) {
        horarioInput.value = maxTime;
      }
    });
}

function obterData(id) {
    const data = document.getElementById("data").value;
    const hora = document.getElementById("hora").value;

    if (!data || !hora) {
      alert("Por favor, selecione data e horário!");
      return;
    }

    // Mostrar caixa de diálogo de confirmação
    const dataFormatada = new Date(data + 'T' + hora).toLocaleDateString('pt-BR');
    const horaFormatada = hora;
    if (!confirm(`Tem certeza que deseja remarcar a consulta para ${dataFormatada} às ${horaFormatada}?`)) {
        return; // Se o usuário cancelar, não executa a ação
    }

    const dataHora = `${data}T${hora}:00`;
    remarcarConsultaSimples(id, dataHora);
}

// Função simples para remarcar consulta apenas com ID
function remarcarConsultaSimples(id, dataHora) {
    console.log("Remarcando consulta ID:", id);
    console.log("Nova data/hora:", dataHora);
    
    fetch(`http://localhost:8080/api/agendamentos/simples/${id}/remarcar`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ dataHora: dataHora }),
    })
      .then((response) => {
        console.log("Response status:", response.status);
        console.log("Response ok:", response.ok);
        
        if (response.ok) {
          alert("Consulta remarcada com sucesso!");
          // Fechar o modal
          const modal = document.getElementById('exampleModal');
          const modalInstance = bootstrap.Modal.getInstance(modal);
          if (modalInstance) {
            modalInstance.hide();
          }
          location.reload();
        } else {
          response.text().then(text => {
            console.log("Error response:", text);
            alert("Erro ao remarcar consulta: " + response.status);
          });
        }
      })
      .catch((error) => {
        console.error("Erro na requisição:", error);
        alert("Erro de conexão. Tente novamente.");
      });
}

// Função alternativa para cancelar consulta (marca como cancelado)
function cancelarConsultaAlternativa(id) {
    // Mostrar caixa de diálogo de confirmação
    if (!confirm("Tem certeza que deseja cancelar esta consulta? Esta ação não pode ser desfeita.")) {
        return; // Se o usuário cancelar, não executa a ação
    }
    
    console.log("Cancelando consulta ID (alternativo):", id);
    
    const idCliente = sessionStorage.getItem("loggedId");
    console.log("ID do cliente logado:", idCliente);
    
    fetch(`http://localhost:8080/api/agendamentos/simples/${id}`, {
      method: "DELETE"
    })
      .then((response) => {
        console.log("Response status:", response.status);
        console.log("Response ok:", response.ok);
        
        if (response.ok) {
          alert("Consulta cancelada com sucesso!");
          location.reload();
        } else {
          response.text().then(text => {
            console.log("Error response:", text);
            alert("Erro ao cancelar consulta: " + response.status);
          });
        }
      })
      .catch((error) => {
        console.error("Erro na requisição:", error);
        alert("Erro de conexão. Tente novamente.");
      });
}