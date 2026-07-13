# Controle Financeiro

Sistema web de controle financeiro pessoal: cadastro de usuário, autenticação via JWT e gerenciamento de transações (receitas e despesas), com dashboard de totais mensais e gráfico de distribuição.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-production-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-black)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED)

**[Acessar o projeto no ar »](https://controle-financeiro-m282.onrender.com)**

> Hospedado no plano gratuito do Render: o servidor "dorme" após alguns minutos sem uso, então a primeira requisição depois de um tempo parado pode levar de 30 a 60 segundos para responder.

## Tecnologias

**Back-end**
- Java 17
- Spring Boot 3.5 (Web, Data JPA)
- MySQL em desenvolvimento local / PostgreSQL em produção (ver [`DEPLOY.md`](DEPLOY.md))
- Autenticação stateless com JWT (implementação própria sobre `javax.crypto`, sem dependências externas de JWT)
- Bean Validation (`jakarta.validation`)
- BCrypt para hash de senha
- Envio de e-mail transacional via API HTTP da Brevo

**Front-end**
- HTML5, CSS3 e JavaScript puro (sem frameworks)
- [Chart.js](https://www.chartjs.org/) para o gráfico do dashboard

## Funcionalidades

- Cadastro e login de usuário
- Recuperação de senha por e-mail (link de uso único, válido por 30 minutos)
- Edição de perfil (nome, e-mail, senha)
- CRUD completo de transações (receitas e despesas)
- Dashboard com totais do mês (entradas, saídas, saldo) e gráfico de distribuição
- Filtro de histórico por mês/ano
- Instalável como app no celular ou PC (PWA — ícone na tela inicial, sem barra de navegador)

## Segurança

- Senhas armazenadas com hash **BCrypt** (nunca em texto puro)
- Autenticação via **token JWT**: toda rota de dados exige um token válido no header `Authorization: Bearer {token}`
- Cada usuário só acessa, edita e exclui as **próprias** transações — o back-end nunca confia em um ID enviado pelo cliente, sempre deriva o usuário autenticado a partir do token
- Recuperação de senha por **token de uso único enviado por e-mail** — a resposta da API nunca revela se um e-mail está ou não cadastrado, evitando enumeração de usuários
- Restrição de **e-mail único** aplicada no próprio banco de dados, além da validação da aplicação
- **Limite de tentativas** (rate limiting) nas rotas de login, cadastro e recuperação de senha, contra ataques de força bruta
- **CORS restrito** a domínios configurados por variável de ambiente (nunca `*`)
- Cabeçalhos de segurança HTTP padrão (`X-Frame-Options`, `X-Content-Type-Options`, `Strict-Transport-Security`)
- Validação de entrada em todos os endpoints (`@Valid`), com respostas de erro padronizadas
- Credenciais de banco de dados, chave JWT e chave de e-mail sempre lidas de variáveis de ambiente, nunca versionadas
- Banco de dados de produção (PostgreSQL/Neon) só acessível com usuário e senha — nunca exposto publicamente

## Como rodar localmente

### Pré-requisitos
- Java 17+
- Maven (ou use o `mvnw` incluso)
- MySQL rodando localmente

### Passos

1. Clone o repositório:
```bash
   git clone https://github.com/gabmerces/controle-financeiro.git
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

Quer colocar sua própria cópia no ar, com link público e banco de produção? Veja o passo a passo em [`DEPLOY.md`](DEPLOY.md).

## Estrutura do projeto
src/main/java/com/merces/controle_financeiro/
├── config/       # Configurações (PasswordEncoder, CORS, registro dos filtros)
├── controller/   # Endpoints REST
├── dto/          # Objetos de entrada/saída da API (nunca expõem a entidade JPA direto)
├── exception/    # Exceções customizadas e tratamento global de erros
├── model/        # Entidades JPA
├── repository/   # Repositórios Spring Data JPA
├── security/     # Geração/validação de JWT, filtro de autenticação e rate limiting
└── service/      # Regras de negócio

## Possíveis melhorias futuras

- Testes automatizados (unitários nos services, integração nos controllers)
- Paginação no histórico de transações
- Categorização de despesas/receitas
- Exportação de relatórios em PDF/Excel

## Autora

Desenvolvido por **Gabriela Merces**.

## Licença

Projeto pessoal, feito para fins de estudo e portfólio.
