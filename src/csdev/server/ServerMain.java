package csdev.server;

import java.io.*;
import java.net.*;
import java.util.*;
import csdev.*;

public class ServerMain {

    public static Map<String, Appointment> schedule = new HashMap<>();

    private static final String FILE_APPOINTMENTS = "data/appointments.dat";
    private static final String FILE_SLOTS = "data/slots.dat";

    private static List<String> weeklySlots = null;

    public static void main(String[] args) {

        loadSchedule();      // Load existing appointments
        loadOrGenerateSlots(); // Load weekly slots OR generate once

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


    private static void loadOrGenerateSlots() {
        weeklySlots = loadSlots();
        if (weeklySlots == null) {
            System.err.println("No slot file found — generating weekly schedule...");
            weeklySlots = generateWeeklySlots();
            saveSlots(weeklySlots);
        } else {
            System.err.println("Weekly slots loaded (" + weeklySlots.size() + " items).");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> loadSlots() {
        File f = new File(FILE_SLOTS);
        if (!f.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<String>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error loading slots: " + e.getMessage());
            return null;
        }
    }
    

    private static void saveSlots(List<String> slots) {
        try {
            File f = new File(FILE_SLOTS);
            f.getParentFile().mkdirs();

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
                oos.writeObject(slots);
            }
            System.err.println("Weekly slots saved (" + slots.size() + ").");
        } catch (IOException e) {
            System.err.println("Error saving slots: " + e.getMessage());
        }
    }

    
    
    private static List<String> generateWeeklySlots() {
        List<String> allSlots = new ArrayList<>();
        Random rnd = new Random();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (String d : days) {
            int start = 8 + rnd.nextInt(3);     // random start hour
            int count = 3 + rnd.nextInt(2);     // number of slots
            int step = 1 + rnd.nextInt(2);      // 1–2 hours apart

            for (int i = 0; i < count; i++) {
                int hour = start + i * step;
                int min = rnd.nextBoolean() ? 0 : 30;

                allSlots.add(String.format("%s %02d:%02d", d, hour, min));
            }
        }
        return allSlots;
    }

    /** Return only free slots = weeklySlots - schedule */
    public static synchronized String[] getFreeSlots() {
        return weeklySlots.stream()
                .filter(slot -> !schedule.containsKey(slot))
                .toArray(String[]::new);
    }

    
    

    public static synchronized boolean isSlotFree(String datetime) {
        return !schedule.containsKey(datetime);
    }

    public static synchronized void addAppointment(Appointment a) {
        schedule.put(a.datetime, a);
        saveSchedule();
    }

    public static synchronized String[] getAllAppointments() {
        return schedule.values().stream()
                .map(Appointment::toString)
                .toArray(String[]::new);
    }

    
    
    public static synchronized void saveSchedule() {
        try {
            File f = new File(FILE_APPOINTMENTS);
            f.getParentFile().mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
                oos.writeObject(schedule);
            }
            System.err.println("Appointments saved (" + schedule.size() + " records).");
        } catch (IOException e) {
            System.err.println("Error saving appointments: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized void loadSchedule() {
        File f = new File(FILE_APPOINTMENTS);
        if (!f.exists()) {
            System.err.println("Appointment file not found — starting fresh.");
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            schedule = (Map<String, Appointment>) ois.readObject();
            System.err.println("Appointments loaded (" + schedule.size() + " records).");
        } catch (Exception e) {
            System.err.println("Error loading appointments: " + e.getMessage());
            schedule = new HashMap<>();
        }
    }
}
