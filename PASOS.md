# 📋 PASOS para Reconstruir JavaFlow

> Nota: Este documento es una **guía histórica de reconstrucción mínima** del proyecto para entornos limpios. La arquitectura actual incluye capa de bots (Ports & Adapters), eventos de dominio y métricas vía Actuator; use `README.md` y `docs/` para la documentación vigente.

## ✅ Estado Actual

JavaFlow está **ejecutándose correctamente** en http://localhost:8080 con:
- Spring Boot 3.2.1
- Java 17
- Base de datos H2 (en memoria)

---

## 🚀 Pasos Realizados

### 1. **Instalación de Dependencias del Sistema**

```bash
# Instalar Java 17
sudo apt update
sudo apt install -y openjdk-17-jdk

# Verificar instalación
java -version
javac -version
```

### 2. **Descargar e Instalar Maven**

```bash
# Descargar Maven 3.9.5
curl -O https://archive.apache.org/dist/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz

# Extraer
tar -xzf apache-maven-3.9.5-bin.tar.gz

# Configurar variables de entorno
export MAVEN_HOME=/home/uko/FLOWABLE/JavaFlow-/apache-maven-3.9.5
export PATH=$MAVEN_HOME/bin:$PATH

# Verificar
mvn -version
```

### 3. **Crear Estructura del Proyecto**

```bash
# Crear directorios
mkdir -p src/main/java/com/javaflow
mkdir -p src/main/resources
```

### 4. **Crear pom.xml Simplificado**

Archivo: `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/>
    </parent>
    
    <groupId>com.javaflow</groupId>
    <artifactId>javaflow</artifactId>
    <version>1.0.0</version>
    <name>JavaFlow</name>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 5. **Crear Clase Principal de la Aplicación**

Archivo: `src/main/java/com/javaflow/JavaFlowApplication.java`

```java
package com.javaflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class JavaFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(JavaFlowApplication.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "JavaFlow Demo OK";
    }

    @GetMapping("/api/status")
    public String status() {
        return "{\"status\":\"running\"}";
    }
}
```

### 6. **Crear Archivo de Configuración**

Archivo: `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: JavaFlow

server:
  port: 8080

logging:
  level:
    root: INFO
    com.javaflow: DEBUG
```

### 7. **Compilar y Ejecutar**

```bash
# Configurar Maven
export MAVEN_HOME=/home/uko/FLOWABLE/JavaFlow-/apache-maven-3.9.5
export PATH=$MAVEN_HOME/bin:$PATH

# Ejecutar la aplicación
mvn spring-boot:run
```

---

## 🌐 Verificar que Funciona

```bash
# En otra terminal
curl http://localhost:8080/
# Respuesta: JavaFlow Demo OK

curl http://localhost:8080/api/status
# Respuesta: {"status":"running"}

curl http://localhost:8080/actuator/health
# Respuesta: {"status":"UP"}
```

---

---

## ✅ PASO 1: Spring Data JPA + H2 Console (COMPLETADO)

### 1.1 Dependencias Agregadas

```xml
<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- H2 Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### 1.2 Configuración application.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:javaflow_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect

  h2:
    console:
      enabled: true
      path: /h2-console
```

### 1.3 Entidades Creadas

**User.java** - Entidad de usuario con JPA

```java
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
```

### 1.4 Repository Creado

**UserRepository.java** - Acceso a datos

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
```

### 1.5 Service Creado

**UserService.java** - Lógica de negocio

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User createUser(String username, String email, String name) { ... }
    public Optional<User> getUserByUsername(String username) { ... }
    public List<User> getAllUsers() { ... }
    public Optional<User> getUserById(Long id) { ... }
    public void deleteUser(Long id) { ... }
}
```

### 1.6 REST Controller Creado

**UserController.java** - Endpoints REST

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(...) { ... }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() { ... }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) { ... }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) { ... }
}
```

### 1.7 Verificación

✅ **Compilación**: `mvn clean compile` - EXITOSA
✅ **Ejecución**: `mvn spring-boot:run` - FUNCIONANDO
✅ **Endpoints**:
- `GET /` → "JavaFlow v1.0 - Spring Data JPA + H2 Console"
- `GET /api/status` → JSON con estado
- `GET /api/users` → Lista de usuarios (vacía inicialmente)
- `POST /api/users?username=...&email=...&name=...` → Crear usuario
- `GET /h2-console` → Consola H2 para ver base de datos

