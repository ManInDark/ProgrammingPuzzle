package abstractclasses;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLayeredPane;

import frame.Frame;
import frame.UserInputInterpreter;
import logic.Constants;
import logic.Debugger;
import world.Images;


public abstract class CustomWindow extends JLayeredPane implements Comparable<CustomWindow>,Constants {

	private int layer = 10000;
	private String title = "Default";
	
	private JButton close = new JButton(new ImageIcon(Images.getImage("rsc/Gui/Window/close.png")));
	private JButton maximise = new JButton(new ImageIcon(Images.getImage("rsc/Gui/Window/maximise.png")));

	private Point mousePoint = new Point();
	private Point reversemousePoint = new Point();
	private Point bottomRight = new Point();

	private Dimension newsize = new Dimension();

	private Mousestate mousestate;
	
	private Canvas window;

	enum Mousestate {
		DEFAULT_CURSOR,MOVE_CURSOR,E_RESIZE_CURSOR,S_RESIZE_CURSOR,N_RESIZE_CURSOR,W_RESIZE_CURSOR,SE_RESIZE_CURSOR,
		SW_RESIZE_CURSOR,NW_RESIZE_CURSOR,NE_RESIZE_CURSOR,CLOSE,MAXIMAISE
	}

	public CustomWindow(String title) {
		this(DEFAULTWITH,DEFAULTHEIGHT,title);
	}

	public CustomWindow(int defaultWidht,int defaultHeight,String title) {
		this(defaultWidht,defaultHeight,new Point(DEFAULTX,DEFAULTY),title,1);
	}

