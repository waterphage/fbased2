from PIL import Image, ImageDraw, ImageFont
import os

def compare_images(img1, img2):
    return img1.tobytes() == img2.tobytes()

def create_image_atlas_with_metadata(image_path, palette_folder, palette_txt, atlas_path, output_txt, cell_size=16):
    # Открываем изображение
    image = Image.open(image_path).convert("RGBA")
    width, height = image.size

    # Загружаем или создаем файл palette.txt
    try:
        with open(palette_txt, 'r') as file:
            palette = [line.strip() for line in file.readlines()]
    except FileNotFoundError:
        palette = []

    # Создаем пустое изображение для атласа
    atlas = Image.new("RGBA", (width * cell_size, height * cell_size), (255, 255, 255, 0))

    # Словарь для уникальных цветов
    unique_colors = {}
    output_lines = []

    # Проходим по каждому пикселю изображения
    for y in range(height):
        for x in range(width):
            # Получаем цвет пикселя
            pixel_color = image.getpixel((x, y))

            # Если цвет уже известен, берем его индекс
            if pixel_color not in unique_colors:
                # Новый цвет, добавляем в уникальные цвета
                color_index = len(unique_colors)
                unique_colors[pixel_color] = color_index

                # Генерируем файл палитры, если его еще нет
                color_file = os.path.join(palette_folder, f"{color_index}.png")
                if not os.path.exists(color_file):
                    placeholder_image = Image.new("RGBA", (cell_size, cell_size), pixel_color)
                    placeholder_image.save(color_file)

                # Добавляем в палитру
                if len(palette) <= color_index:
                    palette.append(str(color_index))

            # Добавляем текстовое описание в output.txt
            index = unique_colors[pixel_color]
            output_lines.append(palette[index])

            # Координаты для вставки картинки
            x_start = x * cell_size
            y_start = y * cell_size

            # Вставляем изображение из палитры
            tile_image = Image.open(os.path.join(palette_folder, f"{unique_colors[pixel_color]}.png"))
            atlas.paste(tile_image, (x_start, y_start))

    # Сохраняем атлас
    atlas.save(atlas_path)
    print(f"Атлас сохранен: {atlas_path}")

    # Сохраняем обновленный palette.txt
    with open(palette_txt, 'w') as file:
        file.write("\n".join(palette) + "\n")
    print(f"Обновленный файл palette.txt сохранен: {palette_txt}")

    # Сохраняем output.txt
    with open(output_txt, 'w') as file:
        file.write("\n".join(output_lines) + "\n")
    print(f"Файл output.txt сохранен: {output_txt}")


if __name__ == "__main__":
    # Пути
    image_path = "goal.png"            # Исходное изображение
    palette_folder = "palette"        # Папка с изображениями
    palette_txt = "palette.txt"       # Файл палитры
    atlas_path = "image_atlas.png"    # Итоговый атлас
    output_txt = "output.txt"         # Описание пикселей

# Создаем атлас с метаданными
create_image_atlas_with_metadata(image_path, palette_folder, palette_txt, atlas_path, output_txt)

def create_full_map(full_map_path, palette_path, image_atlas_path, goal_path, output_path, cell_size=16):
    """
    Вставляет палитру, атлас и увеличенное изображение goal в изображение full_map.

    :param full_map_path: Путь к исходному изображению full_map.png.
    :param palette_path: Путь к изображению палитры palette.png.
    :param image_atlas_path: Путь к изображению атласа image_atlas.png.
    :param goal_path: Путь к изображению goal.png.
    :param output_path: Путь для сохранения итогового изображения.
    :param cell_size: Размер увеличения пикселя (по умолчанию 16x16).
    """
    # Открываем изображения
    full_map = Image.open(full_map_path)
    palette = Image.open(palette_path)
    image_atlas = Image.open(image_atlas_path)
    goal = Image.open(goal_path)

    # Получаем размеры full_map
    full_map_width, full_map_height = full_map.size

    # Получаем размеры изображений палитры и атласа
    palette_width, palette_height = palette.size
    atlas_width, atlas_height = image_atlas.size

    # Вставляем палитру в левую часть full_map
    full_map.paste(palette, (48, 0))

    # Вставляем атлас в верхний правый угол full_map
    full_map.paste(image_atlas, (full_map_width - atlas_width, 0))

    # Увеличиваем изображение goal
    goal_width, goal_height = goal.size
    enlarged_goal = Image.new("RGBA", (goal_width * cell_size, goal_height * cell_size))
    for y in range(goal_height):
        for x in range(goal_width):
            pixel_color = goal.getpixel((x, y))
            for dy in range(cell_size):
                for dx in range(cell_size):
                    enlarged_goal.putpixel((x * cell_size + dx, y * cell_size + dy), pixel_color)

    # Вставляем увеличенное изображение goal под атлас
    enlarged_goal_x = full_map_width - atlas_width
    enlarged_goal_y = atlas_height+16
    full_map.paste(enlarged_goal, (enlarged_goal_x, enlarged_goal_y))

    # Сохраняем итоговое изображение
    full_map.save(output_path)
    print(f"Итоговое изображение сохранено: {output_path}")

# Пример вызова функции
full_map_path = "full_map.png"          # Путь к исходному изображению
palette_path = "palette.png"            # Путь к изображению палитры
image_atlas_path = "image_atlas.png"    # Путь к изображению атласа
goal_path = "goal.png"                  # Путь к изображению goal
output_path = "final_full_map.png"              # Путь для сохранения результата

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
    palette_txt = "palette.txt"       # Файл палитры
    palette_folder = "palette"        # Папка с изображениями
    palette_image_path = "palette.png" # Итоговое изображение палитры

# Создаем изображение палитры
create_palette_image(palette_txt, palette_folder, palette_image_path)

# Создаем итоговое изображение
create_full_map(full_map_path, palette_path, image_atlas_path, goal_path, output_path)