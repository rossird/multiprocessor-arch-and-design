@echo off
for /r %%i in (inputs\*) do (
   echo "%%i"
   java -jar ParallelSort.jar "%%i"
)