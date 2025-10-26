#!/bin/bash

echo "🚀 Instalando JavaFlow - Sistema de Workflow"

# Verificar Java
echo "📋 Verificando Java..."
if ! command -v java &> /dev/null; then
    echo "❌ Java no está instalado. Instale Java 17+ primero."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | sed 's/^1\.//' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java $JAVA_VERSION detectado. Se requiere Java 17+."
    exit 1
fi
echo "✅ Java $JAVA_VERSION detectado"

# Instalar Maven si no existe
echo "📋 Verificando Maven..."
if ! command -v mvn &> /dev/null; then
    echo "📦 Instalando Maven..."
    if command -v apt &> /dev/null; then
        sudo apt update && sudo apt install -y maven
    else
        echo "📥 Descargando Maven..."
        wget https://archive.apache.org/dist/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz
        tar -xzf apache-maven-3.9.5-bin.tar.gz
        export MAVEN_HOME=$PWD/apache-maven-3.9.5
        export PATH=$MAVEN_HOME/bin:$PATH
        echo "✅ Maven instalado temporalmente"
    fi
fi
echo "✅ Maven disponible"

# Instalar PostgreSQL si no existe (opcional)
echo "📋 Verificando PostgreSQL..."
if ! command -v psql &> /dev/null; then
    echo "📦 PostgreSQL no detectado. Usando H2 para desarrollo..."
    echo "💡 Para producción, instale PostgreSQL: sudo apt install postgresql postgresql-contrib"
fi

# Compilar proyecto
echo "🔨 Compilando proyecto..."
export MAVEN_HOME=${MAVEN_HOME:-/usr/share/maven}
export PATH=$MAVEN_HOME/bin:$PATH

mvn clean compile -Dspring.profiles.active=dev
if [ $? -ne 0 ]; then
    echo "❌ Error en compilación"
    exit 1
fi
echo "✅ Proyecto compilado exitosamente"

# Crear base de datos H2 si no existe PostgreSQL
if ! command -v psql &> /dev/null; then
    echo "💾 Usando base de datos H2 (en memoria) para desarrollo"
    echo "📊 Console H2 disponible en: http://localhost:8080/h2-console"
    echo "   Usuario: sa"
    echo "   Sin contraseña"
fi

echo ""
echo "🎯 Para ejecutar el proyecto:"
echo "   mvn spring-boot:run -Dspring.profiles.active=dev"
echo ""
echo "🌐 Acceder a:"
echo "   - UI Web: http://localhost:8080"
echo "   - H2 Console: http://localhost:8080/h2-console"
echo "   - Actuator: http://localhost:8080/actuator/health"
echo ""
echo "✅ ¡Instalación completada!"