### 1.8 URLs de Acceso

- **Aplicación**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
  - Usuario: `sa`
  - Sin contraseña
  - JDBC URL: `jdbc:h2:mem:javaflow_db`

---

## 📦 Próximos Pasos para Agregar Funcionalidades

### Opción A: Agregar Flowable (Motor BPMN)

Agregar a `pom.xml`:

```xml
<dependency>
    <groupId>org.flowable</groupId>
    <artifactId>flowable-spring-boot-starter</artifactId>
    <version>7.0.0</version>
</dependency>

<dependency>
    <groupId>org.flowable</groupId>
    <artifactId>flowable-spring-boot-starter-rest</artifactId>
    <version>7.0.0</version>
</dependency>
```

### Opción B: Agregar Telegram Bot

Agregar a `pom.xml`:

```xml
<dependency>
    <groupId>org.telegram</groupId>
    <artifactId>telegrambots</artifactId>
    <version>6.8.0</version>
</dependency>

<dependency>
    <groupId>org.telegram</groupId>
    <artifactId>telegrambots-spring-boot-starter</artifactId>
    <version>6.8.0</version>
</dependency>
```

### Opción C: Agregar Base de Datos PostgreSQL

Agregar a `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Actualizar `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/javaflow_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
```

### Opción D: Agregar Vaadin (UI Web)

Agregar a `pom.xml`:

```xml
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-spring-boot-starter</artifactId>
    <version>24.3.0</version>
</dependency>
```

**Nota**: Requiere Node.js y npm instalados.

---

## 🐳 Alternativa: Usar Docker

### Crear Dockerfile

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/javaflow-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Ejecutar con Docker

```bash
# Compilar imagen
docker build -t javaflow .

# Ejecutar contenedor
docker run -p 8080:8080 javaflow
```

---

## 📝 Estructura Final del Proyecto

```
JavaFlow/
├── pom.xml
├── Dockerfile
├── src/
│   └── main/
│       ├── java/
│       │   └── com/javaflow/
│       │       └── JavaFlowApplication.java
│       └── resources/
│           └── application.yml
└── target/
    └── javaflow-1.0.0.jar
```

---

## ⚠️ Problemas Encontrados y Soluciones

### Problema 1: Vaadin tiene dependencias complejas
**Solución**: Usar versión sin Vaadin para desarrollo, agregar después cuando sea necesario.

### Problema 2: Java 17 no estaba instalado
**Solución**: `sudo apt install -y openjdk-17-jdk`

### Problema 3: Maven no estaba disponible
**Solución**: Descargar versión portable de Apache Maven

### Problema 4: Caracteres especiales en strings Java
**Solución**: Usar comillas simples en heredocs de bash

---

## 🎯 Comandos Rápidos

### Scripts Mejorados (Recomendado)

```bash
# Ejecutar con manejo inteligente de puertos
./run.sh

# Iniciar (alias de run.sh)
./start.sh

# Detener procesos JavaFlow
./stop.sh

# Ver ayuda de scripts
./scripts-help.sh
```

### Comandos Maven Directos

```bash
# Configurar entorno
export MAVEN_HOME=/home/uko/FLOWABLE/JavaFlow-/apache-maven-3.9.5
export PATH=$MAVEN_HOME/bin:$PATH

# Compilar
mvn clean compile

# Ejecutar
mvn spring-boot:run

# Empaquetar
mvn clean package -DskipTests

# Ver logs
mvn spring-boot:run -X

# Limpiar
mvn clean
```

---

## 📞 URLs de Acceso

- **Aplicación**: http://localhost:8080
- **API Status**: http://localhost:8080/api/status
- **Health Check**: http://localhost:8080/actuator/health
- **Métricas**: http://localhost:8080/actuator/metrics (disponible)

---

## ✅ Checklist de Verificación

- [x] Java 17 instalado: `java -version`
- [x] Maven configurado: `mvn -version`
- [x] Proyecto compilado: `mvn clean compile`
- [x] Aplicación ejecutándose: `mvn spring-boot:run`
- [x] Endpoint `/` responde: `curl http://localhost:8080/`
- [ ] Endpoint `/api/status` responde: `curl http://localhost:8080/api/status` (Endpoint removido para habilitar UI)
- [x] Health check funciona: `curl http://localhost:8080/actuator/health`

---

**Última actualización**: 25 de Octubre, 2025
**Estado**: ✅ Funcionando correctamente
