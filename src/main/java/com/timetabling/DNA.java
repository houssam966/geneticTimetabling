package com.timetabling;

import java.util.ArrayList;
import java.util.Random;

public class DNA {
    private float fitness;
    int[] timetable;
    int numberOfStudents, numberOfClasses, numberOfModules, size;
    Student[] students;
    Activity[] classes;
    Module[] modules;
    Pair[] clashes;
    float[] preferences;

    public DNA(int numberOfStudents, int numberOfClasses, int numberOfModules, Student[] students, Activity[] classes, Module[] modules, Pair[] clashes, float[] preferences){
        this.numberOfClasses = numberOfClasses;
        this.numberOfStudents = numberOfStudents;
        this.numberOfModules = numberOfModules;
        this.students = students;
        this.classes = classes;
        this.modules = modules;
        this.clashes = clashes;
        this.preferences = preferences;

        size = numberOfClasses * numberOfStudents;
        timetable = new int[size];
        Random random = new Random();
        float prob = random.nextFloat();
        //generate a random timetable
        for (int i = 0; i < size; i++) {
            if(prob > 0.5){
                timetable[i] = 1;
            } else {
                timetable[i] = 0;
            }
        }
    }

    /**
     * Hard Constraints:
     * students should only be allocated to classes that they have
     *
     */
    void updateFitness(Weights w){
        int[] studentsPerClass = new int[numberOfClasses];
        int[] tutorialCount = new int[numberOfStudents*numberOfModules];
        int[] practicalCount = new int[numberOfStudents*numberOfModules];

        float classPreferenceTotal = 0;
        int numberOfClashes = 0;
        int extraAllocations = 0;
        int overAllocations = 0;
        int incorrectAllocations = 0;
        int missingAllocations = 0;

        for (int i = 0; i < timetable.length; i++) {
            int assigned = timetable[i];
            int studentNumber = i % numberOfStudents;
            int classNumber = i / numberOfStudents;
            int moduleNumber = classes[classNumber].moduleIndex;

            String type = classes[classNumber].type;
            studentsPerClass[classNumber]+= assigned; //if assigned would add 1, otherwise add 0

            int moduleStudentIndex = moduleNumber*numberOfStudents + studentNumber;
            if(type.equals("Practical")){
                practicalCount[moduleStudentIndex]+=assigned;
            } else{
                tutorialCount[moduleStudentIndex]+=assigned;
            }
            classPreferenceTotal += preferences[i]*assigned;
        }

        //check allocations of students to practical and tutorials (first two constraints)
        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            Module module = modules[moduleNumber];
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];

            if(students[studentNumber].modules[moduleNumber] == 1){
                //student takes module
                if(module.hasTutorial){
                    if(numberOfAssignedTutorials <= 0) missingAllocations++;
                    else if(numberOfAssignedTutorials > 1) extraAllocations+= numberOfAssignedTutorials-1;
                }
                if(module.hasPractical){
                    if(numberOfAssignedPracticals <= 0) missingAllocations++;
                    else if(numberOfAssignedPracticals > 1) extraAllocations+= numberOfAssignedPracticals-1;
                }
            } else {
                incorrectAllocations+= numberOfAssignedPracticals + numberOfAssignedTutorials;
            }
        }

        //check number of students per class
        for (int i = 0; i < studentsPerClass.length; i++) {
            int students = studentsPerClass[i];
            int limit = classes[i].capacity;
            if(students>200){
                overAllocations+= (students-limit);
            }
        }

        //check clashes
        for (int i = 0; i < clashes.length; i++) {
            Pair pair = clashes[i];
            if(timetable[pair.first] == 1 & timetable[pair.second] == 1)
                numberOfClashes++;
        }
//        fitness = 1f/(1+((w.clash*numberOfClashes + w.extra*extraAllocations + w.missing*missingAllocations
//                + w.incorrect*incorrectAllocations + w.over*overAllocations))) + 0.2f*(classPreferenceTotal/(numberOfStudents*numberOfClasses));
        fitness = 1/ (1+ (w.clash*numberOfClashes + w.extra*extraAllocations + w.missing*missingAllocations + w.incorrect*incorrectAllocations + w.over*overAllocations)) + (classPreferenceTotal/(numberOfStudents*numberOfClasses));
