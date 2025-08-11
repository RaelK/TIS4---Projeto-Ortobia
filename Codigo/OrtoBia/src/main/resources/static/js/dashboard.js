document.addEventListener('DOMContentLoaded', function() {
    // Verificar se o usuário está autenticado
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    // Carregar dados do dashboard
    carregarDadosDashboard();
    
    // Atualizar título da agenda com a data atual
    atualizarTituloAgenda();
    
    // Adicionar event listener para o botão de confirmar atendimento
    const btnConfirmarAtendimento = document.getElementById('btnConfirmarAtendimento');
    if (btnConfirmarAtendimento) {
        btnConfirmarAtendimento.addEventListener('click', confirmarAtendimento);
    }
});

// Função para atualizar o título da agenda com a data atual
function atualizarTituloAgenda() {
    const hoje = new Date();
    const dataFormatada = hoje.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
    
    const tituloElement = document.getElementById('titulo-agenda-hoje');
    if (tituloElement) {
        tituloElement.textContent = `Agenda de Hoje - ${dataFormatada}`;
    }
}

// Função principal para carregar todos os dados do dashboard
async function carregarDadosDashboard() {
    try {
        // Buscar estatísticas do dashboard
        const estatisticas = await buscarEstatisticasDashboard();
        
        // Atualizar cards de resumo usando as estatísticas
        atualizarCardsResumoComEstatisticas(estatisticas);
        
        // Criar gráficos usando as estatísticas
        criarGraficoProcedimentosComEstatisticas(estatisticas.procedimentosPorTipo);
        criarGraficoDiasSemanaComEstatisticas(estatisticas.agendamentosPorDiaSemana);
        criarGraficoHorariosComEstatisticas(estatisticas.agendamentosPorHorario);
        criarGraficoMesesComEstatisticas(estatisticas.agendamentosPorMes);
        
        // Carregar agenda do dia
        carregarAgendaHoje();
        
    } catch (error) {
        console.error('Erro ao carregar dados do dashboard:', error);
        mostrarErro('Erro ao carregar dados do dashboard');
    }
}

