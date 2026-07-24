# Diagnostica y exporta la cadena de certificados TLS presentada por el SMTP configurado.
# Uso: ejecutar en la laptop donde falla el login (PowerShell normal, sin admin).
#   powershell -ExecutionPolicy Bypass -File diagnosticar-cert-smtp.ps1

$hostname = "smtp.gmail.com"
$port = 587
$salida = "$PSScriptRoot\smtp-root-cert.cer"

$client = New-Object System.Net.Sockets.TcpClient($hostname, $port)
$stream = $client.GetStream()
$reader = New-Object System.IO.StreamReader($stream)
$writer = New-Object System.IO.StreamWriter($stream)
$writer.NewLine = "`r`n"
$writer.AutoFlush = $true

$reader.ReadLine() | Out-Null
$writer.WriteLine("EHLO diagnostico")
do { $line = $reader.ReadLine() } while ($line -and $line.Length -ge 4 -and $line[3] -eq '-')
$writer.WriteLine("STARTTLS")
$reader.ReadLine() | Out-Null

$sslStream = New-Object System.Net.Security.SslStream($stream, $false, { param($s,$c,$ch,$e) $true })
$sslStream.AuthenticateAsClient($hostname)

$certRemoto = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2($sslStream.RemoteCertificate)
$chain = New-Object System.Security.Cryptography.X509Certificates.X509Chain
$chain.Build($certRemoto) | Out-Null

Write-Host ""
Write-Host "Cadena de certificados recibida al conectar a ${hostname}:${port}" -ForegroundColor Cyan
Write-Host "-----------------------------------------------------------"
foreach ($el in $chain.ChainElements) {
    Write-Host ("Subject: " + $el.Certificate.Subject)
    Write-Host ("Issuer : " + $el.Certificate.Issuer)
    Write-Host ""
}

$raiz = $chain.ChainElements[$chain.ChainElements.Count - 1].Certificate
[System.IO.File]::WriteAllBytes($salida, $raiz.Export("Cert"))
Write-Host "Certificado raiz exportado a: $salida" -ForegroundColor Green
Write-Host ""
Write-Host "Si el 'Subject' de la raiz NO dice algo de Google/GTS (ej. dice el nombre" -ForegroundColor Yellow
Write-Host "de un antivirus tipo Norton/Kaspersky/ESET/Mail Shield), ese es el problema." -ForegroundColor Yellow

$sslStream.Close()
$client.Close()
