#define MyAppName "MazLauncher"
#define MyAppVersion "0.4.4"
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
UninstallDisplayIcon={app}\{#MyAppExeName}
SetupIconFile=..\MazLauncher\Assets\MazLauncher.ico
CloseApplications=yes
RestartApplications=no

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Additional shortcuts:"; Flags: unchecked

[Files]
Source: "..\publish\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\MazLauncher"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\MazLauncher"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Launch MazLauncher"; Flags: nowait postinstall skipifsilent

[Code]
const
  UninstallKey = 'Software\Microsoft\Windows\CurrentVersion\Uninstall\{#MyAppId}_is1';

function FindPreviousUninstaller(var Uninstaller: String): Boolean;
begin
  Result :=
    RegQueryStringValue(HKCU, UninstallKey, 'UninstallString', Uninstaller) or
    RegQueryStringValue(HKLM, UninstallKey, 'UninstallString', Uninstaller) or
    RegQueryStringValue(HKCU64, UninstallKey, 'UninstallString', Uninstaller) or
    RegQueryStringValue(HKLM64, UninstallKey, 'UninstallString', Uninstaller) or
    RegQueryStringValue(HKCU32, UninstallKey, 'UninstallString', Uninstaller) or
    RegQueryStringValue(HKLM32, UninstallKey, 'UninstallString', Uninstaller);
end;

function RemovePreviousVersion(): Boolean;
var
  Uninstaller: String;
  ResultCode: Integer;
  Params: String;
begin
  Result := True;

  if not FindPreviousUninstaller(Uninstaller) then
    exit;

  Uninstaller := RemoveQuotes(Uninstaller);
  if not FileExists(Uninstaller) then
    exit;

  Params := '/VERYSILENT /SUPPRESSMSGBOXES /NORESTART';
  Log('Removing previous MazLauncher installation: ' + Uninstaller);

  if Exec(Uninstaller, Params, '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
  begin
    if ResultCode <> 0 then
    begin
      MsgBox('The old MazLauncher installation could not be removed automatically. Exit code: ' + IntToStr(ResultCode), mbError, MB_OK);
      Result := False;
    end;
  end
  else
  begin
    if ShellExec('runas', Uninstaller, Params, '', SW_SHOWNORMAL, ewWaitUntilTerminated, ResultCode) then
    begin
      if ResultCode <> 0 then
      begin
        MsgBox('The old MazLauncher installation could not be removed. Exit code: ' + IntToStr(ResultCode), mbError, MB_OK);
        Result := False;
      end;
    end
    else
    begin
      MsgBox('MazLauncher needs permission to remove the old installation before upgrading.', mbError, MB_OK);
      Result := False;
    end;
  end;
end;

function PrepareToInstall(var NeedsRestart: Boolean): String;
begin
  Result := '';
  if not RemovePreviousVersion() then
    Result := 'Could not remove the previous MazLauncher installation.';
end;
