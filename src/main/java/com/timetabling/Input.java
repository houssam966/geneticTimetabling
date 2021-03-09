package com.timetabling;

import java.io.*;
import java.util.*;

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
            InputStream inp = new FileInputStream(new File("/Users/houssammahlous/Documents/uni/year3/project/reader/output.xlsx"));
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
//        int[] moduleCounts = new int[modules.length];
//        for (int i = 0; i < students.length; i++) {
//            for (int i1 = 0; i1 < students[i].modules.length; i1++) {
//                if(students[i].modules[i1] == 1) moduleCounts[i1]++;
//            }
//        }
//        for (int i = 0; i < moduleCounts.length; i++) {
//            System.out.println(moduleNames.get(i) + ": " + moduleCounts[i] );
//        }
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
        HashMap<String, Integer> pracCount = new HashMap<>();
        HashMap<String, Integer> tutCount = new HashMap<>();
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
            }
            if(type.equals("Practical")){
                pracList.add(module);
                pracCount.put(module,pracCount.get(module)+1);
            } else if(type.equals("Tutorial")){
                tutList.add(module);
                tutCount.put(module,tutCount.get(module)+1);
            }
        }
        Module[] modules = new Module[moduleNames.size()];
        for (int i = 0; i < moduleNames.size(); i++) {
            String name = moduleNames.get(i);
            modules[i] = new Module(name, tutList.contains(name), pracList.contains(name),
                    pracCount.get(name), tutCount.get(name));
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
        int index = -1;
        for (Row row : sheet) {
            if(index == -1){
                index++;
                continue;
            }

            int module1 = -1;int module2 = -1;int module3 = -1;
            String dayPref = "";String timePref = "";String studentPref = "";

            for (Cell cell : row) {
                CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex()); //cell reference
                String columnHeader = formatter.formatCellValue(sheet.getRow(0).getCell(cell.getColumnIndex()));
                String text = formatter.formatCellValue(cell);
                switch(columnHeader){
                    case "Module 1": module1 = Integer.parseInt(text); break;
                    case "Module 2": module2 = Integer.parseInt(text); break;
                    case "Module 3": module3 = Integer.parseInt(text); break;
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
            student.addModule(module1);student.addModule(module2);student.addModule(module3);
            student.addAllDayPreferences(dayPreferences);
            student.addAllTimePreferences(timePreferences);
            student.addAllStudentPreferences(studentPreferences);
            studentsToReturn[index] = student;
            index++;
        }
        return studentsToReturn;
    }

    void createStudentsWorkbook(int numberOfStudents, int numberOfModules) throws IOException {

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("new sheet");
        Row headers = sheet.createRow(0);
        headers.createCell(0).setCellValue("Students");
        headers.createCell(1).setCellValue("Module 1");
        headers.createCell(2).setCellValue("Module 2");
        headers.createCell(3).setCellValue("Module 3");
        headers.createCell(4).setCellValue("Day Preferences");
        headers.createCell(5).setCellValue("Time Preferences");
        headers.createCell(6).setCellValue("Student Preferences");
        ArrayList<Integer> moduleIndices = new ArrayList<>();
        for (int i = 0; i < numberOfModules; i++) {
            moduleIndices.add(i);
        }

        for (int i = 1; i <= numberOfStudents; i++) {
            Row row = sheet.createRow(i);
            Collections.shuffle(moduleIndices);
            row.createCell(0).setCellValue("Student" + (i-1));
            row.createCell(1).setCellValue(moduleIndices.get(0));
            row.createCell(2).setCellValue(moduleIndices.get(1));
            row.createCell(3).setCellValue(moduleIndices.get(2));
            int[] studentPreferences = new int[numberOfStudents];
            int[] dayPreferences = new int[5];
            int[] timePreferences = new int[5];
            Random r = new Random();
            for (int j = 0; j < numberOfStudents; j++) {
                if(r.nextFloat() > 0.9) studentPreferences[j] = 1;
                else studentPreferences[j] = 0;
            }
            for (int j = 0; j < 5; j++) {
                int timePreference = r.nextFloat() > 0.8?  (r.nextFloat() > 0.5? 1: -1) : 0;
                int dayPreference = r.nextInt(6) * timePreference;
                dayPreferences[j] = dayPreference;
                timePreferences[j] = timePreference;
            }
            row.createCell(4).setCellValue(Arrays.toString(dayPreferences));
            row.createCell(5).setCellValue(Arrays.toString(timePreferences));
            row.createCell(6).setCellValue(Arrays.toString(studentPreferences));
        }
        // Write the output to a file
        try (OutputStream fileOut = new FileOutputStream("workbook.xlsx")) {
            wb.write(fileOut);
        }
    }

}
