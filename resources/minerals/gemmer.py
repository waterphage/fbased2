import csv
import json
import math

ROCKS_FILE = "rocks.txt"
MINERALS_FILE = "mineral_by_target.csv"
OUTPUT_FILE = "generated.txt"

GRID_SIZE = 64

# -----------------------------
# Читаем rocks.txt
# -----------------------------

with open(ROCKS_FILE, "r", encoding="utf-8") as f:
    rocks = [line.strip() for line in f if line.strip()]

if len(rocks) != GRID_SIZE * GRID_SIZE:
    raise ValueError(
        f"В rocks.txt должно быть {GRID_SIZE * GRID_SIZE} строк, "
        f"а найдено {len(rocks)}"
    )

# -----------------------------
# Читаем mineral_by_target.csv
# -----------------------------

mineral_data = {}

with open(MINERALS_FILE, "r", encoding="utf-8-sig", newline="") as f:
    reader = csv.DictReader(f, delimiter=";")
    i=1
    for row in reader:

        target = row["Target"]

        minerals = [
            x.strip()
            for x in row["minerals"].split(",")
            if x.strip()
        ]

        types = [
            x.strip()
            for x in row["types"].split(",")
            if x.strip()
        ]

        if len(minerals) != len(types):
            raise ValueError(
                f"в строке {i}. "
                f"{target}: количество minerals ({len(minerals)}) "
                f"не совпадает с количеством types {types} ({len(types)})"
            )

        mineral_data[target] = {
            "minerals": minerals,
            "types": types,
            "pass": 0.0
        }
        i+=1

# -----------------------------
# Provider для породы
# -----------------------------

def rock_provider(rock):
    return {"rock": {"m":{"Name": f"fbased:{rock}_raw"}}}

def vein_provider(mineral):
    return {"rock": {"m":{"Name": f"fbased:{mineral}_raw"}}}

def cluster_provider(rock, mineral):
    return {"rock": {"m":{"Name": f"fbased:{rock}_raw"}},"type":"c","min":{"m":{"Name": f"fbased:{mineral}_raw"}}}

def cluster_small_provider(rock, mineral):
    return {"rock": {"m":{"Name": f"fbased:{rock}_raw"}},"type":"s","min":{"m":{"Name": f"fbased:{mineral}_raw"}}}

# -----------------------------
# Проверка граничных/центральных строк
# -----------------------------

def is_forbidden_cell(row, col):
    # Крайние два ряда/столбца с каждой стороны
    if row in (0, 1, 62, 63):
        return True

    if col in (0, 1, 62, 63):
        return True

    # Центральные 4 строки
    if col in (30, 31, 32, 33):
        return True

    return False


# -----------------------------
# Генерация
# -----------------------------

result = []

i = 0.0

for index, rock in enumerate(rocks):

    row = index // GRID_SIZE
    col = index % GRID_SIZE

    # По умолчанию ставим породу
    provider = rock_provider(rock)

    # Границы и центральные строки
    if not is_forbidden_cell(row, col):
        #print("проверка на запрет пройдена для {index}")
        data = mineral_data[rock]["minerals"]

        if data is not None:
            #print("проверка на пустоту пройдена для {rock}")
            minerals = mineral_data[rock]["minerals"]
            types = mineral_data[rock]["types"]

            # Берём текущий индекс ДО его увеличения
            mineral_index = math.floor(mineral_data[rock]["pass"])

            if mineral_index < len(minerals):

                mineral = minerals[mineral_index]
                mineral_type = types[mineral_index]

                if mineral_type == "VEIN":

                    provider = vein_provider(mineral)
                    mineral_data[rock]["pass"] += 1.0


                elif mineral_type == "CLUSTER":

                    provider = cluster_provider(rock,mineral)
                    mineral_data[rock]["pass"] += 0.5

                elif mineral_type == "CLUSTER_SMALL" or mineral_type == "CLUSTER_ONE":

                    provider = cluster_small_provider(rock,mineral)
                    mineral_data[rock]["pass"] += 0.5

                else:
                    print(
                        f"WARNING: неизвестный type "
                        f"{mineral_type} для {rock}"
                    )

            # Индекс вышел за список — просто порода

    result.append(provider)


# -----------------------------
# Запись
# -----------------------------

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    for provider in result:
        f.write("          "+json.dumps(provider, ensure_ascii=False, separators=(",", ":")) + ",\n")

print(f"Готово: {OUTPUT_FILE}")
print(f"Позиций: {len(result)}")