package original.stats;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class FormatData {
    public static void main(String[] args) throws IOException {
        String fileName = IOUtil.getFileName();
        Scanner scan =
          new Scanner(new BufferedReader(new FileReader((String)
                                                          ("files/" +
                                                           fileName))));
        PrintWriter fitFout = new PrintWriter((String)
                                                ("files/data_" + fileName));
        int length = IOUtil.skipToInt((Scanner) scan);
        double xError = IOUtil.skipToDouble((Scanner) scan);
        double yError = IOUtil.skipToDouble((Scanner) scan);
        double[][] data = PlotReader.data2Column((Scanner) scan, length);
        PlotUtil p = new PlotUtil((double[][]) data);
        PlotWriter.errors(((PlotUtil) p).x(), ((PlotUtil) p).y(), xError,
                          Calculate.multiply(((PlotUtil) p).y(), yError),
                          (PrintWriter) fitFout);
        ((PrintWriter) fitFout).close();
    }
    
    public FormatData() { super(); }
}
