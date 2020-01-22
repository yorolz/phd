package genetic;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
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

import blender.Blend;

public class EvolutionChart {

	private JFrame evolutionFrame;
	private MutableBoolean stopExecution;
	private CandidateInspector candidateInspector;

	public boolean allowExecution() {
		return !stopExecution.booleanValue();
	}

	public void stopExecution() {
		stopExecution.setTrue();
	}

	public EvolutionChart(final int windowSize) {
		evolutionFrame = new JFrame("GA population timeline");
		Font font = new Font(null, Font.PLAIN, 40);

		Container contentPane = evolutionFrame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		// contentPane.setLayout(new GridLayout(2, 1));

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();

		evolutionFrame.setSize(proportionOfInt(width, 0.75), proportionOfInt(height, 0.75));
		evolutionFrame.setResizable(true);
		evolutionFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		candidateInspector = new CandidateInspector(font);
		candidateInspector.setPreferredSize(new Dimension(proportionOfInt(evolutionFrame.getWidth(), 0.4), 100));

		this.stopExecution = new MutableBoolean(false);
		String stopCommand = "Stop";

		JButton jb = new JButton("Stop Evolution");
		jb.setFont(new Font("Arial", Font.PLAIN, 50));

		jb.setActionCommand(stopCommand);
		jb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String actionCommand = e.getActionCommand();
				if (actionCommand.equals(stopCommand)) {
					stopExecution.setTrue();
				}
			}
		});

		// contentPane.add(chart);
		// contentPane.add(candidateInspector);
		contentPane.add(candidateInspector, BorderLayout.EAST);
		contentPane.add(jb, BorderLayout.SOUTH);

		evolutionFrame.setLocationRelativeTo(null); // center window
	}

	public int proportionOfInt(int value, double proportion) {
		double newval = (double) value * proportion;
		return (int) newval;
	}

	public void addEpoch(double epoch, double fitnessBestOverall, double fitnessBest, double fitnessMiddle, double fitnessWorst) {
	}

	public void show() {
		evolutionFrame.setVisible(true);
	}

	public void dispose() {
		evolutionFrame.dispose();
	}

	private ArrayList<String> sortScoreKeys(Set<String> scoreMapKeySet) {
		ArrayList<String> scoreKeys = new ArrayList<String>(scoreMapKeySet);
		Collections.sort(scoreKeys);
		return scoreKeys;
	}

	public <T> void updateBestCandidate(T t) {
		if (t instanceof Blend) {
			Blend blend = (Blend) t;
			Set<String> keySetUnordered = blend.getScoreMapKeySet();
			ArrayList<String> keySet = sortScoreKeys(keySetUnordered);
			ArrayList<String> valueSet = new ArrayList<>();

			for (String scoreKey : keySet) {
				String score = blend.getScoreForEntry(scoreKey);
				valueSet.add(score);
			}

			{
				keySet.add("concepts");
				int concepts = blend.getOutputSpace().getVertexSet().size();
				valueSet.add(Integer.toString(concepts));
			}
			{
				keySet.add("edges");
				int edges = blend.getOutputSpace().edgeSet().size();
				valueSet.add(Integer.toString(edges));
			}
			{
				keySet.add("fitness");
				double[] score = blend.getScore();
				valueSet.add(Double.toString(score[0]));
			}

			candidateInspector.setScoreMap(keySet, valueSet);
		}
	}

	public void updateGeneticAlgorithmStats(int currentGeneration, double tournamentStrongestProb, double mutationProbability, double currentDiversity) {
		candidateInspector.setGeneticAlgorithmStats(currentGeneration, tournamentStrongestProb, mutationProbability, currentDiversity);
	}

	public void repaint() {
		evolutionFrame.repaint();
		// candidateInspector.repaint();
	}

}
