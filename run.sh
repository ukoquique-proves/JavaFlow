#!/bin/bash

echo "🚀 JavaFlow - Ejecución Rápida"
echo "=================================="

# Configurar Maven
export MAVEN_HOME=${MAVEN_HOME:-/home/uko/FLOWABLE/JavaFlow-/apache-maven-3.9.5}
export PATH=$MAVEN_HOME/bin:$PATH

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "❌ Java no encontrado. Instale Java 17+"
    exit 1
fi

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven no encontrado. Usando versión portable..."
    if [ ! -d "apache-maven-3.9.5" ]; then
        echo "📥 Descargando Maven..."
        curl -O https://archive.apache.org/dist/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz
        tar -xzf apache-maven-3.9.5-bin.tar.gz
    fi
    export MAVEN_HOME=$PWD/apache-maven-3.9.5
    export PATH=$MAVEN_HOME/bin:$PATH
fi

echo "✅ Java y Maven configurados"

# Port conflict resolution is now handled by Java (PortConflictResolver)
# The application will automatically detect and gracefully shutdown existing instances
# via the /actuator/shutdown endpoint

PORT=8080
echo ""
echo "ℹ️  Port conflict resolution is handled automatically by the application"
echo "   If port $PORT is in use, the existing instance will be gracefully shut down"
echo ""

# Ejecutar con perfil de desarrollo (H2)
echo ""
echo "🔨 Ejecutando JavaFlow con base de datos H2..."
echo "📊 Console H2: http://localhost:$PORT/h2-console (usuario: sa)"
echo "🌐 UI Web: http://localhost:$PORT"
echo "🔗 REST API: http://localhost:$PORT/api/v1/workflows"
echo "📈 Actuator: http://localhost:$PORT/actuator/health"
echo ""
echo "Presione Ctrl+C para detener"
echo ""

mvn spring-boot:run -Dspring.profiles.active=dev
