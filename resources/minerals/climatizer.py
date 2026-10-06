from pathlib import Path


INPUT_FILE = Path("generated.txt")
REPLACEMENTS_FILE = Path("climate.txt")
OUTPUT_FILE = Path("generated_final.txt")


def load_replacements(filename):
    replacements = []

    with filename.open("r", encoding="utf-8-sig") as f:
        for line_number, line in enumerate(f, 1):
            line = line.rstrip("\r\n")

            if not line:
                continue

            # Пропускаем заголовок
            if line_number == 1 and line.lower() == "old;new":
                continue

            if ";" not in line:
                print(f"[ОШИБКА] Строка {line_number}: нет ';'")
                continue

            # ВАЖНО: делим только по первой ;
            old, new = line.split(";", 1)

            replacements.append((old, new))

    return replacements


def main():
    text = INPUT_FILE.read_text(encoding="utf-8")

    replacements = load_replacements(REPLACEMENTS_FILE)

    print(f"Загружено замен: {len(replacements)}")
    print()

    total = 0

    for old, new in replacements:
        count = text.count(old)

        if count:
            text = text.replace(old, new)
            total += count

            print(f"[OK] {count}x  {old} -> {new}")
        else:
            print(f"[НЕТ] {old}")

    OUTPUT_FILE.write_text(text, encoding="utf-8")

    print()
    print(f"Всего заменено: {total}")
    print(f"Результат сохранён в: {OUTPUT_FILE}")


if __name__ == "__main__":
    main()