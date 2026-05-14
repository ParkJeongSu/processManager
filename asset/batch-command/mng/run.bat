@echo off
set TARGET_DIR=%1
set TARGET_FILE=%2

echo [Time: %date% %time%] Execution Started > debug.log
echo [Debug] Argument 1 (Dir): %TARGET_DIR% >> debug.log
echo [Debug] Argument 2 (File): %TARGET_FILE% >> debug.log

:: 1. 경로로 이동 (절대 경로라면 /d 옵션으로 바로 이동)
cd /d "%TARGET_DIR%"
if %errorlevel% neq 0 (
    echo [Error] Cannot move to directory: %TARGET_DIR% >> debug.log
    exit /b 1
)

:: 2. JAR 파일 존재 확인
if not exist "%TARGET_FILE%" (
    echo [Error] File not found: %TARGET_FILE% in %cd% >> debug.log
    exit /b 1
)

:: 3. 실행
echo [Info] Running java -jar %TARGET_FILE%... >> debug.log
java -Dlogging.config=config/logback-spring.xml -jar "%TARGET_FILE%" 2>> debug.log

if %errorlevel% neq 0 (
    echo [Error] Java execution failed with code %errorlevel% >> debug.log
)