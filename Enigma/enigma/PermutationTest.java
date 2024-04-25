package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/**
 * The suite of all JUnit tests for the Permutation class. For the purposes of
 * this lab (in order to test) this is an abstract class, but in proj1, it will
 * be a concrete class. If you want to copy your tests for proj1, you can make
 * this class concrete by removing the 4 abstract keywords and implementing the
 * 3 abstract methods.
 *
 *  @author
 */
public class PermutationTest {

    /**
     * For this lab, you must use this to get a new Permutation,
     * the equivalent to:
     * new Permutation(cycles, alphabet)
     * @return a Permutation with cycles as its cycles and alphabet as
     * its alphabet
     * @see Permutation for description of the Permutation conctructor
     */
    Permutation getNewPermutation(String cycles, Alphabet alphabet) {
        return new Permutation(cycles, alphabet);
    }

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet(chars)
     * @return an Alphabet with chars as its characters
     * @see Alphabet for description of the Alphabet constructor
     */
    Alphabet getNewAlphabet(String chars) {
        return new Alphabet(chars);
    };

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet()
     * @return a default Alphabet with characters ABCD...Z
     * @see Alphabet for description of the Alphabet constructor
     */
    Alphabet getNewAlphabet() {
        return new Alphabet();
    };

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /** Check that PERM has an ALPHABET whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha,
                           Permutation perm, Alphabet alpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                    e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                    c, perm.invert(e));
            int ci = alpha.toInt(c), ei = alpha.toInt(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                    ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                    ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        Alphabet alpha = getNewAlphabet();
        Permutation perm = getNewPermutation("", alpha);
        checkPerm("identity", UPPER_STRING, UPPER_STRING, perm, alpha);
    }

    @Test
    public void testSize() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals(4, p.size());
        Permutation p2 = getNewPermutation("(BACD) (FEG)",
                getNewAlphabet("ABCDEFG"));
        assertEquals(7, p2.size());
        Permutation p3 = getNewPermutation("(BaC$) (fE#)",
                getNewAlphabet("aBC$Ef#"));
        assertEquals(7, p3.size());
        Permutation p4 = getNewPermutation("(BACD)", getNewAlphabet("ABCDK"));
        assertEquals(5, p4.size());
        Permutation p5 = getNewPermutation("(BACD) (FEG)",
                getNewAlphabet("ABCDEFGK"));
        assertEquals(8, p5.size());
    }

    @Test
    public void testPermuteInt() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals(2, p.permute(0));
        assertEquals(0, p.permute(1));
        assertEquals(3, p.permute(2));
        assertEquals(1, p.permute(3));
        assertEquals(3, p.permute(-2));
        assertEquals(3, p.permute(6));
        Permutation p2 = getNewPermutation("(BACD)(FEG)",
                getNewAlphabet("ABCDEFG"));
        assertEquals(2, p2.permute(0));
        assertEquals(0, p2.permute(1));
        assertEquals(3, p2.permute(2));
        assertEquals(1, p2.permute(3));
        assertEquals(6, p2.permute(4));
        assertEquals(4, p2.permute(5));
        assertEquals(5, p2.permute(6));
        assertEquals(6, p2.permute(-3));
        assertEquals(5, p2.permute(20));
        Permutation p6 = getNewPermutation("(BACD)  (FEG)",
                getNewAlphabet("ABCDEFG"));
        assertEquals(2, p6.permute(0));
        assertEquals(0, p6.permute(1));
        assertEquals(5, p6.permute(6));
        assertEquals(6, p6.permute(-3));
        assertEquals(5, p6.permute(20));
        Permutation p3 = getNewPermutation("(BaC$) (fE#)",
                getNewAlphabet("aBC$Ef#"));
        assertEquals(2, p3.permute(0));
        assertEquals(0, p3.permute(1));
        assertEquals(3, p3.permute(2));
        assertEquals(1, p3.permute(3));
        assertEquals(6, p3.permute(4));
        assertEquals(4, p3.permute(5));
        assertEquals(5, p3.permute(6));
        assertEquals(6, p2.permute(-3));
        assertEquals(5, p2.permute(20));
        Permutation p4 = getNewPermutation("(BACD)", getNewAlphabet("ABCDK"));
        assertEquals(4, p4.permute(4));
        assertEquals(4, p4.permute(24));
        Permutation p5 = getNewPermutation("(BACD) (FEG)",
                getNewAlphabet("ABCDEFGK"));
        assertEquals(7, p5.permute(7));
        assertEquals(7, p5.permute(23));
    }

    @Test
    public void testInvertInt() {
        Permutation p = getNewPermutation("   (BACD)", getNewAlphabet("ABCD"));
        assertEquals(0, p.invert(2));
        assertEquals(1, p.invert(0));
        assertEquals(2, p.invert(3));
        assertEquals(3, p.invert(1));
        assertEquals(0, p.invert(-2));
        assertEquals(1, p.invert(40));
        Permutation p2 = getNewPermutation("(BA CD) (FEG)",
                getNewAlphabet("ABCDEFG"));
        assertEquals(0, p2.invert(2));
        assertEquals(1, p2.invert(0));
        assertEquals(2, p2.invert(3));
        assertEquals(3, p2.invert(1));
        assertEquals(4, p2.invert(6));
        assertEquals(5, p2.invert(4));
        assertEquals(6, p2.invert(5));
        assertEquals(5, p2.invert(-3));
        assertEquals(0, p2.invert(23));
        Permutation p3 = getNewPermutation("(BaC$) (fE#)",
                getNewAlphabet("aBC$Ef#"));
        assertEquals(0, p3.invert(2));
        assertEquals(1, p3.invert(0));
        assertEquals(2, p3.invert(3));
        assertEquals(3, p3.invert(1));
        assertEquals(4, p3.invert(6));
        assertEquals(5, p3.invert(4));
        assertEquals(6, p3.invert(5));
        assertEquals(5, p2.invert(-3));
        assertEquals(0, p2.invert(23));
        Permutation p4 = getNewPermutation("(BACD)", getNewAlphabet("ABCDK"));
        assertEquals(4, p4.invert(4));
        assertEquals(4, p4.invert(24));
        Permutation p5 = getNewPermutation("(BACD) (FEG)",
                getNewAlphabet("ABCDEFGK"));
        assertEquals(7, p5.invert(7));
        assertEquals(7, p5.invert(23));
    }

    @Test
    public void testPermuteChar() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals('C', p.permute('A'));
        assertEquals('A', p.permute('B'));
        assertEquals('D', p.permute('C'));
        assertEquals('B', p.permute('D'));
        Permutation p2 = getNewPermutation("(BACD) (FEG)",
                getNewAlphabet("ABCDEFG"));
        assertEquals('C', p2.permute('A'));
        assertEquals('A', p2.permute('B'));
        assertEquals('D', p2.permute('C'));
        assertEquals('B', p2.permute('D'));
        assertEquals('G', p2.permute('E'));
        assertEquals('E', p2.permute('F'));
        assertEquals('F', p2.permute('G'));
        Permutation p3 = getNewPermutation("(BaC$) (fE#)",
                getNewAlphabet("aBC$Ef#"));
        assertEquals('C', p3.permute('a'));
        assertEquals('a', p3.permute('B'));
        assertEquals('$', p3.permute('C'));
        assertEquals('B', p3.permute('$'));
        assertEquals('#', p3.permute('E'));
        assertEquals('E', p3.permute('f'));
        assertEquals('f', p3.permute('#'));
        Permutation p4 = getNewPermutation("(BACD)", getNewAlphabet("ABCDK"));
        assertEquals('K', p4.permute('K'));
        Permutation p5 = getNewPermutation("(BACD) (FEG)",
                getNewAlphabet("ABCDEFGK"));
        assertEquals('K', p5.permute('K'));
    }

    @Test
    public void testInvertChar() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals('A', p.invert('C'));
        assertEquals('B', p.invert('A'));
        assertEquals('C', p.invert('D'));
        assertEquals('D', p.invert('B'));
        Permutation p2 = getNewPermutation("(BACD) (FEG)",
                getNewAlphabet("ABCDEFG"));
        assertEquals('A', p2.invert('C'));
        assertEquals('B', p2.invert('A'));
        assertEquals('C', p2.invert('D'));
        assertEquals('D', p2.invert('B'));
        assertEquals('E', p2.invert('G'));
        assertEquals('F', p2.invert('E'));
        assertEquals('G', p2.invert('F'));
        Permutation p3 = getNewPermutation("(BaC$) (fE#)",
                getNewAlphabet("aBC$Ef#"));
        assertEquals('a', p3.invert('C'));
        assertEquals('B', p3.invert('a'));
        assertEquals('C', p3.invert('$'));
        assertEquals('$', p3.invert('B'));
        assertEquals('E', p3.invert('#'));
        assertEquals('f', p3.invert('E'));
        assertEquals('#', p3.invert('f'));
        Permutation p4 = getNewPermutation("(BACD)", getNewAlphabet("ABCDK"));
        assertEquals('K', p4.invert('K'));
        Permutation p5 = getNewPermutation("(BACD) (FEG)",
                getNewAlphabet("ABCDEFGK"));
        assertEquals('K', p5.invert('K'));
    }

    @Test
    public void testDerangement() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertTrue(p.derangement());
        Permutation p2 = getNewPermutation("(BACD) (FEG)",
                getNewAlphabet("ABCDEFG"));
        assertTrue(p2.derangement());
        Permutation p3 = getNewPermutation("(BaC$) (fE#)",
                getNewAlphabet("aBC$Ef#"));
        assertTrue(p3.derangement());
        Permutation p4 = getNewPermutation("(BACD)", getNewAlphabet("ABCDK"));
        assertFalse(p4.derangement());
        Permutation p5 = getNewPermutation("(BACD) (FEG)",
                getNewAlphabet("ABCDEFGK"));
        assertFalse(p5.derangement());
        Permutation p6 = getNewPermutation("(BACD) (FEG)",
                getNewAlphabet("ABCDEFGKX"));
        assertFalse(p6.derangement());
    }

    @Test(expected = EnigmaException.class)
    public void testNotInAlphabet() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        p.invert('F');
    }

}
