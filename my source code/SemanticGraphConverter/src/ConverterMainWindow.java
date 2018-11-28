import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class ConverterMainWindow {

	private JFrame frmSemanticGraphConverter;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private Holder<String> outputFormat = new Holder<>();

	/**
	 * Launch the application.
	 * 
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConverterMainWindow window = new ConverterMainWindow();
					window.frmSemanticGraphConverter.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ConverterMainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmSemanticGraphConverter = new JFrame();
		frmSemanticGraphConverter.setTitle("Semantic Graph Converter");
		frmSemanticGraphConverter.setSize(new Dimension(322, 205));
		frmSemanticGraphConverter.setResizable(false);
		frmSemanticGraphConverter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSemanticGraphConverter.setLocationRelativeTo(null);
		frmSemanticGraphConverter.getContentPane().setLayout(null);

		JPanel radioPanel = new JPanel();
		radioPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Output format", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		radioPanel.setBounds(10, 108, 290, 50);
		frmSemanticGraphConverter.getContentPane().add(radioPanel);
		radioPanel.setLayout(new GridLayout(1, 4, 0, 0));

		JRadioButton csvRadioButton = new JRadioButton("CSV");
		csvRadioButton.setToolTipText("comma separated value, i.e., each line is in the format \"source,relation,target\"");
		csvRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outputFormat.value = "csv";
			}
		});
		csvRadioButton.setSelected(true);
		buttonGroup.add(csvRadioButton);
		radioPanel.add(csvRadioButton);

		JRadioButton dtRadioButton = new JRadioButton("DT");
		dtRadioButton.setToolTipText("Divago format, i.e., each line is in the format \"r(domain,source,relation,target).\" Domain is set to the filename.");
		dtRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outputFormat.value = "dt";
			}
		});
		buttonGroup.add(dtRadioButton);
		radioPanel.add(dtRadioButton);

		JRadioButton proRadioButton = new JRadioButton("PRO");
		proRadioButton.setToolTipText("Prolog format, i.e., each line is in the format \"relation(source,target).\"");
		proRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outputFormat.value = "pro";
			}
		});
		buttonGroup.add(proRadioButton);
		radioPanel.add(proRadioButton);

		JRadioButton tgfRadioButton = new JRadioButton("TGF");
		tgfRadioButton.setToolTipText("Trivial Graph Format");
		tgfRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outputFormat.value = "tgf";
			}
		});
		buttonGroup.add(tgfRadioButton);
		radioPanel.add(tgfRadioButton);

		JPanel dropPanel = new JPanel();
		TitledBorder dropPanelBorder = new TitledBorder(null, "Drop files here", TitledBorder.LEADING, TitledBorder.TOP, null, null);
		dropPanel.setBorder(dropPanelBorder);
		dropPanel.setBounds(10, 11, 290, 86);
		frmSemanticGraphConverter.getContentPane().add(dropPanel);
		dropPanel.setLayout(null);

		// -----------
		outputFormat.value = "csv"; // remember to sync this with the csvRadioButton.setSelected(true) above
		DragUtils.enableDragAndDrop(dropPanel, outputFormat, frmSemanticGraphConverter);
	}
}
