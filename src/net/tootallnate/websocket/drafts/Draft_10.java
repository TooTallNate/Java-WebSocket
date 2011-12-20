package net.tootallnate.websocket.drafts;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.tootallnate.websocket.Base64;
import net.tootallnate.websocket.Draft;
import net.tootallnate.websocket.FrameBuilder;
import net.tootallnate.websocket.Framedata;
import net.tootallnate.websocket.Framedata.Opcode;
import net.tootallnate.websocket.FramedataImpl1;
import net.tootallnate.websocket.HandshakeBuilder;
import net.tootallnate.websocket.Handshakedata;
import net.tootallnate.websocket.exeptions.InvalidHandshakeException;


public class Draft_10 extends Draft {

	private class IncompleteException extends Throwable{
		private int preferedsize;
		public IncompleteException( int preferedsize) {
			this.preferedsize = preferedsize;
		}
		public int getPreferedSize(){
			return preferedsize;
		}
	}
	
	public static int readVersion(Handshakedata handshakedata){
		String vers = handshakedata.getFieldValue ( "Sec-WebSocket-Version" );
		if( !vers.isEmpty () ){
			int v;
			try {
				v = new Integer ( vers.trim () );
				return v;
			} catch ( NumberFormatException e ) {
				return -1;
			}
		}
		return -1;
	}

	private ByteBuffer incompleteframe;

	@Override
	public HandshakeState acceptHandshakeAsClient( Handshakedata request , Handshakedata response ) throws InvalidHandshakeException {
		if ( !request.hasFieldValue( "Sec-WebSocket-Key" )  || !response.hasFieldValue( "Sec-WebSocket-Accept" ) )
			return HandshakeState.NOT_MATCHED;
		
		String seckey_answere = response.getFieldValue ( "Sec-WebSocket-Accept" );
		String seckey_challenge = request.getFieldValue ( "Sec-WebSocket-Key" );
		seckey_challenge = generateFinalKey ( seckey_challenge );

		if( seckey_challenge.equals ( seckey_answere ) )
			return HandshakeState.MATCHED;
		return HandshakeState.NOT_MATCHED;
	}
	
	@Override
	public HandshakeState acceptHandshakeAsServer( Handshakedata handshakedata ) throws InvalidHandshakeException {
		//Sec-WebSocket-Origin is only required for browser clients
		int v = readVersion( handshakedata );
		if( v == 7 || v == 8 )//g
			return basicAccept( handshakedata ) ? HandshakeState.MATCHED : HandshakeState.NOT_MATCHED;
		return HandshakeState.NOT_MATCHED;
	}

	@Override
	public ByteBuffer createBinaryFrame( Framedata framedata ) {
		byte[] mes   = framedata.getPayloadData ();
		boolean mask = framedata.getTransfereMasked();
		int sizebytes = mes.length==0?0:mes.length <= 125 ? 1 : mes.length <= 65535 ? 2 : 4;//4 bytes length currently not useable
		ByteBuffer buf = ByteBuffer.allocate ( 1 + ( sizebytes > 1 ? sizebytes + 1 : sizebytes ) +( mask ? 4 : 0 ) + mes.length );
		byte optcode = fromOpcode ( framedata.getOpcode () );
		byte one = ( byte ) ( framedata.isFin () ? -128 : 0 );
		one |= optcode;
		buf.put ( one );
		byte[] payloadlengthbytes = toByteArray ( mes.length , sizebytes );
		assert( payloadlengthbytes.length == sizebytes );
		if( sizebytes == 0){
			//controllframe
		}
		else if( sizebytes == 1){
			buf.put ( (byte)( (byte)payloadlengthbytes[0] | ( mask ? (byte)-128 : 0 ) ) );
		}
		else if( sizebytes == 2 ){
			buf.put ( (byte)( (byte)126 | ( mask ? (byte)-128 : 0 ) ) );
			buf.put ( payloadlengthbytes );
		}
		else if( sizebytes == 8 ){
			buf.put ( (byte)( (byte)127 | ( mask ? (byte)-128 : 0 ) ) );
			buf.put ( payloadlengthbytes );
		}
		else 
			throw new RuntimeException ( "Size representation not supported/specified" );
		
		
		
		
		if( mask ){
			ByteBuffer maskkey =  ByteBuffer.allocate ( 4 );
			maskkey.putInt ( new Random().nextInt() );
			buf.put ( maskkey.array () );		
			for( int i = 0 ; i < mes.length ; i++){
				buf.put( ( byte ) ( mes[i] ^ maskkey.get ( i % 4 ) ) );
			}
		}
		else
			buf.put ( mes );
		//translateFrame ( buf.array () , buf.array ().length );
		assert( buf.remaining() == 0 ):buf.remaining();
		return buf;
	}

