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
    private Messages output;
    private String response;

    public ServerSocket getServer() {
        return server;
    }

    public Socket getClient() {
        return client;
    }

    public boolean initialize(DataBase dataBase){
        this.dataBase = dataBase;
        this.output = new Messages();

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
        this.output.addObject(response);
        try {
            this.out.writeObject(this.output);
            this.out.flush();
            this.out.reset();
        } catch (Exception e) {
            System.out.println("Error while sending response: " + e.getMessage());
        }
        this.output.clear();
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
                    this.response = this.dataBase.add((Worker) input.getObject(1));
                    break;
                case UPDATE:
                    long id = (long) input.getObject(1);
                    //check if worker exists
                    if (dataBase.returnIndexById(id) != -1){
                        //sending the worker
                        Messages workerToUpdate = new Messages();
                        workerToUpdate.addObject(dataBase.getWorkerByIndex(dataBase.returnIndexById(id)));
                        try {
                            this.out.writeObject(workerToUpdate);
                            this.out.flush();
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                            return;
                        }
                        //getting worker
                        try {
                            this.input = (Messages) this.in.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            System.out.println(e.getMessage());
                        }
                        this.dataBase.remove(String.valueOf(id));
                        this.dataBase.add((Worker) this.input.getObject(0));
                        this.response = "Worker has been successfully updated (server)";
                    } else {
                        this.response = "Invalid id";
                    }
                    break;
                case REMOVE_BY_ID:
                    this.response = this.dataBase.remove((String)input.getObject(1));
                    break;
                case CLEAR:
                    this.dataBase.clear();
                    this.response = "Databse was successfully cleared";
                    break;
                case EXECUTE_SCRIPT:
                    //todo
                    break;
                case EXIT:
                    this.dataBase.save();
                    break;
                case ADD_IF_MAX:
                    this.response = this.dataBase.addIfMax((Worker) input.getObject(1));
                    break;
                case REMOVE_GREATER:
                    this.response = this.dataBase.removeGreater((String) input.getObject(1));
                    break;
                case REMOVE_LOWER:
                    this.response = this.dataBase.removeLower((String) input.getObject(1));
                    break;
                case GROUP_COUNTING_BY_POSITION:
                    this.response = this.dataBase.groupCountingByPosition();
                    break;
                case COUNT_LESS_THAN_START_DATE:
                    this.response = this.dataBase.countLessThanStartDate((String) input.getObject(1));
                    break;
                case FILTER_GREATER_THAN_START_DATE:
                    this.response = this.dataBase.filterGreaterThanStartDate((String) input.getObject(1));
                    break;
                case FILL_FROM_FILE:
                    this.dataBase.setDatabase((LinkedList<Worker>) input.getObject(1));
                    this.response = "Server database successfully merged with the client's";
                    break;
                case INFO:
                    this.response = dataBase.info();
                    break;
                case SHOW:
                    this.response = dataBase.show();
                    break;
                case HELP:
                    this.response = dataBase.help();
                    break;
                default:
                    this.response = ("Unexpected value: " + command);
            }

            sendFeedback();
        }
    }
}
