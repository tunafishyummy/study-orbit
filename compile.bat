@echo off
cd /d "%~dp0"
if not exist out mkdir out
if exist sources.txt del sources.txt
for /f "delims=" %%f in ('dir /s /b src\*.java') do call :writeSource "%%f"
javac -encoding UTF-8 -d out @sources.txt
set BUILD_RESULT=%errorlevel%
if exist sources.txt del sources.txt
if not "%BUILD_RESULT%"=="0" (
  echo Build failed
  exit /b 1
)
echo Build successful
exit /b 0

:writeSource
set "sourcePath=%~1"
echo "%sourcePath:\=/%">>sources.txt
exit /b 0
