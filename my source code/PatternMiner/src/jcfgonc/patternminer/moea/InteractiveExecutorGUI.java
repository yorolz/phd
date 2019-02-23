package jcfgonc.patternminer.moea;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import javax.swing.JTextArea;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.Insets;
import javax.swing.JProgressBar;

public class InteractiveExecutorGUI extends JFrame {

	private JPanel contentPane;
	private JPanel paretoPanel;
	private JPanel settingsPanel;
	private JTextArea textArea;
	private JButton btnStopEvolution;
	private JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InteractiveExecutorGUI frame = new InteractiveExecutorGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public InteractiveExecutorGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 2, 0, 0));

		paretoPanel = new JPanel();
		contentPane.add(paretoPanel);
		GridBagLayout gbl_paretoPanel = new GridBagLayout();
		gbl_paretoPanel.columnWidths = new int[] { 0 };
		gbl_paretoPanel.rowHeights = new int[] { 0 };
		gbl_paretoPanel.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_paretoPanel.rowWeights = new double[] { Double.MIN_VALUE };
		paretoPanel.setLayout(gbl_paretoPanel);

		settingsPanel = new JPanel();
		contentPane.add(settingsPanel);
		GridBagLayout gbl_settingsPanel = new GridBagLayout();
		gbl_settingsPanel.columnWidths = new int[] { 0, 0 };
		gbl_settingsPanel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_settingsPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_settingsPanel.rowWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		settingsPanel.setLayout(gbl_settingsPanel);

		textArea = new JTextArea();
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 0;
		settingsPanel.add(textArea, gbc_textArea);

		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 1;
		settingsPanel.add(progressBar, gbc_progressBar);

		btnStopEvolution = new JButton("Stop Evolution");
		GridBagConstraints gbc_btnStopEvolution = new GridBagConstraints();
		gbc_btnStopEvolution.gridx = 0;
		gbc_btnStopEvolution.gridy = 2;
		settingsPanel.add(btnStopEvolution, gbc_btnStopEvolution);
	}

}
