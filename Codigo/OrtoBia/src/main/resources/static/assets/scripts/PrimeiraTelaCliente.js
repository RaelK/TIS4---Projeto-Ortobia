document.addEventListener("DOMContentLoaded", async function () {
    const mensagem = document.getElementById("boasvindas");
    mensagem.innerHTML = `Olá ${sessionStorage.getItem(
      "nome"
    )}, as próximas consultas agendadas são`;
    // Função para carregar as consultas pelo Id do cliente
    const idCliente = sessionStorage.getItem("loggedId");
    const response = await fetch(
      `http://localhost:8080/agendamentos/usuario/${idCliente}`,
      { method: "GET" }
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
  
    // Verifica se há consultas
    if (consultas.length === 0) {
    } else {
      consultas.forEach((con) => {
        const dataConsulta = new Date(con.dataHora);
  
        const currentDate = new Date();
  
        const differenceInMilliseconds = dataConsulta - currentDate;
  
        // Converte a diferença para dias
        const differenceInDays = differenceInMilliseconds / (1000 * 60 * 60 * 24);
  
        // Extrair dia, mês e ano
        const dia2 = String(dataConsulta.getDate()).padStart(2, "0");
        const mes = String(dataConsulta.getMonth() + 1).padStart(2, "0"); // Mês é zero-indexado, então somamos 1
        const ano = dataConsulta.getFullYear();
  
        // Formatar data no formato dd-mm-yyyy
        const dia = `${dia2}-${mes}-${ano}`;
  
        // Extrair apenas a hora (formato: hh:mm)
        const hora = dataConsulta.toTimeString().split(" ")[0].slice(0, 5);
  
        //Exibe uma notificação sobre o agendamento que irá acontecer em menos de 3 dias
        if (differenceInDays <= 3 && differenceInDays > 0) {
          const toastLiveExample = document.getElementById("liveToast");
          const ct = document.getElementById("corpo-toast");
  
          ct.innerHTML = `Olá ${sessionStorage.getItem(
            "nome"
          )}, a sua próxima consulta está agendada para o dia ${dia} às ${hora} horas`;
  
          const toast = new bootstrap.Toast(toastLiveExample);
          toast.show();
        }
        if (currentDate < dataConsulta && con.atendida == false) {
          if(con.confirmada == false){
            divconsultas.innerHTML += `
            <div class="col">
              <div class="card h-100 bg-light bg-gradient border border-warning border-5">
                <div class="card-body">
                  <h5 class="card-title">Consulta agendada para o dia ${dia} às ${hora} horas</h5>
                  <h5 class="card-title text-danger text-decoration-underline">A consulta AINDA NÃO está confirmada!</h5>
                  <p class="card-text">Consulta no nome de ${con.usuario.nome_completo}.</p>
                  <button type="button" class="btn btn-success m-2" onclick="confirmarConsulta(${con.id})">Confirmar Consulta</button>
          <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#exampleModal" onclick="abrirModal(${con.id})">
             Remarcar Consulta
            </button>
                  <button type="button" class="btn btn-danger m-2" onclick="cancelarConsulta(${con.id})">Cancelar Consulta</button>
                </div>
              </div>
            </div>`;
          }
          if(con.confirmada == true){
            divconsultas.innerHTML += `
            <div class="col">
              <div class="card h-100 bg-light bg-gradient border border-success border-5">
                <div class="card-body">
                  <h5 class="card-title">Consulta agendada para o dia ${dia} às ${hora}</h5>
                  <h5 class="card-title text-success text-decoration-underline">A consulta está confirmada!</h5>
                  <p class="card-text">Consulta no nome de ${con.usuario.nome_completo}.</p>
          <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#exampleModal" onclick="abrirModal(${con.id})">
             Remarcar Consulta
            </button>
                  <button type="button" class="btn btn-danger m-2" onclick="cancelarConsulta(${con.id})">Cancelar Consulta</button>
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
          <p class="card-text">Consulta no nome de ${con.usuario.nome_completo}.</p>
        </div>
      </div>
    </div>`;
        }
      });
    }
});
  
  function cancelarConsulta(id) {
    fetch("http://localhost:8080/agendamentos/" + id, {
      method: "DELETE",
    })
      .then((response) => {
        if (response.ok) {
          console.log("Consulta deletada com sucesso");
          alert("Consulta deletada com sucesso");
          location.reload();
        } else {
          console.log("Falha ao deletar a consulta");
        }
      })
      .catch((error) => console.error("Erro:", error));
  }
  
  function remarcarConsulta(id, dataHora) {
    const nome = sessionStorage.getItem("nome"); // Obtém o nome do usuário a partir do sessionStorage
    fetch(`http://localhost:8080/agendamentos/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ data: dataHora, nome: nome }), // Inclua o nome no body do request
    })
      .then((response) => {
        if (response.ok) {
          alert("Consulta remarcada com sucesso");
          location.reload();
        } else {
          console.error("Falha ao remarcar a consulta");
        }
      })
      .catch((error) => console.error("Erro:", error));
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
      const maxTime = "21:00";
  
      if (horarioInput.value < minTime) {
        horarioInput.value = minTime;
      } else if (horarioInput.value > maxTime) {
        horarioInput.value = maxTime;
      }
    });
  }
  
  function obterData(id) {
    const data = document.getElementById("data").value; // data no formato YYYY-MM-DD
    const hora = document.getElementById("hora").value; // hora no formato HH:MM
  
    if (!data || !hora) {
      alert("Por favor, selecione data e horário!");
      return;
    }
  
    // Concatena a data e a hora no formato YYYY-MM-DDTHH:MM
    const dataHora = `${data}T${hora}:00`; // ":00" para segundos
  
    remarcarConsulta(id, dataHora); // Envia no formato LocalDateTime
  }
  
  function confirmarConsulta(id) {
    fetch(`http://localhost:8080/agendamentos/${id}/confirmar`, {
      method: "PUT"
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