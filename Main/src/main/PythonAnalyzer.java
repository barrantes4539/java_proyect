package main;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.*;

public class PythonAnalyzer {
    private File pythonFile;
    private int commentCount = 0;
    private int tokenCountEqual = 0;
    private int tokenCountLess = 0;
    private int tokenCountLessEqual = 0;
    private Set<String> declaredVariables = new HashSet<>(); // Conjunto para almacenar variables declaradas
    private Set<String> declaredFunctions = new HashSet<>();
    private int tokenCountNotEqual = 0;
    private boolean inImportSection = true; // Nueva variable booleana para detectar la sección de imports

    public PythonAnalyzer(File pythonFile) {
        this.pythonFile = pythonFile;
    }

    public void analyze() {
        String logFileName = pythonFile.getName().replace(".py", "-errores.log");
        File logFile = new File(logFileName);

        try (
            BufferedReader br = new BufferedReader(new FileReader(pythonFile));
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile))
        ) {
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                String formattedLineNumber = String.format("%03d", lineNumber);
                String originalLine = line;

                // Escribir la línea original en el archivo .log con tres dígitos en el número de línea
                bw.write(formattedLineNumber + " " + originalLine + "\n");

                // Detectar comentarios
                if (originalLine.trim().startsWith("#")) {
                    commentCount++;
                }

                // Detectar tokens de operadores
                detectTokens(originalLine);

                // Validar identificadores
                validateIdentifiers(originalLine, formattedLineNumber, bw);
                
                // Validar llamada a funciones no definidas
                validateFunctionCalls(originalLine, formattedLineNumber, bw);

                // Validar estructura de funciones
                validateFunctionStructure(originalLine, formattedLineNumber, bw);

                // Validación de imports usando la nueva variable booleana
                validateImports(originalLine, formattedLineNumber, bw);

                // Validación del comando input
                validateInput(originalLine, formattedLineNumber, bw);
                
                //Validacion de estructuras Try catch
                validateTryExceptBlocks(originalLine, formattedLineNumber, bw);
                
                // Validación de la función print
                validatePrintSyntax(originalLine, formattedLineNumber, bw);
                
                //Validacion de estructuras while
                validateWhileStructure(originalLine, formattedLineNumber, bw);
            }

            // Al final del archivo log, se agregan los totales
            writeSummary(bw);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void detectTokens(String line) {
        if (line.contains("==")) tokenCountEqual++;
        if (line.contains("<=")) tokenCountLessEqual++;
        if (line.contains("<") && !line.contains("<=")) tokenCountLess++;
        if (line.contains("!=")) tokenCountNotEqual++;
    }

    private void validateIdentifiers(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
        // Excluir líneas que no sean asignaciones de variables
        if (line.trim().startsWith("if") || 
            line.trim().startsWith("for") || 
            line.trim().startsWith("print") ||
            line.trim().startsWith("return") || 
            line.contains("==") || 
            line.contains("<=") || 
            line.contains("<") || 
            line.contains("!=") || 
            line.contains("(") || 
            line.contains(")")) {
            return;  // No validar estas líneas como identificadores
        }

        // Dividir la línea en nombre de variable y valor usando "="
        String[] parts = line.split("=", 2);
        if (parts.length < 2) {
            return;  // No es una asignación, salir.
        }

       String variableName = parts[0].trim(); // Extraer el nombre de la variable

        // Registrar la variable si es válida
        if (variableName.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            declaredVariables.add(variableName); // Registrar la variable como declarada
        } else {
            // Manejar error de identificador no válido
            String errorLine = "    -> Error 100: Identificador de variable no válido en la línea " + formattedLineNumber;
            if (variableName.matches("^[0-9].*")) {
                errorLine += ", el nombre de la variable no puede comenzar con un número.";
            } else if (variableName.matches(".*[^a-zA-Z0-9_].*")) {
                errorLine += ", el nombre de la variable contiene caracteres no permitidos.";
            }
            bw.write(errorLine + "\n");
        }
    }



    // Nueva validación de imports usando la variable booleana
    private void validateImports(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
        if (inImportSection) {
            // Si estamos en la sección de imports, validar si la línea empieza con 'import' o 'from'
            if (line.startsWith("import") || line.startsWith("from")) {
                // Sigue siendo parte de la sección de imports
                return;
            } else if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                // Si la línea no es un comentario ni está vacía, salimos de la sección de imports
                inImportSection = false;
            }
        }

        // Si ya hemos salido de la sección de imports y encontramos 'import' o 'from'
        if (!inImportSection && (line.startsWith("import") || line.startsWith("from"))) {
            bw.write("    -> Error 200: 'import' o 'from import' encontrado fuera de la sección permitida en la línea " + formattedLineNumber + ".\n");
        }
    }

    // Validación del comando input
    private void validateInput(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
        if (line.contains("input")) {
            int openParenthesisIndex = line.indexOf("(");
            int closeParenthesisIndex = line.lastIndexOf(")");
            String insideParentheses = "";
            if (openParenthesisIndex != -1 && closeParenthesisIndex != -1) {
                insideParentheses = line.substring(openParenthesisIndex + 1, closeParenthesisIndex);
            }
            int doubleQuotesCount = insideParentheses.length() - insideParentheses.replace("\"", "").length();
            int singleQuotesCount = insideParentheses.length() - insideParentheses.replace("'", "").length();

            if (openParenthesisIndex == -1 || closeParenthesisIndex == -1) {
                bw.write("    -> Error 300: La estructura del comando 'input' es incorrecta, faltan paréntesis.\n");
            } else {
                if ((doubleQuotesCount % 2 != 0) || (singleQuotesCount % 2 != 0)) {
                    bw.write("    -> Error 300: La estructura del comando 'input' es incorrecta, faltan comillas en la cadena de texto.\n");
                } else if (doubleQuotesCount == 0 && singleQuotesCount == 0) {
                    bw.write("    -> Error 300: La estructura del comando 'input' es incorrecta, falta una cadena de texto (comillas).\n");
                }
            }
        }
    }
    
    private Stack<Integer> indentStack = new Stack<>();
    private boolean isInsideTry = false;
    private boolean isInsideExcept = false;
    private boolean hasPrintInBlock = false;

    private void validateTryExceptBlocks(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
        if (line.trim().startsWith("try")) {
            isInsideTry = true;
            isInsideExcept = false;
            hasPrintInBlock = false;
            indentStack.push(getIndentLevel(line) + 1);  // Agregar nivel de indentación esperado
            validateTryBlock(line, formattedLineNumber, bw);
        } else if (line.trim().startsWith("except")) {
            isInsideTry = false;
            isInsideExcept = true;
            hasPrintInBlock = false;
            if (!indentStack.isEmpty()) indentStack.pop();  // Asegurar que no esté vacía antes de pop
            indentStack.push(getIndentLevel(line) + 1);
            validateExceptBlock(line, formattedLineNumber, bw);
        } else if (isInsideTry || isInsideExcept) {
            validateInsideBlock(line, formattedLineNumber, bw);
        }

        if (line.trim().isEmpty() || (!line.startsWith("    ") && !line.trim().startsWith("except") && !line.trim().startsWith("else"))) {
            if (isInsideExcept && !hasPrintInBlock) {
                bw.write("    -> Error 904: Falta un mensaje print dentro del bloque except en la línea " + formattedLineNumber + ".\n");
            }
            isInsideTry = false;
            isInsideExcept = false;
            if (!indentStack.isEmpty()) indentStack.pop();  // Verificar antes de hacer pop
        }
    }


    private void validateTryBlock(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
        if (!line.contains(":")) {
            bw.write("    -> Error 900: El bloque try no contiene los dos puntos (:) necesarios en la línea " + formattedLineNumber + ".\n");
        }
    }

    private void validateExceptBlock(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
        if (!line.contains("ArithmeticError") && !line.contains("ZeroDivisionError") && !line.contains("ValueError")) {
            bw.write("    -> Error 901: El bloque except contiene una excepción no permitida en la línea " + formattedLineNumber + ".\n");
        }
        if (!line.contains(":")) {
            bw.write("    -> Error 902: El bloque except no contiene los dos puntos (:) necesarios en la línea " + formattedLineNumber + ".\n");
        }
    }

    private void validateInsideBlock(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
        if (!indentStack.isEmpty() && getIndentLevel(line) < indentStack.peek()) {
            bw.write("    -> Error 903: Indentación incorrecta dentro del bloque " +
                    (isInsideTry ? "try" : "except") + " en la línea " + formattedLineNumber + ".\n");
        }

        if (isInsideExcept && line.trim().startsWith("print(")) {
            hasPrintInBlock = true;
        }
}



    private int getIndentLevel(String line) {
        int indentLevel = 0;
        // Contar espacios
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                indentLevel++;
            } else if (c == '\t') {
                indentLevel += 4; // Asumimos que cada tabulador equivale a 4 espacios
            } else {
                break;
            }
        }
        return indentLevel / 4; // Cada nivel corresponde a 4 espacios
    }


    private boolean isVariableDeclared(String variableName) {
        return declaredVariables.contains(variableName); // Verificar si la variable está en el conjunto
    }

    private void validatePrintSyntax(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
        // Eliminar espacios en blanco antes y después de la línea (para mayor seguridad)
        line = line.trim();

        // Excluir las líneas que contienen las excepciones específicas de print() basadas en patrones de sintaxis
        if (line.matches("print\\(f?\\\".*\\{.*\\}.*\\,\\s*end=.*\\)") || // print(f"...{var}...", end="")
            line.matches("print\\(\"\\|\",\\s*end=.*\\)") ||               // print("|", end="")
            line.matches("print\\(f?\\\".*\\{.*\\}\\s*\\{.*\\}.*\\)")) {   // print(f"...{var1}...{var2}...")
            return; // No validar estas líneas
        }

        // Detectar si falta el cierre de las comillas o paréntesis
        if (line.matches("print\\([\"'][^\"']*$")) { // print("texto o print('texto
            bw.write("    -> Error 908: Comillas no cerradas en la línea " + formattedLineNumber + ".\n");
            return;
        }

        // Detectar si falta el paréntesis de cierre
        if (line.matches("print\\([\"'][^)]*$")) { // print("texto o print('texto
            bw.write("    -> Error 909: Paréntesis no cerrados en la línea " + formattedLineNumber + ".\n");
            return;
        }

        // Verificar si hay paréntesis balanceados
        if (countOccurrences(line, '(') != countOccurrences(line, ')')) {
            bw.write("    -> Error 909: Paréntesis no balanceados en la línea " + formattedLineNumber + ".\n");
        }

        // Verificar si las comillas están balanceadas
        if (countOccurrences(line, '\"') % 2 != 0 || countOccurrences(line, '\'') % 2 != 0) {
            bw.write("    -> Error 908: Comillas no cerradas en la línea " + formattedLineNumber + ".\n");
        }

        // Validar sintaxis de print con comillas dobles o simples cerradas correctamente
        if (line.matches("print\\([\"'].+[\"']\\)")) { // print("texto") o print('texto')
            return; // Sintaxis válida
        }

        // Validar print(variable)
        if (line.matches("print\\([a-zA-Z_][a-zA-Z0-9_]*\\)")) { // print(variable)
            String variableName = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
            if (!isVariableDeclared(variableName)) {
                bw.write("    -> Error 907: La variable '" + variableName + "' no está declarada en la línea " + formattedLineNumber + ".\n");
            }
            return; // Sintaxis válida
        }

        // Validar print con concatenación de texto + variable
        if (line.matches("print\\([\"'].+\\+\\s*[a-zA-Z_][a-zA-Z0-9_]*\\)")) { // print("texto" + variable)
            return; // Sintaxis válida
        }
    }

    // Método auxiliar para contar las ocurrencias de un carácter en una cadena
    private int countOccurrences(String line, char ch) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }
    
