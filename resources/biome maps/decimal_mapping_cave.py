import json
from PIL import Image

def rgb_to_decimal(rgb):
    """Преобразует RGB в десятичный формат."""
    return rgb[0] << 16 | rgb[1] << 8 | rgb[2]

def generate_biome_json_files(image_path, output_folder, section_width,output_file):
    """
    Создаёт JSON-файлы для каждого пикселя карты.
    
    :param image_path: Путь к изображению карты.
    :param output_folder: Папка для сохранения JSON-файлов.
    :param section_width: Ширина одной секции в пикселях.
    """
    # Открываем изображение
    image = Image.open(image_path)
    width, height = image.size

    # Проверяем, что изображение делится на 6 секций
    if width % section_width != 0 or width // section_width != 6:
        raise ValueError("Ширина изображения должна делиться на 6 секций.")

    # Разделяем секции
    sections = [image.crop((i * section_width, 0, (i + 1) * section_width, height)) for i in range(6)]

    # Генерируем JSON-файлы для каждого пикселя
    for y in range(height):
        for x in range(section_width):
            if (x>=5)and(x<=14):
                downfall = (x-5+1)*0.1
                prec = True
            elif (x<5):
                downfall = 0.0
                prec = False
            else:
                downfall = 0.0
                prec = True
            if (y>=5)and(y<=14):
                temperature = 0.45+(y-5)*0.155
            elif (y<5):
                temperature = 0-(y)*0.155
            else:
                temperature = 2.0+(y-15)*0.155
                prec = False
            # Извлекаем цвета из каждой секции
            grass_color = rgb_to_decimal(sections[0].getpixel((x, y)))
            foliage_color = rgb_to_decimal(sections[1].getpixel((x, y)))
            sky_color = rgb_to_decimal(sections[2].getpixel((x, y)))
            fog_color = rgb_to_decimal(sections[3].getpixel((x, y)))
            water_color = rgb_to_decimal(sections[4].getpixel((x, y)))
            water_fog_color = rgb_to_decimal(sections[5].getpixel((x, y)))

            # Шаблон JSON-данных
            biome_data = {
                "carvers": {
                    "air": []
                },
                "downfall": downfall,
                "temperature": temperature,
                "has_precipitation": prec,
                "effects": {
                    "mood_sound": {
                        "block_search_extent": 8,
                        "offset": 2,
                        "sound": "minecraft:ambient.cave",
                        "tick_delay": 6000
                    },
                    "grass_color": grass_color,
                    "foliage_color": foliage_color,
                    "sky_color": sky_color,
                    "fog_color": fog_color,
                    "water_color": water_color,
                    "water_fog_color": water_fog_color
                },
                "features": [
                    [],
                    [],
                    [],
                    [],
                    [],
                    [],
                    [],
                    [],
                    [],
                    [],
                    [
                        "minecraft:freeze_top_layer"
                    ]
                ],
                "spawn_costs": {},
                "spawners": {
                    "ambient": [],
                    "axolotls": [],
                    "creature": [],
                    "misc": [],
                    "monster": [],
                    "underground_water_creature": [],
                    "water_ambient": [],
                    "water_creature": []
                }
            }

            # Сохраняем JSON в файл
            file_name = f"{x}_{y}.json"
            file_path = f"{output_folder}/{file_name}"
            with open(file_path, "w") as file:
                json.dump(biome_data, file, indent=4)
    
    print(f"JSON-файлы успешно созданы в папке: {output_folder}")
    
    biomes_data = []

    for y in range(height):
        for x in range(section_width):
            layer = 0
            while (layer < 3):
                bx=x-layer
                if (bx<0):bx=0
                match layer:
                    case 0: ds=0.0; de=0.458
                    case 1: ds=-0.458; de=0.0
                    case 2: ds=-2.0; de=-0.458
                layer = layer + 1

                if (x>=5)and(x<=14):
                    hi=(x-5)*(0.667*2)/10-0.667
                    he=(x-4)*(0.667*2)/10-0.667
                elif (x<5):
                    hi=x*(2-0.667)/5-2
                    he=(x+1)*(2-0.667)/5-2
                else:
                    hi=(x-15)*(2-0.667)/5+0.667
                    he=(x-14)*(2-0.667)/5+0.667
                
                if (y>=5)and(y<=14):
                    ti=(y-5)*(0.667*2)/10-0.667
                    te=(y-4)*(0.667*2)/10-0.667
                elif (y<5):
                    ti=y*(2-0.667)/5-2
                    te=(y+1)*(2-0.667)/5-2
                else:
                    ti=(y-15)*(2-0.667)/5+0.667
                    te=(y-14)*(2-0.667)/5+0.667

                biome_entry = {
                    "biome": f"fbased:c_main/{bx}_{y}",
                    "parameters": {
                        "temperature": [ti, te],
                        "humidity": [hi, he],
                        "continentalness": [-2, 2],
                        "erosion": [-2, 2],
                        "weirdness": [-2, 2],
                        "depth": [ds, de],
                        "offset": 0
                    }
                }
                biomes_data.append(biome_entry)

    # Сохраняем данные в JSON-файл
    with open(output_file, 'w') as file:
        json.dump(biomes_data, file, indent=2)
    print(f"Файл {output_file} успешно создан.")

# Пример использования
image_path = "main.png"  # Путь к вашей карте
output_folder = "out"
output_file = "out.json"  # Папка для сохранения JSON-файлов
section_width = 20  # Ширина одной секции (10 пикселей)

# Создание папки, если её нет
import os
if not os.path.exists(output_folder):
    os.makedirs(output_folder)

generate_biome_json_files(image_path, output_folder, section_width,output_file)