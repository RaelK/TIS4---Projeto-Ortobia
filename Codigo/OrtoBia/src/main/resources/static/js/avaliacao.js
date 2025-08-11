document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("formAvaliacao");
  const urlParams = new URLSearchParams(window.location.search);
  const agendamentoId = urlParams.get('agendamentoId');

  if (!agendamentoId) {
    alert("Erro: ID da consulta não encontrado.");
    window.location.href = "primeiraTelaCliente.html";
    return;
  }

  if (form) {
    form.addEventListener("submit", function (event) {
      event.preventDefault();

      const notaSelecionada = document.querySelector('input[name="nota"]:checked');
      if (!notaSelecionada) {
        alert("Por favor, selecione uma nota.");
        return;
      }

      const comentario = document.getElementById("comentario").value;
      const nota = parseInt(notaSelecionada.value);
      const anonimo = document.getElementById("anonimo").checked;

      const usuarioId = sessionStorage.getItem("loggedId");
      if (!usuarioId) {
        alert("Erro: usuário não identificado.");
        return;
      }

      const avaliacao = {
        nota: nota,
        comentario: comentario,
        anonimo: anonimo,
        agendamentoId: agendamentoId
      };

      fetch(`http://localhost:8080/api/avaliacoes`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(avaliacao)
      })
        .then((response) => {
          if (response.ok) {
            alert("Avaliação enviada com sucesso!");
            window.location.href = "primeiraTelaCliente.html";
          } else {
            alert("Erro ao enviar avaliação.");
          }
        })
        .catch((error) => {
          console.error("Erro:", error);
          alert("Erro ao enviar avaliação.");
        });
    });
  }
});
