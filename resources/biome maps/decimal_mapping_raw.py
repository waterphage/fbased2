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
    
    biomes_data = []

    for y in range(height):
        for x in range(section_width):
            layer = 0
            while (layer < 1):
                bx=x+1
                by=y+1
                if (bx>19):bx=19
                if (by>19):by=19
                match layer:
                    case 0: ds=0.458; de=1.1
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
                    "biome": f"fbased:c_raw/{bx}_{by}",
                    "parameters": {
                        "temperature": [ti, te],
                        "humidity": [hi, he],
                        "continentalness": [-0.125, 0.125],
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
image_path = "cave.png"  # Путь к вашей карте
output_folder = "out"
output_file = "raw.json"  # Папка для сохранения JSON-файлов
section_width = 20  # Ширина одной секции (10 пикселей)

# Создание папки, если её нет
import os
if not os.path.exists(output_folder):
    os.makedirs(output_folder)

generate_biome_json_files(image_path, output_folder, section_width,output_file)