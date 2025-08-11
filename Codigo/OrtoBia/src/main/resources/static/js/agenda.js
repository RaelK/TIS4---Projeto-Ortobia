document.addEventListener('DOMContentLoaded', function () {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

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
        selectable: true,
        select: function (info) {
            const dataHora = info.start;
            if (dataHora < new Date()) {
                alert('Não é possível agendar consultas em datas passadas');
                return;
            }
            if (dataHora.getDay() === 0) {
                alert('Não é possível agendar consultas aos domingos');
                return;
            }
            if (dataHora.getDay() === 6 && dataHora.getHours() > 12) {
                alert('Aos sábados, as consultas só podem ser agendadas até 12:30');
                return;
            }

            const pad = n => String(n).padStart(2, '0');
            document.getElementById('dataHora').value =
                dataHora.getFullYear() + '-' +
                pad(dataHora.getMonth() + 1) + '-' +
                pad(dataHora.getDate()) + 'T' +
                pad(dataHora.getHours()) + ':' +
                pad(dataHora.getMinutes());

            carregarDentistas();
            carregarServicos();
            new bootstrap.Modal(document.getElementById('modalAgendamento')).show();
        },
        events: function (info, successCallback, failureCallback) {
            fetch(`http://localhost:8080/api/agendamentos/disponiveis?inicio=${info.startStr}&fim=${info.endStr}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
                .then(response => response.json())
                .then(data => {
                    const events = data.map(agendamento => ({
                        id: agendamento.id,
                        title: agendamento.disponivel ? 'Disponível' : 'Ocupado',
                        start: agendamento.dataHora,
                        backgroundColor: agendamento.disponivel ? '#28a745' : '#dc3545',
                        borderColor: agendamento.disponivel ? '#28a745' : '#dc3545'
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

    // Carrega os dentistas no select
    function carregarDentistas() {
        fetch('http://localhost:8080/usuarios/dentistas', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
            .then(response => response.json())
            .then(dentistas => {
                const select = document.getElementById('dentista');
                select.innerHTML = '<option value="">Selecione um dentista</option>';
                dentistas.forEach(dentista => {
                    select.innerHTML += `<option value="${dentista.id}">${dentista.nome}</option>`;
                });
            })
            .catch(error => console.error('Erro ao carregar dentistas:', error));
    }

    // Carrega os serviços no select
    function carregarServicos() {
        fetch('http://localhost:8080/api/services', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
            .then(response => response.json())
            .then(servicos => {
                const select = document.getElementById('servico');
                select.innerHTML = '<option value="">Selecione um serviço</option>';
                servicos.forEach(servico => {
                    select.innerHTML += `<option value="${servico.id}">${servico.tipo}</option>`;
                });
            })
            .catch(error => console.error('Erro ao carregar serviços:', error));
    }


    // Confirma o agendamento
    document.getElementById('btnConfirmarAgendamento').addEventListener('click', function () {
        // Pega o valor do input (já está no formato yyyy-MM-ddTHH:mm)
        const dataHoraInput = document.getElementById('dataHora').value; // Ex: '2025-06-20T12:00'
        // Garante que terá os segundos
        const dataHora = dataHoraInput.length === 16 ? dataHoraInput + ':00' : dataHoraInput.split('.')[0];

        const dentistaId = document.getElementById('dentista').value;
        const motivo = document.getElementById('motivo').value;
        const loggedId = sessionStorage.getItem('loggedId');
        const servicoId = document.getElementById('servico').value;
        const servicoSelect = document.getElementById('servico');
        const procedimento = servicoSelect.options[servicoSelect.selectedIndex].text;

        if (!dentistaId || !motivo || !servicoId) {
            alert('Por favor, preencha todos os campos');
            return;
        }

        // Verificar se o token existe
        if (!token) {
            alert('Token de autenticação não encontrado. Redirecionando para login...');
            window.location.href = 'login.html';
            return;
        }

        console.log('Token:', token);
        console.log({
            dataHora: dataHora,
            dentistaId: parseInt(dentistaId),
            servicoId: parseInt(servicoId),
            procedimento: procedimento,
            motivo: motivo,
            usuarioId: parseInt(loggedId)
        });

        fetch('http://localhost:8080/api/agendamentos', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                dataHora: dataHora, // Ex: '2025-06-20T12:00:00'
                dentistaId: parseInt(dentistaId),
                servicoId: parseInt(servicoId),
                procedimento: procedimento,
                motivo: motivo,
                usuarioId: parseInt(loggedId)
            })
        })
            .then(response => {
                console.log('Response status:', response.status);
                console.log('Response headers:', response.headers);
                
                if (!response.ok) {
                    if (response.status === 401 || response.status === 403) {
                        // Token expirado ou inválido
                        localStorage.removeItem('token');
                        sessionStorage.removeItem('loggedId');
                        alert('Sua sessão expirou. Redirecionando para login...');
                        window.location.href = 'login.html';
                        return;
                    }
                    return response.text().then(text => { 
                        console.log('Error response text:', text);
                        throw new Error(text || 'Erro ao agendar consulta'); 
                    });
                }
                return response.json();
            })
            .then(data => {
                alert('Consulta agendada com sucesso!');
                bootstrap.Modal.getInstance(document.getElementById('modalAgendamento')).hide();
                calendar.refetchEvents();
            })
            .catch(error => {
                console.error('Erro:', error);
                alert('Erro ao agendar consulta. Por favor, tente novamente.');
            });
    });
}); 