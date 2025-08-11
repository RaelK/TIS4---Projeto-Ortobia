function fazerLogout() {

  // Confirmar logout
  if (confirm('Deseja realmente sair?')) {
    // Limpar localStorage
    localStorage.clear();

    // Limpar sessionStorage
    sessionStorage.clear();
    
    // Redirecionar para index.html
    window.location.href = 'index.html';
  }
}

// Função para carregar header dinamicamente
function carregarHeader() {
  const role = sessionStorage.getItem('role');
  let headerFile = 'header.html';
  if (role === 'USER') {
    headerFile = 'headerUser.html';
  } else if (role === 'DENTISTA') {
    headerFile = 'headerDentista.html';
  }

  fetch(headerFile)
    .then(res => res.text())
    .then(data => {
      document.getElementById('incluir-header').innerHTML = data;
      // Inicializar o logout após carregar o header
      initLogout();
    })
    .catch(error => {
      console.error('Erro ao carregar header:', error);
    });
}

// Função para inicializar o logout após carregamento do header
function initLogout() {
  const btnLogout = document.getElementById('btnLogout');
  if (btnLogout) {
    btnLogout.addEventListener('mouseenter', function () {
      this.style.backgroundColor = '#c0392b';
    });

    btnLogout.addEventListener('mouseleave', function () {
      this.style.backgroundColor = '#e74c3c';
    });
  }
}

// Adicionar hover effect ao botão quando o DOM estiver carregado
document.addEventListener('DOMContentLoaded', function () {
  // Aguardar um pouco para garantir que o header foi carregado
  setTimeout(function () {
    initLogout();
  }, 500);

  // Se existe um elemento incluir-header, carregar automaticamente
  if (document.getElementById('incluir-header')) {
    carregarHeader();
  }
});
