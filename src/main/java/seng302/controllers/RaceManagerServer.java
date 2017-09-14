package seng302.controllers;

import seng302.data.ConnectionManager;
import seng302.data.CourseName;
import seng302.data.ServerPacketBuilder;
import seng302.data.registration.RegistrationType;
import seng302.utilities.ConnectionUtils;
import seng302.views.AvailableRace;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

import static seng302.data.registration.RegistrationType.REQUEST_RUNNING_GAMES;

import static seng302.data.AC35StreamField.HOST_GAME_CURRENT_PLAYERS;

/**
 * Created by dda40 on 11/09/17.
 *
 */
public class RaceManagerServer implements Observer {

    private final ServerPacketBuilder packetBuilder;
    private final ConnectionManager connectionManager;
    private ArrayList<AvailableRace> availableRaces = new ArrayList<>();
    private int nextHostID = 0;
    private int serverListenerId = 0;
    private Map<Integer, Thread> listenerThreads;
    private Set<Socket> sockets = new HashSet<>();

    public RaceManagerServer() throws IOException {
        packetBuilder = new ServerPacketBuilder();
        connectionManager = new ConnectionManager(ConnectionUtils.getVmPort(), false);
        connectionManager.addObserver(this);
        System.out.println("Server: Waiting for races");
        listenerThreads = new HashMap<>();
        Thread managerThread = new Thread(connectionManager);
        managerThread.setName("Connection Manager");
        managerThread.start();
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (observable.equals(connectionManager)) {
            if (arg instanceof Socket) {
                Socket socket = (Socket) arg;
                if(sockets.contains(socket)){
                    System.out.println("this is bad");
                } else{
                    sockets.add(socket);
                    try {
                        startServerListener((Socket) arg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if(observable instanceof ServerListener){
            ServerListener serverListener = (ServerListener) observable;
            if(arg instanceof String) {
                removeAvailableRace(arg);
            } else if (arg instanceof RegistrationType) {
                if (arg.equals(REQUEST_RUNNING_GAMES)) {
                    System.out.println("hi");
                    manageRegistration((ServerListener) observable);
                }
            } else if (arg instanceof AvailableRace) {
                System.out.println("received race");
                updateAvailableRace(((AvailableRace) arg));
            }
            Thread listenerThread = listenerThreads.get(serverListener.getListenerId());
//            serverListener.stop();
            listenerThread.interrupt();
            listenerThreads.remove(serverListener.getListenerId());
//            System.out.println("removed" + serverListener.getListenerId());
        }
    }

    /**
     * Runs through the entire list of avaliable races, updating the ones that have had changes
     * @param race the new avaliable race
     */
    private void updateAvailableRace(AvailableRace race){
        boolean updatedRace = false;
        for (AvailableRace runningRace : availableRaces){
            if (Objects.equals(runningRace.getIpAddress(), race.getIpAddress())){
                System.out.println("VmServer: Updating running race");
                updatedRace = true;
                incrementNumberOfBoats(runningRace, race.getNumBoats());
            }
        }
        int raceMapIndex = CourseName.getCourseIntFromName(race.mapNameProperty().getValue());
        System.out.println(race.mapNameProperty().getValue());
        if (!updatedRace && raceMapIndex != -1) {
            System.out.println("VmServer: Recording game on VM");
            incrementNumberOfBoats(race, 1);
            availableRaces.add(race);
        }
    }

    /**
     * changes the number of boats in a known race to be stored on the vm
     * @param race the new race
     * @param numBoats the new number of boats in the race
     */
    private void incrementNumberOfBoats(AvailableRace race, int numBoats){
        byte[] packet = race.getPacket();
        for (int i = 0; i < 1; i ++) {
            packet[HOST_GAME_CURRENT_PLAYERS.getStartIndex() + i] = (byte) (numBoats >> i * 8);
        }
    }

    /**
     * removes a race with a specific IP address
     * @param ipAddress ip address of the race to remove
     */
    private void removeAvailableRace(Object ipAddress){
        AvailableRace foundRace = null;
        for (AvailableRace race : availableRaces) {
            if (race.getIpAddress().equals(ipAddress)) {
                foundRace = race;
            }
        }
        if (foundRace != null) {
            availableRaces.remove(foundRace);
            System.out.println("VmServer: removed canceled race: " + foundRace.getIpAddress());
        }
    }

    /**
     * Deal with the different types of registrations clients can attempt to connect with
     * Currently ignores GHOST or TUTORIAL connection attempts.
     * @param serverListener the serverListener with the clients socket
     */
    private void manageRegistration(ServerListener serverListener) {
        connectionManager.addMainMenuConnection(nextHostID, serverListener.getSocket());
        for(AvailableRace race : availableRaces){
            byte[] racePacket = packetBuilder.createGameRegistrationPacket(race.getPacket());
            connectionManager.sendToClient(nextHostID, racePacket);
            System.out.println(racePacket.toString());
        }
        nextHostID++;
    }

    /**
     * Starts a new server listener on new thread for which listens to a client
     * @param socket the socket for the client
     */
    protected void startServerListener(Socket socket) throws IOException {
        ServerListener serverListener = new ServerListener(serverListenerId, socket);
        Thread serverListenerThread = new Thread(serverListener);
        serverListenerThread.setName("Server Listener" + serverListenerId);
        serverListenerThread.start();
        serverListener.addObserver(this);
        listenerThreads.put(serverListenerId, serverListenerThread);
        serverListenerId++;
    }
}
