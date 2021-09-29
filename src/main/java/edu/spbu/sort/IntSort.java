package edu.spbu.sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by artemaliev on 07/09/15.
 */
public class IntSort {
  public static void sort (int[] array) { //merge sort
    sort(array, 0, array.length);
  }
  public static void sort (int[] array, int start, int end) { //left/right
    int size = end-start;
    if (size == 1 ||  size == 0){
      return;
    }
    int middle = start + (end - start)/2;
    sort(array, start, middle);
    sort(array, middle, end);
    int l = start, r = middle, i = 0;
    int[] sorted_array = new int[size];
    while (l<middle && r<end) {
      if(array[l]<array[r]){
        sorted_array[i] = array[l];
        l += 1;
      }
      else{
        sorted_array[i] = array[r];
        r += 1;
      }
      i += 1;
    }
    while (l<middle) {
      sorted_array[i] = array[l];
      l += 1;
      i += 1;
    }
    while (r<end) {
      sorted_array[i] = array[r];
      r += 1;
      i += 1;
    }
    //как оптимизировать
    System.arraycopy(sorted_array, 0, array, start, size);
  }

  public static void sort (List<Integer> list) {
    Collections.sort(list);
  }

//  public static void main(String[] args) {
//    int[] array = {1, 5, 4, 4};
//    sort(array);
//    for(int i = 0; i < array.length; i++) {
//      System.out.print(array[i]);
//      System.out.print(" ");
//    }
//  }
}
