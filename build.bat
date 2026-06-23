@echo off
setlocal enabledelayedexpansion

REM Configuration
set SRC_DIR=src
set LIB_DIR=lib
set BUILD_DIR=build
set OUTPUT_JAR=framework.jar

REM Trouver automatiquement le servlet JAR
for %%f in ("%LIB_DIR%\*.jar") do (
    set SERVLET_JAR=%%f
    goto :found
)

:found
if not defined SERVLET_JAR (
    echo Erreur: Aucun JAR servlet trouve dans %LIB_DIR%
    exit /b 1
)

echo Servlet JAR trouve : %SERVLET_JAR%

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

REM Compiler
echo Compilation en cours...
javac -cp "%SERVLET_JAR%" -d %BUILD_DIR%\classes !JAVA_FILES!

if %errorlevel% equ 0 (
    echo Compilation reussie !
    
    REM Creer le JAR
    echo Creation du JAR...
    jar cvf %OUTPUT_JAR% -C %BUILD_DIR%\classes .
    
    echo [OK] JAR cree : %OUTPUT_JAR%
    
    REM Verifier le contenu (sans les libs)
    echo.
    echo Contenu du JAR :
    jar tf %OUTPUT_JAR% | findstr /n "." | findstr "^[1-9]:"
) else (
    echo [ERREUR] Echec de la compilation
    exit /b 1
)

endlocal