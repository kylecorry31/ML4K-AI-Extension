if not exist "_build" mkdir _build
cd _build
if exist "dist" cd dist && del *.aix && cd ..
if not exist "appinventor-sources" git clone https://github.com/mit-cml/appinventor-sources
robocopy ..\src\com\ appinventor-sources\appinventor\components\src\com\ /e
cd appinventor-sources/appinventor
cmd.exe /c ant extensions
cd ..\..
if not exist "dist" mkdir dist
xcopy appinventor-sources\appinventor\components\build\extensions\com.kylecorry.ml4k.aix dist\ /Y
rename dist\com.kylecorry.ml4k.aix ML4K.aix
