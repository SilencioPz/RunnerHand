#!/bin/bash
# run_final.sh - Executa o RunnerHand CORRETAMENTE

echo "=== RUNNERHAND LAUNCHER ==="
echo ""

# Configurações
APP_JAR="RunnerHand.jar"
JNATIVE_JAR="lib/jnativehook-2.2.2.jar"

# Verificar se os JARs existem
if [ ! -f "$APP_JAR" ]; then
    echo "❌ ERRO: $APP_JAR não encontrado!"
    echo "Compile primeiro com: ./build.sh"
    exit 1
fi

if [ ! -f "$JNATIVE_JAR" ]; then
    echo "❌ ERRO: $JNATIVE_JAR não encontrado!"
    echo "Baixe ou copie para: lib/jnativehook-2.2.2.jar"
    exit 1
fi

# 1. Extrair a biblioteca nativa do JAR do JNativeHook
echo "📦 Extraindo biblioteca nativa..."
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

# Extrai a biblioteca para Linux x86_64
unzip -q -j "$JNATIVE_JAR" \
    "com/github/kwhat/jnativehook/lib/linux/x86_64/libJNativeHook.so" \
    -d "$TEMP_DIR" 2>/dev/null

if [ -f "$TEMP_DIR/libJNativeHook.so" ]; then
    echo "✅ Biblioteca extraída para: $TEMP_DIR/"
    export LD_LIBRARY_PATH="$TEMP_DIR:$LD_LIBRARY_PATH"
    export JAVA_LIBRARY_PATH="$TEMP_DIR"
else
    echo "⚠ Não encontrada no JAR, usando método alternativo..."
    # Usa a lib que já funciona
    if [ -f "libJNativeHook-2.2.2.x86_64.so" ]; then
        cp libJNativeHook-2.2.2.x86_64.so "$TEMP_DIR/libJNativeHook.so"
        export LD_LIBRARY_PATH="$TEMP_DIR:$LD_LIBRARY_PATH"
    fi
fi

# 2. Executar
echo ""
echo "🚀 Iniciando RunnerHand..."
echo ""

java -Djava.library.path="$TEMP_DIR" \
     -cp "$APP_JAR:$JNATIVE_JAR" \
     Main
