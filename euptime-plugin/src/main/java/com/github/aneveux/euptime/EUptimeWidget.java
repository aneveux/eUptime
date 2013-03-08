/*
 * eUptime - an Eclipse widget allowing to indicate its uptime using some fancy colors.
 * Definitively not the most useful thing on earth, but at least I wanted it.
 *
 * ---
 * LICENSE:
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *                     Version 2, December 2004
 *
 *  Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>
 *
 *  Everyone is permitted to copy and distribute verbatim or modified
 *  copies of this license document, and changing it is allowed as long
 *  as the name is changed.
 *
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *   0. You just DO WHAT THE FUCK YOU WANT TO.
 *
 * -- http://www.wtfpl.net/txt/copying/
 */
package com.github.aneveux.euptime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * This widget allows to display Eclipse's uptime using a pretty format and a
 * color adapted to it. It works as a Thread itself so as to update itself
 * easily.
 * 
 * @author Antoine Neveux
 * @version 1.0
 * 
 */
public class EUptimeWidget extends WorkbenchWindowControlContribution implements
		Runnable {

	/**
	 * Display object
	 */
	protected Display display;

	/**
	 * Allows to know if the widget is still in use or not
	 */
	protected boolean isActive = false;

	/**
	 * The font object to use in order to customize the way the label will
	 * appear
	 */
	protected Font font;

	/**
	 * The composite containing the label
	 */
	protected Composite background;

	/**
	 * The label where we'll display Eclipse's uptime
	 */
	protected Label uptimeLabel;

	/**
	 * Default constructor
	 */
	public EUptimeWidget() {
		display = null;
		isActive = true;
	}

	/**
	 * Allows to dispose the font when the Thread is disposed
	 */
	@Override
	public void dispose() {
		isActive = false;
		font.dispose();
	}

	/**
	 * This method allows to update the color and content of the label following
	 * Eclipse's uptime
	 */
	@Override
	public void run() {
		while (isActive)
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					uptimeLabel.setText(new StringBuilder("Uptime: ").append(
							getPrettyDuration()).toString());
					uptimeLabel.setForeground(getUptimeColor());
					uptimeLabel.update();
				}
			});
	}

	/**
	 * This method allows to create the control (components) and start the
	 * Thread linked to the whole widget
	 */
	@Override
	protected Control createControl(final Composite parent) {
		display = parent.getDisplay();
		final FontData fontData = parent.getFont().getFontData()[0];
		fontData.setHeight(14);
		fontData.setStyle(1);
		font = new Font(display, fontData);
		background = new Composite(parent, 0);
		background.setLayout(new FormLayout());
		uptimeLabel = new Label(background, 16777216);
		uptimeLabel.setFont(font);
		uptimeLabel.setText("Uptime: ");
		new FormDataBuilder(0).left().top().bottom().right().width(200)
				.apply(uptimeLabel);
		final Thread thread = new Thread(this, new StringBuilder(
				"Uptime Widget ").append(System.currentTimeMillis()).toString());
		thread.setDaemon(true);
		thread.start();
		return background;
	}

	/**
	 * This method allows to get the Eclipse's uptime in a pretty format using
	 * Joda
	 * 
	 * @return something like 1d4h3m2s describing the Eclipse's uptime
	 */
	protected String getPrettyDuration() {
		final Duration duration = new Duration(System.currentTimeMillis()
				- Activator.getDefault().getStartTime());
		final PeriodFormatter formatter = new PeriodFormatterBuilder()
				.appendDays().appendSuffix("d").appendHours().appendSuffix("h")
				.appendMinutes().appendSuffix("m").appendSeconds()
				.appendSuffix("s").toFormatter();
		return formatter.print(duration.toPeriod());
	}

	/**
	 * This method allows to define a color in order to alert the user about the
	 * Eclipse's uptime
	 * 
	 * @return Green if uptime<2h, blue if <4h and red if uptime>4h
	 */
	protected Color getUptimeColor() {
		final long uptime = System.currentTimeMillis()
				- Activator.getDefault().getStartTime();
		if (uptime < 7200000)
			return display.getSystemColor(SWT.COLOR_DARK_GREEN);
		else if (uptime < 14400000)
			return display.getSystemColor(SWT.COLOR_BLUE);
		else
			return display.getSystemColor(SWT.COLOR_RED);
	}

}
