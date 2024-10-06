package src.main.java.com.featherchat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
  private Socket client;
  private BufferedReader in;
  private PrintWriter out;
  private boolean done = false;

  @Override
  public void run() {
    try {
      this.client = new Socket("localhost", 4444);
      out = new PrintWriter(client.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(client.getInputStream()));

      InputHandler inHandler = new InputHandler();
      Thread t = new Thread(inHandler);
      t.start();

      String inMessage;
      while ((inMessage = in.readLine()) != null) {
        System.out.println(inMessage);
      }
    } catch (Exception i) {
      shutdown();
    }
  }

  public void shutdown() {
    done = true;
    try {
      if (in != null)
        in.close();
      if (out != null)
        out.close();
      if (client != null && !client.isClosed()) {
        client.close();
      }
    } catch (IOException e) {
      // ignore
    }
  }

  class InputHandler implements Runnable {
    @Override
    public void run() {
      try {
        BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
        while (!done) {
          String message = inReader.readLine();
          if (message.equals("/quit")) {
            inReader.close();
            shutdown();
          } else {
            out.println(message);
          }
        }
      } catch (IOException e) {
        System.out.println("Um erro aconteceu durante o processo: " + e.toString());
        shutdown();
      }
    }
  }

  public static void main(String[] args) {
    Client client = new Client();
    Thread clientThread = new Thread(client);
    clientThread.start();
  }
}
