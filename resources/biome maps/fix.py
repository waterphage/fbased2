import os
import json

# Пути к папкам
out_folder = "out"
out_c_folder = "out_c"

# Получаем списки всех JSON-файлов в папках
out_files = {f: os.path.join(out_folder, f) for f in os.listdir(out_folder) if f.endswith(".json")}
out_c_files = {f: os.path.join(out_c_folder, f) for f in os.listdir(out_c_folder) if f.endswith(".json")}

# Обрабатываем файлы
for file_name, out_c_path in out_c_files.items():
    out_path = out_files.get(file_name)
    if not out_path:
        continue  # Пропускаем файлы, которых нет в обеих папках

    # Загружаем JSON из файлов
    with open(out_path, "r", encoding="utf-8") as out_file:
        out_data = json.load(out_file)

    with open(out_c_path, "r", encoding="utf-8") as out_c_file:
        out_c_data = json.load(out_c_file)

    # Проверяем и заменяем "sky_color"
    if "effects" in out_c_data and "sky_color" in out_c_data["effects"]:
        out_c_data["effects"]["sky_color"] = out_data["effects"]["sky_color"]
        out_c_data["effects"]["grass_color"] = out_data["effects"]["grass_color"]
        out_c_data["effects"]["foliage_color"] = out_data["effects"]["foliage_color"]
        # Сохраняем изменения в out_c
        with open(out_c_path, "w", encoding="utf-8") as out_c_file:
            json.dump(out_c_data, out_c_file, indent=4, ensure_ascii=False)

print("Заменены значения 'sky_color' в файлах.")
