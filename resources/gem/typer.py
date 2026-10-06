import csv
import json
from pathlib import Path

INPUT = "gem_types.csv"
OUTPUT_DIR = Path("gem")

SCALES = [0.05, 0.154, 0.368, 0.687, 1.0]
WEIGHTS = [0.72, 0.86, 0.92, 0.98, 1.0]


OUTPUT_DIR.mkdir(exist_ok=True)


with open(INPUT, "r", encoding="utf-8") as f:
    reader = csv.reader(f, delimiter=";")

    for row in reader:
        if not row or len(row) < 2:
            continue

        gem_name = row[0].strip()

        # Пропускаем заголовок
        if gem_name == "gem_name":
            continue

        types = row[1].split(",")

        str_list = []

        for type_name in types:
            type_name = type_name.strip()

            if not type_name:
                continue

            for i in range(1, 8):
                str_list.append(f"fbased:{type_name}_{i}")

        result = {
            "feature": {
                "type": "fbased:fossil_s",
                "config": {
                    "str": str_list,
                    "proc": [
                        f"fbased:gem/{gem_name}"
                    ],
                    "scales": SCALES,
                    "weights": WEIGHTS
                }
            },
            "placement": []
        }

        output_file = OUTPUT_DIR / f"{gem_name}.json"

        with open(output_file, "w", encoding="utf-8") as out:
            json.dump(
                result,
                out,
                ensure_ascii=False,
                indent=2
            )

print(f"Создано файлов: {len(list(OUTPUT_DIR.glob('*.json')))}")