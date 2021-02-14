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

    float[] getStudentPreferences(Student[] students, Activity[] classes, Weights w){
        int numberOfStudents = students.length;
        int numberOfClasses = classes.length;
        int timetableSize = numberOfStudents*numberOfClasses;
        float[] preferences = new float[timetableSize];

        for (int i = 0; i < timetableSize ; i++) {
            int studentNumber = i % numberOfStudents;
            int classNumber = i / numberOfStudents;
            Activity activity = classes[classNumber];
            Student student = students[studentNumber];
            int dayNumber = getDayNumber(activity.day);
            //TODO add other soft constraints here
            preferences[i] = student.dayPreferences[dayNumber] * w.day
                    + activity.timePeriod * student.timePreferences[dayNumber] * w.time; //if timeperiod is 1 and student prefers 1, max

            // day is 5/7 while timePeriod is 2/7, maximum is 1.
            // timePeriod!= preferredTimePeriod then -2/7
            // day = -5 and timePeriod not preferred then minimum (-1)

        }
        return preferences;
    }

    ArrayList<Integer> getPrefferedClasses(Student student, ArrayList<Integer> moduleClasses, Activity[] allClasses, Weights w){
        ArrayList<Integer> prefferedClasses = new ArrayList<>();
        float maxScore = -Float.MAX_VALUE;
        for (int i1 = 0; i1 < moduleClasses.size(); i1++) {
            int classNumber = moduleClasses.get(i1);
            Activity activity = allClasses[classNumber];
            int dayNumber = getDayNumber(activity.day);
            float score =  student.dayPreferences[dayNumber] * w.day
                    + activity.timePeriod * student.timePreferences[dayNumber] * w.time;
            if(score > maxScore){
                maxScore = score;
                //remove everything in the arraylist
                prefferedClasses.clear();
                prefferedClasses.add(classNumber);
            }else if(score == maxScore){
                prefferedClasses.add(classNumber);
            }
        }
        return prefferedClasses;
    }

    ArrayList<Integer> getAssignedClasses(int studentNumber, ArrayList<Integer> moduleClasses, DNA timetable){
        ArrayList<Integer> assignedClasses = new ArrayList<>();
        for (int i1 = 0; i1 < moduleClasses.size(); i1++) {
            int classNumber = moduleClasses.get(i1);
            int studentClassIndex = classNumber * timetable.numberOfStudents + studentNumber;
            if(timetable.timetable[studentClassIndex] == 1){
                assignedClasses.add(classNumber);
            }
        }
        return assignedClasses;
    }

    int getInaccuarteAllocations(ArrayList<Integer> prefferedClasses, ArrayList<Integer> assignedClasses){
        int inaccuarteAllocations = 0;
        boolean inaccurate = true;
        int i1 = 0;
        while (i1 < prefferedClasses.size() && inaccurate) {
            for (int i2 = 0; i2 < assignedClasses.size(); i2++) {
                if(prefferedClasses.get(i1) == assignedClasses.get(i2)){
                    inaccurate = false;
                    break;
                }
            }
            i1++;
        }
        if(inaccurate) inaccuarteAllocations++;
        return inaccuarteAllocations;
    }
    int getDayNumber(String day){
        switch (day){
            case("Mon"): return 0;
            case ("Tue"): return 1;
            case ("Wed"): return 2;
            case ("Thu"): return 3;
            case ("Fri"): return 4;
            default: return 0;
        }
    }
}