// Função para buscar estatísticas do dashboard
async function buscarEstatisticasDashboard() {
    const token = localStorage.getItem('token');
    
    const response = await fetch('/api/dashboard/estatisticas', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    
    if (!response.ok) {
        throw new Error('Erro ao buscar estatísticas do dashboard');
    }
    
    return await response.json();
}

// Função para buscar agendamentos do dentista
async function buscarAgendamentosDentista(dataInicio, dataFim) {
    const token = localStorage.getItem('token');
    
    const inicioISO = dataInicio.toISOString();
    const fimISO = dataFim.toISOString();
    
    const response = await fetch(`/api/agendamentos/dentista?inicio=${inicioISO}&fim=${fimISO}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    
    if (!response.ok) {
        throw new Error('Erro ao buscar agendamentos');
    }
    
    return await response.json();
}

// Função para atualizar os cards de resumo usando as estatísticas
function atualizarCardsResumoComEstatisticas(estatisticas) {
    // Buscar agendamentos de hoje separadamente
    buscarAgendamentosHoje().then(agendamentosHoje => {
        document.getElementById('consultas-hoje').textContent = agendamentosHoje.length;
    });
    
    // Buscar próximas consultas
    buscarProximasConsultas().then(proximasConsultas => {
        document.getElementById('proximas-consultas').textContent = proximasConsultas.length;
    });
    
    // Usar estatísticas do backend
    document.getElementById('consultas-pendentes').textContent = estatisticas.agendamentosPendentes || 0;
    document.getElementById('consultas-finalizadas').textContent = estatisticas.agendamentosAtendidos || 0;
}

// Função para buscar agendamentos de hoje
async function buscarAgendamentosHoje() {
    const hoje = new Date();
    const inicioHoje = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
    const fimHoje = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate(), 23, 59, 59);
    
    return await buscarAgendamentosDentista(inicioHoje, fimHoje);
}

// Função para buscar próximas consultas (próximos 7 dias)
async function buscarProximasConsultas() {
    const hoje = new Date();
    const amanha = new Date(hoje);
    amanha.setDate(amanha.getDate() + 1);
    const proximosSete = new Date(hoje);
    proximosSete.setDate(proximosSete.getDate() + 7);
    
    return await buscarAgendamentosDentista(amanha, proximosSete);
}

// Função para atualizar os cards de resumo
function atualizarCardsResumo(agendamentos) {
    const hoje = new Date();
    const inicioHoje = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
    const fimHoje = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate(), 23, 59, 59);
    
    const proximosSete = new Date();
    proximosSete.setDate(proximosSete.getDate() + 7);
    
    const inicioMes = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
    
    // Consultas hoje
    const consultasHoje = agendamentos.filter(agendamento => {
        const dataAgendamento = new Date(agendamento.dataHora);
        return dataAgendamento >= inicioHoje && dataAgendamento <= fimHoje;
    }).length;
    
    // Próximas consultas (próximos 7 dias)
    const proximasConsultas = agendamentos.filter(agendamento => {
        const dataAgendamento = new Date(agendamento.dataHora);
        return dataAgendamento > fimHoje && dataAgendamento <= proximosSete;
    }).length;
    
    // Consultas pendentes (não confirmadas)
    const consultasPendentes = agendamentos.filter(agendamento => 
        !agendamento.confirmada && new Date(agendamento.dataHora) >= inicioHoje
    ).length;
    
    // Consultas finalizadas este mês
    const consultasFinalizadas = agendamentos.filter(agendamento => {
        const dataAgendamento = new Date(agendamento.dataHora);
        return agendamento.atendida && dataAgendamento >= inicioMes;
    }).length;
    
    // Atualizar DOM
    document.getElementById('consultas-hoje').textContent = consultasHoje;
    document.getElementById('proximas-consultas').textContent = proximasConsultas;
    document.getElementById('consultas-pendentes').textContent = consultasPendentes;
    document.getElementById('consultas-finalizadas').textContent = consultasFinalizadas;
}

// Função para criar gráfico de procedimentos usando estatísticas
function criarGraficoProcedimentosComEstatisticas(procedimentosData) {
    if (!procedimentosData || Object.keys(procedimentosData).length === 0) {
        const ctx = document.getElementById('procedimentosChart').getContext('2d');
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Nenhum dado'],
                datasets: [{
                    data: [1],
                    backgroundColor: ['#e9ecef'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
        return;
    }

    // Ordenar por quantidade e pegar os top 5
    const procedimentosOrdenados = Object.entries(procedimentosData)
        .sort(([,a], [,b]) => b - a)
        .slice(0, 5);
    
    const labels = procedimentosOrdenados.map(([nome]) => nome);
    const dados = procedimentosOrdenados.map(([,quantidade]) => quantidade);
    
    const ctx = document.getElementById('procedimentosChart').getContext('2d');
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: dados,
                backgroundColor: [
                    '#FF6384',
                    '#36A2EB',
                    '#FFCE56',
                    '#4BC0C0',
                    '#9966FF'
                ],
                borderWidth: 2,
                borderColor: '#fff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const total = dados.reduce((a, b) => a + b, 0);
                            const percentage = ((context.raw / total) * 100).toFixed(1);
                            return `${context.label}: ${context.raw} (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

// Função para criar gráfico de dias da semana usando estatísticas
function criarGraficoDiasSemanaComEstatisticas(diasSemanaData) {
    const diasSemana = ['domingo', 'segunda-feira', 'terça-feira', 'quarta-feira', 'quinta-feira', 'sexta-feira', 'sábado'];
    const diasSemanaFormatados = ['Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];
    
    const consultasPorDia = diasSemana.map(dia => diasSemanaData[dia] || 0);
    
    const ctx = document.getElementById('diasSemanaChart').getContext('2d');
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: diasSemanaFormatados,
            datasets: [{
                label: 'Consultas',
                data: consultasPorDia,
                backgroundColor: 'rgba(54, 162, 235, 0.8)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
}

// Função para criar gráfico de horários com mais agendamentos
function criarGraficoHorariosComEstatisticas(horariosData) {
    if (!horariosData || Object.keys(horariosData).length === 0) {
        const ctx = document.getElementById('horariosChart').getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Nenhum dado'],
                datasets: [{
                    data: [1],
                    backgroundColor: ['#e9ecef'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });
        return;
    }

    // Criar array com horários de 8h às 18h
    const horarios = [];
    for (let h = 8; h <= 18; h++) {
        horarios.push(`${h.toString().padStart(2, '0')}:00`);
    }
    
    const agendamentosPorHorario = horarios.map(horario => horariosData[horario] || 0);
    
    const ctx = document.getElementById('horariosChart').getContext('2d');
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: horarios,
            datasets: [{
                label: 'Agendamentos',
                data: agendamentosPorHorario,
                backgroundColor: 'rgba(75, 192, 192, 0.8)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Horário'
                    }
                },
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    },
                    title: {
                        display: true,
                        text: 'Número de Agendamentos'
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.raw} agendamento(s) às ${context.label}`;
                        }
                    }
                }
            }
        }
    });
}

// Função para criar gráfico de atendimentos por mês
function criarGraficoMesesComEstatisticas(mesesData) {
    if (!mesesData || Object.keys(mesesData).length === 0) {
        const ctx = document.getElementById('mesesChart').getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: ['Nenhum dado'],
                datasets: [{
                    data: [1],
                    backgroundColor: ['#e9ecef'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });
        return;
    }

    // Definir os meses do ano em português
    const mesesNomes = [
        'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
        'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
    ];
    
    // Pegar os últimos 12 meses
    const hoje = new Date();
    const mesesLabels = [];
    const atendimentosPorMes = [];
    
    for (let i = 11; i >= 0; i--) {
        const mesAtual = new Date(hoje.getFullYear(), hoje.getMonth() - i, 1);
        const chave = `${mesAtual.getFullYear()}-${(mesAtual.getMonth() + 1).toString().padStart(2, '0')}`;
        const label = `${mesesNomes[mesAtual.getMonth()]} ${mesAtual.getFullYear()}`;
        
        mesesLabels.push(label);
        atendimentosPorMes.push(mesesData[chave] || 0);
    }
    
    const ctx = document.getElementById('mesesChart').getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: mesesLabels,        datasets: [{
            label: 'Agendamentos',
            data: atendimentosPorMes,
                backgroundColor: 'rgba(153, 102, 255, 0.2)',
                borderColor: 'rgba(153, 102, 255, 1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: 'rgba(153, 102, 255, 1)',
                pointBorderColor: '#fff',
                pointHoverBackgroundColor: '#fff',
                pointHoverBorderColor: 'rgba(153, 102, 255, 1)'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Mês'
                    }
                },
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    },
                    title: {
                        display: true,
                        text: 'Número de Agendamentos'
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.raw} agendamento(s) em ${context.label}`;
                        }
                    }
                }
            }
        }
    });
}

