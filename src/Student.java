public class Student {
    int[] classes;

    //TODO add preferences
    public Student(int[] classes){
        this.classes = classes;
    }

    public boolean hasClass(int classId){
        for (int i = 0; i < classes.length; i++) {
            if(classId == classes[i]){
                return true;
            }
        }
        return false;
    }
}
