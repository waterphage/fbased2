import nbtlib
from nbtlib import tag
from pathlib import Path

def optimize_structure(path):
    print(f"⏳ Обработка: {path}")
    structure = nbtlib.load(path)
    blocks = structure["blocks"]

    max_x = max(b["pos"][0] for b in blocks)
    max_y = max(b["pos"][1] for b in blocks)
    max_z = max(b["pos"][2] for b in blocks)
    min_x = min(b["pos"][0] for b in blocks)
    min_y = min(b["pos"][1] for b in blocks)
    min_z = min(b["pos"][2] for b in blocks)

    # Смещение к (0,0,0)
    for b in blocks:
        b["pos"][0] -= min_x
        b["pos"][1] -= min_y
        b["pos"][2] -= min_z

    # Обновление
    structure["blocks"] = tag.List[nbtlib.Compound](blocks)
    structure["size"] = tag.List[tag.Int]([
        max_x - min_x + 1,
        max_y - min_y + 1,
        max_z - min_z + 1
    ])

    # Сохранение
    out_path = path.parent / "opt"
    out_path.mkdir(exist_ok=True)
    structure.save(out_path / f"{path.stem}.nbt")
    print(f"✅ Готово: {path.stem}.nbt")

if __name__ == "__main__":
    for path in Path(".").glob("*.nbt"):
        optimize_structure(path)
