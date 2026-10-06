import pandas as pd

input_file = "mineral_distribution.csv"
output_file = "mineral_by_target.csv"

df = pd.read_csv(input_file, sep=";")

# Словарь соответствия типов и значений spacing
spacing_map = {
    "VEIN": 1,
    "CLUSTER": 2,
    "CLUSTER_SMALL": 2
}

# Добавляем столбец spacing на основе столбца type
df["spacing"] = df["type"].map(spacing_map)

result = (
    df.groupby("target", sort=True)
    .agg({
        "mineral_name": lambda x: ",".join(x),
        "type": lambda x: ",".join(x),
        "spacing": "sum"  # Суммируем значения spacing для каждого target
    })
    .reset_index()
)

result.columns = ["Target", "minerals", "types","spacing"]

result.to_csv(output_file, sep=";", index=False)

print(f"Готово: {output_file}")