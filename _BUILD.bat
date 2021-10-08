@echo off
cd /d %~dp0
call mvn clean package -Dappver=webapp0-1.0
pause
