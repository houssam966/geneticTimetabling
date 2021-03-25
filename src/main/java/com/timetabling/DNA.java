package com.timetabling;

import java.util.*;

public class DNA implements Comparable<DNA>{
    private float fitness;
    int[] timetable;
    int numberOfStudents, numberOfClasses, numberOfModules, size;
    Student[] students;
    Activity[] classes;
    Module[] modules;
    Pair[] clashes;
    int[] violations;
    float[] preferences;
    HashMap<Integer, ArrayList<Integer>> clashMap;
    private Map<DNA, Float> fitnessHash = Collections. synchronizedMap( new LinkedHashMap<DNA, Float>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<DNA, Float> eldest) {
                    // Store a maximum of 1000 fitness values
                    return this.size() > 1000; }
            });
    public DNA(int numberOfStudents, int numberOfClasses, int numberOfModules, Student[] students, Activity[] classes, Module[] modules, Pair[] clashes,  HashMap<Integer, ArrayList<Integer>> clashMap, float[] preferences){
        this.numberOfClasses = numberOfClasses;
        this.numberOfStudents = numberOfStudents;
        this.numberOfModules = numberOfModules;
        this.students = students;
        this.classes = classes;
        this.modules = modules;
        this.clashes = clashes;
        this.clashMap = clashMap;
        this.preferences = preferences;
        violations = new int[5];

        size = numberOfClasses * numberOfStudents;
        timetable = new int[size];
        Random random = new Random();
        //generate a random timetable
        for (int i = 0; i < size; i++) {
            if(random.nextFloat() > 0.5){
                timetable[i] = 1;
            } else {
                timetable[i] = 0;
            }
        }
    }

    int[] getStudentsPerClass(){
        int[] studentsPerClass = new int[numberOfClasses];
        for (int i = 0; i < timetable.length; i++) {
            int assigned = timetable[i];
            int classNumber = i / numberOfStudents;
            studentsPerClass[classNumber]+= assigned; //if assigned would add 1, otherwise add 0
        }
        return  studentsPerClass;
    }
    /**
     * Hard Constraints:
     * students should only be allocated to classes that they have
     *
     */
    void updateFitness(Weights w){
        Float storedFitness = this.fitnessHash.get(this);
        if (storedFitness != null) {
            fitness = storedFitness;
        }

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
            if(students>limit){
                overAllocations+= (students-limit);
            }
        }

        //check clashes
        for (int i = 0; i < clashes.length; i++) {
            Pair pair = clashes[i];
            if(timetable[pair.first] == 1 && timetable[pair.second] == 1)
                numberOfClashes++;
        }
//        fitness = 1f/(1+((w.clash*numberOfClashes + w.extra*extraAllocations + w.missing*missingAllocations
//                + w.incorrect*incorrectAllocations + w.over*overAllocations))) + 0.2f*(classPreferenceTotal/(numberOfStudents*numberOfClasses));
//        fitness = 1/(1+numberOfClashes + missingAllocations + extraAllocations);
//        fitness = 1/ (1+ (w.clash*numberOfClashes + w.extra*extraAllocations + w.missing*missingAllocations + w.incorrect*incorrectAllocations + w.over*overAllocations))+ (classPreferenceTotal/(numberOfStudents*numberOfClasses));
        violations[0] = numberOfClashes;
        violations[1] = extraAllocations;
        violations[2] = missingAllocations;
        violations[3] = incorrectAllocations;
        violations[4] = overAllocations;
        float hardConstraintsScore = w.clash*numberOfClashes + w.extra*extraAllocations + w.missing*missingAllocations + w.incorrect*incorrectAllocations + w.over*overAllocations;
//        float hardConstraintsScore = numberOfClashes + extraAllocations + missingAllocations + incorrectAllocations + overAllocations;
        fitness =  -(float) Math.pow((double) hardConstraintsScore ,2)+ classPreferenceTotal;
