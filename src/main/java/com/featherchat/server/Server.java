package src.main.java.com.featherchat.server;

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
      System.out.println("Server is running");
      server = new ServerSocket(defaultPort);
      pool = Executors.newCachedThreadPool();

      while (!done) {
        Socket client = server.accept();
        ConnHandler handler = new ConnHandler(client);
        synchronized (connections) {
          connections.add(handler);
        }
        pool.execute(handler);
      }
    } catch (IOException e) {
      e.printStackTrace();
      shutdown();
    }
  }

  public void shutdown() {
    done = true;
    try {
      if (server != null && !server.isClosed()) {
        server.close();
      }
      pool.shutdown();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  class ConnHandler implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;

    public ConnHandler(Socket client) {
      this.client = client;
    }

    public synchronized void broadcast(String message) {
      synchronized (connections) {
        for (ConnHandler ch : connections) {
          if (ch != null) {
            ch.sendMessage(message);
          }
        }
      }
    }

    @Override
    public void run() {
      try {
        out = new PrintWriter(client.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        broadcast("Someone joined the chat!");

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
        e.printStackTrace();
      }
    }

    public void sendMessage(String message) {
      try {
        out.println(message);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    public void shutdown() {
      clientShutdown();
      synchronized (connections) {
        connections.remove(this);
      }
    }

    public void clientShutdown() {
      try {
        in.close();
        out.close();
        if (!client.isClosed()) {
          client.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  public static void main(String[] args) {
    Server server = new Server();
    Thread serverThread = new Thread(server);
    serverThread.start();
  }

}
