@echo off
:: we use wget.exe to download servicemanager.exe, since the github URL points to a HTML redirect that curl.exe cannot process;
:: we use curl.exe (available in Windows 10) to download wget.exe;

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.

if exist servicemanager.exe goto SERVICEMANAGER_OK
curl.exe -o %DIRNAME%\wget.exe https://eternallybored.org/misc/wget/1.21.3/64/wget.exe
%DIRNAME%\wget.exe -O %DIRNAME%\servicemanager.exe https://github.com/cubiclesoft/service-manager/raw/master/servicemanager.exe
:SERVICEMANAGER_OK

%DIRNAME%\servicemanager.exe status qrng-haproxy
