# Guía de Implementación JavaFlow

## 🚀 Instalación y Configuración

### Paso 1: Requisitos Previos

```bash
# Verificar Java
java -version  # Debe ser 17+

# Verificar Maven
mvn -version   # Debe ser 3.8+

# Verificar PostgreSQL
psql --version # Debe ser 14+
```

### Paso 2: Configurar PostgreSQL Local

```bash
# Crear base de datos
createdb javaflow_db

# Crear usuario
psql -d javaflow_db
CREATE USER javaflow WITH PASSWORD 'javaflow123';
GRANT ALL PRIVILEGES ON DATABASE javaflow_db TO javaflow;
```

### Paso 3: Clonar y Compilar

```bash
cd JavaFlow
mvn clean compile
```

### Paso 4: Configurar Variables de Entorno

```bash
# .env
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
TELEGRAM_BOT_USERNAME=your_bot_username
JWT_SECRET=your-secret-key-min-256-bits
```

Opcional (habilitar/deshabilitar bots desde configuración YML):

```yaml
# src/main/resources/application.yml
javaflow:
  bot:
    telegram:
      enabled: true  # poner en false para deshabilitar el listener en desarrollo
```

### Paso 5: Ejecutar

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Acceder a: http://localhost:8080

#### Opción rápida (desarrollo con H2, sin PostgreSQL)

```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

Esta opción utiliza H2 en memoria y no requiere tener PostgreSQL instalado.

## 🔭 Observabilidad (Metrics)

- Health: `http://localhost:8080/actuator/health`
- Metrics (JSON): `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`

Referencia: `src/main/java/com/javaflow/monitoring/MetricsService.java`

## 📚 Documentación Completa

Ver archivos en `/docs` para más detalles.

Sugerido:

- `docs/implementation/ARCH_REPORT.md` (reporte técnico de arquitectura)
- `docs/migration/SUPABASE.md` (perfil y guía para Supabase)

---

## 🛠️ Troubleshooting

### 1) Puerto 8080 en uso

Síntoma: el arranque falla y los scripts muestran que el puerto está ocupado.

Soluciones:

```bash
# Ver procesos en 8080
lsof -i :8080

# Detener con scripts del proyecto
./stop.sh

# Forzar liberación (con cuidado)
kill -9 $(lsof -t -i:8080)
```

Recomendado: usar `./run.sh` (manejo inteligente de puertos) o responder "y" cuando `./start.sh` pregunte.

### 2) No puedo acceder a H2 Console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:javaflow_db`
- Usuario: `sa` (sin contraseña)
- Perfil: usar `-Dspring.profiles.active=dev` para entorno H2.

### 3) No veo métricas en `/actuator/metrics`

Verificar:

```yaml
# src/main/resources/application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

Luego ejecutar operaciones para generar métricas (p. ej., navegar la UI o enviar mensajes de bot) y consultar:

```bash
curl -s http://localhost:8080/actuator/metrics | jq -r '.names[]' | grep javaflow || true
```

### 4) Telegram bot no responde

Checklist:

- Variable de entorno `TELEGRAM_BOT_TOKEN` y `TELEGRAM_BOT_USERNAME` definidas.
- En `application.yml`: `javaflow.bot.telegram.enabled: true`.
- Revisión de logs: `logs/javaflow.log` o consola.
- Recuerde que el adaptador publica eventos y `BotService` los procesa; no hay dependencia directa.

### 5) Fallos de compilación Maven

Pasos:

```bash
mvn -v             # Verificar Java 17 y Maven >= 3.8
mvn clean compile  # Reconstruir
```

Si persiste, borrar `target/` y reconstruir. Revise conflictos de versiones duplicadas en `pom.xml`.
