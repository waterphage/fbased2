import re

# Читаем исходный файл
with open("inorganic_stone_gem.txt", "r", encoding="utf-8") as f:
    text = f.read()

# Разбиваем на блоки INORGANIC
blocks = re.findall(
    r"\[INORGANIC:([^\]]+)\](.*?)(?=\[INORGANIC:|\Z)",
    text,
    re.S
)

# Записываем результат
with open("output.txt", "w", encoding="utf-8") as out:
    for inorganic, block in blocks:
        envs = []

        # Все ENVIRONMENT
        for env in re.findall(r"\[ENVIRONMENT:([^:\]]+)", block):
            envs.append(env)
        cont=""
        cont2="s"
        # Все ENVIRONMENT_SPEC (отрезаем всё после "_")
        for env in re.findall(r"\[ENVIRONMENT_SPEC:([^:\]]+)", block):
            cont=env.split("_")[0]
            if (cont!=cont2):
                envs.append(env.split("_")[0])
            cont2=cont

        if envs:
            out.write(f"{inorganic} {' '.join(envs)}\n")
        else:
            out.write(f"{inorganic}\n")

print("Готово.")