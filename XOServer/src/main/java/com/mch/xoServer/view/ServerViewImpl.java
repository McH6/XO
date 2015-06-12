package com.mch.xoServer.view;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.mch.xoServer.controller.ServerController;
import com.mch.xoServer.view.awtEvent.MyEventLogAWTEvent;
import com.mch.xoServer.view.awtEvent.MyMessageLogAWTEvent;

public class ServerViewImpl extends JFrame implements ActionListener,
    WindowListener, ServerView {
  private static final long serialVersionUID = 1L;
  // the stop and start buttons
  private JButton btnStartStop;
  private boolean running;
  // JTextArea for the chat room and the events
  private JTextArea txtMessageLog, txtEventLog;
  // The port number
  private JTextField txtPortNumber;
  // my server
  private ServerController controller;
  private int port;
  // to display hh:mm:ss
  private SimpleDateFormat simpleDateFormater;

  // gets the port
  public ServerViewImpl(int port, ServerController controller) {
    super("XO Server");
    this.controller = controller;
    // Inject this View into the Controller
    controller.setView(this);
    this.port = port;
    this.simpleDateFormater = new SimpleDateFormat("HH:mm:ss");

    enableEvents(MyMessageLogAWTEvent.EVENT_ID);
    enableEvents(MyEventLogAWTEvent.EVENT_ID);
    
    addGUIElements();

    appendEventLog("Events log...");
  }

  private void addGUIElements() {
    // in the NorthPanel the PortNumber the Start and Stop buttons
    JPanel north = new JPanel(new FlowLayout());
    north.add(new JLabel("Port number: "));
    txtPortNumber = new JTextField("  " + port);
    north.add(txtPortNumber);
    // to stop or start the server, we start with "Start"
    btnStartStop = new JButton("Start");
    btnStartStop.addActionListener(this);
    north.add(btnStartStop);
    add(north, BorderLayout.NORTH);

    // the event and chat room
    JPanel center = new JPanel(new GridLayout(2, 1));
    txtMessageLog = new JTextArea(80, 80);
    txtMessageLog.setEditable(false);
    appendMessageLog("Message Log...");
    center.add(new JScrollPane(txtMessageLog));
    txtEventLog = new JTextArea(80, 80);
    txtEventLog.setEditable(false);

    center.add(new JScrollPane(txtEventLog));
    add(center);

    // need to be informed when the user click the close button on the frame
    addWindowListener(this);
    setSize(400, 600);
    setVisible(true);
  }

  // append message to the two JTextArea
  // position at the end
  public void appendMessageLog(String str) {
    String time = simpleDateFormater.format(new Date());
    txtMessageLog.append(time + " " + str + "\n");
    txtMessageLog.setCaretPosition(txtMessageLog.getText().length() - 1);
  }

  public void appendEventLog(String str) {
    String time = simpleDateFormater.format(new Date());
    txtEventLog.append(time + " " + str + "\n");
    txtEventLog.setCaretPosition(txtEventLog.getText().length() - 1);
  }

  // start or stop where clicked
  public void actionPerformed(ActionEvent e) {
    // if running we have to stop the server
    if (running) {
      controller.stop();
      txtPortNumber.setEditable(true);
      btnStartStop.setText("Start");
      running = false;
      return;
    }
    // OK start the server
    try {
      port = Integer.parseInt(txtPortNumber.getText().trim());
    } catch (Exception er) {
      appendEventLog("Invalid port number");
      return;
    }
    // and start it as a thread
    new ServerBackgroundWorker().start();
    btnStartStop.setText("Stop");
    txtPortNumber.setEditable(false);
    running = true;
  }

  /*
   * If the user click the X button to close the application I need to close the
   * connection with the server to free the port
   */
  @Override
  public void windowClosing(WindowEvent e) {
    // if my Server exist
    if (running) {
      try {
        controller.stop(); // ask the server to close the connection
      } catch (Exception eClose) {
        System.err.println(eClose.getMessage());
      }
    }
    // dispose the frame
    dispose();
    System.exit(0);
  }

  // I can ignore the other WindowListener method
  @Override
  public void windowClosed(WindowEvent e) {
  }

  @Override
  public void windowOpened(WindowEvent e) {
  }

  @Override
  public void windowIconified(WindowEvent e) {
  }

  @Override
  public void windowDeiconified(WindowEvent e) {
  }

  @Override
  public void windowActivated(WindowEvent e) {
  }

  @Override
  public void windowDeactivated(WindowEvent e) {
  }

  // a single thread to run the Server
  // otherwise the GUI would be unresponsive
  private class ServerBackgroundWorker extends Thread {
    public void run() {
      controller.start(port); // should execute until if fails
      // the server exited
      btnStartStop.setText("Start");
      txtPortNumber.setEditable(true);
    }
  }

  @Override
  protected void processEvent(AWTEvent event) {
    if (event instanceof MyEventLogAWTEvent) {
      MyEventLogAWTEvent ev = (MyEventLogAWTEvent) event;
      // access GUI component
      appendEventLog(ev.getStr());
    } else if (event instanceof MyMessageLogAWTEvent) {
      MyMessageLogAWTEvent ev = (MyMessageLogAWTEvent) event;
      // access GUI component
      appendMessageLog(ev.getStr());
    } else {
      // other events go to the system default process event handler
      super.processEvent(event);
    }
  }

}
