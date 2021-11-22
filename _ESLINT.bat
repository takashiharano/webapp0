@echo off
cd /d %~dp0
cd src\main\webapp\js
call eslint *.js
cd ..\common
call eslint *.js
pause
