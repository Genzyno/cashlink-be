$targetDirs = @("d:\JOHN\My_Projects\Git_Code\CashLink\cashlink-be", "d:\JOHN\My_Projects\Git_Code\CashLink\cashlink-fe")

function Replace-CasePreserving {
    param([string]$Text)

    # We do a replacement using a MatchEvaluator
    $evaluator = [System.Text.RegularExpressions.MatchEvaluator] {
        param($match)
        $val = $match.Value
        if ($val -cmatch "^[A-Z]+$") {
            return "CASHLINK"
        } elseif ($val -cmatch "^[A-Z][a-z]+[A-Z][a-z]+$") {
            return "CashLink"
        } elseif ($val -cmatch "^[a-z]+$") {
            return "cashlink"
        } else {
            return "CashLink"
        }
    }
    
    return [System.Text.RegularExpressions.Regex]::Replace($Text, "(?i)cashlink", $evaluator)
}

foreach ($dir in $targetDirs) {
    if (-not (Test-Path $dir)) { continue }

    # 1. Replace content in files
    $files = Get-ChildItem -Path $dir -Recurse -File -Exclude *.log, *.class, *.jar, *.exe, *.zip, node_modules, target, dist, .git

    foreach ($file in $files) {
        if ($file.FullName -match "node_modules|target|dist|\.git|\.angular") { continue }
        
        $content = Get-Content $file.FullName -Raw
        if ($content -match "(?i)cashlink") {
            $newContent = Replace-CasePreserving -Text $content
            Set-Content -Path $file.FullName -Value $newContent -NoNewline -Encoding UTF8
            Write-Host "Updated file content: $($file.FullName)"
        }
    }

    # 2. Rename files (bottom-up)
    $allItems = Get-ChildItem -Path $dir -Recurse | Sort-Object -Property @{Expression={$_.FullName.Length}; Descending=$true}
    foreach ($item in $allItems) {
        if ($item.FullName -match "node_modules|target|dist|\.git|\.angular") { continue }
        
        if ($item.Name -match "(?i)cashlink") {
            $newName = Replace-CasePreserving -Text $item.Name
            $newPath = Join-Path -Path $item.Parent.FullName -ChildPath $newName
            Rename-Item -Path $item.FullName -NewName $newName
            Write-Host "Renamed: $($item.FullName) -> $newPath"
        }
    }
}
