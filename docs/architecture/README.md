# Arquitectura del Sistema JavaFlow

## 📐 Visión General

JavaFlow es un sistema de gestión de workflows tipo n8n construido con tecnologías Java empresariales, diseñado para ser **local-first** pero fácilmente migrable a la nube.

## 🏗️ Arquitectura de Alto Nivel

```
┌─────────────────────────────────────────────────────────────────┐
│                        CAPA DE PRESENTACIÓN                      │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Vaadin UI (Puerto 8080)                      │  │
│  │  • Dashboard de Workflows                                 │  │
│  │  • Editor Visual de Flujos                                │  │
│  │  • Monitor de Ejecuciones                                 │  │
│  │  • Gestión de Bots                                        │  │
│  │  • Logs y Auditoría                                       │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                      CAPA DE APLICACIÓN                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Spring Boot Application                      │  │
│  │                                                            │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │  │
│  │  │   REST API  │  │  Security   │  │   WebSocket │      │  │
│  │  │  Controller │  │   (JWT)     │  │   (Events)  │      │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │  │
│  │                                                            │  │
│  │  ┌─────────────────────────────────────────────────────┐ │  │
│  │  │           Service Layer (Business Logic)            │ │  │
│  │  │  • WorkflowService                                  │ │  │
│  │  │  • BotService                                       │ │  │
│  │  │  • ExecutionService                                 │ │  │
│  │  │  • MessageService                                   │ │  │
│  │  └─────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA DE MOTOR DE WORKFLOW                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Flowable BPMN Engine                         │  │
│  │                                                            │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │  │
│  │  │ Process  │  │  Task    │  │  Event   │  │ Decision │ │  │
│  │  │ Runtime  │  │ Service  │  │ Registry │  │  Engine  │ │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │  │
│  │                                                            │  │
│  │  ┌─────────────────────────────────────────────────────┐ │  │
│  │  │         Custom Task Handlers                        │ │  │
│  │  │  • SendMessageTask                                  │ │  │
│  │  │  • HTTPRequestTask                                  │ │  │
│  │  │  • DataTransformTask                                │ │  │
│  │  │  • ConditionalTask                                  │ │  │
│  │  └─────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA DE INTEGRACIÓN                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Bot Integration Layer                        │  │
│  │                                                            │  │
│  │  ┌──────────────────┐         ┌──────────────────┐       │  │
│  │  │  Telegram Bot    │         │  WhatsApp Web    │       │  │
│  │  │  • Long Polling  │         │  • QR Auth       │       │  │
│  │  │  • Webhook       │         │  • Message Queue │       │  │
│  │  │  • Media Handler │         │  • Media Handler │       │  │
│  │  └──────────────────┘         └──────────────────┘       │  │
│  │                                                            │  │
│  │  ┌─────────────────────────────────────────────────────┐ │  │
│  │  │         Message Queue (Optional)                    │ │  │
│  │  │         RabbitMQ / Kafka / Redis                    │ │  │
│  │  └─────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                      CAPA DE PERSISTENCIA                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Spring Data JPA / Hibernate                  │  │
│  │                                                            │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │  │
│  │  │  Workflow   │  │     Bot     │  │   Message   │      │  │
│  │  │ Repository  │  │ Repository  │  │ Repository  │      │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                      BASE DE DATOS                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              PostgreSQL 14+                               │  │
│  │                                                            │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │  │
│  │  │  users   │  │workflows │  │   bots   │  │ messages │ │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │  │
│  │                                                            │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │  │
│  │  │   logs   │  │execution │  │  tasks   │  │ variables│ │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │  │
│  │                                                            │  │
│  │  ┌─────────────────────────────────────────────────────┐ │  │
│  │  │         Flowable Tables (ACT_*)                     │ │  │
│  │  │  • ACT_RE_* (Repository)                            │ │  │
│  │  │  • ACT_RU_* (Runtime)                               │ │  │
│  │  │  • ACT_HI_* (History)                               │ │  │
│  │  └─────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## 🔄 Flujo de Datos Detallado

### Escenario 1: Recepción de Mensaje de Telegram

```
┌─────────────┐
│   Usuario   │
│  (Telegram) │
└──────┬──────┘
       │ 1. Envía mensaje
       ↓
┌─────────────────────┐
│  Telegram Bot API   │
│  (Long Polling)     │
└──────┬──────────────┘
       │ 2. Update recibido
       ↓
┌─────────────────────────────────────┐
│  TelegramBotListener                │
│  @Component                          │
│  • Parsea mensaje                   │
│  • Extrae contexto                  │
└──────┬──────────────────────────────┘
       │ 3. MessageReceivedEvent
       ↓
