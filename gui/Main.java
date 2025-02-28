package gui;
//Tenkaichi Gimmick Editor v1.3 by ViveTheModder
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
	public static boolean[] wiiModes = new boolean[2];
	private static final Toolkit DEF_TOOLKIT = Toolkit.getDefaultToolkit();
	private static final Image ICON = DEF_TOOLKIT.getImage(ClassLoader.getSystemResource("img/icon.png"));
	private static final String FONT_FAMILY = "font-family: Tahoma, Geneva, sans-serif; text-align: center; ";
	private static final String HTML_START = "<html><div style='"+FONT_FAMILY+"font-size: 14px;'>";
	private static final String HTML_TITLE = "<html><div style='"+FONT_FAMILY+"font-size: 20px; font-weight: bold; color: orange;'>";
	private static final String HTML_END = "</div></html>";
	private static final String WINDOW_TITLE = "Tenkaichi Gimmick Editor v1.3";
	private static File getFileFromChooser(int btnIndex, int fileIndex) throws IOException
	{
		File pakOrDat=null;
		JFileChooser chooser = new JFileChooser();
		String[] chooserActions = {"Open","Import"};
		FileNameExtensionFilter datFilter = new FileNameExtensionFilter("022_gimmick_param, 022_character_extra_animations (.DAT)", new String[]{"dat"});
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
					if (GimmickParam.isCharaCostumePak(temp,fileIndex)) 
					{
						pakOrDat=temp; break;
					}
					else 
					{
						wiiModes[fileIndex]=false; //disable Wii Mode in case it is accidentally enabled because of the isCharaCostumePak() method
						errorBeep();
						JOptionPane.showMessageDialog(chooser, "This file is NOT a valid character costume file! Try again!", "Invalid File", 0);
					}
				}
				else if (nameLower.endsWith(".dat"))
				{
					if (GimmickParam.isValidGimmickParam(temp,fileIndex))
					{
						pakOrDat=temp; break;
					}
					else 
					{
						wiiModes[fileIndex]=false; //disable Wii Mode in case it is accidentally enabled because of the isValidGimmickParam() method
						errorBeep();
						JOptionPane.showMessageDialog(chooser, "This file is NOT a valid model part movement file! Try again!", "Invalid File", 0);
					}
				}
			}
			else break;
		}
		return pakOrDat;
	}
	private static void errorBeep()
	{
		Runnable runWinErrorSnd = (Runnable) DEF_TOOLKIT.getDesktopProperty("win.sound.exclamation");
		if (runWinErrorSnd!=null) runWinErrorSnd.run();
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
		JTable table = GimmickParam.getGimmickTable(pakOrDat,0);
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
				} catch (Exception e1) {
					errorBeep();
					JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getMessage(), "Exception", 0);
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
					File importedFile = getFileFromChooser(1,1);
					byte[] gimmick = GimmickParam.getGimmickParam(pakOrDat);
					byte[] importedGimmick = GimmickParam.getGimmickParam(importedFile);
					if (GimmickParam.isValidGimmickParam(importedFile,1) || GimmickParam.isCharaCostumePak(importedFile,1))
					{
						if (GimmickParam.bt2Modes[0]==GimmickParam.bt2Modes[1] && wiiModes[0]==wiiModes[1])
						{
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
						}
						else
						{
							String[] fileStates = new String[2];
							String[] fileNames = {pakOrDat.getName(), importedFile.getName()};
							String msg="Currently opened file and imported file are incompatible!\n", regexToGetExt = "^.*\\.(.*)$";
							for (int i=0; i<fileStates.length; i++)
							{
								if (GimmickParam.bt2Modes[i]) fileStates[i]="DBZBT2 File ("+fileNames[i].replaceAll(regexToGetExt, "$1")+")";
								else fileStates[i]="DBZBT3 File ("+fileNames[i].replaceAll(regexToGetExt, "$1")+")";
								if (wiiModes[i]) fileStates[i]+=", Big Endian (Wii)\n";
								else fileStates[i]+=", Little Endian (PS2)\n";
							}
							errorBeep();
							JOptionPane.showMessageDialog(frame, msg+fileStates[0]+fileStates[1], "Incompatible Files", 0);
						}
					}
					else 
					{
						errorBeep();
						JOptionPane.showMessageDialog(frame, "This file is NOT a valid model part movement file!", "Invalid File", 0);
					}
				} catch (Exception e1) {
					errorBeep();
					JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getMessage(), "Exception", 0);
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
			File pakOrDat = getFileFromChooser(0,0);
			if (pakOrDat!=null) setApp(pakOrDat);
		} 
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | IOException e) 
		{
			e.printStackTrace();
		}
	}
}