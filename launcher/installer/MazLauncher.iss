#define MyAppName "MazLauncher"
#define MyAppVersion "0.6.29"
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
OutputDir=output
OutputBaseFilename=MazClient-1.7.15-MazLauncher-0.6.29-Setup
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
UninstallDisplayIcon={app}\MazLauncher.exe
ArchitecturesInstallIn64BitMode=x64compatible

[Files]
Source: "..\MazLauncher\bin\Release\net8.0-windows\win-x64\publish\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\MazLauncher"; Filename: "{app}\MazLauncher.exe"
Name: "{autodesktop}\MazLauncher"; Filename: "{app}\MazLauncher.exe"; Tasks: desktopicon

[Tasks]
Name: "desktopicon"; Description: "Create a &desktop shortcut"; GroupDescription: "Additional icons:"; Flags: unchecked

[Run]
Filename: "{app}\MazLauncher.exe"; Description: "Launch MazLauncher"; Flags: nowait postinstall skipifsilent
