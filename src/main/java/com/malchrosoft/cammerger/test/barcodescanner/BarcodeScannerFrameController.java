package com.malchrosoft.cammerger.test.barcodescanner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.malchrosoft.debug.Log;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BarcodeScannerFrameController
{
	// desired fps
	private final static int MAX_CAMS_COUNT = 6;
	private final static Dimension resolution = new Dimension(800, 600);

	private BarcodeScannerFrame frame;

	private List<Webcam> cams;
	private List<Webcam> effectiveCams;
	private Map<Webcam, BufferedImage> currentImages;

	private boolean launched;
	private Reader reader;

	public BarcodeScannerFrameController()
	{
		this.frame = new BarcodeScannerFrame();
		this.cams = Webcam.getWebcams();
		this.effectiveCams = new ArrayList<>();
		this.currentImages = new HashMap<>();
		this.launched = false;
		Dimension vgaSize = WebcamResolution.VGA.getSize();

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
						Log.info(s + "");
						bestResolution = s;
					}
					if (s.width * s.height == optimal)
					{
						optimalResolution = s;
					}
				}
				Log.info("best resolution for " + cam.getName() + " : " + bestResolution + " -> optimal : " +
					optimalResolution);
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

		initializationReport();
		initListeners();
	}

	private void initListeners()
	{
		this.frame.getCodebarModeToggleBtn().addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent ce)
			{
				if (!((JToggleButton) ce.getSource()).isSelected())
				{
					frame.getScannedBarcodeLabel().setText("-");
				}
			}

		});
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
			@Override
			public void run()
			{
				reader = new MultiFormatReader();
				frame.setVisible(true);
				while (true)
				{
					update();
					refresh();
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
		Graphics2D g = frame.getCamRendererGraphics();
		refreshGraphics(g);
		refreshMousePosition(g);
		g.dispose();
		frame.redrawCamRenderer();
	}

	private int getDecalValue(int size, int current, int decal)
	{
		return (current + decal) % (size - 1);
	}

	private void refreshMousePosition(Graphics2D g)
	{
		g.setColor(Color.GREEN);
		Point mp = frame.getCamRenderCanvas().getMousePosition();
		if (mp != null)
		{
			g.drawString("Mouse : " + mp.x + ", " + mp.y, 5, resolution.height - 40);
			g.drawLine(mp.x, mp.y - 10, mp.x, mp.y + 10);
			g.drawLine(mp.x - 10, mp.y, mp.x + 10, mp.y);
		}
		else
		{
			g.drawString("Mouse not over...", 5, resolution.height - 40);
		}
	}

	private void refreshGraphics(Graphics2D g)
	{
		// g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 1000, 1000);

		int camCount = this.effectiveCams.size();
		int num = 0;
		int x, y, width, height;

		y = 0;
		width = resolution.width / camCount;
		height = resolution.height / camCount;
		boolean scan = frame.getCodebarModeToggleBtn().isSelected();
		BufferedImage camImg;
		for (Webcam c : this.effectiveCams)
		{
			x = (resolution.width / camCount) * num;
			camImg = this.currentImages.get(c);
			g.drawImage(camImg, x, y, width, height, null);

			if (scan) scanCode(g, camImg, c.getViewSize());
			// g.drawImage(tempImage, 0, 0, tempImage.getWidth(), tempImage.getHeight(), null);

			g.setColor(Color.white);
			g.drawLine(x + width - 1, y, x + width - 1, y + height);
			g.drawLine(x, y + height - 1, x + width, y + height - 1);

			// Debug draw
			g.setFont(new Font("Arial", Font.PLAIN, 12));
			g.setColor(Color.lightGray);
			g.drawString("Camera : " + c.getName(), x + 4, y + height + 15);
			g.drawString("Based resolution : " + c.getViewSize().width + "x" + c.getViewSize().height, x + 4,
				y + height + 30);
			Font current = g.getFont();
			g.setFont(new Font("Arial", Font.BOLD, 20));
			g.drawString("" + (num + 1), x + 4, y + 20);
			g.setFont(current);

			num++;
		}

	}

	private void scanCode(Graphics2D g, BufferedImage camImg, Dimension camResolution)
	{
		int lineBase = 40;
		int lineLength = 20;
		int lineLengthFromBorder = lineBase + lineLength;
		g.setColor(Color.pink);
		g.drawLine(lineBase, lineBase, lineBase, lineLengthFromBorder);
		g.drawLine(lineBase, lineBase, lineLengthFromBorder, lineBase);
		g.drawLine(lineBase, resolution.height - lineBase, lineBase, resolution.height - lineLengthFromBorder);
		g.drawLine(lineBase, resolution.height - lineBase, lineLengthFromBorder, resolution.height - lineBase);

		g.drawLine(resolution.width - lineBase, lineBase, resolution.width - lineBase, lineBase);
		g.drawLine(resolution.width - lineBase, lineBase, resolution.width - lineBase, lineLengthFromBorder);

		BufferedImage buf = new BufferedImage(camResolution.width, camResolution.height, 1); // last arg (1) is the same as TYPE_INT_RGB
		buf.getGraphics().drawImage(camImg, 0, 0, null);
		LuminanceSource source = new BufferedImageLuminanceSource(buf);

		Result result;
		try
		{
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			result = reader.decode(bitmap);
			frame.getScannedBarcodeLabel().setText(result.getText());
		} catch (NotFoundException | ChecksumException | FormatException ex)
		{

		}

	}

}
