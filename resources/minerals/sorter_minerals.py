import re
import os
import csv
from collections import defaultdict


# ============================================================
# СРЕДЫ
# ============================================================

ENV_SEDIMENTARY = [
    "sandstone", "siltstone", "mudstone", "shale", "claystone",
    "rock_salt", "limestone", "conglomerate", "dolomite", "chert", "chalk"
]

ENV_IGNEOUS_INTRUSIVE = [
    "granite", "diorite", "gabbro"
]

ENV_IGNEOUS_EXTRUSIVE = [
    "rhyolite", "basalt", "andesite", "dacite", "obsidian"
]

ENV_METAMORPHIC = [
    "quartzite", "slate", "phyllite", "schist", "gneiss", "marble"
]

# Для аллювиальных отложений сохраняем старую привязку.
ALLUVIAL = [
    "siltstone", "mudstone", "claystone",
    "rock_salt", "dolomite", "chert"
]

ENV_MAP = {
    "SEDIMENTARY": ENV_SEDIMENTARY,
    "IGNEOUS_INTRUSIVE": ENV_IGNEOUS_INTRUSIVE,
    "IGNEOUS_EXTRUSIVE": ENV_IGNEOUS_EXTRUSIVE,
    "IGNEOUS_ALL": ENV_IGNEOUS_INTRUSIVE + ENV_IGNEOUS_EXTRUSIVE,
    "METAMORPHIC": ENV_METAMORPHIC,

    "ALL_STONE": (
        ENV_SEDIMENTARY
        + ENV_IGNEOUS_INTRUSIVE
        + ENV_IGNEOUS_EXTRUSIVE
        + ENV_METAMORPHIC
    ),

    "ALLUVIAL": ALLUVIAL,
}


# ============================================================
# ВСЕ КОНЕЧНЫЕ ПОРОДЫ
# ============================================================

ALL_ROCKS = set(
    ENV_SEDIMENTARY
    + ENV_IGNEOUS_INTRUSIVE
    + ENV_IGNEOUS_EXTRUSIVE
    + ENV_METAMORPHIC
    + ALLUVIAL
)


# ============================================================
# ЦВЕТА
# ============================================================

COLOR_MAP = {

    "white": [
        "white", "cream", "ivory", "pearl", "clear",
        "silver", "beige", "flax", "buff"
    ],

    "black": [
        "black", "gray", "slate_gray", "charcoal",
        "taupe_dark", "taupe_gray", "olive",
        "midnight_blue", "dark_indigo"
    ],

    "red": [
        "red", "maroon", "vermilion", "scarlet",
        "cardinal", "puce", "chestnut", "rust",
        "mahogany", "burnt_umber"
    ],

    "green": [
        "green", "moss_green", "sea_green", "emerald",
        "spring_green", "mint_green", "jade", "turquoise",
        "chartreuse", "green_yellow", "yellow_green",
        "aqua", "aquamarine", "teal"
    ],

    "yellow": [
        "yellow", "gold", "golden_yellow", "lemon",
        "amber", "saffron", "goldenrod", "brass",
        "copper"
    ],

    "tan": [
        "brown", "dark_brown", "light_brown", "cinnamon",
        "dark_tan", "taupe_medium", "taupe_sandy",
        "beige", "ecru", "raw_umber"
    ],
}


COLOR_FALLBACK = {
    "plum": "black",
    "periwinkle": "white",
    "cerulean": "black",
    "dark_indigo": "black",
    "purple": "black",
    "violet": "black",
    "lavender": "white",
    "amethyst": "black",
    "dark_violet": "black",
    "indigo": "black",
    "olive": "black",
}


def normalize_name(name):
    """
    Нормализация имён минералов и пород.
    """

    return (
        name
        .strip()
        .lower()
        .replace("-", "_")
        .replace(" ", "_")
    )


def get_base_color(df_color):
    """
    Переводит цвет из inorganic-файла
    в базовый цвет текстуры.
    """

    df_color = normalize_name(df_color)

    for base_color, variants in COLOR_MAP.items():

        if df_color in variants:
            return base_color

    if df_color in COLOR_FALLBACK:
        return COLOR_FALLBACK[df_color]

    return "all"


# ============================================================
# РАЗБОР ModMaterials.java
# ============================================================

def parse_mod_materials(filepath="ModMaterials.java"):

    """
    Ищет, какие цветовые текстуры существуют
    у каждой породы.
    """

    rock_colors = {}

    all_rocks = set(
        ENV_SEDIMENTARY
        + ENV_IGNEOUS_INTRUSIVE
        + ENV_IGNEOUS_EXTRUSIVE
        + ENV_METAMORPHIC
        + ALLUVIAL
    )

    if not os.path.exists(filepath):

        default = [
            "black",
            "white",
            "red",
            "green",
            "yellow",
            "tan"
        ]

        return {
            rock: default[:]
            for rock in all_rocks
        }

    with open(
        filepath,
        "r",
        encoding="utf-8"
    ) as f:

        content = f.read().lower()

    base_colors = list(COLOR_MAP.keys())

    for rock in all_rocks:

        match = re.search(
            rf"\b{re.escape(rock)}\b[^\n;]*",
            content
        )

        if not match:
            continue

        line = match.group(0)

        colors = [
            color
            for color in base_colors
            if re.search(
                rf"\b{re.escape(color)}\b",
                line
            )
        ]

        if colors:
            rock_colors[rock] = colors

    return rock_colors


