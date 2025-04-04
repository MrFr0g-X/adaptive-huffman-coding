# Script to test file encoding and decoding with verification

Write-Host "Adaptive Huffman File Processing Test" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

# Create a test file if it doesn't exist
$testFile = "test_sample.txt"
if (-not (Test-Path $testFile)) {
    Write-Host "Creating test file..." -ForegroundColor Yellow
    $content = "This is a test file for adaptive Huffman coding.`r`n"
    $content += "It contains some repeated text to enable compression.`r`n" 
    $content += "The more repetition, the better the compression.`r`n"
    $content += "AAAAAAABBBBCCCCDDDD - this part should compress well."
    $content | Out-File -FilePath $testFile
}

# Create output file paths
$encodedFile = "test_encoded.bin"
$decodedFile = "test_decoded.txt"

# Step 1: Encode the file
Write-Host "`nStep 1: Encoding test file" -ForegroundColor Green
java -cp bin adaptivehuffman.FileProcessor encode $testFile $encodedFile

# Step 2: Decode the file
Write-Host "`nStep 2: Decoding file" -ForegroundColor Green
java -cp bin adaptivehuffman.FileProcessor decode $encodedFile $decodedFile

# Step 3: Verify the result
Write-Host "`nStep 3: Verifying results" -ForegroundColor Green
$original = Get-Content $testFile -Raw
$decoded = Get-Content $decodedFile -Raw

if ($original -eq $decoded) {
    Write-Host "SUCCESS: Files match perfectly!" -ForegroundColor Green
} else {
    Write-Host "ERROR: Files are different!" -ForegroundColor Red
    
    # Display byte-by-byte comparison of the first few differences
    $originalBytes = [System.IO.File]::ReadAllBytes($testFile)
    $decodedBytes = [System.IO.File]::ReadAllBytes($decodedFile)
    
    Write-Host "First few differences:" -ForegroundColor Yellow
    $diffCount = 0
    for ($i = 0; $i -lt [Math]::Min($originalBytes.Length, $decodedBytes.Length); $i++) {
        if ($originalBytes[$i] -ne $decodedBytes[$i]) {
            Write-Host ("Byte {0}: Original=0x{1:X2}, Decoded=0x{2:X2}" -f $i, $originalBytes[$i], $decodedBytes[$i])
            $diffCount++
            if ($diffCount -ge 10) { break }
        }
    }
    
    # Check if file sizes are different
    if ($originalBytes.Length -ne $decodedBytes.Length) {
        Write-Host ("File sizes differ: Original={0} bytes, Decoded={1} bytes" -f $originalBytes.Length, $decodedBytes.Length)
    }
}

Write-Host "`nTest complete." -ForegroundColor Cyan
