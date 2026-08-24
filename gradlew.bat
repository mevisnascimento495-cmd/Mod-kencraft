@echo off
setlocal
where gradle >nul 2>&1
if errorlevel 1 (
  echo Gradle 8.10.2 was not found on PATH.
  exit /b 1
)
gradle %*
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%
