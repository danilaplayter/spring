@echo off
gradlew.bat clean test jacocoTestReport --no-daemon --stacktrace
pause
