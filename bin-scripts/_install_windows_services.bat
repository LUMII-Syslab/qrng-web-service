@echo off
:: we use wget.exe to download servicemanager.exe, since the github URL points to a HTML redirect that curl.exe cannot process;
:: we use curl.exe (available in Windows 10) to download wget.exe;

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.

:: since servicemanager does not support paths containing "..", we shall find out SERVICE_DIR by ourselves
cd %DIRNAME%\..
set SERVICE_DIR="%CD%"
cd %DIRNAME%

:: since servicemanager does not support paths containing "..", we shall find out SERVICE_DIR by ourselves
cd %DIRNAME%\..\cfg
set CFG_DIR="%CD%"
cd %DIRNAME%

if exist servicemanager.exe goto SERVICEMANAGER_OK
curl.exe -o %DIRNAME%\wget.exe https://eternallybored.org/misc/wget/1.21.3/64/wget.exe
%DIRNAME%\wget.exe -O %DIRNAME%\servicemanager.exe https://github.com/cubiclesoft/service-manager/raw/master/servicemanager.exe
:SERVICEMANAGER_OK

:: sudo in Windows (the command is after &&)
start /w powershell -Command "Start-Process cmd -Verb RunAs -ArgumentList '/k cd /d %DIRNAME% && %DIRNAME%\servicemanager.exe -pid=%SERVICE_DIR%\pid.txt install qrng-web-service %SERVICE_DIR%\NotifyFile %SERVICE_DIR%\bin\qrng-web-service.bat'"

:: sudo in Windows (the command is after &&)
start /w powershell -Command "Start-Process cmd -Verb RunAs -ArgumentList '/k cd /d %DIRNAME% && %DIRNAME%\servicemanager.exe -dir=%SERVICE_DIR%\cfg -pid=%SERVICE_DIR%\haproxy_pid.txt install qrng-haproxy %SERVICE_DIR%\NotifyFile_haproxy C:\cygwin64\opt\oqs\sbin\haproxy -V -f %CFG_DIR%\haproxy_oqs.cfg'"