//    private void validateFunctionStructure(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
//        // Validar que la línea contenga 'def' al principio para ser una función
//        if (line.trim().startsWith("def ") && line.contains("(") && line.contains(")")) {
//            String functionDefinition = line.trim();
//
//            // Validar que tiene los paréntesis y los dos puntos
//            if (!functionDefinition.contains("(") || !functionDefinition.contains(")")) {
//                bw.write("    -> Error 400: Paréntesis incorrectos en la definición de la función en la línea " + formattedLineNumber + ".\n");
//            } else if (!functionDefinition.endsWith(":")) {
//                bw.write("    -> Error 401: Faltan los dos puntos ':' al final de la definición de la función en la línea " + formattedLineNumber + ".\n");
//            } else {
//                // Extraer el nombre de la función de la definición (después de 'def' y antes de '(')
//                String functionName = functionDefinition.split("\\(")[0].replace("def", "").trim();
//
//                // Guardar el nombre de la función en declaredVariables
//                declaredVariables.add(functionName);
//            }
//
//            // Verificar la indentación de las siguientes líneas dentro de la función
//            int indentLevel = getIndentLevel(line); // Obtener el nivel de indentación de la línea de la función
//            boolean functionIndentationError = false;
//
//            // Verificar las líneas posteriores a la definición de la función
//            BufferedReader br = new BufferedReader(new FileReader(pythonFile));
//            String nextLine;
//            int nextLineNumber = Integer.parseInt(formattedLineNumber) + 1;
//
//            // Buscar la siguiente línea después de la definición de la función
//            while ((nextLine = br.readLine()) != null) {
//                nextLine = nextLine.trim();
//                if (nextLineNumber > Integer.parseInt(formattedLineNumber)) {
//                    // Comprobar si la línea de código dentro de la función está correctamente indentada
//                    if (nextLineNumber == Integer.parseInt(formattedLineNumber) + 1) {
//                        if (getIndentLevel(nextLine) <= indentLevel) {
//                            functionIndentationError = true;
//                            bw.write("    -> Error 402: Indentación incorrecta en la línea " + nextLineNumber + ", debe estar indentada dentro de la función.\n");
//                        }
//                    } else {
//                        break; // Salir del ciclo una vez que encontramos una línea fuera de la función
//                    }
//                }
//                nextLineNumber++;
//            }
//
//            // Verificar si se encuentra un 'return' dentro de la función
//            if (line.contains("return")) {
//                // Si el 'return' no está indentado correctamente, reportar error
//                if (getIndentLevel(line) <= indentLevel) {
//                    bw.write("    -> Error 403: El 'return' debe estar indentado correctamente dentro de la función en la línea " + formattedLineNumber + ".\n");
//                }
//            }
//        }
//    }
    private void validateFunctionStructure(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
        if (line.trim().startsWith("def ")) {
            // Extraer nombre de función y validación básica
            String functionSignature = line.trim();
            if (!functionSignature.contains("(") || !functionSignature.contains(")")) {
                bw.write("    -> Error 400: Paréntesis incorrectos en la definición de la función en la línea " + formattedLineNumber + ".\n");
            } else if (!functionSignature.endsWith(":")) {
                bw.write("    -> Error 401: Faltan los dos puntos ':' al final de la definición de la función en la línea " + formattedLineNumber + ".\n");
            } else {
                // Extraer nombre de función
                String functionName = functionSignature.substring(4, functionSignature.indexOf('(')).trim();
                declaredFunctions.add(functionName);
            }

            // Validar indentación de las líneas dentro de la función
            int expectedIndent = getIndentLevel(line) + 1;
            int currentLine = Integer.parseInt(formattedLineNumber);

            try (BufferedReader br = new BufferedReader(new FileReader(pythonFile))) {
                String nextLine;
                while ((nextLine = br.readLine()) != null) {
                    currentLine++;
                    int nextIndent = getIndentLevel(nextLine);

                    if (nextLine.trim().isEmpty()) continue; // Ignorar líneas en blanco
                    if (nextIndent < expectedIndent) {
                        break; // Salimos si encontramos una línea fuera del nivel esperado
                    }

                    if (nextIndent == expectedIndent && nextLine.trim().startsWith("return ")) {
                        // Validar si se incluye una declaración de retorno opcional
                        if (!nextLine.trim().matches("return\\s+[a-zA-Z_][a-zA-Z0-9_]*")) {
                            bw.write("    -> Error 402: Valor de retorno inválido en la línea " + String.format("%03d", currentLine) + ".\n");
                        }
                    }
                }
            } catch (IOException e) {
                bw.write("    -> Error al leer archivo: " + e.getMessage() + "\n");
            }
        }
    }



    
    private void validateFunctionCalls(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
        // Definir las palabras reservadas que no deben ser validadas como funciones
        Set<String> reservedKeywords = new HashSet<>(Arrays.asList("print", "if", "all", "for", "while", "else", "elif", "try", "except", "return", "break", "continue", "input"));

        if (line.contains("(") && line.contains(")")) {
            // Extraer el nombre de la función
            String functionName = line.substring(0, line.indexOf("(")).trim();

            // Comprobar si la función es una palabra reservada
            if (!reservedKeywords.contains(functionName)) {
                // Comprobar si la función está definida previamente
                if (functionName.matches("[a-zA-Z_][a-zA-Z0-9_]*") && !declaredFunctions.contains(functionName)) {
                    bw.write("    -> Error 700: Llamada a función no definida: '" + functionName + "' en la línea " + formattedLineNumber + ".\n");
                }
            }
        }
    }
    
    private void validateWhileStructure(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
        boolean missingColon = false;

        if (line.trim().startsWith("while ")) {
            // Validar que termina con ':'
            if (!line.trim().endsWith(":")) {
                bw.write("    -> Error 500: El bloque while no contiene los dos puntos (:) necesarios en la línea " + formattedLineNumber + ".\n");
                missingColon = true; // Marcamos que hay un error de sintaxis.
            }

            // Validar que contiene una condición válida
            String condition = line.trim().substring(5).split(":")[0].trim(); // Extrae la condición, sin asumir siempre ':'
            if (!isValidCondition(condition)) {
                bw.write("    -> Error 501: Condición inválida en la estructura while en la línea " + formattedLineNumber + ".\n");
            }

            // Si no hay error de dos puntos, validar la indentación
            if (!missingColon) {
                validateWhileIndentation(line, formattedLineNumber, bw);
            }
        }
    }

    private boolean isValidCondition(String condition) {
        // Validar una condición básica usando expresiones regulares para operadores lógicos
        String logicalPattern = "^[a-zA-Z_][a-zA-Z0-9_]*(\\s*[<>=!]=?\\s*[a-zA-Z0-9_]+)?$";
        return Pattern.matches(logicalPattern, condition);
    }

    private void validateWhileIndentation(String line, String formattedLineNumber, BufferedWriter bw) throws IOException {
        int indentLevel = getIndentLevel(line);
        boolean blockStarted = false;

        try (BufferedReader br = new BufferedReader(new FileReader(pythonFile))) {
            String nextLine;
            int lineNumber = Integer.parseInt(formattedLineNumber) + 1;

            while ((nextLine = br.readLine()) != null) {
                if (lineNumber > Integer.parseInt(formattedLineNumber)) {
                    if (nextLine.trim().isEmpty()) {
                        lineNumber++;
                        continue;
                    }

                    int nextIndentLevel = getIndentWhileLevel(nextLine);
                    if (!blockStarted && nextIndentLevel <= indentLevel) {
                        bw.write("    -> Error 502: Falta un bloque de código indentado después del while en la línea " + formattedLineNumber + ".\n");
                        break;
                    }

                    if (blockStarted && nextIndentLevel <= indentLevel) {
                        break; // Bloque terminado
                    }

                    if (nextIndentLevel > indentLevel) {
                        blockStarted = true;
                    }
                }
                lineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getIndentWhileLevel(String line) {
        return line.length() - line.stripLeading().length();
    }



    private void writeSummary(BufferedWriter bw) throws IOException {
        bw.write("\nResumen:\n");
        bw.write(commentCount + " líneas de comentario\n");
        bw.write(tokenCountEqual + " Token ==\n");
        bw.write(tokenCountLess + " Token <\n");
        bw.write(tokenCountLessEqual + " Token <=\n");
        bw.write(tokenCountNotEqual + " Token !=\n");
    }
}

//echo "# java_proyect" >> README.md
//git init
//git add README.md
//git commit -m "first commit"
//git branch -M main
//git remote add origin https://github.com/barrantes4539/java_proyect.git
//git push -u origin main

//Java -jar AnalizadorErrores.jar Juego_Gato.py
//echo. > Python_Gato.py
