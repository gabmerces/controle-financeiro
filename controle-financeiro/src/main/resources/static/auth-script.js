// Elementos dos Cards
const loginBox = document.getElementById('loginBox');
const registerBox = document.getElementById('registerBox');
const recoverBox = document.getElementById('recoverBox');

// Elementos de Alternância
const toRegister = document.getElementById('toRegister');
const toLogin = document.getElementById('toLogin');
const toRecover = document.getElementById('toRecover');
const backToLoginFromRecover = document.getElementById('backToLoginFromRecover');

// Se já existir um token salvo, o usuário já está logado: pula direto pro dashboard
if (localStorage.getItem('token')) {
    window.location.href = 'financas.html';
}

// Liga os campos de senha ao checklist visual de requisitos + botão de mostrar/ocultar
// (ativarCampoSenha vem de password-rules.js)
ativarCampoSenha('loginSenha', { exigirRegras: false, mostrarChecklist: false }); // login não valida regra nem mostra checklist, só o toggle
const senhaCadastro = ativarCampoSenha('regSenha', { exigirRegras: true });

// LÓGICA VISUAL: ALTERNAR ENTRE OS CARDS
if (toRegister) {
    toRegister.addEventListener('click', (e) => {
        e.preventDefault();
        loginBox.classList.add('hidden');
        recoverBox.classList.add('hidden');
        registerBox.classList.remove('hidden');
    });
}

if (toLogin) {
    toLogin.addEventListener('click', (e) => {
        e.preventDefault();
        registerBox.classList.add('hidden');
        recoverBox.classList.add('hidden');
        loginBox.classList.remove('hidden');
    });
}

if (toRecover) {
    toRecover.addEventListener('click', (e) => {
        e.preventDefault();
        loginBox.classList.add('hidden');
        registerBox.classList.add('hidden');
        recoverBox.classList.remove('hidden');
    });
}

if (backToLoginFromRecover) {
    backToLoginFromRecover.addEventListener('click', (e) => {
        e.preventDefault();
        recoverBox.classList.add('hidden');
        registerBox.classList.add('hidden');
        loginBox.classList.remove('hidden');
    });
}

// Elementos do Modal de Notificação
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

// Extrai a mensagem de erro do corpo da resposta, seja ela texto puro ou JSON
// (o back-end agora pode responder tanto {"mensagem": "..."} quanto texto simples)
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

// =======================================================
// SUBMISSÃO DOS FORMULÁRIOS (SPRING API)
// =======================================================

// 1. Formulário de CADASTRO
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const nome = document.getElementById('regNome').value;
        const email = document.getElementById('regEmail').value;
        const senha = document.getElementById('regSenha').value;

        if (!senhaCadastro.valida()) {
            mostrarAviso('Senha Fraca 🔒', 'Sua senha ainda não atende a todos os requisitos listados abaixo do campo.', 'erro');
            return;
        }

        try {
            const resposta = await apiFetch('/api/usuarios/cadastrar', {
                method: 'POST',
                body: JSON.stringify({ nome, email, senha })
            });

            if (resposta.ok) {
                mostrarAviso('Sucesso! 🎉', 'Cadastro realizado com sucesso! Use seus dados para entrar.', 'sucesso');
                acaoAposFecharModal = () => {
                    registerForm.reset();
                    document.getElementById('regSenha').dispatchEvent(new Event('input'));
                    if (toLogin) toLogin.click();
                };
            } else {
                const erroTexto = await extrairMensagemDeErro(resposta, 'Não foi possível concluir o cadastro.');
                mostrarAviso('Ops! Erro no Cadastro ❌', erroTexto, 'erro');
            }
        } catch (erro) {
            mostrarAviso('Erro de Servidor 📡', 'Não foi possível conectar ao servidor do Back-end.', 'erro');
        }
    });
}

// 2. Formulário de LOGIN
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('loginEmail').value;
        const senha = document.getElementById('loginSenha').value;

        try {
            const resposta = await apiFetch('/api/usuarios/login', {
                method: 'POST',
                body: JSON.stringify({ email, senha })
            });

            if (resposta.ok) {
                const dados = await resposta.json();
                // Guardamos o token JWT (usado para autenticar as próximas chamadas)
                // e os dados do usuário só para exibição na tela — nunca para autorização.
                localStorage.setItem('token', dados.token);
                localStorage.setItem('usuarioNome', dados.usuario.nome);
                localStorage.setItem('usuarioEmail', dados.usuario.email);
                window.location.href = 'financas.html';
            } else {
                const erroTexto = await extrairMensagemDeErro(resposta, 'E-mail ou senha incorretos!');
                mostrarAviso('Acesso Recusado 🔒', erroTexto, 'erro');
            }
        } catch (erro) {
            mostrarAviso('Erro de Servidor 📡', 'Não foi possível conectar ao servidor do Back-end.', 'erro');
        }
    });
}

// 3. Formulário de RECUPERAÇÃO DE SENHA (etapa 1: solicitar o link por e-mail)
const recoverForm = document.getElementById('recoverForm');
if (recoverForm) {
    recoverForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('recEmail').value;

        try {
            const resposta = await apiFetch('/api/usuarios/solicitar-recuperacao-senha', {
                method: 'POST',
                body: JSON.stringify({ email })
            });

            if (resposta.ok) {
                // A mensagem é sempre a mesma, exista ou não o e-mail cadastrado —
                // isso é proposital, para não revelar quais e-mails têm conta no sistema.
                mostrarAviso(
                    'Verifique seu e-mail 📬',
                    'Se este e-mail estiver cadastrado, enviamos um link para você redefinir a senha. Ele vale por 30 minutos.',
                    'sucesso'
                );
                acaoAposFecharModal = () => {
                    recoverForm.reset();
                    if (backToLoginFromRecover) backToLoginFromRecover.click();
                };
            } else {
                const erroTexto = await extrairMensagemDeErro(resposta, 'Não foi possível enviar o link de redefinição.');
                mostrarAviso('Não foi possível enviar ❌', erroTexto, 'erro');
            }
        } catch (erro) {
            mostrarAviso('Erro de Servidor 📡', 'Não foi possível conectar ao servidor do Back-end.', 'erro');
        }
    });
}
