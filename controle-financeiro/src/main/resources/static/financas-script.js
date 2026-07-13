let usuarioNome = localStorage.getItem('usuarioNome');

const welcomeText = document.getElementById('welcomeText');
const logoutBtn = document.getElementById('logoutBtn');
const transactionForm = document.getElementById('transactionForm');
const tableBody = document.getElementById('transactionTableBody');
const dashboardTitle = document.getElementById('dashboardTitle');

const formTitle = document.getElementById('formTitle');
const transactionIdInput = document.getElementById('transactionId');
const descInput = document.getElementById('desc');
const valInput = document.getElementById('val');
const typeInput = document.getElementById('type');
const dateInput = document.getElementById('date');
const btnSubmit = document.getElementById('btnSubmit');
const btnCancel = document.getElementById('btnCancel');

const filterMonth = document.getElementById('filterMonth');
const filterYear = document.getElementById('filterYear');

const totalEntriesEl = document.getElementById('totalEntries');
const totalExpensesEl = document.getElementById('totalExpenses');
const totalBalanceEl = document.getElementById('totalBalance');

const customModal = document.getElementById('customModal');
const modalCancelBtn = document.getElementById('modalCancelBtn');
const modalConfirmBtn = document.getElementById('modalConfirmBtn');

const openProfileBtn = document.getElementById('openProfileBtn');
const profileModal = document.getElementById('profileModal');
const profileCancelBtn = document.getElementById('profileCancelBtn');
const profileForm = document.getElementById('profileForm');
const profileNome = document.getElementById('profileNome');
const profileEmail = document.getElementById('profileEmail');
const profileSenha = document.getElementById('profileSenha');

const notificationModal = document.getElementById('notificationModal');
const modalTitle = document.getElementById('modalTitle');
const modalMessage = document.getElementById('modalMessage');
const modalCloseBtn = document.getElementById('modalCloseBtn');

// Campo de senha do perfil: opcional (em branco = mantém a senha atual),
// mas se preenchido precisa atender aos requisitos exibidos no checklist.
const senhaPerfil = ativarCampoSenha('profileSenha', { exigirRegras: true, opcional: true });

let todasTransacoes = [];
let idParaDeletar = null;
let chartInstance = null;

const nomesMeses = [
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
];

// Sem token, não tem sessão válida: manda pro login.
// (O back-end também rejeita qualquer chamada sem token, isso aqui só evita
// mostrar a tela vazia antes do redirecionamento.)
if (!localStorage.getItem('token')) {
    window.location.href = 'index.html';
} else {
    welcomeText.textContent = `Bem-vindo(a), ${usuarioNome || ''}! `;
    const hoje = new Date();
    filterMonth.value = hoje.getMonth();
    filterYear.value = hoje.getFullYear();
}

logoutBtn.addEventListener('click', () => {
    localStorage.clear();
    window.location.href = 'index.html';
});

function mostrarAviso(titulo, mensagem, tipo) {
    modalTitle.textContent = titulo;
    modalMessage.textContent = mensagem;
    modalTitle.className = '';
    if (tipo) modalTitle.classList.add(tipo);
    notificationModal.classList.remove('hidden');
}

if (modalCloseBtn) {
    modalCloseBtn.addEventListener('click', () => {
        notificationModal.classList.add('hidden');
    });
}

async function extrairMensagemDeErro(resposta, mensagemPadrao) {
    try {
        const dados = await resposta.clone().json();
        if (dados && dados.mensagem) return dados.mensagem;
        if (dados && dados.erros) return Object.values(dados.erros).join(' ');
    } catch (e) { /* não era JSON */ }
    return mensagemPadrao;
}

async function carregarTransacoes() {
    try {
        // Não precisa mais informar o ID do usuário: o back-end descobre quem é
        // através do token JWT enviado pelo apiFetch.
        const resposta = await apiFetch('/api/transacoes');
        if (resposta.ok) {
            todasTransacoes = await resposta.json();
            filtrarEAplicarNaTela();
        }
    } catch (erro) {
        console.error('Erro ao buscar dados:', erro);
    }
}

