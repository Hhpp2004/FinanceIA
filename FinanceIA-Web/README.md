# FinanceIA Web

Aplicação de gestão financeira pessoal construída com Java e Spring Boot. Reúne uma interface web responsiva e uma API REST para controlar entradas, saídas, saldos, extratos mensais e relatórios financeiros.

## Funcionalidades

- Cadastro e confirmação de conta por código enviado por e-mail.
- Login web por sessão e autenticação da API com JWT RSA.
- Recuperação e redefinição de senha.
- Dashboard com saldo, entradas, saídas e gráfico de gastos por categoria.
- Destaques da bolsa com as cinco maiores altas e baixas do dia.
- Demonstração da página inicial com valores ilustrativos atualizados a cada 5 segundos.
- Pesquisa por código ou nome de ações e fundos imobiliários.
- Resultados do mercado em links para a página de detalhes de cada ativo.
- Visualização de cotação, variação, abertura, máximas, mínimas, volume, valor de mercado e indicadores disponíveis.
- Cadastro de operações com nome, descrição, valor, data, tipo e categoria.
- Extratos mensais com resumo e histórico de operações.
- Visualização detalhada de cada operação.
- Relatório PDF criado com JasperReports e enviado por e-mail.
- Geração do PDF exclusivamente em memória, sem salvar arquivos no servidor.
- Notificação visual após o envio do relatório.
- Configuração de envio automático de relatórios.
- Atualização de nome, telefone e senha; a troca de e-mail exige um fluxo futuro de reconfirmação.
- Interface responsiva com Thymeleaf, CSS e JavaScript.

## Tecnologias

- Java 21
- Spring Boot 3.5.10
- Spring MVC e Thymeleaf
- Spring Security
- OAuth2 Resource Server e JWT RSA
- Spring Data JPA
- Jakarta Validation
- PostgreSQL 15
- JasperReports 6.21.5
- Spring Mail
- Spring AMQP e RabbitMQ
- HTMX 2.0.4
- Docker Compose
- Maven Wrapper

## Estrutura

```text
src/
├── main/
│   ├── java/financa/backend/
│   │   ├── Config/       # Segurança, JWT e configurações
│   │   ├── Controller/   # Controllers web e REST
│   │   ├── DTO/          # Objetos de entrada e saída
│   │   ├── Exception/    # Exceções e ProblemDetail
│   │   ├── Model/        # Entidades e enums
│   │   ├── Producer/     # Publicação de mensagens no RabbitMQ
│   │   ├── Repository/   # Repositórios Spring Data
│   │   └── Service/      # Regras de negócio
│   └── resources/
│       ├── reports/      # Modelo JRXML do relatório
│       ├── static/       # CSS e JavaScript
│       ├── templates/    # Páginas Thymeleaf
│       ├── keystore/     # Certificado HTTPS de desenvolvimento
│       └── application*.properties
└── test/                 # Testes automatizados
```

## Pré-requisitos

