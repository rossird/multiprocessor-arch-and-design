import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortingInputGenerator {

   public static void main(String[] args) {
      
      if (args.length < 1) {
         System.err.println("Requires at least one parameter");
         return;
      }
      
      int len;
      try {
         len = Integer.parseInt(args[0]);
      }
      catch (NumberFormatException e) {
         System.err.println("Could not parse first input argument");
         return;
      }
      
      // Generate list of desired input length
      List<Integer> list = new ArrayList<Integer>();
      for(int i = 0 ; i < len; i++) { 
         list.add(i);
      }
      
      // Randomize the list
      Collections.shuffle(list);
      
      // Write it out to a file
      String filename = "inputs/rand_" + Integer.toString(len) + ".txt";
      try {
         FileWriter writer = new FileWriter(filename);
         
         // Write all bust last value + ','
         for(int i = 0; i < list.size() - 1; i++) {
            writer.write(list.get(i).toString());
            writer.write(',');
         }
         
         // Write last value
         writer.write(list.get(list.size()-1).toString());
         
         // Make sure to close
         writer.close();
         
      } catch (IOException e) {
         // TODO Auto-generated catch block
         System.out.println("Problem writing the text file");
         e.printStackTrace();
      }
   }
}
