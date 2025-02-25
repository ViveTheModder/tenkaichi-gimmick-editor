package gui;
//having to do this just so the table contents are recognized as floats is crazy...
import javax.swing.table.DefaultTableModel;

public class FloatTableModel extends DefaultTableModel 
{
	public FloatTableModel(Float[][] data, String[] cols)
	{
		super(data,cols);
	}
	@Override
    public Class getColumnClass(int col) 
	{
		return Float.class;
	}
}
