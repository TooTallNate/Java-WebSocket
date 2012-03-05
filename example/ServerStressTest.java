import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.NotYetConnectedException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.java_websocket.WebSocketClient;

public class ServerStressTest extends JFrame {
	private JSlider clients;
	private JSlider interval;
	private JButton start, stop, reset;
	private JLabel clientslabel = new JLabel();
	private JLabel intervallabel = new JLabel();
	private JTextField uriinput = new JTextField( "ws://localhost:8887" );
	private JTextArea text = new JTextArea( "payload" );
	private Timer timer = new Timer( true );

	public ServerStressTest() {
		setTitle( "ServerStressTest" );
		setDefaultCloseOperation( EXIT_ON_CLOSE );
		start = new JButton( "Start" );
		start.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed( ActionEvent e ) {
				start.setEnabled( false );
				stop.setEnabled( true );
				reset.setEnabled( false );
				interval.setEnabled( false );
				clients.setEnabled( false );

				adjust();
			}
		} );
		stop = new JButton( "Stop" );
		stop.setEnabled( false );
		stop.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e ) {
				timer.cancel();
				start.setEnabled( true );
				stop.setEnabled( false );
				reset.setEnabled( true );
				interval.setEnabled( true );
				clients.setEnabled( true );
			}
		} );
		reset = new JButton( "reset" );
		reset.setEnabled( true );
		reset.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e ) {
				while( !websockets.isEmpty())
					websockets.remove( 0 ).close();

			}
		} );
		clients = new JSlider( 0, 500 );
		clients.addChangeListener( new ChangeListener() {
			
			@Override
			public void stateChanged( ChangeEvent e ) {
				clientslabel.setText( "Clients: " + clients.getValue() );
				
			}
		} );
		interval = new JSlider( 0, 5000 );
		interval.addChangeListener( new ChangeListener() {

			@Override
			public void stateChanged( ChangeEvent e ) {
				intervallabel.setText( "Interval: " + interval.getValue() + " ms " );

			}
		} );

		setSize( 300, 400 );
		setLayout( new GridLayout( 8, 1, 10, 10 ) );
		add( new JLabel( "URI" ) );
		add( uriinput );
		add( clientslabel );
		add( clients );
		add( intervallabel );
		add( interval );
		JPanel south = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
		add( text );
		add( south );
		
		south.add( start );
		south.add( stop );
		south.add( reset );

		interval.setValue( 1000 );
		clients.setValue( 1 );

	}

	List<WebSocketClient> websockets = new LinkedList<WebSocketClient>();
	URI uri;
	public void adjust() {
		System.out.println( "Adjust" );
		try {
			uri = new URI( uriinput.getText() );
		} catch ( URISyntaxException e ) {
			e.printStackTrace();
		}
		while ( websockets.size() < clients.getValue() ) {
			WebSocketClient cl = new EmptyClient( uri );
			cl.connect();
			websockets.add( cl );
		}
		while ( websockets.size() > clients.getValue() ) {
			websockets.remove( 0 ).close();
		}
		timer = new Timer( true );
		timer.scheduleAtFixedRate( new TimerTask() {
			
			@Override
			public void run() {
				System.out.println( "send " + System.currentTimeMillis() );
				send();
				
			}
		}, 0, interval.getValue() );

	}
	public void send() {
		for( WebSocketClient cl : websockets ) {
			try {
				cl.send( text.getText() );
			} catch ( NotYetConnectedException e ) {
				e.printStackTrace();
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @param args
	 */
	public static void main( String[] args ) {
		new ServerStressTest().setVisible( true );
	}

}
