package jcfgonc.patternminer.moea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.Insets2D;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.ui.InteractivePanel;

public class InteractiveExecutorGUI extends JFrame {

	private static final long serialVersionUID = 5577378439253898247L;
	private JPanel contentPane;
	private JSplitPane horizontalPane;
	private JPanel ndsPanel;
	private JPanel settingsPanel;
	private JButton stopButton;
	private JPanel buttonsPanel;
	private JButton abortButton;
	private InteractiveExecutor interactiveExecutor;
	private JPanel panel;
	private JLabel epochTitle;
	private JLabel evaluationsTitle;
	private int numberOfVariables;
	private int numberOfObjectives;
	private int numberOfConstraints;
	private JLabel variablesTitle;
	private JLabel objectivesTitle;
	private JLabel constraintsTitle;
	private JLabel epochLabel;
	private JLabel evaluationsLabel;
	private JLabel variablesLabel;
	private JLabel objectivesLabel;
	private JLabel constraintsLabel;
	private JLabel populationSizeTitle;
	private JLabel populationSizeLabel;
	private Properties algorithmProperties;
	private ArrayList<XYPlot> ndsGraphs;
	private JLabel ndsSizeTitle;
	private JLabel ndsSizeLabel;
	private boolean reverseGraphsVertically;
	private boolean reverseGraphsHorizontally;
	private JCheckBox checkBoxReverseH;
	private JPanel emptyPanel0;
	private JCheckBox checkBoxReverseV;
	private JPanel emptyPanel1;
	private int numberNDSGraphs;

	/**
	 * Create the frame.
	 * 
	 * @param properties
	 * @param interactiveExecutor
	 * 
	 * @param k
	 * @param j
	 * @param i
	 */
	public InteractiveExecutorGUI(InteractiveExecutor interactiveExecutor, Properties algorithmProperties, int numberOfVariables, int numberOfObjectives, int numberOfConstraints) {
		this.interactiveExecutor = interactiveExecutor;
		this.algorithmProperties = algorithmProperties;
		this.numberOfVariables = numberOfVariables;
		this.numberOfObjectives = numberOfObjectives;
		this.numberOfConstraints = numberOfConstraints;
		initialize();
	}

	private void initialize() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(0, 0, 640, 480);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		horizontalPane = new JSplitPane();
		horizontalPane.setMinimumSize(new Dimension(608, 432));
		horizontalPane.setPreferredSize(new Dimension(608, 432));
		horizontalPane.setBorder(null);
		contentPane.add(horizontalPane);

		ndsPanel = new JPanel();
		ndsPanel.setBorder(null);
		ndsPanel.setPreferredSize(new Dimension(288, 416));
		horizontalPane.setLeftComponent(ndsPanel);
		ndsPanel.setLayout(new GridLayout(0, 1, 0, 0));

		settingsPanel = new JPanel();
		settingsPanel.setBorder(null);
		settingsPanel.setPreferredSize(new Dimension(288, 416));
		horizontalPane.setRightComponent(settingsPanel);
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

		panel = new JPanel();
		settingsPanel.add(panel);
		panel.setLayout(new GridLayout(0, 2, 0, 0));

		variablesTitle = new JLabel("Variables: ");
		variablesTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(variablesTitle);

		variablesLabel = new JLabel("");
		variablesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(variablesLabel);

		objectivesTitle = new JLabel("Objectives: ");
		objectivesTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(objectivesTitle);

		objectivesLabel = new JLabel("");
		objectivesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(objectivesLabel);

		constraintsTitle = new JLabel("Constraints: ");
		constraintsTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(constraintsTitle);

		constraintsLabel = new JLabel("");
		constraintsLabel.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(constraintsLabel);

		populationSizeTitle = new JLabel("Population Size: ");
		populationSizeTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(populationSizeTitle);

		populationSizeLabel = new JLabel("");
		populationSizeLabel.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(populationSizeLabel);

		epochTitle = new JLabel("Epoch: ");
		epochTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(epochTitle);

		epochLabel = new JLabel("");
		epochLabel.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(epochLabel);

		evaluationsTitle = new JLabel("Evaluations: ");
		evaluationsTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(evaluationsTitle);

		evaluationsLabel = new JLabel("");
		evaluationsLabel.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(evaluationsLabel);

		ndsSizeTitle = new JLabel("Non-Dominated Set Size: ");
		ndsSizeTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(ndsSizeTitle);

		ndsSizeLabel = new JLabel("");
		ndsSizeLabel.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(ndsSizeLabel);

		emptyPanel0 = new JPanel();
		emptyPanel0.setBorder(null);
		panel.add(emptyPanel0);

