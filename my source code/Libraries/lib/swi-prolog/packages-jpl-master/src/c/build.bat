@echo off

rem JAVA_HOME must (already) be set to the root dir of a recent Sun Java SDK
rem PL_HOME must (already) be set to the root dir of a recent SWI-Prolog installation

set DEFINES=/D_REENTRANT /DWIN32 /D_WINDOWS /D__SWI_PROLOG__ /D__SWI_EMBEDDED__ /D_CRT_SECURE_NO_WARNINGS
set JVM_INC=/I "%JAVA_HOME%\include" /I "%JAVA_HOME%\include/win32"
set PL_INC=/I "%PL_HOME%\include"
set JVM_LIB="%JAVA_HOME%\lib\jvm.lib"
set PL_LIB="%PL_HOME%\lib\libswipl.lib"
set PTHREAD_LIB="%PL_HOME%\lib/pthreadVC2.lib"
set OPT_FLAGS=/OD /Ob2 /Oi /Ot /GT /GL /OPT:NOREF /OPT:ICF /LTCG

CL.EXE /W3 /nologo /MD /LD %OPT_FLAGS% %DEFINES% %JVM_INC% %PL_INC% %JVM_LIB% %PL_LIB% %PTHREAD_LIB% jpl.c
pause

