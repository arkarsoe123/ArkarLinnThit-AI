@echo off
setlocal
set GRADLE_VERSION=9.3.1
set DIST=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin
set ROOT=%DIST%\gradle-%GRADLE_VERSION%
if not exist "%ROOT%\bin\gradle.bat" (
  if not exist "%DIST%" mkdir "%DIST%"
  powershell -NoProfile -Command "Invoke-WebRequest -UseBasicParsing https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip -OutFile %DIST%\gradle.zip"
  powershell -NoProfile -Command "Expand-Archive -Force %DIST%\gradle.zip %DIST%"
)
call "%ROOT%\bin\gradle.bat" %*
endlocal
