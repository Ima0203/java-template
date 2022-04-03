package edu.spbu.Server;

import java.net.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws Exception {
        String adress = "google.ru";
        int port = 80;//сервера
        Socket socket = new Socket(InetAddress.getByName(adress),port);

        String request = "GET / HTTP/1.1\r\n\r\n";
        socket.getOutputStream().write(request.getBytes());
        socket.getOutputStream().flush(); //отправить все

        Scanner scanner = new Scanner(socket.getInputStream());
        while (scanner.hasNextLine()){
            System.out.println(scanner.nextLine());
        }

    }
}