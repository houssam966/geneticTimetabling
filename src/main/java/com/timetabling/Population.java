package com.timetabling;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

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
    Pair[] clashes;
    HashMap<Integer, ArrayList<Integer>> clashMap;
    float[] preferences;
    Weights weights;
    int tournamentSize;
    float crossoverRate, temperature, coolingRate, adjustmentRate, improvingRate;
    int elitismCount;
    // Create fitness hashtable


    public Population(Student[] students, Activity[] classes, Module[] modules, float mutationRate, float crossoverRate, int elitismCount, float temperature, float coolingRate, float adjustmentRate, float improvingRate, int populationSize, int maxGenerations, int tournamentSize, Weights weights){
        this.mutationRate = mutationRate;
        population = new DNA[populationSize];
        this.students = students;
        this.classes = classes;
        this.modules = modules;
        this.weights = weights;
        this.crossoverRate = crossoverRate;
        this.temperature = temperature;
        this.coolingRate = coolingRate;
        this.adjustmentRate = adjustmentRate;
        this.improvingRate = improvingRate;
        this.elitismCount = elitismCount;
        preferences = new float[students.length*classes.length];

        matingPool = new ArrayList<>();
        finished = false;
        generations = 0;
        this.maxGenerations = maxGenerations;
        this.tournamentSize = tournamentSize;

    }
    public Population(int populationSize){
        population = new DNA[populationSize];

        matingPool = new ArrayList<>();
        finished = false;
        generations = 0;

    }
    void initialize(){
        Manager manager = new Manager();
        ArrayList<Integer>[] modulePracticals = manager.getModulePracticals(modules, classes);
        ArrayList<Integer>[] moduleTutorials = manager.getModuleTutorials(modules, classes);
        for (int i = 0; i < students.length; i++) {
            Student student = students[i];
            manager.assignClasses(student, classes); //assign required classes to students
            ArrayList<Integer>[] allPreferredClasses = manager.getAllPreferredClasses(student,modules,modulePracticals,moduleTutorials, classes, weights);
            student.setPreferredClasses(allPreferredClasses);
        }

        //get clashes vector
        clashes =  manager.getClashes(classes,students,students.length);
        clashMap = manager.getClashMap(classes);
        preferences = manager.getStudentPreferences(students, classes, weights);


        for (int i = 0; i < population.length; i++) {
            population[i] = new DNA(students.length, classes.length, modules.length ,students, classes, modules, clashes, clashMap, preferences);
            population[i].updateFitness(weights);
        }
        Arrays.sort(population, Collections.reverseOrder());

    }

    //Generate mating pool
    void calculateFitness(){
        //parallel processing
        IntStream.range(0, population.length).parallel()
                .forEach(i -> population[i].updateFitness(weights));
        Arrays.sort(population, Collections.reverseOrder());

        //        for (int i = 0; i < population.length; i++) {
//            population[i].updateFitness(weights);
//        }

    }

    /**
     * Sorts individuals by fitness, helps with elitism count
     */
    void naturalSelection(){
        calculateFitness();
    }

    /**
     * Selects parent for crossover using tournament selection
     *
     * Tournament selection works by choosing N random individuals, and then
     * choosing the best of those.
     *
     * @return The individual selected as a parent
     */
    public DNA selectParent() {
        // Create tournament
        Population tournament = new Population(this.tournamentSize);

        // Add random individuals to the tournament
        Random rnd = new Random();
        for (int i = 0; i < this.tournamentSize; i++) {
            int index = rnd.nextInt(population.length);
            DNA tournamentIndividual = population[index];
            tournament.population[i] = tournamentIndividual;
        }

        // Return the best
        return tournament.getFittest();
    }

    // Create a new generation
    void generate(){
        // Create new population
        DNA[] newPopulation = new DNA[population.length];
        Arrays.sort(population, Collections.reverseOrder());
        for (int i = 0; i < population.length; i++) {
            newPopulation[i] = population[i];
        }
        int replaced = 1;
        for (int i = elitismCount; i < population.length; i++) {
            DNA partnerA = population[i];
//            int fitnessPosition = getFitnessPosition(i);
//            if(i>population.length -10){
//                Random random = new Random();
//
//                DNA eliteParent = population[random.nextInt(elitismCount)];
//                DNA child = partnerA.crossover(eliteParent);
//                newPopulation[i] = child;
//            }
            if (this.crossoverRate > Math.random()) {
                // Find second parent by tournament selection
                DNA partnerB = selectParent();
                DNA child = partnerA.crossover(partnerB);
                child.updateFitness(weights);
                newPopulation[population.length - replaced] = child;
                replaced++;
            }
//            else {
//                // Add individual to new population without applying crossover
//                newPopulation[i] = partnerA;
//            }
        }
        population = newPopulation; //replace current population with newly generated population
        generations++;
        if(generations == maxGenerations || getMaxFitness() == 1) finished = true;
//        if(getMaxFitness() == 1) finished = true;
    }

    //adaptive mutation

    void mutate(){
        Arrays.sort(population, Collections.reverseOrder());
        //skip mutation if this is an elite individual

        for (int i = elitismCount; i < population.length; i++) {
            float maxFitness = getMaxFitness();
            float averageFitness = getAverageFitness();
            DNA individual = population[i];
            //int fitnessPosition = getFitnessPosition(i);

            // Calculate adaptive mutation rate
            float adaptiveMutationRate = this.mutationRate;

//            if (individual.getFitness() > averageFitness) {
//                float fitnessDelta1 =  maxFitness - individual.getFitness();
//                float fitnessDelta2 = maxFitness - averageFitness;
//                adaptiveMutationRate = (fitnessDelta1 / fitnessDelta2) * this.mutationRate;
//            }

            population[i].mutate(adaptiveMutationRate);

        }
    }