# ============================================================
# ПАРСИНГ INORGANIC
# ============================================================

def parse_minerals(filepath):

    """
    Читает inorganic_stone_mineral.txt.

    Для каждого минерала сохраняются:

        color
        direct_envs
        spec_hosts
    """

    minerals = {}

    with open(
        filepath,
        "r",
        encoding="utf-8"
    ) as f:

        content = f.read()

    # Каждый блок начинается с [INORGANIC:...
    blocks = re.split(
        r"(?=\[INORGANIC:)",
        content,
        flags=re.IGNORECASE
    )

    for block in blocks:

        mineral_match = re.search(
            r"\[INORGANIC:([^\]]+)\]",
            block,
            re.IGNORECASE
        )

        if not mineral_match:
            continue

        name = normalize_name(
            mineral_match.group(1)
        )

        # ----------------------------------------------------
        # Цвет
        # ----------------------------------------------------

        color_match = re.search(
            r"\[STATE_COLOR:ALL_SOLID:([^\]]+)\]",
            block,
            re.IGNORECASE
        )

        color = (
            get_base_color(
                color_match.group(1)
            )
            if color_match
            else "all"
        )

        # ----------------------------------------------------
        # Прямые ENVIRONMENT
        # ----------------------------------------------------

        direct_envs = []

        for match in re.finditer(
            r"\[ENVIRONMENT:([^:\]]+):([^:\]]+):",
            block,
            re.IGNORECASE
        ):

            env_type = match.group(1).strip().upper()
            occurrence_type = match.group(2).strip().upper()

            direct_envs.append(
                (
                    env_type,
                    occurrence_type
                )
            )

        # ----------------------------------------------------
        # ENVIRONMENT_SPEC
        # ----------------------------------------------------

        spec_hosts = []

        for match in re.finditer(
            r"\[ENVIRONMENT_SPEC:([^:\]]+):([^:\]]+):",
            block,
            re.IGNORECASE
        ):

            host = normalize_name(
                match.group(1)
            )

            occurrence_type = (
                match.group(2)
                .strip()
                .upper()
            )

            spec_hosts.append(
                (
                    host,
                    occurrence_type
                )
            )

        minerals[name] = {
            "color": color,
            "direct_envs": direct_envs,
            "spec_hosts": spec_hosts,
        }

    return minerals


# ============================================================
# ГРАФ ENVIRONMENT_SPEC
# ============================================================

def build_environment_graph(minerals):

    """
    Создаёт граф:

        mineral -> mineral

    Например:

        anhydrite -> gypsum
        serpentine -> olivine
        chromite -> olivine
    """

    graph = {}

    for name, data in minerals.items():

        graph[normalize_name(name)] = [
            normalize_name(host)
            for host, _ in data["spec_hosts"]
        ]

    return graph


# ============================================================
# РЕКУРСИВНОЕ РАЗВОРАЧИВАНИЕ
# ============================================================

def expand_host(
    host,
    minerals,
    graph,
    visiting=None
):

    """
    Превращает ENVIRONMENT_SPEC
    в конечные породы.

    Например:

        anhydrite
            -> gypsum
                -> SEDIMENTARY
                    -> sandstone
                    -> limestone
                    -> shale
                    ...

    Или:

        serpentine
            -> olivine
                -> gabbro
    """

    if visiting is None:
        visiting = set()

    host = normalize_name(host)

    # --------------------------------------------------------
    # Это уже конечная порода
    # --------------------------------------------------------

    if host in ALL_ROCKS:
        return {host}

    # --------------------------------------------------------
    # Защита от циклов
    # --------------------------------------------------------

    if host in visiting:

        print(
            f"ПРЕДУПРЕЖДЕНИЕ: цикл ENVIRONMENT_SPEC: "
            f"{host}"
        )

        return set()

    # --------------------------------------------------------
    # Минерал не найден
    # --------------------------------------------------------

    if host not in minerals:

        print(
            f"ПРЕДУПРЕЖДЕНИЕ: "
            f"ENVIRONMENT_SPEC ссылается "
            f"на неизвестный объект: {host}"
        )

        return set()

    visiting.add(host)

    result = set()

    data = minerals[host]

    # --------------------------------------------------------
    # Прямые ENVIRONMENT
    # --------------------------------------------------------

    for env_type, occurrence_type in data["direct_envs"]:

        env_type = env_type.upper()

        rocks = ENV_MAP.get(env_type)

        if rocks is None:

            print(
                f"ПРЕДУПРЕЖДЕНИЕ: неизвестный "
                f"ENVIRONMENT {env_type} "
                f"у {host}"
            )

            continue

        result.update(rocks)

    # --------------------------------------------------------
    # Вложенные ENVIRONMENT_SPEC
    # --------------------------------------------------------

    for sub_host in graph.get(host, []):

        result.update(
            expand_host(
                sub_host,
                minerals,
                graph,
                visiting
            )
        )

    visiting.remove(host)

    return result