//
// int numberOfViolatedConstraints = numberOfClashes + extraAllocations + missingAllocations + incorrectAllocations + overAllocations;
//        fitness = 1f/(float) Math.pow((1+numberOfViolatedConstraints),2);
//        fitness = 1f/(1+numberOfViolatedConstraints);

    }

    // Crossover
    public DNA crossover(DNA partner) {
        // A new child
        DNA child = new DNA(numberOfStudents, numberOfClasses, numberOfModules, students, classes, modules, clashes, preferences);
        Random r = new Random();
        int midpoint = r.nextInt(timetable.length); // Pick a midpoint

        // Half from one, half from the other
        for (int i = 0; i < timetable.length; i++) {
            // Use half of parent1's genes and half of parent2's genes
//            if (0.5 > Math.random()) {
//                child.timetable[i] = timetable[i];
//            } else {
//                child.timetable[i] = partner.timetable[i];
//            }
            //this method is much better as it deals with chunks
            if (i > midpoint) child.timetable[i] = timetable[i];
            else child.timetable[i] = partner.timetable[i];
        }
        return child;
    }

    // Based on a mutation probability, picks a new random character
    void mutate(float mutationRate) {
        Random r = new Random();
        //for all genes(all students), pick a random number, if it's less than mutation rate, flip bits
        for (int i = 0; i < timetable.length; i++) {
            if(r.nextFloat() < mutationRate){
                if(timetable[i] == 0) timetable[i] = 1;
                else if(timetable[i] == 1) timetable[i] = 0;
            }

        }
    }

    void improve(float adjustmentRate, boolean elite) {
        Random r = new Random();
        Evaluator evaluator = new Evaluator();
        Properties properties = evaluator.getProperties(this);
        int[] tutorialCount = properties.tutorialCount;
        int[] practicalCount = properties.practicalCount;


        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];
            Module module = modules[moduleNumber];

            if(students[studentNumber].modules[moduleNumber] == 1) {
                //if student takes module
                if (module.hasPractical && numberOfAssignedPracticals <= 0) {
                    float max = -Float.MAX_VALUE;
                    int bestAllocation = -1;
                    for (int i1 = 0; i1 < classes.length; i1++) {
                        if (classes[i1].moduleIndex == moduleNumber && classes[i1].type == "Practical") {
                            int index = i1 * numberOfStudents + studentNumber;
                            if(preferences[index] > max){
                                max = preferences[i];
                                bestAllocation = index;
                            }
                        }
                    }
                    timetable[bestAllocation] = 1;
                }
                if (module.hasTutorial && numberOfAssignedTutorials <= 0) {
                    float max = -Float.MAX_VALUE;
                    int bestAllocation = -1;
                    for (int i1 = 0; i1 < classes.length; i1++) {
                        if (classes[i1].moduleIndex == moduleNumber && classes[i1].type == "Tutorial") {
                            int index = i1 * numberOfStudents + studentNumber;
                            if(preferences[index] > max){
                                max = preferences[i];
                                bestAllocation = index;
                            }
                        }
                    }
                    timetable[bestAllocation] = 1;
                }
                if(numberOfAssignedPracticals > 1){
                    int removed = 0;
                    for( int j=studentNumber; j < timetable.length; j+= numberOfStudents){
                        int classNumber = j / numberOfStudents;
                        if(classes[classNumber].moduleIndex == moduleNumber && classes[classNumber].type == "Practical"){
                            int index = classNumber * numberOfStudents + studentNumber;
                            timetable[index] = 0;
                            removed++;
                        }
                        if(removed == numberOfAssignedPracticals-1) break;
                    }
                }
                if(numberOfAssignedTutorials > 1){
                    int removed = 0;
                    for( int j=studentNumber; j < timetable.length; j+= numberOfStudents){
                        int classNumber = j / numberOfStudents;
                        if(classes[classNumber].moduleIndex == moduleNumber && classes[classNumber].type == "Tutorial"){
                            int index = classNumber * numberOfStudents + studentNumber;
                            timetable[index] = 0;
                            removed++;
                        }
                        if(removed == numberOfAssignedTutorials-1) break;
                    }
                }
            }
        }
        if(!elite){
            for (int i = 0; i < numberOfStudents; i++) {
                for( int j=i; j < timetable.length; j+= numberOfStudents){
                    //all classes for student i
                    int classNumber = j / numberOfStudents;
                    int moduleNumber = classes[classNumber].moduleIndex;
                    //remove extra allocations
                    if(students[i].required[classNumber] == 0 && timetable[j] == 1) timetable[j] = 0;
                    //remove incorrect allocations
                    if(students[i].modules[moduleNumber] == 0 && timetable[j] == 1) timetable[j] = 0;
                    ArrayList<Integer> preferred = students[i].preferredClasses[moduleNumber];
                    if(students[i].required[classNumber] == 1 && timetable[j] == 1 && r.nextFloat() < adjustmentRate){
                        if(!preferred.contains(classNumber)){
                            timetable[j] = 0;
                            int index = preferred.get(r.nextInt(preferred.size())) * numberOfStudents + i;
                            timetable[index] = 1;
                        }
                    }
                }
            }
        }

    }
//    public void printConstraints(){
//        int count = 0;
//        for (int i = 0; i < size; i++) {
//            int studentNumber = i%numberOfStudents;
//            int classNumber = i/numberOfStudents;
//
//            Student currentStudent = students[studentNumber];
//            if(studentNumber == 0) System.out.print("\n");
//
//            //check if student has class or not
//            if(currentStudent.hasClass(classNumber)){
//                //student has the class
//                if(timetable[i] == 1){
//                    System.out.print(" ");
//                } else {
//                    System.out.print("X");
//                    count++;
//                }
//            } else {
//                if(timetable[i] == 1){
//                    System.out.print("X");
//                    count++;
//                } else {
//                    System.out.print(" ");
//                }
//            }
//        }
//        System.out.println();
//        System.out.println("Number of incorrect allocations: " + count);
//    }

    public void print() {
        for (int i = 0; i < size; i++) {
            int student = i%numberOfStudents;
            if(student == 0) System.out.print("\n");
            System.out.print(timetable[i] + " ");
        }
    }

    public int getNumberOfClasses() {
        return numberOfClasses;
    }

    public int getNumberOfStudents() {
        return numberOfStudents;
    }

    public int[] getTimetable() {
        return timetable;
    }

    public int getSize() {
        return size;
    }

    public float getFitness() {
        return fitness;
    }
}
