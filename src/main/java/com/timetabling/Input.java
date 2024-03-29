package com.timetabling;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Input {
    Activity[] classes;
    Module[] modules;
    Student[] students;

    public Input(){
        initialise();
  }

    void initialise() {

        try {
            InputStream inp =  getClass().getClassLoader().getResourceAsStream("year1_sem1.xlsx");

            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);
            Sheet studentsSheet = wb.getSheetAt(1);
            modules = getModulesFromSheet(sheet);

            ArrayList<String> moduleNames = new ArrayList<>();
            for (Module module : modules) {
                moduleNames.add(module.name);
    //            System.out.println(module.name + " Practicals: " + module.numOfPracticals+ " Tutorials: " + module.numOfTutorials);
            }
            classes = getClassesFromSheet(sheet,moduleNames);
//            try {
//                createStudentsWorkbook( modules.length, classes);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            for (int i = 0; i < classes.length; i++) {
                String type = classes[i].type;
                int moduleNumber = classes[i].moduleIndex;
                switch (type){
                    case "Practical":{
                        modules[moduleNumber].addPractical(i);
                        break;
                    }
                    case "Lecture":{
                        modules[moduleNumber].addLecture(i);
                        break;
                    }
                    case "Tutorial":{
                        modules[moduleNumber].addTutorial(i);
                        break;
                    }
                    case "Small Group":{
                        modules[moduleNumber].addSmg(i);
                        break;
                    }
                }
            }
//            for (int i = 0; i < classes.length; i++) {
//                System.out.println(classes[i].type);
//            }
//        for (int i = 0; i < classes.length; i++) {
//            Activity activity = classes[i];
//            System.out.println();
//            System.out.print(activity.day + " ");
//            System.out.print(activity.start + " ");
//            System.out.print(activity.end + " ");
//            System.out.print(activity.type + " ");
//            System.out.print(activity.staff + " ");
//            System.out.print(activity.timePeriod + " ");
//            System.out.print(activity.capacity + " ");
//        }
            students = getStudentsFromSheet(studentsSheet, modules.length, classes.length);
//            for (int i = 0; i < students.length; i++) {
//                System.out.println(Arrays.toString(students[i].modules));
//            }

//        createStudentsWorkbook(numberOfStudents, modules.length);
//        initialiseStudents();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    Module[] getModulesFromSheet(Sheet sheet){
        DataFormatter formatter = new DataFormatter();
        ArrayList<String> moduleNames = new ArrayList<>();
        ArrayList<String> pracList = new ArrayList<>();
        ArrayList<String> tutList= new ArrayList<>();
        ArrayList<String> smgList= new ArrayList<>();
        ArrayList<String> lecList= new ArrayList<>();
        HashMap<String, Integer> pracCount = new HashMap<>();
        HashMap<String, Integer> tutCount = new HashMap<>();
        HashMap<String, Integer> smgCount = new HashMap<>();
        HashMap<String, Integer> lecCount = new HashMap<>();
        int index = -1;
        for (Row row : sheet) {
            if(index == -1){
                index++;
                continue;
            }
            String type = ""; String module = "";
            for (Cell cell : row) {
                String columnHeader = formatter.formatCellValue(sheet.getRow(0).getCell(cell.getColumnIndex()));
                String text = formatter.formatCellValue(cell);
                switch(columnHeader){
                    case "Type": type = text; break;
                    case "Activity": module = text.substring(5,8); break;
                }
            }

            if(!moduleNames.contains(module)){
                moduleNames.add(module);
                pracCount.put(module,0);
                tutCount.put(module,0);
                smgCount.put(module,0);
                lecCount.put(module,0);
            }
            if(type.equals("Practical")){
                pracList.add(module);
                pracCount.put(module,pracCount.get(module)+1);
            } else if(type.equals("Tutorial")){
                tutList.add(module);
                tutCount.put(module,tutCount.get(module)+1);
            } else if(type.equals("Small Group")){
                smgList.add(module);
                smgCount.put(module,smgCount.get(module)+1);
            } else if(type.equals("Lecture")){
                lecList.add(module);
                lecCount.put(module,lecCount.get(module)+1);
            }
        }
        Module[] modules = new Module[moduleNames.size()];
        for (int i = 0; i < moduleNames.size(); i++) {
            String name = moduleNames.get(i);
            modules[i] = new Module(name, tutList.contains(name), pracList.contains(name), smgList.contains(name),
                    lecList.contains(name), pracCount.get(name), tutCount.get(name), smgCount.get(name),
                    lecCount.get(name));
        }
        return modules;
    }

    Activity[] getClassesFromSheet(Sheet sheet, ArrayList<String> modules){
        DataFormatter formatter = new DataFormatter();
        Activity[] classesToReturn = new Activity[sheet.getPhysicalNumberOfRows()-1];
        int index = -1;
        for (Row row : sheet) {
            if(index == -1){
                index++;
                continue;
            }
            String day = " ";String start = "";String end = "";String type = "";String staff = "";String module = "";
            int capacity = 0;
            for (Cell cell : row) {
                CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex()); //cell reference
                String columnHeader = formatter.formatCellValue(sheet.getRow(0).getCell(cell.getColumnIndex()));
                String text = formatter.formatCellValue(cell);
                switch(columnHeader){
                    case "Day": day = text; break;
                    case "Start": start = text; break;
                    case "End": end = text; break;
                    case "Type": type = text; break;
                    case "Staff": staff = text; break;
                    case "Activity": module = text.substring(5,8); break;
                    case "Capacity": capacity = Integer.parseInt(text);
                }
            }
            int timePeriod =  Integer.parseInt(start.substring(0,2)) >= 12? -1: 1;
            classesToReturn[index] = new Activity(start, end, day, timePeriod, type, staff, modules.indexOf(module),capacity);
            index++;
        }
        return classesToReturn;
    }

    Student[] getStudentsFromSheet(Sheet sheet, int numberOfModules, int numberOfClasses){
        DataFormatter formatter = new DataFormatter();
        Student[] studentsToReturn = new Student[sheet.getPhysicalNumberOfRows()-1];
//        Student[] studentsToReturn = new Student[200];
        int index = -1;
        for (Row row : sheet) {
            if(index == -1){
                index++;
                continue;
            }
            if(index == studentsToReturn.length) break;

            int module1 = -1;int module2 = -1;int module3 = -1;int module4 = -1;
            String dayPref = "";String timePref = "";String studentPref = "";

            for (Cell cell : row) {
                CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex()); //cell reference
                String columnHeader = formatter.formatCellValue(sheet.getRow(0).getCell(cell.getColumnIndex()));
                String text = formatter.formatCellValue(cell);
                switch(columnHeader){
                    case "Module 1": module1 = Integer.parseInt(text); break;
                    case "Module 2": module2 = Integer.parseInt(text); break;
                    case "Module 3": module3 = Integer.parseInt(text); break;
                    case "Module 4": module4 = Integer.parseInt(text); break;
                    case "Day Preferences": dayPref = text; break;
                    case "Time Preferences": timePref = text; break;
                    case "Student Preferences": studentPref = text; break;
                }
            }
            String[] stringDayPreferences = dayPref.substring(1,dayPref.length()-1).trim().split(",");
            for (int i = 0; i < stringDayPreferences.length; i++) {
                stringDayPreferences[i] = stringDayPreferences[i].trim();
            }
            int [] dayPreferences = Arrays.stream(stringDayPreferences).mapToInt(Integer::parseInt).toArray();

            String[] stringTimePreferences = timePref.substring(1,timePref.length()-1).trim().split(",");
            for (int i = 0; i < stringTimePreferences.length; i++) {
                stringTimePreferences[i] = stringTimePreferences[i].trim();

            }
            int [] timePreferences = Arrays.stream(stringTimePreferences).mapToInt(Integer::parseInt).toArray();

            String[] stringStudentPreferences = studentPref.substring(1,studentPref.length()-1).split(",");
            for (int i = 0; i < stringStudentPreferences.length; i++) {
                stringStudentPreferences[i] = stringStudentPreferences[i].trim();
            }
            int [] studentPreferences = Arrays.stream(stringStudentPreferences).mapToInt(Integer::parseInt).toArray();

            Student student= new Student(numberOfModules,numberOfClasses,studentsToReturn.length);
            student.addModule(module1);student.addModule(module2);student.addModule(module3);student.addModule(module4);
            student.addAllDayPreferences(dayPreferences);
            student.addAllTimePreferences(timePreferences);
            student.addAllStudentPreferences(studentPreferences);
            studentsToReturn[index] = student;
            index++;
        }
        return studentsToReturn;
    }

    void printClassCapacities(int numberOfModules){
        ArrayList<Integer> moduleIndices = new ArrayList<>();
        for (int i = 0; i < numberOfModules; i++) {
            moduleIndices.add(i);
        }
        int[] capacities = new int[moduleIndices.size()];
        for (int i = 0; i < moduleIndices.size(); i++) {
            int capacityPrac = 0;
            int capacityTut = 0;
            for (int j = 0; j < classes.length; j++) {
                if(classes[j].moduleIndex == moduleIndices.get(i) && classes[j].type.equals("Practical")){
                    capacityPrac+=classes[j].capacity;
                }
                if(classes[j].moduleIndex == moduleIndices.get(i) && classes[j].type.equals("Tutorial")){
                    capacityTut+=classes[j].capacity;
                }
            }
            if(capacityTut == 0) capacities[i] = capacityPrac; //if no tutotirals
            else if(capacityPrac == 0) capacities[i] = capacityTut; // if no practicals
            else capacities[i] = capacityPrac < capacityTut? capacityPrac: capacityTut;
        }
        for (int i = 0; i < capacities.length; i++) {
            System.out.println(capacities[i]);
        }
    }
    void createStudentsWorkbook(int numberOfModules, Activity[] classes) throws IOException {

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("new sheet");
        Row headers = sheet.createRow(0);
        headers.createCell(0).setCellValue("Students");
        headers.createCell(1).setCellValue("Module 1");
        headers.createCell(2).setCellValue("Module 2");
        headers.createCell(3).setCellValue("Module 3");
        headers.createCell(4).setCellValue("Module 4");
        headers.createCell(5).setCellValue("Day Preferences");
        headers.createCell(6).setCellValue("Time Preferences");
        headers.createCell(7).setCellValue("Student Preferences");
        ArrayList<Integer> moduleIndices = new ArrayList<>();
        for (int i = 0; i < numberOfModules; i++) {
            moduleIndices.add(i);
        }
        int[] capacities = new int[moduleIndices.size()];
        for (int i = 0; i < moduleIndices.size(); i++) {
            int capacityPrac = 0;
            int capacityTut = 0;
            int capacitySmg = 0;
            for (int j = 0; j < classes.length; j++) {
                if(classes[j].moduleIndex == moduleIndices.get(i) && classes[j].type.equals("Practical")){
                    capacityPrac+=classes[j].capacity;
                }
                if(classes[j].moduleIndex == moduleIndices.get(i) && classes[j].type.equals("Tutorial")){
                    capacityTut+=classes[j].capacity;
                }
                if(classes[j].moduleIndex == moduleIndices.get(i) && classes[j].type.equals("Small Group")){
                    capacitySmg+=classes[j].capacity;
                }
            }
            if(!modules[moduleIndices.get(i)].hasPractical && !modules[moduleIndices.get(i)].hasSmallGroup){
                capacities[i] = capacityTut;
            }
            else if(!modules[moduleIndices.get(i)].hasSmallGroup){
//                if(capacitySmg == 0) capacities[i] = capacityPrac; //if no smgs
//                else if(capacityPrac == 0) capacities[i] = capacitySmg; // if no practicals
//                else
                capacities[i] = capacityPrac;
            } else if(!modules[moduleIndices.get(i)].hasPractical){
                capacities[i] = capacitySmg;
            } else{
                capacities[i] = capacityPrac < capacitySmg? capacityPrac: capacitySmg;
//                if(capacityTut == 0) capacities[i] = capacityPrac; //if no tutotirals
//                else if(capacityPrac == 0) capacities[i] = capacityTut; // if no practicals
//                else capacities[i] = capacityPrac < capacityTut? capacityPrac: capacityTut;
            }

        }

        for (int i = 0; i < capacities.length; i++) {
            System.out.println(capacities[i]);
        }
        int i = 1;
        while(moduleIndices.size() >= 4){
            Row row = sheet.createRow(i);
//            Collections.shuffle(moduleIndices);
            row.createCell(0).setCellValue("Student" + (i-1));
            row.createCell(1).setCellValue(moduleIndices.get(0));
            row.createCell(2).setCellValue(moduleIndices.get(1));
            ArrayList<Integer> indices = new ArrayList<>();
            indices.add(2);
            indices.add(3);
            indices.add(4);
            Collections.shuffle(indices);
            indices.remove(2);
            Collections.sort(indices);
            row.createCell(3).setCellValue(moduleIndices.get(indices.get(0)));
            row.createCell(4).setCellValue(moduleIndices.get(indices.get(1)));
            capacities[moduleIndices.get(0)]--;
            capacities[moduleIndices.get(1)]--;
            capacities[moduleIndices.get(indices.get(0))]--;
            capacities[moduleIndices.get(indices.get(1))]--;

            if(capacities[moduleIndices.get(indices.get(1))] <= 0) moduleIndices.remove(3);
            if(capacities[moduleIndices.get(indices.get(0))] <= 0) moduleIndices.remove(2);
            if(capacities[moduleIndices.get(1)] <= 0) moduleIndices.remove(1);
            if(capacities[moduleIndices.get(0)] <= 0) moduleIndices.remove(0);


//            int numberOfStudents = 270;
//            int[] studentPreferences = new int[numberOfStudents];
            int[] dayPreferences = new int[5];
            int[] timePreferences = new int[5];
            Random r = new Random();
//            for (int j = 0; j < numberOfStudents; j++) {
//                if(r.nextFloat() > 0.95) studentPreferences[j] = 1;
//                else studentPreferences[j] = 0;
//            }
            for (int j = 0; j < 5; j++) {
                int timePreference = r.nextFloat() > 0.8?  (r.nextFloat() > 0.5? 1: -1) : 0;
                int dayPreference = r.nextInt(6) * timePreference;
                dayPreferences[j] = dayPreference;
                timePreferences[j] = timePreference;
            }
            row.createCell(5).setCellValue(Arrays.toString(dayPreferences));
            row.createCell(6).setCellValue(Arrays.toString(timePreferences));
            //row.createCell(7).setCellValue(Arrays.toString(studentPreferences));
            i++;
        }
        // Write the output to a file
        try (OutputStream fileOut = new FileOutputStream("studentsSem2.xlsx")) {
            wb.write(fileOut);
        }
    }

    public void saveSolution(DNA solution){
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Solution");
        Row headers = sheet.createRow(0);
        headers.createCell(0).setCellValue("Students");
        headers.createCell(1).setCellValue("Classes");

        HashMap<Integer, ArrayList<Integer>> allocations = new HashMap<>();
        for (int i = 0; i < solution.timetable.length; i++) {
            int numberOfStudents = solution.numberOfStudents;
            int studentNumber = i%numberOfStudents;
            int classNumber = i/numberOfStudents;
            if(solution.timetable[i] == 0) continue;
            if(allocations.containsKey(studentNumber)){
                ArrayList<Integer> classes = allocations.get(studentNumber);
                classes.add(classNumber);
                allocations.put(studentNumber, classes);
            } else{
                ArrayList<Integer> classes = new ArrayList<>();
                classes.add(classNumber);
                allocations.put(studentNumber, classes);
            }

        }
        for (int i = 1; i <= solution.numberOfStudents; i++) {
            Row row = sheet.createRow(i);

            row.createCell(0).setCellValue("Student" + (i-1));
            String listString = allocations.get(i-1).stream().map(Object::toString)
                    .collect(Collectors.joining(", "));
            row.createCell(1).setCellValue(listString);
        }
        // Write the output to a file
        try {
            OutputStream fileOut = new FileOutputStream("solution.xlsx");
            wb.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