// Função para carregar a agenda do dia
async function carregarAgendaHoje() {
    try {
        const hoje = new Date();
        const inicioHoje = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
        const fimHoje = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate(), 23, 59, 59);
        
        const agendamentos = await buscarAgendamentosDentista(inicioHoje, fimHoje);
        
        // Filtrar apenas agendamentos de hoje
        const agendamentosHoje = agendamentos.filter(agendamento => {
            const dataAgendamento = new Date(agendamento.dataHora);
            return dataAgendamento >= inicioHoje && dataAgendamento <= fimHoje;
        });
        
        // Ordenar por horário
        agendamentosHoje.sort((a, b) => new Date(a.dataHora) - new Date(b.dataHora));
        
        const agendaContainer = document.getElementById('agenda-hoje');
        
        if (agendamentosHoje.length === 0) {
            agendaContainer.innerHTML = '<p class="text-muted">Nenhuma consulta agendada para hoje.</p>';
            return;
        }
        
        let html = '<div class="list-group">';
        
        agendamentosHoje.forEach(agendamento => {
            const dataHora = new Date(agendamento.dataHora);
            const horario = dataHora.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
            const nomeDentista = sessionStorage.getItem('nome') || 'Dentista';
            
            let statusClass = '';
            let statusText = '';
            let botaoAtender = '';
            
            if (agendamento.atendida) {
                statusClass = 'list-group-item-success';
                statusText = 'Finalizada';
                if (agendamento.observacoes) {
                    botaoAtender = `<br><small><strong>Observações:</strong> ${agendamento.observacoes}</small>`;
                }
            } else if (agendamento.confirmada) {
                statusClass = 'list-group-item-info';
                statusText = 'Confirmada';
                botaoAtender = `<br><button class="btn btn-atender btn-sm mt-2" onclick="abrirModalAtendimento(${agendamento.id}, '${agendamento.usuario.nome}', '${horario}', '${agendamento.servico.tipo}')">Atender</button>`;
            } else {
                statusClass = 'list-group-item-warning';
                statusText = 'Pendente';
                botaoAtender = `<br><button class="btn btn-atender btn-sm mt-2" onclick="abrirModalAtendimento(${agendamento.id}, '${agendamento.usuario.nome}', '${horario}', '${agendamento.servico.tipo}')">Atender</button>`;
            }
            
            html += `
                <div class="list-group-item ${statusClass}">
                    <div class="d-flex w-100 justify-content-between">
                        <h6 class="mb-1">${horario} - ${agendamento.usuario.nome}</h6>
                        <small class="badge bg-secondary">${statusText}</small>
                    </div>
                    <p class="mb-1"><strong>Dentista:</strong> ${nomeDentista}</p>
                    <p class="mb-1"><strong>Serviço:</strong> ${agendamento.servico.tipo}</p>
                    <small><strong>Contato:</strong> ${agendamento.usuario.telefone || 'Não informado'}</small>
                    <br><small><strong>E-mail:</strong> ${agendamento.usuario.email || 'Não informado'}</small>
                    ${agendamento.motivo ? `<br><small><strong>Motivo:</strong> ${agendamento.motivo}</small>` : ''}
                    ${botaoAtender}
                </div>
            `;
        });
        
        html += '</div>';
        agendaContainer.innerHTML = html;
        
    } catch (error) {
        console.error('Erro ao carregar agenda do dia:', error);
        document.getElementById('agenda-hoje').innerHTML = '<p class="text-danger">Erro ao carregar agenda do dia.</p>';
    }
}

