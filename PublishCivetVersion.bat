@echo off
set JAVA_HOME=C:\Progra~1\Java\jdk1.8.0_191
set ANT_HOME=E:\ant
set PATH=%PATH%;%ANT_HOME%\bin
echo Publishing Civet Version: %1
E:
del \EclipseJava\Civet\Civet.jar 2> nul
del \EclipseJava\Civet\CivetLocal.jar 2> nul
del \EclipseJava\Civet\Civet.zip 2> nul
cd \EclipseJava\Civet


rem Build Published Version
echo %1 > src\edu\clemson\lph\civet\res\Version.txt
echo %1 > bin\edu\clemson\lph\civet\res\Version.txt
call ant -f build.xml
cd \EclipseJava\Civet\bin
rem Copy JPedal classes from storage if we need to package.
jar -cfm ../Civet.jar ..\Civet.MF edu/clemson/lph edu/clemson/lph/civet edu/clemson/lph/controls edu/clemson/lph/db edu/clemson/lph/dialogs edu/clemson/lph/mailman edu/clemson/lph/pdfgen com/cai/webservice
if errorlevel 1 goto jarooops
copy \EclipseJava\Civet\Civet.jar \EclipseJava\Civet\Publish\Civet\Civet.jar >nul
cd \EclipseJava\Civet\Publish
jar -cfM ../Civet.zip Civet
if errorlevel 1 goto zipoops
cd \EclipseJava\Civet\Publish\Civet
jar -cfM ../../CivetLib.zip lib
if errorlevel 1 goto zipoops

rem Build the Local Version
echo Publishing Civet Version: %1 Local
cd \EclipseJava\Civet
echo %1 Local > src\edu\clemson\lph\civet\res\Version.txt
echo %1 Local > bin\edu\clemson\lph\civet\res\Version.txt
call ant -f build.xml
cd \EclipseJava\Civet\bin
jar -cfm ../CivetLocal.jar ..\Civet.MF edu/clemson/lph edu/clemson/lph/civet edu/clemson/lph/controls edu/clemson/lph/db edu/clemson/lph/dialogs edu/clemson/lph/mailman edu/clemson/lph/pdfgen com/cai/webservice
cd \EclipseJava\Civet
if errorlevel 1 goto jaroops
echo Completed Publishing CivetLocal.jar to \EclipseJava\Civet



goto done
:zipoops
echo Error in Zip
goto exit
:jaroops
echo Error in Jar
goto exit
:done
cd \EclipseJava\Civet
echo Completed Publishing Civet.jar to \EclipseJava\Civet
:exit

