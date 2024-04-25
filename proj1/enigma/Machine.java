package enigma;

import java.util.ArrayList;

import java.util.Collection;


import static enigma.EnigmaException.*;

/**
 * Class that represents a complete enigma machine.
 *
 * @author Kaitlyn Chen
 */
class Machine {

    /**
     * A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     * and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     * available rotors.
     */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = new ArrayList<Rotor>();
        for (Rotor i : allRotors) {
            _allRotors.add(i);
        }
        _usedRotors = new ArrayList<Rotor>();
    }

    /**
     * Return the number of rotor slots I have.
     */
    int numRotors() {
        return _numRotors;
    }

    /**
     * Return the number pawls (and thus rotating rotors) I have.
     */
    int numPawls() {
        return _pawls;
    }

    /**
     * Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     * #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     * undefined results.
     */
    Rotor getRotor(int k) {
        if (_usedRotors.size() == 0) {
            throw new EnigmaException("_usedRotors is size 0");
        }
        return _usedRotors.get(k);
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /**
     * Set my rotor slots to the rotors named ROTORS from my set of
     * available rotors (ROTORS[0] names the reflector).
     * Initially, all rotors are set at their 0 setting.
     */
    void insertRotors(String[] rotors) {
        _usedRotors.clear();
        for (String i : rotors) {
            for (Rotor j : _allRotors) {
                if (j.name().equals(i)) {
                    _usedRotors.add(j);
                }
            }
        }
        if (rotors.length != _usedRotors.size()) {
            throw new EnigmaException("parameter len != _usedRotors len");
        }
    }

    /**
     * Set my rotors according to SETTING, which must be a string of
     * numRotors()-1 characters in my alphabet. The first letter refers
     * to the leftmost rotor setting (not counting the reflector).
     */
    void setRotors(String setting) {
        if (setting.length() != numRotors() - 1) {
            throw new EnigmaException("setting length != numRotors()-1");
        }
        char[] settingsArr = setting.toCharArray();
        for (int i = 1; i < setting.length() + 1; i++) {
            if (!_alphabet.contains(settingsArr[i - 1])) {
                throw new EnigmaException("setting not in cycle");
            }
            getRotor(i).set(settingsArr[i - 1]);
        }


    }

    /**
     * Return the current plugboard's permutation.
     */
    Permutation plugboard() {
        return _plugboard;
    }

    /**
     * Set the plugboard to PLUGBOARD.
     */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /**
     * Returns the result of converting the input character C (as an
     * index in the range 0..alphabet size - 1), after first advancing
     * the machine.
     */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /**
     * Advance all rotors to their next position.
     */
    private void advanceRotors() {
        ArrayList<Rotor> shouldAdvance = new ArrayList<Rotor>();
        int N = _usedRotors.size();
        for (int i = N - numPawls(); i < N - 1; i++) {
            if (i < 0) {
                throw new EnigmaException("unexpected -- i < 0");
            }
            if (getRotor(i + 1).atNotch()) {
                if (!shouldAdvance.contains(getRotor(i))) {
                    shouldAdvance.add(getRotor(i));
                }
                if (!shouldAdvance.contains(getRotor(i + 1))) {
                    shouldAdvance.add(getRotor(i + 1));
                }
            }
        }
        if (!shouldAdvance.contains(getRotor(N - 1))) {
            shouldAdvance.add(getRotor(N - 1));
        }
        for (Rotor r : shouldAdvance) {
            r.advance();
        }
    }

    /**
     * Return the result of applying the rotors to the character C (as an
     * index in the range 0..alphabet size - 1).
     */
    private int applyRotors(int c) {
        int N = _usedRotors.size();
        for (int i = N - 1; i > 0; i -= 1) {
            c = getRotor(i).convertForward(c);
        }
        c = getRotor(0).permutation().permute(c);
        for (int j = 1; j < N; j++) {
            c = getRotor(j).convertBackward(c);
        }
        return c;
    }

    /**
     * Returns the encoding/decoding of MSG, updating the state of
     * the rotors accordingly.
     */
    String convert(String msg) {
        char[] msgArray = msg.toCharArray();
        String result = "";
        for (char i : msgArray) {
            result += _alphabet.toChar(convert(_alphabet.toInt(i)));
        }
        return result;
    }

    /**
     * Common alphabet of my rotors.
     */
    private final Alphabet _alphabet;

    /**
     * number of rotors.
     */
    private int _numRotors;

    /**
     * number of pawls.
     */
    private int _pawls;

    /**
     * plugboard.
     */
    private Permutation _plugboard;

    /**
     * arraylist of all rotors.
     */
    private ArrayList<Rotor> _allRotors;

    /**
     * array list of used rotors.
     */
    private ArrayList<Rotor> _usedRotors;

}
