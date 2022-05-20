package http_server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServerListener {
    private ServerSocket ss;
    private int reqCount;
    private final File pubFile;

    public HttpServerListener(int port, String pubPath) {
        this.reqCount = 1;

        this.pubFile = new File(pubPath);
        if (!pubFile.exists()) {
            System.out.println("Provided public directory does not exist.");
            System.exit(-1);
        }
        System.out.println("Provided public directory exists!");
        System.out.printf("Absolute path: %s%n%n", pubFile.getAbsolutePath());

        try {
            this.ss = new ServerSocket(port);
        } catch (IllegalArgumentException e) {
            System.out.println("Port out of range: 0 - 65,535");
            System.exit(-1);
        } catch (IOException e) {
            System.out.println("A critical error has occured, please submit the log below to the developers of this server.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void start() {
        while (true) {
            try {
                // This will continue running once the server gets a request.
                Socket socket = ss.accept();
                System.out.printf("Request #%d received! Assigned to new thread.%n", reqCount);

                HttpConnectionThread connectionThread = new HttpConnectionThread(socket, pubFile, reqCount);
                connectionThread.start();
                reqCount++;
                Thread.sleep(500);
            } catch (IOException | InterruptedException e) {
                System.out.println("A critical error has occured, please submit the log below to the developers of this server.");
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Arguments not given, run the program again by " +
                    "specifying port number and relative path to the /public directory.");
            System.exit(-1);
        }

        // Read port and public file path from terminal.
        String pubPath = args[1];
        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Specified port is not a number.");
            System.exit(-1);
        }

        // Create the ServerSocket.
        System.out.println("Starting server...");
        HttpServerListener serverListener = new HttpServerListener(port, pubPath);
        serverListener.start();

    }
}
