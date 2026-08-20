@echo off
setlocal
set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"
if exist "%WRAPPER_JAR%" (
  java -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%." %WRAPPER_LAUNCHER% %*
  exit /b %ERRORLEVEL%
)
echo Maven wrapper jar missing. Install Maven 3.9+ or restore .mvn/wrapper/maven-wrapper.jar
exit /b 1
