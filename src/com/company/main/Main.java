package com.company.main;

import com.company.database.DataBase;
import com.company.enums.Commands;
import com.company.network.Messages;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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

        while (true) {
            //connecting socket
            try {
                client = server.accept();
                System.out.println("Connection accepted");
            } catch (IOException e) {
                System.out.print("Connection error: ");
                System.out.println(e.getMessage());
                System.out.println("Reconnecting...");
                continue;
            }

            System.out.println();

            try {
                out = new ObjectOutputStream(client.getOutputStream());
                in = new ObjectInputStream(client.getInputStream());
            } catch (IOException e) {
                System.out.println(e.getMessage());
                continue;
            }
            
            while (true) {
                try {
                    input = (Messages) in.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error while reading from client: " + e.getMessage());
                    continue;
                }
                Commands command = (Commands) input.getObject(0);

                switch (command){
                    case INFO:
                        output = dataBase.info();
                        break;
                    case SHOW:
                        output = dataBase.show();
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
