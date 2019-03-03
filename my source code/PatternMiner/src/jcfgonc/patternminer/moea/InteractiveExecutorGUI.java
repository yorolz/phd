package jcfgonc.patternminer.moea;

import java.awt.BorderLayout;
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
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import jcfgonc.patternminer.PatternMinerConfig;

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
	private ArrayList<XYSeries> ndsSeries;
	private JLabel ndsSizeTitle;
	private JLabel ndsSizeLabel;
	private boolean reverseGraphsVertically;
	private boolean reverseGraphsHorizontally;
	private JCheckBox checkBoxReverseH;
	private JPanel emptyPanel0;
	private JCheckBox checkBoxReverseV;
	private int numberNDSGraphs;
	private Problem problem;
	private JPanel panel_1;
	private JSpinner spinner;
	private JLabel lblNewLabel;

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
	public InteractiveExecutorGUI(InteractiveExecutor interactiveExecutor) {
		this.interactiveExecutor = interactiveExecutor;
		this.problem = interactiveExecutor.getProblem();
		this.algorithmProperties = interactiveExecutor.getAlgorithmProperties();
		this.numberOfVariables = problem.getNumberOfVariables();
		this.numberOfObjectives = problem.getNumberOfObjectives();
		this.numberOfConstraints = problem.getNumberOfConstraints();
		initialize();
	}

	@SuppressWarnings("deprecation")
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
		ndsPanel.setMinimumSize(new Dimension(256, 10));
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
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
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

		panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		settingsPanel.add(panel_1);
		panel_1.setLayout(new GridLayout(0, 1, 0, 0));

		checkBoxReverseH = new JCheckBox("Flip Results Horizontally");
		checkBoxReverseH.setHorizontalAlignment(SwingConstants.CENTER);
		checkBoxReverseH.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_1.add(checkBoxReverseH);

		checkBoxReverseV = new JCheckBox("Flip Results Vertically");
		checkBoxReverseV.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(checkBoxReverseV);

		emptyPanel0 = new JPanel();
		panel_1.add(emptyPanel0);
		emptyPanel0.setBorder(null);
		FlowLayout fl_emptyPanel0 = new FlowLayout(FlowLayout.CENTER, 5, 5);
		emptyPanel0.setLayout(fl_emptyPanel0);

		lblNewLabel = new JLabel("querykb timeout (s)");
		emptyPanel0.add(lblNewLabel);

		spinner = new JSpinner();
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner mySpinner = (JSpinner) (e.getSource());
				SpinnerNumberModel snm = (SpinnerNumberModel) mySpinner.getModel();
				PatternMinerConfig.QUERY_TIMEOUT_MS = (int) (snm.getNumber().doubleValue() * 1000.0);
				System.out.println(PatternMinerConfig.QUERY_TIMEOUT_MS);
			}
		});
		spinner.setPreferredSize(new Dimension(64, 20));
		spinner.setModel(new SpinnerNumberModel(new Double(60), new Double(0), null, new Double(1)));
		emptyPanel0.add(spinner);
		checkBoxReverseV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reverseGraphsVertically = !reverseGraphsVertically;
			}
		});
		checkBoxReverseH.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reverseGraphsHorizontally = !reverseGraphsHorizontally;
			}
		});

		buttonsPanel = new JPanel();
		buttonsPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
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
				// System.out.println((double) (horizontalPane.getDividerLocation()) / (horizontalPane.getWidth() - horizontalPane.getDividerSize()));
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
		// ndsGraphs = new ArrayList<>();
		ndsSeries = new ArrayList<>();
		int objectiveIndex = 0; // for laying out axis' labels
		for (int i = 0; i < numberNDSGraphs; i++) {
			XYSeries xySeries = new XYSeries("untitled");
			XYSeriesCollection dataset = new XYSeriesCollection();
			dataset.addSeries(xySeries);
			ndsSeries.add(xySeries);

			String xAxisLabel;
			String yAxisLabel;
			if (problem instanceof ProblemDescription) {
				ProblemDescription pd = (ProblemDescription) problem;
				if (objectiveIndex < numberOfObjectives - 1) { // only one objective
					xAxisLabel = pd.getObjectiveDescription(objectiveIndex);
					yAxisLabel = pd.getObjectiveDescription(objectiveIndex + 1);
				} else { // more than two objectives to follow
					xAxisLabel = pd.getObjectiveDescription(0);
					yAxisLabel = pd.getObjectiveDescription(objectiveIndex);
				}
			} else {
				if (objectiveIndex < numberOfObjectives - 1) { // only one objective
					xAxisLabel = String.format("Objective %d", objectiveIndex);
					yAxisLabel = String.format("Objective %d", objectiveIndex + 1);
				} else { // more than two objectives to follow
					xAxisLabel = String.format("Objective %d", 0);
					yAxisLabel = String.format("Objective %d", objectiveIndex);
				}
			}
			objectiveIndex += 2;

			String title = null;// String.format("Non-Dominated Set %d", i);
			JFreeChart xylineChart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, false, false, false);
			ChartPanel chartPanel = new ChartPanel(xylineChart, false);
			ndsPanel.add(chartPanel);

			// ndsGraphs.add(plot);
		}

		resetDividerLocation();
	}

	public void updateStatus(NondominatedPopulation population, int generation, int evaluations) {
		epochLabel.setText(Integer.toString(generation));
		evaluationsLabel.setText(Integer.toString(evaluations));

		if (population == null)
			return;

		// for scaling the axis
//		DescriptiveStatistics dsx = new DescriptiveStatistics();
//		DescriptiveStatistics dsy = new DescriptiveStatistics();

		// update the non-dominated sets
		ndsSizeLabel.setText(Integer.toString(population.size()));

		int objectiveIndex = 0;
		// iterate the scatter plots (each can hold two objectives)
		for (XYSeries graph : ndsSeries) {
			// empty data series
			graph.clear();
			// iterate the solutions
			for (Solution solution : population) {
				// pairs of objectives
				double x;
				double y;
				if (objectiveIndex < numberOfObjectives - 1) {
					x = solution.getObjective(objectiveIndex);
					y = solution.getObjective(objectiveIndex + 1);
				} else {
					x = solution.getObjective(0);
					y = solution.getObjective(objectiveIndex);
				}
				if (reverseGraphsHorizontally) {
					x = -x;
				}
				if (reverseGraphsVertically) {
					y = -y;
				}
				graph.add(x, y);
//				dsx.addValue(x);
//				dsy.addValue(y);
			}
//			double maxx = dsx.getMax() * 1.2;
//			double minx = dsx.getMin() - (maxx - dsx.getMax());
//			double maxy = dsy.getMax() * 1.2;
//			double miny = dsy.getMin() - (maxy - dsy.getMax());

//			graph.getAxis(XYPlot.AXIS_X).setRange(minx, maxx);
//			graph.getAxis(XYPlot.AXIS_Y).setRange(miny, maxy);

			objectiveIndex += 2;
		}
//		ndsPanel.repaint();
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
