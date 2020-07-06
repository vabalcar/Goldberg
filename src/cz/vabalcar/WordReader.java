package cz.vabalcar;

import java.io.*;
import java.util.function.Predicate;

class WordReader implements AutoCloseable {
    private final BufferedReader reader;
    private int lastReadValue = -2; //State when lastReadValue equals -2 occurs only when nothing has been read yet.
    private String lastReadWord;
    private String currentPeek;
    private boolean longWordEnabled = false;
    private char longWordStart;
    private char longWordEnd;

    private boolean lastWordProcessed = false;

    public WordReader(InputStream inputStream) {
        reader = new BufferedReader(new InputStreamReader(inputStream));
    }
    public WordReader(Reader reader) {
        this.reader = new BufferedReader(reader);
    }
    public WordReader(InputStream inputStream, char longWordStart, char longWordEnd) {
        this(inputStream);
        enableLongWords(longWordStart, longWordEnd);
    }
    public WordReader(Reader reader, char longWordStart, char longWordEnd) {
        this(reader);
        enableLongWords(longWordStart, longWordEnd);
    }
    private void enableLongWords(char longWordStart, char longWordEnd) {
        this.longWordStart = longWordStart;
        this.longWordEnd = longWordEnd;
        longWordEnabled = true;
    }
    public boolean isEndReached() {
        return lastReadValue == -1;
    }
    private boolean readWhile(Predicate<Integer> continuePredicate, boolean saveReadChars) throws IOException {
        if (isEndReached() || (lastReadValue >= 0 && !continuePredicate.test(lastReadValue))) return false;
        boolean charRead = false;
        StringBuilder sb = saveReadChars? new StringBuilder() : null;
        if (saveReadChars && lastReadValue >= 0) {
            sb.append((char)lastReadValue);
            charRead = true;
        }
        while ((lastReadValue = reader.read()) != -1 && continuePredicate.test(lastReadValue)) {
            if (!charRead) charRead = true;
            if (saveReadChars) sb.append((char)lastReadValue);
        }
        if (lastReadValue == -1) {
            System.out.println();
        }
        if (charRead && currentPeek != null) currentPeek = null;
        if (saveReadChars) lastReadWord = sb.toString();
        return charRead;
    }
    public void skipWhiteSpaces() throws IOException {
        readWhile(Character::isWhitespace, false);
    }

    public String readWord() throws IOException {
        try {
            if (!lastWordProcessed && lastReadWord != null) {
                return lastReadWord;
            }
            if (!readWhile(c -> !Character.isWhitespace(c), true)) return null;
            if (longWordEnabled && lastReadWord.length() > 0 && lastReadWord.charAt(0) == longWordStart) {
                StringBuilder wordBuilder = new StringBuilder();
                if (lastReadWord.length() > 1) wordBuilder.append(lastReadWord.substring(1));
                if (readWhile(new EndOfLongWordDetector(), true)) {
                    wordBuilder.append(lastReadWord).deleteCharAt(wordBuilder.length() - 1);
                }
                lastReadWord = wordBuilder.toString();
            }
            return lastReadWord;
        } finally {
            lastWordProcessed = true;
        }
    }

    public String[] readNextWords(int count) throws IOException {
        String[] words = new String[count];
        for (int i = 0; i < count; i++) {
            skipWhiteSpaces();
            words[i] = readWord();
            if (words[i] == null) return null;
        }
        return words;
    }

    public String readNextWord() throws IOException {
        String[] words = readNextWords(1);
        return words == null || words.length == 0? null : words[0];
    }

    public String peek(int chars) throws IOException {
        if (currentPeek != null && currentPeek.length() == chars) return currentPeek;
        reader.mark(chars);
        StringBuilder peekBuilder = new StringBuilder();
        int readValue;
        while ((readValue = reader.read()) != -1 && peekBuilder.length() < chars) {
            peekBuilder.append((char)readValue);
        }
        reader.reset();
        currentPeek = peekBuilder.toString();
        return currentPeek;
    }

    public void skipPeek() throws IOException {
        reader.skip(currentPeek.length());
        currentPeek = null;
    }

    public String getLastReadWord() {
        return lastReadWord;
    }

    public boolean setLastWordProcessed(boolean lastWordProcessed) {
        return (this.lastWordProcessed = lastWordProcessed);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private class EndOfLongWordDetector implements Predicate<Integer> {
        private boolean prevCharWasEndOfLongWord = false;
        @Override
        public boolean test(Integer readVal) {
            if (prevCharWasEndOfLongWord) return false;
            char readChar = (char)readVal.intValue();
            if (readChar == longWordEnd) prevCharWasEndOfLongWord = true;
            return true;
        }
    }
}
