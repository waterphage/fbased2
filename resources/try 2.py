import json
from PIL import Image
import os

def load_unique_colors(unique_colors_file):
    """Загружает уникальные цвета из файла JSON, преобразуя строки обратно в кортежи."""
    if os.path.exists(unique_colors_file):
        with open(unique_colors_file, 'r') as file:
            unique_colors_str_keys = json.load(file)
            # Преобразуем строки обратно в кортежи
            return {tuple(map(int, key.split(', '))): value for key, value in unique_colors_str_keys.items()}
    return {}

def save_unique_colors(unique_colors, unique_colors_file):
    """Сохраняет уникальные цвета в файл JSON, преобразуя кортежи в строки."""
    # Преобразуем ключи в строки
    unique_colors_str_keys = {str(key): value for key, value in unique_colors.items()}
    with open(unique_colors_file, 'w') as file:
        json.dump(unique_colors_str_keys, file, indent=4)

def create_image_atlas_with_metadata(image_path, palette_folder, palette_txt, atlas_path, output_txt, unique_colors_file, cell_size=16):
    """
    Создает атлас из изображения с использованием палитры и генерирует сопутствующие файлы.
    
    :param image_path: Путь к исходному изображению.
    :param palette_folder: Папка с 16x16 изображениями палитры.
    :param palette_txt: Файл с текстовым описанием палитры.
    :param atlas_path: Путь для сохранения атласа.
    :param output_txt: Файл с текстовым описанием пикселей.
    :param unique_colors_file: Файл для хранения уникальных цветов.
    :param cell_size: Размер ячейки в атласе.
    """
    # Открываем изображение
    image = Image.open(image_path).convert("RGBA")
    width, height = image.size

    # Загружаем или создаем файл palette.txt
    try:
        with open(palette_txt, 'r') as file:
            palette = [line.strip() for line in file.readlines()]
    except FileNotFoundError:
        palette = []

    # Загружаем уникальные цвета из файла JSON
    unique_colors = load_unique_colors(unique_colors_file)

    # Создаем пустое изображение для атласа
    atlas = Image.new("RGBA", (width * cell_size, height * cell_size), (255, 255, 255, 0))

    palette_files = os.listdir(palette_folder)
    output_lines = []

    # Проходим по каждому пикселю изображения
    for y in range(height):
        for x in range(width):
            # Получаем цвет пикселя
            pixel_color = image.getpixel((x, y))

            # Если цвет уже известен, берем его индекс
            if pixel_color not in unique_colors:
                # Добавляем новый цвет в палитру, если его нет
                unique_colors[pixel_color] = len(unique_colors)

                # Проверяем, существует ли изображение для цвета
                color_file = next((f for f in palette_files if f.startswith(f"{pixel_color}")), None)
                if not color_file:
                    placeholder_path = os.path.join(palette_folder, f"{pixel_color}.png")
                    placeholder_image = Image.new("RGBA", (cell_size, cell_size), pixel_color)
                    placeholder_image.save(placeholder_path)

            # Добавляем текстовое описание в output.txt
            index = unique_colors[pixel_color]
            output_lines.append(str(index))

            # Координаты для вставки картинки
            x_start = x * cell_size
            y_start = y * cell_size

            # Вставляем изображение из палитры
            tile_image = Image.open(os.path.join(palette_folder, f"{pixel_color}.png"))
            atlas.paste(tile_image, (x_start, y_start))

    # Сохраняем атлас
    atlas.save(atlas_path)
    print(f"Атлас сохранен: {atlas_path}")

    # Сохраняем обновленный palette.txt
    with open(palette_txt, 'w') as file:
        file.write("\n".join([str(i) for i in unique_colors.values()]) + "\n")
    print(f"Обновленный файл palette.txt сохранен: {palette_txt}")

    # Сохраняем output.txt
    with open(output_txt, 'w') as file:
        file.write("\n".join(output_lines) + "\n")
    print(f"Файл output.txt сохранен: {output_txt}")

    # Сохраняем уникальные цвета в файл
    save_unique_colors(unique_colors, unique_colors_file)
    print(f"Файл с уникальными цветами сохранен: {unique_colors_file}")


def create_palette_image(palette_txt, palette_folder, palette_image_path, cell_size=16, total_width=516, text_padding=10):
    """
    Создает изображение палитры, где цвета и текст идут вниз, по одной строке для каждого.
    Изображение будет иметь фиксированную ширину.

    :param palette_txt: Файл с текстовым описанием палитры.
    :param palette_folder: Папка с изображениями цветов.
    :param palette_image_path: Путь для сохранения изображения палитры.
    :param cell_size: Размер ячейки для цвета.
    :param total_width: Общая ширина итогового изображения.
    :param text_padding: Отступ между цветом и текстом.
    """
    # Загружаем или создаем файл palette.txt
    try:
        with open(palette_txt, 'r') as file:
            palette = [line.strip() for line in file.readlines()]
    except FileNotFoundError:
        palette = []
    
    # Список всех файлов в папке палитры
    palette_files = os.listdir(palette_folder)

    # Используем шрифт для текста
    try:
        font = ImageFont.truetype("arial.ttf", 14)
    except IOError:
        font = ImageFont.load_default()

    # Расчет ширины для текста (так как она должна поместиться в 516 пикселей)
    max_text_width = total_width - cell_size - text_padding  # Учитываем место под цвет
    text_width = max_text_width  # Размер текста

    # Определяем высоту итогового изображения
    total_height = len(palette) * (cell_size + text_padding)  # Высота на основе количества цветов

    # Создаем изображение
    palette_image = Image.new("RGBA", (total_width, total_height), (255, 255, 255, 255))
    draw = ImageDraw.Draw(palette_image)

    # Рисуем палитру
    y_start = 0
    for index, color_text in enumerate(palette):
        # Рисуем цвет
        color_image_path = os.path.join(palette_folder, f"{index}.png")
        if os.path.exists(color_image_path):
            color_image = Image.open(color_image_path).resize((cell_size, cell_size))
            palette_image.paste(color_image, (0, y_start))
        else:
            # Если изображения нет, создаем заглушку
            placeholder_image = Image.new("RGBA", (cell_size, cell_size), (200, 200, 200, 255))
            palette_image.paste(placeholder_image, (0, y_start))

        # Добавляем текст справа от изображения
        text_x = cell_size + text_padding
        text_bbox = draw.textbbox((text_x, 0), color_text, font=font)  # Получаем размеры текста
        text_y = y_start + (cell_size - (text_bbox[3] - text_bbox[1])) // 2  # Центрируем текст по вертикали
        draw.text((text_x, text_y), color_text, fill="black", font=font)

        # Переходим к следующей строке
        y_start += cell_size + text_padding

    # Сохраняем изображение
    palette_image.save(palette_image_path)
    print(f"Изображение палитры сохранено: {palette_image_path}")


if __name__ == "__main__":
    # Пути
    image_path = "goal.png"            # Исходное изображение
    palette_folder = "palette"        # Папка с изображениями
    palette_txt = "palette.txt"       # Файл палитры
    unique_colors_file = "unique_colors.json"  # Файл для уникальных цветов
    atlas_path = "image_atlas.png"    # Итоговый атлас
    output_txt = "output.txt"         # Описание пикселей
    palette_image_path = "palette.png" # Итоговое изображение палитры

    # Создаем атлас с метаданными
    create_image_atlas_with_metadata(image_path, palette_folder, palette_txt, atlas_path, output_txt, unique_colors_file)

    # Создаем изображение палитры
    create_palette_image(palette_txt, palette_folder, palette_image_path)