# ============================================================
# РАЗРЕШЕНИЕ СРЕДЫ МИНЕРАЛА
# ============================================================

def resolve_mineral_environments(
    mineral_name,
    minerals,
    graph
):

    """
    Возвращает итоговые породы-носители минерала.

    Учитываются:

        ENVIRONMENT
        ENVIRONMENT_SPEC
        ENVIRONMENT_SPEC -> ENVIRONMENT_SPEC
        ENVIRONMENT_SPEC -> ENVIRONMENT
    """

    mineral_name = normalize_name(
        mineral_name
    )

    data = minerals[mineral_name]

    result = defaultdict(int)

    # --------------------------------------------------------
    # Прямой ENVIRONMENT
    # --------------------------------------------------------

    for env_type, occurrence_type in data["direct_envs"]:

        rocks = ENV_MAP.get(
            env_type.upper(),
            []
        )

        for rock in rocks:

            result[
                (
                    rock,
                    occurrence_type
                )
            ] += 1

    # --------------------------------------------------------
    # ENVIRONMENT_SPEC
    # --------------------------------------------------------

    for host, occurrence_type in data["spec_hosts"]:

        rocks = expand_host(
            host,
            minerals,
            graph
        )

        for rock in rocks:

            result[
                (
                    rock,
                    occurrence_type
                )
            ] += 1

    return result


# ============================================================
# ЦВЕТОВЫЕ ЦЕЛИ
# ============================================================

def resolve_target_colors(
    base_color,
    available_colors
):

    """
    Подбирает существующую у породы
    цветовую текстуру.
    """

    interchange = {

        "tan": "red",
        "red": "tan",

        "yellow": "green",
        "green": "yellow",

        "white": "tan",
        "black": "white",
    }

    if base_color == "all":

        return available_colors[:4]

    if base_color in available_colors:

        return [base_color]

    alt_color = interchange.get(
        base_color
    )

    if (
        alt_color
        and alt_color in available_colors
    ):

        return [alt_color]

    return []


# ============================================================
# ЭКСПОРТ
# ============================================================

def export_minerals_to_csv(
    mineral_file,
    mod_materials_file,
    output_csv="mineral_distribution.csv"
):

    print("Чтение ModMaterials.java...")

    rock_colors = parse_mod_materials(
        mod_materials_file
    )

    print("Чтение inorganic_stone_mineral.txt...")

    minerals = parse_minerals(
        mineral_file
    )

    print(
        f"Найдено минералов: {len(minerals)}"
    )

    graph = build_environment_graph(
        minerals
    )

    rows = []

    # --------------------------------------------------------
    # Основная обработка
    # --------------------------------------------------------

    for name, data in minerals.items():

        color = data["color"]

        envs = resolve_mineral_environments(
            name,
            minerals,
            graph
        )

        # ----------------------------------------------------
        # Нет среды
        # ----------------------------------------------------

        if not envs:

            rows.append({

                "mineral_name": name,

                "type": "NONE",

                "target": "NONE",
            })

            continue

        # ----------------------------------------------------
        # Есть среды
        # ----------------------------------------------------

        for (
            rock,
            occurrence_type
        ), _count in sorted(envs.items()):

            available_colors = rock_colors.get(
                rock,
                []
            )

            target_colors = resolve_target_colors(
                color,
                available_colors
            )

            if target_colors:

                target_jsons = ",".join(
                    f"{c}_{rock}"
                    for c in target_colors
                )

            else:

                target_jsons = "NONE"

            rows.append({

                "mineral_name": name,

                "type": occurrence_type,

                "target": target_jsons,
            })

    # --------------------------------------------------------
    # CSV
    # --------------------------------------------------------

    with open(
        output_csv,
        mode="w",
        newline="",
        encoding="utf-8-sig"
    ) as f:

        writer = csv.DictWriter(
            f,
            fieldnames=[
                "mineral_name",
                "type",
                "target"
            ],
            delimiter=";"
        )

        writer.writeheader()

        writer.writerows(rows)

    print()
    print(
        "Экспорт завершен."
    )

    print(
        "Сформирован файл:",
        os.path.abspath(output_csv)
    )


# ============================================================
# ЗАПУСК
# ============================================================

if __name__ == "__main__":

    export_minerals_to_csv(
        "inorganic_stone_mineral.txt",
        "ModMaterials.java",
        "mineral_distribution.csv"
    )