// Função para mostrar erros
function mostrarErro(mensagem) {
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-danger alert-dismissible fade show';
    alertDiv.innerHTML = `
        ${mensagem}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    const container = document.querySelector('.container');
    container.insertBefore(alertDiv, container.firstChild);
    
    // Remove o alerta após 5 segundos
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 5000);
}

// Função para mostrar sucesso
function mostrarSucesso(mensagem) {
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-success alert-dismissible fade show';
    alertDiv.innerHTML = `
        ${mensagem}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    const container = document.querySelector('.container');
    container.insertBefore(alertDiv, container.firstChild);
    
    // Remove o alerta após 5 segundos
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 5000);
}

// Variável global para armazenar o ID do agendamento sendo atendido
let agendamentoAtendimentoId = null;

// Função para abrir o modal de atendimento
function abrirModalAtendimento(agendamentoId, nomeUsuario, horario, servico) {
    agendamentoAtendimentoId = agendamentoId;
    
    // Preencher os campos do modal
    document.getElementById('nomeUsuarioModal').textContent = nomeUsuario;
    document.getElementById('dataHoraAtendimento').value = `Hoje, ${horario}`;
    document.getElementById('servicoAtendimento').value = servico;
    document.getElementById('observacoesAtendimento').value = '';
    
    // Abrir o modal
    const modal = new bootstrap.Modal(document.getElementById('modalAtendimento'));
    modal.show();
}

// Função para confirmar o atendimento
async function confirmarAtendimento() {
    const observacoes = document.getElementById('observacoesAtendimento').value.trim();
    
    if (!observacoes) {
        alert('Por favor, digite as observações do atendimento.');
        return;
    }
    
    try {
        const token = localStorage.getItem('token');
        
        const response = await fetch(`/api/agendamentos/${agendamentoAtendimentoId}/atender`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                observacoes: observacoes
            })
        });
        
        if (!response.ok) {
            throw new Error('Erro ao confirmar atendimento');
        }
        
        // Fechar o modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('modalAtendimento'));
        modal.hide();
        
        // Mostrar mensagem de sucesso
        mostrarSucesso('Atendimento confirmado com sucesso!');
        
        // Recarregar a agenda e os dados do dashboard
        await carregarAgendaHoje();
        await carregarDadosDashboard();
        
    } catch (error) {
        console.error('Erro ao confirmar atendimento:', error);
        mostrarErro('Erro ao confirmar atendimento. Tente novamente.');
    }
}
