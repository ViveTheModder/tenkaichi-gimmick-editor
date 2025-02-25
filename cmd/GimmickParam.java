package cmd;
import java.io.File;
//Gimmick Parameter class by ViveTheModder
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.JTable;
import gui.FloatTableModel;

public class GimmickParam 
{
	public static final int[] FLOAT_SET_POSITIONS = {16,80,144,272,336,464,528,608,672};
	private static final String[] MODEL_PARTS = {"MANTLE","HEAD","CLOTH"};
	
	public static boolean isCharaCostumePak(File pak) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(pak,"rw");
		int numPakContents = LittleEndian.getInt(raf.readInt());
		raf.seek((numPakContents+1)*4);
		int fileSize = LittleEndian.getInt(raf.readInt());
		int actualFileSize = (int) pak.length();
		raf.close();
		if (fileSize==actualFileSize && numPakContents==252) return true;
		return false;
	}
	public static boolean isValidGimmickParam(File dat) throws IOException
	{
		byte[] gimmick = getGimmickParam(dat);
		for (int i=0; i<16; i++)
		{
			//return false if there are no model parts in the header
			if (i==0 && gimmick[i]==0) return false;
			//otherwise, make sure there is at least a valid one
			if (gimmick[i]>=71 && gimmick[i]<=94) return true;
		}
		return false;
	}
	public static byte[] getGimmickParam(File pakOrDat) throws IOException
	{
		int pos=0;
		RandomAccessFile raf = new RandomAccessFile(pakOrDat,"rw");
		if (pakOrDat.getName().toLowerCase().endsWith(".pak"))
		{
			raf.seek(92);
			pos = LittleEndian.getInt(raf.readInt());
		}
		byte[] contents = new byte[752];
		raf.seek(pos);
		raf.read(contents);
		raf.close();
		return contents;
	}
	public static JTable getGimmickTable(File pakOrDat) throws IOException
	{
		byte[] gimmick = getGimmickParam(pakOrDat);
		byte[] header = new byte[16];
		int numModelParts=0;
		String initialName = "GIMMICK_";
		System.arraycopy(gimmick, 0, header, 0, 16);
		for (byte b: header)
			if (b!=0) numModelParts++;
		String[] modelPartNames = new String[numModelParts];
		Float[][] data = new Float[FLOAT_SET_POSITIONS.length][numModelParts];
		for (int i=0; i<numModelParts; i++)
		{
			if (header[i]>=87 && header[i]<=94) modelPartNames[i]=initialName+MODEL_PARTS[2]+(header[i]-86);
			else if (header[i]>=79) modelPartNames[i]=initialName+MODEL_PARTS[1]+(header[i]-78);
			else if (header[i]>=71) modelPartNames[i]=initialName+MODEL_PARTS[0]+(header[i]-70);
		}
		for (int i=0; i<FLOAT_SET_POSITIONS.length; i++)
		{
			int pos = FLOAT_SET_POSITIONS[i];
			for (int j=0; j<numModelParts; j++)
			{
				byte[] floatBytes = new byte[4];
				System.arraycopy(gimmick, pos, floatBytes, 0, 4);
				float result = LittleEndian.getFloatFromByteArray(floatBytes);
				data[i][j] = result;//Float.toString(result);
				pos+=4;
			}
		}
		FloatTableModel model = new FloatTableModel(data,modelPartNames);
		JTable table = new JTable(model);
		return table;
	}
	public static void setGimmickParam(File pakOrDat, byte[] contents) throws IOException
	{
		int pos=0;
		RandomAccessFile raf = new RandomAccessFile(pakOrDat,"rw");
		if (pakOrDat.getName().toLowerCase().endsWith(".pak"))
		{
			raf.seek(92);
			pos = LittleEndian.getInt(raf.readInt());
		}
		raf.seek(pos);
		raf.write(contents);
		raf.close();
	}
}