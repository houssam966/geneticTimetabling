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
        initialiseTimetable();
//        Random random = new Random();
//        //generate a random timetable
//        for (int i = 0; i < size; i++) {
//            if(random.nextFloat() > 0.5){
//                timetable[i] = 1;
//            } else {
//                timetable[i] = 0;
//            }
//        }
    }

    private void initialiseTimetable(){
        int[] assigned = new int[classes.length];
        Manager manager = new Manager();
        for (int i = 0; i < numberOfStudents; i++) {
//            System.out.println("\n Student: " + i);
            Student student = students[i];
            for (int j = 0; j < student.modules.length; j++) {
                if(student.modules[j] == 0) continue;
                Module module = this.modules[j];
//                System.out.println("\nModule: " + module.name + "\nAssigned");
                if(module.hasLecture){
                    ArrayList<Integer> lectures = manager.getModuleClassesByType(j,"Lecture", this.classes);
                    for (int x = 0; x < lectures.size(); x++) {
                        int classNumber = lectures.get(x);
                        int index = classNumber * numberOfStudents + i;
                        timetable[index] = 1;
                        assigned[classNumber]++;
                    }
                }
                if(module.hasTutorial){
                    ArrayList<Integer> tutorials = manager.getModuleClassesByType(j,"Tutorial", this.classes);
                    //assign all available tutorials
                    for (int x = 0; x < tutorials.size(); x++) {
                        int classNumber = tutorials.get(x);
                        int index = classNumber * numberOfStudents + i;
                        timetable[index] = 1;
                        assigned[classNumber]++;
                    }
                }
            }
        }
        for (int i = 0; i < numberOfStudents; i++) {
            Student student = students[i];
            for (int j = 0; j < student.modules.length; j++) {
                if(student.modules[j] == 0) continue;
                Module module = this.modules[j];
                if(module.hasPractical){
                    ArrayList<Integer> practicals = manager.getModuleClassesByType(j,"Practical", this.classes);
                    Collections.shuffle(practicals);
                    for (int x = 0; x < practicals.size(); x++) {
                        int classNumber = practicals.get(x);
                        if(assigned[classNumber] < this.classes[classNumber].capacity  && !isClashing(classNumber, i)){
                            int index = classNumber * numberOfStudents + i;
                            timetable[index] = 1;
                            assigned[classNumber]++;
                            break;
                        }
                    }
                }
                if(module.hasSmallGroup){
                    ArrayList<Integer> smgs = manager.getModuleClassesByType(j,"Small Group", this.classes);
                    Collections.shuffle(smgs);
                    for (int x = 0; x < smgs.size(); x++) {
                        int classNumber = smgs.get(x);
                        if(assigned[classNumber] < this.classes[classNumber].capacity && !isClashing(classNumber, i)){
                            int index = classNumber * numberOfStudents + i;
                            timetable[index] = 1;
                            assigned[classNumber]++;
                            break;
                        }
                    }
                }
            }
        }
//        System.out.println();
//        for (int i = 0; i < timetable.length; i++) {
//            if(i%300 == 0) System.out.println();
//            System.out.print(timetable[i] + ", ");
//        }
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
        int[] tutCount = new int[numberOfStudents * numberOfModules];
        int[] practCount = new int[numberOfStudents * numberOfModules];
        int[] smgCount = new int[numberOfStudents * numberOfModules];
        int[] lecCount = new int[numberOfStudents * numberOfModules];

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
            if (type.equals("Practical")) {
                practCount[moduleStudentIndex] += assigned;
            } else if(type.equals("Tutorial")) {
                tutCount[moduleStudentIndex] += assigned;
            } else if(type.equals("Small Group")) {
                smgCount[moduleStudentIndex] += assigned;
            } else if(type.equals("Lecture")) {
                lecCount[moduleStudentIndex] += assigned;
            }

            classPreferenceTotal += preferences[i]*assigned;
        }

        //check allocations of students to practical and tutorials (first two constraints)
        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            Module module = modules[moduleNumber];
            int assignedTuts = tutCount[i];
            int assignedPracs = practCount[i];
            int assignedSmgs =  smgCount[i];
            int assignedLecs =  lecCount[i];

            if(students[studentNumber].modules[moduleNumber] == 1){
                //student takes module
                if(module.hasTutorial){
                    if(assignedTuts < module.numOfLectures) missingAllocations+= module.numOfTutorials - assignedTuts;
                    else if(assignedTuts > module.numOfLectures) extraAllocations+= assignedTuts - module.numOfTutorials;
                }
                if(module.hasPractical){
                    if(assignedPracs <= 0) missingAllocations++;
                    else if(assignedPracs > 1) extraAllocations+= assignedPracs-1;
                }
                if(module.hasSmallGroup){
                    if(assignedSmgs <= 0) missingAllocations++;
                    else if(assignedSmgs > 1) extraAllocations+= assignedSmgs-1;
                }
                if(module.hasLecture){
                    if(assignedLecs < module.numOfLectures) missingAllocations+= module.numOfLectures - assignedLecs;
                    else if(assignedLecs > module.numOfLectures) extraAllocations+= assignedLecs - module.numOfLectures;
                }
            } else {
                incorrectAllocations+= assignedPracs + assignedTuts + assignedSmgs + assignedLecs;
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
        //(float) Math.pow((double) hardConstraintsScore ,2)
        fitness =  - (float) Math.pow((double) hardConstraintsScore ,2) + 0.5f*classPreferenceTotal;
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
        int midpoint = r.nextInt(numberOfStudents); // Pick a midpoint

        // Half from one, half from the other
        for (int i = 0; i < timetable.length; i++) {
            int studentNumber = i % numberOfStudents;
            if (studentNumber > midpoint) child.timetable[i] = timetable[i];
            else child.timetable[i] = partner.timetable[i];
        }
        return child;
    }

    public void mutate(float mutationRate) {
        Random r = new Random();
        if(r.nextFloat() > mutationRate) return;
        //for all genes, pick a random number, if it's less than mutation rate, flip bits
        for (int i = 0; i < timetable.length; i++) {
            if(timetable[i] == 0 || r.nextFloat() > mutationRate) continue;
            int studentNumber = i % numberOfStudents;
            int classNumber = i / numberOfStudents;
            int moduleNumber = classes[classNumber].moduleIndex;

            ArrayList<Integer> similarClasses = new ArrayList<>();
            switch (classes[classNumber].type){
                case "Practical":{
                    similarClasses=modules[moduleNumber].practicals;
                    break;
                }
                case "Lecture":{
                    similarClasses=modules[moduleNumber].lectures;
                    break;
                }
                case "Tutorial":{
                    similarClasses=modules[moduleNumber].tutorials;
                    break;
                }
                case "Small Group":{
                    similarClasses=modules[moduleNumber].smgs;
                    break;
                }
            }
            Collections.shuffle(similarClasses);
            int newIndex = similarClasses.get(0)*numberOfStudents + studentNumber;
            timetable[i] = 0;
            timetable[newIndex] = 1;
//            if(r.nextFloat() < mutationRate){
//                if(timetable[i] == 0) timetable[i] = 1;
//                else if(timetable[i] == 1) timetable[i] = 0;
//            }
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
        if(r.nextFloat() > adjustmentRate) return;

        if(violations[1] > 0) removeExtraAllocations();
        if(violations[3] > 0) removeIncorrectAllocations();
        if(violations[0] > 0) removeClashes();
        if(violations[4] > 0 ) removeExceedingLimit();
        if(violations[2] > 0) addMissing();
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
        Evaluator evaluator = new Evaluator();
        Properties properties = evaluator.getProperties(this);
        int[] studentsPerClass = properties.studentsPerClass;
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
                            if (classes[preferredClass].type.equals(classes[classNumber].type)
                                    && studentsPerClass[preferredClass] < classes[preferredClass].capacity
                                    && !isClashing(preferredClass,i) ){
                                int index = preferredClass * numberOfStudents + i;
                                timetable[index] = 1;
                                timetable[j] = 0;
                                studentsPerClass[classNumber]--;
                                studentsPerClass[preferredClass]++;
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
        int[] tutCount = properties.tutorialCount;
        int[] practCount = properties.practicalCount;
        int[] smgCount = properties.smgCount;
        int[] lecCount = properties.lecCount;
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
            int assignedTuts = tutCount[i];
            int assignedPracts = practCount[i];
            int assignedSmgs = smgCount[i];
            int assignedLecs = lecCount[i];
            Module module = modules[moduleNumber];

            if(students[studentNumber].modules[moduleNumber] == 1) {
                //if student takes module
                if(assignedPracts > 1){
                    //TODO: keep the best and the one that does not clash
                    int removed = 0;
                    for( int j=studentNumber; j < timetable.length; j+= numberOfStudents){
                        int classNumber = j / numberOfStudents;
                        if(classes[classNumber].moduleIndex == moduleNumber && classes[classNumber].type.equals("Practical") && timetable[j] == 1){
                            int index = classNumber * numberOfStudents + studentNumber;
                            timetable[index] = 0;
                            studentsPerClass[classNumber]--;
                            practCount[i]--;
                            removed++;
                        }
                        if(removed == assignedPracts-1) break;
                    }
                }
//                if(assignedTuts > 1){
//                    int removed = 0;
//                    for( int j=studentNumber; j < timetable.length; j+= numberOfStudents){
//                        int classNumber = j / numberOfStudents;
//                        if(classes[classNumber].moduleIndex == moduleNumber && classes[classNumber].type.equals("Tutorial") && timetable[j] == 1){
//                            int index = classNumber * numberOfStudents + studentNumber;
//                            timetable[index] = 0;
//                            studentsPerClass[classNumber]--;
//                            tutCount[i]--;
//                            removed++;
//                        }
//                        if(removed == assignedTuts-1) break;
//                    }
//                }
                if(assignedSmgs > 1){
                    int removed = 0;
                    for( int j=studentNumber; j < timetable.length; j+= numberOfStudents){
                        int classNumber = j / numberOfStudents;
                        if(classes[classNumber].moduleIndex == moduleNumber && classes[classNumber].type.equals("Small Group") && timetable[j] == 1){
                            int index = classNumber * numberOfStudents + studentNumber;
                            timetable[index] = 0;
                            studentsPerClass[classNumber]--;
                            smgCount[i]--;
                            removed++;
                        }
                        if(removed == assignedSmgs-1) break;
                    }
                }
//                if(assignedLecs > 1){
//                    int removed = 0;
//                    for( int j=studentNumber; j < timetable.length; j+= numberOfStudents){
//                        int classNumber = j / numberOfStudents;
//                        if(classes[classNumber].moduleIndex == moduleNumber && classes[classNumber].type.equals("Lecture") && timetable[j] == 1){
//                            int index = classNumber * numberOfStudents + studentNumber;
//                            timetable[index] = 0;
//                            studentsPerClass[classNumber]--;
//                            lecCount[i]--;
//                            removed++;
//                        }
//                        if(removed == assignedLecs-1) break;
//                    }
//                }
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
        Manager manager = new Manager();
        Evaluator evaluator = new Evaluator();
        Properties properties = evaluator.getProperties(this);
        int[] tutCount = properties.tutorialCount;
        int[] practCount = properties.practicalCount;
        int[] smgCount = properties.smgCount;
        int[] lecCount = properties.lecCount;
        int[] studentsPerClass = properties.studentsPerClass;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int assignedTuts = tutCount[i];
            int assignedPracts = practCount[i];
            int assignedSmgs = smgCount[i];
            int assignedLecs = lecCount[i];
            Module module = modules[moduleNumber];
            if (students[studentNumber].modules[moduleNumber] == 1 && module.hasPractical && assignedPracts <= 0) {
                int bestAllocation = getBestAllocation(studentNumber, moduleNumber, "Practical", studentsPerClass);
                if(bestAllocation!= -1){
                    int index = bestAllocation * numberOfStudents + studentNumber;
                    studentsPerClass[bestAllocation]++;
                    timetable[index] = 1;
                }
            }
            if (students[studentNumber].modules[moduleNumber] == 1 && module.hasTutorial && assignedTuts < module.numOfTutorials) {
                ArrayList<Integer> tutorials = manager.getModuleClassesByType(moduleNumber,"Tutorial", this.classes);
                //assign all available tutorials
                for (int x = 0; x < tutorials.size(); x++) {
                    int classNumber = tutorials.get(x);
                    int index = classNumber * numberOfStudents + studentNumber;
                    if(timetable[index] == 0){
                        timetable[index] = 1;
                        studentsPerClass[classNumber]++;
                    }
                }
                //                int bestAllocation = getBestAllocation(studentNumber, moduleNumber, "Tutorial", studentsPerClass);
//                if(bestAllocation!= -1){
//                    int index = bestAllocation * numberOfStudents + studentNumber;
//                    studentsPerClass[bestAllocation]++;
//                    timetable[index] = 1;
//                }
            }
            if (students[studentNumber].modules[moduleNumber] == 1 && module.hasSmallGroup && assignedSmgs <= 0) {
                int bestAllocation = getBestAllocation(studentNumber, moduleNumber, "Small Group", studentsPerClass);
                if(bestAllocation!= -1){
                    int index = bestAllocation * numberOfStudents + studentNumber;
                    studentsPerClass[bestAllocation]++;
                    timetable[index] = 1;
                }
            }
            if (students[studentNumber].modules[moduleNumber] == 1 && module.hasLecture && assignedLecs < module.numOfLectures) {
                ArrayList<Integer> lectures = manager.getModuleClassesByType(moduleNumber,"Lecture", this.classes);
                //assign all available lectures
                for (int x = 0; x < lectures.size(); x++) {
                    int classNumber = lectures.get(x);
                    int index = classNumber * numberOfStudents + studentNumber;
                    if(timetable[index] == 0){
                        timetable[index] = 1;
                        studentsPerClass[classNumber]++;
                    }
                }
//                int bestAllocation = getBestAllocation(studentNumber, moduleNumber, "Lecture", studentsPerClass);
//                if(bestAllocation!= -1){
//                    int index = bestAllocation * numberOfStudents + studentNumber;
//                    studentsPerClass[bestAllocation]++;
//                    timetable[index] = 1;
//                }
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
