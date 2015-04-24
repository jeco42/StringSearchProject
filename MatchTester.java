import java.util.HashMap;
import java.util.Random;
import java.nio.charset.*;
import java.io.IOException;
import java.nio.file.*;
import java.io.*;

class StringMatching{
   
   public StringMatching(){
   
   }
   
   //returns the number of character comparisons that were made.
   public int bruteForce(String text, String pattern){
      int n = text.length();
      int m = pattern.length();
      int c = 0;
   
      for(int i = 0; i <= (n - m); i++){
         int j = 0;

         while(j < m && pattern.charAt(j) == text.charAt(i + j)){
            j++;
            c++;
         }   
         if(j == m){
            return c;
         }
         c++;      
      }
      return c;
   }
   
   //returns the number of character comparisons that were made.
   public int horspool(String text, String pattern){
      HashMap<Character, Integer> shift = new HashMap<>();
      int n = text.length();
      int m = pattern.length();
      int c = 0;
      
      //compute the shift table
      for(int i = 0; i < (m - 1); i++)
         shift.put(pattern.charAt(i), m - 1 - i);
      
      int i = m - 1;
      while(i <= n - 1){
         int k = 0;
         while((k <= m-1) && (pattern.charAt(m-1-k) == text.charAt(i-k))){
            k++;
            c++;
         }
         if(k==m)
            return c;
         else{
            c++;  
            if(shift.get(text.charAt(i)) != null)
               i+=shift.get(text.charAt(i));
            else
               i+=m;   
         }        
      }
      return c;      
   }
   
   //returns the number of character comparisons that were made.
   public int boyermoore(String text, String pattern){
      int n = text.length();
      int m = pattern.length();
      int[] offset = new int[m];;
      HashMap<Character, Integer> shift = new HashMap<>();
      int c = 0;
      
      //compute the bad character table
      for(int i = 0; i < (m - 1); i++)
         shift.put(pattern.charAt(i), m - 1 - i);

      //computer the good-suffix table
      for(int i = m-1; i > 0; i--){
         boolean check = true;
         String current = pattern.substring(i);
         int index = pattern.substring(0,i).lastIndexOf(current);
         while(check && index != -1){
            if(index > 0){
               if(pattern.charAt(index-1) != pattern.charAt(i-1)){
                  check = false;  
               }
               else{
                  index = pattern.substring(0,index).lastIndexOf(current);
               }
            }
            else{
               check = false;
            }   
         }
         if(index >= 0){
               offset[m-i] = m - 1 - index;
         }
         else{
            boolean found = false;
            for(int j = 1; j < current.length(); j++){
               String suffix = current.substring(j);
               int match = 0;
               for(int k = 0; k < suffix.length(); k++){
                  if(pattern.charAt(k) == suffix.charAt(k))
                     match++;
               }
               if(match == suffix.length()){
                  found = true;
                  offset[m-i] = m - suffix.length();
                  break;
               }                  
            }
            if(!found)
               offset[m-i] = m;              
         }
      }
      
      //final part of algo
      for(int i = m - 1, j; i < n;){
         int k = 0;
         int r;
         for(j = m-1, r = i; pattern.charAt(j) == text.charAt(r); r--, j--){
            k++;
            c++;
            if(j==0)
               return c;
         }
         int s = m;
         if(shift.get(text.charAt(r)) != null){
            s = shift.get(text.charAt(r));
         }   
         int x = Math.max(s-k, 1);
         i += Math.max(offset[m - 1 - j], x);
         c++;
      }

      return c;      
   }   
}

public class MatchTester{
   //Can change these parameters for different experimental results
   static final int MAX_PATTERN_LENGTH = 1000;
   static final double PATTERN_GROWTH_RATE = 1.5;
   static final double TEXT_GROWTH_RATE = 1.3;

