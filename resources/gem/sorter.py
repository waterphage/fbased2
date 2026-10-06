import json
import re
import os
import glob
import math
import csv
from collections import defaultdict

# --- Конфигурация и окружения ---
ENV_SEDIMENTARY = ["sandstone", "siltstone", "mudstone", "shale", "claystone", "rock_salt", "limestone", "conglomerate", "dolomite", "chert", "chalk"]
ENV_IGNEOUS_INTRUSIVE = ["granite", "diorite", "gabbro"]
ENV_IGNEOUS_EXTRUSIVE = ["rhyolite", "basalt", "andesite", "dacite", "obsidian"]
ENV_METAMORPHIC = ["quartzite", "slate", "phyllite", "schist", "gneiss", "marble"]
ALLUVIAL = ["siltstone", "mudstone", "claystone", "rock_salt", "dolomite", "chert"]

ENV_MAP = {
    "SEDIMENTARY": ENV_SEDIMENTARY,
    "IGNEOUS_INTRUSIVE": ENV_IGNEOUS_INTRUSIVE,
    "IGNEOUS_EXTRUSIVE": ENV_IGNEOUS_EXTRUSIVE,
    "IGNEOUS_ALL": ENV_IGNEOUS_INTRUSIVE + ENV_IGNEOUS_EXTRUSIVE,
    "METAMORPHIC": ENV_METAMORPHIC,
    "ALL_STONE": ENV_SEDIMENTARY + ENV_IGNEOUS_INTRUSIVE + ENV_IGNEOUS_EXTRUSIVE + ENV_METAMORPHIC,
    "ALLUVIAL": ALLUVIAL
}

FALLBACK_MINERALS = {
    "bauxite": ENV_SEDIMENTARY,
    "kimberlite": ENV_IGNEOUS_INTRUSIVE,
    "kaolinite": ENV_SEDIMENTARY,
    "malachite": ["limestone", "marble", "dolomite"],
    "chromite": ["gabbro"],
    "diamond_fy": ENV_IGNEOUS_INTRUSIVE
}

COLOR_MAP = {
    "white": ["white", "cream", "ivory", "pearl", "clear", "silver", "beige", "flax"],
    "black": ["black", "taupe_dark", "olive", "gray"],
    "red": ["red", "scarlet", "maroon", "cardinal", "puce", "chestnut", "rust", "mahogany"],
    "green": ["green", "moss_green", "sea_green", "emerald", "spring_green", "mint_green", "jade", "turquoise", "chartreuse", "green-yellow"],
    "yellow": ["yellow", "golden_yellow", "lemon", "amber", "saffron", "goldenrod"],
    "tan": ["brown", "dark_brown", "light_brown", "cinnamon"]
}

def get_base_color(df_color):
    for base_color, variants in COLOR_MAP.items():
        if df_color in variants:
            return base_color
    return "all"

def parse_mod_materials(filepath="ModMaterials.java"):
    rock_colors = {}
    all_rocks = set(ENV_SEDIMENTARY + ENV_IGNEOUS_INTRUSIVE + ENV_IGNEOUS_EXTRUSIVE + ENV_METAMORPHIC + ALLUVIAL)
    
    if not os.path.exists(filepath):
        # Дефолтные цвета пород, если ModMaterials.java не найден
        return {rock: ["black", "white", "red", "green", "yellow", "tan"] for rock in all_rocks}

    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read().lower()
        for rock in all_rocks:
            match = re.search(rf'\b{rock}\b[^\n;]+', content)
            if match:
                line = match.group(0)
                colors = [c for c in ["black", "white", "red", "green", "yellow", "tan"] if c in line]
                if colors:
                    rock_colors[rock] = colors
    return rock_colors

def parse_gems(filepath):
    gems = []
    current_gem = {}
    
    with open(filepath, 'r', encoding='utf-8') as f:
        for line in f:
            gem_match = re.search(r'\[INORGANIC:([^\]]+)\]', line)
            if gem_match:
                if current_gem.get("name"):
                    gems.append(current_gem)
                current_gem = {
                    "name": gem_match.group(1).lower().replace(' ', '_').replace('\'', ''),
                    "environments": defaultdict(int),
                    "color": "all"
                }
                continue
            
            color_match = re.search(r'\[STATE_COLOR:ALL_SOLID:([^\]]+)\]', line)
            if color_match and current_gem:
                current_gem["color"] = get_base_color(color_match.group(1).lower())
            
            env_match = re.search(r'\[ENVIRONMENT:([^:]+):[^:]+:(\d+)\]', line)
            if env_match and current_gem:
                env_type, weight = env_match.group(1), int(env_match.group(2))
                if env_type in ENV_MAP:
                    for rock in ENV_MAP[env_type]:
                        current_gem["environments"][rock] += weight
            
            env_spec_match = re.search(r'\[ENVIRONMENT_SPEC:([^:]+):[^:]+:(\d+)\]', line)
            if env_spec_match and current_gem:
                spec_rock, weight = env_spec_match.group(1).lower(), int(env_spec_match.group(2))
                rocks_to_add = FALLBACK_MINERALS.get(spec_rock, [spec_rock])
                for rock in rocks_to_add:
                    current_gem["environments"][rock] += weight
                    
    if current_gem.get("name"):
        gems.append(current_gem)
        
    return gems

def resolve_target_colors(base_color, available_colors):
    interchange = {"tan": "red", "red": "tan", "yellow": "green", "green": "yellow"}
    if base_color == "all":
        return available_colors[:4]
    if base_color in available_colors:
        return [base_color]
    alt_color = interchange.get(base_color)
    if alt_color and alt_color in available_colors:
        return [alt_color]
    return []

def export_gems_to_csv(gem_file, mod_materials_file, output_csv="gem_distribution.csv"):
    rock_colors = parse_mod_materials(mod_materials_file)
    gems = parse_gems(gem_file)
    
    rows = []
    
    for gem in gems:
        name = gem["name"]
        color = gem["color"]
        envs = gem["environments"]
        
        if not envs:
            rows.append({
                "gem_name": name,
                "base_color": color,
                "rock_type": "NONE",
                "original_weight": 0,
                "final_weight": 0,
                "target_colors": "NONE",
                "target_json_files": "NONE"
            })
            continue
            
        for rock, weight in envs.items():
            avail_colors = rock_colors.get(rock, [])
            target_colors = resolve_target_colors(color, avail_colors)
            
            final_weight = math.ceil(weight / 4.0) if color == "all" else weight
            
            target_colors_str = ",".join(target_colors) if target_colors else "NONE"
            target_jsons = ",".join([f"{c}_{rock}" for c in target_colors]) if target_colors else "NONE"
            
            rows.append({
                "gem_name": name,
                "weight": final_weight,
                "target": target_jsons
            })
            
    fieldnames = [
        "gem_name", 
        "weight", 
        "target"
    ]
    
    # utf-8-sig обеспечивает корректное открытие русского текста и разделителей в Excel
    with open(output_csv, mode='w', newline='', encoding='utf-8-sig') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames, delimiter=';')
        writer.writeheader()
        writer.writerows(rows)
        
    print(f"Экспорт завершен. Сформирован файл: {os.path.abspath(output_csv)}")

if __name__ == "__main__":
    export_gems_to_csv("inorganic_stone_gem.txt", "ModMaterials.java", "gem_distribution.csv")