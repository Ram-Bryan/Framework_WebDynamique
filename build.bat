@echo off
setlocal enabledelayedexpansion

REM Configuration
set SRC_DIR=src
set LIB_DIR=lib
set BUILD_DIR=build
set OUTPUT_JAR=framework.jar

REM Construire le classpath avec TOUS les JARs
set CP=
for %%f in ("%LIB_DIR%\*.jar") do (
    if "!CP!"=="" (
        set CP=%%f
    ) else (
        set CP=!CP!;%%f
    )
)

if not defined CP (
    echo Erreur: Aucun JAR trouve dans %LIB_DIR%
    exit /b 1
)

echo Tous les JARs trouves dans le classpath

REM Nettoyer
if exist %BUILD_DIR% rd /s /q %BUILD_DIR%
mkdir %BUILD_DIR%\classes 2>nul

REM Trouver tous les fichiers Java recursivement
set JAVA_FILES=
for /r "%SRC_DIR%" %%f in (*.java) do (
    set JAVA_FILES=!JAVA_FILES! "%%f"
)

if not defined JAVA_FILES (
    echo Erreur: Aucun fichier Java trouve dans %SRC_DIR%
    exit /b 1
)

REM Compiler avec TOUS les JARs dans le classpath
echo Compilation en cours avec classpath : %CP%
javac -cp "%CP%" -d %BUILD_DIR%\classes !JAVA_FILES!

if %errorlevel% equ 0 (
    echo Compilation reussie !
    
    REM Creer le JAR
    echo Creation du JAR...
    jar cvf %OUTPUT_JAR% -C %BUILD_DIR%\classes .
    
    echo [OK] JAR cree : %OUTPUT_JAR%
    
    REM Verifier le contenu
    echo.
    echo Contenu du JAR :
    jar tf %OUTPUT_JAR% | findstr /n "." | findstr "^[1-9]:"
) else (
    echo [ERREUR] Echec de la compilation
    exit /b 1
)

endlocal