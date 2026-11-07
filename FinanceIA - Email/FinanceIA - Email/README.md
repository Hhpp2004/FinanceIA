# FinanceIA - Email

Microsserviço responsável por consumir solicitações de e-mail do RabbitMQ, enviar mensagens pelo SMTP do Gmail e registrar o resultado no PostgreSQL. O serviço aceita e-mails de texto simples e relatórios PDF gerados por outro módulo, como o `FinanceIA - Web`.

## Fluxo da aplicação

```text
FinanceIA - Web
  └─ gera o relatório PDF com Jasper
       └─ publica EmailDto no RabbitMQ
            └─ FinanceIA - Email
                 ├─ envia o e-mail pelo Gmail
                 └─ registra ENVIADO ou ERRO no PostgreSQL
```

Quando `filePDFExists` for `true` e `filePDF` contiver bytes, o arquivo será enviado como `relatorio.pdf`, com o tipo `application/pdf`. Sem um PDF válido, o serviço envia um e-mail de texto simples.

## Tecnologias

- Java 21
- Spring Boot 3.5
- Spring AMQP e RabbitMQ
- Spring Mail e Gmail SMTP
- Spring Data JPA
- PostgreSQL 15
- Maven
- Docker Compose
- Lombok

## Requisitos

- JDK 21
- Maven 3.9 ou superior
- Docker e Docker Compose para executar o PostgreSQL local
- Uma instância do RabbitMQ acessível
- Uma conta Gmail com senha de app

Confirme que o Maven está usando o JDK correto:

```bash
java -version
mvn -version
```

Os dois comandos devem indicar Java 21 ou uma versão compatível.

## Configuração

Crie um arquivo `.env` na raiz do projeto:

```properties
USERNAME_DB=financeia
PASSWORD_DB=troque-esta-senha
KEY_RABBITMQ=amqps://usuario:senha@servidor/vhost
QUEUE_RABBITMQ=financeia.email
SENHA_APP=senha-de-app-do-gmail
```

Variáveis utilizadas:

| Variável | Descrição |
| --- | --- |
| `USERNAME_DB` | Usuário do PostgreSQL |
| `PASSWORD_DB` | Senha do PostgreSQL |
| `KEY_RABBITMQ` | Endereço de conexão do RabbitMQ |
| `QUEUE_RABBITMQ` | Nome da fila consumida pelo serviço |
| `SENHA_APP` | Senha de app da conta Gmail |

O servidor inicia na porta `8444`. O PostgreSQL local é exposto na porta `5434`.

> Não versione o arquivo `.env` e nunca publique senhas ou endereços do RabbitMQ com credenciais.

## Executando o projeto

Inicie o banco de dados:

```bash
docker compose up -d
```

Compile e execute os testes:

```bash
mvn clean test
```

Inicie a aplicação:

```bash
mvn spring-boot:run
```

Para encerrar o PostgreSQL:

```bash
docker compose down
```

O volume `postgres_data` preserva os registros entre reinicializações.

## Contrato da mensagem RabbitMQ

Exemplo de mensagem sem anexo:

```json
{
  "userId": 42,
  "emailTo": "cliente@exemplo.com",
  "subject": "Notificação FinanceIA",
  "text": "Seu processamento foi concluído.",
  "nome": "Cliente",
  "code": null,
  "filePDF": null,
  "filePDFExists": false
}
```

Exemplo de mensagem com relatório:

```json
{
  "userId": 42,
  "emailTo": "cliente@exemplo.com",
  "subject": "Relatório financeiro",
  "text": "Segue o relatório solicitado.",
  "nome": "Cliente",
  "code": null,
  "filePDF": "JVBERi0xLjQKJc...",
  "filePDFExists": true
}
```

Em JSON, `filePDF` deve conter o PDF codificado em Base64. Conversores Jackson normalmente fazem automaticamente a conversão entre Base64 e `byte[]`.

O consumidor usa `filePDFExists` como nome principal e também aceita `isFilePDFExists` para compatibilidade.

## Estrutura principal

```text
src/main/java/com/microservice
├── configs/RabbitMQConfig.java       # Fila e conversor JSON
├── consumers/EmailConsumer.java     # Consome mensagens do RabbitMQ
├── dtos/EmailDto.java               # Contrato recebido da fila
├── models/FinanceEmailSender.java   # Histórico persistido
├── repository/EmailRepository.java  # Acesso ao PostgreSQL
├── service/EmailService.java        # Envio simples ou com PDF
└── Main.java                        # Inicialização do Spring Boot
```

O conteúdo do PDF não é salvo no banco. Ele existe apenas durante o processamento da mensagem. O banco armazena os dados do envio e o status `ENVIADO` ou `ERRO`.

## Problemas comuns

### `ClassNotFoundException: com.microservice.Main`

Compile o projeto antes de executá-lo e confirme que o Maven utiliza o JDK 21:

```bash
mvn clean test
```

### `release version 21 not supported`

O Maven está usando outro JDK. Configure `JAVA_HOME` para uma instalação do JDK 21 e verifique novamente com `mvn -version`.

### `Unrecognized field "filePDFExists"`

Atualize e recompile este serviço. O contrato atual aceita `filePDFExists` e o alias `isFilePDFExists`.

### A mensagem com erro não foi processada novamente

Erros fatais de conversão podem ser rejeitados pelo listener sem requeue. Depois de corrigir o contrato, publique uma nova solicitação ou utilize a fila de mensagens mortas, caso ela esteja configurada.

### O e-mail não foi enviado

Verifique:

- se `SENHA_APP` é uma senha de app válida do Gmail;
- se o destinatário e o assunto foram enviados na mensagem;
- se a aplicação consegue acessar `smtp.gmail.com:587`;
- se o registro correspondente foi salvo com status `ERRO`;
- se o PDF e a mensagem não ultrapassam os limites do RabbitMQ ou do provedor de e-mail.
