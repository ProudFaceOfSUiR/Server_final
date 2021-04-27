package com.company.main;

import com.company.classes.Worker;
import com.company.database.DataBase;
import com.company.enums.Commands;
import com.company.network.Messages;
import com.company.network.Server;

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
        while (true){
            //connecting socket
            if (!server.connectSocket()){
                System.out.println("Reconnecting...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
                continue;
            }

            //reading commands from socket
            server.readCommands();
        }
    }
}
