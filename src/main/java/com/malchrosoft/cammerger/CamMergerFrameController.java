package com.malchrosoft.cammerger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.github.sarxos.webcam.Webcam;
import com.malchrosoft.cammerger.gui.CamMergerFrame;
import com.malchrosoft.debug.Log;

public class CamMergerFrameController
{
	// desired fps
	private final static int MAX_FPS = 24;
	// maximum number of frames to be skipped
	private final static int MAX_FRAME_SKIPS = 10;
	// the frame period
	private final static int FRAME_PERIOD = 1000 / MAX_FPS;

	private final static int MAX_CAMS_COUNT = 9;

	private final BufferedImage[] tempImgs;
	private int tempConsuption = 0;

	private CamMergerFrame frame;
	private List<Webcam> cams;

	private boolean launched;
	private BufferedImage img, tempImage;
	private List<Webcam> effectiveCams;
	private Dimension bestResolution;

	public CamMergerFrameController()
	{
		this.frame = new CamMergerFrame();
		this.cams = Webcam.getWebcams();
		this.launched = false;
		this.img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		this.tempImgs = new BufferedImage[10];

		this.effectiveCams = new ArrayList<>();
		for (Webcam cam : cams)
		{
			try
			{
				bestResolution = cam.getViewSize();
				int biggestResolution = 0;
				for (Dimension s : cam.getViewSizes())
				{
					if (s.width * s.height > biggestResolution)
					{
						biggestResolution = s.width * s.height;
						bestResolution = s;
					}
				}
				Log.info("best resolution for " + cam.getName() + " : " + bestResolution);
				cam.setViewSize(bestResolution);
				cam.open();
			} catch (Exception e)
			{
				e.printStackTrace();
				continue;
			}
			effectiveCams.add(cam);
			if (effectiveCams.size() >= MAX_CAMS_COUNT) break;
		}

		effectiveCams.addAll(Arrays.asList(effectiveCams.get(0), effectiveCams.get(0)));

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
					refresh();

					// calculate how long did the cycle take
					timeDiff = System.currentTimeMillis() - beginTime;
					// calculate sleep time
					// sleepTime = (int) (FRAME_PERIOD - timeDiff);
					// frame.setTitle(CamMerger.TITLE + " - " + Math.abs(sleepTime) + " FPS");
					// if (sleepTime > 0)
					// {
					// if sleepTime > 0 we're OK
					try
					{
						// send the thread to sleep for a short period
						// very useful for battery saving
						// Thread.sleep(sleepTime);
						Thread.sleep(FRAME_PERIOD);
					} catch (InterruptedException e)
					{
					}
					// }
					// while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS)
					// {
					// // we need to catch up
					// // update without rendering
					// // ???
					//
					// // add frame period to check if in next frame
					// sleepTime += FRAME_PERIOD;
					// framesSkipped++;
					// }

				}
			}
		});
		loopThread.start();
		launched = true;
	}

	private synchronized void refresh()
	{
//		Graphics2D expG = this.img.createGraphics();

		
		
		Graphics2D g = this.frame.getDrawGraphics();
		refreshGraphics(g);
		this.frame.redrawGraphics();
	}
	
	private void refreshGraphics(Graphics2D g)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, 800, 600);
		
		int camCount = this.effectiveCams.size();
		int num = 0;
		Dimension currentCamSize;
		int x, y, width, height;
		Point mp = frame.getMousePositionOnCanvas();

		y = 0;
		width = 800 / camCount;
		height = 600 / camCount;
		for (Webcam c : this.effectiveCams)
		{
			tempImage = c.getImage();

			currentCamSize = c.getViewSize();
			x = (800 / camCount) * num;

			g.drawImage(tempImage, x, y, width, height, null);
			// g.drawImage(tempImage, 0, 0, tempImage.getWidth(), tempImage.getHeight(), null);

			g.setColor(Color.white);
			g.drawLine(x + width - 1, y, x + width - 1, y + height);
			g.drawLine(x, y + height - 1, x + width, y + height - 1);

			// Debug draw
			g.drawString("Camera : " + c.getName(), x + 4, y + height - 4);
			g.drawString("Resolution : " + c.getViewSize().width + "x" + c.getViewSize().height, x + 4, y + height - 20);

			// expG.drawImage(tempImage, (800 / camCount) * num, 0, 800 / camCount, 600 / camCount, null);
			num++;
		}
		g.setColor(Color.GREEN);

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

		
		g.dispose();
	}

}
