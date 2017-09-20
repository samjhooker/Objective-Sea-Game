package seng302.controllers;

import seng302.data.ConnectionManager;
import seng302.data.CourseName;
import seng302.data.ServerPacketBuilder;
import seng302.data.registration.RegistrationType;
import seng302.utilities.ConnectionUtils;
import seng302.views.AvailableRace;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

import static seng302.data.registration.RegistrationType.REQUEST_RUNNING_GAMES;

import static seng302.data.AC35StreamField.HOST_GAME_CURRENT_PLAYERS;

/**
 * Created by dda40 on 11/09/17.
 *
 */
public class GameRecorder implements Observer {

    private final ServerPacketBuilder packetBuilder;
    private final ConnectionManager connectionManager;
    private ArrayList<AvailableRace> availableRaces = new ArrayList<>();
    private int nextHostID = 0;
    private Set<Socket> sockets = new HashSet<>();

    public GameRecorder() throws IOException {
        packetBuilder = new ServerPacketBuilder();
        connectionManager = new ConnectionManager(ConnectionUtils.getGameRecorderPort(), false);
        connectionManager.addObserver(this);
        System.out.println("Server: Waiting for races");
        Thread managerThread = new Thread(connectionManager);
        managerThread.setName("Connection Manager");
        managerThread.start();
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (observable.equals(connectionManager)) {
            if (arg instanceof Socket) {
                Socket socket = (Socket) arg;
                sockets.add(socket);
                try {
                    startServerListener((Socket) arg);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else if(observable instanceof ServerListener){
            if(arg instanceof String) {
                removeAvailableRace(arg);
            } else if (arg instanceof RegistrationType) {
                if (arg.equals(REQUEST_RUNNING_GAMES)) {
                    respondToRequestForGames((ServerListener) observable);
                }
            } else if (arg instanceof AvailableRace) {
                updateAvailableRace(((AvailableRace) arg));
            }
        }
    }

    /**
     * Runs through the entire list of avaliable races, updating the ones that have had changes
     * @param newRace the new avaliable race
     */
    private void updateAvailableRace(AvailableRace newRace){
        boolean updatedRace = false;
        for (AvailableRace runningRace : availableRaces){
            if (Objects.equals(runningRace.getIpAddress(), newRace.getIpAddress())){
                System.out.println("Game Recorder: Updating running race");
                updatedRace = true;
                updateNumberOfBoats(runningRace, newRace.getNumBoats());
            }
        }
        int raceMapIndex = CourseName.getCourseIntFromName(newRace.mapNameProperty().getValue());
        if (!updatedRace && raceMapIndex != -1) {
            System.out.println("Game Recorder: Recording new server");
            updateNumberOfBoats(newRace, newRace.getNumBoats());
            availableRaces.add(newRace);
        }
    }

    /**
     * changes the number of boats in a known race to be stored on the vm
     * @param race the new race
     * @param numBoats the new number of boats in the race
     */
    private void updateNumberOfBoats(AvailableRace race, int numBoats){
        byte[] packet = race.getPacket();
        packet[HOST_GAME_CURRENT_PLAYERS.getStartIndex()] = (byte) numBoats;
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
            System.out.println("Game Recorder: removed canceled race: " + foundRace.getIpAddress());
        }
    }

    /**
     * Respond to a request for running games
     * @param serverListener the serverListener with the clients socket
     */
    private void respondToRequestForGames(ServerListener serverListener) {
        connectionManager.addMainMenuConnection(nextHostID, serverListener.getSocket());
        for(AvailableRace race : availableRaces) {
            byte[] racePacket = packetBuilder.createGameRegistrationPacket(race.getPacket());
            connectionManager.sendToClient(nextHostID, racePacket);
        }
        nextHostID++;
    }

    /**
     * Starts a new server listener on new thread for which listens to a client
     * @param socket the socket for the client
     */
    protected void startServerListener(Socket socket) throws IOException {
        ServerListener serverListener = new ServerListener(socket);
        Thread serverListenerThread = new Thread(serverListener);
        serverListenerThread.setName("Game Recorder Listener");
        serverListenerThread.start();
        serverListener.addObserver(this);
    }
}
