package original.stats;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class AverageData {
    public static void main(String[] args) throws IOException {
        System.out.
          printf(
            ("Please type the name of the data file you wish to average. " +
             "\n"), (Object[]) (new Object[] {  }));
        String choice = IOUtil.getFileName();
        if (!((String) choice).equals(null)) {
            Scanner scan =
              new Scanner(new BufferedReader(new FileReader((String)
                                                              ("files/" +
                                                               choice))));
            int length = IOUtil.skipToInt((Scanner) scan);
            double[] data = PlotReader.data1Column((Scanner) scan, length);
            double mean = StatsUtil.mean((double[]) data);
            System.out.printf("Mean value %g \n",
                              (Object[])
                                (new Object[] { Double.valueOf(mean) }));
            System.out.
              printf(
                "std Dev value %g \n",
                (Object[])
                  (new Object[] { Double.valueOf(
                                           Math.sqrt(
                                                  StatsUtil.variance(
                                                              (double[]) data,
                                                              mean))) }));
            System.exit(0);
        }
    }
    
    public AverageData() { super(); }
}
