#!/bin/bash

echo "🛑 Deteniendo JavaFlow..."
echo ""
echo "💡 Tip: Para un apagado más graceful, use:"
echo "   curl -X POST http://localhost:8080/actuator/shutdown"
echo ""
echo "Continuando con terminación de procesos..."
echo ""

# Función para encontrar procesos JavaFlow específicos
find_javaflow_processes() {
    # Buscar procesos Java que contengan "javaflow" o "spring-boot" en su línea de comandos
    ps aux | grep -i java | grep -E "(javaflow|spring-boot|JavaFlowApplication)" | grep -v grep | awk '{print $2}'
}

# Función para encontrar procesos Java usando puerto 8080
find_java_on_port_8080() {
    if command -v lsof &> /dev/null; then
        lsof -i :8080 2>/dev/null | grep java | awk '{print $2}' | sort -u
    else
        echo ""
    fi
}

# Buscar procesos JavaFlow específicos
JAVAFLOW_PIDS=$(find_javaflow_processes)
PORT_PIDS=$(find_java_on_port_8080)

# Combinar PIDs únicos
ALL_PIDS=$(echo "$JAVAFLOW_PIDS $PORT_PIDS" | tr ' ' '\n' | sort -u | tr '\n' ' ')

if [ -z "$ALL_PIDS" ]; then
    echo "ℹ️  No se encontraron procesos JavaFlow ejecutándose"
    exit 0
fi

echo "🔍 Procesos JavaFlow encontrados:"
for pid in $ALL_PIDS; do
    if ps -p $pid > /dev/null 2>&1; then
        local cmd=$(ps -p $pid -o args= 2>/dev/null | cut -c1-80)
        echo "  - PID $pid: $cmd"
    fi
done

echo ""
echo "🔪 Terminando procesos JavaFlow..."

# Terminar procesos gracefully
for pid in $ALL_PIDS; do
    if ps -p $pid > /dev/null 2>&1; then
        echo "  Terminando PID $pid..."
        kill $pid 2>/dev/null
    fi
done

# Esperar a que se detengan
echo "⏳ Esperando terminación graceful..."
sleep 3

# Verificar cuáles siguen corriendo
REMAINING_PIDS=""
for pid in $ALL_PIDS; do
    if ps -p $pid > /dev/null 2>&1; then
        REMAINING_PIDS="$REMAINING_PIDS $pid"
    fi
done

# Forzar terminación si es necesario
if [ -n "$REMAINING_PIDS" ]; then
    echo "⚠️  Algunos procesos no se detuvieron. Forzando terminación..."
    for pid in $REMAINING_PIDS; do
        if ps -p $pid > /dev/null 2>&1; then
            echo "  Forzando terminación de PID $pid..."
            kill -9 $pid 2>/dev/null
        fi
    done
    sleep 1
fi

# Verificación final
FINAL_CHECK=$(find_javaflow_processes)
PORT_CHECK=$(find_java_on_port_8080)
FINAL_PIDS=$(echo "$FINAL_CHECK $PORT_CHECK" | tr ' ' '\n' | sort -u | tr '\n' ' ')

if [ -z "$FINAL_PIDS" ]; then
    echo "✅ JavaFlow detenido correctamente"
    
    # Verificar que el puerto 8080 esté libre
    if command -v lsof &> /dev/null; then
        if ! lsof -i :8080 &> /dev/null; then
            echo "✅ Puerto 8080 liberado"
        else
            echo "⚠️  Puerto 8080 aún en uso por otros procesos"
        fi
    fi
    
    exit 0
else
    echo "❌ No se pudieron detener todos los procesos JavaFlow:"
    for pid in $FINAL_PIDS; do
        if ps -p $pid > /dev/null 2>&1; then
            local cmd=$(ps -p $pid -o args= 2>/dev/null | cut -c1-60)
            echo "  - PID $pid: $cmd"
        fi
    done
    exit 1
fi
