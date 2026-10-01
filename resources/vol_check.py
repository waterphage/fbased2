from pathlib import Path

base_file = Path("tthr_1_1.nbt")
base_size = base_file.stat().st_size

for file in sorted(Path(".").glob("*.nbt")):
    ratio = ( base_size/file.stat().st_size) ** (1 / 3)
    if(ratio<1.0):
        print(f'Map.entry(new Identifier("fbased","{file.stem}"), {ratio:.6f}f),')