package com.company.main;

import com.company.Login.User;
import com.company.database.DataBase;
import com.company.database.DataBase1;
import com.company.database.FileParser1;
import com.company.exceptions.NotConnectedException;
import com.company.network.Server;

//variant 331122

public class Main {

    public static void main(String[] args) {

        DataBase dataBase = new DataBase();
        DataBase1 dataBase1 = new DataBase1();
        dataBase1.initialize();
        //dataBase.initialize();

        dataBase.setDatabase(FileParser1.stringToDatabase());


        dataBase.show();

        Server server = new Server();
        boolean isInitialized = false;
        while (!isInitialized){
            isInitialized = server.initialize(dataBase);
            if (!isInitialized) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        boolean isConnected = server.connectSocket();
        boolean user = false;
        while (true){
            //connecting socket
            if (!isConnected){
                System.out.println("Reconnecting...");
                isConnected = server.connectSocket();
                continue;
            }
            User user1 = new User();
            //reading commands from socket
            try {
                if (!user) {
                    user1 = server.readCommand();
                    server.addUser(user1);
                    dataBase.setUser(user1);
                    user = true;
                } else if (!user1.equals(server.readCommand())){
                    user = false;
                }
                else {
                    if (!user1.equals(server.readCommand())){
                        System.out.println("wrong password");
                    }
                }
            } catch (NotConnectedException e) {
                System.out.println(e.getMessage());
                isConnected = false;
            }
        }
    }
}
