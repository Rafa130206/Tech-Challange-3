# Tech Challenge 3 — APIs de atendimento médico

Sistema composto por quatro microsserviços para autenticação, agendamento de consultas, notificações e histórico médico. As APIs utilizam JWT e autorização por perfil: médico (`DOCTOR`), enfermagem (`NURSE`) e paciente (`PATIENT`).

Esta documentação descreve os contratos e as regras presentes no código do repositório. Os exemplos são ilustrativos; IDs, tokens e datas devem ser substituídos pelos valores do ambiente. Não representam respostas capturadas em uma execução dos serviços.

## Sumário

- [Arquitetura e tecnologias](#arquitetura-e-tecnologias)
- [Execução e configuração](#execução-e-configuração)
- [Convenções das APIs](#convenções-das-apis)
- [Autenticação](#autenticação)
- [Matriz de permissões](#matriz-de-permissões)
- [Agendamentos](#agendamentos)
- [Disponibilidade](#disponibilidade)
- [Notificações](#notificações)
- [Histórico médico — GraphQL](#histórico-médico--graphql)
- [Erros](#erros)
- [Eventos e integração assíncrona](#eventos-e-integração-assíncrona)
- [Postman e validação](#postman-e-validação)
- [Limitações do contrato atual](#limitações-do-contrato-atual)

## Arquitetura e tecnologias

| Serviço | Responsabilidade | Interface local | Persistência |
| --- | --- | --- | --- |
| `auth-service` | Autenticar contas e emitir JWT | `http://localhost:8080/auth/token` | Contas em memória, configuradas por propriedades |
| `scheduling-service` | Gerenciar consultas e consultar horários | `http://localhost:8081` — REST | PostgreSQL |
| `notification-service` | Registrar e processar lembretes | `http://localhost:8082/notificacoes` — REST | MongoDB |
| `history-service` | Gerenciar histórico médico | `http://localhost:8083/graphql` — GraphQL | PostgreSQL |

O projeto usa Java 21, Spring Boot 4.1.1, Maven Wrapper, Spring Security, Spring Data JPA, Spring GraphQL e Kafka. O Docker Compose declara PostgreSQL 16, MongoDB 8.0 e Kafka 4.3.1.

O agendamento publica eventos no Kafka. O serviço de notificações consome esses eventos, persiste lembretes e publica eventos de envio. O histórico é gerenciado por operações GraphQL explícitas; não há consumidor que o crie automaticamente a partir de uma consulta.

## Execução e configuração

### Executar com Docker Compose

Pré-requisitos: Docker com suporte a Compose e acesso aos registros de imagens e repositórios Maven durante o build. O build Java ocorre nos contêineres.

Na raiz do projeto, crie `.env` a partir de `.env.example`, caso ainda não exista. No PowerShell:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
docker compose up --build -d
docker compose ps
docker compose logs -f auth-service scheduling-service notification-service history-service
```

O Compose usa `depends_on`, sem verificações de prontidão. Caso uma aplicação falhe enquanto banco ou Kafka ainda inicializam, consulte os logs e reinicie o serviço afetado após a infraestrutura estar pronta.

Para encerrar os contêineres preservando os volumes:

```sh
docker compose down
```

| Dependência | Acesso pelo host | Acesso entre contêineres |
| --- | --- | --- |
| PostgreSQL de agendamento | `localhost:5434` | `postgres-scheduling-service:5432` |
| PostgreSQL de histórico | `localhost:5435` | `postgres-history-service:5432` |
| MongoDB de notificações | `localhost:27018` | `mongo-notification-service:27017` |
| Kafka | `localhost:29092` | `kafka:9092` |

Use a porta `29092` para clientes Kafka executados no host: esse listener anuncia `localhost`. A porta `9092` anuncia o nome interno `kafka`.

### Executar as aplicações localmente

Além do Docker para a infraestrutura, instale JDK 21 e configure `JAVA_HOME`. O Maven é fornecido pelo Wrapper.

```sh
docker compose up -d postgres-scheduling-service postgres-history-service mongo-notification-service kafka
```

Abra um terminal PowerShell por serviço. Para os serviços que usam Kafka, ajuste o endereço para o listener do host; para notificações, ajuste também a porta do MongoDB:

```powershell
# Terminal 1
.\mvnw.cmd -f auth-service/pom.xml spring-boot:run
```

```powershell
# Terminal 2
$env:SPRING_KAFKA_BOOTSTRAP_SERVERS = "localhost:29092"
.\mvnw.cmd -f scheduling-service/pom.xml spring-boot:run
```

```powershell
# Terminal 3
$env:SPRING_KAFKA_BOOTSTRAP_SERVERS = "localhost:29092"
$env:SPRING_MONGODB_URI = "mongodb://localhost:27018/notification-service"
.\mvnw.cmd -f notification-service/pom.xml spring-boot:run
```

```powershell
# Terminal 4
.\mvnw.cmd -f history-service/pom.xml spring-boot:run
```

Em Linux/macOS, use `./mvnw` e `export NOME=valor`. O arquivo `.env` é usado pelo Compose; ele não é carregado automaticamente ao executar o Maven. Se alterar credenciais de banco ou o segredo JWT, configure os mesmos valores nos terminais correspondentes.

### Variáveis e propriedades

| Configuração | Serviço/escopo | Padrão e finalidade |
| --- | --- | --- |
| `AUTH_JWT_SECRET` | Todos os serviços | Segredo compartilhado de desenvolvimento definido em `.env.example` e nas propriedades; assinatura HS256 |
| `AUTH_TOKEN_TTL_SECONDS` | Auth | `3600` segundos |
| `AUTH_DOCTOR_USERNAME`, `AUTH_DOCTOR_PASSWORD`, `AUTH_DOCTOR_SUBJECT` | Auth | `dr.silva`, `doctor123`, `1` |
| `AUTH_NURSE_USERNAME`, `AUTH_NURSE_PASSWORD`, `AUTH_NURSE_SUBJECT` | Auth | `nurse.souza`, `nurse123`, `2` |
| `AUTH_PATIENT_USERNAME`, `AUTH_PATIENT_PASSWORD`, `AUTH_PATIENT_SUBJECT` | Auth | `patient.jose`, `patient123`, `3` |
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Compose: banco de agendamento | `scheduling`, `admin`, `adminPostgres` |
| `SPRING_DATASOURCE_URL` | Scheduling / History | `jdbc:postgresql://localhost:5434/scheduling` / `jdbc:postgresql://localhost:5435/history` |
| `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Scheduling / History | `admin`, `adminPostgres` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Scheduling / Notification | Propriedades locais: `localhost:9092`; com a infraestrutura Compose, usar `localhost:29092` no host |
| `SPRING_MONGODB_URI` | Notification | Propriedades locais: `mongodb://localhost:27017/notification-service`; Compose publica a porta `27018` no host |
| `app.notification.dispatch-interval-ms` | Notification | `60000`; atraso entre execuções do processamento de lembretes |

O Compose repassa explicitamente `AUTH_JWT_SECRET`, mas não as demais variáveis `AUTH_*`. Para personalizar contas ou expiração em contêineres, adicione essas variáveis ao bloco `environment` de `auth-service`. `DB_PORT`, embora presente em `.env.example`, não é referenciado pelo Compose: a porta publicada de agendamento permanece `5434`.

As credenciais fornecidas são de demonstração. Para outro ambiente, substitua-as e configure um segredo HS256 com pelo menos 32 bytes, igual nos quatro serviços.

## Convenções das APIs

| Item | Convenção |
| --- | --- |
| Corpo de requisição | JSON, com `Content-Type: application/json`, quando houver corpo |
| Autenticação de negócio | `Authorization: Bearer <accessToken>` |
| Identificador de consulta | UUID, por exemplo `550e8400-e29b-41d4-a716-446655440000` |
| Identificadores de usuários REST | Inteiros compatíveis com `Long` |
| Identificadores GraphQL | Tipo `ID`; exemplos usam strings; médico e paciente precisam representar valores numéricos compatíveis com `Long` |
| Data de disponibilidade | `YYYY-MM-DD` |
| Data/hora de consulta | ISO 8601 sem offset, por exemplo `2026-10-15T10:00:00` (`LocalDateTime`) |
| Listagens REST | Arrays JSON, sem paginação; lista vazia: `[]` |
| GraphQL | Corpo com `query` e, opcionalmente, `variables` e `operationName`; resposta com `data` e/ou `errors` |

Os exemplos HTTP podem ser reproduzidos em clientes como Postman. Substitua `<TOKEN_DOCTOR>`, `<TOKEN_NURSE>` e `<TOKEN_PATIENT>` por tokens dos respectivos perfis. Escolha datas pelo menos três dias no futuro para criar ou reagendar consultas.

## Autenticação

### `POST /auth/token`

Autentica via **HTTP Basic**, sem corpo, e emite um JWT. Usuário e senha não são enviados como JSON.

| Parâmetro | Local | Obrigatório | Descrição |
| --- | --- | --- | --- |
| `Authorization` | Header | Sim | `Basic <base64(usuario:senha)>`; o cliente HTTP pode gerar o header |

Contas de demonstração:

| Perfil | Usuário | Senha | `sub` padrão |
| --- | --- | --- | --- |
| Médico | `dr.silva` | `doctor123` | `1` |
| Enfermagem | `nurse.souza` | `nurse123` | `2` |
| Paciente | `patient.jose` | `patient123` | `3` |

```sh
curl -X POST http://localhost:8080/auth/token -u dr.silva:doctor123
```

No Windows PowerShell, use `curl.exe` se `curl` estiver associado a outro comando.

Resposta — **200 OK**:

```json
{
  "accessToken": "<JWT_GERADO_PELO_SERVICO>",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

| Campo de resposta | Tipo | Descrição |
| --- | --- | --- |
| `accessToken` | string | JWT assinado com HS256 |
| `tokenType` | string | `Bearer` |
| `expiresIn` | inteiro | Tempo de validade em segundos |

O token contém `sub`, `iat`, `exp` e `roles`. O `sub` identifica o usuário; `roles` contém o perfil, sem o prefixo `ROLE_`. Credenciais ausentes ou inválidas resultam em **401 Unauthorized**. Para obter outro token após a expiração, autentique-se novamente; não há endpoint de refresh ou logout.

As contas de autenticação são independentes dos usuários persistidos no agendamento. O agendamento insere os três usuários de demonstração apenas quando a tabela está vazia. Em um banco recém-criado, os IDs esperados são `1`, `2` e `3`; em bancos reutilizados, mantenha os subjects do JWT alinhados aos IDs reais, especialmente para as consultas do próprio paciente.

## Matriz de permissões

| Operação | DOCTOR | NURSE | PATIENT |
| --- | :---: | :---: | :---: |
| `POST /appointments` | Sim | Sim | Não |
| `PUT /appointments/{id}` | Sim | Não | Não |
| `PATCH /appointments/{id}/status` | Sim | Sim | Não |
| `GET /appointments` | Sim | Sim | Não |
| `GET /appointments/{id}` | Sim | Sim | Não |
| `GET /appointments/me` | Não | Não | Sim |
| `GET /availability` | Sim | Sim | Sim |
| `GET /notificacoes` | Sim | Sim | Não |
| GraphQL: `getAll`, `getByDoctorId`, `getByPatientId`, `getBySchedulingId` | Sim | Sim | Não |
| GraphQL: `myHistory` | Não | Não | Sim |
| GraphQL: `create` | Não | Sim | Não |
| GraphQL: `update`, `delete` | Sim | Não | Não |

As operações administrativas verificam o perfil, sem restringir os registros ao médico autenticado. As operações `me` e `myHistory` filtram pelo `sub` do paciente autenticado.

## Agendamentos

Base: `http://localhost:8081`.

### Modelo de entrada e saída

`AppointmentRequest`, usado na criação e na atualização:

| Campo | Tipo | Obrigatório para uma chamada válida | Regra |
| --- | --- | --- | --- |
| `patientId` | inteiro | Sim | Usuário existente com perfil `PATIENT` |
| `doctorId` | inteiro | Sim | Usuário existente com perfil `DOCTOR` |
| `dateTime` | string de data/hora | Sim | Antecedência mínima de três dias e ausência de conflito para o médico |
| `notes` | string | Não | Observações; pode ser `null` |

`AppointmentResponse` contém os quatro campos acima e:

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | string UUID | Identificador gerado para a consulta |
| `status` | string enum | `SCHEDULED`, `COMPLETED` ou `CANCELLED` |

### Criar consulta — `POST /appointments`

Perfis: `DOCTOR` ou `NURSE`. Cria a consulta com status `SCHEDULED` e publica `agendamento.criado`.

```http
POST /appointments HTTP/1.1
Host: localhost:8081
Authorization: Bearer <TOKEN_NURSE>
Content-Type: application/json

{
  "patientId": 3,
  "doctorId": 1,
  "dateTime": "2026-10-15T10:00:00",
  "notes": "Consulta de acompanhamento"
}
```

Resposta — **201 Created**:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "patientId": 3,
  "doctorId": 1,
  "dateTime": "2026-10-15T10:00:00",
  "status": "SCHEDULED",
  "notes": "Consulta de acompanhamento"
}
```

### Atualizar consulta — `PUT /appointments/{id}`

Perfil: `DOCTOR`. Atualiza **somente `dateTime` e `notes`**; preserva paciente, médico e status. Publica `agendamento.atualizado`.

| Parâmetro | Local | Tipo | Obrigatório |
| --- | --- | --- | --- |
| `id` | Path | UUID | Sim |
| Corpo | Body | `AppointmentRequest` | Sim |

Envie os IDs originais de médico e paciente: ambos passam por validação, embora não sejam alterados. A verificação de disponibilidade usa o `doctorId` recebido no corpo.

```http
PUT /appointments/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Host: localhost:8081
Authorization: Bearer <TOKEN_DOCTOR>
Content-Type: application/json

{
  "patientId": 3,
  "doctorId": 1,
  "dateTime": "2026-10-20T14:00:00",
  "notes": "Consulta reagendada"
}
```

Resposta — **200 OK**:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "patientId": 3,
  "doctorId": 1,
  "dateTime": "2026-10-20T14:00:00",
  "status": "SCHEDULED",
  "notes": "Consulta reagendada"
}
```

### Alterar status — `PATCH /appointments/{id}/status`

Perfis: `DOCTOR` ou `NURSE`. Publica `agendamento.atualizado`.

| Parâmetro | Local | Tipo | Obrigatório | Valores aceitos para transição |
| --- | --- | --- | --- | --- |
| `id` | Path | UUID | Sim | ID existente |
| `status` | Body | string enum | Sim | `COMPLETED` ou `CANCELLED` |

```http
PATCH /appointments/550e8400-e29b-41d4-a716-446655440000/status HTTP/1.1
Host: localhost:8081
Authorization: Bearer <TOKEN_DOCTOR>
Content-Type: application/json

{
  "status": "COMPLETED"
}
```

Resposta — **200 OK**:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "patientId": 3,
  "doctorId": 1,
  "dateTime": "2026-10-20T14:00:00",
  "status": "COMPLETED",
  "notes": "Consulta reagendada"
}
```

| Estado atual | Estado solicitado | Resultado |
| --- | --- | --- |
| `SCHEDULED` | `COMPLETED` | Permitido |
| `SCHEDULED` | `CANCELLED` | Permitido |
| `SCHEDULED` | `SCHEDULED` | 400 |
| `COMPLETED` ou `CANCELLED` | Qualquer estado, inclusive o mesmo | 400 |

Não há endpoint de exclusão de consulta. Para cancelá-la, envie `{"status":"CANCELLED"}` nesta operação.

### Listar consultas — `GET /appointments`

Perfis: `DOCTOR` ou `NURSE`. Sem parâmetros; retorna todas as consultas.

```http
GET /appointments HTTP/1.1
Host: localhost:8081
Authorization: Bearer <TOKEN_DOCTOR>
```

Resposta — **200 OK**:

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "patientId": 3,
    "doctorId": 1,
    "dateTime": "2026-10-20T14:00:00",
    "status": "COMPLETED",
    "notes": "Consulta reagendada"
  }
]
```

### Buscar consulta — `GET /appointments/{id}`

Perfis: `DOCTOR` ou `NURSE`.

| Parâmetro | Local | Tipo | Obrigatório |
| --- | --- | --- | --- |
| `id` | Path | UUID | Sim |

```http
GET /appointments/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Host: localhost:8081
Authorization: Bearer <TOKEN_DOCTOR>
```

Resposta — **200 OK**: um `AppointmentResponse`, com o mesmo formato da resposta de atualização. Se o UUID não corresponder a uma consulta, retorna **404**.

### Consultar minhas consultas — `GET /appointments/me`

Perfil: `PATIENT`. Sem parâmetros. Obtém o ID do paciente do `sub` do JWT.

```http
GET /appointments/me HTTP/1.1
Host: localhost:8081
Authorization: Bearer <TOKEN_PATIENT>
```

Resposta — **200 OK**: array de `AppointmentResponse`, com o formato da listagem acima, contendo apenas consultas do paciente autenticado. Retorna `[]` quando não houver registros, incluindo consultas de todos os status quando existirem.

### Regras de agendamento

- Criação e atualização exigem `dateTime >= LocalDateTime.now().plusDays(3)`, conforme o relógio e fuso local do processo.
- Há conflito quando outra consulta do mesmo médico está no intervalo inclusivo de uma hora antes a uma hora depois do horário solicitado. A diferença precisa ser **maior que uma hora**; exatamente uma hora ainda conflita.
- Consultas `CANCELLED` não bloqueiam horários; consultas `COMPLETED` continuam bloqueando. Na atualização, a própria consulta é excluída da comparação.
- Médico e paciente precisam existir e possuir os perfis correspondentes.
- A gravação não restringe o horário à grade de disponibilidade e não verifica conflitos do paciente.

## Disponibilidade

### `GET /availability`

Base: `http://localhost:8081`. Perfis: `DOCTOR`, `NURSE` ou `PATIENT`.

| Parâmetro | Local | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- | --- |
| `date` | Query | data `YYYY-MM-DD` | Sim | Dia consultado |
| `doctorId` | Query | inteiro | Não | Médico consultado; quando omitido, retorna todos os usuários `DOCTOR` |

```http
GET /availability?doctorId=1&date=2026-10-15 HTTP/1.1
Host: localhost:8081
Authorization: Bearer <TOKEN_PATIENT>
```

Resposta — **200 OK**, exemplo para um médico sem consultas no intervalo consultado:

```json
[
  {
    "doctorId": 1,
    "date": "2026-10-15",
    "availableSlots": [
      "07:00:00", "08:00:00", "09:00:00", "10:00:00",
      "11:00:00", "12:00:00", "13:00:00", "14:00:00",
      "15:00:00", "16:00:00", "17:00:00", "18:00:00"
    ]
  }
]
```

| Campo de resposta | Tipo | Descrição |
| --- | --- | --- |
| `doctorId` | inteiro | Médico ao qual a grade se refere |
| `date` | string de data | Dia consultado |
| `availableSlots` | array de horários | Horários não bloqueados por consultas |

A resposta é sempre um array, mesmo quando `doctorId` é informado. A grade vai de 07h a 18h, em intervalos de uma hora. Uma consulta às 10h bloqueia 09h, 10h e 11h. O cálculo considera também consultas próximas aos limites do dia.

Esta consulta não aplica a antecedência de três dias, não exclui finais de semana e não valida a existência ou o perfil de um `doctorId` informado. Um horário retornado ainda pode ser rejeitado na criação por outras regras ou por uma reserva concorrente.

## Notificações

### `GET /notificacoes`

Base: `http://localhost:8082`. Perfis: `DOCTOR` ou `NURSE`. Sem parâmetros. Lista todos os registros em ordem crescente de `scheduledSendAt`.

```http
GET /notificacoes HTTP/1.1
Host: localhost:8082
Authorization: Bearer <TOKEN_NURSE>
```

Resposta — **200 OK**, quando ainda não há notificações:

```json
[]
```

Cada item de uma lista não vazia segue `NotificationResponse`:

| Campo | Tipo no DTO | Descrição |
| --- | --- | --- |
| `id` | `ObjectId` | Identificador MongoDB; não há conversão explícita para string no DTO |
| `appointmentId` | string | UUID da consulta |
| `patientUsername` | string | Login do paciente |
| `patientName` | string | Nome do paciente |
| `channel` | string | `EMAIL` |
| `message` | string | Texto do lembrete ou `Consulta cancelada` |
| `appointmentStatus` | string | Status da consulta recebido pelo evento |
| `scheduledSendAt` | `OffsetDateTime` | Um dia antes da consulta |
| `sentAt` | `OffsetDateTime` ou null | Instante do processamento de envio |
| `status` | string | `PENDENTE`, `ENVIADA` ou `CANCELADA` |

Exemplo ilustrativo dos campos de um item, **omitindo `id`**. A representação JSON de `ObjectId` depende do serializador ativo e não é fixada pelo contrato do DTO:

```json
{
  "appointmentId": "550e8400-e29b-41d4-a716-446655440000",
  "patientUsername": "patient.jose",
  "patientName": "José Oliveira",
  "channel": "EMAIL",
  "message": "Lembrete: consulta com Dr. Carlos Silva em 2026-10-15T10:00Z",
  "appointmentStatus": "SCHEDULED",
  "scheduledSendAt": "2026-10-14T10:00:00Z",
  "sentAt": null,
  "status": "PENDENTE"
}
```

Não há endpoints HTTP para criar, editar ou enviar notificações. Esses processos são acionados por eventos e pelo agendador interno:

1. Um evento de consulta cria um lembrete `PENDENTE`, previsto para um dia antes do atendimento.
2. Se já houver lembrete pendente para a consulta, seus dados são atualizados no mesmo documento.
3. Um evento `CANCELLED` altera o lembrete pendente para `CANCELADA`; se não houver pendência, não cria outro registro.
4. A cada execução, com atraso configurado de 60 segundos entre execuções, o agendador busca pendências vencidas, marca `ENVIADA`, preenche `sentAt` e publica `notificacao.enviada`.

O código não integra um provedor de e-mail. `ENVIADA` indica o processamento interno com publicação do evento, sem comprovação de entrega de e-mail. Eventos `COMPLETED` seguem o fluxo de criação/atualização de pendências, pois apenas `CANCELLED` possui tratamento de cancelamento.

## Histórico médico — GraphQL

Endpoint: **`POST http://localhost:8083/graphql`**. Todas as chamadas exigem Bearer JWT. O GraphiQL está habilitado em `/graphiql`, mas também é protegido pela configuração de segurança.

### Tipos do schema

```graphql
type History {
  id: ID!
  schedulingId: ID!
  patientId: ID!
  doctorId: ID!
  date: String!
  medicalRecords: String!
}

input HistoryInput {
  schedulingId: ID!
  patientId: ID!
  doctorId: ID!
  date: String!
  medicalRecords: String!
}
```

| Campo de `HistoryInput` | Obrigatório | Regra |
| --- | --- | --- |
| `schedulingId` | Sim | Identificador único por histórico; use o UUID retornado pelo agendamento para correlacionar os serviços |
| `patientId` | Sim | ID numérico do paciente |
| `doctorId` | Sim | ID numérico do médico |
| `date` | Sim | Data/hora ISO 8601, com ou sem offset |
| `medicalRecords` | Sim | Texto não vazio e não composto apenas por espaços |

O campo `id` de saída é gerado pelo banco. Os campos GraphQL `ID` são retornados como strings. Não há consulta ao serviço de agendamento para validar a existência de `schedulingId`, médico ou paciente.

Datas com offset são convertidas usando `toLocalDateTime()`: o offset é descartado, **sem conversão para UTC**. Por exemplo, `2026-10-15T10:00:00-03:00` é armazenado como `2026-10-15T10:00`. A resposta usa `LocalDateTime.toString()`, podendo omitir segundos iguais a zero.

### Operações disponíveis

| Tipo | Operação e argumentos | Retorno | Perfil |
| --- | --- | --- | --- |
| Query | `getAll` | `[History!]!` | `DOCTOR`, `NURSE` |
| Query | `getByDoctorId(doctorId: ID!)` | `[History!]!` | `DOCTOR`, `NURSE` |
| Query | `getByPatientId(patientId: ID!)` | `[History!]!` | `DOCTOR`, `NURSE` |
| Query | `getBySchedulingId(schedulingId: ID!)` | `History` | `DOCTOR`, `NURSE` |
| Query | `myHistory` | `[History!]!` | `PATIENT` |
| Mutation | `create(input: HistoryInput!)` | `History!` | `NURSE` |
| Mutation | `update(input: HistoryInput!, schedulingId: ID!)` | `History!` | `DOCTOR` |
| Mutation | `delete(schedulingId: ID!)` | `Boolean!` | `DOCTOR` |

Todos os argumentos com `!` são obrigatórios. As listagens não possuem paginação nem ordem explícita definida pelo serviço.

### Criar histórico — `create`

```http
POST /graphql HTTP/1.1
Host: localhost:8083
Authorization: Bearer <TOKEN_NURSE>
Content-Type: application/json

{
  "query": "mutation CreateHistory($input: HistoryInput!) { create(input: $input) { id schedulingId patientId doctorId date medicalRecords } }",
  "variables": {
    "input": {
      "schedulingId": "550e8400-e29b-41d4-a716-446655440000",
      "patientId": "3",
      "doctorId": "1",
      "date": "2026-10-20T14:00:00",
      "medicalRecords": "Atendimento realizado. Retorno conforme orientação."
    }
  }
}
```

Resposta de sucesso — **200 OK**:

```json
{
  "data": {
    "create": {
      "id": "1",
      "schedulingId": "550e8400-e29b-41d4-a716-446655440000",
      "patientId": "3",
      "doctorId": "1",
      "date": "2026-10-20T14:00",
      "medicalRecords": "Atendimento realizado. Retorno conforme orientação."
    }
  }
}
```

### Consultar históricos administrativos

As operações abaixo podem ser executadas separadamente ou na mesma requisição por médico ou enfermagem. O corpo JSON de `POST /graphql` recebe este documento no campo `query` e os valores no campo `variables`.

```graphql
query ConsultarHistoricos($doctorId: ID!, $patientId: ID!, $schedulingId: ID!) {
  getAll {
    id schedulingId patientId doctorId date medicalRecords
  }
  getByDoctorId(doctorId: $doctorId) {
    id schedulingId patientId doctorId date medicalRecords
  }
  getByPatientId(patientId: $patientId) {
    id schedulingId patientId doctorId date medicalRecords
  }
  getBySchedulingId(schedulingId: $schedulingId) {
    id schedulingId patientId doctorId date medicalRecords
  }
}
```

Variáveis:

```json
{
  "doctorId": "1",
  "patientId": "3",
  "schedulingId": "550e8400-e29b-41d4-a716-446655440000"
}
```

Exemplo de resposta **200 OK** para uma consulta individual que solicita apenas `getBySchedulingId { id patientId doctorId }`:

```json
{
  "data": {
    "getBySchedulingId": {
      "id": "1",
      "patientId": "3",
      "doctorId": "1"
    }
  }
}
```

Cada campo solicitado aparece com seu próprio nome dentro de `data`. `getAll`, `getByDoctorId` e `getByPatientId` retornam arrays de objetos com os campos selecionados, ou `[]` quando não há correspondência. `getBySchedulingId` inexistente gera erro `NOT_FOUND`.

### Consultar meu histórico — `myHistory`

```http
POST /graphql HTTP/1.1
Host: localhost:8083
Authorization: Bearer <TOKEN_PATIENT>
Content-Type: application/json

{
  "query": "query { myHistory { id schedulingId patientId doctorId date medicalRecords } }"
}
```

Resposta de sucesso — **200 OK**:

```json
{
  "data": {
    "myHistory": [
      {
        "id": "1",
        "schedulingId": "550e8400-e29b-41d4-a716-446655440000",
        "patientId": "3",
        "doctorId": "1",
        "date": "2026-10-20T14:00",
        "medicalRecords": "Atendimento realizado. Retorno conforme orientação."
      }
    ]
  }
}
```

O paciente é identificado pelo `sub` do JWT; não há argumento `patientId` nessa operação.

### Atualizar histórico — `update`

Perfil: `DOCTOR`. O argumento externo `schedulingId` identifica o registro. Apenas `doctorId`, `date` e `medicalRecords` são alterados. `patientId` e `schedulingId` originais são preservados, embora todos os campos de `HistoryInput` continuem obrigatórios.

Envie `input.schedulingId` igual ao argumento externo e mantenha o paciente original. O validador de unicidade também consulta `input.schedulingId`.

```http
POST /graphql HTTP/1.1
Host: localhost:8083
Authorization: Bearer <TOKEN_DOCTOR>
Content-Type: application/json

{
  "query": "mutation UpdateHistory($schedulingId: ID!, $input: HistoryInput!) { update(schedulingId: $schedulingId, input: $input) { id schedulingId patientId doctorId date medicalRecords } }",
  "variables": {
    "schedulingId": "550e8400-e29b-41d4-a716-446655440000",
    "input": {
      "schedulingId": "550e8400-e29b-41d4-a716-446655440000",
      "patientId": "3",
      "doctorId": "1",
      "date": "2026-10-20T14:00:00",
      "medicalRecords": "Registro revisado pelo médico responsável."
    }
  }
}
```

Resposta de sucesso — **200 OK**:

```json
{
  "data": {
    "update": {
      "id": "1",
      "schedulingId": "550e8400-e29b-41d4-a716-446655440000",
      "patientId": "3",
      "doctorId": "1",
      "date": "2026-10-20T14:00",
      "medicalRecords": "Registro revisado pelo médico responsável."
    }
  }
}
```

### Excluir histórico — `delete`

Perfil: `DOCTOR`. Remove o registro do banco pelo `schedulingId`.

```http
POST /graphql HTTP/1.1
Host: localhost:8083
Authorization: Bearer <TOKEN_DOCTOR>
Content-Type: application/json

{
  "query": "mutation DeleteHistory($schedulingId: ID!) { delete(schedulingId: $schedulingId) }",
  "variables": {
    "schedulingId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

Resposta de sucesso — **200 OK**:

```json
{
  "data": {
    "delete": true
  }
}
```

Se não houver registro, retorna erro `NOT_FOUND`; não retorna `false`.

## Erros

### REST

| HTTP | Situação |
| --- | --- |
| `200 OK` | Consulta, alteração ou emissão de token concluída |
| `201 Created` | Consulta criada |
| `400 Bad Request` | Data sem antecedência mínima, participante inválido ou transição de status não permitida; também pode ocorrer por falha de conversão da requisição |
| `401 Unauthorized` | Credenciais Basic inválidas, JWT inválido/expirado ou autenticação ausente |
| `403 Forbidden` | Usuário autenticado sem o perfil exigido para a operação REST |
| `404 Not Found` | Consulta não encontrada; o agendamento também possui handler para usuário não encontrado |
| `409 Conflict` | Conflito de horário do médico |
| `500 Internal Server Error` | Falha não tratada; não existe contrato uniforme para todos os casos de entrada inválida |

O `scheduling-service` mapeia suas exceções de negócio para `ProblemDetail`. Os títulos definidos são `Appointment Not Found`, `User Not Found`, `Access Denied`, `Appointment Conflict`, `Invalid Appointment Date`, `Invalid Appointment Status Transition` e `Invalid Appointment Participant`.

Exemplo de consulta inexistente — **404 Not Found**:

```json
{
  "type": "about:blank",
  "title": "Appointment Not Found",
  "status": 404,
  "detail": "Appointment not found: 550e8400-e29b-41d4-a716-446655440000",
  "instance": "/appointments/550e8400-e29b-41d4-a716-446655440000"
}
```

Exemplo de antecedência insuficiente — **400 Bad Request**:

```json
{
  "type": "about:blank",
  "title": "Invalid Appointment Date",
  "status": 400,
  "detail": "Appointments must be scheduled at least 3 days in advance",
  "instance": "/appointments"
}
```

O campo `instance` é preenchido conforme a requisição pelo framework. Erros na cadeia de segurança, desserialização e outros serviços não necessariamente usam esse mesmo corpo.

### GraphQL

Erros de negócio resolvidos durante a execução GraphQL são retornados em `errors`, normalmente com HTTP **200**. Não interprete apenas o status HTTP como confirmação de sucesso. Falhas de autenticação podem impedir a execução GraphQL e retornar HTTP **401**.

| Situação | `errors[].extensions.classification` |
| --- | --- |
| Histórico não encontrado | `NOT_FOUND` |
| `schedulingId` duplicado | `BAD_REQUEST` |
| Campos obrigatórios inválidos ou data inválida, tratados pelo serviço | `BAD_REQUEST` |

O handler próprio não define um contrato para negações de perfil, erros de validação do schema ou outras exceções; esses casos dependem do tratamento do framework.

Exemplo ilustrativo de criação duplicada:

```json
{
  "data": null,
  "errors": [
    {
      "message": "Scheduling 550e8400-e29b-41d4-a716-446655440000 already has a history record",
      "locations": [{ "line": 1, "column": 48 }],
      "path": ["create"],
      "extensions": { "classification": "BAD_REQUEST" }
    }
  ]
}
```

`locations` depende do documento enviado. A propagação de `null` depende da nulabilidade do campo: falha em `create: History!` pode tornar `data` nulo; em `getBySchedulingId: History`, o campo pode ser `null` enquanto outros resultados permanecem disponíveis.

## Eventos e integração assíncrona

Mensagens Kafka são JSON serializado como string. A chave é o UUID da consulta.

| Tópico | Produtor | Consumidor implementado | Gatilho |
| --- | --- | --- | --- |
| `agendamento.criado` | Scheduling | Notification | Criação da consulta |
| `agendamento.atualizado` | Scheduling | Notification | Alteração de data/observações ou status |
| `notificacao.enviada` | Notification | Nenhum neste repositório | Processamento do lembrete vencido |

### Contrato de evento de agendamento

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `eventId` | string UUID | Gerado a cada publicação |
| `eventType` | string | Nome do tópico |
| `appointmentId` | string UUID | Consulta relacionada |
| `patientUsername` | string | Login do paciente |
| `patientName` | string | Nome do paciente |
| `doctorName` | string | Nome do médico |
| `scheduledAt` | string com offset | Data/hora da consulta com offset UTC anexado |
| `status` | string | Status da consulta |

```json
{
  "eventId": "7b15a6d3-82b3-4c8c-b72d-1ccf6154d761",
  "eventType": "agendamento.criado",
  "appointmentId": "550e8400-e29b-41d4-a716-446655440000",
  "patientUsername": "patient.jose",
  "patientName": "José Oliveira",
  "doctorName": "Dr. Carlos Silva",
  "scheduledAt": "2026-10-15T10:00Z",
  "status": "SCHEDULED"
}
```

O produtor usa `dateTime.atOffset(UTC)`: anexa UTC ao horário local recebido, sem converter o fuso. Esse comportamento deve ser considerado ao integrar clientes e interpretar os lembretes.

### Contrato de evento de notificação

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `eventId` | string UUID | Identificador desta publicação |
| `eventType` | string | `notificacao.enviada` |
| `notificationId` | string | ObjectId convertido explicitamente para string neste evento |
| `appointmentId` | string UUID | Consulta relacionada |
| `patientUsername`, `patientName` | string | Identificação do paciente |
| `channel`, `message` | string | Canal `EMAIL` e conteúdo do lembrete |
| `appointmentStatus` | string | Status da consulta armazenado na notificação |
| `scheduledSendAt`, `sentAt`, `publishedAt` | string com offset | Previsão, processamento e publicação |
| `status` | string | `ENVIADA` |

```json
{
  "eventId": "8677a5c7-0c46-4cb7-81bb-383ba720478a",
  "eventType": "notificacao.enviada",
  "notificationId": "6acde0123456789012345678",
  "appointmentId": "550e8400-e29b-41d4-a716-446655440000",
  "patientUsername": "patient.jose",
  "patientName": "José Oliveira",
  "channel": "EMAIL",
  "message": "Lembrete: consulta com Dr. Carlos Silva em 2026-10-15T10:00Z",
  "appointmentStatus": "SCHEDULED",
  "scheduledSendAt": "2026-10-14T10:00Z",
  "sentAt": "2026-10-14T10:00:30Z",
  "status": "ENVIADA",
  "publishedAt": "2026-10-14T10:00:30Z"
}
```

A atualização da consulta e a aparição do lembrete são assíncronas. Uma resposta HTTP de sucesso não comprova o consumo da mensagem. A persistência e a publicação Kafka não usam outbox ou transação distribuída; o código não aguarda a confirmação do `send`. O consumidor não deduplica por `eventId`, embora reutilize lembretes ainda pendentes para a mesma consulta.

## Postman e validação

As coleções estão no diretório `postman`:

| Arquivo | Finalidade |
| --- | --- |
| `tc3-auth.postman_collection.json` | Emitir tokens por perfil |
| `tc3-scheduling.postman_collection.json` | Consultas, status, disponibilidade e cenários de validação |
| `tc3-notification.postman_collection.json` | Autenticação por perfil e consulta de notificações |
| `tc3-history.postman_collection.json` | Queries e mutations de histórico |

Fluxo sugerido:

1. Inicie os serviços e importe as quatro coleções.
2. Confira `baseUrl` e, onde aplicável, `authBaseUrl`.
3. Execute a autenticação dentro de cada coleção de negócio; os scripts armazenam `doctorToken`, `nurseToken` e `patientToken` na própria coleção. A coleção Auth utiliza `accessToken`.
4. Na coleção Scheduling, confira `patientId` e `doctorId`; os scripts geram datas futuras e armazenam IDs de consultas criadas.
5. Consulte disponibilidade, crie uma consulta e verifique sua presença na listagem do paciente.
6. Consulte notificações após o consumo do evento. O envio fica previsto para um dia antes da consulta.
7. Para correlacionar um histórico com a consulta criada, copie o UUID para `schedulingId` da coleção History; seu valor inicial de demonstração é `9001`.
8. Crie o histórico com o token de enfermagem, consulte como paciente e atualize como médico. Execute exclusão ou cancelamento apenas quando desejar encerrar os registros de teste.

Com JDK 21 e as dependências necessárias disponíveis, os comandos Maven são:

```powershell
# Compilação e testes dos módulos
.\mvnw.cmd verify

# Teste de um módulo
.\mvnw.cmd -f scheduling-service/pom.xml test
```

Os testes Java presentes em Scheduling e Notification verificam carregamento de contexto; não demonstram cobertura completa dos contratos de API. Esta documentação foi revisada estaticamente contra o código, sem execução dos serviços ou das coleções.

## Limitações do contrato atual

- Não há cadastro de usuários, refresh/revogação de tokens, paginação ou endpoint de exclusão de consultas implementados.
- Os DTOs REST de agendamento não têm validações declarativas de nulidade. Campos obrigatórios ausentes podem provocar erros não tratados; não é garantido um `400` padronizado para todos os corpos inválidos, inclusive `status: null`.
- As restrições de estado terminal são aplicadas pelo `PATCH` de status. O `PUT` não impede alteração de data e observações de consultas concluídas ou canceladas.
- O histórico preserva paciente e vínculo de agendamento no update, mas não valida a integridade desses vínculos entre serviços.
- A grade de disponibilidade é consultiva: não constitui reserva e não garante proteção contra gravações concorrentes.
- Os serviços não usam uma convenção única de fuso horário: agendamento trabalha com `LocalDateTime`, eventos anexam UTC e histórico descarta offsets recebidos.
- O formato JSON do `ObjectId` na resposta REST de notificações não é explicitamente normalizado para string.

Esses pontos descrevem a implementação existente e devem ser considerados ao construir clientes ou evoluir o contrato das APIs.
