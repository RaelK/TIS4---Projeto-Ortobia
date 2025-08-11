// Configuração da URL base da API
const API_BASE_URL = 'http://localhost:8080/api';

// Função para formatar CPF
function formatCPF(cpf) {
    cpf = cpf.replace(/\D/g, '');
    cpf = cpf.replace(/(\d{3})(\d)/, '$1.$2');
    cpf = cpf.replace(/(\d{3})(\d)/, '$1.$2');
    cpf = cpf.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    return cpf;
}

// Função para formatar telefone
function formatPhone(phone) {
    phone = phone.replace(/\D/g, '');
    phone = phone.replace(/(\d{2})(\d)/, '($1) $2');
    phone = phone.replace(/(\d{5})(\d{4})$/, '$1-$2');
    return phone;
}

// Inicialização dos formulários
document.addEventListener('DOMContentLoaded', () => {
    // Formulário de login
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Formulário de cadastro
    const cadastroForm = document.getElementById('cadastro-form');
    if (cadastroForm) {
        // Adicionar formatação de CPF
        const cpfInput = document.getElementById('cpf');
        if (cpfInput) {
            cpfInput.addEventListener('input', (e) => {
                e.target.value = formatCPF(e.target.value);
            });
        }

        // Adicionar formatação de telefone
        const phoneInput = document.getElementById('telefone');
        if (phoneInput) {
            phoneInput.addEventListener('input', (e) => {
                e.target.value = formatPhone(e.target.value);
            });
        }

        cadastroForm.addEventListener('submit', handleCadastro);
    }
});

// Função para lidar com o login
async function handleLogin(event) {
    event.preventDefault();
    
    const formData = {
        email: document.getElementById('email').value,
        senha: document.getElementById('password').value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            const data = await response.json();
            // Armazenar o token e o ID do usuário no sessionStorage
            console.log(data);
            localStorage.setItem('token', data.token);
            sessionStorage.setItem('loggedId', data.id);
            sessionStorage.setItem('nome', data.nome);
            sessionStorage.setItem('role', data.role);
            alert('Login realizado com sucesso!');
            if (data.role == 'DENTISTA' || data.role == 'SECRETARIA') {
                // Redirecionar para a página do dentista
                window.location.href = 'dashboard.html';
            }
            else{
            // Redirecionar para a página principal
            window.location.href = 'primeiraTelaCliente.html';
            }
        } else {
            const error = await response.json();
            alert(`Erro ao fazer login: ${error.message}`);
        }
    } catch (error) {
        console.error('Erro ao fazer login:', error);
        alert('Erro ao fazer login. Por favor, tente novamente.');
    }
}

// Função para lidar com o cadastro
async function handleCadastro(event) {
    event.preventDefault();
    
    const senha = document.getElementById('senha').value;
    const confirmarSenha = document.getElementById('confirmar-senha').value;

    if (senha !== confirmarSenha) {
        alert('As senhas não coincidem!');
        return;
    }

    const formData = {
        nome_completo: document.getElementById('nome').value,
        email: document.getElementById('email').value,
        username: document.getElementById('email').value.split('@')[0], // Usando parte do email como username
        cpf: document.getElementById('cpf').value.replace(/\D/g, ''),
        telefone: document.getElementById('telefone').value.replace(/\D/g, ''),
        endereco: document.getElementById('endereco').value,
        password: senha
    };

    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            alert('Cadastro realizado com sucesso!');
            window.location.href = 'login.html';
        } else {
            const error = await response.json();
            alert(`Erro ao cadastrar: ${error.message}`);
        }
    } catch (error) {
        console.error('Erro ao cadastrar:', error);
        alert('Erro ao cadastrar. Por favor, tente novamente.');
    }
}

// Função para verificar se o usuário está autenticado
function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        // Redirecionar para a página de login se não estiver autenticado
        if (!window.location.pathname.includes('login.html') && 
            !window.location.pathname.includes('cadastro.html')) {
            window.location.href = 'login.html';
        }
    }
    return token;
}

// Função para fazer logout
function logout() {
    localStorage.removeItem('token');
    window.location.href = 'login.html';
}

// Verificar autenticação em todas as páginas exceto login e cadastro
if (!window.location.pathname.includes('login.html') && 
    !window.location.pathname.includes('cadastro.html')) {
    checkAuth();
} 