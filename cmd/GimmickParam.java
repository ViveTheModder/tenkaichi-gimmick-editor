package cmd;
//Gimmick Parameter class by ViveTheModder
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import gui.FloatTableModel;
import gui.Main;

public class GimmickParam 
{
	public static final int[] FLOAT_SET_POSITIONS = new int[9];
	public static boolean[] bt2Modes = new boolean[2];
	private static final String[] MODEL_PARTS = {"MANTLE","HEAD","CLOTH"};
	
	public static boolean isCharaCostumePak(File pak, int pakIndex) throws IOException
	{
		LittleEndian.wiiMode=false; //this reset is necessary for the imported file
		RandomAccessFile raf = new RandomAccessFile(pak,"rw");
		int numPakContents = LittleEndian.getInt(raf.readInt());
		System.out.println(numPakContents);
		if (numPakContents<0) //prevent negative seek offset
		{
			numPakContents = LittleEndian.getInt(numPakContents); //reverse byte order
			Main.wiiModes[pakIndex]=true;
			LittleEndian.wiiMode=true;
		}
		raf.seek((numPakContents+1)*4);
		int fileSize = LittleEndian.getInt(raf.readInt());
		int actualFileSize = (int) pak.length();
		raf.close();
		if (fileSize==actualFileSize)
		{
			if (numPakContents==252) 
			{
				bt2Modes[pakIndex]=false;
				int[] bt3Pos = {16,80,144,272,336,464,528,608,672};
				System.arraycopy(bt3Pos, 0, FLOAT_SET_POSITIONS, 0, FLOAT_SET_POSITIONS.length);
				return true;
			}
			else if (numPakContents==250)
			{
				bt2Modes[pakIndex]=true;
				int[] bt2Pos = {12,60,108,204,252,348,396,456,504};
				System.arraycopy(bt2Pos, 0, FLOAT_SET_POSITIONS, 0, FLOAT_SET_POSITIONS.length);
				return true;
			}
			else return false;
		}
		return false;
	}
	public static boolean isValidGimmickParam(File dat, int datIndex) throws IOException
	{
		byte[] gimmick = getGimmickParam(dat);
		if (gimmick.length==896) //BT3 file size
		{
			bt2Modes[datIndex]=false;
			int[] bt3Pos = {16,80,144,272,336,464,528,608,672};
			System.arraycopy(bt3Pos, 0, FLOAT_SET_POSITIONS, 0, FLOAT_SET_POSITIONS.length);
		}
		else if (gimmick.length==704) //BT2 file size
		{
			bt2Modes[datIndex]=true;
			int[] bt2Pos = {12,60,108,204,252,348,396,456,504};
			System.arraycopy(bt2Pos, 0, FLOAT_SET_POSITIONS, 0, FLOAT_SET_POSITIONS.length);
		}
		else return false;
		for (int i=0; i<16; i++)
		{
			//return false if there are no model parts in the header
			if (i==0 && gimmick[i]==0) return false;
			if (i==3)
			{
				//check for byte order based on the last byte of the first float
				if (!(gimmick[i]>0x39 && gimmick[i]<0x4A)) //positive float 
				{
					Main.wiiModes[datIndex] = true; LittleEndian.wiiMode=true;
				}
				else if (!(gimmick[i]>0xB9 && gimmick[i]<0xCA)) //negative float
				{
					Main.wiiModes[datIndex] = true; LittleEndian.wiiMode=true;
				}
			}
			//otherwise, make sure there is at least a valid one
			if (gimmick[i]>=71 && gimmick[i]<=94) return true;
		}
		return false;
	}
	public static byte[] getGimmickParam(File pakOrDat) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(pakOrDat,"rw");
		int pos1=0, size=(int)raf.length();
		if (pakOrDat.getName().toLowerCase().endsWith(".pak"))
		{
			raf.seek(92);
			pos1 = LittleEndian.getInt(raf.readInt());
			raf.seek(96);
			int pos2 = LittleEndian.getInt(raf.readInt());
			size=pos2-pos1;
			System.out.println(pos2+","+pos1);
		}
		byte[] contents = new byte[size];
		raf.seek(pos1);
		raf.read(contents);
		raf.close();
		return contents;
	}
	public static JTable getGimmickTable(File pakOrDat, int fileIndex) throws IOException
	{
		int headerSize=16;
		if (bt2Modes[fileIndex]) headerSize=12;
		byte[] gimmick = getGimmickParam(pakOrDat);
		byte[] header = new byte[headerSize];
		int numModelParts=0;
		String initialName = "GIMMICK_";
		System.arraycopy(gimmick, 0, header, 0, headerSize);
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
				data[i][j] = result;
				pos+=4;
			}
		}
		FloatTableModel model = new FloatTableModel(data,modelPartNames);
		JTable table = new JTable(model);
		return table;
	}
	public static void setGimmickTableFromModel(TableModel model, byte[] contents, int rows, int cols)
	{
		for (int i=0; i<rows; i++)
		{
			int pos = FLOAT_SET_POSITIONS[i];
			for (int j=0; j<cols; j++)
			{
				byte[] floatBytes = new byte[4];
				System.arraycopy(contents, pos, floatBytes, 0, 4);
				float result = LittleEndian.getFloatFromByteArray(floatBytes);
				model.setValueAt(result,i,j);
				pos+=4;
			}
		}
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