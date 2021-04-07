package com.timetabling;

/**
 * This class represents the properties of a timetable
 * this includes relevant information such as number of students per class or number of practicals/tutorials
 * assigned to students
 */
public class Properties {
    int[] practicalCount, tutorialCount, smgCount, lecCount;
    int[] studentsPerClass;
    public Properties(int[] practicalCount, int[] tutorialCount, int[] smgCount, int[] lecCount, int[] studentsPerClass){
        this.practicalCount = practicalCount;
        this.tutorialCount = tutorialCount;
        this.smgCount = smgCount;
        this.lecCount = lecCount;
        this.studentsPerClass = studentsPerClass;
    }
}
