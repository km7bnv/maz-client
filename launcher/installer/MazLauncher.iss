#define MyAppName "MazLauncher"
#define MyAppVersion "0.6.20"
#define MyAppPublisher "MazClient"
#define MyAppExeName "MazLauncher.exe"
#define MyAppId "{8F27A55B-3B16-4A08-A56D-6E90D8596944}"

[Setup]
AppId={{8F27A55B-3B16-4A08-A56D-6E90D8596944}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={localappdata}\Programs\MazLauncher
DefaultGroupName=MazLauncher
DisableProgramGroupPage=yes
UsePreviousAppDir=no
OutputDir=..\..\dist
OutputBaseFilename=MazLauncher-Setup-{#MyAppVersion}
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
PrivilegesRequired=lowest
UninstallDisplayName=MazLauncher
UninstallDisplayIcon={app}\Assets\MazLauncher.ico
SetupIconFile=..\MazLauncher\Assets\MazLauncher.ico
CloseApplications=yes
RestartApplications=no

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Additional shortcuts:"; Flags: unchecked

[Files]
Source: "..\publish\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\MazLauncher\Assets\MazLauncher.ico"; DestDir: "{app}\Assets"; DestName: "MazLauncher.ico"; Flags: ignoreversion

[Icons]
Name: "{autoprograms}\MazLauncher"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\Assets\MazLauncher.ico"; IconIndex: 0
Name: "{autodesktop}\MazLauncher"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\Assets\MazLauncher.ico"; IconIndex: 0; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Launch MazLauncher"; Flags: nowait postinstall skipifsilent
Filename: "{app}\{#MyAppExeName}"; Flags: nowait; Check: WizardSilent

[Code]
const
  UninstallKey = 'Software\Microsoft\Windows\CurrentVersion\Uninstall\{#MyAppId}_is1';

function FindPreviousUninstaller(var Uninstaller: String): Boolean;
var
  Fallback: String;
begin
  Result :=
    RegQueryStringValue(HKCU, UninstallKey, 'UninstallString', Uninstaller) or
    RegQueryStringValue(HKLM, UninstallKey, 'UninstallString', Uninstaller) or
    RegQueryStringValue(HKCU64, UninstallKey, 'UninstallString', Uninstaller) or
    RegQueryStringValue(HKLM64, UninstallKey, 'UninstallString', Uninstaller) or
    RegQueryStringValue(HKCU32, UninstallKey, 'UninstallString', Uninstaller) or
    RegQueryStringValue(HKLM32, UninstallKey, 'UninstallString', Uninstaller);

  if not Result then
  begin
    Fallback := ExpandConstant('{localappdata}\Programs\MazLauncher\unins000.exe');
    if FileExists(Fallback) then
    begin
      Uninstaller := Fallback;
      Result := True;
    end;
  end;
end;

procedure StopRunningLauncher();
var
  ResultCode: Integer;
begin
  Exec(ExpandConstant('{cmd}'), '/C taskkill /IM MazLauncher.exe /F >nul 2>&1', '', SW_HIDE, ewWaitUntilTerminated, ResultCode);
end;

function RemovePreviousVersion(): Boolean;
var
  Uninstaller, Params: String;
  ResultCode: Integer;
begin
  Result := True;
  StopRunningLauncher();

  if FindPreviousUninstaller(Uninstaller) then
  begin
    StringChangeEx(Uninstaller, '"', '', True);
    Params := '/VERYSILENT /SUPPRESSMSGBOXES /NORESTART';
    if not Exec(Uninstaller, Params, '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
    begin
      MsgBox('MazLauncher could not remove the previous version automatically. Close MazLauncher and run setup again.', mbError, MB_OK);
      Result := False;
    end;
  end;
end;

function PrepareToInstall(var NeedsRestart: Boolean): String;
begin
  if RemovePreviousVersion() then
    Result := ''
  else
    Result := 'Previous MazLauncher installation could not be removed.';
end;
