package com.malchrosoft.cammerger.gui;

public abstract class Refreshable implements Runnable
{
	private int index;

	public Refreshable()
	{
	}

	@Override
	public final void run()
	{
		refresh(this.index);
	}

	public final int getIndex()
	{
		return index;
	}

	public final void setIndex(int index)
	{
		this.index = index;
	}

	public abstract void refresh(int index);

}
