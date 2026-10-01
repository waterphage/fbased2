import math
import random
from dataclasses import dataclass
from typing import Generic, List, TypeVar
from PIL import Image, ImageDraw, ImageFont
import os

T = TypeVar("T")
@dataclass
class point:
    Xl: float
    Yl: float
    def d(self,X:float,Y:float)->float:
        return ((X-self.Xl)**2+(Y-self.Yl)**2)**(0.5)+1e-9
@dataclass
class point2:
    Xl: float
    Yl: float
    Ax: float
    By: float
    Ф: float
    @property
    def Фr(self):return math.radians(self.Ф)
    def xl(self,X:float,Y:float)->float:
        return (X-self.Xl)*math.cos(self.Фr)*self.Ax+(Y-self.Yl)*math.sin(self.Фr)*self.By
    def yl(self,X:float,Y:float)->float:
        return -(X-self.Xl)*math.sin(self.Фr)*self.Ax+(Y-self.Yl)*math.cos(self.Фr)*self.By
    def d(self,X:float,Y:float)->float:
        xl=point2.xl(self,X,Y)
        yl=point2.yl(self,X,Y)
        return ((xl)**2+(yl)**2)**(0.5)+1e-9
@dataclass
class line:
    Xl: float
    Yl: float
    Ax: float
    By: float
    def d(self,X:float,Y:float)->float:
        return abs(self.Ax*X+self.By*Y-self.Xl*self.Ax-self.Yl*self.By)/((self.Ax**2+self.By**2)**0.5)+1e-9
@dataclass
class cont:
    def d(self,X:float,Y:float)->float: return 1.0


@dataclass
class Prompt(Generic[T]):
    type: type
    factor: float
    power: float
    layer: float
    tex: bool
    pattern: List[T]

    def weight(self, X: float, Y: float) -> float:
        return (self.type.d(X,Y)**-self.power)*self.factor

    def get(self, X: float, Y: float) -> T:
        if self.tex:
            return random.choice(self.pattern)
        dist = self.type.d(X,Y)
        idx = round(dist * self.layer) % len(self.pattern)
        return self.pattern[idx]


@dataclass
class PMesh(Generic[T]):
    xS: int
    yS: int
    X_min: float
    X_max: float
    Y_min: float
    Y_max: float
    prompts: List[Prompt[T]]

    def fill(self) -> List[T]:
        result = []

        for y in range(self.yS):
            for x in range(self.xS):

                X = self.X_min + x * (self.X_max - self.X_min) / (self.xS - 1)
                Y = self.Y_min + y * (self.Y_max - self.Y_min) / (self.yS - 1)

                best_prompt = None
                best_weight = float("-inf")

                for prompt in self.prompts:
                    w = prompt.weight(X, Y)
                    if w > best_weight:
                        best_weight = w
                        best_prompt = prompt

                result.append(best_prompt.get(X, Y))

        return result
f_1=0.10156
p_1=1e-5
x_goal=61.0
y_goal=31.5
mesh = PMesh(
    xS=64,
    yS=64,
    X_min=-1.0,
    X_max=1.0,
    Y_min=-1.0,
    Y_max=1.0,
    prompts=[
        Prompt(
            type=point(30.5/31.5,-30.5/31.5),
            factor=f_1,
            power=f_1,
            layer=31.5/2,
            tex=False,
            pattern=[
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_basalt_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_obsidian_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_gabbro_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_diorite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_dacite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_andesite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_rhyolite_raw"}}',

                     ]),

                Prompt(
            type=point2(1.0,-1/31.5,1,1,0),
            factor=0.0545,
            power=1.0,
            layer=31.5,
            tex=False,
            pattern=[
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:tan_slate_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_slate_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_slate_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_slate_raw"}}',

                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_phyllite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_phyllite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_phyllite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_phyllite_raw"}}',

                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:yellow_chert_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:tan_chert_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_chert_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_chert_raw"}}',

                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_marble_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_marble_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_marble_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_marble_raw"}}',

                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_shale_raw"}}',
            ]),
        Prompt(
            type=point2(1.0,1/31.5,1,1,0),
            factor=0.0545,
            power=1.0,
            layer=31.5,
            tex=False,
            pattern=[
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:tan_quartzite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_quartzite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_quartzite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_quartzite_raw"}}',

                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_gneiss_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_gneiss_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_gneiss_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_gneiss_raw"}}',

                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_schist_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_schist_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_schist_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_schist_raw"}}',

                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_granite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_granite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_granite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_granite_raw"}}',

                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_obsidian_raw"}}',
                     ]),
        Prompt(
            type=point(1.0-2.05/31.5,1.0),
            factor=f_1,
            power=f_1,
            layer=31.5/2,
            tex=False,
            pattern=[
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_basalt_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_obsidian_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_gabbro_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_diorite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_dacite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_andesite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_rhyolite_raw"}}',

                     ]),
        Prompt(
            type=point(1.0,1.0-2.05/31.5),
            factor=f_1,
            power=f_1,
            layer=31.5/2,
            tex=False,
            pattern=[
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_basalt_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_obsidian_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_gabbro_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_diorite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_dacite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_andesite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:white_rhyolite_raw"}}',
                     ]),
        Prompt(
            type=point(1.0-1/31.5,1.0-1/31.5*(math.sqrt(3.0))),
            factor=f_1,
            power=f_1,
            layer=31.5/2,
            tex=False,
            pattern=[
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_basalt_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_obsidian_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_gabbro_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_diorite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_dacite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_andesite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:red_rhyolite_raw"}}',
                     ]),
        Prompt(
            type=point(1.0-1/31.5*(math.sqrt(3.0)),1.0-1/31.5),
            factor=f_1,
            power=f_1,
            layer=31.5/2,
            tex=False,
            pattern=[
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_basalt_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_obsidian_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_gabbro_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_diorite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_dacite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name":"fbased:green_andesite_raw"}}',
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:green_rhyolite_raw"}}',
                     ]),
        Prompt(
            type=point(1.0-0.75/31.5,1.0-0.75/31.5),
            factor=f_1*0.99,
            power=f_1,
            layer=31.5,
            tex=False,
            pattern=[
                '{"type": "minecraft:simple_state_provider","state": {"Name": "fbased:black_basalt_raw"}}',
                     ]),
        Prompt(
            type=cont(),
            factor=1.0/9.0,
            power=0,
            layer=1.0,
            tex=True,
            pattern=['{"type": "minecraft:simple_state_provider","state": {"Name": "minecraft:pink_concrete"}}'
                     ])
    ]
)



