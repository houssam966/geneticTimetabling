package com.timetabling;

import java.util.ArrayList;

public class Evaluator {

    int getNumberOfClashes(DNA timetable){
        int numberOfClashes = 0;
        for (int i = 0; i < timetable.clashes.length; i++) {
            Pair pair = timetable.clashes[i];
            if(timetable.timetable[pair.first] == 1 && timetable.timetable[pair.second] == 1){
                //there is a clash
                numberOfClashes++;
            }
        }
        return numberOfClashes;
    }

    Properties getProperties(DNA timetable){
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        int numberOfClasses = timetable.numberOfClasses;
        Activity[] classes = timetable.classes;
        int[] studentsPerClass = new int[numberOfClasses];
        int[] tutorialCount = new int[numberOfStudents * numberOfModules];
        int[] practicalCount = new int[numberOfStudents * numberOfModules];

        for (int i = 0; i < timetable.timetable.length; i++) {
            int assigned = timetable.timetable[i];
            int studentNumber = i % numberOfStudents;
            int classNumber = i / numberOfStudents;
            int moduleNumber = classes[classNumber].moduleIndex;
            String type = classes[classNumber].type;
            studentsPerClass[classNumber] += assigned; //if assigned would add 1, otherwise add 0
            int moduleStudentIndex = moduleNumber * numberOfStudents + studentNumber;

            //assuming only two possible types, practical or tutorial
            if (type.equals("Practical")) {
                practicalCount[moduleStudentIndex] += assigned;
            } else {

                tutorialCount[moduleStudentIndex] += assigned;
            }
        }
        return new Properties(practicalCount, tutorialCount, studentsPerClass);
    }

