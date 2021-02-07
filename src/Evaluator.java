public class Evaluator {

    int getNumberOfClashes(DNA timetable){
        int numberOfClashes = 0;
        for (int i = 0; i < timetable.clashes.length; i++) {
            Pair pair = timetable.clashes[i];
            if(timetable.timetable[pair.first] == 1 & timetable.timetable[pair.second] == 1){
                //there is a clash
                numberOfClashes++;
            }
        }
        return numberOfClashes;
    }

    Properties getProperties(DNA timetable){
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        int numberOfClasses = timetable.numberOfClasses;
        Activity[] classes = timetable.classes;
        int[] studentsPerClass = new int[numberOfClasses];
        int[] tutorialCount = new int[numberOfStudents * numberOfModules];
        int[] practicalCount = new int[numberOfStudents * numberOfModules];

        for (int i = 0; i < timetable.timetable.length; i++) {
            int assigned = timetable.timetable[i];
            int studentNumber = i % numberOfStudents;
            int classNumber = i / numberOfStudents;
            int moduleNumber = classes[classNumber].moduleIndex;
            String type = classes[classNumber].type;
            studentsPerClass[classNumber] += assigned; //if assigned would add 1, otherwise add 0
            int moduleStudentIndex = moduleNumber * numberOfStudents + studentNumber;

            //assuming only two possible types, practical or tutorial
            if (type.equals("Practical")) {
                practicalCount[moduleStudentIndex] += assigned;
            } else {

                tutorialCount[moduleStudentIndex] += assigned;
            }
        }
        return new Properties(practicalCount, tutorialCount, studentsPerClass);
    }

    int getIncorrectAllocations(DNA timetable) {
        int incorrectAllocations = 0;
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        Student[] students = timetable.students;
        Properties properties = getProperties(timetable);
        int[] tutorialCount = properties.tutorialCount;
        int[] practicalCount = properties.practicalCount;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];
            if(students[studentNumber].modules[moduleNumber] == 0) {
                incorrectAllocations+= numberOfAssignedPracticals + numberOfAssignedTutorials;
            }
        }
        return incorrectAllocations;
    }

    int getMissingAllocations(DNA timetable){
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        Student[] students = timetable.students;
        Properties properties = getProperties(timetable);
        int[] tutorialCount = properties.tutorialCount;
        int[] practicalCount = properties.practicalCount;
        int missingAllocations = 0;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];
            Module module = timetable.modules[moduleNumber];

            if(students[studentNumber].modules[moduleNumber] == 1) {
                //if student takes module
                if (module.hasPractical && numberOfAssignedPracticals <= 0)
                    missingAllocations++;
                if (module.hasTutorial && numberOfAssignedTutorials <= 0)
                    missingAllocations++;
            }
        }
        return missingAllocations;
    }
    int getExtraAllocations(DNA timetable){
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        Student[] students = timetable.students;
        Properties properties = getProperties(timetable);
        int[] tutorialCount = properties.tutorialCount;
        int[] practicalCount = properties.practicalCount;
        int extraAllocations = 0;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];

            if(students[studentNumber].modules[moduleNumber] == 1) {
                //student takes module
                if(numberOfAssignedPracticals > 1)
                    extraAllocations+= numberOfAssignedPracticals-1;
                if(numberOfAssignedTutorials > 1)
                    extraAllocations+= numberOfAssignedTutorials -1;
            }
        }
        return extraAllocations;
    }
    int getOverLimitClasses(DNA timetable) {
        int overLimitClasses = 0;
        Properties properties = getProperties(timetable);
        int[] studentsPerClass = properties.studentsPerClass;
        //check number of students per class
        for (int i = 0; i < studentsPerClass.length; i++) {
            int students = studentsPerClass[i];
            //Assuming all classes have capacity of 20
            //TODO: take limit here from class object
            if (students > 20) overLimitClasses++;
        }
        return overLimitClasses;
    }

    void getStudentProperties(DNA timetable) {
        int numberOfStudents = timetable.numberOfStudents;
        int numberOfModules = timetable.numberOfModules;
        int numberOfClasses = timetable.numberOfClasses;
        Student[] students = timetable.students;
        Activity[] classes = timetable.classes;
        Module[] modules = timetable.modules;
        Properties properties = getProperties(timetable);
        int[] tutorialCount = properties.tutorialCount;
        int[] practicalCount = properties.practicalCount;

        for (int i = 0; i < numberOfStudents*numberOfModules; i++) {
            int studentNumber = i%numberOfStudents;
            int moduleNumber = i/numberOfStudents;
            Module module = modules[moduleNumber];
            int numberOfAssignedTutorials = tutorialCount[i];
            int numberOfAssignedPracticals = practicalCount[i];

            System.out.print("Student " + studentNumber + ": ");
            System.out.print(module.name + ": " + numberOfAssignedPracticals + " Practicals, "
                    + numberOfAssignedTutorials + " Tutorials");
            System.out.println();
        }
    }
}
