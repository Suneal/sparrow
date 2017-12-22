package edu.berkeley.sparrow.daemon.util;

public class MinMax {
    //Find maximum (largest) value in array using loop
    public static double getMaxValue(double[] numbers){
        double maxValue = numbers[0];
        for(int i=1;i<numbers.length;i++){
            if(numbers[i] > maxValue){
                maxValue = numbers[i];
            }
        }
        return maxValue;
    }
    //Find minimum (lowest) value in array using loop
    public static double getMinValue(double[] numbers){
        double minValue = numbers[0];
        for(int i=1;i<numbers.length;i++){
            if(numbers[i] < minValue){
                minValue = numbers[i];
            }
        }
        return minValue;
    }
    public static int getMaxValue(int[] numbers){
        int maxValue = numbers[0];
        for(int i=1;i<numbers.length;i++){
            if(numbers[i] > maxValue){
                maxValue = numbers[i];
            }
        }
        return maxValue;
    }
    //Find minimum (lowest) value in array using loop
    public static int getMinValue(int[] numbers){
        int minValue = numbers[0];
        for(int i=1;i<numbers.length;i++){
            if(numbers[i] < minValue){
                minValue = numbers[i];
            }
        }
        return minValue;
    }
}
