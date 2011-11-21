package net.tootallnate.websocket.drafts;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

import net.tootallnate.websocket.Draft;
import net.tootallnate.websocket.Framedata;
import net.tootallnate.websocket.HandshakeBuilder;
import net.tootallnate.websocket.Handshakedata;
import net.tootallnate.websocket.HandshakedataImpl1;
import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.exeptions.InvalidHandshakeException;

public class Draft_76 extends Draft_75 {

	@Override
	public boolean acceptHandshakeAsClient( Handshakedata request , Handshakedata response ) {
		return super.acceptHandshakeAsClient( request , response );//TODO validate the handshake
	}

	@Override
	public boolean acceptHandshakeAsServer( Handshakedata handshakedata ) {
		
		if( !handshakedata.getFieldValue ( "Sec-WebSocket-Key1" ).isEmpty() 
			&&  !handshakedata.getFieldValue ( "Sec-WebSocket-Key1" ).isEmpty()
			/*new String ( handshakedata.getContent () ).endsWith ( "\r\n\r\n" )*/)
			return true;
		return false;
	}
	

	  private String generateKey() {
		Random r = new Random();
		long maxNumber = 4294967295L;
		long spaces = r.nextInt(12) + 1;
		int max = new Long(maxNumber / spaces).intValue();
		max = Math.abs(max);
		int number = r.nextInt(max) + 1;
		long product = number * spaces;
		String key = Long.toString(product);
		//always insert atleast one random character
		int numChars = r.nextInt(12)+1;
		for (int i=0; i < numChars; i++){
			int position = r.nextInt(key.length());
			position = Math.abs(position);
			char randChar = (char)(r.nextInt(95) + 33);
			//exclude numbers here
			if(randChar >= 48 && randChar <= 57){
				randChar -= 15;
			}
			key = new StringBuilder(key).insert(position, randChar).toString();
		}
		for (int i = 0; i < spaces; i++){
			int position = r.nextInt(key.length() - 1) + 1;
			position = Math.abs(position);
			key = new StringBuilder(key).insert(position,"\u0020").toString();
		}
		return key;
	}

	@Override
	public HandshakeBuilder postProcessHandshakeRequestAsClient( HandshakeBuilder request ) {
		request.put ( "Sec-WebSocket-Key1" , this.generateKey() );
		request.put ( "Sec-WebSocket-Key2" , this.generateKey() );
		byte[] key3 = new byte[8];
	    (new Random()).nextBytes( key3 );
		request.setContent( key3 );
		return request;
		
	}

	@Override
	public HandshakeBuilder postProcessHandshakeResponseAsServer( Handshakedata request  , HandshakeBuilder response ) throws InvalidHandshakeException {
		super.postProcessHandshakeResponseAsServer( request , response );
		String key1 = request.getFieldValue("Sec-WebSocket-Key1");
		String key2 = request.getFieldValue("Sec-WebSocket-Key2");
		byte[] key3 = request.getContent();
		if (key1 == null || key2 == null || key3 == null || key3.length != 8) {
				throw new InvalidHandshakeException("Bad keys");
		}
		response.setContent( createChallenge( key1 , key2 , key3 ) );
		return response;
	}
	
	private static byte[] getPart(String key) throws InvalidHandshakeException {
		try {
			long keyNumber = Long.parseLong(key.replaceAll("[^0-9]",""));
			long keySpace = key.split("\u0020").length - 1;
			long part = new Long(keyNumber / keySpace);
			return new byte[] {
				(byte)( part >> 24 ),
				(byte)( (part << 8) >> 24 ),
				(byte)( (part << 16) >> 24 ),
				(byte)( (part << 24) >> 24 )      
			};
		} catch ( NumberFormatException e ) {
			throw new InvalidHandshakeException("invalid Sec-WebSocket-Key (/key2/ or /key3/)");
		}
	}
	
	
	public static byte[] createChallenge( String key1 , String key2, byte[] key3 ) throws InvalidHandshakeException{
			 byte[] part1 = getPart(key1);
			byte[] part2 = getPart(key2);
			byte[] challenge = new byte[16];
			challenge[0] = part1[0];
			challenge[1] = part1[1];
			challenge[2] = part1[2];
			challenge[3] = part1[3];
			challenge[4] = part2[0];
			challenge[5] = part2[1];
			challenge[6] = part2[2];
			challenge[7] = part2[3];
			challenge[8] = key3[0];
			challenge[9] = key3[1];
			challenge[10] = key3[2];
			challenge[11] = key3[3];
			challenge[12] = key3[4];
			challenge[13] = key3[5];
			challenge[14] = key3[6];
			challenge[15] = key3[7];
			 MessageDigest md5;
			try {
				md5 = MessageDigest.getInstance("MD5");
			} catch ( NoSuchAlgorithmException e ) {
				throw new RuntimeException(e);
			}
			return md5.digest(challenge);
	}
}
