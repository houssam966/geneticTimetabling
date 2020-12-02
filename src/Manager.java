import java.time.LocalTime;
import java.util.ArrayList;

public class Manager {
    public Manager(){

    }

    void assignClasses(Student student, Activity[] classes){
        for (int i = 0; i < classes.length; i++) {
            int moduleIndex = classes[i].moduleIndex;
            if(student.modules[moduleIndex] == 1){
                student.required[i] = 1;
            } else{
                student.required[i] = 0;
            }
        }
    }

    /**
     * overlap:
     * (s2<=s1<e2 || s2<e1<e2) ||
     * (s1<=s2<e1 || s1<e2<e1)
     *
     * @param classes
     * @param students
     * @return
     */
    Pair[] getClashes(Activity[] classes, Student[] students, int numberOfStudents){
        ArrayList<Pair> clashes = new ArrayList<>();

        for (int x = 0; x < classes.length; x++) {
            Activity c1 = classes[x];
            String d1 = c1.day;
            LocalTime s1 = LocalTime.parse(c1.start);
            LocalTime e1 = LocalTime.parse(c1.end);

            for (int y = x+1; y < classes.length; y++) {
                Activity c2 = classes[y];
                String d2 = c2.day;
                LocalTime s2 = LocalTime.parse(c2.start);
                LocalTime e2 = LocalTime.parse(c2.end);

                if(d1.equals(d2)){
                    if(((s1.equals(s2) || s1.isAfter(s2)) && s1.isBefore(e2)) ||
                            (e1.isAfter(s2) && e1.isBefore(e2)) ||
                            ((s2.equals(s1) || s2.isAfter(s1)) && s2.isBefore(e1)) ||
                            ((e2.isAfter(s1)) && e2.isBefore(e1))) {
                        //there is a clash between class x and class y
                        //for every student add both classes to clashes
                        for (int z = 0; z < students.length; z++) {
                            int index1 = z + x*numberOfStudents; // class x, student z
                            int index2 = z + y*numberOfStudents; // class y, student z
                            clashes.add(new Pair(index1, index2));
                        }
                    }
                }
            }
        }

        return clashes.toArray(new Pair[clashes.size()]);
    }
}
