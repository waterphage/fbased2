import os
import re
import json

GENERATED_FILE = "generated.txt"
CHECK_DIR = "check"

# --------------------------------------------------
# Читаем список блоков из папки check
# --------------------------------------------------

textures = set()

for filename in os.listdir(CHECK_DIR):
    name, ext = os.path.splitext(filename)

    if ext.lower() in (".png", ".jpg", ".jpeg", ".webp"):
        textures.add(name)

print(f"Текстур найдено: {len(textures)}")

# --------------------------------------------------
# Читаем generated.txt
# --------------------------------------------------

with open(GENERATED_FILE, "r", encoding="utf-8") as f:
    lines = [line.rstrip("\n") for line in f]

print(f"Строк в generated.txt: {len(lines)}")

if len(lines) != 4096:
    print(f"WARNING: ожидалось 4096 строк, найдено {len(lines)}")

# --------------------------------------------------
# Проверяем JSON и извлекаем блоки
# --------------------------------------------------
block_pattern = re.compile(r'm":{"Name":"fbased:([^"]+_raw)')

used_blocks = set()
errors = []

provider_types = set()

for line_number, line in enumerate(lines, 1):

    # JSON
    try:
        data = json.loads(line.rstrip(","))
    except json.JSONDecodeError as e:
        errors.append(
            f"Строка {line_number}: ошибка JSON: {e}"
        )
        continue

    # Тип provider
    provider_type = data.get("type")

    if provider_type is not None:
        provider_types.add(provider_type)

    # Ищем все fbased:<...>_raw
    matches = block_pattern.findall(line)

    if not matches:
        errors.append(
            f"Строка {line_number}: не найден fbased:<block>_raw"
        )
        continue

    for block in matches:
        used_blocks.add(block)

        if block not in textures:
            errors.append(
                f"Строка {line_number}: "
                f"блок '{block}' отсутствует в папке check"
            )

# --------------------------------------------------
# Какие блоки используются
# --------------------------------------------------

missing = textures - used_blocks
unknown = used_blocks - textures

# --------------------------------------------------
# Вывод
# --------------------------------------------------

print()
print("========== РЕЗУЛЬТАТ ==========")

print(f"Уникальных текстур:       {len(textures)}")
print(f"Уникальных блоков в JSON:  {len(used_blocks)}")

print()
print("Provider types:")

for provider_type in sorted(provider_types):
    print(f"  {provider_type}")

print()
print(f"Ошибок: {len(errors)}")

if errors:
    print()
    print("========== ОШИБКИ ==========")

    for error in errors:
        print(error)

print()
print("========== ОТСУТСТВУЮЩИЕ БЛОКИ ==========")

if missing:
    for block in sorted(missing):
        print(block)
else:
    print("Нет. Все текстуры представлены.")

print()
print("========== НЕИЗВЕСТНЫЕ БЛОКИ ==========")

if unknown:
    for block in sorted(unknown):
        print(block)
else:
    print("Нет.")

# --------------------------------------------------
# Проверка всех ожидаемых типов
# --------------------------------------------------

expected_types = {
    "minecraft:simple_state_provider",
    "minecraft:weighted_state_provider",
    "minecraft:noise_threshold_provider"
}

print()
print("========== ТИПЫ PROVIDER ==========")

missing_types = expected_types - provider_types

if missing_types:
    print("Не представлены:")
    for t in sorted(missing_types):
        print("  " + t)
else:
    print("Все три типа provider представлены.")

# --------------------------------------------------
# Финальный результат
# --------------------------------------------------

print()
if (
    len(lines) == 4096
    and not errors
    and not missing
    and not unknown
    and not missing_types
):
    print("✓ ПРОВЕРКА ПРОЙДЕНА УСПЕШНО")
else:
    print("✗ ЕСТЬ ПРОБЛЕМЫ")

    