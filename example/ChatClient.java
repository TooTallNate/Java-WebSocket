import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.tootallnate.websocket.WebSocketClient;
import net.tootallnate.websocket.WebSocketDraft;

/**
 * A barebones chat client that uses the WebSocket protocol.
 */
public class ChatClient extends WebSocketClient {
    private final JTextArea ta;

    public ChatClient(URI uri, JTextArea ta, WebSocketDraft draft) {
        super(uri, draft);
        this.ta = ta;
    }

    public void onMessage(String message) {
        ta.append(message + "\n");
    }

    public void onOpen() {
        ta.append("You are connected to ChatServer: " + getURI() + "\n");
    }

    public void onClose() {
        ta.append("You have been disconnected from: " + getURI() + "\n");
    }

    /**
     * The JFrame for our Chat client.
     */
    private static class Frame extends JFrame implements ActionListener {
        private final JTextField uriField;
        private final JButton connect;
        private final JButton close;
        private final JTextArea area;
        private final JTextField chatField;
        private ChatClient cc;

        public Frame() {
            super("WebSocket Chat Client");
            Container c = getContentPane();
            GridLayout layout = new GridLayout();
            layout.setColumns(1);
            layout.setRows(5);
            c.setLayout(layout);

            uriField = new JTextField();
            uriField.setText("ws://localhost:8887");
            c.add(uriField);

            connect = new JButton("Connect");
            connect.addActionListener(this);
            c.add(connect);
            
            close = new JButton("Close");
            close.addActionListener(this);
            c.add(close);

            JScrollPane scroll = new JScrollPane();
            area = new JTextArea();
            scroll.setViewportView(area);
            c.add(scroll);

            chatField = new JTextField();
            chatField.setText("");
            chatField.addActionListener(this);
            c.add(chatField);

            java.awt.Dimension d = new java.awt.Dimension(300, 400);
            setPreferredSize(d);
            setSize(d);

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (cc != null) {
                        try {
                            cc.close();
                        } catch (IOException ex) { ex.printStackTrace(); }
                    }
                    Frame.this.dispose();
                }
            });

            setLocationRelativeTo(null);
            setVisible(true);
        }

        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == chatField) {
                if (cc != null) {
                    try {
                        cc.send(chatField.getText());
                        chatField.setText("");
                        chatField.requestFocus();
                    } catch (IOException ex) { ex.printStackTrace(); }
                }


            } else if (e.getSource() == connect) {
                connect.setEnabled(false);
                uriField.setEditable(false);
                try {
                    cc = new ChatClient(new URI(uriField.getText()), area, WebSocketDraft.AUTO);
                    cc.connect();
                } catch (URISyntaxException ex) {
                    area.append(uriField.getText() + " is not a valid WebSocket URI\n");
                    connect.setEnabled(true);
                    uriField.setEditable(true);
                }
            } else if (e.getSource() == close) {
                try {
                    cc.close();
                } catch(Exception ex) {
                  ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Frame();
            }
        });
    }
}
