package com.malchrosoft.cammerger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.malchrosoft.cammerger.gui.CamMergerFrame;
import com.malchrosoft.cammerger.gui.Refreshable;
import com.malchrosoft.debug.Log;

public class CamMergerFrameControllerNew
{
	// desired fps
	private final static int MAX_FPS = 25;
	// maximum number of frames to be skipped
	private final static int MAX_FRAME_SKIPS = 10;
	// the frame period
	private final static int FRAME_PERIOD = 1000 / MAX_FPS;
	private final static int TAILLE_TAMPON = 20;
	private final static int MAX_CAMS_COUNT = 6;

	private final BufferedImage[] tempImgs;
	private int tempConsuption;
	private boolean tempImgInitialized;

	private CamMergerFrame frame;
	private List<Webcam> cams;

	private boolean launched;
	private List<Webcam> effectiveCams;
	private Map<Webcam, BufferedImage> currentImages;

	private Refreshable guiViewRefresh;

	private Map<Webcam, Dimension> camResolutions;

	public CamMergerFrameControllerNew()
	{
		this.frame = new CamMergerFrame();
		this.cams = Webcam.getWebcams();
		this.launched = false;
		this.tempImgs = new BufferedImage[TAILLE_TAMPON];
		Dimension vgaSize = WebcamResolution.VGA.getSize();

		for (int i = 0; i < TAILLE_TAMPON; i++)
		{
			this.tempImgs[i] = new BufferedImage(vgaSize.width, vgaSize.height, BufferedImage.TYPE_INT_RGB);
		}
		this.tempConsuption = 0;

		this.effectiveCams = new ArrayList<>();
		this.camResolutions = new HashMap<>();
		this.currentImages = new LinkedHashMap<>();

		Dimension bestResolution;
		Dimension optimalResolution;
		for (Webcam cam : cams)
		{
			try
			{
				bestResolution = cam.getViewSize();
				optimalResolution = bestResolution;
				int biggestResolution = 0;
				int optimal = vgaSize.height * vgaSize.width;
				for (Dimension s : cam.getViewSizes())
				{
					if (s.width * s.height > biggestResolution)
					{
						biggestResolution = s.width * s.height;
						Log.warn(s + "");
						bestResolution = s;
					}
					if (s.width * s.height == optimal)
					{
						optimalResolution = s;
					}
				}
				Log.info("best resolution for " + cam.getName() + " : " + bestResolution + " -> optimal : "
					+ optimalResolution);
				cam.setViewSize(optimalResolution);
				this.camResolutions.put(cam, optimalResolution);
				cam.open();
			} catch (Exception e)
			{
				e.printStackTrace();
				continue;
			}
			effectiveCams.add(cam);
			if (effectiveCams.size() >= MAX_CAMS_COUNT) break;
		}
//		effectiveCams.addAll(Arrays.asList(effectiveCams.get(0)));

		guiViewRefresh = new Refreshable()
		{
			@Override
			public void refresh(int index)
			{
				Graphics2D g = frame.getDrawGraphics();
				g.drawImage(tempImgs[index], 0, 0, 200, 150, null);
				refreshMousePosition(g);
				frame.redrawGraphics();
			}
		};

		initializationReport();
		initListeners();
	}

	private void initListeners()
	{

	}

	private void initializationReport()
	{
		Log.info("Effective webcams : " + this.effectiveCams.size());
	}

	public synchronized void launchRefreshLoop()
	{
		if (launched) return;
		Log.info("-- launchRefreshLoop --");

		// Boucle de rafraichissement generale
		Thread loopThread = new Thread(new Runnable()
		{
			private long beginTime; // the time when the cycle begun
			private long timeDiff; // the time it took for the cycle to execute
			private int sleepTime; // ms to sleep (<0 if we're behind)
			private int framesSkipped; // number of frames being skipped

			@Override
			public void run()
			{
				frame.setVisible(true);
				while (true)
				{
					beginTime = System.currentTimeMillis();
					framesSkipped = 0; // resetting the frames skipped
					// graphics refresh
					update();
					refresh();

					// calculate how long did the cycle take
					timeDiff = System.currentTimeMillis() - beginTime;
					// calculate sleep time
					sleepTime = (int) (FRAME_PERIOD - timeDiff);
					frame.setTitle(CamMerger.TITLE);
					if (sleepTime > 0)
					{
						// if sleepTime > 0 we're OK
						try
						{
							// send the thread to sleep for a short period
							// very useful for battery saving
							Thread.sleep(sleepTime);
							// Thread.sleep(FRAME_PERIOD);
						} catch (InterruptedException e)
						{
						}
					}
					while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS)
					{
						// we need to catch up
						// update without rendering
						// ???
						// update();

						// add frame period to check if in next frame
						sleepTime += FRAME_PERIOD;
						framesSkipped++;
					}
				}
			}
		});
		loopThread.start();
		launched = true;
	}

	private synchronized void update()
	{
		for (Webcam c : this.effectiveCams)
		{
			this.currentImages.put(c, c.getImage());
		}
	}

	private synchronized void refresh()
	{
		if (tempImgInitialized)
		{
			this.guiViewRefresh.setIndex(getDecalValue(TAILLE_TAMPON, tempConsuption, 1));
			new Thread(this.guiViewRefresh).start();
		}
		tempConsuption++;
		refreshGraphics(tempImgs[tempConsuption - 1].createGraphics());
		if (tempConsuption >= TAILLE_TAMPON)
		{
			if (!tempImgInitialized) tempImgInitialized = true;
			tempConsuption = 0;
		}
	}

	private int getDecalValue(int size, int current, int decal)
	{
		return (current + decal) % (size - 1);
	}

	private void refreshMousePosition(Graphics2D g)
	{
		g.setColor(Color.GREEN);
		Point mp = frame.getMousePositionOnCanvas();
		if (mp != null)
		{
			g.drawString("Mouse : " + mp.x + ", " + mp.y, 5, 600 - 4);
			g.drawLine(mp.x, mp.y - 20, mp.x, mp.y + 20);
			g.drawLine(mp.x - 20, mp.y, mp.x + 20, mp.y);
		}
		else
		{
			g.drawString("Mouse not over...", 5, 600 - 4);
		}
	}

	private void refreshGraphics(Graphics2D g)
	{
		// g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, 800, 600);

		int camCount = this.effectiveCams.size();
		int num = 0;
		int x, y, width, height;

		y = 0;
		width = 800 / camCount;
		height = 600 / camCount;
		for (Webcam c : this.effectiveCams)
		{
			x = (800 / camCount) * num;

			g.drawImage(this.currentImages.get(c), x, y, width, height, null);
			// g.drawImage(tempImage, 0, 0, tempImage.getWidth(), tempImage.getHeight(), null);

			g.setColor(Color.black);
			g.drawLine(x + width - 1, y, x + width - 1, y + height);
			g.drawLine(x, y + height - 1, x + width, y + height - 1);

			// Debug draw
			g.setColor(Color.lightGray);
			g.drawString("Camera : " + c.getName(), x + 4, y + height - 4);
			g.drawString("Resolution : " + c.getViewSize().width + "x" + c.getViewSize().height, x + 4, y + height - 20);
			Font current = g.getFont();
			g.setFont(new Font("Arial", Font.BOLD, 20));
			g.drawString("" + (num + 1), x + 4, y + 20);
			g.setFont(current);

			num++;
		}
		g.dispose();
	}

}
