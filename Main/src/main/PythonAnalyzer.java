package main;

import java.io.*;
import java.util.regex.*;

public class PythonAnalyzer {
    private File pythonFile;
    private int commentCount = 0;
    private int tokenCountEqual = 0;
    private int tokenCountLess = 0;
    private int tokenCountLessEqual = 0;
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

                // Validación de imports usando la nueva variable booleana
                validateImports(originalLine, formattedLineNumber, bw);

                // Validación del comando input
                validateInput(originalLine, formattedLineNumber, bw);
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

        // Expresión regular para una declaración de variable válida
        Pattern pattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*=.*"); //Porción \\s* eliminada tal como se solicitó en primera revisión
        Matcher matcher = pattern.matcher(line.trim());

        if (line.contains("=") && !matcher.matches()) {
            String errorLine = "    -> Error 100: Identificador de variable no válido en la línea " + formattedLineNumber;
            if (line.trim().matches("^[0-9].*")) {
                errorLine += ", el nombre de la variable no puede comenzar con un número.";
            } else if (line.trim().matches(".*[^a-zA-Z0-9_=\\s].*")) {
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

    private void writeSummary(BufferedWriter bw) throws IOException {
        bw.write("\nResumen:\n");
        bw.write(commentCount + " líneas de comentario\n");
        bw.write(tokenCountEqual + " Token ==\n");
        bw.write(tokenCountLess + " Token <\n");
        bw.write(tokenCountLessEqual + " Token <=\n");
        bw.write(tokenCountNotEqual + " Token !=\n");
    }
}
