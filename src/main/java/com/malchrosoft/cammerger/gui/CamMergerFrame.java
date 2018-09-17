package com.malchrosoft.cammerger.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import javax.swing.JButton;
import javax.swing.JMenu;

public class CamMergerFrame extends JFrame
{

	private JPanel contentPane;
	private Canvas canvas;
	private BufferStrategy bs;
	private JButton btnAddAWebcam;
	private JPanel panel;
	private JMenu mnFichier;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					CamMergerFrame frame = new CamMergerFrame();
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CamMergerFrame()
	{
		setResizable(false);
		setTitle("Cam Merger");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1024, 768);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFichier = new JMenu("Fichier");
		menuBar.add(mnFichier);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(1, 1));

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		contentPane.add(toolBar, BorderLayout.NORTH);

		btnAddAWebcam = new JButton("Ajouter une webcam");
		toolBar.add(btnAddAWebcam);

		panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		this.canvas = new Canvas();
		this.canvas.setIgnoreRepaint(true);
		panel.add(canvas, BorderLayout.CENTER);
		canvas.setBackground(Color.DARK_GRAY);
		this.canvas.setBounds(0, 0, 200, 150);

		this.pack();

		this.init();
	}

	private void init()
	{
		this.canvas.createBufferStrategy(4);
		this.bs = this.canvas.getBufferStrategy();
	}

	public Graphics2D getDrawGraphics()
	{
		return (Graphics2D) this.bs.getDrawGraphics();
	}

	public final void redrawGraphics()
	{
		this.bs.show();
	}

	public final Point getMousePositionOnCanvas()
	{
		return canvas.getMousePosition();
	}

	public final void addMouseListenerOnCanvas(MouseMotionListener motionListener)
	{
		this.canvas.addMouseMotionListener(motionListener);
	}

	public final void addMouseListenerOnCanvas(MouseListener listener)
	{
		this.canvas.addMouseListener(listener);
	}

	public final void addMouseListenerOnCanvas(MouseWheelListener wheelListener)
	{
		this.canvas.addMouseWheelListener(wheelListener);
	}

}
