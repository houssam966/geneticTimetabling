package com.timetabling;

import java.util.ArrayList;

public class Evaluator {

    int getNumberOfClashes(DNA timetable){
        int numberOfClashes = 0;
        for (int i = 0; i < timetable.clashes.length; i++) {
            Pair pair = timetable.clashes[i];
            if(timetable.timetable[pair.first] == 1 && timetable.timetable[pair.second] == 1){
                //there is a clash
                int studentNumber = pair.first  % timetable.numberOfStudents;
                int classNumber1 = pair.first  / timetable.numberOfStudents;
                int classNumber2 = pair.second  / timetable.numberOfStudents;
                //System.out.println(timetable.modules[timetable.classes[classNumber1].moduleIndex].name + ": " + classNumber1 + timetable.modules[timetable.classes[classNumber1].moduleIndex].name + ": " + ", " + classNumber2 );
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
        int[] tutCount = new int[numberOfStudents * numberOfModules];
        int[] practCount = new int[numberOfStudents * numberOfModules];
        int[] smgCount = new int[numberOfStudents * numberOfModules];
        int[] lecCount = new int[numberOfStudents * numberOfModules];

        for (int i = 0; i < timetable.timetable.length; i++) {
            int assigned = timetable.timetable[i];
            int studentNumber = i % numberOfStudents;
            int classNumber = i / numberOfStudents;
            int moduleNumber = classes[classNumber].moduleIndex;
            String type = classes[classNumber].type;
            studentsPerClass[classNumber] += assigned; //if assigned would add 1, otherwise add 0
            int moduleStudentIndex = moduleNumber * numberOfStudents + studentNumber;

            if (type.equals("Practical")) {
                practCount[moduleStudentIndex] += assigned;
            } else if(type.equals("Tutorial")) {
                tutCount[moduleStudentIndex] += assigned;
            } else if(type.equals("Small Group")) {
                smgCount[moduleStudentIndex] += assigned;
            } else if(type.equals("Lecture")) {
                lecCount[moduleStudentIndex] += assigned;
            }
        }
        return new Properties(practCount, tutCount, smgCount, lecCount, studentsPerClass);
    }

    int getIncorrectAllocations(DNA timetable) {
        int incorrectAllocations = 0;
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        Student[] students = timetable.students;
        Properties properties = getProperties(timetable);
        int[] tutCount = properties.tutorialCount;
        int[] practCount = properties.practicalCount;
        int[] smgCount = properties.smgCount;
        int[] lecCount = properties.lecCount;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int assignedTuts = tutCount[i];
            int assignedPracts = practCount[i];
            int assignedSmgs = smgCount[i];
            int assignedLecs = lecCount[i];
            if(students[studentNumber].modules[moduleNumber] == 0) {
                incorrectAllocations+= assignedTuts + assignedPracts + assignedSmgs + assignedLecs;
            }
        }
        return incorrectAllocations;
    }

    int getMissingAllocations(DNA timetable){
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        Student[] students = timetable.students;
        Properties properties = getProperties(timetable);
        int[] tutCount = properties.tutorialCount;
        int[] practCount = properties.practicalCount;
        int[] smgCount = properties.smgCount;
        int[] lecCount = properties.lecCount;
        int missingAllocations = 0;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int assignedTuts = tutCount[i];
            int assignedPracts = practCount[i];
            int assignedSmgs = smgCount[i];
            int assignedLecs = lecCount[i];
            Module module = timetable.modules[moduleNumber];
            boolean missing = false;
            if(students[studentNumber].modules[moduleNumber] == 1) {
                //if student takes module
                if (module.hasPractical && assignedPracts <= 0){
                    missingAllocations++;
                    missing = true;
                    System.out.println("PRACTICAL MISSING");
                }

                if (module.hasTutorial && assignedTuts <= 0){
                    missingAllocations++;
                    missing = true;
                    System.out.println("TUTORIAL MISSING");
                }
                if (module.hasSmallGroup && assignedSmgs <= 0){
                    System.out.println("SMG MISSING");
                    missingAllocations++;
                    missing = true;
                }
                if (module.hasLecture && assignedLecs <= 0){
                    missingAllocations++;
                    System.out.println("LECTURE MISSING");
                    missing = true;
                }
            }
            if(missing) System.out.println("STUDENT NUMBER " +studentNumber + " Missing class for module " + module.name);
        }
        return missingAllocations;
    }
    int getExtraAllocations(DNA timetable){
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        Student[] students = timetable.students;
        Properties properties = getProperties(timetable);
        int[] tutCount = properties.tutorialCount;
        int[] practCount = properties.practicalCount;
        int[] smgCount = properties.smgCount;
        int[] lecCount = properties.lecCount;
        int extraAllocations = 0;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int assignedTuts = tutCount[i];
            int assignedPracts = practCount[i];
            int assignedSmgs = smgCount[i];
            int assignedLecs = lecCount[i];
            Module module = timetable.modules[moduleNumber];
            if(students[studentNumber].modules[moduleNumber] == 1) {
                //student takes module
                if(assignedPracts > 1) extraAllocations+= assignedPracts-1;
                if(module.hasTutorial){
                    if(assignedTuts > module.numOfTutorials) extraAllocations+= assignedTuts - module.numOfTutorials;
                }
                if(assignedSmgs > 1) extraAllocations+= assignedSmgs -1;
                if(module.hasLecture){
                    if(assignedLecs > module.numOfLectures) extraAllocations+= assignedLecs - module.numOfLectures;
                }
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

//    void getStudentProperties(DNA timetable) {
//        int numberOfStudents = timetable.numberOfStudents;
//        int numberOfModules = timetable.numberOfModules;
//        Module[] modules = timetable.modules;
//        Properties properties = getProperties(timetable);
//        int[] tutorialCount = properties.tutorialCount;
//        int[] practicalCount = properties.practicalCount;
//
//        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
//            int studentNumber = i%numberOfStudents;
//            int moduleNumber = i/numberOfStudents;
//            Module module = modules[moduleNumber];
//            int numberOfAssignedTutorials = tutorialCount[i];
//            int numberOfAssignedPracticals = practicalCount[i];
//
//            System.out.print("Student " + studentNumber + ": ");
//            System.out.print(module.name + ": " + numberOfAssignedPracticals + " Practicals, "
//                    + numberOfAssignedTutorials + " Tutorials");
//            if(numberOfAssignedPracticals > 1 || numberOfAssignedTutorials > 1);
//            System.out.println();
//        }
//    }


    int getInaccurateAllocations(DNA timetable, Weights weights){
        int inaccurateAllocations = 0;
        Student[] students = timetable.students;
        Module[] modules = timetable.modules;
        Activity[] classes = timetable.classes;
        Manager manager = new Manager();
        ArrayList<Integer>[] modulePracs = manager.getModuleClasses(modules, classes, "Practical");
        ArrayList<Integer>[] moduleTuts = manager.getModuleClasses(modules, classes, "Tutorial");
        ArrayList<Integer>[] moduleSmgs = manager.getModuleClasses(modules, classes, "Small Group");
        ArrayList<Integer>[] moduleLecs = manager.getModuleClasses(modules, classes, "Lecture");


        for (int i = 0; i < timetable.numberOfStudents; i++) {
            Student student = students[i];
            for (int moduleNumber = 0; moduleNumber < student.modules.length; moduleNumber++) {
                Module module = modules[moduleNumber];
                if(student.modules[moduleNumber] == 1){
                    //takes module
                    if(module.hasPractical){
                        ArrayList<Integer> preferredClasses = manager.getPreferredClasses(student,modulePracs[moduleNumber],classes,weights);
                        ArrayList<Integer> assignedClasses = manager.getAssignedClasses(i, modulePracs[moduleNumber],timetable);
                        int inac = manager.getInaccurateAllocations(preferredClasses, assignedClasses);
                        inaccurateAllocations+=inac;

                    }
                    if(module.hasTutorial){
                        ArrayList<Integer> preferredClasses = manager.getPreferredClasses(student,moduleTuts[moduleNumber],classes,weights);
                        ArrayList<Integer> assignedClasses = manager.getAssignedClasses(i, moduleTuts[moduleNumber],timetable);
                        int inac = manager.getInaccurateAllocations(preferredClasses, assignedClasses);
                        inaccurateAllocations+=inac;
                    }
                    if(module.hasSmallGroup){
                        ArrayList<Integer> preferredClasses = manager.getPreferredClasses(student,moduleSmgs[moduleNumber],classes,weights);
                        ArrayList<Integer> assignedClasses = manager.getAssignedClasses(i, moduleSmgs[moduleNumber],timetable);
                        int inac = manager.getInaccurateAllocations(preferredClasses, assignedClasses);
                        inaccurateAllocations+=inac;
                    }
                    if(module.hasLecture){
                        ArrayList<Integer> preferredClasses = manager.getPreferredClasses(student,moduleLecs[moduleNumber],classes,weights);
                        ArrayList<Integer> assignedClasses = manager.getAssignedClasses(i, moduleLecs[moduleNumber],timetable);
                        int inac = manager.getInaccurateAllocations(preferredClasses, assignedClasses);
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