function filtrarEAplicarNaTela() {
    const mesSelecionado = parseInt(filterMonth.value);
    const anoSelecionado = parseInt(filterYear.value);

    if (dashboardTitle) {
        dashboardTitle.textContent = `Movimentações de ${nomesMeses[mesSelecionado]} de ${anoSelecionado}`;
    }

    const transacoesDoMes = todasTransacoes
        .filter(t => {
            if (!t.data) return false;
            const partes = t.data.split('-');
            return (parseInt(partes[1]) - 1) === mesSelecionado && parseInt(partes[0]) === anoSelecionado;
        })
        .sort((a, b) => a.data.localeCompare(b.data));

    let entradasDoMes = 0;
    let saidasDoMes = 0;

    transacoesDoMes.forEach(t => {
        const tipo = (t.tipo || "").toUpperCase();
        if (tipo === 'RECEITA') entradasDoMes += t.valor;
        else if (tipo === 'DESPESA') saidasDoMes += t.valor;
    });

    let saldoAcumuladoAteOMes = 0;
    todasTransacoes.forEach(t => {
        if (!t.data) return;
        const partes = t.data.split('-');
        const anoTransacao = parseInt(partes[0]);
        const mesTransacao = parseInt(partes[1]) - 1;

        if (anoTransacao < anoSelecionado || (anoTransacao === anoSelecionado && mesTransacao <= mesSelecionado)) {
            const tipo = (t.tipo || "").toUpperCase();
            if (tipo === 'RECEITA') saldoAcumuladoAteOMes += t.valor;
            else if (tipo === 'DESPESA') saldoAcumuladoAteOMes -= t.valor;
        }
    });

    renderizarTabelasECards(transacoesDoMes, entradasDoMes, saidasDoMes, saldoAcumuladoAteOMes);
    atualizarGrafico(entradasDoMes, saidasDoMes, saldoAcumuladoAteOMes);
}

function renderizarTabelasECards(transacoes, entradas, saidas, saldoTotal) {
    tableBody.innerHTML = '';

    transacoes.forEach(t => {
        const valorFormatado = t.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
        const dataFormatada = new Date(t.data + 'T00:00:00').toLocaleDateString('pt-BR');
        const tipo = (t.tipo || "").toUpperCase();

        let classeValor = tipo === 'RECEITA' ? 'text-success' : 'text-danger';
        let tipoTexto = tipo === 'RECEITA' ? 'Entrada' : 'Saída';

        const line = document.createElement('tr');
        line.innerHTML = `
            <td>${t.descricao}</td>
            <td class="${classeValor}">${valorFormatado}</td>
            <td>${tipoTexto}</td>
            <td>${dataFormatada}</td>
            <td class="actions-cell">
                <button class="btn-edit" onclick="prepararEdicao(${t.id})">Editar</button>
                <button class="btn-delete" onclick="abrirModalExclusao(${t.id})">Excluir</button>
            </td>
        `;
        tableBody.appendChild(line);
    });

    totalEntriesEl.textContent = entradas.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    totalExpensesEl.textContent = saidas.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    totalBalanceEl.textContent = saldoTotal.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

// FUNÇÃO QUE CONSTRÓI O GRÁFICO NA TELA
function atualizarGrafico(entradas, saidas, saldoTotal) {
    const canvasElement = document.getElementById('monthlyChart');
    if (!canvasElement) return;

    const ctx = canvasElement.getContext('2d');
    if (chartInstance) {
        chartInstance.destroy();
    }

    const temDados = entradas > 0 || saidas > 0;
    const dataValues = temDados ? [entradas, saidas] : [1];
    const bgColors = temDados ? ['#93375a', '#b56a89'] : ['#e9ddd0'];
    const labelLabels = temDados ? ['Entradas (R$)', 'Saídas (R$)'] : ['Nenhuma transação registrada'];

    chartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labelLabels,
            datasets: [{
                data: dataValues,
                backgroundColor: bgColors,
                borderWidth: 3,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            cutout: '74%'
        }
    });

    // Texto central do rosco (saldo total)
    const centerLabel = document.getElementById('chartCenterValue');
    if (centerLabel) {
        centerLabel.textContent = saldoTotal.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    }

    // Legenda personalizada — mesmos dados reais, formatados como lista
    const legendEl = document.getElementById('chartLegend');
    if (legendEl) {
        const total = entradas + saidas;
        const pctEntradas = total > 0 ? ((entradas / total) * 100).toFixed(1) : '0.0';
        const pctSaidas = total > 0 ? ((saidas / total) * 100).toFixed(1) : '0.0';
        const fmt = v => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

        legendEl.innerHTML = `
            <div class="legend-row">
                <span class="legend-dot" style="background:#93375a"></span>
                <span class="legend-label">Entradas</span>
                <span class="legend-values"><strong>${fmt(entradas)}</strong><small>${pctEntradas}%</small></span>
            </div>
            <div class="legend-row">
                <span class="legend-dot" style="background:#b56a89"></span>
                <span class="legend-label">Saídas</span>
                <span class="legend-values"><strong>${fmt(saidas)}</strong><small>${pctSaidas}%</small></span>
            </div>
            <div class="legend-row">
                <span class="legend-dot" style="background:#d8c8b6"></span>
                <span class="legend-label">Saldo</span>
                <span class="legend-values"><strong>${fmt(saldoTotal)}</strong><small>acumulado</small></span>
            </div>
        `;
    }
}

filterMonth.addEventListener('change', filtrarEAplicarNaTela);
filterYear.addEventListener('change', filtrarEAplicarNaTela);

transactionForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = transactionIdInput.value;
    // Repare que não mandamos mais nenhum campo "usuario": o back-end associa
    // a transação ao dono do token JWT automaticamente.
    const dadosTransacao = {
        descricao: descInput.value,
        valor: parseFloat(valInput.value),
        tipo: typeInput.value,
        data: dateInput.value
    };

    const caminho = id ? `/api/transacoes/${id}` : '/api/transacoes';
    const method = id ? 'PUT' : 'POST';

    try {
        const resposta = await apiFetch(caminho, {
            method: method,
            body: JSON.stringify(dadosTransacao)
        });
        if (resposta.ok) {
            cancelarEdicao();
            carregarTransacoes();
        } else {
            const erroTexto = await extrairMensagemDeErro(resposta, 'Não foi possível salvar a transação.');
            mostrarAviso('Erro ❌', erroTexto, 'erro');
        }
    } catch (erro) {
        mostrarAviso('Erro 📡', 'Conexão perdida com o servidor.', 'erro');
    }
});

window.prepararEdicao = function(id) {
    const transacao = todasTransacoes.find(t => t.id === id);
    if (!transacao) return;

    transactionIdInput.value = transacao.id;
    descInput.value = transacao.descricao;
    valInput.value = transacao.valor;
    typeInput.value = transacao.tipo;
    dateInput.value = transacao.data;

    formTitle.textContent = 'Editar Transação';
    btnSubmit.textContent = 'Salvar Alterações';
    btnCancel.classList.remove('hidden');
};

function cancelarEdicao() {
    transactionForm.reset();
    transactionIdInput.value = '';
    formTitle.textContent = 'Nova Transação';
    btnSubmit.textContent = 'Adicionar';
    btnCancel.classList.add('hidden');
}
btnCancel.addEventListener('click', cancelarEdicao);

window.abrirModalExclusao = function(id) {
    idParaDeletar = id;
    if (customModal) customModal.classList.remove('hidden');
};

function fecharModalExclusao() {
    idParaDeletar = null;
    if (customModal) customModal.classList.add('hidden');
}
if (modalCancelBtn) modalCancelBtn.addEventListener('click', fecharModalExclusao);

if (modalConfirmBtn) {
    modalConfirmBtn.addEventListener('click', async () => {
        if (!idParaDeletar) return;
        try {
            const resposta = await apiFetch(`/api/transacoes/${idParaDeletar}`, { method: 'DELETE' });
            if (resposta.ok || resposta.status === 204) {
                fecharModalExclusao();
                carregarTransacoes();
            }
        } catch (erro) {
            console.error(erro);
        }
    });
}

// Perfil Modal ações
if (openProfileBtn) {
    openProfileBtn.addEventListener('click', () => {
        profileNome.value = localStorage.getItem('usuarioNome') || '';
        profileEmail.value = localStorage.getItem('usuarioEmail') || '';
        profileSenha.value = '';
        // Garante que o campo volte a ficar oculto e o checklist escondido
        // toda vez que o modal é reaberto.
        const wrapper = profileSenha.closest('.password-field');
        if (wrapper) {
            wrapper.classList.remove('is-visible');
            profileSenha.type = 'password';
        }
        const checklist = document.getElementById('profileSenhaChecklist');
        if (checklist) checklist.classList.add('hidden');
        profileModal.classList.remove('hidden');
    });
}
if (profileCancelBtn) profileCancelBtn.addEventListener('click', () => profileModal.classList.add('hidden'));

if (profileForm) {
    profileForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!senhaPerfil.valida()) {
            mostrarAviso('Senha Fraca 🔒', 'Sua nova senha ainda não atende a todos os requisitos listados abaixo do campo.', 'erro');
            return;
        }

        const dadosPerfil = {
            nome: profileNome.value,
            email: profileEmail.value,
            senha: profileSenha.value || null
        };

        try {
            // O back-end identifica o usuário pelo token JWT, não é mais preciso
            // (nem possível) informar o ID na URL.
            const resposta = await apiFetch('/api/usuarios/atualizar-perfil', {
                method: 'PUT',
                body: JSON.stringify(dadosPerfil)
            });

            if (resposta.ok) {
                const usuarioAtualizado = await resposta.json();
                localStorage.setItem('usuarioNome', usuarioAtualizado.nome);
                localStorage.setItem('usuarioEmail', usuarioAtualizado.email);
                welcomeText.textContent = `Bem-vindo(a), ${usuarioAtualizado.nome}! `;
                profileModal.classList.add('hidden');
                mostrarAviso('Perfil Atualizado! ✅', 'Seus dados foram salvos com sucesso.', 'sucesso');
            } else {
                const erroTexto = await extrairMensagemDeErro(resposta, 'Não foi possível atualizar o perfil.');
                mostrarAviso('Erro ❌', erroTexto, 'erro');
            }
        } catch (erro) {
            mostrarAviso('Erro 📡', 'Conexão perdida com o servidor.', 'erro');
        }
    });
}

carregarTransacoes();
