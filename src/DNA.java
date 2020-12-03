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
        updateFitness();
    }

    /**
     * Hard Constraints:
     * students should only be allocated to classes that they have
     *
     */
    void updateFitness(){
        int[] studentsPerClass = new int[numberOfClasses];
        int[] tutorialCount = new int[numberOfStudents*numberOfModules];
        int[] practicalCount = new int[numberOfStudents*numberOfModules];

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

            fitness += preferences[i]*assigned*0.02; //soft constraints(day, time, TA)
//
//            if(assigned == 1){
//
//                studentsPerClass[classNumber]++;
//
//
//
//                if(students[studentNumber].required[classNumber] == 1){
//                    //assigned and required
//                    //TODO increase fitness
//                } else{
//                    //assigned and not required
//                    //TODO decrease fitness
//                }
//
//            } else {
//                if(students[studentNumber].required[classNumber] == 1){
//                    //not assigned and required
//                    //TODO decrease fitness
//                } else{
//                    //not assigned and not required
//                    //TODO increase fitness
//                }
//            }

        }

        //check allocations of students to practical and tutorials (first two constraints)
        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            Module module = modules[moduleNumber];
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];
            if(students[studentNumber].modules[moduleNumber] == 1){
                if(module.hasTutorial){
                fitness += (float) 1/(1+ Math.abs(numberOfAssignedTutorials-1));
                }
                if(module.hasPractical){
                    fitness += (float) 1/(1+ Math.abs(numberOfAssignedPracticals-1));
                }
            } else {
                fitness+= 1 - numberOfAssignedPracticals - numberOfAssignedTutorials; //if tutCount & pracCount = 0  fitness will be max (1)
            }
        }

        //check number of students per class
        for (int i = 0; i < studentsPerClass.length; i++) {
            int students = studentsPerClass[i];
            if(students>20){
                fitness+= (students-20)* 0.2; //penalty
            } else {
                fitness+= 0.5; //reward
            }
        }

        //check clashes
        for (int i = 0; i < clashes.length; i++) {
            Pair pair = clashes[i];
            if(timetable[pair.first] == 1 & timetable[pair.second] == 1){
                //penalty (there is a clash)
                fitness-=0.2;
            } else {
                //reward (no clashes)
                fitness+=0.02;
            }
        }

//        int score = 0;
//        for (int i = 0; i < genes.length; i++) {
//            if(genes[i]== target.charAt(i)){
//                score++;
//            }
//        }
//        this.fitness = (float) pow(2, score);
    }

    // Crossover
    public DNA crossover(DNA partner) {
        // A new child
        DNA child = new DNA(numberOfStudents, numberOfClasses, numberOfModules, students, classes, modules, clashes, preferences);
        Random r = new Random();
        int midpoint = r.nextInt(timetable.length); // Pick a midpoint

        // Half from one, half from the other
        for (int i = 0; i < timetable.length; i++) {
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

    public float getFitness() {
        return fitness;
    }
}