	@Override
	public List<Framedata> createFrames( byte[] binary , boolean mask ) {
		throw new RuntimeException ( "not yet implemented" );
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

	private byte fromOpcode( Opcode opcode ){
		if( opcode == Opcode.CONTINIOUS )
			return 0;
		else if( opcode == Opcode.TEXT )
			return 1;
		else if( opcode == Opcode.BINARY)
			return 2;
		else if( opcode == Opcode.CLOSING)
			return 8;
		else if( opcode == Opcode.PING)
			return 9;
		else if( opcode == Opcode.PONG)
			return 10;
		throw new RuntimeException ( "Don't know how to handle "+opcode.toString() );
	}

	private String generateFinalKey( String in ){
		String seckey = in.trim ();
		String acc = seckey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		MessageDigest sh1;
		try {
			sh1 = MessageDigest.getInstance ( "SHA1" );
		} catch ( NoSuchAlgorithmException e ) {
			throw new RuntimeException ( e );
		}
		return Base64.encodeBytes ( sh1.digest ( acc.getBytes () ) );
	}

	@Override
	public HandshakeBuilder postProcessHandshakeRequestAsClient( HandshakeBuilder request ) {
		request.put ( "Upgrade" , "websocket" );
		request.put ( "Connection" , "Upgrade" ); //to respond to a Connection keep alives
		request.put ( "Sec-WebSocket-Version" , "8" );
		
		byte[] random = new byte[16];
		new Random().nextBytes( random );
		request.put( "Sec-WebSocket-Key" , Base64.encodeBytes( random ) );
		
		return request;
	}
	
	@Override
	public HandshakeBuilder postProcessHandshakeResponseAsServer( Handshakedata request , HandshakeBuilder response) throws InvalidHandshakeException {
		response.put ( "Upgrade" , "websocket" );
		response.put ( "Connection" , request.getFieldValue ( "Connection" ) ); //to respond to a Connection keep alives
		response.setHttpStatusMessage( "Switching Protocols" );
		String seckey = request.getFieldValue ( "Sec-WebSocket-Key" );
		if ( seckey == null )
			throw new InvalidHandshakeException ( "missing Sec-WebSocket-Key" );
		response.put( "Sec-WebSocket-Accept" , generateFinalKey( seckey ) );
		return response;
	}
	
	private byte[] toByteArray( long val, int bytecount ){
        byte[] buffer = new byte[bytecount];
        int highest = 8 * bytecount - 8;
        for( int i = 0 ; i < bytecount ; i++ ){
        	buffer[i] = (byte) ( val >>> ( highest - 8 * i ) );
        }
        return buffer;
	}
	
	private Opcode toOpcode( byte opcode ){
		switch ( opcode ) {
			case 0 :
				return Opcode.CONTINIOUS;
			case 1 :
				return Opcode.TEXT;
			case 2 :
				return Opcode.BINARY;
			//3-7 are not yet defined
			case 8 :
				return Opcode.CLOSING;
			case 9 :
				return Opcode.PING;
			case 10 :
				return Opcode.PONG;
			//11-15 are not yet defined
			default :
				return null;
		}
	}
	@Override
	public List<Framedata> translateFrame( ByteBuffer buffer , int available ) {
		List<Framedata> frames = new LinkedList<Framedata> ();
		int offset = 0;
		Framedata cur;
		
		if( incompleteframe != null){
			while(true){
				try {
					int available_next_byte_count = available;// The number of bytes received
					int expected_next_byte_count = incompleteframe.remaining();// The number of bytes to complete the incomplete frame

					if( expected_next_byte_count > available_next_byte_count ){
						//did not receive enough bytes to complete the frame
						incompleteframe.put( buffer.array() , 0 , available_next_byte_count );
						//assert( buffer.limit() == available_next_byte_count + buffer.position() );
						//buffer.position( available );
						return frames; 
					}
					
					incompleteframe.put( buffer.array() , 0 , expected_next_byte_count );
					//buffer.position( buffer.position() + expected_next_byte_count );
					
					cur = translateSingleFrame ( incompleteframe , 0 , incompleteframe.limit () );
					frames.add ( cur );
					offset = expected_next_byte_count;
					incompleteframe = null;
					break; // go on with the normal frame receival
				} catch ( IncompleteException e ) {
					//extending as much as suggested
					ByteBuffer extendedframe = ByteBuffer.allocate ( e.getPreferedSize () );
					assert( extendedframe.limit () > incompleteframe.limit() );
					extendedframe.put ( incompleteframe.array () );
					incompleteframe = extendedframe;
				}
			}
		}
		
		while( offset < available ){//Read as much as possible full frames
			try {
				cur = translateSingleFrame ( buffer , offset , available );
				//System.out.println ( "index "+offset+":" + cur );
				offset = buffer.position();
				frames.add ( cur );
			} catch ( IncompleteException e ) { 
				//remember the incomplete data
				int pref = e.getPreferedSize (); 
				incompleteframe = ByteBuffer.allocate ( pref );
				incompleteframe.put( buffer.array() , offset , available - offset );
				break;
			}
		}
		return frames;
	}

	public Framedata translateSingleFrame( ByteBuffer buffer , int foff, int available ) throws IncompleteException {
		int maxpacketsize = available - foff;
		int realpacketsize = 2;
		byte[] b = buffer.array ();
		if( maxpacketsize < realpacketsize )
			throw new IncompleteException( realpacketsize );
		boolean FIN  =  b[ foff + 0 ] >> 8 != 0;
		boolean MASK =  ( b[ foff + 1 ] & -128 ) != 0;
		int payloadlength = (byte)( b[ foff + 1 ] & ~(byte)128 );
		
		if( payloadlength <= 125 ){
		}
		else if( payloadlength == 126 ){
			realpacketsize += 2; // additional length bytes
			if( maxpacketsize < realpacketsize )
				throw new IncompleteException( realpacketsize );
			byte[] sizebytes = new byte[2];
			sizebytes[0] = b[ foff + 1 + 1 ];
			sizebytes[1] = b[ foff + 1 + 2 ];
			payloadlength = new BigInteger ( sizebytes ).intValue();
		}
		else{
			realpacketsize += 8; // additional length bytes
			if( maxpacketsize < realpacketsize )
				throw new IncompleteException( realpacketsize );
			byte[] bytes = new byte[8];
			for( int i = 0 ; i < 8 ; i++ ){
				bytes[i] = b[ foff + 1 + i ];
			} 
			long length = new BigInteger ( bytes ).longValue();
			if( length > Integer.MAX_VALUE ){
				throw new RuntimeException ( "Payloadsize is to big..." );
			}
		}
		int maskskeystart = foff + realpacketsize;
		realpacketsize += ( MASK ? 4 : 0 );
		int payloadstart = foff + realpacketsize;
		realpacketsize += payloadlength;

		if( maxpacketsize < realpacketsize )
			throw new IncompleteException( realpacketsize );
		
		ByteBuffer payload=ByteBuffer.allocate ( payloadlength );
		if( MASK ){
			byte[] maskskey  = ByteBuffer.allocate ( 4 ).put (  b , maskskeystart , 4 ).array ();
			for( int i = 0 ; i < payloadlength ; i++ ){
				payload.put ( ( byte ) ( (byte)b[ payloadstart + i ] ^ (byte)maskskey[ i % 4  ] ) );
			}
		}
		else{
			payload.put ( b , payloadstart , payloadlength );
		}
		
		buffer.position ( payloadstart + payloadlength );
		FrameBuilder frame = new FramedataImpl1();
		frame.setFin ( FIN );
		frame.setOptcode ( toOpcode ( (byte) ( b[ foff + 0 ] & 15 ) ) );
		frame.setPayload ( payload.array () );
		
		return frame;
	}

	@Override
	public void reset( ) {
		incompleteframe = null;
	}
}
