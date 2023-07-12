@echo off

powershell -Command "Start-Process cmd -Verb RunAs -ArgumentList '/c taskkill /f /im haproxy.exe'"
