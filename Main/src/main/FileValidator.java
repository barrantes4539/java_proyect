package main;

import java.io.File;

public class FileValidator {

    public boolean validateFile(File file) {
        if (!file.getName().endsWith(".py")) {
            System.out.println("Error: Solo se permiten archivos con extensión .py");
            return false;
        }

        if (!file.exists()) {
            System.out.println("Error: El archivo no existe");
            return false;
        }

        return true;
    }
}