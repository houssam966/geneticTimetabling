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
//    int[] maxClasses; // max classes per day
    //TODO add preferences
    public Student(int[] modules, int numberOfClasses, int numberOfStudents){
        this.modules = modules;
        required = new int[numberOfClasses];
        classPreferences = new int[numberOfClasses];
        dayPreferences = new int[5];
        studentPreferences = new int[numberOfStudents];
        timePreferences = new int[5];
    }
    public Student(int numberOfModules ,int numberOfClasses, int numberOfStudents){
        this.modules = new int[numberOfModules];
        required = new int[numberOfClasses];
        classPreferences = new int[numberOfClasses];
        dayPreferences = new int[5];
        studentPreferences = new int[numberOfStudents];
        timePreferences = new int[5];
    }
    public Student(int[] modules, int numberOfClasses, int[]dayPreferences,
                   int[]studentPreferences, int[]timePreferences){
        this.modules = modules;
        required = new int[numberOfClasses];
        classPreferences = new int[numberOfClasses];
        this.dayPreferences = dayPreferences;
        this.studentPreferences = studentPreferences;
        this.timePreferences = timePreferences;
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
    void addDayPreference(int dayNumber, int preferenceNumber){
        dayPreferences[dayNumber] = preferenceNumber;
    }
//    void addTaPreference(String ta, int preferenceNumber){
//
//    }
    void addStudentPreference(int studentNumber, int preferenceNumber){
        studentPreferences[studentNumber] = preferenceNumber;
    }
    void addTimePreference(int dayNumber, int preferenceNumber){
        timePreferences[dayNumber] = preferenceNumber;
    }

    void setPreferredClasses(ArrayList<Integer>[] preferredClasses){
        this.preferredClasses = preferredClasses;
    }
}
