//*****************************************************************************
// Author: Rick Rossi
// Date:   April 2021
//
// Description:
// Implementation for final project of EN605.616 Multiprocessor Architecture
// and Design. This class implements merge sort in a parallel fashion using
// Java's ForkJoinPool architecture, which allows for creation of tasks 
// recursively and enables work-stealing.
//
//*****************************************************************************
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class ParallelSort {
   
   public static void main(String[] args) {
      
      //***********************************************************************
      // Get and check input arguments
      if (args.length < 1) {
         System.out.println("Requires one argument.");
         return;
      }
      
      Path inputPath  = Paths.get(args[0]);
      if (!inputPath.toFile().exists()) {
         System.out.println("Input file does not exist.");
         return;
      }
      
      //***********************************************************************
      // Read input values
      System.out.println("Using: "+ inputPath.getFileName());
      int[] values = ParseFile(inputPath);
      int[] values2 = values.clone();
      
      MergeSorter         ssorter = new MergeSorter();
      ParallelMergeSorter psorter = new ParallelMergeSorter();
      
      long tStart = 0;
      long tEnd   = 0;
      
      //***********************************************************************
      // Do parallel sort
      tStart = System.nanoTime();
      psorter.sort(values);
      tEnd   = System.nanoTime();
      System.out.println("Parallel sort runtime: " + (tEnd - tStart) / 1000 + " us");
      
      if (!IsSorted(values)) {
         System.out.println("Parallel sort didn't work!");
      }
      
      //***********************************************************************
      // Do serial sort
      tStart = System.nanoTime();
      ssorter.sort(values2);
      tEnd   = System.nanoTime();
      System.out.println("Serial sort runtime: " + (tEnd - tStart) / 1000 + " us");
      
      if (!IsSorted(values2)) {
         System.out.println("Serial sort didn't work!");
      }
      
   }
   
   //**************************************************************************
   // Name: ParseFile
   //
   // Description:
   //    Parse the input file and return an array of Integers.
   //
   //    If there's a problem reading the file, null is returned.
   //
   //**************************************************************************
   public static int[] ParseFile(Path inputPath) {
      
      List<Integer> inputList = new ArrayList<Integer>();
      
      // Input is expected to be a comma-separated text file of integers
      try {
         Scanner scanner = new Scanner(inputPath.toFile());
         scanner.useDelimiter(",");
         
         while(scanner.hasNext()) {
            inputList.add(scanner.nextInt());
         }
         
         scanner.close();
         
      } catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
         System.out.println("Problem parsing the text file");
         e.printStackTrace();
         return null;
      }
      
      int[] inputValues = new int[inputList.size()];
      for(int idx = 0; idx < inputList.size(); idx++) {
         inputValues[idx] = inputList.get(idx);
      }
      
      return inputValues;
      
   }
   
   //**************************************************************************
   // Name: WriteOutputFile
   //
   // Description:
   //    Check if the given array of values is sorted
   //
   //    If file exists, overwrites the contents.
   //
   //**************************************************************************
   public static boolean IsSorted(int[] values) {
      for(int i = 0; i < values.length-1; i++ ) {
         if (values[i] > values[i+1]) {
            return false;
         }
      }
      return true;
   }

}

//*****************************************************************************
// Name: BubbleSorter
//
// Description:
//    Implementation for BubbleSorter.
//
//*****************************************************************************
class BubbleSort {
   
   // Generic bubbleSort
   public static void sort(int[] values, int start, int end) {
      int size = end - start + 1;
      for (int i = 0; i < size - 1; i++) {
         for (int j = start; j < end - i; j++) {
            if (values[j] > values[j+1]) {
               int tmp = values[j];
               values[j] = values[j+1];
               values[j+1] = tmp;
            }
         }
      }
   }
}

//*****************************************************************************
// Name: Merger
//
// Description:
//    Merger interface with default merge implementation so my parallel
//    action class can use it (I'm bad at java).
//
//*****************************************************************************
class Merger {
   
