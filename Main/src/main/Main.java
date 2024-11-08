package main;

import main.PythonAnalyzer;
import main.FileValidator;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: Debe proporcionar un archivo Python .py");
            return;
        }

        String pythonFileName = args[0];
        File pythonFile = new File(pythonFileName);

        // Validar archivo
        FileValidator validator = new FileValidator();
        if (!validator.validateFile(pythonFile)) {
            return;
        }

        // Realizar análisis
        PythonAnalyzer analyzer = new PythonAnalyzer(pythonFile);
        analyzer.analyze();

        System.out.println("Análisis completado. Los errores se han guardado en: " + pythonFileName.replace(".py", "-errores.log"));
    }
}