variants=mesh.fill()
file = "mesh.txt" 
with open(file, 'w', encoding='utf-8') as outfile:
    modified_lines = [f"    {v},\n" for v in variants]
    outfile.writelines(modified_lines)

json_file = r"C:\Users\1\Desktop\fb\src\main\resources\data\fbased\worldgen\noise_settings\overworld.json"

with open("mesh.txt", "r", encoding="utf-8") as f:
    mesh_lines = f.readlines()

assert len(mesh_lines) == 4096

with open(json_file, "r", encoding="utf-8") as f:
    lines = f.readlines()
lines[150:4246] = mesh_lines
lines[4246] = lines[4246].rstrip(",\n").__add__("\n")

with open(json_file, "w", encoding="utf-8") as f:
    f.writelines(lines)


SIZE = 64
FILE = "mesh.txt"
OUTPUT = "mesh_with_legend.png"

# читаем исходные строки без изменения текста
with open(FILE, "r", encoding="utf-8") as f:
    values = [line.rstrip("\n") for line in f if line.rstrip("\n")]


# создаём цвета для каждого уникального текста
palette = {}

for text in values:
    if text not in palette:
        palette[text] = (
            random.randint(0, 255),
            random.randint(0, 255),
            random.randint(0, 255)
        )


# создаём карту
map_img = Image.new("RGB", (SIZE, SIZE), "white")
pixels = map_img.load()

for i, text in enumerate(values):
    if i >= SIZE * SIZE:
        break

    x = i % SIZE
    y = i // SIZE

    pixels[x, y] = palette[text]


# параметры легенды
legend_width = 300
row_height = 20
scale = 8

width = legend_width + SIZE* scale
height = max(SIZE* scale, len(palette) * row_height)

img = Image.new("RGB", (width, height), "white")


map_img = map_img.resize(
    (SIZE * scale, SIZE * scale),
    Image.Resampling.NEAREST
)
# карта справа
img.paste(map_img, (legend_width, 0))

draw = ImageDraw.Draw(img)

try:
    font = ImageFont.truetype("arial.ttf", 12)
except:
    font = ImageFont.load_default()


# легенда с исходными строками
for i, (text, color) in enumerate(palette.items()):
    y = i * row_height

    draw.rectangle(
        [5, y + 3, 17, y + 15],
        fill=color
    )

    draw.text(
        (25, y),
        text,   # <-- оригинальный текст из mesh.txt
        fill="black",
        font=font
    )


img.save(OUTPUT)
json_file2 = r"C:\Users\1\Desktop\fb\src\main\resources\data\fbased\worldgen\density_function\cont.json"
with open(json_file2, 'w', encoding='utf-8') as outfile:
    outfile.writelines(f'{x_goal/31.5-1}')
json_file2 = r"C:\Users\1\Desktop\fb\src\main\resources\data\fbased\worldgen\density_function\eros.json"
with open(json_file2, 'w', encoding='utf-8') as outfile:
    outfile.writelines(f'{-1+y_goal/31.5}')

print(f"Готово: {OUTPUT}")