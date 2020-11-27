import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.pow;

public class DNA {
    private float fitness;
    private int[] timetable;
    int numberOfStudents, numberOfClasses, size;
    Student[] students;

    public DNA(int numberOfStudents, int numberOfClasses, Student[] students){
        this.numberOfClasses = numberOfClasses;
        this.numberOfStudents = numberOfStudents;
        this.students = students;

        size = numberOfClasses * numberOfStudents;
        timetable = new int[size];

        //generate a random timetable
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            int student = i%numberOfStudents;
            float prob = random.nextFloat();
            if(prob > 0.5){
                timetable[i] = 1;
            } else {
                timetable[i] = 0;
            }
        }

    }

    void updateFitness(){
        for (int i = 0; i < size; i++) {
            int studentNumber = i%numberOfStudents;
            int classNumber = i/numberOfStudents;

            Student currentStudent = students[studentNumber];
            if(currentStudent.hasClass(classNumber)){
                //student has the class
                if(timetable[i] == 1){
                    fitness = fitness + 0.2f;
                } else {
                    fitness = fitness - 0.2f;
                }
            } else {
                if(timetable[i] == 1){
                    fitness = fitness - 0.2f;
                } else {
                    fitness = fitness + 0.2f;
                }
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
        DNA child = new DNA(numberOfStudents, numberOfClasses, students);
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
