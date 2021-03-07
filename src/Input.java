import java.util.Random;

public class Input {
    Activity[] classes;
    Module[] modules;
    Student[] students;
    int numberOfStudents;

    public Input(int numberOfModules, int numberOfStudents, int numberOfClasses){
        classes = new Activity[numberOfClasses];
        modules = new Module[numberOfModules];
        students = new Student[numberOfStudents];
        this.numberOfStudents = numberOfStudents;
  }
    void initialise(){
        initialiseModules();
        initialiseClasses();
        initialiseStudents();
    }

    void initialiseClasses(){
        classes = new Activity[] {
                //MON
                new Activity("11:00:00","12:00:00", "Mon", 1,"Tutorial", "Petr", 0,60), //HCI 0
                new Activity("11:00:00","13:00:00", "Mon", 1,"Practical", "Amanda",1,20), //AIP 1
                new Activity("12:00:00","14:00:00", "Mon", -1, "Practical", "Amanda",1,20), //AIP 2
                new Activity("17:00:00","18:00:00", "Mon", -1,"Tutorial", "Mcburney",2,80), //AIN 3
                //TUE
                new Activity("09:00:00","11:00:00", "Tue", 1,"Practical", "Petr",0,20), //HCI 4
                new Activity("11:00:00","12:00:00", "Tue", 1,"Practical", "Mcburney",2,20), //AIN 5
                new Activity("12:00:00","13:00:00", "Tue", -1,"Practical", "Mcburney",2,20), //AIN 6
                new Activity("13:00:00","14:00:00", "Tue", -1,"Practical", "Mcburney",2,20), //AIN 7
                new Activity("15:00:00","16:00:00", "Tue", -1,"Practical", "Mcburney",2,20), //AIN 8
                new Activity("16:00:00","18:00:00", "Tue", -1,"Practical", "Hannah",3,20), //VER 9
                //WED
                new Activity("09:00:00","11:00:00", "Wed", 1,"Practical", "Amanda",1,20), //AIP 10
                new Activity("11:00:00","13:00:00", "Wed", 1,"Practical", "Amanda",1,20), //AIP 11
                new Activity("11:00:00","13:00:00", "Wed", 1,"Practical", "Petr",0,20), //HCI 12
                new Activity("12:00:00","13:00:00", "Wed", -1,"tutorial", "Michael",4,100), //COV 13
                //THU
                new Activity("10:00:00","12:00:00", "Thu", 1,"Practical", "Hannah",3,20), //VER 13
                //FRI
                new Activity("14:00:00","16:00:00", "Fri", -1,"Practical", "Petr",0,20), //HCI 14
                new Activity("15:00:00","16:00:00", "Fri", -1,"Tutorial", "Osvaldo",5,100), //COS 15
        };
    }
    void initialiseModules(){
        modules = new Module[]{
                new Module("HCI", true, true),
                new Module("AIP", false, true),
                new Module("AIN", true, true),
                new Module("VER", false, true),
                new Module("COV", true, false),
                new Module("COS", true, false),
        };
    }
    void initialiseStudents(){
        //todo generate data programmatically
        students = new Student[numberOfStudents];
        for (int i = 0; i < numberOfStudents; i++) {
            Student student = new Student(modules.length, classes.length, students.length);
            students[i] = student;
        }

        //All 80 students take aip and ain, last 20 take ver, first 60 take HCI
        for (int i = 0; i < numberOfStudents; i++) {
            Student student = students[i];
            if(i<60) student.addModule(0);
            student.addModule(1);
            student.addModule(2);
            if(i>=60) student.addModule(3);
            int[] studentPreferences = new int[numberOfStudents];
            int[] dayPreferences = new int[5];
            int[] timePreferences = new int[5];
            Random r = new Random();
            for (int j = 0; j < numberOfStudents; j++) {
                if(r.nextFloat() > 0.5) studentPreferences[j] = 1;
                else studentPreferences[j] = 0;
            }
            for (int j = 0; j < 5; j++) {
                int timePreference = r.nextFloat() > 0.5? 1: -1;
                int dayPreference = r.nextInt(6) * timePreference;
                dayPreferences[j] = dayPreference;
                timePreferences[j] = timePreference;
            }
            student.addAllStudentPreferences(studentPreferences);
            student.addAllDayPreferences(dayPreferences);
            student.addAllTimePreferences(timePreferences);
        }

//
//        Student student0 = new Student(new int[]{1,1,1,0}, classes.length, students.length);  // takes HCI, AIP and AIN
//        student0.addAllStudentPreferences(new int[]{0,0,0,0,0,1});
//        student0.addAllDayPreferences(new int[]{-5,5,0,0,0});
//        student0.addAllTimePreferences(new int[]{1,1,1,1,1});
//
//        Student student1 = new Student(new int[]{1,1,1,0}, classes.length, students.length); // takes HCI, AIP and AIN
//        student1.addAllStudentPreferences(new int[]{0,0,0,0,1,0});
//        student1.addAllDayPreferences(new int[]{5,-5,0,0,0});
//        student1.addAllTimePreferences(new int[]{0,1,1,1,1});
//
//        Student student2 = new Student(new int[]{0,1,1,1}, classes.length, students.length); // takes AIP and AIN, and VER
//        student2.addAllStudentPreferences(new int[]{0,0,0,1,0,0});
//        student2.addAllDayPreferences(new int[]{0,0,-5,5,0});
//        student2.addAllTimePreferences(new int[]{1,1,1,1,1});
//
//        Student student3 = new Student(new int[]{0,1,1,1}, classes.length, students.length); // takes AIP and AIN, and VER
//        student3.addAllStudentPreferences(new int[]{0,0,1,0,0,0});
//        student3.addAllDayPreferences(new int[]{0,0,5,-5,0});
//        student3.addAllTimePreferences(new int[]{0,0,0,0,0});
//
//        Student student4 = new Student(new int[]{1,0,1,1}, classes.length, students.length); // takes HCI, AIN and VER
//        student4.addAllStudentPreferences(new int[]{0,1,0,0,0,0});
//        student4.addAllDayPreferences(new int[]{-5,0,0,0,5});
//        student4.addAllTimePreferences(new int[]{0,0,0,0,0});
//
//        Student student5 = new Student(new int[]{1,1,0,1}, classes.length, students.length); // takes HCI, AIP and VER
//        student5.addAllStudentPreferences(new int[]{1,0,0,0,0,0});
//        student5.addAllDayPreferences(new int[]{5,5,0,0,-5});
//        student5.addAllTimePreferences(new int[]{0,0,0,0,0});
//        Student student6 = new Student(new int[]{1,1,0,1}, classes.length, students.length); // takes HCI, AIP and VER
//        student6.addAllStudentPreferences(new int[]{1,0,0,0,0,0});
//        student6.addAllDayPreferences(new int[]{5,5,0,0,-5});
//        student6.addAllTimePreferences(new int[]{0,0,0,0,0});
//        Student student7 = new Student(new int[]{1,1,0,1}, classes.length, students.length); // takes HCI, AIP and VER
//        student7.addAllStudentPreferences(new int[]{1,0,0,0,0,0});
//        student7.addAllDayPreferences(new int[]{5,5,0,0,-5});
//        student7.addAllTimePreferences(new int[]{0,0,0,0,0});
//        Student student8 = new Student(new int[]{1,1,0,1}, classes.length, students.length); // takes HCI, AIP and VER
//        student8.addAllStudentPreferences(new int[]{1,0,0,0,0,0});
//        student8.addAllDayPreferences(new int[]{5,5,0,0,-5});
//        student8.addAllTimePreferences(new int[]{0,0,0,0,0});
//        Student student9 = new Student(new int[]{1,1,0,1}, classes.length, students.length); // takes HCI, AIP and VER
//        student9.addAllStudentPreferences(new int[]{1,0,0,0,0,0});
//        student9.addAllDayPreferences(new int[]{5,5,0,0,-5});
//        student9.addAllTimePreferences(new int[]{0,0,0,0,0});
//        Student student10 = new Student(new int[]{1,1,0,1}, classes.length, students.length); // takes HCI, AIP and VER
//        student10.addAllStudentPreferences(new int[]{1,0,0,0,0,0});
//        student10.addAllDayPreferences(new int[]{5,5,0,0,-5});
//        student10.addAllTimePreferences(new int[]{0,0,0,0,0});

//        students = new Student[]{student0, student1, student2, student3, student4, student5,student6,student7,student8,student9,student10};
    }
}
