package seng302;


import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import seng302.controllers.MockRaceRunner;
import seng302.data.AC35StreamMessage;
import seng302.data.BoatStatus;
import seng302.data.MockStream;
import seng302.data.RaceStatus;
import seng302.models.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;
import static seng302.data.AC35StreamField.SPEED_OVER_GROUND;


public class MockStreamTest {

    private MockRaceRunner mockRaceRunner;
    private MockStream mockStream;
    private Socket connectionSocket;
    private Thread upStream;
    private static int i = 0;

    @Before
    public void startMockRaceRunner() throws IOException {
        mockRaceRunner = new MockRaceRunner();
        Thread runner = new Thread(mockRaceRunner);
        runner.start();

        mockStream = new MockStream(2829 + i, mockRaceRunner);
        mockRaceRunner.setScaleFactor(50);

        upStream = new Thread(mockStream);
        upStream.start();
        connectionSocket = new Socket("localhost", 2829 + i);
        i++;
    }


    @Test
    public void checkUpstreamIsSending(){

        try {

            assertEquals(71, connectionSocket.getInputStream().read());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void sendRaceXmlTest(){
        try {

//            Socket connectionSocket = new Socket("localhost", 2829);
            InputStream stream = connectionSocket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(stream);
            byte[] header = new byte[15];
            dataInputStream.readFully(header);
            int length = ((header[14] & 0xFF) << 8) + (header[13] & 0xFF);
            byte[] body = new byte[length];
            dataInputStream.readFully(body);
            FileWriter outputFileWriter = new FileWriter("testRace.xml");

            BufferedWriter bufferedWriter = new BufferedWriter(outputFileWriter);
            bufferedWriter.write(new String(Arrays.copyOfRange(body, 14, body.length)));
            bufferedWriter.close();

            String receivedStrPath = new File("testRace.xml").getAbsolutePath();
            Path receivedPath = Paths.get(receivedStrPath);
            List<String> receivedContent = Files.readAllLines(receivedPath);

            String raceStrPath = new File("src/main/resources/defaultFiles/Race.xml").getAbsolutePath();
            Path racePath = Paths.get(raceStrPath);
            List<String> raceBodyContent = Files.readAllLines(racePath);
            for (int i = 0; i < receivedContent.size(); i++){
                assertEquals(receivedContent.get(i).trim(), raceBodyContent.get(i).trim());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void sendBodyXmlTest(){
        try {

//            Socket connectionSocket = new Socket("localhost", 2829);
            InputStream stream = connectionSocket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(stream);

            byte[] header = new byte[15];
            dataInputStream.readFully(header);
            int length = ((header[14] & 0xFF) << 8) + (header[13] & 0xFF);
            dataInputStream.readFully(new byte[length]);
            dataInputStream.readFully(new byte[4]); //read crc

            byte[] boatHeader = new byte[15];
            dataInputStream.readFully(boatHeader);
            int boatLength = ((boatHeader[14] & 0xFF) << 8) + (boatHeader[13] & 0xFF);
            byte[] body = new byte[boatLength];
            dataInputStream.readFully(body);
            FileWriter outputFileWriter = new FileWriter("testBoat.xml");

            BufferedWriter bufferedWriter = new BufferedWriter(outputFileWriter);
            bufferedWriter.write(new String(Arrays.copyOfRange(body, 14, body.length)));
            bufferedWriter.close();

            String receivedStrPath = new File("testBoat.xml").getAbsolutePath();
            Path receivedPath = Paths.get(receivedStrPath);
            List<String> receivedContent = Files.readAllLines(receivedPath);

            String boatStrPath = new File("src/main/resources/defaultFiles/Boat.xml").getAbsolutePath();
            Path boatPath = Paths.get(boatStrPath);
            List<String> boatBodyContent = Files.readAllLines(boatPath);
            for (int i = 0; i < receivedContent.size(); i++){
                assertEquals(boatBodyContent.get(i).trim(), receivedContent.get(i).trim());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readUtilMessageType(DataInputStream dataInputStream, int type) throws IOException {
        boolean passMarkType = false;
        byte[] header = new byte[15];
        while (!passMarkType){
            dataInputStream.readFully(header);
            if (header[2] == type){
                passMarkType = true;
            } else {
                int length = ((header[14] & 0xFF) << 8) + (header[13] & 0xFF);
                dataInputStream.readFully(new byte[length]);
                dataInputStream.readFully(new byte[4]);
            }
        }
    }


    @Ignore
    @Test
    public void sendBoatLocationTest(){

        try {
            MockRaceRunner mockRaceRunner = mock(MockRaceRunner.class);

            Race mockRace = mock(Race.class);

            when(mockRaceRunner.getRace()).thenReturn(mockRace);
            when(mockRaceRunner.getRaceId()).thenReturn(String.valueOf(1122));
            when(mockRace.getRaceStatus()).thenReturn(RaceStatus.STARTED);

            Boat boat = new Boat(1, "NZ", "NZ", 20);
            when(mockRace.getCompetitors()).thenReturn(new ArrayList<>(Arrays.asList(boat)));

            //Socket connectionSocket = new Socket("localhost", 2829);
            InputStream stream = null;
            stream = connectionSocket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(stream);
            readUtilMessageType(dataInputStream, 37);
            byte[] body = new byte[56];
            dataInputStream.readFully(body);
            //boat.setStatus(BoatStatus.FINISHED);

            assertEquals(1, body[0]);
            assertEquals(1, body[15]);
            assertEquals(101, body[7]);
            assertEquals(0, body[24]);
            assertEquals(0, body[28]);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Ignore
    @Test
    public void sendMarkRoundedTest(){
        try {

            InputStream stream = connectionSocket.getInputStream();

            DataInputStream dataInputStream = new DataInputStream(stream);
            readUtilMessageType(dataInputStream, 38);
            byte[] body = new byte[21];
            dataInputStream.readFully(body);
            assertEquals(1, body[0]);
            assertEquals(101, body[13]);
            assertEquals(0, body[18]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void sendRaceStatusTest(){
        try {

//            Socket connectionSocket = new Socket("localhost", 2829);
            InputStream stream = null;
            stream = connectionSocket.getInputStream();

            DataInputStream dataInputStream = new DataInputStream(stream);
            boolean passMarkType = false;
            byte[] header = new byte[15];
            while (!passMarkType){

                dataInputStream.readFully(header);
                if (header[2] == 12){
                    passMarkType = true;
                } else {
                    int length = ((header[14] & 0xFF) << 8) + (header[13] & 0xFF);
                    dataInputStream.readFully(new byte[length]);
                    dataInputStream.readFully(new byte[4]);
                }
            }

            byte[] body = new byte[24+(26*6)];
            dataInputStream.readFully(body);

            assertEquals(2, body[0]);
            assertEquals(2, body[23]);
            assertEquals(0, body[30]); //leg number

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void closeOpened() throws IOException {
        mockStream.stop();
        while (mockStream.isSending()) {
        }
        //connectionSocket.getInputStream().close();
        //cconnectionSocket.close();



    }

}
