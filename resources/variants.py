from PIL import Image, ImageDraw, ImageFont
import os

def create_palette_image(variants_folder, output_image_path, cell_size=16, text_padding=10, font_size=12, tiles_per_row=6):
    """
    Создает палитру с тайлами из папки variants, добавляя имена файлов в качестве подписей.

    :param variants_folder: Папка с изображениями тайлов.
    :param output_image_path: Путь для сохранения итогового изображения палитры.
    :param cell_size: Размер одной ячейки (тайла).
    :param text_padding: Отступ между тайлом и текстом.
    :param font_size: Размер шрифта для текста.
    :param tiles_per_row: Количество тайлов в одном ряду.
    """
    # Получаем список файлов в папке variants
    variant_files = [f for f in os.listdir(variants_folder) if f.endswith('.png')]
    variant_files.sort()  # Сортируем файлы по имени

    # Настраиваем шрифт
    font = ImageFont.load_default()

    # Рассчитываем размеры текста
    dummy_draw = ImageDraw.Draw(Image.new("RGBA", (1, 1)))
    max_text_width = max(dummy_draw.textbbox((0, 0), os.path.splitext(f)[0], font=font)[2] for f in variant_files)

    # Рассчитываем размеры палитры
    row_width = tiles_per_row * (cell_size + text_padding + max_text_width)
    row_height = cell_size + text_padding
    rows_count = (len(variant_files) + tiles_per_row - 1) // tiles_per_row
    palette_height = rows_count * row_height

    # Создаем пустое изображение
    palette_image = Image.new("RGBA", (row_width, palette_height), (255, 255, 255, 255))
    draw = ImageDraw.Draw(palette_image)

    # Рисуем палитру
    for index, file_name in enumerate(variant_files):
        # Рассчитываем позицию в палитре
        col = index % tiles_per_row
        row = index // tiles_per_row
        x_start = col * (cell_size + text_padding + max_text_width)
        y_start = row * row_height

        # Рисуем изображение
        image_path = os.path.join(variants_folder, file_name)
        tile_image = Image.open(image_path).resize((cell_size, cell_size))
        palette_image.paste(tile_image, (x_start, y_start))

        # Добавляем текст справа от изображения
        label = os.path.splitext(file_name)[0]
        text_x = x_start + cell_size + text_padding
        text_bbox = draw.textbbox((text_x, 0), label, font=font)
        text_y = y_start + (cell_size - (text_bbox[3] - text_bbox[1])) // 2
        draw.text((text_x, text_y), label, fill="black", font=font)

    # Сохраняем результат
    palette_image.save(output_image_path)
    print(f"Изображение палитры сохранено: {output_image_path}")

if __name__ == "__main__":
    variants_folder = "variants"  # Папка с изображениями тайлов
    output_image_path = "variants_palette.png"  # Итоговое изображение палитры

    create_palette_image(variants_folder, output_image_path)
