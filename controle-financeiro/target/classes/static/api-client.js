// Wrapper central para chamadas à API:
// - Anexa automaticamente o token JWT (quando existir) no header Authorization
// - Se o servidor responder 401 (token ausente/inválido/expirado), limpa a sessão
//   e redireciona pro login, num único lugar em vez de repetir essa lógica em cada fetch()
async function apiFetch(caminho, opcoes = {}) {
    const token = localStorage.getItem('token');

    const headers = {
        'Content-Type': 'application/json',
        ...(opcoes.headers || {})
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const resposta = await fetch(`${API_BASE_URL}${caminho}`, { ...opcoes, headers });

    if (resposta.status === 401) {
        localStorage.clear();
        window.location.href = 'index.html';
        throw new Error('Sessão expirada. Faça login novamente.');
    }

    return resposta;
}
