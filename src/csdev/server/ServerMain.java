package csdev.server;

import java.io.*;
import java.net.*;
import java.util.*;
import csdev.*;

public class ServerMain {

    public static Map<String, Appointment> schedule = new HashMap<>();
    private static final String FILE_NAME = "data/appointments.dat";

    public static void main(String[] args) {
        loadSchedule();

        try (ServerSocket server = new ServerSocket(Protocol.PORT)) {
            System.err.println("Dental appointment server started on port " + Protocol.PORT);
            System.err.println("Press Ctrl+C to stop.");

            while (true) {
                Socket socket = server.accept();
                System.err.println("Client connected: " + socket.getInetAddress().getHostName());
                new ServerThread(socket).start();
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e);
        } finally {
            saveSchedule();
        }
    }

    
    public static synchronized boolean isSlotFree(String datetime) {
        return !schedule.containsKey(datetime);
    }

    
    public static synchronized void addAppointment(Appointment a) {
        schedule.put(a.datetime, a);
        saveSchedule();
    }

    
    private static List<String> weeklySlots = null;

    public static synchronized String[] getFreeSlots() {
        if (weeklySlots == null) {
            weeklySlots = generateWeeklySlots(); // 1 раз за запуск
        }
        return weeklySlots.stream()
                .filter(slot -> !schedule.containsKey(slot))
                .toArray(String[]::new);
    }

    private static List<String> generateWeeklySlots() {
        List<String> allSlots = new ArrayList<>();
        Random rnd = new Random();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : days) {
            int start = 8 + rnd.nextInt(3);
            int count = 3 + rnd.nextInt(4);
            int step = 1 + rnd.nextInt(2);
            for (int i = 0; i < count; i++) {
                int hour = start + i * step;
                int min = rnd.nextBoolean() ? 0 : 30;
                allSlots.add(String.format("%s %02d:%02d", d, hour, min));
            }
        }
        return allSlots;
    }

   
    public static synchronized String[] getAllAppointments() {
        return schedule.values().stream()
                .map(Appointment::toString)
                .toArray(String[]::new);
    }

    
    public static synchronized void saveSchedule() {
        try {
            File f = new File(FILE_NAME);
            f.getParentFile().mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
                oos.writeObject(schedule);
            }
            System.err.println("Schedule saved (" + schedule.size() + " records).");
        } catch (IOException e) {
            System.err.println("Error saving schedule: " + e.getMessage());
        }
    }

    
    @SuppressWarnings("unchecked")
    public static synchronized void loadSchedule() {
        File f = new File(FILE_NAME);
        if (!f.exists()) {
            System.err.println("Schedule file not found, creating new one.");
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            schedule = (Map<String, Appointment>) ois.readObject();
            System.err.println("Schedule loaded (" + schedule.size() + " records).");
        } catch (Exception e) {
            System.err.println("Error loading schedule: " + e.getMessage());
            schedule = new HashMap<>();
        }
    }
}
