import os
import json

INPUT_DIR = "minerals"
OUTPUT_DIR = "out"

os.makedirs(OUTPUT_DIR, exist_ok=True)

for filename in os.listdir(INPUT_DIR):
    if not filename.lower().endswith(".png"):
        continue

    mineral = os.path.splitext(filename)[0].removesuffix("_raw")

    data = {
  "type": "fbased:fossil_s",
  "config": {
    "str": [
      "fbased:icos_1_1",
      "fbased:icos_1_2",
      "fbased:icos_1_3",
      "fbased:icos_1_4",
      "fbased:icos_1_5",
      "fbased:icos_1_6",
      "fbased:icos_1_7"
    ],
    "proc": [
      f"fbased:min/{mineral}"
    ],
    "scales": [0.05,0.154,0.368,0.687,1.0],
    "weights": [0.72,0.86,0.92,0.98,1.0]
  }
}

    output_path = os.path.join(
        OUTPUT_DIR,
        f"{mineral}.json"
    )

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"Создан: {output_path}")