# 🚀 Guia de Deploy — colocando o projeto no ar de graça

Este guia coloca sua aplicação no ar com um link público, funcionando em qualquer PC ou
celular (Android/iPhone), com banco de dados de verdade e sem custo. A stack escolhida:

| Camada | Serviço | Por quê |
|---|---|---|
| Código | GitHub | Onde o projeto já vai morar, e de onde o Render vai puxar o deploy automático |
| Back-end (API + site) | [Render](https://render.com) | Free tier, deploy automático a cada `git push`, HTTPS grátis |
| Banco de dados | [Neon](https://neon.tech) | PostgreSQL gratuito **sem expirar**, só acessível com usuário/senha (nunca fica público) |
| E-mail (recuperação de senha) | Gmail (SMTP com senha de app) | Grátis, você já tem conta |

> ⚠️ Sobre o plano gratuito do Render: o servidor "dorme" depois de ~15 min sem uso e
> demora uns 30-60s para acordar na primeira visita seguinte. É normal e não é erro —
> é a troca pelo custo zero. Se isso incomodar, dá pra eliminar com um plano pago do Render
> (consulte os valores atuais em [render.com/pricing](https://render.com/pricing)).

---

## 1. Suba o código pro GitHub

```bash
cd controle-financeiro
git init
git add .
git commit -m "Primeira versão"
```

Crie um repositório novo em github.com (pode ser público — nenhum segredo está no
código, tudo vem de variáveis de ambiente), depois:

```bash
git remote add origin https://github.com/SEU-USUARIO/controle-financeiro.git
git branch -M main
git push -u origin main
```

## 2. Crie o banco de dados no Neon

1. Crie uma conta grátis em [neon.tech](https://neon.tech) (dá pra entrar com GitHub).
2. Clique em **New Project**, dê um nome (ex.: `controle-financeiro`) e crie.
3. Na tela do projeto, copie a **Connection string**. Ela vem parecida com:
   ```
   postgresql://usuario:senha@ep-xxxxx.us-east-2.aws.neon.tech/neondb?sslmode=require
   ```
4. Transforme isso em 3 valores que você vai usar no passo 4 (variáveis de ambiente do Render):
   - `DB_URL` → troque `postgresql://` por `jdbc:postgresql://` e tire o usuário/senha da frente:
     `jdbc:postgresql://ep-xxxxx.us-east-2.aws.neon.tech/neondb?sslmode=require`
   - `DB_USERNAME` → o usuário que aparecia na connection string
   - `DB_PASSWORD` → a senha que aparecia na connection string

Esse banco só é acessível por quem tem essas credenciais — ele nunca fica exposto
publicamente, e ninguém consegue "vazar" os dados só de saber a URL do seu site.

> ℹ️ O Neon "hiberna" o banco depois de alguns minutos sem uso e acorda sozinho na
> próxima consulta (leva menos de 1 segundo) — não precisa fazer nada a respeito, é
> só o motivo de uma eventual primeira consulta do dia ser um pouquinho mais lenta.

## 3. Crie uma conta gratuita na Brevo (pra enviar o e-mail de recuperação de senha)

> ⚠️ Importante: hospedagens gratuitas como o Render **bloqueiam** o tráfego de saída nas
> portas usadas por SMTP tradicional (as que o Gmail usa) — é uma proteção deles contra spam.
> Por isso o projeto já vem configurado pra enviar e-mail pela **API HTTP da Brevo**, que
> funciona normalmente mesmo com esse bloqueio, e tem plano grátis de 300 e-mails/dia sem
> expirar.

1. Crie uma conta grátis em [app.brevo.com](https://app.brevo.com) (não pede cartão).
2. Vá em **Settings → Senders, Domains & Dedicated IPs → Senders** e cadastre o e-mail que
   vai aparecer como remetente (pode ser seu próprio e-mail). A Brevo manda um código de
   confirmação — confirme por lá.
3. Vá em **Settings → SMTP & API → API Keys** e gere uma nova chave (**Generate a new API key**).
   Guarde esse valor — é o `BREVO_API_KEY` do próximo passo.

## 4. Deploy no Render

1. Crie uma conta grátis em [render.com](https://render.com) (dá pra entrar com GitHub).
2. **New +** → **Web Service** → conecte o repositório `controle-financeiro`.
3. O Render vai detectar o `Dockerfile` automaticamente — deixe **Runtime: Docker**.
4. Escolha o plano **Free**.
5. Em **Environment Variables**, adicione:

   | Nome | Valor |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DB_URL` | (do passo 2) |
   | `DB_USERNAME` | (do passo 2) |
   | `DB_PASSWORD` | (do passo 2) |
   | `JWT_SECRET` | uma frase aleatória bem longa e única (ex.: gere uma em [1password.com/password-generator](https://1password.com/password-generator/) com 40+ caracteres) |
   | `APP_MAIL_ENABLED` | `true` |
   | `BREVO_API_KEY` | a chave gerada no passo 3 |
   | `MAIL_FROM` | o e-mail que você verificou como remetente na Brevo |
   | `MAIL_FROM_NAME` | `Controle Financeiro` |
   | `APP_BASE_URL` | *(deixe em branco por enquanto — volte aqui depois do passo 6)* |
   | `APP_ALLOWED_ORIGIN` | *(idem)* |

6. Clique em **Create Web Service**. O primeiro build demora uns 5-10 minutos.

## 5. Ajuste as URLs depois do primeiro deploy

Quando o deploy terminar, o Render te dá uma URL pública, algo como:
`https://controle-financeiro.onrender.com`

Volte em **Environment** e preencha as duas variáveis que faltaram:

- `APP_BASE_URL` → `https://controle-financeiro.onrender.com` (sem barra no final)
- `APP_ALLOWED_ORIGIN` → o mesmo valor

Salve — o Render faz o redeploy automaticamente.

## 6. Teste tudo

Acesse a URL do Render e confira:
- [ ] Consegue se cadastrar e fazer login
- [ ] Consegue lançar uma transação e ela aparece no dashboard
- [ ] "Esqueci minha senha" chega um e-mail de verdade com o link
- [ ] Abre e usa normalmente no celular (Chrome/Android e Safari/iPhone)
- [ ] No celular, o navegador oferece "Adicionar à tela inicial" / "Instalar app" — se sim, o PWA está funcionando

## 7. Publique no LinkedIn

Use a URL do Render (ou um domínio próprio, veja abaixo) direto no post — qualquer
pessoa com o link consegue se cadastrar e usar, de qualquer PC ou celular.

### (Opcional) Domínio próprio
Se quiser algo como `financeiro.seunome.com` em vez do link `.onrender.com`:
1. Compre o domínio (Registro.br, Namecheap etc.)
2. No Render, vá em **Settings → Custom Domains** e siga as instruções (o Render dá o
   certificado HTTPS de graça também).
3. Depois de apontado, atualize `APP_BASE_URL` e `APP_ALLOWED_ORIGIN` para o novo domínio.

---

## Deploys seguintes

A partir daqui, todo `git push` na branch `main` faz o Render atualizar o site sozinho —
não precisa repetir nenhum passo acima.

## Rodando localmente (sem mudar nada)

O `application.properties` padrão continua apontando pro MySQL local, então
`./mvnw spring-boot:run` continua funcionando exatamente como antes, sem precisar do
Render/Neon/Gmail — essas configurações só entram quando `SPRING_PROFILES_ACTIVE=prod`.
