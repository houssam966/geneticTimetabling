package com.timetabling;
import java.util.ArrayList;

public class Student {
    int[] modules; //which modules are taken
    int[] required; //which classes are required
    int[] classPreferences; // takes values from -5 to 5, student can choose which classes they prefer
    int[] dayPreferences; //takes values from -5 to 5
    int[] studentPreferences; // 1 or 0
    int[] timePreferences; //1 is morning, -1 is afternoon, 0 is no preference
    ArrayList<Integer>[] preferredClasses;

    public Student(int numberOfModules ,int numberOfClasses, int numberOfStudents){
        this.modules = new int[numberOfModules];
        required = new int[numberOfClasses];
        classPreferences = new int[numberOfClasses];
        dayPreferences = new int[5];
        studentPreferences = new int[numberOfStudents];
        timePreferences = new int[5];
    }

    void addModule(int moduleNumber){
        modules[moduleNumber] = 1;
    }
    void addAllDayPreferences(int[] preferences){
        dayPreferences = preferences;
    }
    void addAllStudentPreferences(int[] preferences){
        studentPreferences = preferences;
    }
    void addAllTimePreferences(int[] preferences){
        timePreferences = preferences;
    }

    void setPreferredClasses(ArrayList<Integer>[] preferredClasses){
        this.preferredClasses = preferredClasses;
    }

}
