import csv
import json
from pathlib import Path

INPUT = "gem_distribution.csv"
OUTPUT_DIR = Path("wall")


def calc_pass(weights, index):
    remaining = sum(weights[index:])
    return weights[index] / remaining


rocks = {}

with open(INPUT, "r", encoding="utf-8") as f:
    reader = csv.reader(f, delimiter=";")

    for row in reader:
        if not row or len(row) < 3:
            continue

        try:
            weight = float(row[1])
        except ValueError:
            continue

        gem = row[0]

        for rock in row[2].split(","):
            rock = rock.strip()

            if rock not in rocks:
                rocks[rock] = []

            rocks[rock].append((gem, weight))


OUTPUT_DIR.mkdir(exist_ok=True)


for rock, gems in rocks.items():

    weights = [weight for gem, weight in gems]

    features = []

    for i, (gem, weight) in enumerate(gems):

        pass_value = calc_pass(weights, i)

        features.append({
            "feature": f"fbased:wall/gem/{gem}",
            "placement": [
                {
                    "type": "fbased:random_c",
                    "pass": pass_value
                }
            ]
        })

    result = {
        "feature": {
            "type": "fbased:mul",
            "config": {
                "features": features
            }
        },
        "placement": []
    }

    output_file = OUTPUT_DIR / f"{rock}.json"

    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

print(f"Создано пород: {len(rocks)}")