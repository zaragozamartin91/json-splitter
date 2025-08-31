 [string]$PomFile = "pom.xml"

# Load pom.xml as XML
[xml]$pom = Get-Content -Path $PomFile

# Extract the <version> value
$version = $pom.project.version

# Split version into parts
$parts = $version.Split('.')

# Ensure at least major.minor exist
if ($parts.Count -lt 2) {
    Write-Error "Version must be in format <major>.<minor>[.<patch>...]"
    exit 1
}

# Parse major and minor
$major = [int]$parts[0]
$minor = [int]$parts[1]

# Increment minor
$minor++

# Rebuild version string
$newVersion = "$major.$minor"

# Update version
$pom.project.version = $NewVersion

# Save changes (preserves UTF-8 encoding)
$pom.Save((Resolve-Path $PomFile))

Write-Output "Updated <version> to $NewVersion in $PomFile"
