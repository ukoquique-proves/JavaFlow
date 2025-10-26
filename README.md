# JavaFlow - Sistema de Workflow tipo n8n

Sistema completo de gestión de workflows con Spring Boot, Vaadin, Flowable y PostgreSQL.

## 🎯 Características Principales

- **Backend**: Spring Boot 3.x con Flowable Engine
- **Frontend**: Vaadin 24 (UI administrativa)
- **Motor de Workflow**: Flowable BPMN Engine
- **Base de Datos**: PostgreSQL (local primero, migrable a Supabase)
- **Integraciones**: Telegram Bot API, WhatsApp Web (via whatsapp-web.js)
- **Arquitectura**: Monolito modular, fácilmente migrable a microservicios

## 📁 Estructura del Proyecto

```
JavaFlow/
├── docs/                          # Documentación completa
│   ├── architecture/              # Diagramas de arquitectura
│   ├── deployment/                # Guías de despliegue
│   └── migration/                 # Guía de migración a Supabase
├── src/main/
│   ├── java/com/javaflow/
│   │   ├── config/               # Configuraciones
│   │   ├── workflow/             # Motor de workflows
│   │   ├── bot/                  # Integraciones de bots (Ports & Adapters)
│   │   │   ├── port/             # BotPort (contratos)
│   │   │   └── adapter/          # TelegramBotAdapter, WhatsAppBotAdapter (placeholder)
│   │   ├── ui/                   # Vaadin UI
│   │   ├── service/              # Servicios de negocio
│   │   ├── repository/           # Repositorios JPA
│   │   └── model/                # Entidades y DTOs
│   └── resources/
│       ├── application.yml       # Configuración principal
│       ├── application-local.yml # Perfil local
│       ├── application-supabase.yml # Perfil Supabase
│       └── processes/            # Definiciones BPMN
└── docker/                       # Docker Compose para desarrollo

```

## 🚀 Inicio Rápido

### Instalación Automática (Recomendado)

1. **Ejecutar script de instalación**
```bash
./install.sh
```

2. **Ejecutar la aplicación**
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

3. **Acceder a la UI**
- URL: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console (usuario: sa)

### Instalación Manual

#### Prerrequisitos
- Java 17+
- Maven 3.8+ (o usar el script de instalación)

#### Desarrollo con H2 (Base de datos en memoria)
```bash
# Compilar y ejecutar
mvn clean compile spring-boot:run -Dspring.profiles.active=dev
```

#### Producción con PostgreSQL
```bash
# 1. Instalar PostgreSQL
sudo apt install postgresql postgresql-contrib

# 2. Crear base de datos
createdb javaflow_db

# 3. Ejecutar
mvn spring-boot:run -Dspring.profiles.active=local
```

## 🔧 Configuración de Desarrollo

### Credenciales de Acceso
- **Dashboard Web**: http://localhost:8080 (sin login requerido - `@PermitAll`)
- **Consola H2**: http://localhost:8080/h2-console (usuario: sa, sin contraseña)
- **API REST**: http://localhost:8080/api/v1/** (acceso público)
- **Página de Login**: http://localhost:8080/login
- **Credenciales**: `user` / `password` (cuando se requiera autenticación)

### Consideraciones de Seguridad
- **Desarrollo**: Dashboard y API accesibles sin autenticación para facilitar el desarrollo
- **Producción**: Se recomienda configurar autenticación apropiada para todas las rutas
- **API**: Los endpoints REST están configurados como públicos para integración y testing
- **Health Checks**: http://localhost:8080/actuator/health (monitoreo público)

### 💡 Gestión Automática de Puertos
- **Port Conflict Resolution**: La aplicación detecta automáticamente si el puerto 8080 está en uso y apaga gracefully la instancia existente usando Spring Boot Actuator
- **Cross-platform**: Funciona en Windows, Linux y macOS sin dependencias de scripts de shell
- **Graceful Shutdown**: Usa `/actuator/shutdown` en lugar de `kill -9` para permitir limpieza de recursos

## 🔭 Observabilidad (Metrics)

- **Actuator Health**: `http://localhost:8080/actuator/health`
- **Actuator Metrics (JSON)**: `http://localhost:8080/actuator/metrics`
- **Prometheus scrape**: `http://localhost:8080/actuator/prometheus`
- Los métricos incluyen: JVM, HTTP, DB (Hikari), cache y métricas personalizadas de JavaFlow (workflows y bots).

Referencias:
- `src/main/java/com/javaflow/monitoring/MetricsService.java`
- `src/main/resources/application.yml` (management.endpoints y metrics)