		checkBoxReverseH = new JCheckBox("Flip Results Horizontally");
		checkBoxReverseH.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reverseGraphsHorizontally = !reverseGraphsHorizontally;
			}
		});
		panel.add(checkBoxReverseH);

		emptyPanel1 = new JPanel();
		emptyPanel1.setBorder(null);
		panel.add(emptyPanel1);

		checkBoxReverseV = new JCheckBox("Flip Results Vertically");
		checkBoxReverseV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reverseGraphsVertically = !reverseGraphsVertically;
			}
		});
		panel.add(checkBoxReverseV);

		buttonsPanel = new JPanel();
		settingsPanel.add(buttonsPanel);

		stopButton = new JButton("Stop Optimization");
		stopButton.setToolTipText("Waits for the current epoch to complete and returns the best results so far.");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopOptimization();
			}
		});
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		buttonsPanel.add(stopButton);
		stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		stopButton.setBorder(UIManager.getBorder("Button.border"));

		abortButton = new JButton("Abort Optimization");
		abortButton.setToolTipText("Aborts the optimization by discarding the current epoch's results and returns the best results so far.");
		abortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abortOptimization();
			}
		});
		abortButton.setBorder(UIManager.getBorder("Button.border"));
		abortButton.setAlignmentX(0.5f);
		buttonsPanel.add(abortButton);

		addComponentListener(new ComponentAdapter() { // window resize event
			@Override
			public void componentResized(ComponentEvent e) {
				windowResized(e);
			}
		});
	}

	private void abortOptimization() {
		// default icon, custom title
		int n = JOptionPane.showConfirmDialog(null, "Aborting optimization will discard the results of the current epoch.\nAre you sure?", "Abort Optimization",
				JOptionPane.YES_NO_OPTION);
		if (n != 0)
			return;
		interactiveExecutor.abortOptimization();
	}

	private void stopOptimization() {
		interactiveExecutor.stopOptimization();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	private int proportionOfInt(int value, double proportion) {
		double newval = (double) value * proportion;
		return (int) newval;
	}

	/**
	 * contains the rest of the stuff which cannot be initialized in the initialize function (because of the windowbuilder IDE)
	 */
	public void initializeTheRest() {
		variablesLabel.setText(Integer.toString(numberOfVariables));
		objectivesLabel.setText(Integer.toString(numberOfObjectives));
		constraintsLabel.setText(Integer.toString(numberOfConstraints));
		populationSizeLabel.setText(algorithmProperties.getProperty("populationSize"));

		reverseGraphsHorizontally = true;
		reverseGraphsVertically = true;
		updateGraphControls(); // sync controls with boolean vars

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		this.setLocationRelativeTo(null); // center jframe

		this.setSize(proportionOfInt(width, 0.333), proportionOfInt(height, 0.333));
		this.pack();

		numberNDSGraphs = (int) Math.ceil((double) numberOfObjectives / 2); // they will be plotted in pairs of objectives
		ndsGraphs = new ArrayList<>();
		for (int i = 0; i < numberNDSGraphs; i++) {
			Color markerColor = Color.RED;
			XYPlot xyp = addScatterPlot(markerColor, "Non-Dominated Set " + i);
			ndsGraphs.add(xyp);
		}

		resetDividerLocation();
	}

	public void updateStatus(NondominatedPopulation population, int generation, int maxGenerations, int evaluations) {
		epochLabel.setText(Integer.toString(generation));
		evaluationsLabel.setText(Integer.toString(evaluations));

		if (population == null)
			return;

		// update the non-dominated sets
		ndsSizeLabel.setText(Integer.toString(population.size()));

		int objectiveIndex = 0;
		// iterate the scatter plots (each can hold two objectives)
		for (XYPlot graph : ndsGraphs) {
			// create a new data series
			DataTable data = (DataTable) graph.getData().get(0);
			data.clear();
			// DataTable data = new DataTable(Double.class, Double.class);
			// iterate the solutions
			for (Solution solution : population) {
				// pairs of objectives
				double o0;
				double o1;
				if (objectiveIndex < numberOfObjectives - 1) {
					o0 = solution.getObjective(objectiveIndex);
					o1 = solution.getObjective(objectiveIndex + 1);
				} else {
					o0 = solution.getObjective(0);
					o1 = solution.getObjective(objectiveIndex);
				}
				if (reverseGraphsHorizontally) {
					o0 = -o0;
				}
				if (reverseGraphsVertically) {
					o1 = -o1;
				}
				data.add(o0, o1);
			}
			objectiveIndex += 2;
		}
		ndsPanel.repaint();
	}

	private XYPlot addScatterPlot(Color markerColor, String title) {
		@SuppressWarnings("unchecked")
		DataTable data = new DataTable(Double.class, Double.class);
		XYPlot plot = new XYPlot(data);

		plot.setInsets(new Insets2D.Double(20.0, 40.0, 40.0, 40.0));
		plot.getTitle().setText(title);
		plot.getPointRenderers(data).get(0).setColor(markerColor);
		InteractivePanel interactivePanel = new InteractivePanel(plot);
		ndsPanel.add(interactivePanel);
		return plot;
	}

	protected void windowResized(ComponentEvent e) {
		// updateDividerLocation();
	}

	private void resetDividerLocation() {
		SplitPaneUI spui = horizontalPane.getUI();
		if (spui instanceof BasicSplitPaneUI) {
			((BasicSplitPaneUI) spui).getDivider().addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent arg0) {
					if (arg0.getClickCount() == 2) {
						horizontalPane.setDividerLocation(0.5);
					}
				}
			});
		}
	}

	private void updateGraphControls() {
		checkBoxReverseH.setSelected(reverseGraphsHorizontally);
		checkBoxReverseV.setSelected(reverseGraphsVertically);
	}

}
