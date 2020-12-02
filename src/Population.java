import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Population {
    float mutationRate;           // Mutation rate
    DNA[] population;             // Array to hold the current population
    ArrayList<DNA> matingPool;    // ArrayList which we will use for our "mating pool"
    int generations;              // Number of generations
    boolean finished;             // Are we finished evolving?
    Student[] students;
    Activity[] classes;
    Module[] modules;
    int maxGenerations;
    int[] solution;
    Pair[] clashes;

    public Population(Student[] students, Activity[] classes, Module[] modules, float mutationRate, int populationSize, int maxGenerations){
        this.mutationRate = mutationRate;
        population = new DNA[populationSize];
        this.students = students;
        this.classes = classes;
        this.modules = modules;


        matingPool = new ArrayList<>();
        finished = false;
        generations = 0;
        this.maxGenerations = maxGenerations;

    }

    void initialize(){
        Manager manager = new Manager();

        //assign required classes to students
        for (int i = 0; i < students.length; i++) {
            manager.assignClasses(students[i], classes);
        }

        //get clashes vector
       clashes =  manager.getClashes(classes,students,students.length);

        //TODO:assign preferences

        for (int i = 0; i < population.length; i++) {
            population[i] = new DNA(students.length, classes.length, modules.length ,students, classes, modules,  clashes);
        }

    }

    //Generate mating pool
    void calculateFitness(){
        for (int i = 0; i < population.length; i++) {
            population[i].updateFitness();
        }
    }

    void naturalSelection(){
        calculateFitness();
        // Clear the ArrayList
        matingPool.clear();
        float maxFitness =  population[0].getFitness();;
        for (int i = 1; i < population.length; i++) {
            if (population[i].getFitness() > maxFitness) {
                maxFitness = population[i].getFitness();
            }
        }

        // Based on fitness, each member will get added to the mating pool a certain number of times
        // a higher fitness = more entries to mating pool = more likely to be picked as a parent
        // a lower fitness = fewer entries to mating pool = less likely to be picked as a parent
        for (int i = 0; i < population.length; i++) {
            float fitness = population[i].getFitness()/maxFitness;
            int n = (int) fitness * 100;  // Arbitrary multiplier, we can also use monte carlo method
            for (int j = 0; j < n; j++) {              // and pick two random numbers
                matingPool.add(population[i]);
            }
        }
    }

    // Create a new generation
    void generate(){
        Random r = new Random();
        // Refill the population with children from the mating pool
        for (int i = 0; i < population.length; i++) {
            int a = r.nextInt(matingPool.size());
            int b = r.nextInt(matingPool.size());
            DNA partnerA = matingPool.get(a);
            DNA partnerB = matingPool.get(b);
            DNA child = partnerA.crossover(partnerB);
            child.mutate(mutationRate);
            population[i] = child;
        }
        generations++;
        for (int i = 0; i < population.length; i++) {
                if(Arrays.equals(population[i].timetable, solution)){
                    finished = true;
                }
        }
        if(generations == maxGenerations) finished = true;
    }

    void mutate(){
        for (int i = 0; i < population.length; i++) {
            population[i].mutate(mutationRate);
        }
    }

    void print(){
        for (int i = 0; i < population.length; i++) {
            population[i].print();
            System.out.println();
            System.out.println("Fitness: " + population[i].getFitness());
            System.out.println();
        }
    }

    float getMaxFitness(){
        float maxFitness = 0;
        for (int i = 0; i < population.length; i++) {
            if (population[i].getFitness() > maxFitness) {
                maxFitness = population[i].getFitness();
            }
        }
        return maxFitness;
    }

    DNA getFittest(){
        float maxFitness = population[0].getFitness();
        DNA fittest = population[0];
        for (int i = 1; i < population.length; i++) {
            if (population[i].getFitness() > maxFitness) {
                maxFitness = population[i].getFitness();
                fittest = population[i];
            }
        }
        return fittest;
    }


    float getAverageFitness(){
        float total = 0;
        for (int i = 0; i < population.length; i++) {
            total+= population[i].getFitness();
        }
        return (total/population.length);
    }

    public int getGenerations() {
        return generations;
    }

    public static void main(String[] args){
        long start = System.currentTimeMillis();
        int popmax = 50;
        int maxGenerations = 1000;
        float mutationRate = 0.01f;
        Manager manager = new Manager();

        Module[] modules = {
                new Module("HCI", true, true),
                new Module("AIP", true, true),
                new Module("AIN", true, true),
                new Module("VER", true, true),
        };

        Activity[] classes = {
                //MON
                new Activity("11:00:00","12:00:00", "Mon", "Tutorial", "Petr", 0), //HCI 0
                new Activity("11:00:00","13:00:00", "Mon", "Practical", "Amanda",1), //AIP 1
                new Activity("12:00:00","14:00:00", "Mon", "Practical", "Amanda",1), //AIP 2
                new Activity("17:00:00","18:00:00", "Mon", "Tutorial", "Mcburney",2), //AIN 3
                //TUE
                new Activity("09:00:00","11:00:00", "Tue", "Practical", "Petr",0), //HCI 4
                new Activity("11:00:00","12:00:00", "Tue", "Practical", "Mcburney",2), //AIN 5
                new Activity("12:00:00","13:00:00", "Tue", "Practical", "Mcburney",2), //AIN 6
                new Activity("13:00:00","14:00:00", "Tue", "Practical", "Mcburney",2), //AIN 7
                new Activity("15:00:00","16:00:00", "Tue", "Practical", "Mcburney",2), //AIN 8
                new Activity("16:00:00","18:00:00", "Tue", "Practical", "Hannah",3), //VER 9
                //WED
                new Activity("09:00:00","11:00:00", "Wed", "Practical", "Amanda",1), //AIP 10
                new Activity("11:00:00","13:00:00", "Wed", "Practical", "Amanda",1), //AIP 11
                new Activity("11:00:00","13:00:00", "Wed", "Practical", "Petr",0), //HCI 12
                //THU
                new Activity("10:00:00","12:00:00", "Thu", "Practical", "Hannah",3), //VER 13
                //FRI
                new Activity("14:00:00","16:00:00", "Fri", "Practical", "Petr",0), //HCI 14
        };

        Student[] students = {
                new Student(new int[]{1,1,1,0}, classes.length), // takes HCI, AIP and AIN
                new Student(new int[]{1,1,1,0}, classes.length), // takes HCI, AIP and AIN

                new Student(new int[]{0,1,1,1}, classes.length), // takes AIP and AIN, and VER
                new Student(new int[]{0,1,1,1}, classes.length), // takes AIP and AIN, and VER

                new Student(new int[]{1,0,1,1}, classes.length), // takes HCI, AIN and VER

                new Student(new int[]{1,1,0,1}, classes.length), // takes HCI, AIP and VER
        };


//        Student[] students = new Student[300];
//        for (int i = 0; i < 300; i++) {
//            if(i<100){
//                students[i] = new Student(new Module[]{modules[0], modules[1], modules[2]});
//            } else if( i<200){
//                students[i] = new Student(new Module[]{modules[1], modules[2], modules[3]});
//            } else{
//                students[i] = new Student(new Module[]{modules[0], modules[1], modules[3]});
//            }
//        }



        /**
         * TODO:
         * instead of computing the fitness for each student by going to the object and checking the time + size  etc etc
         * just find which combination of rows is closet to optimal for a given student
         * so encode them in terms of the 0's and 1s
         * one possible initial population is one where it satisfies all the soft constraints
         */
        /**
         * Students should be initialized with modules and each module has classes
         */
        /**
         * class 0: 0,2,4,5
         * class 1: 0,1,2,3,5
         * class 2: 0,1,2,3,4
         * class 3: 1,3,4,5
         */


        //maybe the order of operators will depend on how close we are to the optimal solution
        //so if we are close (high average fitness) reduce the probability of mutation
        // if all fitnesses look fairly similar, increase mutation so we dont get stuck
        //make all these variables dynamic

        Population population = new Population(students, classes, modules, mutationRate, popmax, maxGenerations);
        //initialize population
        population.initialize();
        population.calculateFitness();

        System.out.println("Initial Max Fitness = " + population.getMaxFitness());
        System.out.println("Initial Average Fitness = " + population.getAverageFitness());

        while(!population.finished){
            // Generate mating pool
            population.naturalSelection();
            //Create next generation
            population.generate();
            //Mutate
            population.mutate();
            // Calculate fitness
            population.calculateFitness();
        }

        long end = System.currentTimeMillis();
        System.out.println("Final Max Fitness = " + population.getMaxFitness());
        System.out.println("Final Average Fitness = " + population.getAverageFitness());
        System.out.println("Total Generations = " + population.getGenerations());
        System.out.println("Time taken " + (end - start) + "ms");
        System.out.println("===================================");
        System.out.println("Fittest Solution: ");
        population.getFittest().print();

        System.out.println("\n===================================");
//        population.getFittest().printConstraints();

        population.print();
    }
}
