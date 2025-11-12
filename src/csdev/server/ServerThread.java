package csdev.server;

import java.io.*;
import java.net.Socket;
import csdev.*;

public class ServerThread extends Thread {

    private Socket sock;
    private ObjectInputStream is;
    private ObjectOutputStream os;
    private String clientName;
    private boolean connected = false;

    public ServerThread(Socket s) throws IOException {
        sock = s;
        os = new ObjectOutputStream(sock.getOutputStream());
        is = new ObjectInputStream(sock.getInputStream());
        this.setDaemon(true);
    }

    public void run() {
        try {
            while (true) {
                Message msg = null;
                try {
                    msg = (Message) is.readObject();
                } catch (EOFException e) {
                    break;
                } catch (ClassNotFoundException e) {
                    continue;
                }

                if (msg == null) continue;

                switch (msg.getID()) {
                    case Protocol.CMD_CONNECT:
                        connect((MessageConnect) msg);
                        break;

                    case Protocol.CMD_GET_SLOTS:
                        sendSlots();
                        break;

                    case Protocol.CMD_APPOINTMENT:
                        makeAppointment((MessageAppointmentRequest) msg);
                        break;

                    case Protocol.CMD_DISCONNECT:
                        disconnect();
                        return;

                    case Protocol.CMD_SHOW_ALL:
                        showAllAppointments();
                        break;
                    case Protocol.CMD_CANCEL_APPOINTMENT:
                        cancelAppointment((MessageCancelAppointment) msg);
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Connection lost: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void connect(MessageConnect msg) throws IOException {
        clientName = msg.clientName;
        connected = true;
        os.writeObject(new MessageConnectResult());
        System.err.println("Client connected: " + clientName);
    }

    private void sendSlots() throws IOException {
        String[] freeSlots = ServerMain.getFreeSlots();
        os.writeObject(new MessageSlotsResult(freeSlots));
    }

    private void makeAppointment(MessageAppointmentRequest msg) throws IOException {
        if (ServerMain.isSlotFree(msg.datetime)) {
            Appointment a = new Appointment(msg.datetime, msg.fullName, msg.phone, msg.complaint);
            ServerMain.addAppointment(a);
            os.writeObject(new MessageAppointmentResult());
            System.err.println("Appointment created: " + a);
        } else {
            os.writeObject(new MessageAppointmentResult("Time " + msg.datetime + " is already taken!"));
        }
    }
    
    private void cancelAppointment(MessageCancelAppointment msg) throws IOException {
        synchronized (ServerMain.class) {
            if (ServerMain.schedule.containsKey(msg.datetime)) {
                ServerMain.schedule.remove(msg.datetime);
                ServerMain.saveSchedule();
                os.writeObject(new MessageCancelResult());
                System.err.println("Appointment cancelled: " + msg.datetime);
            } else {
                os.writeObject(new MessageCancelResult("Appointment not found for " + msg.datetime));
            }
        }
    }


    private void disconnect() {
        if (connected) {
            System.err.println("Client disconnected: " + clientName);
            connected = false;
        }
        try {
            is.close();
            os.close();
            sock.close();
        } catch (IOException e) { }
    }

    private void showAllAppointments() throws IOException {
        String[] all = ServerMain.getAllAppointments();
        os.writeObject(new MessageShowAllResult(all));
    }
}
