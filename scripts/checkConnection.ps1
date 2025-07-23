# Alternative method to get your public IP address = https://myexternalip.com/, https://ifconfig.me/

$otherIp = (Invoke-WebRequest ifconfig.me/ip).Content.Trim()
[String]::Concat("==my ip==", $otherIp)

$Url = "/checkConnection"
$Username = "m"
$Password = "i"
$base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${Username}:${Password}"))

$response = Invoke-RestMethod -Uri $Url -Method Get -Headers @{
    Authorization = "Basic $base64AuthInfo"
    "Content-Type" = "application/json"
}

$response