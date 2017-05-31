package org.java_websocket.drafts;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.NotSendableException;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.framing.Framedata.Opcode;
import org.java_websocket.util.Charsetfunctions;

public class Draft_18 extends Draft_17{
	@Override
	public List<Framedata> createFrames( String text, boolean mask ) {
		int frame_len = 2048;
		byte[] bytes = Charsetfunctions.utf8Bytes( text );
		ArrayList<Framedata> list = new ArrayList<Framedata>();
		
		int length = bytes.length;
		for(int i = 0; i < length ; i += frame_len){
			
			int begin = i;
			int end = i + frame_len - 1;
			if(end >= length){
				end = length - 1;
			}
			
			byte[] data = copyBytes(bytes, begin, end);
			if(data == null){
				System.err.println("Draft_18.copyBytes has wrong!");
				continue;
			}
			
			FrameBuilder curframe = new FramedataImpl1();
			try {
				curframe.setPayload( ByteBuffer.wrap( data ));
			} catch ( InvalidDataException e ) {
				throw new NotSendableException( e );
			}
			
			if(end == length - 1){
				curframe.setFin(true);
			}else{
				curframe.setFin(false);
			}
			
			curframe.setOptcode( Opcode.TEXT );
			curframe.setTransferemasked( mask );
			
			list.add(curframe);
		}
		
		return list;
	}
	
	private byte[] copyBytes(final byte[] src, int begin, int end){
		if(end < begin)
			return null;
		if(begin >= src.length)
			return null;
		if(begin < 0)
			begin = 0;
		if(end < 0)
			return null;
		if(end >= src.length)
			end = src.length -1;
		byte[] bytes  = new byte[end-begin+1];
		int index = 0;
		for(int i = begin; i <= end; i++){
			bytes[index] = src[i];
			index++;
		}
		
		return bytes;
	}

}
