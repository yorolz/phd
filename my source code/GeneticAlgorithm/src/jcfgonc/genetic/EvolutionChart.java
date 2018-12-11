package jcfgonc.genetic;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
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

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.painters.TracePainterPolyline;

public class EvolutionChart {

	private JFrame evolutionFrame;
	private Trace2DLtd dataCandidateCurrentBest;
	private MutableBoolean stopExecution;
	private Trace2DLtd dataCandidateOverallBest;
	private Trace2DLtd dataCandidateCurrentWorst;
	private Trace2DLtd dataCandidateCurrentMiddle;
	private CandidateInspector candidateInspector;

	public boolean allowExecution() {
		return !stopExecution.booleanValue();
	}

	public void stopExecution() {
		stopExecution.setTrue();
	}

	public EvolutionChart(final int windowSize) {
		evolutionFrame = new JFrame("GA population timeline");
		Font font = new Font(null, Font.PLAIN, 20);

		Container contentPane = evolutionFrame.getContentPane();
		contentPane.setLayout(new BorderLayout());

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();

		evolutionFrame.setSize(proportionOfInt(width, 0.333), proportionOfInt(height, 0.333));
		evolutionFrame.setResizable(true);
		evolutionFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		Chart2D chart = new Chart2D();
		chart.setUseAntialiasing(true);
		chart.setMinPaintLatency(100);
		chart.getAxisX().getAxisTitle().setTitle("epoch");
		chart.getAxisX().setPaintGrid(true);
		chart.getAxisX().setStartMajorTick(false);
		chart.getAxisX().setPaintScale(true);
		//chart.getAxisX().se

		chart.getAxisY().getAxisTitle().setTitle("fitness");
		chart.getAxisY().setPaintGrid(true);
		chart.getAxisY().setPaintScale(true);

		dataCandidateCurrentBest = new Trace2DLtd(windowSize);
		chart.addTrace(dataCandidateCurrentBest);
		dataCandidateCurrentBest.setStroke(new BasicStroke(3));
		dataCandidateCurrentBest.setColor(Color.GREEN);
		dataCandidateCurrentBest.setName("[current best]");
		dataCandidateCurrentBest.setTracePainter(new TracePainterPolyline());

		dataCandidateCurrentMiddle = new Trace2DLtd(windowSize);
		chart.addTrace(dataCandidateCurrentMiddle);
		dataCandidateCurrentMiddle.setStroke(new BasicStroke(3));
		dataCandidateCurrentMiddle.setColor(Color.BLUE);
		dataCandidateCurrentMiddle.setName("[current median]");
		dataCandidateCurrentMiddle.setTracePainter(new TracePainterPolyline());

		dataCandidateCurrentWorst = new Trace2DLtd(windowSize);
		chart.addTrace(dataCandidateCurrentWorst);
		dataCandidateCurrentWorst.setStroke(new BasicStroke(3));
		dataCandidateCurrentWorst.setColor(Color.GRAY);
		dataCandidateCurrentWorst.setName("[current 1st quartile]");
		dataCandidateCurrentWorst.setTracePainter(new TracePainterPolyline());

		dataCandidateOverallBest = new Trace2DLtd(windowSize);
		chart.addTrace(dataCandidateOverallBest);
		dataCandidateOverallBest.setStroke(new BasicStroke(3));
		dataCandidateOverallBest.setColor(Color.RED);
		dataCandidateOverallBest.setName("[overall best]");
		dataCandidateOverallBest.setTracePainter(new TracePainterPolyline());

		candidateInspector = new CandidateInspector(font);
		candidateInspector.setPreferredSize(new Dimension(proportionOfInt(evolutionFrame.getWidth(), 0.1), 100));

		chart.setFont(font);

		this.stopExecution = new MutableBoolean(false);
		String stopCommand = "Stop";

		JButton jb = new JButton("Stop Evolution");
		jb.setFont(new Font("Arial", Font.PLAIN, 36));

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
		contentPane.add(chart, BorderLayout.CENTER);
		// contentPane.add(candidateInspector);
		contentPane.add(candidateInspector, BorderLayout.EAST);
		contentPane.add(jb, BorderLayout.SOUTH);

		evolutionFrame.setLocationRelativeTo(null); // center window
	}

	public int proportionOfInt(int value, double proportion) {
		double newval = (double) value * proportion;
		return (int) newval;
	}

	/**
	 * Updates the graphs.
	 * 
	 * @param epoch
	 * @param fitnessBestOverall
	 * @param fitnessBest
	 * @param fitnessMiddle
	 * @param fitnessWorst
	 */
	public void addEpoch(double epoch, double fitnessBestOverall, double fitnessBest, double fitnessMiddle, double fitnessWorst) {
		dataCandidateOverallBest.addPoint(epoch, fitnessBestOverall);
		dataCandidateCurrentBest.addPoint(epoch, fitnessBest);
		dataCandidateCurrentMiddle.addPoint(epoch, fitnessMiddle);
		dataCandidateCurrentWorst.addPoint(epoch, fitnessWorst);
	}

	public void show() {
		evolutionFrame.setVisible(true);
	}

	public void dispose() {
		evolutionFrame.dispose();
	}

	public ArrayList<String> sortScoreKeys(Set<String> scoreMapKeySet) {
		ArrayList<String> scoreKeys = new ArrayList<String>(scoreMapKeySet);
		Collections.sort(scoreKeys);
		return scoreKeys;
	}

	public <T> void updateBestCandidate(T t) {
		// TODO: externalize class
		// if (t instanceof Blend) {
		// Blend blend = (Blend) t;
		// Set<String> keySetUnordered = blend.getScoreMapKeySet();
		// ArrayList<String> keySet = sortScoreKeys(keySetUnordered);
		// ArrayList<String> valueSet = new ArrayList<>();
		//
		// for (String scoreKey : keySet) {
		// String score = blend.getScoreForEntry(scoreKey);
		// valueSet.add(score);
		// }
		//
		// {
		// keySet.add("concepts");
		// int concepts = blend.getOutputSpace().getVertexSet().size();
		// valueSet.add(Integer.toString(concepts));
		// }
		// {
		// keySet.add("edges");
		// int edges = blend.getOutputSpace().edgeSet().size();
		// valueSet.add(Integer.toString(edges));
		// }
		// {
		// keySet.add("fitness");
		// double[] score = blend.getScore();
		// valueSet.add(Double.toString(score[0]));
		// }
		//
		// candidateInspector.setScoreMap(keySet, valueSet);
		// }
	}

	public void updateGeneticAlgorithmStats(int currentGeneration, double tournamentStrongestProb, double mutationProbability, double currentDiversity) {
		candidateInspector.setGeneticAlgorithmStats(currentGeneration, tournamentStrongestProb, mutationProbability, currentDiversity);
	}

	public void repaint() {
		evolutionFrame.repaint();
		// candidateInspector.repaint();
	}

}
