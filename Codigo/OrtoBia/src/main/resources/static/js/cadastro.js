const API_BASE_URL = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('cadastro-form');
    if (form) {
        form.addEventListener('submit', async function (event) {
            event.preventDefault();

            // Pegando os valores dos campos do formulário
            const nome = document.getElementById('nome').value;
            const email = document.getElementById('email').value;
            const telefone = document.getElementById('telefone').value;
            const cpf = document.getElementById('cpf').value;
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            // Montando o objeto para enviar
            const usuario = {
                nome: nome,
                email: email,
                telefone: telefone,
                cpf: cpf,
                username: username,
                password: password,
            };

            try {
                const response = await fetch(`${API_BASE_URL}/auth/register`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(usuario)
                });

                if (response.ok) {
                    alert('Cadastro realizado com sucesso!');
                    window.location.href = 'login.html';
                } else {
                    const error = await response.json();
                    alert('Erro ao cadastrar: ' + (error.message || 'Erro desconhecido'));
                }
            } catch (error) {
                console.error('Erro ao cadastrar:', error);
                alert('Erro ao cadastrar. Por favor, tente novamente.');
            }
        });
    }
});