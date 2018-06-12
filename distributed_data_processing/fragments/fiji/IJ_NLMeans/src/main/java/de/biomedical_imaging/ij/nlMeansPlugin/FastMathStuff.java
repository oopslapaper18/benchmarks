package IJ_NLMeans.src.main.java.de.biomedical_imaging.ij.nlMeansPlugin;

public class FastMathStuff {
    private static final int BIG_ENOUGH_INT = 16 * 1024;
    private static final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5;
    
    public static double max(final double a, final double b) {
        if (a > b) { return a; }
        if (a < b) { return b; }
        if (a != b) { return Double.NaN; }
        long bits = Double.doubleToRawLongBits(a);
        if (bits == -9223372036854775808L) { return b; }
        return a;
    }
    
    public static int fastRound(double x) {
        return (int) (x + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
    }
    
    public FastMathStuff() { super(); }
}
