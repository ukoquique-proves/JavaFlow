#!/bin/bash

echo "🚀 Iniciando JavaFlow..."
echo "========================="

# Verificar que run.sh existe
if [ ! -f "./run.sh" ]; then
    echo "❌ No se encontró run.sh en el directorio actual"
    exit 1
fi

# Hacer ejecutable run.sh si no lo es
if [ ! -x "./run.sh" ]; then
    echo "🔧 Haciendo run.sh ejecutable..."
    chmod +x ./run.sh
fi

echo "📋 Delegando a run.sh para manejo inteligente de puertos..."
echo ""

# Delegar a run.sh que tiene toda la lógica inteligente
exec ./run.sh
