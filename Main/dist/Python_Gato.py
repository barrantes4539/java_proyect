  # Ejemplo de errores en estructuras para validación

# Error 1: Falta de dos puntos en while
while True
    print("Esto es un bucle infinito.")

# Error 2: Falta de indentación en el bloque del while
while False:
print("Sin indentación")

# Error 3: Correcto while pero con indentación insuficiente
while True:
    print("Correcto")
print("Fuera del bucle incorrectamente")

# Error 4: Definición incorrecta de función sin dos puntos
def mi_funcion()
    print("Hola, mundo!")

# Error 5: Definición de función sin indentación en el cuerpo
def otra_funcion():
print("Hola, mundo!")

# Error 6: Llamada a función no definida
funcion_inexistente()

# Error 7: Definición de función correcta pero falta llamada
def funcion_correcta():
    print("Función definida correctamente")

# Error 8: Uso incorrecto de print
print(variable_no_definida)  # Error: Variable no definida

# Error 9: Correcta definición de print pero variable inexistente
variable_correcta = 10
print(variable_no_existente)

# Error 10: Bloque try sin except
try:
    x = 1 / 0
# No hay except

# Error 11: Except con excepción no permitida
try:
    x = 1 / 0
except KeyError:  # Excepción no permitida
    print("Esto es un error de clave.")

# Error 12: Except correcto, pero falta print en el bloque
try:
    x = int("abc")
except ValueError:
    pass  # No hay print aquí, error.

# Error 13: Correcto try/except pero sin indentación
try:
x = 1 / 0
except ZeroDivisionError:
print("División entre cero.")


