from PIL import Image
import os

def compare_images(img1, img2):
    """
    Сравнивает два изображения побайтово.
    :param img1: Первое изображение (PIL.Image).
    :param img2: Второе изображение (PIL.Image).
    :return: True, если изображения идентичны, иначе False.
    """
    return img1.tobytes() == img2.tobytes()

def update_palette_with_variants(image_path, palette_folder, variants_folder, palette_txt, tile_size=(16, 16), start_x=48, start_y=0, step_y=26):
    """
    Обновляет `palette.txt` и заменяет изображения в папке `palette` на основании сопоставления тайлов из `final_full_map.png` с образцами из `variants`.
    
    :param image_path: Путь к изображению с картой (final_full_map.png).
    :param palette_folder: Папка с извлечёнными тайлами.
    :param variants_folder: Папка с образцами тайлов.
    :param palette_txt: Путь к файлу `palette.txt`.
    :param tile_size: Размер тайла (по умолчанию 16x16).
    :param start_x: Начальная координата X для извлечения тайлов.
    :param start_y: Начальная координата Y для извлечения тайлов.
    :param step_y: Шаг по оси Y между тайлами.
    """
    # Открываем изображение
    image = Image.open(image_path)

    # Получаем список файлов в папке variants
    variants_files = sorted(os.listdir(variants_folder))

    # Загружаем изображения из variants в память
    variant_images = {
        os.path.splitext(file)[0]: Image.open(os.path.join(variants_folder, file)).convert("RGBA")
        for file in variants_files
    }

    # Загружаем текущий `palette.txt` (или создаём пустой)
    try:
        with open(palette_txt, "r") as file:
            palette_data = [line.strip() for line in file.readlines()]
    except FileNotFoundError:
        palette_data = []

    # Извлекаем тайлы и обновляем файл
    y = start_y
    n = 0  # Номер текущего тайла
    while y + tile_size[1] <= image.height:
        # Вырезаем текущий тайл
        tile = image.crop((start_x, y, start_x + tile_size[0], y + tile_size[1]))

        # Сравниваем с каждым изображением из папки variants
        matched_name = None
        for variant_name, variant_image in variant_images.items():
            if compare_images(tile, variant_image):
                matched_name = variant_name  # Название без расширения
                break

        # Записываем название найденного файла в palette.txt
        if matched_name is not None:
            if n < len(palette_data):
                palette_data[n] = matched_name
            else:
                palette_data.append(matched_name)

        # Заменяем изображение в папке palette
        tile.save(os.path.join(palette_folder, f"{n}.png"))
        print(f"Заменено изображение {n}.png")

        # Переходим к следующему тайлу
        y += step_y
        n += 1

    # Сохраняем обновленный `palette.txt`
    with open(palette_txt, "w") as file:
        file.write("\n".join(palette_data) + "\n")
    print(f"Файл palette.txt обновлён: {palette_txt}")

if __name__ == "__main__":
    # Пути
    image_path = "final_full_map.png"  # Путь к изображению с картой
    palette_folder = "palette"         # Папка для сохранения тайлов
    variants_folder = "variants"       # Папка с образцами
    palette_txt = "palette.txt"        # Файл палитры

    # Обновляем palette.txt и заменяем изображения
    update_palette_with_variants(image_path, palette_folder, variants_folder, palette_txt)

from PIL import Image
import os

def create_image_atlas_with_metadata(image_path, palette_folder, palette_txt, atlas_path, output_txt, cell_size=16):
    """
    Создает атлас из изображения с использованием палитры и генерирует сопутствующие файлы.
    """
    from PIL import Image
    import os

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


from PIL import Image, ImageDraw, ImageFont
import os

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

from PIL import Image, ImageDraw, ImageFont
import os

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

from PIL import Image

def create_full_map(full_map_path, palette_path, image_atlas_path, output_path):
    """
    Вставляет изображение палитры и атласа в изображение full_map.
    
    :param full_map_path: Путь к исходному изображению full_map.png.
    :param palette_path: Путь к изображению палитры palette.png.
    :param image_atlas_path: Путь к изображению атласа image_atlas.png.
    :param output_path: Путь для сохранения итогового изображения.
    """
    # Открываем изображения
    full_map = Image.open(full_map_path)
    palette = Image.open(palette_path)
    image_atlas = Image.open(image_atlas_path)

    # Получаем размеры full_map
    full_map_width, full_map_height = full_map.size

    # Получаем размеры изображений палитры и атласа
    palette_width, palette_height = palette.size
    atlas_width, atlas_height = image_atlas.size

    # Вставляем палитру в левую часть full_map
    full_map.paste(palette, (48, 0))

    # Вставляем атлас в верхний правый угол full_map
    full_map.paste(image_atlas, (full_map_width - atlas_width, 0))

    # Сохраняем итоговое изображение
    full_map.save(output_path)
    print(f"Итоговое изображение сохранено: {output_path}")

if __name__ == "__main__":
    # Пути
    full_map_path = "full_map.png"        # Исходное изображение full_map
    palette_path = "palette.png"          # Путь к изображению палитры
    image_atlas_path = "image_atlas.png"  # Путь к изображению атласа
    output_path = "final_full_map.png"    # Путь для сохранения итогового изображения

# Создаем атлас с метаданными
create_image_atlas_with_metadata(image_path, palette_folder, palette_txt, atlas_path, output_txt)

# Создаем изображение палитры
create_palette_image(palette_txt, palette_folder, palette_image_path)

# Создаем итоговое изображение
create_full_map(full_map_path, palette_path, image_atlas_path, output_path)