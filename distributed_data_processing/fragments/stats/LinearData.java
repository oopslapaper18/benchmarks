package original.stats;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class LinearData {
    public static void main(String[] args) throws IOException {
        String fileName = IOUtil.getFileName();
        Scanner scan =
          new Scanner(new BufferedReader(new FileReader((String)
                                                          ("files/" +
                                                           fileName))));
        PrintWriter fitFout = new PrintWriter("files/linear.txt");
        int length = IOUtil.skipToInt((Scanner) scan);
        double xError = IOUtil.skipToDouble((Scanner) scan);
        double yError = IOUtil.skipToDouble((Scanner) scan);
        double[][] data = PlotReader.data2Column((Scanner) scan, length);
        PlotUtil p = new PlotUtil((double[][]) data);
        PrintWriter residualsWriter = new PrintWriter("files/residuals.txt");
        handleData((PrintWriter) fitFout, (PrintWriter) residualsWriter,
                   ((PlotUtil) p).x(), ((PlotUtil) p).y(), xError, yError);
    }
    
    public static void handleData(PrintWriter fitFout,
                                  PrintWriter residualsWriter, double[] x,
                                  double[] y, double xError, double yError) {
        double xMean = StatsUtil.mean((double[]) x);
        double yMean = StatsUtil.mean((double[]) y);
        double xVar = StatsUtil.variance((double[]) x, xMean);
        double yVar = StatsUtil.variance((double[]) y, yMean);
        double gradient = StatsUtil.gradient(StatsUtil.covariance(xMean, yMean,
                                                                  (double[]) x,
                                                                  (double[]) y),
                                             xVar);
        double offset = StatsUtil.yIntercept(xMean, yMean, gradient);
        double[] fit = StatsUtil.fit((double[]) x, gradient, offset);
        PlotWriter.errors((double[]) x,
                          StatsUtil.residuals((double[]) y, (double[]) fit),
                          xError, Calculate.multiply((double[]) y, yError),
                          (PrintWriter) residualsWriter);
        ((PrintWriter) residualsWriter).close();
        System.out.printf("\nLength of data = %2.0f  ",
                          (Object[])
                            (new Object[] { Float.valueOf((float)
                                                            ((double[])
                                                               x).length) }));
        double standardError = StatsUtil.standardError((double[]) y,
                                                       (double[]) fit);
        System.out.printf(
                     "\nGradient= %2.4f with error +/-  %2.4f ",
                     (Object[])
                       (new Object[] { Double.valueOf(gradient),
                        Double.valueOf(
                                 StatsUtil.errorGradient(xVar,
                                                         standardError,
                                                         ((double[])
                                                            x).length)) }));
        System.out.printf(
                     "\nResidual sum squares  = %2.2f ",
                     (Object[])
                       (new Object[] { Double.valueOf(
                                                Math.sqrt(standardError /
                                                              (((double[])
                                                                  x).length -
                                                                 1))) }));
        System.out.printf(
                     "\nOffset = %g with error  +/-  %g ",
                     (Object[])
                       (new Object[] { Double.valueOf(offset),
                        Double.valueOf(
                                 StatsUtil.errorOffset(((double[]) x).length,
                                                       xVar,
                                                       xMean,
                                                       standardError)) }));
        System.out.
          printf(
            "\nLinear Correlation Coefficient %g",
            (Object[])
              (new Object[] { Double.
                 valueOf(
                   StatsUtil.linearCorrelationCoefficient(
                               StatsUtil.regressionSumOfSquares((double[]) fit,
                                                                yMean),
                               yVar)) }));
        PlotWriter.errorsFit((double[]) x, (double[]) y, (double[]) fit, xError,
                             Calculate.multiply((double[]) y, yError),
                             (PrintWriter) fitFout);
        ((PrintWriter) fitFout).close();
        System.exit(0);
    }
    
    public LinearData() { super(); }
}