┌─────────────────────────────────────┐
│  MessageService                     │
│  • Guarda mensaje en BD             │
│  • Identifica workflow asociado     │
└──────┬──────────────────────────────┘
       │ 4. startWorkflow()
       ↓
┌─────────────────────────────────────┐
│  WorkflowService                    │
│  • Crea instancia de proceso        │
│  • Setea variables                  │
└──────┬──────────────────────────────┘
       │ 5. runtimeService.startProcessInstanceByKey()
       ↓
┌─────────────────────────────────────┐
│  Flowable Engine                    │
│  • Ejecuta nodos BPMN               │
│  • Llama a Service Tasks            │
└──────┬──────────────────────────────┘
       │ 6. Ejecuta tareas custom
       ↓
┌─────────────────────────────────────┐
│  Custom Task Handlers               │
│  • ProcessMessageTask               │
│  • SendResponseTask                 │
│  • LogTask                          │
└──────┬──────────────────────────────┘
       │ 7. Operaciones de negocio
       ↓
┌─────────────────────────────────────┐
│  Business Services                  │
│  • Consulta BD                      │
│  • Aplica lógica                    │
│  • Prepara respuesta                │
└──────┬──────────────────────────────┘
       │ 8. Guarda en BD
       ↓
┌─────────────────────────────────────┐
│  PostgreSQL                         │
│  • messages                         │
│  • workflow_executions              │
│  • logs                             │
└──────┬──────────────────────────────┘
       │ 9. Envía respuesta
       ↓
┌─────────────────────────────────────┐
│  TelegramBotService                 │
│  • sendMessage()                    │
└──────┬──────────────────────────────┘
       │ 10. API Call
       ↓
┌─────────────────────┐
│  Telegram Bot API   │
└──────┬──────────────┘
       │ 11. Mensaje entregado
       ↓
┌─────────────┐
│   Usuario   │
│  (Telegram) │
└─────────────┘
```

### Escenario 2: Ejecución Manual desde UI

```
┌─────────────┐
│  Admin UI   │
│  (Vaadin)   │
└──────┬──────┘
       │ 1. Click "Ejecutar Workflow"
       ↓
┌─────────────────────────────────────┐
│  WorkflowView (Vaadin)              │
│  • Valida permisos                  │
│  • Captura parámetros               │
└──────┬──────────────────────────────┘
       │ 2. workflowService.execute()
       ↓
┌─────────────────────────────────────┐
│  WorkflowService                    │
│  • Valida workflow                  │
│  • Crea execution context           │
└──────┬──────────────────────────────┘
       │ 3. Start process instance
       ↓
┌─────────────────────────────────────┐
│  Flowable Engine                    │
│  • Ejecuta proceso BPMN             │
│  • Emite eventos de progreso        │
└──────┬──────────────────────────────┘
       │ 4. WebSocket events
       ↓
┌─────────────────────────────────────┐
│  ExecutionEventPublisher            │
│  • Publica eventos en tiempo real   │
└──────┬──────────────────────────────┘
       │ 5. WebSocket push
       ↓
