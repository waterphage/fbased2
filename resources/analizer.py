from PIL import Image
import numpy as np
import matplotlib.pyplot as plt
from scipy.special import erf


img = Image.open("noise.png").convert("L")

pixels = np.array(img).flatten()

# яркость -> [-1,1]
x = (pixels.astype(float) - 127.5) / 127.5

mean = np.mean(x)
std = np.std(x)
power = 2.0
x = erf((x - mean) / (std * np.sqrt(2)))


bins = np.linspace(-1, 1, 21)

hist, edges = np.histogram(
    x,
    bins=bins
)

percent = hist / len(x) * 100

for i in range(len(percent)):
    print(
        f"{edges[i]: .2f} .. {edges[i+1]: .2f}: "
        f"{percent[i]:5.2f}%"
    )
from scipy.special import erf
import numpy as np


inputs = np.linspace(-1, 1, 11)

outputs = erf((inputs - 0) / (std * np.sqrt(2)))

for x, y in zip(inputs, outputs):
        print(
        f'      {{'
        f'"location": {x:.3f}, '
        f'"derivative": {0.05:.3f}, '
        f'"value": {y:.4f}'
        f'}},'
    )