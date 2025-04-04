# JavaFX Visualization Setup and Run Script

# Check if JavaFX directory exists
$JAVAFX_PATH = ".\visualization\javafx-sdk-21.0.6\lib"
if (-not (Test-Path $JAVAFX_PATH)) {
    Write-Host "JavaFX SDK not found at $JAVAFX_PATH" -ForegroundColor Red
    Write-Host "Please download JavaFX SDK 21 from https://gluonhq.com/products/javafx/" -ForegroundColor Yellow
    Write-Host "Extract it to $JAVAFX_PATH" -ForegroundColor Yellow
    exit 1
}

# Create bin directory
Write-Host "Creating bin directory..."
mkdir -Force bin | Out-Null

# Step 1: Compile the core project files first
Write-Host "Compiling core files..." -ForegroundColor Cyan
javac -d bin src\adaptivehuffman\*.java

if (-not $?) {
    Write-Host "Failed to compile core files!" -ForegroundColor Red
    exit 1
}

# Step 2: Fix the visualization file and import structure
Write-Host "Fixing visualization file..." -ForegroundColor Cyan

# Create visualization output directory in bin
mkdir -Force bin\adaptivehuffman | Out-Null

# Fix the imports in HuffmanTreeVisualizer.java
$FILE_PATH = ".\visualization\adaptivehuffman\HuffmanTreeVisualizer.java"

if (-not (Test-Path $FILE_PATH)) {
    Write-Host "Visualization file not found at $FILE_PATH" -ForegroundColor Red
    exit 1
}

# Read the file content
$content = Get-Content $FILE_PATH -Raw

# Remove the redundant imports 
$content = $content -replace "import adaptivehuffman\.Node;", "// No need to import - same package"
$content = $content -replace "import adaptivehuffman\.HuffmanTree;", "// No need to import - same package"
$content = $content -replace "import adaptivehuffman\.Encoder;", "// No need to import - same package"
$content = $content -replace "import adaptivehuffman\.\*;", "// No need to import - same package"

# Write it back to the file
$content | Set-Content $FILE_PATH

# Step 3: Compile the visualization with JavaFX
Write-Host "Compiling visualization..." -ForegroundColor Cyan
javac --module-path "$JAVAFX_PATH" --add-modules javafx.controls,javafx.graphics,javafx.base -d bin -cp bin $FILE_PATH

if (-not $?) {
    Write-Host "Visualization compilation failed!" -ForegroundColor Red
    exit 1
}

# Step 4: Run the visualization
Write-Host "Running visualization..." -ForegroundColor Green
java --module-path "$JAVAFX_PATH" --add-modules javafx.controls,javafx.graphics,javafx.base -cp bin adaptivehuffman.HuffmanTreeVisualizer
