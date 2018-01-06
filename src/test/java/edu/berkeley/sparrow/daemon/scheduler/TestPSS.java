package edu.berkeley.sparrow.daemon.scheduler;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestPSS {

//The function outputs cdf in the range of 0 to 1 from the given worker speed
@Test
public void getCDFWorkerSpeed() throws IOException {
    ArrayList<Double> final_worker_speeds = new ArrayList<Double>(Arrays.asList(1.0, 1.0, 1.0, 0.01, 0.109, 0.406, 1.0, 1.0, 1.0, 1.0));
    double[] received = UnconstrainedTaskPlacer.getCDFWokerSpeed(final_worker_speeds);
    double[] expected = new double[]{0.13289037,  0.26578073,  0.3986711,   0.4,         0.41448505 , 0.46843854,
            0.6013289,   0.73421927,  0.86710963,  1.0};

    assertArrayEquals(expected, received,7); //7 is the precision.
}

//The unique reservation should output the index from the cdf_worker_speed.
//If the difference between succesive element is large, then the
//binary search using uniform sampling should output that index with higher frequency
//Similarly, if the range is small, uniform sampling has lesser probability of getting a number
//from that range.
    @Test
    public void getIndexFromPSS(){
        ArrayList<Integer> workerSpeedList = new ArrayList<Integer>();
        double[] cdf_worker_speed= new double[]{0.01,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1};
        for(int i = 0; i<1000; i++) {
            workerSpeedList.add(UnconstrainedTaskPlacer.getIndexFromPSS(cdf_worker_speed));
        }
        //Added these indices for further check
        int zerothIndexCount= 0;
        int firstIndexCount = 0;
        int secondIndexCount = 0;
        int thirdIndexCount = 0;
        int fourthIndexCount = 0;
        int fifthIndexCount = 0;
        int sixthIndexCount = 0;
        int seventhIndexCount = 0;
        int eightIndexCount = 0;
        int ninthIndexCount = 0;

        for(int i = 0; i<workerSpeedList.size(); i++) {
            //Counted all the index anyways
            if(workerSpeedList.get(i) == 0){
                zerothIndexCount +=1;
            }
            if(workerSpeedList.get(i) == 1){
                firstIndexCount +=1;
            }
            if(workerSpeedList.get(i) ==2){
                secondIndexCount +=1;
            }
            if(workerSpeedList.get(i) == 3){
                thirdIndexCount +=1;
            }
            if(workerSpeedList.get(i) == 4){
                fourthIndexCount +=1;
            }
            if(workerSpeedList.get(i) == 5){
                fifthIndexCount +=1;
            }
            if(workerSpeedList.get(i) == 6){
                sixthIndexCount +=1;
            }
            if(workerSpeedList.get(i) == 7){
                seventhIndexCount +=1;
            }
            if(workerSpeedList.get(i) == 8){
                eightIndexCount +=1;
            }
            if(workerSpeedList.get(i) == 9){
                ninthIndexCount +=1;
            }
        }
        assertEquals(firstIndexCount>zerothIndexCount && firstIndexCount> thirdIndexCount, true);
        assertEquals(zerothIndexCount<thirdIndexCount&& zerothIndexCount< firstIndexCount, true);
        assertEquals(thirdIndexCount>zerothIndexCount && thirdIndexCount < firstIndexCount, true);
    }
}