- JDK 21 ou superior completo, incluindo `javac`.
- Docker e Docker Compose para o PostgreSQL.
- RabbitMQ acessível para os e-mails publicados na fila.
- Conta SMTP com senha de aplicativo para o envio dos relatórios.
- Conta e chave de API da [brapi.dev](https://brapi.dev/dashboard) para pesquisar e consultar todos os ativos da B3.

Não é necessário instalar Maven: o repositório contém `mvnw` e `mvnw.cmd`.

## Configuração das variáveis

Crie um arquivo `.env` na raiz. Ele é ignorado pelo Git.

```dotenv
DB_USERNAME=postgres
DB_PASSWORD=uma-senha-segura
DB_NAME=financas
DB_PORT=5433

RABBITMQ_KEY=amqp://usuario:senha@localhost:5672
NAME_QUEUE=email

SENHA_APP=senha-de-aplicativo-do-provedor-smtp

BRAPI_TOKEN=chave-gerada-no-dashboard-da-brapi
```

O endereço SMTP está configurado em `application.properties`. Em produção, o usuário SMTP, a senha, as chaves RSA e os certificados devem ser fornecidos por variáveis de ambiente ou por um gerenciador de segredos.

Nunca versione o arquivo `.env` ou credenciais reais.

> O Spring Boot não carrega arquivos `.env` automaticamente quando é iniciado diretamente pelo Maven. Exporte as variáveis no terminal, configure-as na IDE ou utilize o Docker Compose para repassá-las ao processo.

## Banco de dados

Inicie somente o serviço principal do PostgreSQL:

```bash
docker compose up -d db
```

Comandos úteis:

```bash
docker compose logs -f db
docker compose stop db
```

O banco é exposto por padrão em `localhost:5433` e os dados são mantidos no volume `pgdata`. O Hibernate está configurado com `ddl-auto=update`.

> Os serviços `db` e `dbTeste` do Compose usam a mesma porta local. Não inicie os dois simultaneamente sem alterar a porta e o volume do banco de testes.

## Executando a aplicação

Compile e execute os testes:

```bash
./mvnw clean test
```

Inicie a aplicação:

```bash
./mvnw spring-boot:run
```

O perfil definido atualmente como padrão é `prod`. Tanto a configuração atual de desenvolvimento quanto a de produção expõem a aplicação em:

```text
https://localhost:8443
```

O certificado incluído no projeto é autoassinado. O navegador pode exibir um aviso no primeiro acesso.

Para selecionar explicitamente o perfil:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Se estiver executando diretamente pelo terminal, configure a chave da BRAPI antes de iniciar:

```bash
export BRAPI_TOKEN="sua-chave"
./mvnw spring-boot:run
```

Também é possível configurar somente para uma execução:

```bash
BRAPI_TOKEN="sua-chave" ./mvnw spring-boot:run
```

Informe somente a chave ou o valor completo iniciado por `Bearer`; a aplicação aceita os dois formatos. Reinicie a aplicação após alterar a variável.

## Mercado financeiro

O dashboard consulta a [brapi.dev](https://brapi.dev/docs) para exibir os destaques do mercado. Cada item é um link para a tela de dados do ativo.

O card da página inicial é apenas uma demonstração visual: seus preços e percentuais são simulados no navegador e mudam a cada 5 segundos. As cotações reais são consultadas somente no dashboard autenticado.

A barra de pesquisa aceita código ou nome, a partir de dois caracteres. O backend pesquisa separadamente:

- Ações (`type=stock`);
- Fundos imobiliários (`type=fund`).

Os resultados duplicados são removidos e até dez opções são exibidas como links para `/acoes/{codigo}`.

A consulta detalhada apresenta os campos fornecidos pela BRAPI, incluindo:

- Preço e variação;
- Abertura e fechamento anterior;
- Máxima, mínima e faixa do dia;
- Volume negociado;
- Valor de mercado;
- Faixa de 52 semanas;
- Preço/lucro e lucro por ação, quando disponíveis.

Alguns instrumentos especiais encontrados na listagem, como direitos, recibos e bônus de subscrição, podem não possuir cotação detalhada no endpoint de ações. A interface informa quando o ativo não é compatível, quando o token é recusado, quando o plano não oferece acesso ou quando o limite de consultas é atingido.

Nunca coloque o `BRAPI_TOKEN` em JavaScript, HTML ou arquivos versionados. Todas as chamadas autenticadas são feitas pelo backend.

## Relatórios por e-mail

O modelo está em:

```text
src/main/resources/reports/finance_model.jrxml
```

O fluxo de envio é:

1. O usuário abre um extrato.
2. Seleciona **Enviar relatório por e-mail**.
3. O backend valida que o extrato pertence ao usuário autenticado.
4. O JasperReports executa a consulta parametrizada pelo usuário e pelo extrato.
5. O PDF é exportado para um `byte[]` em memória.
6. O Spring Mail envia o conteúdo como anexo.
7. A interface mostra uma notificação de sucesso.

Nenhum PDF é salvo no computador ou no servidor.

O relatório usa os parâmetros:

- `USER_ID`: identificador do usuário autenticado.
- `EXTRATO_ID`: UUID do extrato selecionado.

O teste `GenerateFilePDFTest` verifica se o arquivo JRXML continua compilando com a versão do JasperReports usada pelo projeto.

## Rotas web

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/` | Página inicial |
| `GET` | `/login` | Login |
| `GET` | `/cadastro` | Cadastro de conta |
| `GET` | `/confirmar-email` | Confirmação por código |
| `GET` | `/esqueci-senha` | Recuperação de senha |
| `GET` | `/verificar-codigo` | Validação do código de recuperação |
| `GET` | `/redefinir-senha` | Definição da nova senha |
| `GET` | `/app` | Dashboard financeiro |
| `GET` | `/api/acoes/pesquisa?termo={termo}` | Pesquisa ações e fundos imobiliários |
| `GET` | `/acoes/{codigo}` | Cotação e dados detalhados de um ativo |
| `GET` | `/nova-operacao` | Formulário de operação |
| `POST` | `/web-operacao` | Cadastro da operação |
| `GET` | `/extratos/{id}` | Detalhes do extrato |
| `GET` | `/extratos/{extratoId}/operacoes/{operacaoId}` | Detalhes da operação |
| `POST` | `/extratos/{id}/relatorio-email` | Gera e envia o PDF por e-mail |
| `GET` | `/configuracoes` | Configurações da conta |
| `POST` | `/web-configuracoes` | Atualização da conta |

As rotas financeiras verificam a propriedade dos recursos. Alterar um UUID na URL não permite acessar dados de outro usuário.

## API REST

### Cadastro

```http
POST /cadastro
Content-Type: application/json
```

```json
{
  "nome": "Maria Silva",
  "telefone": "71999999999",
  "senha": "Senha@123",
  "email": "maria@example.com"
}
```

### Confirmação de e-mail

```http
POST /confirmacao
Content-Type: application/json
```

```json
{
  "email": "maria@example.com",
  "codigo": "123456"
}
```

### Login da API

```http
POST /login
Content-Type: application/json
```

```json
{
  "email": "maria@example.com",
  "senha": "Senha@123"
}
```

Resposta:

```json
{
  "jwtKey": "token-jwt",
  "time": 3600
}
```

Use o token nas rotas protegidas:

```http
Authorization: Bearer token-jwt
```

### Usuário

- `GET /user`
- `PUT /user`
- `PUT /user/envio-automatico`

### Operações

- `POST /operacao`
- `GET /operacao?extrato=Julho%20/%202026`
- `GET /operacao?extrato=Julho%20/%202026&operacao=Supermercado`

Exemplo de cadastro:

```json
{
  "nome": "Supermercado",
  "descricao": "Compras do mês",
  "valor": 350.90,
  "data": "2026-07-11",
  "categoria": "ALIMENTACAO",
  "tipo": "SAIDA"
}
```

Categorias: `MORADIA`, `TRANSPORTE`, `INVESTIMENTOS`, `LAZER`, `SAUDE`, `OUTROS`, `IMPOSTOS` e `ALIMENTACAO`.

Tipos: `ENTRADA` e `SAIDA`.

### Extratos

- `GET /extrato`
- `GET /extrato?nome=Julho%20/%202026`
- `GET /file` com um JSON contendo o mês para gerar e enviar o relatório pela API legada.

## Regras financeiras

- Datas aceitas: `dd-MM-yyyy`, `yyyy-MM-dd` e `dd/MM/yyyy`.
- Datas futuras são rejeitadas.
- Operações do mês atual atualizam os totais mensais e o extrato corrente.
- Operações históricas atualizam o extrato correspondente.
- O saldo atual representa o acumulado geral do usuário.
- O dashboard e o gráfico mostram os dados do mês atual.
- Os extratos usam o formato `Mês / ano`, como `Julho / 2026`.

## Segurança

- Senhas armazenadas com BCrypt.
- Política de senha forte na interface web.
- Cookies de sessão com `HttpOnly`, `SameSite=Lax` e `Secure` sob HTTPS.
- JWT assinado com chaves RSA para a API.
- Proteção CSRF nas rotas web, incluindo o envio do relatório.
- Validação de propriedade para usuários, extratos e operações.

## Build e testes

```bash
./mvnw test
./mvnw clean package
```

Para gerar o pacote sem executar os testes:

```bash
./mvnw -DskipTests package
```

O artefato é criado em `target/`.

O Maven Wrapper deste repositório também detecta um `JAVA_HOME` incompleto e, em Linux, tenta usar automaticamente o JDK associado ao comando `javac`.

## Tratamento de erros

- A API utiliza `ProblemDetail`.
- Falhas de validação retornam HTTP 400 com detalhes dos campos inválidos.
- Exceções financeiras são tratadas pelo `GlobalExceptionHandler`.
- Os formulários web exibem mensagens contextualizadas.
- A integração com a BRAPI diferencia chave recusada, acesso não permitido pelo plano, ativo inexistente e limite de requisições.

### Problemas com cotações

Se a tela informar que não foi possível consultar um ativo:

1. Confirme que a variável está disponível no mesmo processo que executa o Spring Boot:

   ```bash
   if [ -n "$BRAPI_TOKEN" ]; then echo "Token configurado"; else echo "Token ausente"; fi
   ```

2. Reinicie a aplicação após configurar o token.
3. Teste primeiro um ativo comum, como `PETR4` ou `BBAS3`.
4. Se ativos comuns funcionarem e um código especial não funcionar, o instrumento provavelmente não oferece os mesmos dados no endpoint de cotação.
5. Consulte o painel da BRAPI para verificar a validade da chave, o plano e o limite de uso.

## Próximas melhorias

- Adicionar migrations com Flyway ou Liquibase.
- Separar banco e volume do serviço `dbTeste` no Docker Compose.
- Migrar todas as configurações SMTP e TLS para variáveis de ambiente.
- Ampliar a cobertura de testes unitários e de integração.
- Documentar a API com OpenAPI/Swagger.
- Adicionar integração contínua para build e testes.