//    //annealing mutation
//    void mutate(){
//        for (int i = 0; i < population.length; i++) {
//            int fitnessPosition = getFitnessPosition(i);
//            //skip mutation if this is an elite individual
//            if(fitnessPosition >= this.elitismCount){
//                // Calculate adaptive mutation rate
//                float annealingMutationRate = this.mutationRate * this.temperature;
//                population[i].mutate(annealingMutationRate);
//            }
//
//        }
//    }

    void coolTemperature(){
        this.temperature*= (1-this.coolingRate);
    }

    void improveAllocations(){
        // * this.temperature
        Arrays.sort(population, Collections.reverseOrder());
        AtomicReference<Float> adaptiveAdjustmentRate = new AtomicReference<>(this.adjustmentRate * this.temperature);
        IntStream.range(0, population.length).parallel()
                .forEach(i -> {
//                    DNA individual = population[i];
//                    float averageFitness = getAverageFitness();
//                    float maxFitness = getMaxFitness();

//                    if (individual.getFitness() > averageFitness) {
//                        float fitnessDelta1 =  maxFitness - individual.getFitness();
//                        float fitnessDelta2 = maxFitness - averageFitness;
//                        adaptiveAdjustmentRate.set((fitnessDelta1 / fitnessDelta2) * this.mutationRate);
//                    }
                    //skip mutation if this is an elite individual
                    //if(fitnessPosition < this.elitismCount) continue;
                    boolean elite =  i < this.elitismCount;
//                    if(elite) System.out.println(i + " Fitness: " + population[i].getFitness());
                    if(!elite) population[i].improve(adaptiveAdjustmentRate.get());


                });
    }
    void removeExceedingLimit(){
        IntStream.range(0, population.length).parallel()
                .forEach(i -> {
                    population[i].removeExceedingLimit();
                });
    }
    void improveSoftConstraints(){
        IntStream.range(0, population.length).parallel()
                .forEach(i -> {
                    population[i].improveSoftConstraints(improvingRate);
                });
    }

    void removeExtraAllocations(){
        for (int i = 0; i < population.length; i++) {
            population[i].removeExtraAllocations();
        }
    }
    void removeIncorrectAllocations(){
        for (int i = 0; i < population.length; i++) {
            population[i].removeIncorrectAllocations();
        }
    }
    void addMissingAllocations(){
        for (int i = 0; i < population.length; i++) {
            population[i].addMissing();
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
        Arrays.sort(population, Collections.reverseOrder());
        return population[0].getFitness();
//        float maxFitness = -Float.MAX_VALUE;
//
//        for (int i = 0; i < population.length; i++) {
//            if (population[i].getFitness() > maxFitness) {
//                maxFitness = population[i].getFitness();
//            }
//        }
//        return maxFitness;
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

    int getFitnessPosition(int individual){
        int position = 0;
        float individualFitness = population[individual].getFitness();
        for (int i = 0; i < population.length; i++) {
            if(i!=individual){
                if(population[i].getFitness() > individualFitness){
                    position++;
                }
            }
        }
        return position;
    }

    public int getGenerations() {
        return generations;
    }
}
