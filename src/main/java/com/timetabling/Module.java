package com.timetabling;

import java.util.ArrayList;

public class Module {
    String name;
    boolean hasTutorial, hasPractical, hasSmallGroup, hasLecture;
    int numOfPracticals, numOfTutorials, numOfSmallGroups, numOfLectures;
    ArrayList<Integer> practicals, tutorials, smgs, lectures;


    public Module(String name, boolean hasTutorial, boolean hasPractical, boolean hasSmallGroup,
                  boolean hasLecture, int numOfPracticals, int numOfTutorials, int numOfSmallGroups, int numOfLectures){
        this.name = name;
        this.hasPractical = hasPractical;
        this.hasTutorial = hasTutorial;
        this.hasSmallGroup = hasSmallGroup;
        this.hasLecture = hasLecture;

        this.numOfPracticals = numOfPracticals;
        this.numOfTutorials = numOfTutorials;
        this.numOfSmallGroups = numOfSmallGroups;
        this.numOfLectures = numOfLectures;
        practicals = new ArrayList<>();
        lectures = new ArrayList<>();
        tutorials = new ArrayList<>();
        smgs = new ArrayList<>();
    }

    void addPractical(int classNumber){
        if(practicals.contains(classNumber)) return;
        practicals.add(classNumber);
    }
    void addLecture(int classNumber){
        if(lectures.contains(classNumber)) return;
        lectures.add(classNumber);
    }
    void addTutorial(int classNumber){
        if(tutorials.contains(classNumber)) return;
        tutorials.add(classNumber);
    }
    void addSmg(int classNumber){
        if(smgs.contains(classNumber)) return;
        smgs.add(classNumber);
    }
}