## 🤖 Capa de Abstracción de Bots (Ports & Adapters)

- Contrato común: `src/main/java/com/javaflow/bot/port/BotPort.java`
- Adaptador Telegram: `src/main/java/com/javaflow/bot/adapter/TelegramBotAdapter.java`
- Adaptador WhatsApp (placeholder): `src/main/java/com/javaflow/bot/adapter/WhatsAppBotAdapter.java`
- Orquestación: `src/main/java/com/javaflow/service/BotService.java` (inyecta `Map<String, BotPort>`)

Notas:
- El listener de Telegram publica eventos (`BotMessageReceivedEvent`) y `BotService` los procesa (sin dependencias cíclicas).


## 📊 Perfiles de Configuración

- **`dev`**: H2 en memoria, sin PostgreSQL (ideal para desarrollo)
- **`local`**: PostgreSQL local (desarrollo con datos persistentes)
- **`supabase`**: Supabase cloud (producción)

## 📚 Documentación

- [Arquitectura Completa](docs/architecture/README.md)
- [Reporte de Arquitectura (Implementación)](docs/implementation/ARCH_REPORT.md)
- [Guía de Implementación](docs/implementation/GUIDE.md)
- [Migración a Supabase](docs/migration/SUPABASE.md)
- [Alternativas Cloud](docs/deployment/ALTERNATIVES.md)
- [API Reference](docs/api/README.md)

## 🔄 Flujo de Datos

```
Cliente (Telegram/WhatsApp)
    ↓
Bot Listener (Spring Boot)
    ↓
Workflow Engine (Flowable)
    ↓
Business Logic (Services)
    ↓
PostgreSQL Database
    ↓
Response Handler
    ↓
Cliente (Respuesta)
```

## 🛠️ Stack Tecnológico

| Componente | Tecnología | Versión |
|------------|-----------|---------|
| Framework | Spring Boot | 3.2.x |
| UI | Vaadin | 24.x |
| Workflow Engine | Flowable | 7.0.x |
| Database | PostgreSQL | 14+ |
| ORM | Spring Data JPA | 3.2.x |
| Security | Spring Security | 6.2.x |
| Bot Framework | TelegramBots | 6.8.x |
| WhatsApp | whatsapp-web.js | Node bridge |

## 🔐 Seguridad

- Autenticación JWT
- RBAC (Role-Based Access Control)
- Encriptación de credenciales de bots
- Rate limiting para APIs
- Preparado para Row-Level Security (Supabase)

## 📊 Base de Datos

### Esquema Principal

- **users**: Usuarios del sistema
- **workflows**: Definiciones de flujos
- **workflow_executions**: Historial de ejecuciones
- **bot_configurations**: Configuración de bots
- **messages**: Mensajes enviados/recibidos
- **logs**: Logs de sistema

## 🌐 Despliegue

### Local (Desarrollo)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Docker
```bash
docker-compose up --build
```

### Producción (VPS)
Ver [docs/deployment/VPS.md](docs/deployment/VPS.md)

### Supabase
Ver [docs/migration/SUPABASE.md](docs/migration/SUPABASE.md)

## ⚠️ Puntos Críticos (Migración a Supabase)

1. **Conexiones SSL**: Supabase requiere SSL obligatorio
2. **Connection Pooling**: Límite de 60 conexiones en plan gratuito
3. **Row-Level Security**: Requiere refactorización de queries
4. **Funciones Nativas**: Algunas funciones PostgreSQL pueden no estar disponibles
5. **Latencia**: Considerar caché para reducir latencia de red

## 🔄 Alternativas a Supabase

1. **Railway.app**: $5/mes, PostgreSQL gestionado, Java-friendly
2. **Render.com**: Plan gratuito con PostgreSQL
3. **Fly.io**: PostgreSQL + App hosting
4. **DigitalOcean**: VPS desde $4/mes
5. **Oracle Cloud**: Always Free tier con PostgreSQL

## 📈 Roadmap

- [x] Arquitectura base
- [x] Integración Flowable
- [x] UI Vaadin
- [x] Capa de abstracción de bots (Telegram operativo básico)
- [ ] WhatsApp Web integration
- [x] Backend de métricas vía Actuator/Prometheus
- [ ] Dashboard de métricas (UI)
- [ ] Export/Import de workflows
- [ ] Marketplace de workflows

## 🤝 Contribución

Ver [CONTRIBUTING.md](CONTRIBUTING.md)

## 📄 Licencia

MIT License - Ver [LICENSE](LICENSE)

## 📞 Soporte

- Issues: GitHub Issues
- Docs: [docs/](docs/)
- Email: support@javaflow.com
