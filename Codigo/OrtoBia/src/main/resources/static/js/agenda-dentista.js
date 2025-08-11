document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    let consultaAtual = null;

    // Inicializa o calendário
    const calendarEl = document.getElementById('calendario');
    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'timeGridWeek',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        locale: 'pt-br',
        slotMinTime: '09:00:00',
        slotMaxTime: '18:00:00',
        allDaySlot: false,
        slotDuration: '00:30:00',
        eventClick: function(info) {
            const agendamentoId = info.event.id;
            carregarDetalhesConsulta(agendamentoId);
        },
        events: function(info, successCallback, failureCallback) {
            fetch(`/api/agendamentos/dentista?inicio=${info.startStr}&fim=${info.endStr}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
            .then(response => response.json())
            .then(data => {
                const events = data.map(agendamento => ({
                    id: agendamento.id,
                    title: `${agendamento.usuario.nome} - ${agendamento.procedimento}`,
                    start: agendamento.dataHora,
                    backgroundColor: agendamento.atendida ? '#28a745' : '#007bff',
                    borderColor: agendamento.atendida ? '#28a745' : '#007bff'
                }));
                successCallback(events);
            })
            .catch(error => {
                console.error('Erro ao carregar agendamentos:', error);
                failureCallback(error);
            });
        }
    });
    calendar.render();

    // Carrega as consultas do dia
    function carregarConsultasDia() {
        const hoje = new Date();
        const inicio = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
        const fim = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate(), 23, 59, 59);

        fetch(`/api/agendamentos/dentista?inicio=${inicio.toISOString()}&fim=${fim.toISOString()}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(response => response.json())
        .then(data => {
            const container = document.getElementById('consultas-dia');
            container.innerHTML = '';

            if (data.length === 0) {
                container.innerHTML = '<p class="text-center">Nenhuma consulta agendada para hoje.</p>';
                return;
            }

            data.forEach(agendamento => {
                const item = document.createElement('a');
                item.href = '#';
                item.className = 'list-group-item list-group-item-action';
                item.innerHTML = `
                    <div class="d-flex w-100 justify-content-between">
                        <h5 class="mb-1">${agendamento.usuario.nome}</h5>
                        <small>${new Date(agendamento.dataHora).toLocaleTimeString()}</small>
                    </div>
                    <p class="mb-1">${agendamento.motivo}</p>
                    <small>${agendamento.atendida ? 'Atendida' : 'Pendente'}</small>
                `;
                item.addEventListener('click', (e) => {
                    e.preventDefault();
                    carregarDetalhesConsulta(agendamento.id);
                });
                container.appendChild(item);
            });
        })
        .catch(error => console.error('Erro ao carregar consultas:', error));
    }

    // Carrega os detalhes da consulta
    function carregarDetalhesConsulta(agendamentoId) {
        fetch(`/api/agendamentos/${agendamentoId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(response => response.json())
        .then(agendamento => {
            consultaAtual = agendamento;
            document.getElementById('paciente-nome').textContent = agendamento.usuario.nome;
            document.getElementById('dentista-nome').textContent = agendamento.dentista.nome;
            document.getElementById('servico-nome').textContent = agendamento.servico.nome;
            document.getElementById('consulta-data').textContent = new Date(agendamento.dataHora).toLocaleString();
            document.getElementById('consulta-motivo').textContent = agendamento.motivo;
            document.getElementById('consulta-status').textContent = agendamento.atendida ? 'Atendida' : 'Pendente';
            
            // Ajusta visibilidade dos botões
            document.getElementById('btnDesmarcar').style.display = agendamento.atendida ? 'none' : 'block';
            document.getElementById('btnFinalizar').style.display = agendamento.atendida ? 'none' : 'block';
            
            new bootstrap.Modal(document.getElementById('modalConsulta')).show();
        })
        .catch(error => console.error('Erro ao carregar detalhes:', error));
    }

    // Desmarca a consulta
    document.getElementById('btnDesmarcar').addEventListener('click', function() {
        if (!consultaAtual || !confirm('Tem certeza que deseja desmarcar esta consulta?')) {
            return;
        }

        fetch(`/api/agendamentos/${consultaAtual.id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Erro ao desmarcar consulta');
            }
            bootstrap.Modal.getInstance(document.getElementById('modalConsulta')).hide();
            calendar.refetchEvents();
            carregarConsultasDia();
            alert('Consulta desmarcada com sucesso!');
        })
        .catch(error => {
            console.error('Erro:', error);
            alert('Erro ao desmarcar consulta. Por favor, tente novamente.');
        });
    });

    // Finaliza a consulta
    document.getElementById('btnFinalizar').addEventListener('click', function() {
        if (!consultaAtual || !confirm('Tem certeza que deseja finalizar esta consulta?')) {
            return;
        }

        fetch(`/api/agendamentos/${consultaAtual.id}/finalizar`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Erro ao finalizar consulta');
            }
            bootstrap.Modal.getInstance(document.getElementById('modalConsulta')).hide();
            calendar.refetchEvents();
            carregarConsultasDia();
            alert('Consulta finalizada com sucesso!');
        })
        .catch(error => {
            console.error('Erro:', error);
            alert('Erro ao finalizar consulta. Por favor, tente novamente.');
        });
    });

    // Carrega as consultas do dia ao iniciar
    carregarConsultasDia();
}); 