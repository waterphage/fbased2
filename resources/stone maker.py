import os
import shutil
from enum import Enum

SOURCE_DIR = 'probe'
BASE_OUTPUT_DIR = 'out'
REPLACE_FROM = 'alabaster'

class Material(Enum):
        A1=["sandstone","black","white","yellow","tan"]
        A2=["siltstone","black","white","yellow","tan"]
        A3=["mudstone","black","white","yellow","tan"]
        A4=["shale","black","white","yellow","red"]
        A5=["claystone","black","white","yellow","tan"]
        A6=["rock_salt","black","white","yellow","red"]
        A7=["limestone","black","white","yellow","red"]
        A8=["conglomerate","black","white","yellow","tan"]
        A9=["dolomite","black","white","yellow","tan"]
        A10=["chert","black","white","yellow","tan"]
        A11=["chalk","black","white","yellow","tan"]
        A12=["granite","black","white","green","red"]
        A13=["diorite","black","white","green","red"]
        A14=["gabbro","black","white","green","red"]
        A15=["rhyolite","black","white","green","red"]
        A16=["basalt","black","white","green","red"]
        A17=["andesite","black","white","green","red"]
        A18=["dacite","black","white","green","red"]
        A19=["obsidian","black","white","green","red"]
        A20=["quartzite","black","white","green","tan"]
        A21=["slate","black","white","green","tan"]
        A22=["phyllite","black","white","green","red"]
        A23=["schist","black","white","green","red"]
        A24=["gneiss","black","white","green","red"]
        A25=["marble","black","white","green","red"]

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
        
        process_dir(SOURCE_DIR, BASE_OUTPUT_DIR,f'{material.value[1]}_{material.value[0]}')
        process_dir(SOURCE_DIR, BASE_OUTPUT_DIR,f'{material.value[2]}_{material.value[0]}')
        process_dir(SOURCE_DIR, BASE_OUTPUT_DIR,f'{material.value[3]}_{material.value[0]}')
        process_dir(SOURCE_DIR, BASE_OUTPUT_DIR,f'{material.value[4]}_{material.value[0]}')
        print(f"✅ {material.name}: заменено '{REPLACE_FROM}' на '{material.value[0]}'")
