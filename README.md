# FinanceIA

Plataforma de gestão financeira pessoal desenvolvida com Java e Spring Boot. O repositório reúne a aplicação web, um serviço assíncrono de e-mail e um módulo independente para ações.

## Módulos

| Módulo | Diretório | O que faz | Porta padrão |
| --- | --- | --- | --- |
| FinanceIA Web | [`FinanceIA-Web`](FinanceIA-Web) | Interface Thymeleaf e API REST para contas, operações, saldos e extratos; consulta de ativos pela BRAPI; geração de relatórios PDF | `8443` (HTTPS local; internamente no Compose) |
| FinanceIA Email | [`FinanceIA - Email/FinanceIA - Email`](FinanceIA%20-%20Email/FinanceIA%20-%20Email) | Consome pedidos em uma fila RabbitMQ, envia e-mails via Gmail SMTP e registra o resultado | `8444` |
| financa.acoes | [`financa.acoes`](financa.acoes) | Módulo Spring Boot com configuração de JPA, PostgreSQL e RabbitMQ; ainda não integrado ao Compose ou ao fluxo principal | `8445` |

O Compose da raiz publica o site nas portas `80` e `443`, usando Caddy e Nginx como proxy reverso. Ele inicia bancos PostgreSQL separados para a aplicação web e para o histórico de e-mails. O RabbitMQ é uma dependência externa e precisa estar acessível aos dois serviços.

## Arquitetura principal

```text
Navegador
   |
   v
Caddy (80/443) -> Nginx (8080) -> FinanceIA Web
                                      |       |
                                      |       +-> PostgreSQL (finanças)
                                      v
                                 RabbitMQ externo
                                      |
                                      v
                              FinanceIA Email -> Gmail SMTP
                                      |
                                      +-> PostgreSQL (histórico de e-mails)
```

A aplicação web gera o relatório PDF em memória e envia uma mensagem pela fila. O serviço de e-mail consome a mensagem, envia o anexo quando presente e persiste o status do envio.

## Tecnologias

- Java 21 nos módulos FinanceIA Web e FinanceIA Email;
- Spring Boot, Spring MVC, Thymeleaf, Spring Security e Spring Data JPA;
- PostgreSQL 16 e Docker Compose;
- RabbitMQ com Spring AMQP;
- JasperReports para relatórios PDF;
- BRAPI para pesquisa e consulta de ações e fundos imobiliários;
- Caddy e Nginx como proxies no Compose da raiz.

O módulo `financa.acoes` está configurado com Java 25 e Spring Boot 4.1.0, e deve ser tratado separadamente dos requisitos da aplicação principal.

## Requisitos

- Docker com o plugin Docker Compose;
- JDK 21 para executar os módulos web e de e-mail fora dos containers;
- RabbitMQ acessível pela aplicação web e pelo serviço de e-mail;
- chave BRAPI para as consultas de mercado;
- senha de aplicativo Gmail para o envio SMTP.

## Configuração do Compose da raiz

O Compose da raiz lê as credenciais dos bancos de um arquivo `.env` na raiz e carrega configurações próprias dos dois serviços por meio de `env_file`. Crie os três arquivos abaixo; não reutilize as variáveis de um arquivo em outro sem conferir os nomes esperados.

### `.env` na raiz

Usado para provisionar os dois bancos:

```dotenv
DB_USERNAME=financeia
DB_PASSWORD=troque-por-uma-senha-forte
```

### `FinanceIA-Web/.env`

```dotenv
DB_USERNAME=financeia
DB_PASSWORD=troque-por-uma-senha-forte
DB_NAME=financas
DB_PORT=5432
RABBITMQ_KEY=amqp://usuario:senha@host-do-rabbitmq:5672
NAME_QUEUE=email
URL_BRAPI=https://brapi.dev/api/quote
TOKEN=sua-chave-da-brapi
```

Dentro do Compose, o banco web é acessível como `db:5432`. `DB_NAME` deve corresponder ao nome do banco criado pelo serviço `db` (`financas`).

### `FinanceIA - Email/FinanceIA - Email/.env`

```dotenv
DB_HOST=dbEmail
DB_PORT=5432
DB_USERNAME=financeia
DB_PASSWORD=troque-por-uma-senha-forte
KEY_RABBITMQ=amqp://usuario:senha@host-do-rabbitmq:5672
NAME_EMAIL_QUEUE=email
SENHA_APP=sua-senha-de-aplicativo-gmail
```

`NAME_QUEUE` e `NAME_EMAIL_QUEUE` precisam apontar para a mesma fila. Configure o hostname do RabbitMQ de forma que seja alcançável de dentro dos containers. As credenciais de usuário e senha dos bancos devem coincidir com as do `.env` da raiz.

> Os nomes `RABBITMQ_KEY` e `KEY_RABBITMQ` são diferentes porque os módulos web e de e-mail possuem configurações próprias.

## Executar com Docker Compose

Com os arquivos de configuração criados e o RabbitMQ disponível:

```bash
docker compose up -d --build
```

A configuração local do Caddy atende `http://localhost`. O host `financeia.ddns.net` está configurado para HTTPS; para usá-lo, o DNS e o acesso externo às portas `80` e `443` precisam apontar para esta implantação. O Nginx está configurado como proxy interno para a aplicação web.

Comandos úteis:

```bash
docker compose ps
docker compose logs -f financeia-web financeia-email
docker compose down
```

`docker compose down` preserva os dados nos volumes nomeados. Para apagar também os bancos, use `docker compose down -v`; isso remove permanentemente os dados locais.

## Desenvolvimento local

Para detalhes de configuração, execução direta, rotas, API e funcionalidades da aplicação, consulte o [README do FinanceIA Web](FinanceIA-Web/README.md). Para contrato da mensagem RabbitMQ e configuração SMTP, consulte o [README do FinanceIA Email](FinanceIA%20-%20Email/FinanceIA%20-%20Email/README.md).

O Compose da raiz é voltado à execução em containers. Para iniciar o Spring Boot diretamente pelo Maven, confira os endereços e variáveis requeridos nos arquivos `application*.properties` de cada módulo; nomes de host como `db` e `dbEmail` só resolvem na rede Docker.

## Build e testes

FinanceIA Web (Maven Wrapper incluído):

```bash
cd FinanceIA-Web
./mvnw clean test
```

FinanceIA Email (requer Maven instalado):

```bash
cd "FinanceIA - Email/FinanceIA - Email"
mvn clean test
```

O módulo `financa.acoes` também possui um teste básico de contexto; ele requer Java 25 e Maven. Os testes e builds não são executados pelo Compose da raiz.

## Segurança

Não versione arquivos `.env`, tokens BRAPI, senhas SMTP ou de banco, URLs RabbitMQ com credenciais, nem chaves privadas. Em produção, injete segredos pelo ambiente de implantação ou por um gerenciador de segredos. O repositório inclui material de certificado/chaves para desenvolvimento; revise e substitua qualquer credencial ou chave antes de publicar uma implantação.
