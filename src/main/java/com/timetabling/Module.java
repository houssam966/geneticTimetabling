package com.timetabling;

public class Module {
    String name;
    boolean hasTutorial;
    boolean hasPractical;
    int numOfPracticals, numOfTutorials;

    public Module(String name, boolean hasTutorial, boolean hasPractical, int numOfPracticals, int numOfTutorials){
        this.name = name;
        this.hasPractical = hasPractical;
        this.hasTutorial = hasTutorial;
        this.numOfPracticals = numOfPracticals;
        this.numOfTutorials = numOfTutorials;
    }
}
