package com.timetabling;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TableExample extends JFrame
{
    public TableExample(Population population)
    {
        DNA fittest = population.getFittest();
//        for (int i = 0; i < fittest.size; i++) {
//            int student = i%fittest.numberOfStudents;
//            if(student == 0) System.out.print("\n");
//            System.out.print(fittest.timetable[i] + " ");
//        }

        //headers for the table
        String[] columns = new String[fittest.numberOfStudents + 1];
        columns[0] = " ";
        for (int i = 0; i < fittest.numberOfStudents; i++) {
            columns[i+1] = "Student " + i;
        }

        //actual data for the table in a 2d array
        Object[][] data = new Object[fittest.size][fittest.numberOfStudents + 1];
        for (int i = 0; i < fittest.numberOfClasses; i++) {
            Activity activity = population.classes[i];
            data[i][0] = population.modules[activity.moduleIndex].name + " " +  activity.type + " " + i;
        }
        int classNumber = -1;
        for (int i = 0; i < fittest.size; i++) {
            int student = i%fittest.numberOfStudents;
            if(student == 0) classNumber++;
            data[classNumber][student+1] = fittest.timetable[i];
        }

        final Class[] columnClass = new Class[fittest.numberOfStudents+1];
        for (int i = 0; i < fittest.numberOfStudents+1; i++) {
            columnClass[i] = String.class;
        }
        //create table model with data
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex)
            {
                return columnClass[columnIndex];
            }
        };
        JTable table = new JTable(model);

        this.add(new JScrollPane(table));
        this.setTitle("Timetable");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    public static void main(String[] args)
    {
        long start = System.currentTimeMillis();
        int popmax = 150;
        int maxGenerations = 2000;
        float mutationRate = 0.003f;
        float crossoverRate = 0.95f;
        int elitismCount = 3;
        int tournamentSize = 5;
        float temperature = 1f;
        float coolingRate = 0.00f;
        float adjustmentRate = 0.01f;

        Manager manager = new Manager();
        Input input = new Input();
        Module[] modules = input.modules;
        Activity[] classes = input.classes;
        Student[] students = input.students;
        Weights weights = new Weights(0.5f,0.7f,0.9f,0.9f,0.2f,1f/7,2f/7);
        Population population = new Population(students, classes, modules, mutationRate, crossoverRate, elitismCount, temperature, coolingRate, adjustmentRate, popmax, maxGenerations, tournamentSize, weights);
        //initialize population
        population.initialize();

        population.calculateFitness();

        System.out.println("Initial Max Fitness = " + population.getMaxFitness());
        System.out.println("Initial Average Fitness = " + population.getAverageFitness());

        while(!population.finished){
            // Update fitness and sort by fitness
            population.naturalSelection();
            //Create next generation (crossover)
            population.generate();
            //Mutate
            population.mutate();
            population.improveAllocations();
            // Calculate fitness
            population.calculateFitness();
            population.coolTemperature();
            System.out.println("Max: " + population.getMaxFitness() + " Average: " + population.getAverageFitness());
        }

        population.calculateFitness();
        long end = System.currentTimeMillis();
        DNA fittest = population.getFittest();
        Evaluator evaluator = new Evaluator();

        evaluator.getStudentProperties(fittest);
        evaluator.checkSoftConstraints(fittest, weights);
        System.out.println("Fittest Solution: ");
        //number of clashes

        int numberOfClashes = evaluator.getNumberOfClashes(fittest);

        //number of incorrect class allocations (student gets a class not in his module)
        int incorrectAllocations = evaluator.getIncorrectAllocations(fittest);
        //number of extra class allocations (student gets two labs or two practicals)
        int extraAllocations = evaluator.getExtraAllocations(fittest);
        //number of missing class allocations (student doesn't get a lab or practical for a module he takes)
        int missingAllocations = evaluator.getMissingAllocations(fittest);
        //number of classes exceeding capacity
        int overLimitClasses = evaluator.getOverLimitClasses(fittest);
        int inaccurateAllocations = evaluator.getInaccurateAllocations(fittest,weights);


        //population.getFittest().print();
        System.out.println("Number of clashes: " + numberOfClashes);
        System.out.println("Number of incorrect allocations: " + incorrectAllocations);
        System.out.println("Number of extra allocations: " + extraAllocations);
        System.out.println("Number of missing allocations: " + missingAllocations);
        System.out.println("Number of classes exceeding limit capacity: " + overLimitClasses);
        System.out.println("Number of inaccurate allocations: " + inaccurateAllocations);
        System.out.println("\n===================================");

        System.out.println("Final Max Fitness = " + population.getMaxFitness());
        System.out.println("Final Average Fitness = " + population.getAverageFitness());
        System.out.println("Total Generations = " + population.getGenerations());
        System.out.println("Time taken " + (end - start) + "ms");
        System.out.println("===================================");


        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                new TableExample(population);
            }
        });
    }
}