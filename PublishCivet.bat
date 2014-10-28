@echo off
E:
cd \EclipseJava\Civet\bin
if [%1] NEQ [local] rmdir /S /Q edu\clemson\lph\civet\addons
jar -cfm ../Civet.jar ..\Civet.MF edu/clemson/lph edu/clemson/lph/civet edu/clemson/lph/controls edu/clemson/lph/db edu/clemson/lph/dialogs edu/clemson/lph/mailman edu/clemson/lph/pdfgen com/cai/webservice
if errorlevel 1 goto jarooops
goto done
:jaroops
echo Error in Jar
:done
cd \EclipseJava\Civet
echo Completed Publishing Civet.jar to \EclipseJava\Civet
pause