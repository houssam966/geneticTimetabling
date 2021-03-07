package com.timetabling;

public class Module {
    String name;
    boolean hasTutorial;
    boolean hasPractical;
    public Module(String name, boolean hasTutorial, boolean hasPractical){
        this.name = name;
        this.hasPractical = hasPractical;
        this.hasTutorial = hasTutorial;
    }
}
