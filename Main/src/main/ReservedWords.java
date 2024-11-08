package main;

public class ReservedWords {
    private String[] functionWords = {"print", "input", "def", "class", "lambda"};
    private String[] controlWords = {"break", "continue", "return", "if", "else", "elif", "while", "for", "import", "try", "except", "raise", "assert"};

    public boolean isFunctionWord(String word) {
        for (String func : functionWords) {
            if (word.equals(func)) {
                return true;
            }
        }
        return false;
    }

    public boolean isControlWord(String word) {
        for (String control : controlWords) {
            if (word.equals(control)) {
                return true;
            }
        }
        return false;
    }
}
