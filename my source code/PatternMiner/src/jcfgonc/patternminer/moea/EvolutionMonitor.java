package jcfgonc.patternminer.moea;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.apache.commons.lang3.mutable.MutableBoolean;

public class EvolutionMonitor {

	private JFrame evolutionFrame;
	private MutableBoolean stopExecution;
	private boolean terminated;

	public boolean isCanceled() {
		return stopExecution.booleanValue();
	}

	public void cancelExecution() {
		stopExecution.setTrue();
	}

	public EvolutionMonitor() {
		evolutionFrame = new JFrame("GA population timeline");
	//	Font font = new Font(null, Font.PLAIN, 20);

		Container contentPane = evolutionFrame.getContentPane();
		contentPane.setLayout(new BorderLayout());

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();

		evolutionFrame.setSize(proportionOfInt(width, 0.333), proportionOfInt(height, 0.333));
		evolutionFrame.setResizable(true);
		evolutionFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		this.stopExecution = new MutableBoolean(false);
		String stopCommand = "Stop";

		JButton stopButton = new JButton("Stop Evolution");
		stopButton.setFont(new Font("Arial", Font.PLAIN, 36));

		stopButton.setActionCommand(stopCommand);
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String actionCommand = e.getActionCommand();
				if (actionCommand.equals(stopCommand)) {
					stopExecution.setTrue();
				}
			}
		});

		// contentPane.add(chart, BorderLayout.CENTER);
		// contentPane.add(candidateInspector, BorderLayout.EAST);
		contentPane.add(stopButton, BorderLayout.SOUTH);

		evolutionFrame.setLocationRelativeTo(null); // center window
	}

	private int proportionOfInt(int value, double proportion) {
		double newval = (double) value * proportion;
		return (int) newval;
	}

	public void show() {
		if (!terminated) {
			evolutionFrame.setVisible(true);
		}
	}

	public void dispose() {
		this.terminated = true;
		evolutionFrame.dispose();
	}

	public ArrayList<String> sortScoreKeys(Set<String> scoreMapKeySet) {
		ArrayList<String> scoreKeys = new ArrayList<String>(scoreMapKeySet);
		Collections.sort(scoreKeys);
		return scoreKeys;
	}

	public void repaint() {
		if (!terminated) {
			evolutionFrame.repaint();
			// candidateInspector.repaint();
		}
	}
}
