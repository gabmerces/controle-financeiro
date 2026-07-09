// =======================================================
// Regras de senha, compartilhadas entre cadastro, redefinição de senha
// e edição de perfil. Mantidas em sincronia com a validação do back-end
// (@Pattern em UsuarioCadastroDTO, RedefinirSenhaDTO e UsuarioAtualizarDTO).
// =======================================================

const REGRAS_SENHA = [
    { chave: 'tamanho', texto: 'Pelo menos 8 caracteres', teste: (s) => s.length >= 8 },
    { chave: 'maiuscula', texto: 'Uma letra maiúscula (A-Z)', teste: (s) => /[A-Z]/.test(s) },
    { chave: 'minuscula', texto: 'Uma letra minúscula (a-z)', teste: (s) => /[a-z]/.test(s) },
    { chave: 'numero', texto: 'Um número (0-9)', teste: (s) => /\d/.test(s) },
];

function senhaAtendeTodasAsRegras(senha) {
    return REGRAS_SENHA.every(regra => regra.teste(senha || ''));
}

// Cria o <ul> com o checklist de requisitos e insere logo após o elemento de referência
function criarChecklistSenha(idContainer) {
    const ul = document.createElement('ul');
    ul.className = 'password-requirements';
    ul.id = idContainer;

    REGRAS_SENHA.forEach(regra => {
        const li = document.createElement('li');
        li.dataset.regra = regra.chave;
        li.innerHTML = `<span class="req-icon"></span><span>${regra.texto}</span>`;
        ul.appendChild(li);
    });

    return ul;
}

// Atualiza visualmente quais requisitos já foram atendidos
function atualizarChecklistSenha(idContainer, senha) {
    const ul = document.getElementById(idContainer);
    if (!ul) return;

    REGRAS_SENHA.forEach(regra => {
        const li = ul.querySelector(`li[data-regra="${regra.chave}"]`);
        if (!li) return;
        li.classList.toggle('valid', regra.teste(senha || ''));
    });
}

// Liga um input de senha a: (1) checklist visual e (2) botão de mostrar/ocultar
// exigirRegras: se true, o checklist é obrigatório (cadastro/recuperação);
// se false, o checklist só aparece quando o usuário começa a digitar (edição de perfil,
// onde o campo pode ficar em branco para manter a senha atual).
// mostrarChecklist: se false, nenhum checklist é criado (ex.: campo de senha do login,
// que é uma senha já existente, não uma senha nova a ser validada).
function ativarCampoSenha(inputId, { exigirRegras = true, opcional = false, mostrarChecklist = true } = {}) {
    const input = document.getElementById(inputId);
    if (!input) return null;

    const wrapper = input.closest('.password-field');
    const idChecklist = `${inputId}Checklist`;

    let checklist = null;
    if (wrapper) {
        if (mostrarChecklist) {
            checklist = criarChecklistSenha(idChecklist);
            if (opcional) checklist.classList.add('hidden');
            wrapper.insertAdjacentElement('afterend', checklist);
        }

        // Botão de mostrar/ocultar senha
        const toggleBtn = wrapper.querySelector('.password-toggle');
        if (toggleBtn) {
            toggleBtn.addEventListener('click', () => {
                const mostrando = wrapper.classList.toggle('is-visible');
                input.type = mostrando ? 'text' : 'password';
                toggleBtn.setAttribute('aria-label', mostrando ? 'Ocultar senha' : 'Mostrar senha');
            });
        }

        if (checklist) {
            input.addEventListener('input', () => {
                if (opcional) {
                    checklist.classList.toggle('hidden', input.value.length === 0);
                }
                atualizarChecklistSenha(idChecklist, input.value);
            });
        }
    }

    return {
        valida() {
            if (opcional && input.value.length === 0) return true; // campo em branco = mantém senha atual
            if (!exigirRegras) return true;
            return senhaAtendeTodasAsRegras(input.value);
        }
    };
}
