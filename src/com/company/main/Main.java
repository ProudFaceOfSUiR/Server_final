package com.company.main;

import com.company.classes.Worker;
import com.company.database.DataBase;
import com.company.enums.Commands;
import com.company.network.Messages;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {

        DataBase dataBase = new DataBase();
        dataBase.initialize();

        ServerSocket server = null;
        try {
            server = new ServerSocket(1488);
            System.out.println("Server has started");
        } catch (IOException e) {
            System.out.println("Server couldn't start");
            System.out.println(e.getMessage());
            System.exit(1);
        }

        Socket client = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Messages input = null;
        String output = null;

        loop: while (true) {
            //connecting socket
            try {
                client = server.accept();
                System.out.println("Connection accepted");
            } catch (IOException e) {
                System.out.print("Connection error: " + e.getMessage());
                System.out.println("Reconnecting...");
                continue;
            }

            System.out.println();

            try {
                out = new ObjectOutputStream(client.getOutputStream());
                in = new ObjectInputStream(client.getInputStream());
            } catch (IOException e) {
                System.out.println("Error while getting streams: " + e.getMessage());
                continue;
            }
            
            while (true) {
                try {
                    input = (Messages) in.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error while reading from client: " + e.getMessage());
                    continue loop;
                }

                Commands command = (Commands) input.getObject(0);

                System.out.println("Command is " + command.toString());

                switch (command){
                    case ADD:
                        dataBase.add((Worker) input.getObject(1));
                        output = "Worker has been successfully added";
                        break;
                    case UPDATE:
                        //int index = (int) input.getObject(1);
                        break;
                    case REMOVE_BY_ID:
                        break;
                    case CLEAR:
                        dataBase.clear();
                        output = "Databse was successfully cleared";
                        break;
                    case EXECUTE_SCRIPT:
                        break;
                    case EXIT:
                        break;
                    case ADD_IF_MAX:
                        break;
                    case REMOVE_GREATER:
                        break;
                    case REMOVE_LOWER:
                        break;
                    case GROUP_COUNTING_BY_POSITION:
                        break;
                    case COUNT_LESS_THAN_START_DATE:
                        break;
                    case FILTER_GREATER_THAN_START_DATE:
                        break;
                    case FILL_FROM_FILE:
                        dataBase.setDatabase((LinkedList<Worker>) input.getObject(1));
                        output = "Database was successfully ";
                        break;
                    case INFO:
                        output = dataBase.info();
                        break;
                    case SHOW:
                        output = dataBase.show();
                        System.out.println(output);
                        break;
                    case HELP:
                        output = dataBase.help();
                        break;
                }

                try {
                    out.writeObject(output);
                    out.flush();
                } catch (Exception e) {
                    System.out.println("Error while sending response: " + e.getMessage());
                }
            }
        }
    }
}
