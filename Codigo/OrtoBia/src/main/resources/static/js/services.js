// Verificação de autenticação e permissão
if (!sessionStorage.getItem('loggedId') || sessionStorage.getItem('role') !== 'DENTISTA') {
    window.location.href = 'login.html';
}

const API_BASE_URL = 'http://localhost:8080/api';

const serviceNames = {
    'Extrações no geral': 'Extrações no geral',
    'Implantes': 'Implantes',
    'Próteses': 'Próteses',
    'Clareamento': 'Clareamento',
    'Limpeza': 'Limpeza',
    'Raspagem radicular/Tratamento periodontal': 'Raspagem radicular/Tratamento periodontal',
    'Tratamento ortodôntico': 'Tratamento ortodôntico',
    'Tratamentos esqueléticos cirúrgicos': 'Tratamentos esqueléticos cirúrgicos',
    'Atendimento pediátrico': 'Atendimento pediátrico',
    'Restaurações de cáries': 'Restaurações de cáries',
    'Facetas': 'Facetas'
};

function formatPrice(price) {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(price);
}

function formatDuration(minutes) {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return hours > 0 ? `${hours}h${mins > 0 ? ` ${mins}min` : ''}` : `${mins}min`;
}

async function loadServices() {
    try {
        const response = await fetch(`${API_BASE_URL}/services`);
        if (response.ok) {
            const services = await response.json();
            displayServices(services);
        } else {
            console.error('Erro ao carregar serviços');
        }
    } catch (error) {
        console.error('Erro ao carregar serviços:', error);
    }
}

function displayServices(services) {
    const tbody = document.getElementById('services-body');
    tbody.innerHTML = '';

    services.forEach(service => {

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${service.tipo}</td>
            <td>${service.descricao}</td>
            <td>${formatPrice(service.preco)}</td>
            <td>${formatDuration(service.duracao)}</td>
            <td>
                <button onclick="editService(${service.id})" class="btn-edit">Editar</button>
                <button onclick="deleteService(${service.id})" class="btn-delete">Excluir</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function handleServiceSubmit(event) {
    event.preventDefault();

    const form = event.target;
    const id = form.dataset.editId;

    const servico = {
        tipo: document.getElementById('service-type').value,
        nome: document.getElementById('service-type').selectedOptions[0].text,
        descricao: document.getElementById('description').value,
        preco: parseFloat(document.getElementById('price').value),
        duracao: parseInt(document.getElementById('duration').value)
    };

    const url = id ? `${API_BASE_URL}/services/${id}` : `${API_BASE_URL}/services`;
    const method = id ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(servico)
        });

        if (response.ok) {
            alert(`Serviço ${id ? 'atualizado' : 'cadastrado'} com sucesso!`);
            form.reset();
            delete form.dataset.editId;
            document.querySelector('button[type="submit"]').textContent = 'Cadastrar Serviço';
            loadServices();
        } else {
            const error = await response.json();
            alert(`Erro: ${error.message || 'Erro desconhecido'}`);
        }
    } catch (error) {
        console.error('Erro ao cadastrar/atualizar serviço:', error);
        alert('Erro de conexão com o servidor.');
    }
}

async function editService(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/services/${id}`);
        if (response.ok) {
            const service = await response.json();

            document.getElementById('description').value = service.descricao;
            document.getElementById('price').value = service.preco;
            document.getElementById('duration').value = service.duracao;

            // Tentar encontrar o value correto pelo nome
            const select = document.getElementById('service-type');
            for (let option of select.options) {
                if (option.text === service.nome) {
                    option.selected = true;
                    break;
                }
            }

            const form = document.getElementById('service-form');
            form.dataset.editId = id;
            document.querySelector('button[type="submit"]').textContent = 'Atualizar Serviço';
        }
    } catch (error) {
        console.error('Erro ao carregar serviço:', error);
        alert('Erro ao carregar serviço para edição.');
    }
}

async function deleteService(id) {
    if (!confirm('Tem certeza que deseja excluir este serviço?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/services/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            alert('Serviço excluído com sucesso!');
            loadServices();
        } else {
            const error = await response.json();
            alert(`Erro ao excluir serviço: ${error.message}`);
        }
    } catch (error) {
        console.error('Erro ao excluir serviço:', error);
        alert('Erro ao excluir serviço. Por favor, tente novamente.');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('service-form');
    form.addEventListener('submit', handleServiceSubmit);
    loadServices();
});