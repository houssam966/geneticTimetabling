public class Student {
    int[] modules;
    int[] required;
    int[] classPreferences;
    int[] dayPreferences;
    //int[] studentPreferences;

    //TODO add preferences
    public Student(int[] modules, int numberOfClasses, int[] dayPreferences){
        this.modules = modules;
        required = new int[numberOfClasses];
        classPreferences = new int[numberOfClasses];
        this.dayPreferences = dayPreferences;
//        studentPreferences = new int[];
    }

    void addDayPreference(int dayNumber, int preferenceNumber){
        dayPreferences[dayNumber] = preferenceNumber;
    }
//    void addTaPreference(String ta, int preferenceNumber){
//
//    }
//    void addStudentPreference(int studentNumber, int preferenceNumber){
//        studentPreferences[studentNumber] = preferenceNumber;
//
//    }

}