┌─────────────┐
│  Admin UI   │
│  (Vaadin)   │
│  • Actualiza │
│    progreso  │
└─────────────┘
```

## 🗄️ Modelo de Datos

### Esquema de Base de Datos

```sql
-- USUARIOS Y SEGURIDAD
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- WORKFLOWS
CREATE TABLE workflows (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    bpmn_xml TEXT NOT NULL,
    version INTEGER DEFAULT 1,
    status VARCHAR(50) DEFAULT 'DRAFT',
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- EJECUCIONES DE WORKFLOWS
CREATE TABLE workflow_executions (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT REFERENCES workflows(id),
    process_instance_id VARCHAR(255) UNIQUE,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    started_by BIGINT REFERENCES users(id),
    error_message TEXT,
    variables JSONB
);

-- CONFIGURACIÓN DE BOTS
CREATE TABLE bot_configurations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- TELEGRAM, WHATSAPP
    token VARCHAR(500), -- Encriptado
    webhook_url VARCHAR(500),
    status VARCHAR(50) DEFAULT 'INACTIVE',
    config JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- MENSAJES
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    bot_id BIGINT REFERENCES bot_configurations(id),
    external_id VARCHAR(255),
    chat_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    direction VARCHAR(20) NOT NULL, -- INBOUND, OUTBOUND
    content TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT',
    metadata JSONB,
    workflow_execution_id BIGINT REFERENCES workflow_executions(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- LOGS
CREATE TABLE system_logs (
    id BIGSERIAL PRIMARY KEY,
    level VARCHAR(20) NOT NULL,
    logger VARCHAR(255),
    message TEXT NOT NULL,
    exception TEXT,
    workflow_execution_id BIGINT REFERENCES workflow_executions(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ÍNDICES
CREATE INDEX idx_messages_bot_id ON messages(bot_id);
CREATE INDEX idx_messages_chat_id ON messages(chat_id);
CREATE INDEX idx_messages_created_at ON messages(created_at);
CREATE INDEX idx_workflow_executions_workflow_id ON workflow_executions(workflow_id);
CREATE INDEX idx_workflow_executions_status ON workflow_executions(status);
CREATE INDEX idx_system_logs_created_at ON system_logs(created_at);
```

## 🔌 Componentes Principales

### 1. Flowable Engine Configuration

```java
@Configuration
public class FlowableConfig {
    
    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration(
            DataSource dataSource,
            PlatformTransactionManager transactionManager) {
        
        SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();
        config.setDataSource(dataSource);
        config.setTransactionManager(transactionManager);
        config.setDatabaseSchemaUpdate("true");
        config.setAsyncExecutorActivate(true);
        config.setMailServerHost("localhost");
        
        return config;
    }
}
```

### 2. Vaadin UI Architecture

```
VaadinUI/
├── MainLayout.java              # Layout principal con menú
├── views/
│   ├── DashboardView.java       # Dashboard principal
│   ├── WorkflowListView.java    # Lista de workflows
│   ├── WorkflowEditorView.java  # Editor visual BPMN
│   ├── ExecutionMonitorView.java # Monitor de ejecuciones
│   ├── BotManagementView.java   # Gestión de bots
│   └── LogsView.java            # Visor de logs
├── components/
│   ├── WorkflowCard.java        # Card de workflow
│   ├── ExecutionTimeline.java   # Timeline de ejecución
│   └── BotStatusBadge.java      # Badge de estado
└── security/
    └── SecurityConfig.java      # Configuración de seguridad
```

### 3. Bot Integration Layer

```
bot/
├── telegram/
│   ├── TelegramBotService.java
│   ├── TelegramBotListener.java
│   └── TelegramMessageHandler.java
├── whatsapp/
│   ├── WhatsAppBotService.java
│   ├── WhatsAppWebClient.java (Node.js bridge)
│   └── WhatsAppMessageHandler.java
└── common/
    ├── BotMessage.java
    ├── BotMessageHandler.java
    └── BotEventPublisher.java
```

## 🔐 Seguridad

### Capas de Seguridad

1. **Autenticación**: JWT tokens con Spring Security
2. **Autorización**: RBAC con roles (ADMIN, USER, VIEWER)
3. **Encriptación**: Credenciales de bots encriptadas con AES-256
4. **Rate Limiting**: Limitación de requests por IP/usuario
5. **Audit Log**: Registro de todas las acciones críticas

### Configuración de Seguridad

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
            .build();
    }
}
```

## 📊 Monitoreo y Observabilidad

### Métricas Clave

- **Workflows**: Total, activos, fallidos
- **Mensajes**: Enviados, recibidos, tasa de error
- **Bots**: Estado, uptime, latencia
- **Sistema**: CPU, memoria, conexiones DB

### Stack de Monitoreo

- **Actuator**: Endpoints de health y metrics
- **Micrometer**: Métricas exportables
- **Prometheus**: (Opcional) Recolección de métricas
- **Grafana**: (Opcional) Dashboards

## 🚀 Escalabilidad

### Estrategias de Escalado

1. **Horizontal**: Múltiples instancias con load balancer
2. **Vertical**: Incrementar recursos de la instancia
3. **Database**: Connection pooling, read replicas
4. **Cache**: Redis para sesiones y datos frecuentes
5. **Async**: Message queue para operaciones pesadas

### Límites y Consideraciones

- **PostgreSQL local**: ~200 conexiones concurrentes
- **Flowable**: ~1000 procesos activos simultáneos
- **Vaadin**: ~100 usuarios concurrentes por instancia
- **Bots**: Límites de API de Telegram/WhatsApp

## 🔄 Patrones de Diseño Utilizados

1. **Repository Pattern**: Acceso a datos
2. **Service Layer**: Lógica de negocio
3. **Factory Pattern**: Creación de handlers de bots
4. **Observer Pattern**: Eventos de workflow
5. **Strategy Pattern**: Diferentes tipos de tareas
6. **Builder Pattern**: Construcción de workflows

## 📝 Próximos Pasos

Ver documentos específicos:
- [Guía de Implementación](../implementation/GUIDE.md)
- [Migración a Supabase](../migration/SUPABASE.md)
- [Alternativas de Deployment](../deployment/ALTERNATIVES.md)
