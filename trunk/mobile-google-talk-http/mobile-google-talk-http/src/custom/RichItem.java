package custom;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class RichItem {

	private String content = null;
	private Image image = null;
	private int color = -1;
	private boolean bold  = false;
	private Vector words = new Vector();
	private RichText parent;
	private int x;
	private int y;
	private int height;
	private int wordHeight = 0;
	
	public RichItem(RichText parent) 
	{
		super();
		this.parent = parent;
		if(parent.getBold().getHeight()>parent.getNormal().getHeight())
			wordHeight = parent.getBold().getHeight();
		else
			wordHeight = parent.getNormal().getHeight();
	}

	private int wordWidth(String word)
	{
		if(bold)
			return parent.getBold().stringWidth(word);
		else
			return parent.getNormal().stringWidth(word);
	}
	
	private void cutToWords()
	{
		words.removeAllElements();
		String tmp = content;
		for(int i=0; i<tmp.length(); i++)
		{
			if(tmp.charAt(i)==' ' || tmp.charAt(i)==',' || tmp.charAt(i)=='.' || 
					tmp.charAt(i)=='!' || tmp.charAt(i)=='?' || tmp.charAt(i)=='\n' 
						|| tmp.charAt(i)==';' || tmp.charAt(i)==':' || tmp.charAt(i)=='-' || tmp.charAt(i)=='+' ||
						wordWidth(tmp.substring(0, i+1))>parent.getCanvasWidth())
			{
				String word = "";
				if(wordWidth(tmp.substring(0, i+1))>parent.getCanvasWidth())
				{
					word = tmp.substring(0, i);
					tmp = tmp.substring(i, tmp.length());
				}
				else
				{
					word = tmp.substring(0, i+1);
					tmp = tmp.substring(i+1, tmp.length());
				}
				words.addElement(word);
//				System.out.println("Added word ["+word+"]");
				i = -1;
			}
		}
		if(!tmp.equals(""))
		{
			words.addElement(tmp);
//			System.out.println("Added word ["+tmp+"]");
		}
	}
	
	public void setContent(String content, int color, boolean bold)
	{
		this.content = content;
		this.bold = bold;
		this.color = color;
		image = null;
		cutToWords();
	}
	
	public void setImage(Image image)
	{
		this.image = image;
		content = null;
	}
	
	void recalcHeight(int x, int y, int height)
	{
		if(image!=null)
		{
			if(image.getHeight()>height)
				height = image.getHeight();
			if(image.getWidth()+x>parent.getCanvasWidth())
			{
				if(y==0)
					y = height;
				y += height;
				x = image.getWidth();
			}
			else
			{
				if(image.getHeight()>height)
					height = image.getHeight();
				x+=image.getWidth();
			}
		}
		else
		{
			for(int i=0; i<words.size(); i++)
			{
				String word = (String) words.elementAt(i);
				int ww = wordWidth(word);
				if(ww+x>parent.getCanvasWidth())
				{
					if(y==0)
						y = height;
					y += height;
					x = ww;
					height = wordHeight;
				}
				else
				{
					if(wordHeight>height)
						height = wordHeight;
					x+=ww;
				}
				if( word.charAt(word.length()-1)=='\n')
				{
					if(y==0)
						y = height;
					y += height;
					x = 0;
					height = wordHeight;
				}
//				System.out.println("Word = "+word+", x = "+x+", y = "+y+", h = "+height);
			}
		}
		this.x = x;
		this.y = y;
		this.height = height;
	}

	private String getWord(String word)
	{
		if(word.charAt(word.length()-1)=='\n')
			return word.substring(0, word.length()-1);
		return word;
	}
	void paint(Graphics g, int x, int y, int height)
	{
		if(image!=null)
		{
			if(image.getWidth()+x>parent.getCanvasWidth())
			{
				y += height;
				x = image.getWidth();
				g.drawImage(image, 0, y, Graphics.TOP|Graphics.LEFT);
				height = image.getHeight();
				
			}
			else
			{
				g.drawImage(image, x, y, Graphics.TOP|Graphics.LEFT);
				if(image.getHeight()>height)
					height = image.getHeight();
				x+=image.getWidth();
			}			
		}
		else
		{
			for(int i=0; i<words.size(); i++)
			{
				String word = (String) words.elementAt(i);
				g.setColor(color);
				g.setFont(bold?parent.getBold():parent.getNormal());

				
				int ww = wordWidth(word);
				if(ww+x>parent.getCanvasWidth())
				{
					y += height;
					x = ww;
//					System.out.println("Drawing = "+word+", x = 0, y = "+y);
					g.drawString(getWord(word), 0, y, Graphics.TOP|Graphics.LEFT);
					height = wordHeight;
				}
				else
				{
//					System.out.println("Drawing = "+word+", x = "+x+", y = "+y);
					g.drawString(getWord(word), x, y, Graphics.TOP|Graphics.LEFT);

					if(wordHeight>height)
						height = wordHeight;
					x+=ww;
				}
				if( word.charAt(word.length()-1)=='\n')
				{
					y += height;
					x = 0;
					height = wordHeight;
				}
			}			
		}
		this.x = x;
		this.y = y;
		this.height = height;
	}

	/**
	 * @return Returns the x.
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return Returns the y.
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return Returns the height.
	 */
	public int getHeight() {
		return height;
	}

}
