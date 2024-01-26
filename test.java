import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeAnalyzer {

    public static void main(String[] args) {
        String filePath = "path/to/your/input/file.csv"; //Path of the file to be read
        analyzeEmployeeData(filePath);
    }

    private static void analyzeEmployeeData(String filePath) {
        // Define constants
        final int CONSECUTIVE_DAYS_THRESHOLD = 7;
        final int MIN_HOURS_BETWEEN_SHIFTS = 1;
        final int MAX_HOURS_IN_SINGLE_SHIFT = 14;

        // Initialize data structures
        Map<String, EmployeeData> employees = new HashMap<>();

        // Read CSV file
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header line
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String employeeId = fields[0].trim();
                String employeeName = fields[1].trim();
                Date startTime = parseTime(fields[2].trim());
                Date endTime = parseTime(fields[3].trim());

                // Update or add employee data
                if (employees.containsKey(employeeId)) {
                    employees.get(employeeId).getShifts().add(new Shift(startTime, endTime));
                } else {
                    List<Shift> shifts = new ArrayList<>();
                    shifts.add(new Shift(startTime, endTime));
                    employees.put(employeeId, new EmployeeData(employeeName, shifts));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Analyze employee data
        for (Map.Entry<String, EmployeeData> entry : employees.entrySet()) {
            String employeeId = entry.getKey();
            EmployeeData data = entry.getValue();
            List<Shift> shifts = data.getShifts();
            int consecutiveDays = 1;
            Date prevEndTime = null;

            for (Shift shift : shifts) {
                Date startTime = shift.getStartTime();
                Date endTime = shift.getEndTime();

                // Check consecutive days
                if (prevEndTime != null && isConsecutiveDay(prevEndTime, startTime)) {
                    consecutiveDays++;
                } else {
                    consecutiveDays = 1;
                }

                // Check less than 10 hours between shifts
                long hoursBetweenShifts = hoursBetween(prevEndTime, startTime);
                if (MIN_HOURS_BETWEEN_SHIFTS < hoursBetweenShifts && hoursBetweenShifts < 10) {
                    System.out.println(data.getName() + " (" + employeeId + "): Less than 10 hours between shifts");
                }

                // Check more than 14 hours in a single shift
                long shiftHours = hoursBetween(startTime, endTime);
                if (shiftHours > MAX_HOURS_IN_SINGLE_SHIFT) {
                    System.out.println(data.getName() + " (" + employeeId + "): Worked more than 14 hours in a single shift");
                }

                prevEndTime = endTime;

                // Print if worked for 7 consecutive days
                if (consecutiveDays == CONSECUTIVE_DAYS_THRESHOLD) {
                    System.out.println(data.getName() + " (" + employeeId + "): Worked for 7 consecutive days");
                }
            }
        }
    }

    private static Date parseTime(String timeStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean isConsecutiveDay(Date date1, Date date2) {
        long diffInDays = (date2.getTime() - date1.getTime()) / (24 * 60 * 60 * 1000);
        return diffInDays == 1;
    }

    private static long hoursBetween(Date startTime, Date endTime) {
        long diffInMilliseconds = endTime.getTime() - startTime.getTime();
        return diffInMilliseconds / (60 * 60 * 1000);
    }
}

class EmployeeData {
    private String name;
    private List<Shift> shifts;

    public EmployeeData(String name, List<Shift> shifts) {
        this.name = name;
        this.shifts = shifts;
    }

    public String getName() {
        return name;
    }

    public List<Shift> getShifts() {
        return shifts;
    }
}

class Shift {
    private Date startTime;
    private Date endTime;

    public Shift(Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
}