    int getIncorrectAllocations(DNA timetable) {
        int incorrectAllocations = 0;
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        Student[] students = timetable.students;
        Properties properties = getProperties(timetable);
        int[] tutorialCount = properties.tutorialCount;
        int[] practicalCount = properties.practicalCount;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];
            if(students[studentNumber].modules[moduleNumber] == 0) {
                incorrectAllocations+= numberOfAssignedPracticals + numberOfAssignedTutorials;
            }
        }
        return incorrectAllocations;
    }

    int getMissingAllocations(DNA timetable){
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        Student[] students = timetable.students;
        Properties properties = getProperties(timetable);
        int[] tutorialCount = properties.tutorialCount;
        int[] practicalCount = properties.practicalCount;
        int missingAllocations = 0;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];
            Module module = timetable.modules[moduleNumber];

            if(students[studentNumber].modules[moduleNumber] == 1) {
                //if student takes module
                if (module.hasPractical && numberOfAssignedPracticals <= 0)
                    missingAllocations++;
                if (module.hasTutorial && numberOfAssignedTutorials <= 0)
                    missingAllocations++;
            }
        }
        return missingAllocations;
    }
    int getExtraAllocations(DNA timetable){
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        Student[] students = timetable.students;
        Properties properties = getProperties(timetable);
        int[] tutorialCount = properties.tutorialCount;
        int[] practicalCount = properties.practicalCount;
        int extraAllocations = 0;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];

            if(students[studentNumber].modules[moduleNumber] == 1) {
                //student takes module
                if(numberOfAssignedPracticals > 1)
                    extraAllocations+= numberOfAssignedPracticals-1;

                if(numberOfAssignedTutorials > 1)
                    extraAllocations+= numberOfAssignedTutorials -1;
            }
        }
        return extraAllocations;
    }
    int getOverLimitClasses(DNA timetable) {
        int overLimitClasses = 0;
        Properties properties = getProperties(timetable);
        int[] studentsPerClass = properties.studentsPerClass;
        //check number of students per class
        for (int i = 0; i < studentsPerClass.length; i++) {
            int students = studentsPerClass[i];
            int limit = timetable.classes[i].capacity;
            if (students > limit) overLimitClasses+= students-limit;
        }
        return overLimitClasses;
    }

    void getStudentProperties(DNA timetable) {
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        Module[] modules = timetable.modules;
        Properties properties = getProperties(timetable);
        int[] tutorialCount = properties.tutorialCount;
        int[] practicalCount = properties.practicalCount;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            Module module = modules[moduleNumber];
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];

            System.out.print("Student " + studentNumber + ": ");
            System.out.print(module.name + ": " + numberOfAssignedPracticals + " Practicals, "
                    + numberOfAssignedTutorials + " Tutorials");
            if(numberOfAssignedPracticals > 1 || numberOfAssignedTutorials > 1);
            System.out.println();
        }
    }


    int getInaccurateAllocations(DNA timetable, Weights weights){
        int inaccurateAllocations = 0;
        Student[] students = timetable.students;
        Module[] modules = timetable.modules;
        Activity[] classes = timetable.classes;
        Manager manager = new Manager();
        ArrayList<Integer>[] modulePracticals = manager.getModulePracticals(modules, classes);
        ArrayList<Integer>[] moduleTutorials = manager.getModuleTutorials(modules, classes);


        for (int i = 0; i < timetable.numberOfStudents; i++) {
            Student student = students[i];
            for (int moduleNumber = 0; moduleNumber < student.modules.length; moduleNumber++) {
                Module module = modules[moduleNumber];
                if(student.modules[moduleNumber] == 1){
                    //takes module
                    if(module.hasPractical){
                        ArrayList<Integer> preferredClasses = manager.getPreferredClasses(student,modulePracticals[moduleNumber],classes,weights);
                        ArrayList<Integer> assignedClasses = manager.getAssignedClasses(i, modulePracticals[moduleNumber],timetable);
                        int inac = manager.getInaccurateAllocations(preferredClasses, assignedClasses);
                        if(inac > 0){
                            System.out.println("Student " + i + " Module: " + module.name + " Type: Practical");
                        }
                        inaccurateAllocations+=inac;

                    }
                    if(module.hasTutorial){
                        ArrayList<Integer> preferredClasses = manager.getPreferredClasses(student,moduleTutorials[moduleNumber],classes,weights);
                        ArrayList<Integer> assignedClasses = manager.getAssignedClasses(i, moduleTutorials[moduleNumber],timetable);
                        int inac = manager.getInaccurateAllocations(preferredClasses, assignedClasses);
                        if(inac > 0){
                            System.out.println("Student " + i + " Module: " + module.name + " Type: TUTORIAL");
                        }
                        inaccurateAllocations+=inac;
                    }
                }
            }
        }
        return inaccurateAllocations;
    }

    void checkSoftConstraints(DNA timetable, Weights weights){
        Student[] students = timetable.students;
        Module[] modules = timetable.modules;
        Activity[] classes = timetable.classes;
        ArrayList<Integer>[] modulePracticals = new ArrayList[modules.length];
        ArrayList<Integer>[] moduleTutorials = new ArrayList[modules.length];

        //initialise arraylists within array
        for (int i = 0; i < modules.length; i++) {
            modulePracticals[i] = new ArrayList<>();
            moduleTutorials[i] = new ArrayList<>();
        }
        Manager manager = new Manager();
        for (int j = 0; j < modules.length;j++) {
            System.out.println("\n"+ modules[j].name + " ");
            ArrayList<Integer> practicals = new ArrayList<>();
            ArrayList<Integer> tutorials = new ArrayList<>();
            for (int k = 0; k < classes.length; k++) {
                if(classes[k].moduleIndex == j){
                    if(classes[k].type.equals("Practical")){
                        practicals.add(k);
                        modulePracticals[j].add(k);
                    } else{
                        tutorials.add(k);
                        moduleTutorials[j].add(k);
                    }
                }
            }
            if(modules[j].hasPractical){
                System.out.println("\t"+"Practical Options: ");
                for (int i1 = 0; i1 < practicals.size(); i1++) {
                    int classNumber = practicals.get(i1);
                    System.out.print("\t\t"+ modules[j].name + classNumber + " " + classes[classNumber].day);
                    if(classes[classNumber].timePeriod == 1){
                        System.out.print(" AM");
                    }else{
                        System.out.print(" PM");
                    }
                }
            }
            if(modules[j].hasTutorial) {
                System.out.println("\n\t" + "Tutorial Options: ");
                for (int i1 = 0; i1 < practicals.size(); i1++) {
                    int classNumber = practicals.get(i1);
                    System.out.print("\t\t" + modules[j].name + classNumber + " " + classes[classNumber].day);
                    if (classes[classNumber].timePeriod == 1) {
                        System.out.print(" AM");
                    } else {
                        System.out.print(" PM");
                    }
                }
            }

        }

        for (int i = 0; i < timetable.numberOfStudents; i++) {
            Student student = students[i];
            System.out.println("\n\nStudent " + i + " :");
            for (int moduleNumber = 0; moduleNumber < student.modules.length; moduleNumber++) {
                Module module = modules[moduleNumber];
                if(student.modules[moduleNumber] == 1){
                    //takes module
                    System.out.println("\n\t"+ module.name);
                    if(module.hasPractical){
                        ArrayList<Integer> prefferedClasses = manager.getPreferredClasses(student,modulePracticals[moduleNumber],classes,weights);
                        ArrayList<Integer> assignedClasses = manager.getAssignedClasses(i, modulePracticals[moduleNumber],timetable);
                        System.out.print("\n\t\t prefers: ");
                        for (int i1 = 0; i1 < prefferedClasses.size(); i1++) {
                            System.out.print( module.name + prefferedClasses.get(i1) + " ");
                        }
                        System.out.print(", assigned: " );

                        for (int i1 = 0; i1 < assignedClasses.size(); i1++) {
                            System.out.print( module.name + assignedClasses.get(i1) + " ");
                        }

                        //check if person was assigned one of the preferred, if not add it to variable
                    }
                    if(module.hasTutorial){
                        ArrayList<Integer> prefferedClasses = manager.getPreferredClasses(student,moduleTutorials[moduleNumber],classes,weights);
                        ArrayList<Integer> assignedClasses = manager.getAssignedClasses(i, moduleTutorials[moduleNumber],timetable);

                        System.out.print("\n\t\t prefers: ");
                        for (int i1 = 0; i1 < prefferedClasses.size(); i1++) {
                            System.out.print( module.name + prefferedClasses.get(i1) + " ");
                        }
                        System.out.print(", assigned: " );

                        for (int i1 = 0; i1 < assignedClasses.size(); i1++) {
                            System.out.print( module.name + assignedClasses.get(i1) + " ");
                        }

                        //check if person was assigned one of the preferred, if not add it to variable
                    }

                }
            }
        }
    }
