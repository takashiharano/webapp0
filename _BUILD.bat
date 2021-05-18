@echo off
cd /d %~dp0
call mvn clean package -Dappver=webapp1-1.0
pause
