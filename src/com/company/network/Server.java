package com.company.network;

import com.company.Login.User;
import com.company.PostgreSQL.Check;
import com.company.classes.Worker;
import com.company.database.DataBase;
import com.company.database.FileParser1;
import com.company.enums.Commands;
import com.company.exceptions.NotConnectedException;
import sun.rmi.transport.proxy.CGIHandler;

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
    private User user;
    private String login;

    public void setClient(Socket client) {
        this.client = client;
    }

    public ServerSocket getServer() {
        return server;
    }

    public Socket getClient() {
        return client;
    }

    public void addUser(User user){
        this.user = new User(user);
    }

    public boolean initialize(DataBase dataBase){
        this.dataBase = dataBase;
        this.output = new Messages();
        return true;
    }

    public boolean connectSocket(){
        //connecting socket
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
            System.out.println(this.output.getObject(0));
            this.out.writeObject(this.output);
            this.out.flush();
            this.out.reset();
        } catch (Exception e) {
            System.out.println("Error while sending response: " + e.getMessage());
        }
        this.output.clear();
    }

    public User readCommand() throws NotConnectedException {
        //getting message
        User user = new User();
        try {
            this.input = (Messages) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error while reading from client: " + e.getMessage());
            throw new NotConnectedException();
        }

        //getting command from messages
        Commands command;
        try {
            command = (Commands) input.getObject(0);
        } catch (IndexOutOfBoundsException e){
            System.out.println("Empty input error");
            return null;
        }
        System.out.println("Command is " + command.toString());

        switch (command) {
            case SIGN_IN:
                if(Check.check()){
                    System.out.println("Database was initialised, all servers working nominally");
                    Check.create();
                    //User user = new User();
                    user =(User) input.getObject(1);
                    System.out.println(user.getLogin());
                    user.setPassword(User.encryptThisString(user.getPassword()));
                    if (user.isValid()&&Check.checkForMatch(user.getLogin(), user.getPassword())){
                        System.out.println(Check.checkForMatch(user.getLogin(), user.getPassword()));
                        this.output.addObject(Commands.SIGN_IN);
                        this.output.addObject(Check.signIn(user));
                        this.addUser(user);
                        this.login = this.user.getLogin();
                        System.out.println(this.user.getLogin());
                    } else {
                        this.output.addObject(Commands.SIGN_IN);
                        this.output.addObject(false);
                        this.output.addObject("Login or password is not valid");
                    }
                }
                //System.out.println("sign_in");
                //Check.delete(1);
                break;
            case SIGN_UP:
                System.out.println("sign_up");
                //User user1 = new User();
                user = (User) input.getObject(1);
                //System.out.println(user1.getLogin());
                Check.create();
                user.setPassword(User.encryptThisString(user.getPassword()));
                System.out.println(!Check.checkForMatch(user.getLogin(), user.getPassword()));
                if(!Check.checkForMatch(user.getLogin(), user.getPassword())){
                    System.out.println("ypupupuppu");
                    Check.signUp(user);
                    this.output.addObject(Commands.SIGN_UP);
                    this.output.addObject(true);
                    this.addUser(user);
                    this.login = this.user.getLogin();//&&&&
                    System.out.println(this.user.getLogin());
                } else {
                    this.output.addObject(Commands.SIGN_UP);
                    this.output.addObject(false);
                    this.output.addObject("Login or password is not valid");
                }
                break;
            case ADD:
                System.out.println();
                try {
                    if (this.user.getLogin().equals(this.login)){
                    }else{
                        this.user = new User();
                        user.setLogin(this.login);
                    }
                } catch (NullPointerException e){
                    this.user = new User();
                    user.setLogin(this.login);
                }
                System.out.println(this.user.getLogin());//????
                this.output.addObject(Commands.NO_FEEDBACK);
                this.response = this.dataBase.add((Worker) input.getObject(1),this.user);
                break;
            case UPDATE:
                this.output.addObject(Commands.NO_FEEDBACK);
                int id = (int) input.getObject(1);
                //check if worker exists
                int num = -1;
                for (int i = 0; i< this.dataBase.database.size();i++){
                    if (this.dataBase.database.get(i).getId()==id){
                        num = i;
                        System.out.println(num);
                    }
                }

                //int num = dataBase.returnIndexById(id);
                try {
                    if (true) {
                        if (dataBase.returnIndexById(num) != -1) {

                            //sending the worker
                            Messages workerToUpdate = new Messages();
                            workerToUpdate.addObject(dataBase.getWorkerByIndex(dataBase.returnIndexById(num)));
                            String log = dataBase.database.get(num).getUser().getLogin();
                            System.out.println(log);
                            try {
                                this.out.writeObject(workerToUpdate);
                                this.out.flush();
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                                return user;
                            }
                            //getting worker
                            try {
                                this.input = (Messages) this.in.readObject();
                                System.out.println("fllll");
                            } catch (IOException | ClassNotFoundException e) {
                                System.out.println(e.getMessage());
                            }
                            System.out.println("check");
                            System.out.println(this.user.getLogin());
                            System.out.println(this.dataBase.database.get((num)).getLogin());
                            if (log.equals(this.login)||this.dataBase.database.get((num)).getLogin().equals(this.user.getLogin())) {

                                this.dataBase.remove(String.valueOf(id));
                                this.dataBase.add((Worker) this.input.getObject(0), this.user);
                                this.response = "Worker has been successfully updated (server)";
                            } else {
                                System.out.println("Invalid ID");
                            }
                            } else {
                            this.response = "Invalid id";
                        }
                    }
                }catch (NullPointerException e){
                    this.response = "invalid id";
                }
                break;
            case REMOVE_BY_ID:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.response = this.dataBase.remove((String)input.getObject(1));
                break;
            case CLEAR:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.dataBase.clear();
                this.response = "Database was successfully cleared";
                break;
            case EXIT:
                //System.out.println(FileParser1.dataBaseToString(dataBase.database));
                //FileParser1 fileParser1 = new FileParser1();
                //System.out.println(this.user.getLogin());
                //System.out.println(this.login);
                System.out.println(this.login);
                System.out.println(this.user.getLogin());
                String s = FileParser1.dataBaseToString(dataBase.getDatabase(),this.login);
                System.out.println(s);
                Check.save(s, login);
                //this.dataBase.save();
                break;
            case ADD_IF_MAX:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.response = this.dataBase.addIfMax((Worker) input.getObject(1));
                break;
            case REMOVE_GREATER:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.response = this.dataBase.removeGreater((String) input.getObject(1));
                break;
            case REMOVE_LOWER:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.response = this.dataBase.removeLower((String) input.getObject(1));
                break;
            case GROUP_COUNTING_BY_POSITION:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.response = this.dataBase.groupCountingByPosition();
                break;
            case COUNT_LESS_THAN_START_DATE:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.response = this.dataBase.countLessThanStartDate((String) input.getObject(1));
                break;
            case FILTER_GREATER_THAN_START_DATE:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.response = this.dataBase.filterGreaterThanStartDate((String) input.getObject(1));
                break;
            case FILL_FROM_FILE:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.dataBase.setDatabase((LinkedList<Worker>) input.getObject(1));
                this.response = "Server database has been successfully replaced by client's";
                break;
            case INFO:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.response = dataBase.info();
                break;
            case SHOW:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.response = dataBase.show();
                break;
            case HELP:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.response = dataBase.help();
                break;
            default:
                this.output.addObject(Commands.NO_FEEDBACK);
                this.response = ("Unexpected value: " + command);
        }

        sendFeedback();
        return user;
    }
}
