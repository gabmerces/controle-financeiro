# 💰 Controle Financeiro

Sistema web de controle financeiro pessoal, com cadastro de usuário, autenticação via JWT e gerenciamento de transações (receitas e despesas), incluindo dashboard com totais mensais e gráfico.

> 🚀 Quer colocar o projeto no ar (grátis) com um link público? Veja **[DEPLOY.md](DEPLOY.md)**.

## 🚀 Tecnologias

**Back-end**
- Java 17
- Spring Boot 3.5 (Web, Data JPA)
- MySQL em desenvolvimento local / PostgreSQL em produção (ver `DEPLOY.md`)
- Autenticação stateless com JWT (implementação própria em cima de `javax.crypto`, sem dependências externas de JWT)
- Bean Validation (`jakarta.validation`)
- BCrypt para hash de senha
- Envio de e-mail transacional via API HTTP da Brevo

**Front-end**
- HTML5, CSS3 e JavaScript puro (sem frameworks)
- [Chart.js](https://www.chartjs.org/) para o gráfico do dashboard

## ✨ Funcionalidades

- Cadastro e login de usuário
- Recuperação de senha por e-mail (link de uso único, válido por 30 minutos)
- Edição de perfil (nome, e-mail, senha)
- CRUD completo de transações (receitas e despesas)
- Dashboard com totais do mês (entradas, saídas, saldo) e gráfico de distribuição
- Filtro de histórico por mês/ano
- Instalável como app no celular ou PC (PWA — ícone na tela inicial, sem barra de navegador)

## 🔐 Segurança

- Senhas armazenadas com hash **BCrypt** (nunca em texto puro)
- Autenticação via **token JWT**: toda rota de dados exige um token válido no header `Authorization: Bearer {token}`
- Cada usuário só acessa, edita e exclui as **próprias** transações — o back-end nunca confia em um ID enviado pelo cliente, sempre deriva o usuário autenticado a partir do token
- Recuperação de senha por **token de uso único enviado por e-mail** (nunca por "nome + e-mail", que permitiria qualquer pessoa que soubesse seu nome trocar sua senha) — a resposta da API também nunca revela se um e-mail está ou não cadastrado
- **Limite de tentativas** (rate limiting) nas rotas de login, cadastro e recuperação de senha, contra ataques de força bruta
- **CORS restrito** a domínios configurados por variável de ambiente (nunca `*`)
- Cabeçalhos de segurança HTTP padrão (`X-Frame-Options`, `X-Content-Type-Options`, `Strict-Transport-Security`)
- Validação de entrada em todos os endpoints (`@Valid`), com respostas de erro padronizadas
- Credenciais de banco de dados, chave JWT e chave de e-mail sempre lidas de variáveis de ambiente, nunca versionadas
- Banco de dados de produção (PostgreSQL/Neon) só acessível com usuário e senha — nunca exposto publicamente

## 📦 Como rodar localmente

### Pré-requisitos
- Java 17+
- Maven (ou use o `mvnw` incluso)
- MySQL rodando localmente

### Passos

1. Clone o repositório:
   ```bash
   git clone <url-do-repositorio>
   cd controle-financeiro
   ```

2. Configure as variáveis de ambiente (ou use os valores padrão de desenvolvimento já definidos em `application.properties`):
   ```bash
   export DB_URL=jdbc:mysql://localhost:3306/db_financeiro?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   export DB_USERNAME=root
   export DB_PASSWORD=sua_senha
   export JWT_SECRET=uma-string-aleatoria-bem-longa-aqui
   ```

3. Rode a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Acesse **http://localhost:8080** no navegador.

O banco de dados e as tabelas são criados automaticamente na primeira execução (`spring.jpa.hibernate.ddl-auto=update`).

## 📁 Estrutura do projeto

```
src/main/java/com/merces/controle_financeiro/
├── config/         # Configurações (PasswordEncoder, registro do filtro JWT)
├── controller/      # Endpoints REST
├── dto/             # Objetos de entrada/saída da API (nunca expõem a entidade JPA direto)
├── exception/        # Exceções customizadas + tratamento global de erros
├── model/           # Entidades JPA
├── repository/       # Repositórios Spring Data JPA
├── security/         # Geração/validação de JWT e filtro de autenticação
└── service/          # Regras de negócio
```

## 🛣️ Possíveis melhorias futuras

- Testes automatizados (unitários nos services, integração nos controllers)
- Paginação no histórico de transações
- Categorização de despesas/receitas
- Exportação de relatórios em PDF/Excel

## 📄 Licença

Projeto pessoal, feito para fins de estudo e portfólio.
