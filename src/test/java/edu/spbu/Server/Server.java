package edu.spbu.Server;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Server {

    private static String recieveRequest(Socket socket) {

        String fileName = null;
        try {
            InputStream inputStream = socket.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            fileName = scanner.nextLine().split(" ")[1].substring(1);
        } catch (IOException e) {
        } finally {
            return fileName;
        }
    }

    private static void sendResponse(String fileName, Socket socket) {
        try {
            File file = new File(fileName);
            if (fileName != null && file.isFile()) {
                String content = new String(Files.readAllBytes(Paths.get(fileName)));
                OutputStream stream = socket.getOutputStream();
                stream.write("HTTP/1.1 200 OK\r\n".getBytes());
                stream.write(("Content-Length: " + (content.length() + 2) + "\r\n\r\n").getBytes());
                stream.write((content).getBytes());
                stream.write("\r\n".getBytes());
            } else {
                socket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n".getBytes());
                socket.getOutputStream().write("Content-Length: 0\r\n\r\n".getBytes());
            }
        } catch (IOException e) {
            try {
                socket.getOutputStream().write("HTTP/1.1 500 Internal Error\r\n".getBytes()); //ошибка сервера
                socket.getOutputStream().write("Content-Length: 0\r\n\r\n".getBytes());
            } catch (IOException er) {
            }
        }
    }


    public static void main(String[] args) {
        try {
            int port = 44440;
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Waiting for connection...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connection accepted.");

                String fileName = recieveRequest(socket);
                sendResponse(fileName, socket);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}