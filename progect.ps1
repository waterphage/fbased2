$root = Get-Location
$output = Join-Path $root "project_dump.txt"

$exclude = @(
    ".git",
    ".gradle",
    "build",
    "run",
    "out",
    ".idea"
)

Remove-Item $output -ErrorAction SilentlyContinue

Get-ChildItem -Path $root -Recurse -File |
    Where-Object {
        $relative = $_.FullName.Substring($root.Path.Length).TrimStart('\')
        -not ($exclude | Where-Object { $relative -like "$_*" })
    } |
    Sort-Object FullName |
    ForEach-Object {
        $relative = $_.FullName.Substring($root.Path.Length).TrimStart('\')

        Add-Content $output "`r`n"
        Add-Content $output "============================================================"
        Add-Content $output "FILE: $relative"
        Add-Content $output "============================================================"
        
        try {
            Get-Content $_.FullName -Raw -ErrorAction Stop |
                Add-Content $output
        }
        catch {
            Add-Content $output "[Не удалось прочитать файл]"
        }
	Write-Host "делаю: $FullName"
    }

Write-Host "Готово: $output"