#define MyAppName "SDRERC"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "RENIEC"
#define MyAppJar "SDRERC-V2.jar"
#define MyAppLauncher "run-v2.bat"

[Setup]
AppId={{A70F7F99-3F78-4B4B-A9A5-8A4180E9FA4D}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\SDRERC
DefaultGroupName=SDRERC
DisableProgramGroupPage=yes
OutputDir=output
OutputBaseFilename=SDRERC-V2-Setup
Compression=lzma
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=admin

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Dirs]
Name: "{app}\config"
Name: "{app}\lib"
Name: "{app}\logs"

[Files]
Source: "..\SDRERC-V2\{#MyAppJar}"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\SDRERC-V2\{#MyAppLauncher}"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\SDRERC-V2\config\*"; DestDir: "{app}\config"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\SDRERC-V2\lib\*.jar"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs skipifsourcedoesntexist

[Icons]
Name: "{autoprograms}\SDRERC"; Filename: "{app}\{#MyAppLauncher}"; WorkingDir: "{app}"
Name: "{autodesktop}\SDRERC"; Filename: "{app}\{#MyAppLauncher}"; WorkingDir: "{app}"

[Run]
Filename: "{app}\{#MyAppLauncher}"; Description: "Ejecutar SDRERC"; Flags: postinstall shellexec skipifsilent unchecked
