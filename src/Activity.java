public class Activity {
    String start,end,day,staff,type;
    int moduleIndex;
    int timePeriod; //either 1 (AM) or -1 (PM)

    public Activity(String start, String end, String day, int timePeriod, String type, String staff, int moduleIndex) {
        this.day = day;
        this.end = end;
        this.staff = staff;
        this.start = start;
        this.type = type;
        this.moduleIndex = moduleIndex;
        this.timePeriod = timePeriod;
    }


}
