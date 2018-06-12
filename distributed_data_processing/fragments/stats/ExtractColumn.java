package original.stats;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class ExtractColumn {
    public static void main(String[] args) throws IOException {
        System.out.printf(
                     "Which file would you like to pull a column out of? \n",
                     (Object[]) (new Object[] {  }));
        String inFileName = IOUtil.getFileName();
        if (((String) inFileName).equals("!")) {
            System.out.println("No file selected.");
        }
        else {
            Scanner scan =
              new Scanner(new BufferedReader(new FileReader((String)
                                                              ("files/" +
                                                               inFileName))));
            PrintWriter outFile = new PrintWriter("column.txt");
            int length = IOUtil.skipToInt((Scanner) scan);
            double xError = IOUtil.skipToDouble((Scanner) scan);
            double yError = IOUtil.skipToDouble((Scanner) scan);
            double[][] data = PlotReader.data2Column((Scanner) scan, length);
            PlotUtil p = new PlotUtil((double[][]) data);
            PlotWriter.aColumn(((PlotUtil) p).y(), (PrintWriter) outFile);
            ((PrintWriter) outFile).close();
        }
    }
    
    public ExtractColumn() { super(); }
}
