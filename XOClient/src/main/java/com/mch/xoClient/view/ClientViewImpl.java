package com.mch.xoClient.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mch.xoClient.controller.ClientController;
import com.mch.xoData.defaults.Defaults;

@Component
public class ClientViewImpl extends JFrame implements ActionListener,
    ClientView {

  private static final long serialVersionUID = 1L;

  @Autowired(required = true)
  private ClientController controller;

  private static String defTitle = "Chat Client";
  // will first hold the "user name", later the "message"
  private JLabel lblLog;
  private JLabel lblYouMark;
  private JLabel lblMark;
  private JLabel lblYouEnemy;
  private JLabel lblEnemy;
  // to hold the user name and later on the messages
  private JTextField txtLog;
  // to hold the server address an the port number
  private JTextField txtServer, txtPort;
  // to Logout and get the list of the users
  private JButton btnLogin, btnLogout, btnWhoIsIn, btnInvite;
  private JButton btnLoad;
  private JButton btnSave;
  // display messages here
  private JButton[] xoButtons;
  // for faster look-up
  private Map<JButton, Integer> xoLocations;
  // Combo box with users
  private JComboBox<String> cmbUsers;
  // if it is for connection
  private String userName;
  // the default port number
  private int serverPort;
  private String serverHost;

  // Constructor connection receiving a socket number
  public ClientViewImpl() {
    super(defTitle);
    this.serverPort = Defaults.DEFAULT_PORT;
    this.serverHost = Defaults.DEFAULT_HOST;
    // this.controller = new ClientServer(host, port, userName, this);
    addGUIElements();
  }

  private void addGUIElements() {
    // The NorthPanel with:
    JPanel northPanel = new JPanel(new GridLayout(4, 1));
    // the server name and the port number
    JPanel serverAndPort = new JPanel(new GridLayout(1, 2));
    JPanel panelUserSend = new JPanel();

    JPanel panelServer = new JPanel(new BorderLayout());
    JPanel panelPort = new JPanel(new BorderLayout());
    txtServer = new JTextField(serverHost);
    txtServer.setHorizontalAlignment(JTextField.CENTER);
    txtPort = new JTextField("" + serverPort);
    txtPort.setHorizontalAlignment(SwingConstants.CENTER);

    panelServer.add(new JLabel("Server Address:  "), BorderLayout.WEST);
    panelServer.add(txtServer, BorderLayout.CENTER);
    panelPort.add(new JLabel("Port Number:  "), BorderLayout.WEST);
    panelPort.add(txtPort, BorderLayout.CENTER);
    // adds the Server and port field to the GUI
    serverAndPort.add(panelServer);
    serverAndPort.add(panelPort);
    northPanel.add(serverAndPort);

    // the Label and the TextField
    JPanel logPanel = new JPanel(new BorderLayout());
    lblLog = new JLabel("Username: ", SwingConstants.CENTER);
    logPanel.add(lblLog, BorderLayout.WEST);
    txtLog = new JTextField("Anonymous");
    txtLog.setBackground(Color.WHITE);
    logPanel.add(txtLog, BorderLayout.CENTER);
    northPanel.add(logPanel);

    cmbUsers = new JComboBox<String>();
    panelUserSend.add(cmbUsers);
    btnWhoIsIn = new JButton("Refresh");
    panelUserSend.add(btnWhoIsIn);
    btnInvite = new JButton("Invite");
    panelUserSend.add(btnInvite);
    btnSave = new JButton("Save");
    panelUserSend.add(btnSave);
    btnLoad = new JButton("Load");
    panelUserSend.add(btnLoad);
    northPanel.add(panelUserSend);

    lblYouMark = new JLabel("Your mark: ");
    panelUserSend.add(lblYouMark);
    lblMark = new JLabel();
    panelUserSend.add(lblMark);

    lblYouEnemy = new JLabel(" / Enemy: ");
    panelUserSend.add(lblYouEnemy);
    lblEnemy = new JLabel();
    panelUserSend.add(lblEnemy);

    add(northPanel, BorderLayout.NORTH);

    // The CenterPanel with the playing Table
    JPanel centerPanel = new JPanel(new GridLayout(1, 1));
    centerPanel.add(createTable());
    add(centerPanel, BorderLayout.CENTER);

    // the 3 buttons
    btnLogin = new JButton("Login");
    btnLogin.addActionListener(this);
    btnLogout = new JButton("Logout");
    btnLogout.addActionListener(this);
    btnLogout.setEnabled(false); // you have to login before being able to
    // logout
    btnWhoIsIn.addActionListener(this);
    btnWhoIsIn.setEnabled(false); // you have to login before being able to
    // Who is in
    btnInvite.addActionListener(this);
    btnInvite.setEnabled(false); // you have to login before being able to Who

    btnSave.addActionListener(this);
    btnSave.setEnabled(false);
    btnLoad.addActionListener(this);
    btnLoad.setEnabled(false);

    JPanel southPanel = new JPanel();
    southPanel.add(btnLogin);
    southPanel.add(btnLogout);
    add(southPanel, BorderLayout.SOUTH);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        // we are logged in
        if (btnLogout.isEnabled())
          controller.doLogout();
        System.exit(0);
      }
    });
    // setSize(600, 600);
    pack();
    setVisible(true);
  }

  private JPanel createTable() {
    JPanel buttonHolder = new JPanel(new GridLayout(Defaults.N, Defaults.N));
    int size = Defaults.N * Defaults.N;
    xoButtons = new JButton[size];
    xoLocations = new HashMap<>();
    for (int i = 0; i < size; i++) {
      JButton btn = new JButton();
      // button width has to be at least 50, height of 50 would be too high
      // so I can't a perfect square (taking my resolution into account)
      btn.setPreferredSize(new Dimension(50, 40));
      btn.setEnabled(false);
      xoLocations.put(btn, i);

      btn.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          Object o = e.getSource();
          int location = xoLocations.get(o);
          putMark(location, controller.getMark());
          txtLog.setText("Marked " + location + " with " + controller.getMark());
          for (JButton b : xoButtons)
            b.setEnabled(false);
          controller.mark(location);
        }
      });

      xoButtons[i] = btn;
      buttonHolder.add(btn);
    }
    return buttonHolder;
  }

  // Button or JTextField clicked
  @Override
  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();

    // if it is the Logout button
    if (o == btnLogout) {
      controller.doLogout();
      this.setTitle(defTitle);
      return;
    }
    // if it the who is in button
    if (o == btnWhoIsIn) {
      controller.doGetUserList();
      return;
    }
    // OK it is coming from the JTextField
    if (o == btnInvite) {
      // just have to send the message
      String enemy = (String) cmbUsers.getSelectedItem();
      if (enemy != null) {
        String to = enemy.split("--")[0].trim();
        controller.invite(to);
      } else {
        JOptionPane.showMessageDialog(this, "No enemy selected!", "No enemy!",
            JOptionPane.OK_OPTION);
      }
      return;
    }

    if (o == btnSave)
      controller.saveGame();

    if (o == btnLoad) {
      int res = JOptionPane.showConfirmDialog(this,
          "This will erase the current game.\nConfirm?", "Sure?",
          JOptionPane.YES_NO_OPTION);
      if (res == JOptionPane.YES_OPTION)
        controller.loadGame();
    }

    if (o == btnLogin) {
      userName = txtLog.getText().trim();
      if (userName.isEmpty())
        return;
      // empty serverAddress ignore it
      String server = txtServer.getText().trim();
      if (server.isEmpty())
        return;
      // empty or invalid port number, ignore it
      String portNumber = txtPort.getText().trim();
      if (portNumber.isEmpty())
        return;
      try {
        serverPort = Integer.parseInt(portNumber);
      } catch (NumberFormatException en) {
        return;
      }
      // test if we can start the Client
      if (!controller.start(userName, serverHost, serverPort))
        return;
      this.setTitle(defTitle + ": " + userName);
      lblLog.setText("Log: ");

      // disable login button
      btnLogin.setEnabled(false);
      // enable the 2 buttons
      btnLogout.setEnabled(true);
      btnWhoIsIn.setEnabled(true);
      btnInvite.setEnabled(true);
      // disable the Server and Port JTextField
      txtServer.setEditable(false);
      txtPort.setEditable(false);
      // Action listener for when the user enter a message
      txtLog.addActionListener(this);
      controller.doGetUserList();
    }

  }

  // called by the GUI is the connection failed
  // we reset our buttons, label, text field
  @Override
  public void connectionFailed() {
    btnLogin.setEnabled(true);
    btnLogout.setEnabled(false);
    btnWhoIsIn.setEnabled(false);
    lblLog.setText("Username: ");
    txtLog.setText("Anonymous");
    // reset port number and host name as a construction time
    txtPort.setText("" + serverPort);
    txtServer.setText(serverHost);
    // let the user change them
    txtServer.setEditable(false);
    txtPort.setEditable(false);
    // don't react to a <CR> after the user name
    txtLog.removeActionListener(this);
  }

  @Override
  public void setUsers(List<String> users) {
    cmbUsers.setModel(new DefaultComboBoxModel<String>());
    for (String u : users)
      cmbUsers.addItem(u);
  }

  @Override
  public void putMark(int location, char mark) {
    for (JButton b : xoButtons) {
      if (b.getText() == null || b.getText().trim().isEmpty())
        b.setEnabled(true);
    }
    JButton btn = xoButtons[location];
    btn.setText("" + mark);
    btn.setEnabled(false);
  }

  @Override
  public void showMessage(String error) {
    JOptionPane.showMessageDialog(this, error);
  }

  @Override
  public void log(String msg) {
    txtLog.setText(msg);
  }

  @Override
  public void notifyInvited(char mark, String enemy) {
    lblMark.setText("" + mark);
    lblEnemy.setText(enemy);
    btnInvite.setEnabled(false);
    // enable Table for X, disable for Y
    if (mark == 'X') {
      for (JButton b : xoButtons)
        b.setEnabled(true);
    } else {
      for (JButton b : xoButtons)
        b.setEnabled(false);
    }
    btnLoad.setEnabled(true);
    btnSave.setEnabled(true);
  }

  @Override
  public void updateWholeTable(char[][] table, boolean active, char mark) {
    // reset the whole table
    lblMark.setText("" + mark);
    for (JButton b : xoButtons) {
      b.setEnabled(true);
      b.setText("");
    }
    int pos = 0;
    for (char[] cv : table) {
      for (char c : cv) {
        if (c == 'X' || c == 'O') {
          xoButtons[pos].setText("" + c);
          xoButtons[pos].setEnabled(false);
        }
        pos++;
      }
    }
    if (!active) {
      for (JButton b : xoButtons) {
        b.setEnabled(false);
      }
    }
  }

  @Override
  public void notifyGameEnded(Boolean won) {
    if (won) {
      log("YOU WON THE GAME, VERY WELL DONE!");
    } else {
      log("I'M AFRAID YOU ARE A LOOSER!");
    }
    for (JButton b : xoButtons) {
      b.setEnabled(false);
    }
  }
}