	public CustomWindow(int defaultWidht,int defaultHeight,Point defaultPosition,String title,int level) {
		setLocation(defaultPosition.x,defaultPosition.y);
		
		window = new Canvas();
		window.setLocation(0,0);
		
		setSize(
				Frame.getWidth() > defaultWidht + defaultPosition.getX() ? defaultWidht
						: Frame.getWidth() - (int) defaultPosition.getX(),
				Frame.getHeight() > defaultHeight + defaultPosition.getY() ? defaultHeight
						: Frame.getHeight() - (int) defaultPosition.getY());
		
		setnewSize(getSize().width,getSize().height);
		
		window.setSize(getSize());
		
		setTitle(title + "  " + new Random().nextInt(999));

		close.setBounds(getWidth() - 27,6,17,17);
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Frame.getWindowManager().removeWindow(getThis());
			}
		});
		close.setFocusPainted(false);
		close.setRolloverEnabled(false);
		
		maximise.setBounds(getWidth() - 47,6,17,17);
		maximise.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setnewLocation(0,0);
				setnewSize(Frame.getMaxDimension().width,Frame.getMaxDimension().height);
			}
		});
		maximise.setFocusPainted(false);
		maximise.setRolloverEnabled(false);

		Frame.getWindowManager().addWindow(level,this);

		window.addKeyListener(new UserInputInterpreter());

		window.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				Frame.getWindowManager().windowToFront(getThis());
				saveBounds(e.getPoint());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				processMousePressedEvent(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				Frame.getFrame().setCursor(Cursor.getDefaultCursor());
			}

		});

		window.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				changeCursor(e);
				processMouseMovedEvent(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				processMouseMovedEvent(e);
				drag(e);
			}

		});

		window.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				processMouseWheelMovedEvent(e);
			}

		});

		add(window,JLayeredPane.DEFAULT_LAYER);
		
		add(close,JLayeredPane.PALETTE_LAYER);
		add(maximise,JLayeredPane.PALETTE_LAYER);
		
		close.repaint();
		maximise.repaint();
		
		window.setIgnoreRepaint(true);
		window.createBufferStrategy(3);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public CustomWindow getThis() {
		return this;
	}

	public void Render() {
		Debugger.startDraw(this);

		setSize(newsize);
		window.setSize(newsize);
		
		close.setBounds(getWidth() - 27,6,17,17);
		maximise.setBounds(getWidth() - 47,6,17,17);
		
		BufferedImage draw = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = draw.createGraphics();

		BufferedImage drawimage;

		try {
			drawimage = getImage();
		} catch (Exception e) {
			e.printStackTrace();
			drawimage = getErrorImage();
		}

		g.setColor(Color.black);
		g.fillRect(0,0,getWidth(),getHeight());
		
		if (drawimage != null)
			g.drawImage(drawimage,getImageborders().x,getImageborders().y,null);

		g.drawImage(Images.getImage("rsc/Gui/Window/top left.png"),0,0,18,30,null);
		g.drawImage(Images.getImage("rsc/Gui/Window/top.png"),18,0,getWidth() - 34,30,null);
		g.drawImage(Images.getImage("rsc/Gui/Window/top right.png"),getWidth() - 18,0,null);
		g.drawImage(Images.getImage("rsc/Gui/Window/side.png"),0,30,18,getHeight() - 48,null);
		g.drawImage(Images.getImage("rsc/Gui/Window/side.png"),getWidth() - 18,30,18,getHeight() - 48,null);
		g.drawImage(Images.getImage("rsc/Gui/Window/bottom left.png"),0,getHeight() - 18,18,18,null);
		g.drawImage(Images.getImage("rsc/Gui/Window/bottom.png"),18,getHeight() - 18,getWidth(),18,null);
		g.drawImage(Images.getImage("rsc/Gui/Window/bottom right.png"),getWidth() - 18,getHeight() - 18,null);

		// Zeichnet den Titel des Fensters
		g.setColor(Color.BLACK);
		g.setFont(new Font("Consolas",Font.BOLD,18));
		String newtitle = "";
		for (int i = 0; i < title.length() * 10; i += 10) {
			if (i + (SIDEBARWIDTH * 0.7) < maximise.getX() - maximise.getWidth())
				newtitle += title.charAt(i / 10);
			else
				break;
		}
		g.drawString(newtitle,(int) (SIDEBARWIDTH * 0.7),20);

		g.dispose();
		BufferStrategy bs = window.getBufferStrategy();

		do {
			do {
				Graphics g2 = bs.getDrawGraphics();
				g2.drawImage(draw,0,0,null);
				g2.dispose();

			} while (bs.contentsRestored());
			bs.show();
		} while (bs.contentsLost());

		close.repaint();
		maximise.repaint();

		Debugger.endDraw(this);
	}

	/**
	 * Gibt ein BufferedImage zur�ck, das die Ausma�e des Fensters hat.
	 */
	protected BufferedImage getEmptyImage() {
		return new BufferedImage(getImageborders().width,getImageborders().height,BufferedImage.TYPE_INT_RGB);
	}

	/**
	 * Gibt die Ausma�e des Fensters zur�ck.
	 * 
	 * @return
	 */
	public Rectangle getImageborders() {
		return new Rectangle(SIDEBARWIDTH,TOPBARWIDTH,getWidth() - SIDEBARWIDTH * 2,
				getHeight() - TOPBARWIDTH - SIDEBARWIDTH);
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public void setnewLocation(int x,int y) {
		setLocation(x,y);
	}

	public void setnewSize(int w,int h) {
		this.newsize = new Dimension(w,h);
	}
	
	public void setComponentBounds(int x,int y, int width,int height, Component component) {
		component.setBounds(x+getImageborders().x,y+getImageborders().y,width,height);
	}
	
	public void setComponentLocation(int x,int y, Component component) {
		setComponentBounds(x,y,component.getWidth(),component.getHeight(),component);
	}

	/**
	 * Gibt ein Fehlerbild zur�ck, das leicht zu erkennen l�sst, das etwas nicht stimmt.
	 * 
	 * @return
	 */
	private BufferedImage getErrorImage() {
		BufferedImage image = getEmptyImage();
		Graphics2D g2 = image.createGraphics();
		g2.setColor(Color.BLUE);
		g2.fillRect(0,0,image.getWidth(),image.getHeight());
		g2.setFont(new Font("Default",Font.BOLD,20));
		g2.setColor(Color.BLACK);
		g2.drawString("Nothing to paint",image.getWidth() / 2 - 60,image.getHeight() / 2);
		g2.dispose();
		return image;
	}

	private static int checkWidht(int check) {
		return Math.min(Math.max(check,DEFAULTMINWIDTH),Frame.getMaxDimension().width);
	}

	private static int checkHeight(int check) {
		return Math.min(Math.max(check,DEFAULTMINHEIGHT),Frame.getMaxDimension().height);
	}

	@SuppressWarnings("incomplete-switch")
	public void drag(MouseEvent e) {
		switch (mousestate) {
			case MOVE_CURSOR:
				setnewLocation(e.getLocationOnScreen().x - mousePoint.x,e.getLocationOnScreen().y - mousePoint.y);
			break;
			case E_RESIZE_CURSOR:
				setnewSize(checkWidht(e.getLocationOnScreen().x - getX() + reversemousePoint.x),getHeight());
			break;
			case S_RESIZE_CURSOR:
				setnewSize(getWidth(),checkHeight(e.getLocationOnScreen().y - getY() + reversemousePoint.y));
			break;
			case N_RESIZE_CURSOR:
				setnewLocation(getX(),e.getLocationOnScreen().y - mousePoint.y);
				setnewSize(getWidth(),checkHeight(bottomRight.y - getY()));
			break;
			case W_RESIZE_CURSOR:
				setnewLocation(e.getLocationOnScreen().x - mousePoint.x,getY());
				setnewSize(checkWidht(bottomRight.x - getX()),getHeight());
			break;
			case SE_RESIZE_CURSOR:
				setnewSize(checkWidht(e.getLocationOnScreen().x - getX() + reversemousePoint.x),
						checkHeight(e.getLocationOnScreen().y - getY() + reversemousePoint.y));
			break;
			case SW_RESIZE_CURSOR:
				setnewLocation(e.getLocationOnScreen().x - mousePoint.x,getY());
				setnewSize(checkWidht(bottomRight.x - getX()),
						checkHeight(e.getLocationOnScreen().y - getY() + reversemousePoint.y));
			break;
			case NW_RESIZE_CURSOR:
				setnewLocation(e.getLocationOnScreen().x - mousePoint.x,e.getLocationOnScreen().y - mousePoint.y);
				setnewSize(checkWidht(bottomRight.x - getX()),checkHeight(bottomRight.y - getY()));
			break;
			case NE_RESIZE_CURSOR:
				setnewLocation(getX(),e.getLocationOnScreen().y - mousePoint.y);
				setnewSize(checkWidht(e.getLocationOnScreen().x - getX() + reversemousePoint.x),
						checkHeight(bottomRight.y - getY()));
			break;
			case DEFAULT_CURSOR:
			break;
		}
	}

	public void saveBounds(Point point) {
		mousePoint = point;
		reversemousePoint = new Point(getWidth() - point.x,getHeight() - point.y);
		bottomRight = new Point(getX() + getWidth(),getY() + getHeight());
	}

	/**
	 * override to process click events in windowimage
	 * 
	 * @param point
	 */
	public void mousePressed(Point point) {
	}

	/**
	 * override to process mousemove events in windowimage leave super.Mousemoved(point);
	 * 
	 * @param point
	 */
	public void mouseMoved(Point point) {
	}

	/**
	 * override to process mouseWheelmove events in windowimage
	 * 
	 * @param direction
	 */
	public void mouseWheelMoved(int direction) {
	}

	public void processMousePressedEvent(MouseEvent e) {
		if (e.getPoint().getX() >= getImageborders().getX()
				&& e.getPoint().getX() <= getImageborders().getWidth() + getImageborders().getX()
				&& e.getPoint().getY() >= getImageborders().getY()
				&& e.getPoint().getY() <= getImageborders().getHeight() + getImageborders().getY()) {

			mousePressed(new Point((int) (e.getPoint().getX() - getImageborders().getX()),
					(int) (e.getPoint().getY() - getImageborders().getY())));
		}
	}

	public void processMouseMovedEvent(MouseEvent e) {
		if (e.getPoint().getX() >= getImageborders().getX() // test if in picture
				&& e.getPoint().getX() <= getImageborders().getWidth() + getImageborders().getX()
				&& e.getPoint().getY() >= getImageborders().getY()
				&& e.getPoint().getY() <= getImageborders().getHeight() + getImageborders().getY()) {

			mouseMoved(new Point((int) (e.getPoint().getX() - getImageborders().getX()),
					(int) (e.getPoint().getY() - getImageborders().getY())));
		}
	}

	public void processMouseWheelMovedEvent(MouseWheelEvent e) {
		if (e.getPoint().getX() >= getImageborders().getX() // test if in picture
				&& e.getPoint().getX() <= getImageborders().getWidth() + getImageborders().getX()
				&& e.getPoint().getY() >= getImageborders().getY()
				&& e.getPoint().getY() <= getImageborders().getHeight() + getImageborders().getY()) {

			mouseWheelMoved(e.getWheelRotation());
		}
	}

	public void changeCursor(MouseEvent e) {
		if (!(e.getPoint().getX() >= getImageborders().getX() // test if not in picture
				&& e.getPoint().getX() <= getImageborders().getWidth() + getImageborders().getX()
				&& e.getPoint().getY() >= getImageborders().getY()
				&& e.getPoint().getY() <= getImageborders().getHeight() + getImageborders().getY())) {
			// edges
			if (e.getPoint().getX() >= 0 && e.getPoint().getX() <= RESIZEWIDTHHEIGHT_INTEGER
					&& e.getPoint().getY() < getHeight() - CORNERWIDTH && e.getPoint().getY() > CORNERWIDTH) {
				Frame.getFrame().setCursor(new Cursor(Cursor.W_RESIZE_CURSOR)); // left
				mousestate = Mousestate.W_RESIZE_CURSOR;
			} else if (e.getPoint().getX() >= getWidth() - RESIZEWIDTHHEIGHT_INTEGER && e.getPoint().getX() <= getWidth()
					&& e.getPoint().getY() < getHeight() - CORNERWIDTH && e.getPoint().getY() > CORNERWIDTH) {
				Frame.getFrame().setCursor(new Cursor(Cursor.E_RESIZE_CURSOR)); // right
				mousestate = Mousestate.E_RESIZE_CURSOR;
			} else if (e.getPoint().getX() > CORNERWIDTH && e.getPoint().getX() < getWidth() - CORNERWIDTH
					&& e.getPoint().getY() <= RESIZEWIDTHHEIGHT_INTEGER && e.getPoint().getY() >= 0) {
				Frame.getFrame().setCursor(new Cursor(Cursor.N_RESIZE_CURSOR)); // top
				mousestate = Mousestate.N_RESIZE_CURSOR;
			} else if (e.getPoint().getX() > CORNERWIDTH && e.getPoint().getX() < getWidth() - CORNERWIDTH
					&& e.getPoint().getY() <= getHeight()
					&& e.getPoint().getY() >= getHeight() - RESIZEWIDTHHEIGHT_INTEGER) {
				Frame.getFrame().setCursor(new Cursor(Cursor.S_RESIZE_CURSOR)); // bottom
				mousestate = Mousestate.S_RESIZE_CURSOR;
			}
			// corners
			else if (e.getPoint().getX() >= 0 && e.getPoint().getX() <= RESIZEWIDTHHEIGHT_INTEGER
					&& e.getPoint().getY() <= CORNERWIDTH && e.getPoint().getY() >= 0) {
				Frame.getFrame().setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR)); // left top left
				mousestate = Mousestate.NW_RESIZE_CURSOR;
			} else if (e.getPoint().getX() >= 0 && e.getPoint().getX() <= CORNERWIDTH
					&& e.getPoint().getY() <= RESIZEWIDTHHEIGHT_INTEGER && e.getPoint().getY() >= 0) {
				Frame.getFrame().setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR)); // left top top
				mousestate = Mousestate.NW_RESIZE_CURSOR;
			} else if (e.getPoint().getX() >= getWidth() - CORNERWIDTH && e.getPoint().getX() <= getWidth()
					&& e.getPoint().getY() <= RESIZEWIDTHHEIGHT_INTEGER && e.getPoint().getY() >= 0) {
				Frame.getFrame().setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR)); // right top top
				mousestate = Mousestate.NE_RESIZE_CURSOR;
			} else if (e.getPoint().getX() >= getWidth() - RESIZEWIDTHHEIGHT_INTEGER && e.getPoint().getX() <= getWidth()
					&& e.getPoint().getY() <= CORNERWIDTH && e.getPoint().getY() >= 0) {
				Frame.getFrame().setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR)); // right top right
				mousestate = Mousestate.NE_RESIZE_CURSOR;
			} else if (e.getPoint().getX() >= getWidth() - CORNERWIDTH && e.getPoint().getX() <= getWidth()
					&& e.getPoint().getY() <= getHeight()
					&& e.getPoint().getY() >= getHeight() - RESIZEWIDTHHEIGHT_INTEGER) {
				Frame.getFrame().setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR)); // right bottom bottom
				mousestate = Mousestate.SE_RESIZE_CURSOR;
			} else if (e.getPoint().getX() >= getWidth() - RESIZEWIDTHHEIGHT_INTEGER && e.getPoint().getX() <= getWidth()
					&& e.getPoint().getY() <= getHeight() && e.getPoint().getY() >= getHeight() - CORNERWIDTH) {
				Frame.getFrame().setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR)); // right bottom right
				mousestate = Mousestate.SE_RESIZE_CURSOR;
			} else if (e.getPoint().getX() >= 0 && e.getPoint().getX() <= RESIZEWIDTHHEIGHT_INTEGER
					&& e.getPoint().getY() <= getHeight()
					&& e.getPoint().getY() >= getHeight() - RESIZEWIDTHHEIGHT_INTEGER) {
				Frame.getFrame().setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR)); // left bottom left
				mousestate = Mousestate.SW_RESIZE_CURSOR;
			} else if (e.getPoint().getX() >= 0 && e.getPoint().getX() <= CORNERWIDTH && e.getPoint().getY() <= getHeight()
					&& e.getPoint().getY() >= getHeight() - CORNERWIDTH) {
				Frame.getFrame().setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR)); // left bottom bottom
				mousestate = Mousestate.SW_RESIZE_CURSOR;
			} else if (e.getPoint().getX() >= getWidth() - 27 && e.getPoint().getX() <= getWidth() - 7
					&& e.getPoint().getY() <= 28 && e.getPoint().getY() >= 8) {
				Frame.getFrame().setCursor(new Cursor(Cursor.HAND_CURSOR)); // close
				mousestate = Mousestate.CLOSE;
			} else if (e.getPoint().getX() >= getWidth() - 57 && e.getPoint().getX() <= getWidth() - 37
					&& e.getPoint().getY() <= 28 && e.getPoint().getY() >= 8) {
				Frame.getFrame().setCursor(new Cursor(Cursor.HAND_CURSOR)); // maxima
				mousestate = Mousestate.MAXIMAISE;
			} else if (e.getPoint().getX() > RESIZEWIDTHHEIGHT_INTEGER
					&& e.getPoint().getX() < getWidth() - RESIZEWIDTHHEIGHT_INTEGER
					&& e.getPoint().getY() < TOPBARWIDTH - RESIZEWIDTHHEIGHT_INTEGER
					&& e.getPoint().getY() > RESIZEWIDTHHEIGHT_INTEGER) {
				Frame.getFrame().setCursor(new Cursor(Cursor.MOVE_CURSOR)); // move
				mousestate = Mousestate.MOVE_CURSOR;
			} else {
				Frame.getFrame().setCursor(Cursor.getDefaultCursor());
				mousestate = Mousestate.DEFAULT_CURSOR;
			}
		} else {
			Frame.getFrame().setCursor(Cursor.getDefaultCursor());
			mousestate = Mousestate.DEFAULT_CURSOR;
		}

	}

	/**
	 * the returned image is cut to windowWidht - 20 and windowHeight - 32
	 * 
	 * @return must return a buffered image
	 */
	public abstract BufferedImage getImage();

	@Override
	public int compareTo(CustomWindow o) {
		return (int) Math.signum(this.getLayer() - o.getLayer());
	}

}
