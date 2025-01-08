def modify_file(input_file, output_file, prefix, suffix):
    """
    Добавляет текст в начало и конец каждой строки файла.

    :param input_file: Путь к исходному файлу.
    :param output_file: Путь для сохранения измененного файла.
    :param prefix: Текст, добавляемый в начало каждой строки.
    :param suffix: Текст, добавляемый в конец каждой строки.
    """
    try:
        # Открываем файл для чтения
        with open(input_file, 'r', encoding='utf-8') as infile:
            lines = infile.readlines()

        # Добавляем текст к каждой строке
        modified_lines = [f"{prefix}{line.strip()}{suffix}\n" for line in lines]

        # Сохраняем результат в новый файл
        with open(output_file, 'w', encoding='utf-8') as outfile:
            outfile.writelines(modified_lines)

        print(f"Файл успешно сохранён: {output_file}")
    except Exception as e:
        print(f"Произошла ошибка: {e}")


if __name__ == "__main__":
    # Пути к файлам
    input_file = "output.txt"  # Исходный файл
    output_file = "output2.txt"  # Результат

# Выполняем изменение
modify_file(input_file, output_file,'          {"type": "minecraft:simple_state_provider","state": {"Name": "fbased:','"}},')
