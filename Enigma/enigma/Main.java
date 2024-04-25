package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.error;


/**
 * Enigma simulator.
 *
 * @author Kaitlyn Chen
 */
public final class Main {

    /**
     * Process a sequence of encryptions and decryptions, as
     * specified by ARGS, where 1 <= ARGS.length <= 3.
     * ARGS[0] is the name of a configuration file.
     * ARGS[1] is optional; when present, it names an input file
     * containing messages.  Otherwise, input comes from the standard
     * input.  ARGS[2] is optional; when present, it names an output
     * file for processed messages.  Otherwise, output goes to the
     * standard output. Exits normally if there are no errors in the input;
     * otherwise with code 1.
     */
    public static void main(String... args) {
        try {
            CommandArgs options =
                    new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                        + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /**
     * Open the necessary files for non-option arguments ARGS (see comment
     * on main).
     */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /**
     * Return a Scanner reading from the file named NAME.
     */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Return a PrintStream writing to the file named NAME.
     */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Configure an Enigma machine from the contents of configuration
     * file _config and apply it to the messages in _input, sending the
     * results to _output.
     */
    private void process() {
        Machine newMachine = readConfig();
        String machineSettings = _input.nextLine();
        if (!machineSettings.contains("*")) {
            throw new EnigmaException("* not found");
        }
        setUp(newMachine, machineSettings);
        while (_input.hasNextLine()) {
            String currLine = _input.nextLine();
            while (!currLine.contains("*")) {
                for (char v : currLine.replaceAll(" ", "").toCharArray()) {
                    if (!_alphabet.getChars().contains(Character.toString(v))) {
                        throw new EnigmaException("char not in alphabet");
                    }
                }
                String result;
                result = newMachine.convert(currLine.replaceAll(" ", ""));
                result = result.replaceAll(" ", "");
                printMessageLine(result);
                if (!_input.hasNextLine()) {
                    break;
                }
                currLine = _input.nextLine();
            }
            if (currLine.contains("*")) {
                setUp(newMachine, currLine);
            }
        }
    }

    /**
     * Return an Enigma machine configured from the contents of configuration
     * file _config.
     */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.next());
            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();
            _config.nextLine();
            readRotor();
            return new Machine(_alphabet, numRotors, numPawls, _allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /**
     * Return a rotor, reading its description from _config.
     */
    private Rotor readRotor() {
        _allRotors = new ArrayList<>();
        boolean called = false;
        String rotor = "";
        try {
            while (_config.hasNext() || called) {
                if (!called) {
                    rotor = _config.nextLine().trim();
                }
                called = false;
                Scanner rotorScanner = new Scanner(rotor);
                String name = rotorScanner.next();
                String typeAndNotches = rotorScanner.next();
                String cycles = rotor.substring(rotor.indexOf("("));
                Rotor addedRotor = null;
                if (typeAndNotches.charAt(0) == 'M') {
                    String shit;
                    if (_config.hasNextLine()) {
                        shit = _config.nextLine().trim();
                    } else {
                        shit = "";
                    }
                    if (shit.length() != 0) {
                        if (shit.trim().charAt(0) == '(') {
                            cycles += shit;
                        } else {
                            called = true;
                            rotor = shit;
                        }
                    }
                    Permutation perm = new Permutation(cycles, _alphabet);
                    String notches = typeAndNotches.substring(1);
                    addedRotor = new MovingRotor(name, perm, notches);
                } else if (typeAndNotches.charAt(0) == 'N') {
                    if (typeAndNotches.length() > 1) {
                        throw new EnigmaException("should not have notches");
                    }
                    Permutation perm = new Permutation(cycles, _alphabet);
                    addedRotor = new FixedRotor(name, perm);
                } else if (typeAndNotches.charAt(0) == 'R') {
                    if (typeAndNotches.length() > 1) {
                        throw new EnigmaException("should not have notches");
                    }
                    String shit = _config.nextLine().trim();
                    if (shit.trim().charAt(0) == '(') {
                        cycles += shit;
                    } else {
                        called = true;
                        rotor = shit;
                    }
                    Permutation perm = new Permutation(cycles, _alphabet);
                    addedRotor = new Reflector(name, perm);
                }
                _allRotors.add(addedRotor);
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
        return null;
    }

    /**
     * Set M according to the specification given on SETTINGS,
     * which must have the format specified in the assignment.
     */
    private void setUp(Machine M, String settings) {
        Scanner scanner = new Scanner(settings);
        if (settings.split(" ").length - 1 < M.numRotors()) {
            throw new EnigmaException("Not enough arguments in setting");
        }
        scanner.next();
        String[] R = new String[M.numRotors()];
        for (int i = 0; i < M.numRotors(); i++) {
            R[i] = scanner.next();
        }
        if (!scanner.hasNext()) {
            throw new EnigmaException("NoSuchElement -- scanner.next()");
        }
        String rotorSettings = scanner.next();
        if (scanner.hasNext()) {
            String permcycles = settings.substring(settings.indexOf('('));
            Permutation perm = new Permutation(permcycles, _alphabet);
            M.setPlugboard(perm);
        } else {
            Permutation perm = new Permutation("", _alphabet);
            M.setPlugboard(perm);
        }
        M.insertRotors(R);
        M.setRotors(rotorSettings);
    }

    /**
     * Return true iff verbose option specified.
     */
    static boolean verbose() {
        return _verbose;
    }

    /**
     * Print MSG in groups of five (except that the last group may
     * have fewer letters).
     */
    private void printMessageLine(String msg) {
        int cnt = 0;
        for (char i : msg.toCharArray()) {
            cnt++;
            _output.print(i);
            if (cnt % 5 == 0) {
                _output.print(" ");
            }
        }
        _output.print("\n");
    }

    /**
     * Alphabet used in this machine.
     */
    private Alphabet _alphabet;

    /**
     * Source of input messages.
     */
    private Scanner _input;

    /**
     * Source of machine configuration.
     */
    private Scanner _config;

    /**
     * File for encoded/decoded messages.
     */
    private PrintStream _output;

    /**
     * ArrayList of all possible rotors.
     */
    private ArrayList<Rotor> _allRotors;

    /**
     * True if --verbose specified.
     */
    private static boolean _verbose;
}
