package net.tootallnate.websocket.drafts;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import sun.misc.BASE64Encoder;

import net.tootallnate.websocket.Draft;
import net.tootallnate.websocket.FrameBuilder;
import net.tootallnate.websocket.Framedata;
import net.tootallnate.websocket.HandshakeBuilder;
import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.Framedata.Opcode;
import net.tootallnate.websocket.Handshakedata;
import net.tootallnate.websocket.FramedataImpl1;
import net.tootallnate.websocket.exeptions.InvalidHandshakeException;


public class Draft_10 extends Draft {

	/**
	* The WebSocket protocol expects UTF-8 encoded bytes.
	*/ 
	public final static Charset  UTF8_CHARSET = Charset.forName ( "UTF-8" );
	
	@Override
	public List<Framedata> translateFrame( byte[] buffer , int read ) {
		byte[] b = ByteBuffer.allocate ( read ).put ( buffer , 0 , read ).array ();//socketBuffer.array ()
		boolean FIN  =  b[0] >> 8 != 0;
		boolean MASK =  ( b[0] &~1 ) != 0;
		Opcode opcode =  Opcode.TEXT; //TODO Read out Opcode
		int payloadlength = (byte)( b[1] & ~(byte)128 );
		System.out.println ( "pll: " + payloadlength );
		int maskskeytart = payloadlength < 125 ? 1 + 1 : payloadlength == 126 ? 1 + 2 : 1 + 8 ;
		int extdatastart = maskskeytart+4; //TODO allow extradata
		int payloadstart = extdatastart; 
		byte[] maskskey  = ByteBuffer.allocate ( 4 ).put (  b , maskskeytart , 4 ).array ();
		//demasking the payloaddata 
		ByteBuffer payload=ByteBuffer.allocate ( payloadlength );
		for( int i = 0 ; i < payloadlength ; i++ ){
			payload.put ( ( byte ) ( (byte)b[payloadstart + i] ^ (byte)maskskey[ i%4  ] ) );
		}
		
		/*b[1]&=~(byte)128; //echo test
		for( int i = 0 ; i < payloadlength ; i++ ){
			b[ payloadstart + i - 4 ] =  ( byte ) ( (byte)b[payloadstart + i] ^ (byte)maskskey[ i%4  ] );
		}
		byte[] c = ByteBuffer.allocate ( read-4 ).put ( b , 0 , read-4 ).array ();
		channelWrite ( ByteBuffer.wrap ( c ) );*/
		
		FrameBuilder frame = new FramedataImpl1();
		frame.setFin ( FIN );
		frame.setOptcode ( opcode );
		frame.setPayload ( payload.array () );
		
		return Collections.singletonList ( (Framedata)frame );
	}

	@Override
	public ByteBuffer createBinaryFrame( Framedata framedata ) {
		byte[] mes   = framedata.getPayloadData ();
		boolean mask = framedata.getTransfereMasked();
		ByteBuffer buf = ByteBuffer.allocate ( 2 + mes.length+( mask ? 4 : 0 ) );
		ByteBuffer maskkey=  ByteBuffer.allocate ( 4 );
		byte one = ( byte ) -127;
		buf.put ( one ); 						// b1 controll
		buf.put ( ( byte ) mes.length );		// b2 length
		if( mask ){
			maskkey.putInt ( Integer.MIN_VALUE );
			buf.put ( maskkey.array () );		
			for( int i = 0 ; i < mes.length ; i++){
				buf.put( ( byte ) ( mes[i] ^ maskkey.get ( i % 4 ) ) );
			}
		}
		else
			buf.put ( mes );
		return buf;
	}

	@Override
	public List<Framedata> createFrames( String text , boolean mask ) {
		FrameBuilder curframe = new FramedataImpl1();
		byte[] pay   = text.getBytes ( UTF8_CHARSET );
		curframe.setPayload ( pay );
		curframe.setFin ( true );
		curframe.setOptcode ( Opcode.TEXT );
		curframe.setTransferemasked ( mask );
		return Collections.singletonList  ( (Framedata) curframe );
	}

	@Override
	public List<Framedata> createFrames( byte[] binary , boolean mask ) {
		throw new RuntimeException ( "not yet implemented" );
	}
	
	@Override
	public boolean acceptHandshakeAsServer( Handshakedata handshakedata ) throws InvalidHandshakeException {
		//TODO Do a more detailed formal handshake test
		String vers = handshakedata.getFieldValue ( "Sec-WebSocket-Version" );
		if( vers != null ){
			int v;
			try {
				v = new Integer ( vers.trim () );
			} catch ( NumberFormatException e ) {
				throw new InvalidHandshakeException( e);
			}
			if( v == 7 || v == 8 )//g
				return true;
		}
			
		return false;
	}

	@Override
	public boolean acceptHandshakeAsClient( Handshakedata request , Handshakedata response ) {
		throw new RuntimeException ( "not yet implemented" );
	}

	@Override
	public HandshakeBuilder postProcessHandshakeRequestAsClient( HandshakeBuilder request ) {
		throw new RuntimeException ( "not yet implemented" );
	}

	@Override
	public HandshakeBuilder postProcessHandshakeResponseAsServer( Handshakedata request , HandshakeBuilder response) throws InvalidHandshakeException {
		String seckey = request.getFieldValue ( "Sec-WebSocket-Key" );
		if ( seckey == null )
			throw new InvalidHandshakeException ( "missing Sec-WebSocket-Key" );
		seckey = seckey.trim ();
		String acc = seckey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		MessageDigest sh1;
		try {
			sh1 = MessageDigest.getInstance ( "SHA1" );
		} catch ( NoSuchAlgorithmException e ) {
			throw new RuntimeException ( e );
		}
		response.put( "Sec-WebSocket-Accept" , new BASE64Encoder ().encode ( sh1.digest ( acc.getBytes () ) ) );
		return super.postProcessHandshakeResponseAsServer ( request , response );
	}

}
