import java.util.ArrayList;
import java.util.Random;


public class Population {
    float mutationRate;           // Mutation rate
    DNA[] population;             // Array to hold the current population
    ArrayList<DNA> matingPool;    // ArrayList which we will use for our "mating pool"
    int generations;              // Number of generations
    boolean finished;             // Are we finished evolving?
    Student[] students;
    Tutorial[] classes;
    int maxGenerations;
    public static void main(String[] args){
        long start = System.currentTimeMillis();
        int popmax = 5;
        int maxGenerations = 100;
        float mutationRate = 0.01f;

        /**
         * class 0: 0,2,4,5
         * class 1: 0,1,2,3,5
         * class 2: 0,1,2,3,4
         * class 3: 1,3,4,5
         */
        Student[] students = new Student[]{
                new Student( new int[]{0,1,2}),
                new Student( new int[]{1,2,3}),
                new Student( new int[]{0,1,2}),
                new Student( new int[]{1,2,3}),
                new Student( new int[]{0,2,3}),
                new Student( new int[]{0,1,3})
        };

        Tutorial[] classes = new Tutorial[]{
                new Tutorial(0, "HCI"),
                new Tutorial(1, "AIN"),
                new Tutorial(2, "VER"),
                new Tutorial(3, "AIP"),
        };

        //maybe the order of operators will depend on how close we are to the optimal solution
        //so if we are close (high average fitness) reduce the probability of mutation
        // if all fitnesses look fairly similar, increase mutation so we dont get stuck
        //make all these variables dynamic

        Population population = new Population(students, classes, mutationRate, popmax, maxGenerations);
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
        System.out.println("Time taken " + (end - start) + "ms");
        population.print();
    }

    public Population(Student[] students, Tutorial[] classes, float mutationRate, int populationSize, int maxGenerations){
        this.mutationRate = mutationRate;
        population = new DNA[populationSize];
        this.students = students;
        this.classes = classes;
        matingPool = new ArrayList<>();
        finished = false;
        generations = 0;
        this.maxGenerations = maxGenerations;
    }

    void initialize(){
        for (int i = 0; i < population.length; i++) {
            population[i] = new DNA(students.length, classes.length, students);
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
        float maxFitness = 0;
        for (int i = 0; i < population.length; i++) {
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

    float getAverageFitness(){
        float total = 0;
        for (int i = 0; i < population.length; i++) {
            total+= population[i].getFitness();
        }
        return (total/population.length);
    }
}
