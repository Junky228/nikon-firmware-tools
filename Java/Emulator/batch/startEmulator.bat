@echo off
echo Starting emulator UI
java -cp "%~dp0/FrEmulator.jar";"%~dp0/lib/commons-io-2.1.jar";"%~dp0/lib/commons-lang3-3.1.jar";"%~dp0/lib/xstream-1.4.2.jar";"%~dp0/lib/jacksum.jar" com.nikonhacker.gui.EmulatorUI %*
echo Done.
pause