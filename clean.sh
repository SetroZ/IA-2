#!/bin/bash
echo "Cleaning..."

for dir in results out; do
    if [ -d "$dir" ]; then
        rm -rf "$dir"
        echo "Removed directory: $dir"
    else
        echo "Directory not found: $dir"
    fi
done