   public static void main(String[] args){      
      StringMatching sm = new StringMatching();
      Random rng = new Random();
      Writer output = null, output2 = null;
      
      try{
         output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("binaryOut.txt"), "utf-8"));
         output2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("naturalOut.txt"), "utf-8"));     
      } catch(Exception e){
         System.out.println("Failed to created output writer: " + e);
      }
           
      int answer, answer2, answer3;
     
      //TESTS ON RANDOMLY GENERATED BINARY FILES
      int plength, tlength;
      StringBuilder t, p;
      String tt, pp;
      try{ 
         for(int trial = 0; trial < 10; trial++){
            System.out.println("Starting binary trial " + trial);
            plength = 1;
            tlength = 10;  
             
            while(tlength < 20000000){
               t = new StringBuilder(tlength);
               for(int i = 0; i < tlength; i++){
                  t.append(rng.nextInt(2));
               }
               tt = t.toString();
               
               plength = 1;
               while(plength < 1000){
                  if(plength > tlength)
                     break;  
                     
                  p = new StringBuilder(plength);
                  for(int i = 0; i < plength; i++){
                     p.append(rng.nextInt(2));
                  }
      
                  pp = p.toString();
                                
                  output.write(pp.length() + "\t" + tt.length() + "\t"); //pattern length and text length
                                
                  answer = sm.bruteForce(tt, pp);
                  output.write(answer + "\t"); //number of comparions for brute force algorithm
                   
                  answer2 = sm.horspool(tt, pp);
                  output.write(answer2 + "\t"); //number of comparisons for horspool's algorithm
                  
                  answer3 = sm.boyermoore(tt, pp);
                  output.write(answer3 + "\n"); //number of comparisons for boyer-moore algorithm
                  
                  plength *= 13;
                  plength /= 10;
                  if(plength < 10)
                     plength *= 2;
               }     
               tlength *= 13;
               tlength /= 10;
            }
         }   
      
         output.close();   
      } catch (IOException e){
         System.out.println("Something went wrong while writing to output file: " + e);
      }


      //TESTS ON NATURAL LANGUAGE TEXTS
      String[] books = new String[20];
      int minBookLength;
      for(int i = 0; i < 20; i++){
         try{
            books[i] = readFile("book"+i+".txt", StandardCharsets.UTF_8);
         } catch(IOException e){
            System.out.println("Something failed while reading files: " + e);
         }   
      }

      minBookLength = books[0].length();
      for(int i = 1; i < books.length; i++){
         if(books[i].length() < minBookLength)
            minBookLength = books[i].length();
      }
      
      int[] plengths = genPLengths(MAX_PATTERN_LENGTH);
      int[] tlengths = genTLengths(minBookLength);
      int plen, tlen, book, start, index;
      long avg;
      String toSearch, toFind;
      try{            
         for(int trial = 0; trial < 10; trial++){
            System.out.println("Starting natural lang trial " + trial);
            for(int j = 0; j < tlengths.length; j++){
               tlen = tlengths[j];
               index = 0;
                              
               //pick a books at random
               book = rng.nextInt(books.length);
               //pick a random section from that book to be the text
               start = rng.nextInt(books[book].length() - tlen);
               toSearch = books[book].substring(start, start + tlen);
               
               plen = 1;
               while(index < plengths.length && plengths[index] <= tlen){
                  plen = plengths[index];
                  //pick a book at random
                  book = rng.nextInt(books.length);
                  //pick a random  section from that book to be the pattern
                  start = rng.nextInt(books[book].length() - plen);
                  toFind = books[book].substring(start, start + plen);               
                  output2.write(plen + "\t" + tlen + "\t");
                                    
                  //search for the pattern in the text using the algorithms
                  answer = sm.bruteForce(toSearch, toFind);
                  output2.write(answer + "\t");  
                  
                  answer2 = sm.horspool(toSearch, toFind);
                  output2.write(answer2 + "\t");
                  
                  answer3 = sm.boyermoore(toSearch, toFind);
                  output2.write(answer3 + "\n");
                  
                  index++;
               }
            }
         }
         
         output2.close();   
      } catch (IOException e){
         System.out.println("Something went wrong while writing to output file: " + e);
      }     

   }
   
   private static String readFile(String path, Charset encoding) throws IOException{
      byte[] encoded = Files.readAllBytes(Paths.get(path));
      return new String(encoded, encoding);
   }
   
   private static int[] genPLengths(int max){
      int len = 1;
      int i = 1;
      while(i < max){
         len++;
         if(i < 10)
            i *= 2;
         else   
            i = (int)((double)i * PATTERN_GROWTH_RATE);
      }
      
      int[] values = new int[len];
      int val = 1;
      for(i = 0; i < len; i++){
         values[i] = val;
         if(val < 10)
            val *= 2;
         else
            val = (int)((double)val * PATTERN_GROWTH_RATE);      
      }
      
      return values;
   }
   
   private static int[] genTLengths(int max){
      int len = 0;
      int i = 10;
      while(i < max){
         len++;
         i = (int)((double)i * TEXT_GROWTH_RATE);
      }
      
      int[] values = new int[len];
      int val = 10;
      for(i = 0; i < values.length; i++){
         values[i] = val;
         val = (int)((double)val * TEXT_GROWTH_RATE);

      }
            
      return values;
   }
}