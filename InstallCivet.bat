@echo off
rem Setup Directories
SET CIVET_HOME="E:\Documents\Civet"
mkdir %CIVET_HOME%\CivetInbox
mkdir %CIVET_HOME%\CivetOutbox
mkdir %CIVET_HOME%\CivetToBeFiled
mkdir %CIVET_HOME%\CivetToBeMailedOut
mkdir %CIVET_HOME%\CivetToBeMailedErrors
mkdir %CIVET_HOME%\Lib


copy .\Lib\* %CIVET_HOME%\Lib
move %CIVET_HOME%\Lib\Civet.jar %CIVET_HOME%
copy .\*.ico %CIVET_HOME%

copy .\*.txt %CIVET_HOME%

echo Press Any Key to Edit Email Settings in CivetConfig.txt
pause

echo IMPORTANT: Microsoft's license prohibits distribution of its free
echo    JDBC Driver.  Until all SQL is removed you must go to 
echo	  http://msdn.microsoft.com/en-us/sqlserver/aa937724.aspx
echo   	  Click on Download the Microsoft JDBC Driver 4.0 for SQL Server
echo	  Make sure ALL script blocking is turned off!!!
echo	  Click Download
echo	  Select sqljdbc_4.0.????_enu.tar.gz
echo	  Save File
echo	  Open with WinZip, WinRar, etc.
echo	  Extract the file sqljdbc_4.0\enu\sqljdbc4.jar
echo	  Place in Civet\Lib folder
pause