//    int[] get(DNA timetable, Weights weights){
//        int inaccurateAllocations = 0;
//        Student[] students = timetable.students;
//        Module[] modules = timetable.modules;
//        Activity[] classes = timetable.classes;
//        Manager manager = new Manager();
//        ArrayList<Integer>[] modulePracticals = manager.getModulePracticals(modules, classes);
//        ArrayList<Integer>[] moduleTutorials = manager.getModuleTutorials(modules, classes);
//
//
//        for (int i = 0; i < timetable.numberOfStudents; i++) {
//            Student student = students[i];
//            for (int moduleNumber = 0; moduleNumber < student.modules.length; moduleNumber++) {
//                Module module = modules[moduleNumber];
//                if(student.modules[moduleNumber] == 1){
//                    //takes module
//                    if(module.hasPractical){
//                        ArrayList<Integer> preferredClasses = manager.getPreferredClasses(student,modulePracticals[moduleNumber],classes,weights);
//                        ArrayList<Integer> assignedClasses = manager.getAssignedClasses(i, modulePracticals[moduleNumber],timetable);
//                        inaccurateAllocations+= manager.getInaccurateAllocations(preferredClasses, assignedClasses);
//
//                    }
//                    if(module.hasTutorial){
//                        ArrayList<Integer> preferredClasses = manager.getPreferredClasses(student,moduleTutorials[moduleNumber],classes,weights);
//                        ArrayList<Integer> assignedClasses = manager.getAssignedClasses(i, moduleTutorials[moduleNumber],timetable);
//                        inaccurateAllocations+= manager.getInaccurateAllocations(preferredClasses, assignedClasses);
//                    }
//                }
//            }
//        }
//        return inaccurateAllocations;
//    }
}
