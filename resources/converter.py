import numpy as np
import matplotlib.pyplot as plt
from opensimplex import OpenSimplex


# Настройки
SIZE = 1000
SAMPLES = SIZE * SIZE

noise = OpenSimplex(seed=12345)


# Аналог одной октавы Minecraft
# firstOctave влияет на масштаб, здесь просто крупный шум
scale = 2 ** -11


values = []

for x in range(SIZE):
    for z in range(SIZE):
        v = noise.noise2(
            x * scale,
            z * scale
        )
        values.append(v)
    if(x%10==0):print(f"{x/10}%")


values = np.array(values)


def remap(x, power):
    return np.sign(x) * np.power(np.abs(x), power)


powers = [
    1.0,
    0.8,
    0.7,
    0.6,
    0.5
]


plt.figure(figsize=(12, 8))

for i, p in enumerate(powers):
    transformed = remap(values, p)

    plt.subplot(3, 2, i + 1)

    plt.hist(
        transformed,
        bins=100,
        density=True
    )

    plt.title(f"power = {p}")
    plt.xlim(-1, 1)
    plt.ylim(0, 3)


plt.tight_layout()
plt.show()


# Численная проверка равномерности
print("Среднее:", values.mean())
print("Стандартное отклонение:", values.std())

for p in powers:
    y = remap(values, p)

    # Насколько близко к равномерному:
    # у равномерного [-1,1] std = 0.577
    print(
        f"power {p}:",
        "mean=", round(y.mean(), 4),
        "std=", round(y.std(), 4)
    )