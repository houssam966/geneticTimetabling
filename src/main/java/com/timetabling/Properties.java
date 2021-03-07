package com.timetabling;

/**
 * This class represents the properties of a timetable
 * this includes relevant information such as number of students per class or number of practicals/tutorials
 * assigned to students
 */
public class Properties {
    int[] practicalCount;
    int[] tutorialCount;
    int[] studentsPerClass;
    public Properties(int[] practicalCount, int[] tutorialCount, int[] studentsPerClass){
        this.practicalCount = practicalCount;
        this.tutorialCount = tutorialCount;
        this.studentsPerClass = studentsPerClass;
    }
}
