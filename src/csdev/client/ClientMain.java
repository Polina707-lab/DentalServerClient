package csdev.client;

import java.io.*;
import java.net.*;
import java.util.*;
import csdev.*;

public class ClientMain {

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java csdev.client.ClientMain <Name> [host]");
            waitKey();
            return;
        }

        try (Socket socket = (args.length == 1
                ? new Socket(/*InetAddress.getLocalHost()*/InetAddress.getByName("DESKTOP-PUTBKDN"), Protocol.PORT)
                : new Socket(args[1], Protocol.PORT))) {

            System.err.println("Connected to server.");
            System.out.println("ser name "+socket.getInetAddress().getHostName());
            session(socket, args[0]);

        } catch (Exception e) {
            System.err.println("Client error: " + e);
        } finally {
            System.err.println("Exit...");
        }
    }

    static void waitKey() {
        System.err.println("Press Enter to exit...");
        try { System.in.read(); } catch (IOException e) {}
    }

    static void session(Socket socket, String clientName) {
        try (Scanner in = new Scanner(new InputStreamReader(System.in, "UTF-8"));
             ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream is = new ObjectInputStream(socket.getInputStream())) {

            if (!connect(clientName, os, is)) return;

            while (true) {
                printMenu();
                if (!in.hasNextLine()) {
                    System.out.println("No input detected. Exiting client...");
                    break;
                }
                String cmd = in.nextLine().trim();

                switch (cmd) {
                    case "1": requestSlots(os, is); break;
                    case "2": makeAppointment(os, is, in); break;
                    case "3": disconnect(os); return;
                    case "4": showAll(os, is); break;
                    case "5": cancelAppointment(os, is, in); break;
                    default: System.out.println("Invalid input.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Session error: " + e);
        }
    }

    static boolean connect(String name, ObjectOutputStream os, ObjectInputStream is)
            throws IOException, ClassNotFoundException {

        os.writeObject(new MessageConnect(name));
        os.flush();
        MessageConnectResult res = (MessageConnectResult) is.readObject();

        if (res.Error()) {
            System.err.println("Connection error: " + res.getErrorMessage());
            return false;
        }
        System.out.println("Welcome, " + name + "!");
        return true;
    }

    static void disconnect(ObjectOutputStream os) throws IOException {
        os.writeObject(new MessageDisconnect());
        os.flush();
        System.out.println("You have disconnected from the server.");
    }

    static void requestSlots(ObjectOutputStream os, ObjectInputStream is)
            throws IOException, ClassNotFoundException {

        os.writeObject(new MessageSlotsRequest());
        os.flush();
        MessageSlotsResult res = (MessageSlotsResult) is.readObject();

        if (res.Error()) System.err.println("Error: " + res.getErrorMessage());
        else if (res.availableSlots == null || res.availableSlots.length == 0)
            System.out.println("No free slots available.");
        else {
            System.out.println("Available slots:");
            for (String slot : res.availableSlots)
                System.out.println("  - " + slot);
        }
    }

    static void makeAppointment(ObjectOutputStream os, ObjectInputStream is, Scanner in)
            throws IOException, ClassNotFoundException {

        os.writeObject(new MessageSlotsRequest());
        os.flush();
        MessageSlotsResult res = (MessageSlotsResult) is.readObject();

        if (res.Error() || res.availableSlots == null || res.availableSlots.length == 0) {
            System.out.println("No free slots available.");
            return;
        }

        System.out.println("Available slots:");
        for (String s : res.availableSlots)
            System.out.println("  - " + s);

        System.out.print("Enter one of them (e.g., Mon 09:00): ");
        if (!in.hasNextLine()) return;
        String time = in.nextLine().trim();
        time = time.replaceAll("\\b(\\d):", "0$1:");

        System.out.println("-> You selected: [" + time + "]");

        boolean exists = Arrays.asList(res.availableSlots).contains(time);
        if (!exists) {
            System.out.println("--- Invalid slot. Try again. ---");
            return;
        }

        System.out.print("Enter full name: ");
        if (!in.hasNextLine()) return;
        String name = in.nextLine().trim();

        System.out.print("Enter phone number: ");
        if (!in.hasNextLine()) return;
        String phone = in.nextLine().trim();

        System.out.print("Enter complaint: ");
        if (!in.hasNextLine()) return;
        String complaint = in.nextLine().trim();

        MessageAppointmentRequest req = new MessageAppointmentRequest(time, name, phone, complaint);
        System.out.println("-> Sending appointment request...");
        os.writeObject(req);
        os.flush();

        MessageAppointmentResult res2 = (MessageAppointmentResult) is.readObject();

        if (res2.Error())
            System.err.println("Error: " + res2.getErrorMessage());
        else
            System.out.println("--- Appointment successfully created! ---");
    }

    static void showAll(ObjectOutputStream os, ObjectInputStream is)
            throws IOException, ClassNotFoundException {
        os.writeObject(new MessageShowAll());
        os.flush();
        MessageShowAllResult res = (MessageShowAllResult) is.readObject();

        if (res.Error()) System.err.println("Error: " + res.getErrorMessage());
        else if (res.allAppointments == null || res.allAppointments.length == 0)
            System.out.println("No appointments yet.");
        else {
            System.out.println("All appointments:");
            for (String s : res.allAppointments)
                System.out.println("  - " + s);
        }
    }

    static void cancelAppointment(ObjectOutputStream os, ObjectInputStream is, Scanner in)
            throws IOException, ClassNotFoundException {

        System.out.print("Enter appointment time to cancel (e.g., Mon 09:30): ");
        if (!in.hasNextLine()) return;
        String time = in.nextLine().trim();

        os.writeObject(new MessageCancelAppointment(time));
        os.flush();

        MessageCancelResult res = (MessageCancelResult) is.readObject();
        if (res.Error())
            System.err.println("Error: " + res.getErrorMessage());
        else
            System.out.println("Appointment successfully cancelled.");
    }

    static void printMenu() {
        System.out.println("\nSelect an action:");
        System.out.println("1. Show available slots");
        System.out.println("2. Make an appointment");
        System.out.println("3. Exit");
        System.out.println("4. Show all appointments (admin)");
        System.out.println("5. Cancel appointment");
        System.out.print("Your choice: ");
    }
}
