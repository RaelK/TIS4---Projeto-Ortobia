const API_BASE_URL = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', () => {
    const evaluationForm = document.getElementById('evaluation-form');
    if (evaluationForm) {
        evaluationForm.addEventListener('submit', handleEvaluationSubmit);
    }
});

async function handleEvaluationSubmit(event) {
    event.preventDefault();

    const rating = document.querySelector('input[name="rating"]:checked');
    if (!rating) {
        alert('Por favor, selecione uma nota para o atendimento.');
        return;
    }

    const agendamentoId = new URLSearchParams(window.location.search).get('agendamentoId');
    if (!agendamentoId) {
        alert('ID do agendamento não encontrado.');
        return;
    }

    const avaliacao = {
        agendamento: {
            id: agendamentoId
        },
        paciente: {
            id: sessionStorage.getItem('loggedId')
        },
        nota: parseInt(rating.value),
        comentario: document.getElementById('comment').value,
        anonimo: document.getElementById('anonymous').checked
    };

    try {
        const response = await fetch(`${API_BASE_URL}/avaliacoes`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(avaliacao)
        });

        if (response.ok) {
            alert('Avaliação enviada com sucesso!');
            window.location.href = 'primeiraTelaCliente.html';
        } else {
            const error = await response.json();
            alert(`Erro ao enviar avaliação: ${error.message || 'Erro desconhecido'}`);
        }
    } catch (error) {
        console.error('Erro ao enviar avaliação:', error);
        alert('Erro ao enviar avaliação. Por favor, tente novamente.');
    }
} 