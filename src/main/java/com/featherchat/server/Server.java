package com.featherchat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Server implements Runnable {

  public Integer defaultPort = 4444;
  private ArrayList<ConnHandler> connections;
  private ServerSocket server;
  private boolean done;

  private ExecutorService pool;

  public Server() {
    connections = new ArrayList<>();
    done = false;
  }

  @Override
  public void run() {
    try {
      System.out.println("server is running");
      server = new ServerSocket(defaultPort);
      pool = Executors.newCachedThreadPool();

      while (!done) {
        Socket client = server.accept();
        ConnHandler handler = new ConnHandler(client);
        connections.add(handler);
        pool.execute(handler);
      }
    } catch (IOException e) {
      shutdown();
    }
  }

  public void shutdown() {
    try {
      done = true;
      if (!server.isClosed()) {
        server.close();
      }
    } catch (Exception e) {
      System.out.println("Something happened in Server.shutdown");
    }
  }

  class ConnHandler implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;

    public ConnHandler(Socket client) {
      this.client = client;
    }

    public void broadcast(String message) {
      for (ConnHandler ch : connections) {
        if (ch != null) {
          ch.sendMessage(message);
        }
      }
    }

    @Override
    public void run() {
      try {
        out = new PrintWriter(client.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        broadcast("Someone joined the chad!");

        String message;

        while ((message = in.readLine()) != null) {
          if (message.startsWith("/quit")) {
            broadcast("Someone just left.");
            shutdown();
            break;
          } else {
            broadcast(message);
          }
        }
      } catch (IOException e) {
        System.out.println("something happened trying to shutdown the connhandler");
      }
    }

    public void sendMessage(String message) {
      try {
        out.println(message);
      } catch (Exception e) {
        System.out.println("Something happened trying to send a message");
      }
    }

    public void clientShutdown() {
      try {
        in.close();
        out.close();
        if (!client.isClosed()) {
          client.close();
        }
      } catch (Exception e) {
        System.out.println("Something happened trying to shutdown the client");
      }
    }

  }

  public static void main(String[] args) {
    Server server = new Server();
    server.run();
  }

}
