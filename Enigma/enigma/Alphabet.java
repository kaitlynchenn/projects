package enigma;

/**
 * An alphabet of encodable characters.  Provides a mapping from characters
 * to and from indices into the alphabet.
 *
 * @author Kaitlyn Chen
 */
class Alphabet {



    /**
     * A new alphabet containing CHARS. The K-th character has index
     * K (numbering from 0). No character may be duplicated.
     * @param c alphabet
     */
    Alphabet(String c) {
        chars = c;
    }

    /**
     * A default alphabet of all upper-case characters.
     */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /**
     * Returns the size of the alphabet.
     */
    int size() {
        return chars.length();
    }

    /**
     * Returns true if CH is in this alphabet.
     */
    boolean contains(char ch) {
        return chars.indexOf(ch) != -1;
    }

    /**
     * Returns character number INDEX in the alphabet, where
     * 0 <= INDEX < size().
     */
    char toChar(int index) {
        return chars.charAt(index);

    }

    /**
     * Returns the index of character CH which must be in
     * the alphabet. This is the inverse of toChar().
     */
    int toInt(char ch) {
        return chars.indexOf(ch);
    }

    /**
     * string of chars.
     */
    private String chars;

    String getChars() {
        return chars;
    }
}
