import os

def find_missing_files_in_palette(palette_file, variants_folder):
    """
    Находит файлы из папки variants, которые отсутствуют в palette.txt.

    :param palette_file: Путь к файлу palette.txt.
    :param variants_folder: Путь к папке variants.
    """
    # Читаем строки из palette.txt
    with open(palette_file, 'r') as file:
        palette_entries = {line.strip() for line in file.readlines()}

    # Получаем список файлов из папки variants (без .png)
    variant_files = {os.path.splitext(f)[0] for f in os.listdir(variants_folder) if f.endswith('.png')}

    # Находим отсутствующие файлы
    missing_files = variant_files - palette_entries

    # Выводим результат
    if missing_files:
        print("Файлы из variants, отсутствующие в palette.txt:")
        for missing_file in sorted(missing_files):
            print(missing_file)
    else:
        print("Все файлы из variants присутствуют в palette.txt.")

if __name__ == "__main__":
    palette_file = "palette.txt"  # Файл с палитрой
    variants_folder = "variants"  # Папка с файлами

    find_missing_files_in_palette(palette_file, variants_folder)
