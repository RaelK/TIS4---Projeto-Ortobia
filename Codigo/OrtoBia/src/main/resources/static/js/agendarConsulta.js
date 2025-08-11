document.getElementById("schedule-form").addEventListener("submit", function(event) {
    event.preventDefault(); // Evita o reload da página

    const agendamento = {
        nome: document.getElementById("name").value,
        email: document.getElementById("email").value,
        telefone: document.getElementById("phone").value,
        data: document.getElementById("date").value,
        horario: document.getElementById("time").value
    };

    fetch("/agendamentos", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(agendamento)
    })
    .then(response => {
        if (response.ok) {
            mostrarToast("Consulta agendada com sucesso!");
            document.getElementById("schedule-form").reset();
        } else {
            mostrarToast("Erro ao agendar a consulta. Tente novamente.", true);
        }
    })
    .catch(error => {
        console.error("Erro:", error);
        mostrarToast("Erro de conexão com o servidor.", true);
    });
});

function mostrarToast(mensagem, erro = false) {
    const toast = document.getElementById("toast");
    toast.textContent = mensagem;
    toast.style.backgroundColor = erro ? "#e53935" : "#4CAF50";
    toast.classList.add("show");

    setTimeout(() => {
        toast.classList.remove("show");
    }, 5000);
}