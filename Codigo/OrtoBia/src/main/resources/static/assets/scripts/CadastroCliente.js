const API_URL = 'http://localhost:8080/usuarios';

function cadastrarUsuario() {
    const nomeCompleto = document.getElementById('nomeCompleto').value;
    const cpf = document.getElementById('cpf').value;
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const email = document.getElementById('email').value;
    const telefone = document.getElementById('telefone').value;

const dados = {
    nome_completo : nomeCompleto,
    cpf : cpf,
    username: username,
    password: password,
    email: email,
    telefone : telefone
};

fetch(API_URL, {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
    },
    body: JSON.stringify(dados),
})
.then(response => {
    if (!response.ok) {
        throw new Error('Erro ao cadastrar o usuário.');
    }
    return response.json();
})
.then(data => {
    console.log('Sucesso:', data);
    alert("Usuário cadastrado " + nomeCompleto + " com sucesso!")

    window.location.href = "index.html";
})
.catch(error => {
    console.error('Erro:', error);
    alert("Erro ao cadastrar novo usuário")
});

}