//        fitness =  1000 - 5* hardConstraintsScore + classPreferenceTotal;
//        fitness =  1/(hardConstraintsScore+1);
//
// int numberOfViolatedConstraints = numberOfClashes + extraAllocations + missingAllocations + incorrectAllocations + overAllocations;
//        fitness = 1f/(float) Math.pow((1+numberOfViolatedConstraints),2);
//        fitness = 1f/(1+numberOfViolatedConstraints);
        // Store fitness in hashtable
        this.fitnessHash.put(this, fitness);
    }

    // Crossover
    public DNA crossover(DNA partner) {
        // A new child
        DNA child = new DNA(numberOfStudents, numberOfClasses, numberOfModules, students, classes, modules, clashes, clashMap, preferences);
        Random r = new Random();
//        int midpoint = r.nextInt(timetable.length); // Pick a midpoint
        int midpoint = r.nextInt(numberOfStudents); // Pick a midpoint

        // Half from one, half from the other
        for (int i = 0; i < timetable.length; i++) {
            // Use half of parent1's genes and half of parent2's genes
//            if (0.7 > Math.random()) {
//                child.timetable[i] = timetable[i];
//            } else {
//                child.timetable[i] = partner.timetable[i];
//            }
            int studentNumber = i % numberOfStudents;
//            this method is much better as it deals with chunks
            if (studentNumber > midpoint) child.timetable[i] = timetable[i];
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
    boolean isClashing(int classNumber, int studentNumber){
        if(clashMap.containsKey(classNumber)){
            ArrayList<Integer> clashingClasses = clashMap.get(classNumber);
            for (int i = 0; i < clashingClasses.size(); i++) {
                int index = clashingClasses.get(i)*numberOfStudents + studentNumber;
                if(timetable[index] == 1){
                    return true;
                }

            }
        }
        return false;

//        //check if it clashes with any allocation
//        for (int i = 0; i < clashes.length; i++) {
//            Pair pair = clashes[i];
//            if(pair.first/numberOfStudents == classNumber){
//                if(timetable[pair.second] == 1){
//                    //there is a clash
//                    isClashing = true;
//                    break;
//                }
//
//            } else if(pair.second/numberOfStudents == i){
//                if(timetable[pair.first] == 1){
//                    //there is a clash
//                    isClashing = true;
//                    break;
//                }
//            }
//        }
//        return isClashing;
    }

    /**
     * violations[0] = numberOfClashes;
     * violations[1] = extraAllocations;
     * violations[2] = missingAllocations;
     * violations[3] = incorrectAllocations;
     * violations[4] = overAllocations;
     * @param adjustmentRate
     */
    void improve(float adjustmentRate) {
        Random r = new Random();

        if(r.nextFloat() > adjustmentRate) {
            return;
        }

        if(violations[1] > 0){
            removeExtraAllocations();
        }
        if(violations[3] > 0){
            removeIncorrectAllocations();
        }
        if(violations[0] > 0){
            removeClashes();
        }
        if(violations[4] > 0 ){
            removeExceedingLimit();
        }
        if(violations[2] > 0){
            addMissing();
        }

//

//
//        Evaluator evaluator = new Evaluator();
//        Properties properties = evaluator.getProperties(this);
//        int[] tutorialCount = properties.tutorialCount;
//        int[] practicalCount = properties.practicalCount;
//        int[] studentsPerClass = properties.studentsPerClass;
//
//
//        for (int i = 0; i < numberOfStudents; i++) {
//            for( int j=i; j < timetable.length; j+= numberOfStudents){
//                //all classes for student i
//                int classNumber = j / numberOfStudents;
//                int moduleNumber = classes[classNumber].moduleIndex;
//                //remove extra allocations
//                if(students[i].required[classNumber] == 0 && timetable[j] == 1){
//                    timetable[j] = 0;
//                    studentsPerClass[classNumber]--;
//                }
//                //remove incorrect allocations
//                if(students[i].modules[moduleNumber] == 0 && timetable[j] == 1){
//                    timetable[j] = 0;
//                    studentsPerClass[classNumber]--;
//                }
//                ArrayList<Integer> preferred = students[i].preferredClasses[moduleNumber];
////                if(students[i].required[classNumber] == 1 && timetable[j] == 1){
////                    if(!preferred.contains(classNumber)){
////                        timetable[j] = 0;
////                        int index = preferred.get(r.nextInt(preferred.size())) * numberOfStudents + i;
////                        timetable[index] = 1;
////                    }
////                }
//            }
//        }
//        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
//            int studentNumber = i%numberOfStudents;
//            int moduleNumber = i/numberOfStudents;
//            int numberOfAssignedTutorials = tutorialCount[i];
//            int numberOfAssignedPracticals = practicalCount[i];
//            Module module = modules[moduleNumber];
//
//            if(students[studentNumber].modules[moduleNumber] == 1) {
//                //if student takes module
//                if(numberOfAssignedPracticals > 1){
//                    //TODO: keep the best and the one that does not clash
//                    int removed = 0;
//                    for( int j=studentNumber; j < timetable.length; j+= numberOfStudents){
//                        int classNumber = j / numberOfStudents;
//                        if(classes[classNumber].moduleIndex == moduleNumber && classes[classNumber].type.equals("Practical")){
//                            int index = classNumber * numberOfStudents + studentNumber;
//                            timetable[index] = 0;
//                            studentsPerClass[classNumber]--;
//                            practicalCount[i]--;
//                            removed++;
//                        }
//                        if(removed == numberOfAssignedPracticals-1) break;
//                    }
//                }
//                if(numberOfAssignedTutorials > 1){
//                    int removed = 0;
//                    for( int j=studentNumber; j < timetable.length; j+= numberOfStudents){
//                        int classNumber = j / numberOfStudents;
//                        if(classes[classNumber].moduleIndex == moduleNumber && classes[classNumber].type.equals("Tutorial")){
//                            int index = classNumber * numberOfStudents + studentNumber;
//                            timetable[index] = 0;
//                            studentsPerClass[classNumber]--;
//                            tutorialCount[i]--;
//                            removed++;
//                        }
//                        if(removed == numberOfAssignedTutorials-1) break;
//                    }
//                }
//                if (module.hasPractical && numberOfAssignedPracticals <= 0) {
//                    float max = -Float.MAX_VALUE;
//                    int bestAllocation = -1;
//                    for (int i1 = 0; i1 < classes.length; i1++) {
//                        if (classes[i1].moduleIndex == moduleNumber && classes[i1].type.equals("Practical")) {
//                            int index = i1 * numberOfStudents + studentNumber;
//                            boolean isClashing = isClashing(i1, studentNumber);
//                            if(!isClashing && preferences[index] > max && studentsPerClass[i1]< classes[i1].capacity){
//                                max = preferences[index];
//                                bestAllocation = i1;
//                            }
//                        }
//                    }
//                    if(bestAllocation!= -1){
//                        int index = bestAllocation * numberOfStudents + studentNumber;
//                        studentsPerClass[bestAllocation]++;
//                        timetable[index] = 1;
//                    }
//                }
//                if (module.hasTutorial && numberOfAssignedTutorials <= 0) {
//                    float max = -Float.MAX_VALUE;
//                    int bestAllocation = -1;
//                    for (int i1 = 0; i1 < classes.length; i1++) {
//                        if (classes[i1].moduleIndex == moduleNumber && classes[i1].type.equals("Tutorial")) {
//                            int index = i1 * numberOfStudents + studentNumber;
//                            boolean isClashing = isClashing(i1, studentNumber);
//                            if(!isClashing && preferences[index] > max && studentsPerClass[i1]< classes[i1].capacity){
//                                max = preferences[index];
//                                bestAllocation = i1;
//                            }
//                        }
//                    }
//                    if(bestAllocation!= -1){
//                        int index = bestAllocation * numberOfStudents + studentNumber;
//                        studentsPerClass[bestAllocation]++;
//                        timetable[index] = 1;
//                    }
//                }
//            }
//        }
//
//        for (int i = 0; i < clashes.length; i++) {
//            Pair pair = clashes[i];
//            if(timetable[pair.first] == 1 && timetable[pair.second] == 1){
//                //there is a clash
//                Random random = new Random();
//                if(random.nextFloat() > 0.5){
//                    timetable[pair.first] = 0;
//
//                }
//                else{
//                    timetable[pair.second] = 0;
//                }
//            }
//        }
//    }
    }

    public void removeClashes(){
        for (int i = 0; i < clashes.length; i++) {
            Pair pair = clashes[i];
            if(timetable[pair.first] == 1 && timetable[pair.second] == 1){
                //there is a clash
                Random random = new Random();
                if(random.nextFloat() > 0.5){
                    timetable[pair.first] = 0;

                }
                else{
                    timetable[pair.second] = 0;
                }
            }
        }
    }
    HashMap<Integer, ArrayList<Integer>> getSimilarClasses(){
        HashMap<Integer, ArrayList<Integer>> similarClasses = new HashMap<>();
        for (int i = 0; i < classes.length; i++) {

            for (int j = 0; j < classes.length; j++) {
                if(j==i) continue;
                if(classes[i].type.equals(classes[j].type) && classes[i].moduleIndex == classes[j].moduleIndex){
                    if(similarClasses.containsKey(i)){
                        ArrayList<Integer> similar = similarClasses.get(i);
                        similar.add(j);
                        similarClasses.put(i,similar);
                    } else{
                        ArrayList<Integer> similar = new ArrayList<>();
                        similar.add(j);
                        similarClasses.put(i,similar);
                    }
                }
            }
        }
        return  similarClasses;
    }
    void removeExceedingLimit(){
        Evaluator evaluator = new Evaluator();
        Properties properties = evaluator.getProperties(this);
        int[] studentsPerClass = properties.studentsPerClass;
//        System.out.println("=============================");
//        System.out.println("Before: ");
//        for (int i = 0; i < studentsPerClass.length; i++) {
//            System.out.print(studentsPerClass[i] + ", ");
//        }
//        System.out.println();
//
        HashMap<Integer, ArrayList<Integer>> similarClasses = getSimilarClasses();
        for (int i = 0; i < classes.length; i++) {
            int extra = studentsPerClass[i] - classes[i].capacity;
            if(extra <=0 ) continue;
            //exceeds capacity
            ArrayList<Integer> similar = similarClasses.get(i);
            for (int j = 0; j < similar.size(); j++) {
                int similarClass = similar.get(j);
                int spots = classes[similarClass].capacity - studentsPerClass[similarClass];
                int currentStudent = 0;
                while (spots > 0 && extra > 0 && currentStudent < students.length){
                    //class that is exceeding capacity is i
                    //class that is similar and empty is j
                    int indexToRemove = i*numberOfStudents + currentStudent;
                    int indexToAdd = similarClass*numberOfStudents + currentStudent;
                    if(timetable[indexToRemove] == 1 && timetable[indexToAdd] == 0){
                        //was already 1 so we can remove
                        timetable[indexToRemove] = 0;
                        extra--;
                        studentsPerClass[i]--;
                        timetable[indexToAdd] = 1;
                        spots--;
                        studentsPerClass[similarClass]++;
                    }
                    currentStudent++;
                }
                if(extra <=0 ) break;
            }
        }
//        for (int i = 0; i < studentsPerClass.length; i++) {
//            int limit = classes[i].capacity;
//            if (studentsPerClass[i] > limit){
//                //class exceeds the limit
//                //find similar classes and how much space they have
//                //HashMap<Integer, Integer> similarClasses = new HashMap<>();
//                ArrayList<Integer> similarClasses = new ArrayList<>();
//                for (int j = 0; j < classes.length; j++) {
//                    if(j== i) continue;
//                    if(classes[j].moduleIndex == classes[i].moduleIndex && classes[j].type.equals(classes[i].type)){
////                        similarClasses.put(j,classes[j].capacity - studentsPerClass[j]);
//                        similarClasses.add(j);
//                    }
//                }
//                for (int j = 0; j < similarClasses.size() && studentsPerClass[i] > limit ; j++) {
//                    int current = similarClasses.get(j);
//                    int spots = classes[current].capacity - studentsPerClass[current];
//                    while(spots > 0 && studentsPerClass[i] > limit){
//                        for(int x = i*numberOfStudents; x < i*numberOfStudents + numberOfStudents; x++){
//                            int studentNumber = x%numberOfStudents;
//                            int indexToAdd = current*numberOfStudents + studentNumber;
//                            if(timetable[indexToAdd] == 0 && timetable[x] == 1){
//                                timetable[indexToAdd] = 1;
//                                timetable[x] = 0;
//                                spots--;
//                                studentsPerClass[current]++;
//                                studentsPerClass[i]--;
//                                if(studentsPerClass[i] <= limit || spots <=0) break;
//                            }
//                        }
//                    }
//                }


//                for (int j = i*numberOfStudents; j < i*numberOfStudents + numberOfStudents; j++) {
//                    //all students for that class
////
////                    if(timetable[j] == 1){
////                        timetable[j] = 0;
////                        studentsPerClass[i]--;
////                    }
//
//                    int studentNumber = j%numberOfStudents;
//                    int moduleNumber = classes[i].moduleIndex;
//                    String type = classes[i].type;
//                    for (int x = 0; x < classes.length; x++) {
//                        if(classes[x].moduleIndex == moduleNumber && classes[x].type.equals(type)){
//                            //same class and same type
//                            //!isClashing(x,studentNumber) &&
//                            if(studentsPerClass[x] < classes[x].capacity){
//                                int indexToRemove = i*numberOfStudents + studentNumber;
//                                int indexToAdd = x*numberOfStudents + studentNumber;
//                                timetable[indexToAdd] = 1;
//                                timetable[indexToRemove] = 0;
//
//                                studentsPerClass[x]++;
//                                break;
//                            }
//                        }
//                    }
//                    if(studentsPerClass[i] <= limit){
//                        break;
//                    }
//                    //timetable[j];
                //}
           // }
       // }
//        int[] studentsPerClassAfter = evaluator.getProperties(this).studentsPerClass;
//        System.out.println("After: ");
//        for (int i = 0; i < studentsPerClass.length; i++) {
//            System.out.print(studentsPerClass[i] + ", ");
//        }
//        System.out.println();
//        System.out.println("Properties: ");
//        for (int i = 0; i < studentsPerClassAfter.length; i++) {
//            System.out.print(studentsPerClassAfter[i] + ", ");
//        }
//        System.out.println();
//        System.out.println("=============================");
    }
    void improveSoftConstraints(float improvingRate){
        Random r = new Random();
        if(r.nextFloat() > improvingRate) return;
        for (int i = 0; i < numberOfStudents; i++) {
            for( int j=i; j < timetable.length; j+= numberOfStudents){
                //all classes for student i
                int classNumber = j / numberOfStudents;
                int moduleNumber = classes[classNumber].moduleIndex;
                ArrayList<Integer> preferred = students[i].preferredClasses[moduleNumber];
                if(students[i].required[classNumber] == 1 && timetable[j] == 1){
                    if(!preferred.contains(classNumber)){
                        for (int x = 0; x < preferred.size(); x++) {
                            int preferredClass = preferred.get(x);
                            if (!isClashing(preferredClass,i)){
                                int index = preferred.get(x) * numberOfStudents + i;
                                timetable[index] = 1;
                                timetable[j] = 0;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    public void removeIncorrectAllocations(){
        for (int i = 0; i < numberOfStudents; i++) {
            for( int j=i; j < timetable.length; j+= numberOfStudents){
                //all classes for student i
                int classNumber = j / numberOfStudents;
                int moduleNumber = classes[classNumber].moduleIndex;
                //remove extra allocations
                if(students[i].required[classNumber] == 0 && timetable[j] == 1){
                    timetable[j] = 0;
                }
                //remove incorrect allocations
                if(students[i].modules[moduleNumber] == 0 && timetable[j] == 1){
                    timetable[j] = 0;
                }
                //ArrayList<Integer> preferred = students[i].preferredClasses[moduleNumber];
//                if(students[i].required[classNumber] == 1 && timetable[j] == 1){
//                    if(!preferred.contains(classNumber)){
//                        timetable[j] = 0;
//                        int index = preferred.get(r.nextInt(preferred.size())) * numberOfStudents + i;
//                        timetable[index] = 1;
//                    }
//                }
            }
        }
    }
    public void removeExtraAllocations() {
        Evaluator evaluator = new Evaluator();
        Properties properties = evaluator.getProperties(this);
        int[] tutorialCount = properties.tutorialCount;
        int[] practicalCount = properties.practicalCount;
        int[] studentsPerClass = properties.studentsPerClass;
//        System.out.println("=============================");
//        System.out.println("Before: ");
//        for (int i = 0; i < tutorialCount.length; i++) {
//            System.out.print(tutorialCount[i] + ", ");
//        }
//        System.out.println();
        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];
            Module module = modules[moduleNumber];

            if(students[studentNumber].modules[moduleNumber] == 1) {
                //if student takes module
                if(numberOfAssignedPracticals > 1){
                    //TODO: keep the best and the one that does not clash
                    int removed = 0;
                    for( int j=studentNumber; j < timetable.length; j+= numberOfStudents){
                        int classNumber = j / numberOfStudents;
                        if(classes[classNumber].moduleIndex == moduleNumber && classes[classNumber].type.equals("Practical") && timetable[j] == 1){
                            int index = classNumber * numberOfStudents + studentNumber;
                            timetable[index] = 0;
                            studentsPerClass[classNumber]--;
                            practicalCount[i]--;
                            removed++;
                        }
                        if(removed == numberOfAssignedPracticals-1) break;
                    }
                }
                if(numberOfAssignedTutorials > 1){
                    int removed = 0;
                    for( int j=studentNumber; j < timetable.length; j+= numberOfStudents){
                        int classNumber = j / numberOfStudents;
                        if(classes[classNumber].moduleIndex == moduleNumber && classes[classNumber].type.equals("Tutorial") && timetable[j] == 1){
                            int index = classNumber * numberOfStudents + studentNumber;
                            timetable[index] = 0;
                            studentsPerClass[classNumber]--;
                            tutorialCount[i]--;
                            removed++;
                        }
                        if(removed == numberOfAssignedTutorials-1) break;
                    }
                }
            }
        }
//        int[] practicalCountAfter = evaluator.getProperties(this).practicalCount;
//        System.out.println("After: ");
//        for (int i = 0; i < practicalCount.length; i++) {
//            System.out.print(practicalCount[i] + ", ");
//        }
//        System.out.println();
//        System.out.println("Properties: ");
//        for (int i = 0; i < practicalCountAfter.length; i++) {
//            System.out.print(practicalCountAfter[i] + ", ");
//        }
//        System.out.println();
//        System.out.println("=============================");
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

    public void addMissing(){
        Evaluator evaluator = new Evaluator();
        Properties properties = evaluator.getProperties(this);
        int[] tutorialCount = properties.tutorialCount;
        int[] practicalCount = properties.practicalCount;
        int[] studentsPerClass = properties.studentsPerClass;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];
            Module module = modules[moduleNumber];
            if (students[studentNumber].modules[moduleNumber] == 1 && module.hasPractical && numberOfAssignedPracticals <= 0) {
                int bestAllocation = getBestAllocation(studentNumber, moduleNumber, "Practical", studentsPerClass);
                if(bestAllocation!= -1){
                    int index = bestAllocation * numberOfStudents + studentNumber;
                    studentsPerClass[bestAllocation]++;
                    timetable[index] = 1;
                }
            }
            if (students[studentNumber].modules[moduleNumber] == 1 && module.hasTutorial && numberOfAssignedTutorials <= 0) {
                int bestAllocation = getBestAllocation(studentNumber, moduleNumber, "Tutorial", studentsPerClass);
                if(bestAllocation!= -1){
                    int index = bestAllocation * numberOfStudents + studentNumber;
                    studentsPerClass[bestAllocation]++;
                    timetable[index] = 1;
                }
            }
        }
    }

    int getBestAllocation(int studentNumber,int moduleNumber, String type, int[] studentsPerClass){
        float max = -Float.MAX_VALUE;
        int bestAllocation = -1;
        for (int i1 = 0; i1 < classes.length; i1++) {
            if (classes[i1].moduleIndex == moduleNumber && classes[i1].type.equals(type)) {
                int index = i1 * numberOfStudents + studentNumber;
                boolean isClashing = isClashing(i1, studentNumber);
                if(!isClashing && preferences[index] > max && studentsPerClass[i1]< classes[i1].capacity){
                    max = preferences[index];
                    bestAllocation = i1;
                }
            }
        }
        return bestAllocation;
    }

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


    @Override
    public int compareTo(DNA o) {
        return this.fitness > o.getFitness() ? 1 : this.fitness < o.getFitness() ? -1 : 0;

    }
    /**
     * Generates hash code based on individual's * chromosome
     *
     * @return Hash value
     */
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(this.timetable);
        return hash;
    }
    /**
     * Equates based on individual's chromosome *
     * @return Equality boolean
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false;
        }
        if (getClass() != obj.getClass()) { return false;
        }
        DNA individual = (DNA) obj;
        return Arrays.equals(this.timetable, individual.timetable);
    }
}
