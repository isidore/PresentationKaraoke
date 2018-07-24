package com.spun.powerpointkaraoke;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.spun.util.Colors;
import com.spun.util.NumberUtils;
import com.spun.util.ObjectUtils;
import com.spun.util.WindowUtils;
import com.spun.util.io.FileUtils;
import com.spun.util.io.SimpleFileFilter;
import com.spun.util.logger.SimpleLogger;

public class Viewer extends JPanel implements KeyListener
{
  private static final int NEW_SPEAKER    = -1;
  private File[]           slides;
  private BufferedImage    slide;
  int                      totalIndex     = 0;
  int                      currentIndex   = NEW_SPEAKER;
  private int              numberOfSlides = 5;
  private String           directory;
  public Viewer(String directory)
  {
    setSlideDirectory(directory);
    addKeyListener(this);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    this.setPreferredSize(d);
    setFocusable(true);
  }
  private void setSlideDirectory(String directory)
  {
    this.directory = directory;
    loadPictures(readAllFiles());
  }
  @Override
  public void paint(Graphics g)
  {
    try
    {
      super.paint(g);
      Image scaled = slide.getScaledInstance(this.getWidth(), this.getHeight(), Image.SCALE_DEFAULT);
      g.drawImage(scaled, 0, 0, this);
    }
    catch (Throwable t)
    {
      // do nothing
    }
  }
  public static void main(String[] args)
  {
    launch(false);
  }
  private static String getDirectoryForImages(boolean force)
  {
    Preferences prefs = Preferences.userNodeForPackage(Viewer.class);
    String directory = prefs.get("directory", null);
    if (!force && directory != null && new File(directory).isDirectory())
    {
      return directory;
    }
    else
    {
      File starting = new File(directory == null ? "." : directory);
      JFileChooser j = new JFileChooser();
      j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      j.setCurrentDirectory(starting);
      Integer opt = j.showOpenDialog(null);
      directory = j.getSelectedFile().getAbsolutePath();
      prefs.put("directory", directory);
      return directory;
    }
  }
  private static void launch(boolean askForDirectory)
  {
    Viewer panel = new Viewer(getDirectoryForImages(askForDirectory));
    JFrame frame = new JFrame("Presentation Karaoke");
    frame.getContentPane().add(panel);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.setVisible(true);
    frame.setCursor(frame.getToolkit().createCustomCursor(new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB),
        new Point(0, 0), "null"));
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    gd.setFullScreenWindow(frame);
    WindowUtils.testFrame(frame, new WindowAdapter[]{});
  }
  private File[] readAllFiles()
  {
    File dir = new File(directory);
    return FileUtils.getRecursiveFileList(dir, new SimpleFileFilter());
  }
  private void advance()
  {
    if (slides.length <= totalIndex)
    {
      this.loadPictures(slides);
    }
    currentIndex++;
    boolean advanced = showStartScreen() || showEndScreen() || showNextSlide();
  }
  private void goBack()
  {
    boolean advanced = backFromStartScreen() || backFromNextSlide() || backFromEndScreen();
  }
  private boolean backFromEndScreen()
  {
    if (numberOfSlides < currentIndex)
    {
      currentIndex--;
      totalIndex -= 2;
      showNextSlide();
    }
    return false;
  }
  private boolean backFromNextSlide()
  {
    if (0 < currentIndex && currentIndex <= numberOfSlides)
    {
      currentIndex -= 2;
      totalIndex = Integer.max(0, totalIndex - 2);
      advance();
      return true;
    }
    return false;
  }
  private boolean backFromStartScreen()
  {
    // doNothing
    return (currentIndex == 0);
  }
  private boolean showNextSlide()
  {
    try
    {
      slide = ImageIO.read(slides[totalIndex++]);
    }
    catch (IOException e)
    {
      throw ObjectUtils.throwAsError(e);
    }
    return true;
  }
  private boolean showStartScreen()
  {
    if (currentIndex != 0) { return false; }
    slide = createBlankSlide(Color.white, "Presenting...");
    return true;
  }
  private BufferedImage createBlankSlide(Color color, String text)
  {
    Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    BufferedImage img = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_BGR);
    Graphics2D g = img.createGraphics();
    Font f = new Font("Times New Roman", Font.BOLD, 100);
    g.setFont(f);
    g.setColor(color);
    drawCenteredString(g, text, d.width / 2, d.height / 2);
    g.dispose();
    return img;
  }
  private boolean showEndScreen()
  {
    if (currentIndex <= numberOfSlides) { return false; }
    slide = createBlankSlide(Colors.Reds.IndianRed, "The End.");
    currentIndex = NEW_SPEAKER;
    return true;
  }
  private void loadPictures(File... slides)
  {
    this.slides = NumberUtils.getShuffled(slides, slides.length);
    totalIndex = 0;
    currentIndex = NEW_SPEAKER;
    advance();
  }
  @Override
  public void keyTyped(KeyEvent e)
  {
  }
  @Override
  public void keyPressed(KeyEvent e)
  {
  }
  @Override
  public void keyReleased(KeyEvent e)
  {
    int keyCode = e.getKeyCode();
    //System.out.println("code: " + keyCode);
    if (keyCode == 27) //esc
    {
      showExitScreen();
    }
    else if (keyCode == 37)
    {
      goBack();
    }
    else
    {
      advance();
    }
    repaint();
  }
  private void showExitScreen()
  {
    String[] answers = new String[]{"Exit", "Change Slide Directory", "Cancel"};
    final int CANCEL = 2;
    final int DIRECTORY = 1;
    int response = JOptionPane.showOptionDialog(this, "Would you like to Exit?", "Exit",
        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, answers, answers[0]);
    SimpleLogger.variable("response", response);
    switch (response)
    {
      case CANCEL :
        // do nothing
        break;
      case DIRECTORY :
        SwingUtilities.getWindowAncestor(this).dispose();
        launch(true);
        break;
      default :
        System.exit(0);
    }
  }
  public static void drawCenteredString(Graphics g, String text, int x, int y)
  {
    FontMetrics metrics = g.getFontMetrics();
    int width = metrics.stringWidth(text);
    x -= (width / 2);
    int height = metrics.getHeight();
    y -= (height / 2);
    y += metrics.getAscent();
    g.drawString(text, x, y);
  }
}
