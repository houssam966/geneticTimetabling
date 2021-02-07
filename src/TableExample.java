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
            //if(student == 0) System.out.print("\n");
            if(student == 0) classNumber++;
            data[classNumber][student+1] = fittest.timetable[i];
        }

        final Class[] columnClass = new Class[] {
                String.class, String.class, String.class, String.class, String.class,String.class,String.class
        };
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
        JLabel label = new JLabel();
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
        int popmax = 50;
        int maxGenerations = 1000;
        float mutationRate = 0.01f;
        Manager manager = new Manager();

        Module[] modules = {
                new Module("HCI", true, true),
                new Module("AIP", false, true),
                new Module("AIN", true, true),
                new Module("VER", false, true),
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
                new Student(new int[]{1,1,1,0}, classes.length, new int[]{5,0,0,0,0}), // takes HCI, AIP and AIN
                new Student(new int[]{1,1,1,0}, classes.length, new int[]{0,5,0,0,0}), // takes HCI, AIP and AIN

                new Student(new int[]{0,1,1,1}, classes.length, new int[]{0,0,5,0,0}), // takes AIP and AIN, and VER
                new Student(new int[]{0,1,1,1}, classes.length, new int[]{0,0,0,5,0}), // takes AIP and AIN, and VER

                new Student(new int[]{1,0,1,1}, classes.length, new int[]{0,0,0,0,5}), // takes HCI, AIN and VER

                new Student(new int[]{1,1,0,1}, classes.length, new int[]{5,0,0,0,0}), // takes HCI, AIP and VER
        };
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
        //number of clashes
        DNA fittest = population.getFittest();
        Evaluator evaluator = new Evaluator();
        int numberOfClashes = evaluator.getNumberOfClashes(fittest);

        //number of incorrect class allocations (student gets a class not in his module)
        int incorrectAllocations = evaluator.getIncorrectAllocations(fittest);
        //number of extra class allocations (student gets two labs or two practicals)
        int extraAllocations = evaluator.getExtraAllocations(fittest);
        //number of missing class allocations (student doesn't get a lab or practical for a module he takes)
        int missingAllocations = evaluator.getMissingAllocations(fittest);
        //number of classes exceeding capacity
        int overLimitClasses = evaluator.getOverLimitClasses(fittest);


        //population.getFittest().print();
        System.out.println("Number of clashes: " + numberOfClashes);
        System.out.println("Number of incorrect allocations: " + incorrectAllocations);
        System.out.println("Number of extra allocations: " + extraAllocations);
        System.out.println("Number of missing allocations: " + missingAllocations);
        System.out.println("Number of classes exceeding limit capacity: " + overLimitClasses);
        System.out.println("\n===================================");

        evaluator.getStudentProperties(fittest);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                new TableExample(population);
            }
        });
    }
}