// Service worker simples: cacheia os arquivos estáticos do "esqueleto" do site (HTML/CSS/JS/
// ícones), pra deixar a aplicação instalável no celular e carregar mais rápido em visitas
// seguintes. NÃO cacheia nenhuma chamada de API (/api/...) — os dados financeiros são
// sempre buscados ao vivo no servidor, nunca guardados aqui.

const CACHE_NAME = 'controle-financeiro-v1';

const ARQUIVOS_ESTATICOS = [
    'index.html',
    'financas.html',
    'redefinir-senha.html',
    'base-style.css',
    'components-style.css',
    'auth-style.css',
    'financas-style.css',
    'config.js',
    'api-client.js',
    'auth-script.js',
    'financas-script.js',
    'password-rules.js',
    'redefinir-senha-script.js',
    'manifest.json',
    'favicon.svg',
    'icons/icon-192.png',
    'icons/icon-512.png'
];

self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => cache.addAll(ARQUIVOS_ESTATICOS))
    );
    self.skipWaiting();
});

self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((nomes) =>
            Promise.all(nomes.filter((nome) => nome !== CACHE_NAME).map((nome) => caches.delete(nome)))
        )
    );
    self.clients.claim();
});

self.addEventListener('fetch', (event) => {
    const { request } = event;

    // Nunca intercepta chamadas de API: dados financeiros sempre vêm direto do servidor.
    if (request.url.includes('/api/')) {
        return;
    }

    // Estratégia "network first, falling back to cache": sempre tenta buscar a versão mais
    // nova, e só usa o cache se estiver offline — evita mostrar telas desatualizadas.
    event.respondWith(
        fetch(request)
            .then((resposta) => {
                const respostaClone = resposta.clone();
                caches.open(CACHE_NAME).then((cache) => cache.put(request, respostaClone));
                return resposta;
            })
            .catch(() => caches.match(request))
    );
});
