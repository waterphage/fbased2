import os
import json

INPUT_DIR = "check"
OUTPUT_DIR = "out"

os.makedirs(OUTPUT_DIR, exist_ok=True)

for filename in os.listdir(INPUT_DIR):
    if not filename.lower().endswith(".png"):
        continue

    mineral = os.path.splitext(filename)[0].removesuffix("_raw")
    print(f'"fbased:{mineral}_raw"')