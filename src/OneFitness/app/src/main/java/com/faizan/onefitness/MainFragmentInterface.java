package com.faizan.onefitness;

/*
Interface used to send updated values from
Tracker classes to the Main Fragment
 */
public interface MainFragmentInterface {
    void sendSteps(long totalSteps);

    void sendCalories(int totalCalories);

    void sendDistance(float totalDistance);
}

