package genetic;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

public class CandidateInspector extends JPanel {
	private static final long serialVersionUID = -954306083728414142L;
	List<String> valueSet;
	List<String> keySet;
	int currentGeneration;
	double tournamentStrongestProb;
	double mutationProbability;
	double currentDiversity;

	public CandidateInspector(Font font) {
		this.setFont(font);
		this.setBackground(Color.WHITE);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (valueSet != null && keySet != null) {
			Graphics2D graphics = (Graphics2D) g;
			// graphics.setFont(LEFONT);
			int ypos = 80;
			int xpos = 200;
			int fontSize = this.getFont().getSize();

			{
				graphics.setColor(Color.RED);
				graphics.drawString("STATS", xpos, ypos);
				ypos += fontSize;

				graphics.setColor(Color.BLACK);
				graphics.drawString("Generation: " + currentGeneration, xpos, ypos);
				ypos += fontSize;
				graphics.drawString("Diversity: " + currentDiversity, xpos, ypos);
				ypos += fontSize;
				graphics.drawString("TournamentStrongestProb: " + tournamentStrongestProb, xpos, ypos);
				ypos += fontSize;
				graphics.drawString("Mutation Probability: " + mutationProbability, xpos, ypos);
				ypos += fontSize;
			}

			ypos += 1 * fontSize;

			{
				graphics.setColor(Color.RED);
				graphics.drawString("OVERALL BEST", xpos, ypos);
				ypos += fontSize;

				graphics.setColor(Color.BLACK);
				Iterator<String> values = valueSet.iterator();
				for (String scoreKey : keySet) {
					String value = values.next();
					graphics.drawString(scoreKey + ": " + value, xpos, ypos);
					ypos += fontSize;
				}
			}
		}
	}

	public void setScoreMap(List<String> keySet, List<String> valueSet) {
		this.keySet = keySet;
		this.valueSet = valueSet;

		this.repaint();
	}

	public void setGeneticAlgorithmStats(int currentGeneration, double tournamentStrongestProb, double mutationProbability, double currentDiversity) {
		this.currentGeneration = currentGeneration;
		this.tournamentStrongestProb = tournamentStrongestProb;
		this.mutationProbability = mutationProbability;
		this.currentDiversity = currentDiversity;
	}
}
