#!/bin/bash

# Configuration
SRC_DIR="src"
LIB_DIR="lib"
BUILD_DIR="build"
OUTPUT_JAR="framework.jar"

# Construire le classpath avec TOUS les JARs du dossier lib
CP=""
for jar in "$LIB_DIR"/*.jar; do
    if [ -f "$jar" ]; then
        if [ -z "$CP" ]; then
            CP="$jar"
        else
            CP="$CP:$jar"
        fi
    fi
done

if [ -z "$CP" ]; then
    echo "Erreur: Aucun JAR trouvé dans $LIB_DIR"
    exit 1
fi

echo "CP trouvé : $CP"

# Nettoyer
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/classes"

# Trouver tous les fichiers Java récursivement
JAVA_FILES=$(find "$SRC_DIR" -name "*.java")

if [ -z "$JAVA_FILES" ]; then
    echo "Erreur: Aucun fichier Java trouvé dans $SRC_DIR"
    exit 1
fi

# Compiler
echo "Compilation en cours..."
javac -parameters -cp "$CP" -d "$BUILD_DIR/classes" $JAVA_FILES

if [ $? -eq 0 ]; then
    echo "Compilation réussie !"

    # Créer le JAR
    echo "Création du JAR..."
    jar cvf "$OUTPUT_JAR" -C "$BUILD_DIR/classes" .

    echo "[OK] JAR créé : $OUTPUT_JAR"

    echo ""
    echo "Contenu du JAR :"
    jar tf "$OUTPUT_JAR" | nl -ba
else
    echo "[ERREUR] Échec de la compilation"
    exit 1
fi