package gui;
//Tenkaichi Gimmick Editor v1.2 by ViveTheModder
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import cmd.GimmickParam;
import cmd.LittleEndian;

public class Main 
{
	public static boolean wiiMode=false;
	private static final Toolkit DEF_TOOLKIT = Toolkit.getDefaultToolkit();
	private static final Image ICON = DEF_TOOLKIT.getImage(ClassLoader.getSystemResource("img/icon.png"));
	private static final String FONT_FAMILY = "font-family: Tahoma, Geneva, sans-serif; text-align: center; ";
	private static final String HTML_START = "<html><div style='"+FONT_FAMILY+"font-size: 14px;'>";
	private static final String HTML_TITLE = "<html><div style='"+FONT_FAMILY+"font-size: 20px; font-weight: bold; color: orange;'>";
	private static final String HTML_END = "</div></html>";
	private static final String WINDOW_TITLE = "Tenkaichi Gimmick Editor v1.2";
	private static File getFileFromChooser(int btnIndex) throws IOException
	{
		File pakOrDat=null;
		JFileChooser chooser = new JFileChooser();
		String[] chooserActions = {"Open","Import"};
		FileNameExtensionFilter datFilter = new FileNameExtensionFilter("Model Part Movement File (022_gimmick_param.dat)", new String[]{"dat"});
		FileNameExtensionFilter pakFilter = new FileNameExtensionFilter("Character Costume File (.PAK)", new String[]{"pak"});
		chooser.addChoosableFileFilter(datFilter);
		chooser.addChoosableFileFilter(pakFilter);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(pakFilter);
		chooser.setDialogTitle(chooserActions[btnIndex]+" File...");
		while (true)
		{
			int result = chooser.showOpenDialog(chooser);
			if (result==0)
			{
				File temp = chooser.getSelectedFile();
				String nameLower = temp.getName().toLowerCase();
				if (nameLower.endsWith(".pak"))
				{
					if (GimmickParam.isCharaCostumePak(temp)) 
					{
						pakOrDat=temp; break;
					}
					else 
					{
						wiiMode=false; //disable Wii Mode in case it is accidentally enabled because of the isCharaCostumePak() method
						JOptionPane.showMessageDialog(chooser, "This file is NOT a valid character costume file! Try again!", "Invalid File", 0);
					}
				}
				else if (nameLower.endsWith(".dat"))
				{
					if (GimmickParam.isValidGimmickParam(temp))
					{
						pakOrDat=temp; break;
					}
					else 
					{
						wiiMode=false; //disable Wii Mode in case it is accidentally enabled because of the isValidGimmickParam() method
						JOptionPane.showMessageDialog(chooser, "This file is NOT a valid model part movement file! Try again!", "Invalid File", 0);
					}
				}
				//this line of code is technically unreachable, but I was not aware of setAcceptAllFileFilterUsed() up until now
				else JOptionPane.showMessageDialog(chooser, "This file is NOT a valid file! Try again!", "Invalid File", 0);
			}
			else break;
		}
		return pakOrDat;
	}
	private static void setApp(File pakOrDat) throws IOException
	{
		//initialize components
		Box titleBox = Box.createHorizontalBox();
		Image img = ICON.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
		ImageIcon imgIcon = new ImageIcon(img);
		JButton applyBtn = new JButton(HTML_START+"Apply Changes"+HTML_END);
		JButton importBtn = new JButton(HTML_START+"Import Data from PAK/DAT"+HTML_END);
		JFrame frame = new JFrame(WINDOW_TITLE+" - "+pakOrDat.getAbsolutePath());
		JLabel iconLabel = new JLabel(" ");
		JLabel title = new JLabel(HTML_TITLE+WINDOW_TITLE+"<br>by ViveTheModder"+HTML_END);
		JPanel header = new JPanel();
		JPanel footer = new JPanel();
		JTable table = GimmickParam.getGimmickTable(pakOrDat);
		JTableHeader tableHeader = table.getTableHeader();
		JScrollPane pane = new JScrollPane(table);
		//set component properties
		iconLabel.setIcon(imgIcon);
		tableHeader.setFont(new Font("Tahoma", Font.BOLD, 10));
		tableHeader.setToolTipText("GIMMICK_HEAD often refers to hair model parts, while GIMMICK_CLOTH may refer to belt model parts.");
		table.setFont(new Font("Tahoma", Font.PLAIN, 24));
		table.setRowHeight(table.getRowHeight()+15);
		//add action listeners
		applyBtn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				TableModel newModel = table.getModel();
				try {
					byte[] gimmick = GimmickParam.getGimmickParam(pakOrDat);
					int numModelParts = newModel.getColumnCount();
					int numFloatSets = newModel.getRowCount();
					for (int i=0; i<numFloatSets; i++)
					{
						int posInPak = GimmickParam.FLOAT_SET_POSITIONS[i];
						int posInArr=0;
						byte[] newData = new byte[numModelParts*4];
						for (int j=0; j<numModelParts; j++)
						{
							Float val = (Float)newModel.getValueAt(i,j);
							byte[] arr = LittleEndian.getByteArrayFromFloat(val);
							System.arraycopy(arr, 0, newData, posInArr, 4);
							posInArr+=4;
						}
						System.arraycopy(newData, 0, gimmick, posInPak, newData.length);
					}
					GimmickParam.setGimmickParam(pakOrDat,gimmick);
					DEF_TOOLKIT.beep();
					JOptionPane.showMessageDialog(frame, "Changes applied successfully!", WINDOW_TITLE, 1);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		importBtn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				TableModel newModel = table.getModel();
				try {
					File importedFile = getFileFromChooser(1);
					byte[] gimmick = GimmickParam.getGimmickParam(pakOrDat);
					byte[] importedGimmick = GimmickParam.getGimmickParam(importedFile);
					int numModelParts = newModel.getColumnCount();
					int numFloatSets = newModel.getRowCount();
					for (int i=0; i<numFloatSets; i++)
					{
						int posInPak = GimmickParam.FLOAT_SET_POSITIONS[i];
						System.arraycopy(importedGimmick, posInPak, gimmick, posInPak, 4*numModelParts);
					}
					GimmickParam.setGimmickParam(pakOrDat,gimmick);
					GimmickParam.setGimmickTableFromModel(newModel, gimmick, numFloatSets, numModelParts);
					DEF_TOOLKIT.beep();
					JOptionPane.showMessageDialog(frame, "Gimmick data imported successfully!", WINDOW_TITLE, 1);
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		//add components
		titleBox.add(iconLabel);
		titleBox.add(title);
		header.add(titleBox);
		footer.add(applyBtn);
		footer.add(importBtn);
		frame.add(header,BorderLayout.NORTH);
		frame.add(pane,BorderLayout.CENTER);
		frame.add(footer,BorderLayout.SOUTH);
		//set frame properties
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setIconImage(ICON);
		frame.setSize(1800,500);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
	}
	public static void main(String[] args) 
	{
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			File pakOrDat = getFileFromChooser(0);
			if (pakOrDat!=null) setApp(pakOrDat);
		} 
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | IOException e) 
		{
			e.printStackTrace();
		}
	}
}