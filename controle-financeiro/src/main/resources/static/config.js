// Configuração central do endereço da API.
// Em desenvolvimento local, o front (servido pelo Spring) e o back estão no mesmo host,
// então detectamos automaticamente se estamos em localhost.
// Em produção, ajuste API_BASE_URL para o domínio real do seu back-end (ou deixe vazio
// se front e back forem servidos pela mesma aplicação/domínio).
const API_BASE_URL = (() => {
    const host = window.location.hostname;
    if (host === 'localhost' || host === '127.0.0.1') {
        return 'http://localhost:8080';
    }
    return ''; // mesmo domínio em produção
})();

// Registra o Service Worker (deixa o site instalável no celular/PC e mais rápido em
// visitas seguintes). Só roda em produção via HTTPS — em localhost sobre HTTP simples
// alguns navegadores bloqueiam Service Workers fora de localhost mesmo, então isso é seguro.
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('service-worker.js').catch(() => {
            // Falha silenciosa: a ausência do Service Worker não deve quebrar o uso do site.
        });
    });
}
