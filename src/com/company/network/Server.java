package com.company.network;

import com.company.classes.Worker;
import com.company.database.DataBase;
import com.company.enums.Commands;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    DataBase dataBase;

    private ServerSocket server;
    private Socket client;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Messages input;
    private String output;

    public boolean initialize(DataBase dataBase){
        this.dataBase = dataBase;

        try {
            this.server = new ServerSocket(1488);
            System.out.println("Server has started");
        } catch (IOException e) {
            System.out.println("Server couldn't start: " + e.getMessage());
            return false;
        }

        return true;
    }

    public boolean connectSocket(){
        //connecting socket
        try {
            this.client = server.accept();
            System.out.println("Connection accepted");
        } catch (IOException e) {
            System.out.print("Connection error: " + e.getMessage());
            return false;
        }

        //getting streams
        try {
            this.out = new ObjectOutputStream(client.getOutputStream());
            this.in = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            System.out.println("Error while getting streams: " + e.getMessage());
            return false;
        }

        return true;
    }

    protected void sendFeedback(){
        try {
            this.out.writeObject(output);
            this.out.flush();
        } catch (Exception e) {
            System.out.println("Error while sending response: " + e.getMessage());
        }
    }

    public void readCommands(){
        while (true) {
            //getting message
            try {
                this.input = (Messages) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error while reading from client: " + e.getMessage());
                return;
            }

            //getting command from messages
            Commands command;
            try {
                command = (Commands) input.getObject(0);
            } catch (IndexOutOfBoundsException e){
                System.out.println("Empty input error");
                continue;
            }
            System.out.println("Command is " + command.toString());

            switch (command) {
                case ADD:
                    this.output = this.dataBase.add((Worker) input.getObject(1));
                    break;
                case UPDATE:
                    //todo
                    //int index = (int) input.getObject(1);
                    break;
                case REMOVE_BY_ID:
                    this.output = this.dataBase.remove((String)input.getObject(1));
                    break;
                case CLEAR:
                    this.dataBase.clear();
                    this.output = "Databse was successfully cleared";
                    break;
                case EXECUTE_SCRIPT:
                    //todo
                    break;
                case EXIT:
                    //todo
                    break;
                case ADD_IF_MAX:
                    this.output = this.dataBase.addIfMax((Worker) input.getObject(1));
                    break;
                case REMOVE_GREATER:
                    this.output = this.dataBase.removeGreater((String) input.getObject(1));
                    break;
                case REMOVE_LOWER:
                    this.output = this.dataBase.removeLower((String) input.getObject(1));
                    break;
                case GROUP_COUNTING_BY_POSITION:
                    this.output = this.dataBase.groupCountingByPosition();
                    break;
                case COUNT_LESS_THAN_START_DATE:
                    this.output = this.dataBase.countLessThanStartDate((String) input.getObject(1));
                    break;
                case FILTER_GREATER_THAN_START_DATE:
                    this.output = this.dataBase.filterGreaterThanStartDate((String) input.getObject(1));
                    break;
                case FILL_FROM_FILE:
                    this.dataBase.setDatabase((LinkedList<Worker>) input.getObject(1));
                    this.output = "Database was successfully ";
                    break;
                case INFO:
                    this.output = dataBase.info();
                    break;
                case SHOW:
                    this.output = dataBase.show();
                    break;
                case HELP:
                    this.output = dataBase.help();
                    break;
                default:
                    this.output = ("Unexpected value: " + command);
            }

            sendFeedback();
        }
    }
}
