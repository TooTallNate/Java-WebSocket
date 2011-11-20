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

public class Draft_76 extends Draft_75 {

	@Override
	public boolean acceptHandshakeAsClient( Handshakedata request , Handshakedata response ) {
		if( response.getContent ().length >= 20 && new String ( response.getContent () ).endsWith ( "\r\n\r\n" )){
			/*{ //TODO finish function
				  if ( reply == null ) {
				        return false;
				      }
				      byte[] challenge = new byte[] {
				        (byte)( this.number1 >> 24 ),
				        (byte)( (this.number1 << 8) >> 24 ),
				        (byte)( (this.number1 << 16) >> 24 ),
				        (byte)( (this.number1 << 24) >> 24 ),
				        (byte)(  this.number2 >> 24 ),
				        (byte)( (this.number2 << 8) >> 24 ),
				        (byte)( (this.number2 << 16) >> 24 ),
				        (byte)( (this.number2 << 24) >> 24 ),
				        this.key3[0],
				        this.key3[1],
				        this.key3[2],
				        this.key3[3],
				        this.key3[4],
				        this.key3[5],
				        this.key3[6],
				        this.key3[7]
				      };
				      MessageDigest md5;
					  try {
						md5 = MessageDigest.getInstance( "MD5" );
					  } catch ( NoSuchAlgorithmException e ) {
						throw new RuntimeException ( e );//Will never occur on a valid jre.
					  }
				      byte[] expected = md5.digest(challenge);
				      for (int i = 0; i < reply.length; i++) {
				        if (expected[i] != reply[i]) {
				          return false;
				        }
				      } 
			}*/
			return true;
		}
		return false;
	}

	@Override
	public boolean acceptHandshakeAsServer( Handshakedata handshakedata ) {
		
		if( handshakedata.getFieldValue ( "Sec-WebSocket-Key1" ).equals ( "8" ) && new String ( handshakedata.getContent () ).endsWith ( "\r\n\r\n" ))
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
		return request;
		
	}
	
	

	@Override
	public HandshakeBuilder postProcessHandshakeResponseAsServer( Handshakedata response , HandshakeBuilder request ) {
		throw new RuntimeException ( "not yet implemented" );
	}
}
