package com.malchrosoft.cammerger;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

/**
 * Proof of concept of how to handle webcam video stream from Java
 *
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamViewerExample extends JFrame implements Runnable, WebcamListener, WindowListener,
	UncaughtExceptionHandler, ItemListener
{

	private static final long serialVersionUID = 1L;

	private Webcam webcam = null;
	private WebcamPanel panel = null;

	@Override
	public void run()
	{

		setTitle("Java Webcam Capture POC");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		addWindowListener(this);

		for (Webcam wc : Webcam.getWebcams())
		{
			webcam = wc;
		}

		if (webcam == null)
		{
			System.out.println("No webcams found...");
			System.exit(1);
		}

		webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.addWebcamListener(WebcamViewerExample.this);

		panel = new WebcamPanel(webcam, false);

		add(panel, BorderLayout.CENTER);

		pack();
		setVisible(true);

		Thread t = new Thread()
		{

			@Override
			public void run()
			{
				panel.start();
			}

		};
		t.setName("example-starter");
		t.setDaemon(true);
		t.setUncaughtExceptionHandler(this);
		t.start();
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new WebcamViewerExample());
	}

	@Override
	public void webcamOpen(WebcamEvent we)
	{
		System.out.println("webcam open");
	}

	@Override
	public void webcamClosed(WebcamEvent we)
	{
		System.out.println("webcam closed");
	}

	@Override
	public void webcamDisposed(WebcamEvent we)
	{
		System.out.println("webcam disposed");
	}


	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
		webcam.close();
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
	}

	@Override
	public void windowOpened(WindowEvent e)
	{
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
		System.out.println("webcam viewer resumed");
		panel.resume();
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
		System.out.println("webcam viewer paused");
		panel.pause();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e)
	{
		System.err.println(String.format("Exception in thread %s", t.getName()));
		e.printStackTrace();
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		if (e.getItem() != webcam)
		{
			if (webcam != null)
			{

				panel.stop();

				remove(panel);

				webcam.removeWebcamListener(this);
				webcam.close();

				webcam = (Webcam) e.getItem();
				webcam.setViewSize(WebcamResolution.VGA.getSize());
				webcam.addWebcamListener(this);

				System.out.println("selected " + webcam.getName());

				panel = new WebcamPanel(webcam, false);

				add(panel, BorderLayout.CENTER);
				pack();

				Thread t = new Thread()
				{

					@Override
					public void run()
					{
						panel.start();
					}

				};
				t.setName("example-stoper");
				t.setDaemon(true);
				t.setUncaughtExceptionHandler(this);
				t.start();
			}
		}
	}

}
