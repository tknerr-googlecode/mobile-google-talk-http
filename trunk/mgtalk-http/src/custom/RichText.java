package custom;
import java.util.Vector;

import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * 
 */
//package javax.microedition.lcdui;

/**
 * @author Kostya
 *
 */
public class RichText extends CustomItem {

	private int canvasWidth;
	private Font normal = Font.getFont(0, Font.STYLE_PLAIN, 0);
	private Font bold = Font.getFont(0, Font.STYLE_BOLD, 0);
	private int defaultColor = -1;
	int textHeight = 0;
	private Vector items = new Vector();
	boolean finished = false;
	
	public RichText(Display d) {
		super(null);
		defaultColor = d.getColor( Display.COLOR_FOREGROUND );
		recalcHeight();
		// TODO Auto-generated constructor stub
	}

	public void finish()
	{
		finished = true;
	}
	protected int getMinContentWidth() {
		// TODO Auto-generated method stub
		return getPrefContentWidth(0);
	}

	protected int getMinContentHeight() {
		// TODO Auto-generated method stub
		return getPrefContentHeight(0);
	}

	protected int getPrefContentWidth(int arg0) {
		// TODO Auto-generated method stub
//		System.out.println("getPrefContentWidth: arg0 = "+arg0);
		return getCanvasWidth();
	}

	protected int getPrefContentHeight(int arg0) {
		// TODO Auto-generated method stub
//		System.out.println("getPrefContentHeight: arg0 = "+arg0);
		return textHeight+5;
	}

	protected void paint(Graphics arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		if(!finished)
			return;

		int x = 0;
		int y = 0;
		int height = 0;
		
		for(int i=0; i<items.size(); i++)
		{
			RichItem ri = (RichItem) items.elementAt(i);
			ri.paint(arg0, x, y, height);
			x = ri.getX();
			y = ri.getY();
//			System.out.println("paint = "+y);
			height = ri.getHeight();
		}
		
//		System.out.println("paint: arg1 = "+arg1+", arg2 = "+arg2);
		
	}

	/**
	 * @return Returns the canvasWidth.
	 */
	public int getCanvasWidth() {
		return canvasWidth==0?1:canvasWidth;
	}

	/**
	 * @param canvasWidth The canvasWidth to set.
	 */
	public void setCanvasWidth(int canvasWidth) {
		this.canvasWidth = canvasWidth;
	}

	/**
	 * @return Returns the bold.
	 */
	public Font getBold() {
		return bold;
	}

	/**
	 * @return Returns the normal.
	 */
	public Font getNormal() {
		return normal;
	}

	/**
	 * @return Returns the defaultColor.
	 */
	public int getDefaultColor() {
		return defaultColor;
	}

	public void recalcHeight()
	{
		int x =0;
		int y = 0;
		int height = 0;
		
		for(int i=0; i<items.size(); i++)
		{
			RichItem ri = (RichItem) items.elementAt(i);
			ri.recalcHeight(x, y, height);
			x = ri.getX();
			y = ri.getY();
			height = ri.getHeight();
//			System.out.println("recalcHeight() cycle = "+y);
		}
		textHeight = y==0?height:y;
		setPreferredSize(getPrefContentWidth(0), (y==0?height:y)+3);
	}
	
	public void addContent(String content, int color, boolean bold)
	{
		if(finished)
			return;
		RichItem ri = new RichItem(this);
		ri.setContent(content, color, bold);
		items.addElement(ri);
		recalcHeight();
//		sizeChanged(getPreferredWidth(), getPreferredHeight());
	}

	public void addContent(String content, int color)
	{
		addContent(content, color, false);
	}

	public void addContent(String content)
	{
		addContent(content, getDefaultColor(), false);
	}
	
	public void reset()
	{
		items.removeAllElements();
		finished = false;
	}
	
	public void addImage(Image img)
	{
		if(finished)
			return;		
		RichItem ri = new RichItem(this);
		ri.setImage(img);
		items.addElement(ri);
		recalcHeight();
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CustomItem#keyPressed(int)
	 */
//	protected void keyPressed(int arg0) {
//		// TODO Auto-generated method stub
//		if(arg0==-5)
//		{
//			notifyStateChanged();
//			return;
//		}
//		super.keyPressed(arg0);
//	}
	
}
