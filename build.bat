@echo off
echo Building project...

REM Make sure the output directory exists
if not exist out (
    mkdir out
)

REM Compile Java files
for /R src\main\java %%f in (*.java) do (
    javac -d out %%f
)

REM Copy resource files
xcopy /E /Y src\main\resources\* out\

echo Running project...
java -cp out your.package.Main
