@Echo OFF
mkdir c:\Temp\Animator >NUL
del /F /Q C:\Temp\Animator\*.jar >NUL 
del /F /Q C:\Temp\Animator\*.bat >NUL
del /F /Q /S C:\Temp\Animator\doc >NUL

REM Copy the latest build into temp
echo .log > c:\Temp\excludeFiles.txt
echo Shortcut >> c:\Temp\excludeFiles.txt
echo animationEvents >> c:\Temp\excludeFiles.txt
echo run-local.bat >> c:\Temp\excludeFiles.txt
xcopy "V:\projects\applications\(Ongoing) - World Wind Suite\Animator\Builds\Current\*" c:\Temp\Animator /Y /Q /S /EXCLUDE:c:\Temp\excludeFiles.txt
del /F c:\Temp\excludeFiles.txt

c:
cd c:\temp\Animator
c:\temp\Animator\run.bat