import os
import json

TEST_DIR = "test"
BLOCKS_FILE = "blocks.txt"

# --------------------------------------------------
# Блоки
# --------------------------------------------------

with open(BLOCKS_FILE, "r", encoding="utf-8") as f:
    blocks = {
        line.strip()
        for line in f
        if line.strip()
    }

# --------------------------------------------------
# Рекурсивно проверяем все строковые значения JSON
# --------------------------------------------------

def check_value(value, filename, path, errors):

    if isinstance(value, str):

        # Разбираем namespace:name
        if ":" in value:

            namespace, name = value.split(":", 1)

            # Если это один из известных блоков,
            # namespace обязан быть fbased или minecraft
            if name in blocks:
                if namespace not in ("fbased", "minecraft"):
                    errors.append(
                        f"{filename}: {path} -> "
                        f'"{value}" '
                        f'(ожидался fbased:{name} '
                        f'или minecraft:{name})'
                    )

        # Если строка ровно совпадает с названием блока,
        # namespace вообще отсутствует
        elif value in blocks:

            errors.append(
                f"{filename}: {path} -> "
                f'"{value}" '
                f"(нет namespace)"
            )

    elif isinstance(value, dict):

        for key, val in value.items():
            check_value(
                val,
                filename,
                f"{path}.{key}",
                errors
            )

    elif isinstance(value, list):

        for i, val in enumerate(value):
            check_value(
                val,
                filename,
                f"{path}[{i}]",
                errors
            )


# --------------------------------------------------
# Файлы
# --------------------------------------------------

errors = []

for filename in os.listdir(TEST_DIR):

    if not filename.lower().endswith(".json"):
        continue

    path = os.path.join(TEST_DIR, filename)

    try:
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)

        check_value(
            data,
            filename,
            "$",
            errors
        )

    except json.JSONDecodeError as e:
        print(f"{filename}: ОШИБКА JSON: {e}")


# --------------------------------------------------
# Результат
# --------------------------------------------------

if errors:

    print("\nНАЙДЕНЫ ОШИБКИ:\n")

    for error in errors:
        print(error)

else:

    print("Ошибок не найдено.")