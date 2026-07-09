// Elementos do modal de aviso (mesmo padrão usado no restante do sistema)
const notificationModal = document.getElementById('notificationModal');
const modalTitle = document.getElementById('modalTitle');
const modalMessage = document.getElementById('modalMessage');
const modalCloseBtn = document.getElementById('modalCloseBtn');

let acaoAposFecharModal = null;

function mostrarAviso(titulo, mensagem, tipo) {
    modalTitle.textContent = titulo;
    modalMessage.textContent = mensagem;
    modalTitle.className = '';
    modalTitle.classList.add(tipo);
    notificationModal.classList.remove('hidden');
}

if (modalCloseBtn) {
    modalCloseBtn.addEventListener('click', () => {
        notificationModal.classList.add('hidden');
        if (typeof acaoAposFecharModal === 'function') {
            acaoAposFecharModal();
            acaoAposFecharModal = null;
        }
    });
}

async function extrairMensagemDeErro(resposta, mensagemPadrao) {
    try {
        const dados = await resposta.clone().json();
        if (dados && dados.mensagem) return dados.mensagem;
        if (dados && dados.erros) return Object.values(dados.erros).join(' ');
    } catch (e) {
        // não era JSON, tenta como texto
    }
    try {
        const texto = await resposta.text();
        if (texto) return texto;
    } catch (e) { /* ignora */ }
    return mensagemPadrao;
}

// Pega o token da URL (?token=...), enviado no link do e-mail
const params = new URLSearchParams(window.location.search);
const token = params.get('token');

const redefinirBox = document.getElementById('redefinirBox');
const linkInvalidoBox = document.getElementById('linkInvalidoBox');

if (!token) {
    redefinirBox.classList.add('hidden');
    linkInvalidoBox.classList.remove('hidden');
} else {
    const senhaNova = ativarCampoSenha('novaSenha', { exigirRegras: true });

    const redefinirForm = document.getElementById('redefinirForm');
    redefinirForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const novaSenha = document.getElementById('novaSenha').value;

        if (!senhaNova.valida()) {
            mostrarAviso('Senha Fraca 🔒', 'Sua nova senha ainda não atende a todos os requisitos listados abaixo do campo.', 'erro');
            return;
        }

        try {
            const resposta = await apiFetch('/api/usuarios/redefinir-senha', {
                method: 'POST',
                body: JSON.stringify({ token, novaSenha })
            });

            if (resposta.ok) {
                mostrarAviso('Senha Alterada! 🔑', 'Sua senha foi redefinida com sucesso. Faça login com a nova senha.', 'sucesso');
                acaoAposFecharModal = () => {
                    window.location.href = 'index.html';
                };
            } else {
                const erroTexto = await extrairMensagemDeErro(resposta, 'Não foi possível redefinir a senha. O link pode ter expirado.');
                mostrarAviso('Não foi possível redefinir ❌', erroTexto, 'erro');
            }
        } catch (erro) {
            mostrarAviso('Erro de Servidor 📡', 'Não foi possível conectar ao servidor do Back-end.', 'erro');
        }
    });
}
