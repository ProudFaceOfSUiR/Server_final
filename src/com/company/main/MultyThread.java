package com.company.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import com.company.Login.User;
import com.company.database.DataBase;
import com.company.database.DataBase1;
import com.company.database.FileParser1;
import com.company.exceptions.NotConnectedException;
import com.company.network.Server;


public class MultyThread implements Runnable {
    private static Socket clientDialog;

    public MultyThread(Socket client) {
        MultyThread.clientDialog = client;
    }

    @Override
    public void run() {

        try {
            // инициируем каналы общения в сокете, для сервера

            // канал записи в сокет следует инициализировать сначала канал чтения для избежания блокировки выполнения программы на ожидании заголовка в сокете
            DataOutputStream out = new DataOutputStream(clientDialog.getOutputStream());

// канал чтения из сокета
            DataInputStream in = new DataInputStream(clientDialog.getInputStream());
            System.out.println("DataInputStream created");

            System.out.println("DataOutputStream  created");
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // основная рабочая часть //
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            DataBase dataBase = new DataBase();
            DataBase1 dataBase1 = new DataBase1();
            dataBase1.initialize();
            //dataBase.initialize();

            dataBase.setDatabase(FileParser1.stringToDatabase());


            dataBase.show();

            Server server = new Server();
            boolean isInitialized = false;
            while (!isInitialized) {
                isInitialized = server.initialize(dataBase);
                if (!isInitialized) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            server.setClient(clientDialog);
            boolean isConnected = server.connectSocket();
            boolean user = false;
            while (true) {
                //connecting socket
                if (!isConnected) {
                    System.out.println("Reconnecting...");
                    Thread.sleep(5000);
                    isConnected = server.connectSocket();
                    continue;
                }
                User user1 = new User();
                //reading commands from socket
                try {
                    user1 = server.readCommand();
                    server.addUser(user1);
                    dataBase.setUser(user1);
                } catch (NotConnectedException e) {
                    System.out.println(e.getMessage());
                    isConnected = false;
                } catch (NullPointerException e){
                    System.out.println("nu;;");
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("check");
            e.printStackTrace();
        }

    }
}