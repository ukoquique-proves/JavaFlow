# Migración a Supabase - Guía Completa

## 📋 Índice

1. [Visión General](#visión-general)
2. [Diferencias Clave](#diferencias-clave)
3. [Cambios de Configuración](#cambios-de-configuración)
4. [Puntos Críticos](#puntos-críticos)
5. [Migración Paso a Paso](#migración-paso-a-paso)
6. [Row-Level Security](#row-level-security)
7. [Limitaciones del Plan Gratuito](#limitaciones-del-plan-gratuito)
8. [Problemas Comunes y Soluciones](#problemas-comunes-y-soluciones)

## 🎯 Visión General

Supabase es una alternativa open-source a Firebase que proporciona:
- PostgreSQL gestionado
- Autenticación integrada
- APIs REST y GraphQL automáticas
- Row-Level Security (RLS)
- Realtime subscriptions

### ¿Cuándo migrar a Supabase?

✅ **Migrar cuando:**
- Necesitas escalabilidad automática
- Quieres autenticación gestionada
- Requieres APIs REST automáticas
- Necesitas realtime sin configuración

❌ **NO migrar si:**
- Tienes <100 usuarios
- Necesitas control total de la BD
- Usas funciones PostgreSQL avanzadas
- Requieres >60 conexiones simultáneas (plan free)

## 🔄 Diferencias Clave: Local vs Supabase

### Arquitectura Local

```
┌─────────────────┐
│  Spring Boot    │
│  Application    │
└────────┬────────┘
         │ JDBC directo
         ↓
┌─────────────────┐
│  PostgreSQL     │
│  (localhost)    │
│  Puerto 5432    │
└─────────────────┘
```

### Arquitectura con Supabase

```
┌─────────────────┐
│  Spring Boot    │
│  Application    │
└────────┬────────┘
         │ JDBC + SSL
         │ Connection Pooling
         ↓
┌─────────────────────────────┐
│  Internet                   │
└────────┬────────────────────┘
         │
         ↓
┌─────────────────────────────┐
│  Supabase Cloud             │
│  ┌─────────────────────┐    │
│  │  Connection Pooler  │    │
│  │  (PgBouncer)        │    │
│  └──────────┬──────────┘    │
│             ↓                │
│  ┌─────────────────────┐    │
│  │  PostgreSQL 15      │    │
│  │  + Extensions       │    │
│  │  + RLS              │    │
│  └─────────────────────┘    │
│                              │
│  ┌─────────────────────┐    │
│  │  Supabase Auth      │    │
│  └─────────────────────┘    │
│                              │
│  ┌─────────────────────┐    │
│  │  REST API           │    │
│  │  (PostgREST)        │    │
│  └─────────────────────┘    │
└─────────────────────────────┘
```

## ⚙️ Cambios de Configuración

### 1. application-supabase.yml

```yaml
spring:
  datasource:
    # URL de conexión con SSL
    url: jdbc:postgresql://db.xxxxxxxxxxxx.supabase.co:5432/postgres?sslmode=require
    username: postgres
    password: ${SUPABASE_DB_PASSWORD}
    
    # Connection pooling crítico para Supabase
    hikari:
      maximum-pool-size: 10  # Máximo 10 para plan free (60 total)
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      
  jpa:
    properties:
      hibernate:
        # Desactivar auto-schema en producción
        hbm2ddl.auto: validate
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          # Batch processing para reducir conexiones
          batch_size: 20
        order_inserts: true
        order_updates: true

# Configuración específica de Supabase
supabase:
  url: https://xxxxxxxxxxxx.supabase.co
  anon-key: ${SUPABASE_ANON_KEY}
  service-role-key: ${SUPABASE_SERVICE_ROLE_KEY}
  
  # Usar Supabase Auth en lugar de JWT propio
  auth:
    enabled: true
    jwt-secret: ${SUPABASE_JWT_SECRET}
```

### 2. Dependencias Adicionales (pom.xml)

```xml
<!-- Cliente de Supabase para Java -->
<dependency>
    <groupId>io.supabase</groupId>
    <artifactId>supabase-kt</artifactId>
    <version>1.3.2</version>
</dependency>

<!-- PostgreSQL JDBC con soporte SSL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
</dependency>

<!-- Connection pooling mejorado -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.0.1</version>
</dependency>
```

### 3. Configuración de SSL

```java
@Configuration
@Profile("supabase")
public class SupabaseDataSourceConfig {
    
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        config.setJdbcUrl(supabaseUrl);
        config.setUsername(username);
        config.setPassword(password);
        
        // SSL obligatorio
        config.addDataSourceProperty("ssl", "true");
        config.addDataSourceProperty("sslmode", "require");
        
        // Connection pooling optimizado
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // Validación de conexiones
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        
        return new HikariDataSource(config);
    }
}
```

## ⚠️ Puntos Críticos

### 1. Límite de Conexiones

**Problema**: Plan gratuito permite solo 60 conexiones simultáneas.

**Solución**:
```java
// Configurar pool pequeño
hikari.maximum-pool-size: 10  // Por instancia

// Usar connection pooler de Supabase
// URL: db.xxxx.supabase.co:6543 (puerto 6543, no 5432)
url: jdbc:postgresql://db.xxxx.supabase.co:6543/postgres?sslmode=require

// Cerrar conexiones explícitamente
@Transactional(timeout = 30)
public void operation() {
    // Operación rápida
}
```

### 2. SSL Obligatorio

**Problema**: Conexiones sin SSL son rechazadas.

**Solución**:
```yaml
# SIEMPRE incluir sslmode=require
url: jdbc:postgresql://db.xxxx.supabase.co:5432/postgres?sslmode=require

# O en código
properties.setProperty("ssl", "true");
properties.setProperty("sslmode", "require");
```

### 3. Latencia de Red

**Problema**: Latencia de 50-200ms vs <1ms local.

**Solución**:
```java
@Configuration
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        // Cachear queries frecuentes
        return new CaffeineCacheManager("workflows", "bots", "users");
    }
}

// Usar caché en servicios
@Cacheable(value = "workflows", key = "#id")
public Workflow findById(Long id) {
    return workflowRepository.findById(id).orElse(null);
}
```

### 4. Row-Level Security (RLS)

**Problema**: Supabase recomienda RLS, pero complica queries desde Java.

**Solución**: Ver sección [Row-Level Security](#row-level-security)

### 5. Funciones PostgreSQL Personalizadas

**Problema**: Algunas extensiones pueden no estar disponibles.

**Verificar disponibilidad**:
```sql
-- Verificar extensiones disponibles
SELECT * FROM pg_available_extensions;

-- Extensiones comunes en Supabase
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";
```

### 6. Migraciones de Schema

**Problema**: `hbm2ddl.auto=update` puede fallar con RLS.

**Solución**:
```yaml
# Usar Flyway o Liquibase
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

```sql
-- V1__initial_schema.sql
CREATE TABLE IF NOT EXISTS workflows (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    -- ...
);

-- Aplicar RLS después
ALTER TABLE workflows ENABLE ROW LEVEL SECURITY;
```

## 🔐 Row-Level Security (RLS)

### ¿Qué es RLS?

Row-Level Security permite filtrar filas automáticamente basado en el usuario actual.

### Problema con Spring Boot

Spring Boot usa una única conexión de servicio, no sabe qué "usuario" está haciendo la query.

### Solución 1: Service Role Key (Bypass RLS)

```java
// Usar service_role key para bypass RLS
@Configuration
public class SupabaseConfig {
    
    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;
    
    // Esta key bypasea RLS
    // Implementar seguridad en capa de aplicación
}
```

**Pros**: Simple, funciona igual que local
**Contras**: No aprovecha RLS de Supabase

### Solución 2: SET LOCAL para RLS

```java
@Service
public class SecureWorkflowService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<Workflow> findByUser(Long userId) {
        // Setear contexto de usuario para RLS
        entityManager.createNativeQuery(
            "SET LOCAL request.jwt.claim.sub = :userId"
        ).setParameter("userId", userId.toString()).executeUpdate();
        
        // Ahora las queries respetan RLS
        return workflowRepository.findAll();
    }
}
```

### Políticas RLS Ejemplo

```sql
-- Política: usuarios solo ven sus propios workflows
CREATE POLICY "Users can view own workflows"
ON workflows
FOR SELECT
USING (created_by = current_setting('request.jwt.claim.sub')::bigint);

-- Política: usuarios solo pueden insertar con su ID
CREATE POLICY "Users can insert own workflows"
ON workflows
FOR INSERT
WITH CHECK (created_by = current_setting('request.jwt.claim.sub')::bigint);
```

### Recomendación

Para JavaFlow, **usar Service Role Key** inicialmente y manejar seguridad en Spring Security:

```java
@PreAuthorize("hasRole('ADMIN') or @workflowSecurity.isOwner(#id, authentication)")
public Workflow getWorkflow(Long id) {
    return workflowRepository.findById(id).orElseThrow();
}
```

## 📊 Limitaciones del Plan Gratuito

| Recurso | Límite Free | Límite Pro | Notas |
|---------|-------------|------------|-------|
| **Conexiones DB** | 60 | 200 | Crítico para apps Java |
| **Storage** | 500 MB | 100 GB | Para archivos/media |
| **Bandwidth** | 5 GB | 250 GB | Tráfico mensual |
| **Auth Users** | 50,000 | Ilimitado | Usuarios registrados |
| **Realtime** | 200 concurrentes | 500 | WebSocket connections |
| **Edge Functions** | 500,000 invocaciones | 2M | Serverless functions |
| **Pausa automática** | Después 7 días inactivo | No | Plan free solo |

### Estrategias para Plan Free

1. **Connection Pooling Agresivo**
```yaml
hikari:
  maximum-pool-size: 5  # Muy conservador
  leak-detection-threshold: 60000  # Detectar leaks
```

2. **Caché Agresivo**
```java
@Cacheable(value = "workflows", unless = "#result == null")
public List<Workflow> findAll() {
    return workflowRepository.findAll();
}
```

3. **Batch Operations**
```java
// Insertar en batch para reducir conexiones
@Transactional
public void saveAll(List<Message> messages) {
    messageRepository.saveAll(messages);  // Batch insert
}
```

4. **Async Processing**
```java
@Async
public CompletableFuture<Void> processMessages(List<Message> messages) {
    // Procesar en background sin mantener conexión
}
```

## 🚀 Migración Paso a Paso

### Paso 1: Crear Proyecto en Supabase

1. Ir a https://supabase.com
2. Crear nuevo proyecto
3. Guardar credenciales:
   - Database URL
   - anon key
   - service_role key
   - JWT secret

### Paso 2: Exportar Schema Local

```bash
# Exportar schema de PostgreSQL local
pg_dump -h localhost -U postgres -d javaflow_db --schema-only > schema.sql

# Exportar datos (opcional)
pg_dump -h localhost -U postgres -d javaflow_db --data-only > data.sql
```

### Paso 3: Importar a Supabase

```bash
# Conectar a Supabase
psql "postgresql://postgres:[PASSWORD]@db.xxxx.supabase.co:5432/postgres?sslmode=require"

# Importar schema
\i schema.sql

# Importar datos
\i data.sql
```

### Paso 4: Configurar Variables de Entorno

```bash
# .env.supabase
SUPABASE_DB_URL=jdbc:postgresql://db.xxxx.supabase.co:5432/postgres?sslmode=require
SUPABASE_DB_PASSWORD=your-password
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=eyJhbGc...
SUPABASE_SERVICE_ROLE_KEY=eyJhbGc...
SUPABASE_JWT_SECRET=your-jwt-secret
```

### Paso 5: Actualizar application.yml

```yaml
spring:
  profiles:
    active: supabase
  config:
    import: optional:file:.env.supabase[.properties]
```

### Paso 6: Probar Conexión

```java
@SpringBootTest
@ActiveProfiles("supabase")
class SupabaseConnectionTest {
    
    @Autowired
    private DataSource dataSource;
    
    @Test
    void testConnection() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5));
            
            // Verificar SSL
            assertTrue(conn.getMetaData().getURL().contains("sslmode=require"));
        }
    }
}
```

### Paso 7: Migrar Datos Gradualmente

```java
@Service
public class DataMigrationService {
    
    @Autowired
    private DataSource localDataSource;
    
    @Autowired
    @Qualifier("supabaseDataSource")
    private DataSource supabaseDataSource;
    
    public void migrateWorkflows() {
        // Leer de local
        List<Workflow> workflows = localRepository.findAll();
        
        // Escribir a Supabase
        supabaseRepository.saveAll(workflows);
    }
}
```

### Paso 8: Configurar Monitoreo

```java
@Component
public class ConnectionPoolMonitor {
    
    @Scheduled(fixedRate = 60000)
    public void monitorPool() {
        HikariDataSource ds = (HikariDataSource) dataSource;
        HikariPoolMXBean pool = ds.getHikariPoolMXBean();
        
        log.info("Active connections: {}", pool.getActiveConnections());
        log.info("Idle connections: {}", pool.getIdleConnections());
        log.info("Total connections: {}", pool.getTotalConnections());
        
        // Alertar si >80% de capacidad
        if (pool.getActiveConnections() > 8) {
            log.warn("Connection pool near limit!");
        }
    }
}
```

## 🐛 Problemas Comunes y Soluciones

### Problema 1: "Connection refused"

**Causa**: SSL no configurado

**Solución**:
```yaml
url: jdbc:postgresql://db.xxxx.supabase.co:5432/postgres?sslmode=require
```

### Problema 2: "Too many connections"

**Causa**: Pool size muy grande

**Solución**:
```yaml
hikari:
  maximum-pool-size: 5
  leak-detection-threshold: 60000
```

### Problema 3: "Timeout acquiring connection"

**Causa**: Todas las conexiones ocupadas

**Solución**:
```java
// Reducir timeout de transacciones
@Transactional(timeout = 30)

// Usar async para operaciones largas
@Async
public void longOperation() { }
```

### Problema 4: "Permission denied for table"

**Causa**: RLS bloqueando queries

**Solución**:
```sql
-- Desactivar RLS temporalmente
ALTER TABLE workflows DISABLE ROW LEVEL SECURITY;

-- O usar service_role key que bypasea RLS
```

### Problema 5: "Function does not exist"

**Causa**: Extensión no instalada

**Solución**:
```sql
-- Instalar extensión necesaria
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
```

### Problema 6: Latencia alta

**Causa**: Distancia geográfica al datacenter

**Solución**:
```java
// Implementar caché
@Cacheable("workflows")
public List<Workflow> findAll() { }

// Usar batch operations
@Transactional
public void saveAll(List<Entity> entities) { }

// Considerar cambiar región de Supabase
// O usar CDN para assets estáticos
```

## 📈 Monitoreo en Supabase

### Dashboard de Supabase

1. **Database**: Métricas de conexiones, queries
2. **API**: Requests, latencia
3. **Auth**: Usuarios activos, sign-ups
4. **Storage**: Uso de espacio

### Alertas Recomendadas

```java
@Component
public class SupabaseHealthCheck {
    
    @Scheduled(fixedRate = 300000) // Cada 5 min
    public void checkHealth() {
        // Verificar conexiones
        if (activeConnections > 50) {
            alertService.send("Supabase connections high!");
        }
        
        // Verificar latencia
        long latency = measureLatency();
        if (latency > 500) {
            alertService.send("High latency: " + latency + "ms");
        }
    }
}
```

## ✅ Checklist de Migración

- [ ] Proyecto Supabase creado
- [ ] Credenciales guardadas en .env
- [ ] Schema exportado de local
- [ ] Schema importado a Supabase
- [ ] SSL configurado en JDBC URL
- [ ] Connection pooling optimizado (<10)
- [ ] Caché implementado
- [ ] Transacciones con timeout
- [ ] RLS configurado o desactivado
- [ ] Extensiones necesarias instaladas
- [ ] Datos migrados y verificados
- [ ] Tests de integración pasando
- [ ] Monitoreo configurado
- [ ] Plan de rollback documentado

## 🔄 Plan de Rollback

Si algo falla, volver a local:

```yaml
# Cambiar perfil
spring:
  profiles:
    active: local
```

```bash
# Restaurar backup local
psql -U postgres -d javaflow_db < backup.sql
```

## 📚 Recursos Adicionales

- [Supabase Java Client](https://github.com/supabase-community/supabase-kt)
- [PostgreSQL JDBC SSL](https://jdbc.postgresql.org/documentation/ssl/)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Supabase Limits](https://supabase.com/docs/guides/platform/org-based-billing#usage-based-billing-for-compute)

## 🎯 Conclusión

**Recomendación**: 
- Empezar con PostgreSQL local
- Migrar a Supabase solo cuando tengas >100 usuarios activos
- Considerar alternativas como Railway o Render si necesitas más control

**Ventajas de Supabase**:
- Setup rápido
- Autenticación incluida
- APIs REST automáticas
- Realtime sin configuración

**Desventajas para Java**:
- Límite de conexiones bajo
- Latencia de red
- RLS complica integración
- Menos control sobre PostgreSQL
