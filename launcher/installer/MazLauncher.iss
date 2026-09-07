#define MyAppName "MazLauncher"
#define MyAppVersion "0.3.0"
#define MyAppPublisher "MazClient"
#define MyAppExeName "MazLauncher.exe"

[Setup]
AppId={{8F27A55B-3B16-4A08-A56D-6E90D8596944}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={localappdata}\Programs\MazLauncher
DefaultGroupName=MazLauncher
DisableProgramGroupPage=yes
OutputDir=..\..\dist
OutputBaseFilename=MazLauncher-Setup-{#MyAppVersion}
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
PrivilegesRequired=lowest
UninstallDisplayName=MazLauncher

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Additional shortcuts:"; Flags: unchecked

[Files]
Source: "..\publish\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\MazLauncher"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\MazLauncher"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Launch MazLauncher"; Flags: nowait postinstall skipifsilent
