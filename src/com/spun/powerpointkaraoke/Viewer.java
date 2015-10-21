package com.spun.powerpointkaraoke;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.spun.util.FrameCloser;
import com.spun.util.NumberUtils;
import com.spun.util.ObjectUtils;
import com.spun.util.WindowUtils;
import com.spun.util.io.FileUtils;
import com.spun.util.io.SimpleFileFilter;

public class Viewer extends JPanel implements KeyListener
{
  private File[]        slides;
  private BufferedImage slide;
  int                   index           = 0;
  private int           numberOfSlides  = 5;
  private boolean       currentlyPaused = false;
  private String        directory;
  public Viewer(String directory)
  {
    this.directory = directory;
    loadPictures(readAllFiles());
    addKeyListener(this);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    this.setPreferredSize(d);
    setFocusable(true);
  }
  @Override
  public void paint(Graphics g)
  {
    super.paint(g);
    Image scaled = slide.getScaledInstance(this.getWidth(), this.getHeight(), Image.SCALE_DEFAULT);
    g.drawImage(scaled, 0, 0, this);
  }
  public static void main(String[] args)
  {
    String commandLine = 0 < args.length ? args[0] : null;
    String directory = getDirectoryForImages(commandLine);
    launch(directory);
  }
  private static String getDirectoryForImages(String commandLine)
  {
    String directory = ".";
    if (commandLine != null)
    {
      directory = commandLine;
    }
    else
    {
      //      boolean forKids = false;
      //      directory = forKids ? "/Users/llewellyn/Desktop/kidskaraoke/" : "/Users/llewellyn/Desktop/Presentation/";
      JFileChooser j = new JFileChooser();
      j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      Integer opt = j.showOpenDialog(null);
      directory = j.getSelectedFile().getAbsolutePath();
    }
    return directory;
  }
  private static void launch(String directory)
  {
    Viewer panel = new Viewer(directory);
    JFrame frame = new JFrame("Presentation Karaoke");
    frame.getContentPane().add(panel);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.setVisible(true);
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    gd.setFullScreenWindow(frame);
    WindowUtils.testFrame(frame, new WindowAdapter[]{new FrameCloser()});
  }
  private File[] readAllFiles()
  {
    File dir = new File(directory);
    return FileUtils.getRecursiveFileList(dir, new SimpleFileFilter());
  }
  private void advance()
  {
    if (slides.length <= index)
    {
      this.loadPictures(slides);
    }
    try
    {
      if (isPaused())
      {
        pause();
      }
      else
      {
        slide = ImageIO.read(slides[index++]);
      }
    }
    catch (IOException e)
    {
      throw ObjectUtils.throwAsError(e);
    }
  }
  private void pause()
  {
    slide = new BufferedImage(10, 10, BufferedImage.TYPE_INT_BGR);
  }
  private boolean isPaused()
  {
    if (index % numberOfSlides == 0 && index != 0)
    {
      currentlyPaused = !currentlyPaused;
    }
    return currentlyPaused;
  }
  private void loadPictures(File... slides)
  {
    this.slides = NumberUtils.getShuffled(slides, slides.length);
    index = 0;
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
    if (keyCode == 27) //esc
    {
      System.exit(0);
    }
    //    System.out.println("code: " + keyCode);
    advance();
    repaint();
  }
}
