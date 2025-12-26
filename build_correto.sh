#!/bin/bash
# build_correto.sh - Execute na pasta RunnerHand/

echo "=== BUILD RUNNERHAND ==="
echo "Diretório atual: $(pwd)"
echo ""

# Verificar se estamos na pasta certa
if [ ! -f "RunnerHand.iml" ] && [ ! -d "lib" ]; then
    echo "❌ ERRO: Você não está na raiz do projeto RunnerHand!"
    echo "Vá para: cd ~/IdeaProjects/RunnerHand"
    exit 1
fi

# Encontrar arquivos .java
JAVA_FILES=$(find . -name "*.java" -type f)

if [ -z "$JAVA_FILES" ]; then
    echo "❌ Nenhum arquivo .java encontrado!"
    echo "Procurando em subpastas..."
    find . -type d -name "*src*"
    exit 1
fi

echo "✅ Encontrados $(echo "$JAVA_FILES" | wc -l) arquivos .java"
echo "Primeiros 5:"
echo "$JAVA_FILES" | head -5
echo ""

# Criar/limpar bin
rm -rf bin
mkdir -p bin

# Compilar
echo "🔄 Compilando..."
for java_file in $JAVA_FILES; do
    echo "  Compilando: $java_file"
    javac -cp "lib/jnativehook-2.2.2.jar" -d bin "$java_file" 2>/dev/null
done

# Verificar se compilou
if [ -f "bin/Main.class" ]; then
    echo ""
    echo "✅ COMPILAÇÃO BEM-SUCEDIDA!"
    echo "Classes em: bin/"
    echo ""
    echo "Para criar o JAR:"
    echo "  jar cfe RunnerHand.jar Main -C bin ."
else
    echo ""
    echo "⚠ AVISO: Main.class não encontrado em bin/"
    echo "Tentando compilação direta..."
    javac -cp "lib/jnativehook-2.2.2.jar" -d bin $(find . -name "*.java")
fi



