import os
import shutil
from enum import Enum

SOURCE_DIR = 'probe'
BASE_OUTPUT_DIR = 'out'
REPLACE_FROM = 'alabaster'

class Material(Enum):
    A1 = "brown_gem"
    A2 = "red_gem"
    A3 = "orange_gem"
    A4 = "yellow_gem"
    A5 = "lime_gem"
    A6 = "green_gem"
    A7 = "cyan_gem"
    A8 = "light_blue_gem"
    A9 = "blue_gem"
    A10 = "purple_gem"
    A11 = "magenta_gem"
    A12 = "pink_gem"
    A13 = "white_gem"
    A14 = "light_gray_gem"
    A15 = "gray_gem"
    A16 = "black_gem"

    B1 = "brown_foggy_gem"
    B2 = "red_foggy_gem"
    B3 = "orange_foggy_gem"
    B4 = "yellow_foggy_gem"
    B5 = "lime_foggy_gem"
    B6 = "green_foggy_gem"
    B7 = "cyan_foggy_gem"
    B8 = "light_blue_foggy_gem"
    B9 = "blue_foggy_gem"
    B10 = "purple_foggy_gem"
    B11 = "magenta_foggy_gem"
    B12 = "pink_foggy_gem"
    B13 = "white_foggy_gem"
    B14 = "light_gray_foggy_gem"
    B15 = "gray_foggy_gem"
    B16 = "black_foggy_gem"

    C1 = "brown_dirty_gem"
    C2 = "red_dirty_gem"
    C3 = "orange_dirty_gem"
    C4 = "yellow_dirty_gem"
    C5 = "lime_dirty_gem"
    C6 = "green_dirty_gem"
    C7 = "cyan_dirty_gem"
    C8 = "light_blue_dirty_gem"
    C9 = "blue_dirty_gem"
    C10 = "purple_dirty_gem"
    C11 = "magenta_dirty_gem"
    C12 = "pink_dirty_gem"
    C13 = "white_dirty_gem"
    C14 = "light_gray_dirty_gem"
    C15 = "gray_dirty_gem"
    C16 = "black_dirty_gem"

def process_dir(src, dst, replace_to):
    for root, dirs, files in os.walk(src):
        rel_path = os.path.relpath(root, src)
        rel_path_replaced = rel_path.replace(REPLACE_FROM, replace_to)
        target_root = os.path.join(dst, rel_path_replaced)
        os.makedirs(target_root, exist_ok=True)

        for file in files:
            src_file = os.path.join(root, file)
            new_filename = file.replace(REPLACE_FROM, replace_to)
            dst_file = os.path.join(target_root, new_filename)

            try:
                with open(src_file, 'r', encoding='utf-8') as f:
                    content = f.read()
                content = content.replace(REPLACE_FROM, replace_to)
                with open(dst_file, 'w', encoding='utf-8') as f:
                    f.write(content)
            except UnicodeDecodeError:
                shutil.copy2(src_file, dst_file)

if __name__ == '__main__':
    if os.path.exists(BASE_OUTPUT_DIR):
        shutil.rmtree(BASE_OUTPUT_DIR)
    os.makedirs(BASE_OUTPUT_DIR)

    for material in Material:
        process_dir(SOURCE_DIR, BASE_OUTPUT_DIR, material.value)
        print(f"✅ {material.name}: заменено '{REPLACE_FROM}' на '{material.value}'")
