package com.company.main;

import com.company.database.DataBase;
import com.company.exceptions.NotConnectedException;
import com.company.network.Server;

public class Main {

    public static void main(String[] args) {

        DataBase dataBase = new DataBase();
        dataBase.initialize();

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
        while (true){
            //connecting socket
            if (!isConnected){
                System.out.println("Reconnecting...");
                isConnected = server.connectSocket();
                continue;
            }

            //reading commands from socket
            try {
                server.readCommand();
            } catch (NotConnectedException e) {
                System.out.println(e.getMessage());
                isConnected = false;
            }
        }
    }
}
