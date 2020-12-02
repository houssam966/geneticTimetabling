public class Student {
    int[] modules;
    int[] required;
    int[] preferences;

    //TODO add preferences
    public Student(int[] modules, int numberOfClasses){
        this.modules = modules;
        required = new int[numberOfClasses];
    }

}
