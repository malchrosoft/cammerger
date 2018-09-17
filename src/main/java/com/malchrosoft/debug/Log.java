/*
 * Copyright MalchroSoft - Aymeric MALCHROWICZ. All right reserved.
 * The source code that contains this comment is an intellectual property
 * of MalchroSoft [Aymeric MALCHROWICZ]. Use is subject to licence terms.
 */
package com.malchrosoft.debug;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DEBUG UTILITIES CLASS
 * @author Aymeric Malchrowicz
 */
public class Log
{
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	public static enum Level
	{
		DEBUG(3), INFO(2), WARN(1), ERROR(0), NO_LOG(-1);
		private int value;

		private Level(int value)
		{
			this.value = value;
		}

		protected int getValue()
		{
			return value;
		}

	}
	
	private SimpleDateFormat df;

	private Level level;
	/**
	 * Object who modifies the status
	 */
	private String modifierLocalization;

	private Log()
	{
		this.df = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
		this.level = Level.INFO;
	}

	public final void setLevel(Level level)
	{
		if (level != null)
		{
			this.level = level;
		}
	}

	private static Log instance;

	public static Log get()
	{
		if (Log.instance == null)
		{
			Log.instance = new Log();
		}
		return Log.instance;
	}

	/**
	 * Retruns the class localization who has modified the status.
	 * @return the class localization who has modified the status
	 */
	public static String getModifierLocalization()
	{
		return Log.get().modifierLocalization;
	}

	/**
	 * Prints a text with a trace line from the calling class
	 * @param text the text
	 */
	public synchronized static void debug(String text)
	{
		if (get().level.getValue() < Level.DEBUG.getValue()) return;
		Log.printWithTraceLine(get().now() + "DEBUG : " + text, 1, System.out);
	}

	/**
	 * Print a text with the calling class line
	 * @param text the text to info
	 */
	public synchronized static void info(String text)
	{
		if (get().level.getValue() < Level.INFO.getValue()) return;
		Log.printWithTraceLine(ANSI_BLUE + get().now() + "INFO : " + text + ANSI_RESET, 1, System.out);
	}
	
	public synchronized static void error(String text, Throwable ex)
	{
		error(text);
		ex.printStackTrace();
	}

	/**
	 * Print a error with the calling class line
	 * @param text the error text
	 */
	public synchronized static void error(String text)
	{
		if (get().level.getValue() < Level.ERROR.getValue()) return;
		Log.printWithTraceLine(ANSI_RED + get().now() + "ERROR : " + text + ANSI_RESET, 1, System.out);
	}

	public synchronized static void warn(String text, Throwable ex)
	{
		warn(text);
		ex.printStackTrace();
	}
	
	public synchronized static void warn(String text)
	{
		if (get().level.getValue() < Level.ERROR.getValue()) return;
		Log.printWithTraceLine(ANSI_YELLOW + get().now() + "WARN : " + text + ANSI_RESET, 1, System.out);
	}

	private synchronized static void printWithTraceLine(String text, int l,
		PrintStream s)
	{
		StackTraceElement[] trace = new Throwable().getStackTrace();
		s.println(text);
		s.println("\tat " + trace[l + 1]);
	}
	
	private String now()
	{
		return df.format(new Date()) + "s ";
	}

	public static boolean isDebugMode()
	{
		return get().level.value >= Level.DEBUG.value;
	}

	/**
	 * TESTS
	 * @param args
	 * @throws java.lang.ClassNotFoundException
	 * @throws java.lang.InterruptedException
	 */
	public static void main(String[] args) throws ClassNotFoundException, InterruptedException
	{
		Log.get().setLevel(Level.DEBUG);
		Thread.sleep(1000);
		Log.debug("message");
		Thread.sleep(1000);
		Log.error(Log.getModifierLocalization());
		Thread.sleep(1000);
		Log.info(Log.getModifierLocalization());
		Thread.sleep(1000);
		Log.warn("avertissement...");
		Thread.sleep(1000);
		System.out.println("Test colors  " +
			"\n" + ANSI_BLUE + "blue" +
			"\n" + ANSI_CYAN + "cyan" +
			"\n" + ANSI_GREEN + "green" +
			"\n" + ANSI_PURPLE + "purple" +
			"\n" + ANSI_RED + "red" +
			"\n" + ANSI_WHITE + "white" +
			"\n" + ANSI_YELLOW + "yellow" +
			"\n" + ANSI_RESET + "reset");
	}

}
