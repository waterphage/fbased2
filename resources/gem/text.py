from collections import Counter

with open("output.txt", "r", encoding="utf-8") as file:
    lines = file.readlines()

# Убираем дубликаты строк и сохраняем исходный порядок
unique_lines = list(dict.fromkeys(line.rstrip("\n") for line in lines))

# Выбираем строки, в которых "fbased" встречается минимум 2 раза
result = [
    line for line in unique_lines
]

# Сохраняем результат
with open("output2.txt", "w", encoding="utf-8") as file:
    file.write("\n".join(result))

print(f"Найдено уникальных строк: {len(result)}")