   static final int THRESHOLD = 8; // Threshold to determine when to use serial sort
   
   // Generic merge routine
   static void merge(int[] values, int start, int middle, int end, int[] helper)
   {

      // Create copy of input array so we can modify it for output
      for(int i = start; i <= end; i++) {
         helper[i] = values[i];
      }
      
      // Now we merge the two
      int leftPos  = start;   // Start at position start for the left side (in helper array)
      int rightPos = middle+1;  // Start at position middle for the right side (in helper array)
      int outPos   = start;   
      while(leftPos <= middle && rightPos <= end) {
         
         // Copy next value
         if (helper[leftPos] <= helper[rightPos]) {
            values[outPos] = helper[leftPos];
            leftPos++;
         }
         else {
            values[outPos] = helper[rightPos];
            rightPos++;
         }
         
         outPos++;
      }
      
      // Copy any remaining entries in either side
      while(leftPos <= middle) {
         values[outPos] = helper[leftPos];
         leftPos++;
         outPos++;
      }
      while(rightPos <= end) {
         values[outPos] = helper[rightPos];
         rightPos++;
         outPos++;
      }
   }
}

//*****************************************************************************
// Name: MergeSorter
//
// Description:
//    Classic merge sort implementation (non-stable)
//
//*****************************************************************************
class MergeSorter {
   
   public void sort(int[] values) { 
      sort(values, 0, values.length-1);
   }
   
   public void sort(int[] values, int start, int end) {
      // Need a helper array for doing the merging
      int[] helper = new int[values.length];
      mergeSort(values, start, end, helper);
   }
   
   private void mergeSort(int[] values, int start, int end, int[] helper) {
      int size = end - start + 1;
      
      // If size is below pre-determined threshold, sort using bubble sort
      if (size <= Merger.THRESHOLD) {
         BubbleSort.sort(values, start, end);
      }
      // Otherwise, recurse!
      else {
         int mid = (start+end) / 2;
         mergeSort(values, start, mid, helper);
         mergeSort(values, mid+1, end, helper);
         Merger.merge(values, start, mid, end, helper);
      }
   }


}

//*****************************************************************************
// Name: ParallelMergeSorter
//
// Description:
//    Sort the given array of integer in a parallel fashion.
//
//*****************************************************************************
class ParallelMergeSorter  { 

   public void sort(int[] values) { 
      sort(values, 0, values.length-1);
   }
   
   public void sort(int[] values, int start, int end) {

      // Need a helper array for doing the merging
      int size = end - start + 1;
      int[] helper = new int[size];
      ParallelMergeSortAction sorter = new ParallelMergeSortAction(values, start, end, helper);
      
      ForkJoinPool forkJoinPool = new ForkJoinPool(7);
      forkJoinPool.invoke(sorter);
   }

   // This class will actually do all the work. It uses the RecursiveAction class
   @SuppressWarnings("serial")
   class ParallelMergeSortAction extends RecursiveAction {
      
      int[]       Values;
      int         Start;
      int         End;
      int[]       Helper;
      
      public ParallelMergeSortAction(int[] values, int start, int end, int[] helper) {
         Values = values;
         Start  = start;
         End    = end;
         Helper = helper;
      }

      @Override
      protected void compute() {
         int size = End-Start + 1;
         
         // If size is below pre-determined threshold, sort using bubble sort
         if (size <= Merger.THRESHOLD) {
            BubbleSort.sort(Values, Start, End);
         }  else {
   
            // Split input array into two halves.
            int mid = (Start+End) / 2;
            ParallelMergeSortAction l = new ParallelMergeSortAction(Values, Start, mid, Helper);
            ParallelMergeSortAction r = new ParallelMergeSortAction(Values, mid+1, End, Helper); 
            
            // Sort each half recursively.
            // This uses the Fork/Join framework to allow for task-stealing.a
            ForkJoinTask.invokeAll(l,r);
            
            // Merge the results.
            Merger.merge(Values, Start, mid, End, Helper);
         }
      }
      
   }
   
}



