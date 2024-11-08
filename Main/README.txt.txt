README.txt

Descripción del Proyecto
Este proyecto consiste en un programa en Java que analiza un archivo Python (.py) ubicado en la carpeta dist del proyecto para identificar posibles errores de sintaxis y generar un archivo de log con la extensión .log. El programa busca errores relacionados con identificadores de variables no válidos, el uso incorrecto de la instrucción import (que debe estar al inicio del archivo), y además cuenta la cantidad de comentarios y ciertos operadores (==, <, <=) presentes en el archivo Python. Cabe recalcar que el archivo .jar tambien debe estar en el mismo nivel de jerarquía que el archivo Python, es decir, en la carpeta dist.

Requisitos Previos
Tener instalado Java Development Kit (JDK).
Tener configurado Apache Ant y NetBeans (opcional para facilitar la compilación y ejecución del proyecto).
Contar con el archivo Python que será analizado (por ejemplo, Juego_Gato.py).

*********Instrucciones de Ejecución********

1. Compilación
Si está utilizando la terminal o el símbolo del sistema (Command Prompt), siga estos pasos para compilar el archivo Java:

	1.a. Abra la terminal y navegue al directorio donde se encuentra el archivo Main.java. (~\Main\src\main\Main.java)
	1.b. Compile el archivo ejecutando el siguiente comando: javac Main.java

2. Ejecución del Programa

	2.a. Ubiquese en la carpeta dist del proyecto usando el comando cd. (~\Main\dist)
	2.b. Una vez compilado y ubicado en la carpeta dist, ejecute el programa proporcionando el archivo Python que desea analizar como argumento. Use el 	siguiente comando: Java -jar AnalizadorErrores.jar Juego_Gato.py. 	
	2.c. Enc caso de querer analizar otro archivo .py, asegurese de sustituir Juego_Gato.py por el archivo de python deseado y ubicarlo en la carpeta dist. 

3. Salida del Programa
Si el archivo Python contiene errores de sintaxis o identificadores no válidos, se creará un archivo de log con los errores encontrados. Este archivo tendrá el mismo nombre que el archivo Python, pero con la extensión .log.

Por ejemplo, si se ejecuta el programa con Juego_Gato.py, se generará un archivo llamado Juego_Gato-errores.log.

En este archivo log se incluirán los siguientes elementos:

	3.a. Errores de identificadores de variables no válidos.
	3.b. Errores relacionados con la instrucción import en líneas posteriores 	a la primera.
	3.c Totales de líneas de comentario.
	3.d Totales de tokens ==, <, <=.

4. Ejemplo de Ejecución
Si ejecuta el programa con el archivo Python Juego_Gato.py, la salida de la terminal será algo similar a: "Análisis completado. Los errores se han guardado en: Juego_Gato-errores.log"

En el archivo Juego_Gato-errores.log se encontrarán detalles sobre los errores detectados y un resumen de los comentarios y operadores encontrados en el código.

5. Manejo de Errores
Si no se proporciona un archivo Python como argumento, o si el archivo proporcionado no tiene la extensión .py, el programa mostrará un mensaje de error en la terminal, como por ejemplo: "Error: Debe proporcionar un archivo Python .py"

Si el archivo no existe, el programa mostrará: "Error: El archivo no existe"

6. Notas adicionales
Asegúrese de que el archivo Python que desea analizar esté en la carpeta dist, al igual que el archivo .jar que requiera crear

	6.1. Si se desea cambiar el nombre del archivo .jar, debe cambiar la linea del build.xml para cambiar el nombre default del archivo .jar
		6.1.1. <jar destfile="dist/AnalizadorErrores.jar" basedir="build">, el nombre AnalizadorErrores.jar puede sustituirlo por otro nombre en esa linea del archivo xml, ejemplo, Analizador.jar

