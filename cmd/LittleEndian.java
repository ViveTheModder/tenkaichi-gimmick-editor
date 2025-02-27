package cmd;
//Little Endian class by ViveTheModder
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import gui.Main;

public class LittleEndian 
{
	public static float getFloat(float data)
	{
		if (Main.wiiMode) return data;
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.asFloatBuffer().put(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getFloat();
	}
	public static float getFloatFromByteArray(byte[] data)
	{
		ByteBuffer bb = ByteBuffer.wrap(data);
		if (!Main.wiiMode) bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getFloat();
	}
	public static int getInt(int data)
	{
		if (Main.wiiMode) return data;
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.asIntBuffer().put(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}
	public static int getIntFromByteArray(byte[] data)
	{
		ByteBuffer bb = ByteBuffer.wrap(data);
		if (!Main.wiiMode) bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}
	public static byte[] getByteArrayFromFloat(float data)
	{
		ByteBuffer bb = ByteBuffer.allocate(4);
		if (!Main.wiiMode) bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.asFloatBuffer().put(data);
		return bb.array();
	}
	
}
