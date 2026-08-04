# FinanceIA

O FinanceIA é uma plataforma de gestão financeira pessoal formada por dois serviços Java/Spring Boot: uma aplicação web para controle das finanças e um microsserviço assíncrono para envio de e-mails. A comunicação entre eles ocorre por RabbitMQ.

## Componentes

| Módulo | Diretório | Responsabilidade | Porta |
| --- | --- | --- | --- |
| FinanceIA Web | [`FinanceIA-Web`](FinanceIA-Web) | Interface web, API REST, operações financeiras, extratos, cotações e geração de relatórios PDF | `8443` (HTTPS) |
| FinanceIA Email | [`FinanceIA - Email/FinanceIA - Email`](FinanceIA%20-%20Email/FinanceIA%20-%20Email) | Consumo da fila, envio de e-mails SMTP e histórico dos envios | `8444` |

Cada módulo possui seu próprio PostgreSQL no desenvolvimento:

- Web: `localhost:5433`;
- E-mail: `localhost:5434`.

## Arquitetura

```text
Navegador / cliente REST
          |
          v
FinanceIA Web (Spring Boot + Thymeleaf + API)
          |                 |
          |                 +--> PostgreSQL (finanças)
          |
          +--> RabbitMQ --> FinanceIA Email --> SMTP
                                  |
                                  +--> PostgreSQL (histórico de e-mails)
```

A aplicação web gera relatórios de extrato em PDF na memória e publica a solicitação de envio no RabbitMQ. O microsserviço de e-mails consome a mensagem, envia o e-mail e persiste o resultado (`ENVIADO` ou `ERRO`).

## Tecnologias principais

- Java 21 e Spring Boot
- Spring MVC, Thymeleaf e Spring Security
- Spring Data JPA e PostgreSQL 15
- RabbitMQ com Spring AMQP
- SMTP com Spring Mail
- JasperReports para PDFs
- Docker Compose para os bancos de desenvolvimento

## Pré-requisitos

- JDK 21;
- Docker com Docker Compose;
- RabbitMQ acessível aos dois serviços;
- Credenciais SMTP (senha de aplicativo);
- Chave da [BRAPI](https://brapi.dev/) para consultas de ações e fundos imobiliários pela aplicação web.

## Configuração rápida

Os arquivos `.env` não devem ser versionados. Crie um em cada módulo antes de iniciá-los.

### Aplicação web

Crie `FinanceIA-Web/.env`:

```dotenv
DB_USERNAME=postgres
DB_PASSWORD=uma-senha-segura
DB_NAME=financas
DB_PORT=5433

RABBITMQ_KEY=amqp://usuario:senha@localhost:5672
NAME_QUEUE=email

URL_BRAPI=https://brapi.dev/api/quote
TOKEN=sua-chave-da-brapi
```

### Microsserviço de e-mails

Crie `FinanceIA - Email/FinanceIA - Email/.env`:

```dotenv
USERNAME_DB=financeia
PASSWORD_DB=uma-senha-segura
KEY_RABBITMQ=amqp://usuario:senha@localhost:5672
QUEUE_RABBITMQ=email
SENHA_APP=senha-de-aplicativo-smtp
```

`NAME_QUEUE` e `QUEUE_RABBITMQ` devem ter o mesmo valor. Ajuste os endereços e credenciais conforme a sua instância do RabbitMQ e o provedor SMTP utilizado.

## Como executar localmente

1. Suba os bancos de dados:

   ```bash
   cd FinanceIA-Web
   docker compose up -d db
   cd "../FinanceIA - Email/FinanceIA - Email"
   docker compose up -d
   ```

2. Inicie o microsserviço de e-mails:

   ```bash
   cd "FinanceIA - Email/FinanceIA - Email"
   mvn spring-boot:run
   ```

3. Em outro terminal, inicie a aplicação web:

   ```bash
   cd FinanceIA-Web
   ./mvnw spring-boot:run
   ```

4. Acesse `https://localhost:8443`. O certificado local é autoassinado; o navegador pode apresentar um aviso no primeiro acesso.

## Testes e build

```bash
cd FinanceIA-Web
./mvnw clean test

cd "../FinanceIA - Email/FinanceIA - Email"
mvn clean test
```

## Documentação dos módulos

- [FinanceIA Web](FinanceIA-Web/README.md): funcionalidades, rotas web e API, segurança, variáveis e relatórios.
- [FinanceIA Email](FinanceIA%20-%20Email/FinanceIA%20-%20Email/README.md): contrato da mensagem RabbitMQ, configuração SMTP e resolução de problemas.

## Segurança

Não envie ao repositório arquivos `.env`, tokens da BRAPI, credenciais SMTP, senhas de banco, URLs RabbitMQ com credenciais ou chaves privadas. Para produção, prefira variáveis de ambiente e um gerenciador de segredos.
