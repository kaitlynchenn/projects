package enigma;

import java.util.ArrayList;

import static enigma.EnigmaException.*;

/**
 * Represents a permutation of a range of integers starting at 0 corresponding
 * to the characters of an alphabet.
 *
 * @author Kaitlyn Chen
 */
class Permutation {




    /**
     * Set this Permutation to that specified by CYCLES, a string in the
     * form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     * is interpreted as a permutation in cycle notation.  Characters in the
     * alphabet that are not included in any cycle map to themselves.
     * Whitespace is ignored.
     */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        String[] b = cycles.split("\\)");
        for (String i : b) {
            String j = i.replaceAll("\\(", "");
            String l = j.replaceAll(" ", "");
            String k = l.trim();
            for (char v : k.toCharArray()) {
                if (!_alphabet.getChars().contains(Character.toString(v))) {
                    throw new EnigmaException("char not in alphabet");
                }
                permutation.add(k);
            }
        }
    }

    /**
     * Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     * c0c1...cm.
     */
    private void addCycle(String cycle) {
        permutation.add(cycle);
    }

    /**
     * Return the value of P modulo the size of this permutation.
     */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /**
     * Returns the size of the alphabet I permute.
     */
    int size() {
        return _alphabet.size();
    }

    /**
     * Return the result of applying this permutation to P modulo the
     * alphabet size.
     */
    int permute(int p) {
        char inputChar = _alphabet.toChar(wrap(p));
        for (String cycle : permutation) {
            if (cycle.indexOf(inputChar) != -1) {
                int outputIndex = (cycle.indexOf(inputChar) + 1)
                        % cycle.length();
                char outputChar = cycle.charAt(outputIndex);
                return _alphabet.toInt(outputChar);
            }
        }
        return wrap(p);
    }

    /**
     * Return the result of applying the inverse of this permutation
     * to  C modulo the alphabet size.
     */
    int invert(int c) {
        char inputChar = _alphabet.toChar(wrap(c));
        for (String cycle : permutation) {
            if (cycle.indexOf(inputChar) != -1) {
                int outputIndex = (cycle.indexOf(inputChar) - 1);
                if (outputIndex < 0) {
                    outputIndex += cycle.length();
                }
                char outputChar = cycle.charAt(outputIndex);
                return _alphabet.toInt(outputChar);
            }
        }
        return wrap(c);
    }

    /**
     * Return the result of applying this permutation to the index of P
     * in ALPHABET, and converting the result to a character of ALPHABET.
     */
    char permute(char p) {
        for (String cycle : permutation) {
            if (cycle.indexOf(p) != -1) {
                int outputIndex = (cycle.indexOf(p) + 1) % cycle.length();
                return cycle.charAt(outputIndex);
            }
        }
        return p;
    }

    /**
     * Return the result of applying the inverse of this permutation to C.
     */
    char invert(char c) {
        for (String cycle : permutation) {
            if (cycle.indexOf(c) != -1) {
                int outputIndex = (cycle.indexOf(c) - 1);
                if (outputIndex < 0) {
                    outputIndex += cycle.length();
                }
                return cycle.charAt(outputIndex);
            }
        }
        return c;
    }

    /**
     * Return the alphabet used to initialize this Permutation.
     */
    Alphabet alphabet() {
        return _alphabet;
    }

    /**
     * Return true iff this permutation is a derangement (i.e., a
     * permutation for which no value maps to itself).
     */
    boolean derangement() {
        for (char i : _alphabet.getChars().toCharArray()) {
            if (permute(i) == i) {
                return false;
            }
        }
        return true;
    }

    /**
     * Alphabet of this permutation.
     */
    private Alphabet _alphabet;

    /**
     * arraylist of permutation, each cycle is a string.
     */
    private ArrayList<String> permutation = new ArrayList<String>();

}
