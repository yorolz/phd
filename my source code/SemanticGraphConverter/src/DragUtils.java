import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import graph.GraphReadWrite;
import graph.StringGraph;

public class DragUtils {

	private static void convertFiles(List<File> list, String outputFormat, JFrame mainFrame) {
		int convertedFiles = 0;
		// Now get the first file from the list
		for (File f : list) {
			if (f.isFile()) {
				convertFile(f, outputFormat);
				convertedFiles++;
			} else {
				// TODO get file list recursively
			}
		}
		infoBox(String.format("Converted %d files of %d", convertedFiles, list.size()), "Report", mainFrame);
	}

	private static void convertFile(File file, String outputFormat) {
		try {
			StringGraph graph = GraphReadWrite.readAutoDetect(file);
			String canonicalPath = file.getCanonicalPath();
			// generate new filename
			String pathNoExtension = FilenameUtils.removeExtension(canonicalPath);
			String outFilename = createNewFilenameIncreasedIfExisting(pathNoExtension, outputFormat);
			switch (outputFormat) {
			case "dt":
				String baseName = FilenameUtils.getBaseName(canonicalPath); // use basename as domain
				GraphReadWrite.writeDT(outFilename, graph, baseName);
				break;
			case "pro":
				GraphReadWrite.writePRO(outFilename, graph);
				break;
			case "tgf":
				GraphReadWrite.writeTGF(outFilename, graph);
				break;
			case "csv":
				GraphReadWrite.writeCSV(outFilename, graph);
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * dumb name, ie, creates a new filename with unique identifier at the end if an existing file with the same name exists
	 * 
	 * @param pathNoExtension
	 * @param outputFormat    extension
	 * @return
	 */
	private static String createNewFilenameIncreasedIfExisting(String pathNoExtension, String outputFormat) {
		String outFilename = pathNoExtension + "." + outputFormat;
		int num = 0;
		while (new File(outFilename).exists()) {
			outFilename = pathNoExtension + "_" + (Integer.toString(num++)) + "." + outputFormat;
		}
		return outFilename;
	}

	public static DropTarget enableDragAndDrop(Component whereTo, Holder<String> outputFormat, JFrame mainFrame) {
		DropTarget target = new DropTarget(whereTo, new DropTargetListener() {
			public void dragEnter(DropTargetDragEvent e) {
			}

			public void dragExit(DropTargetEvent e) {
			}

			public void dragOver(DropTargetDragEvent e) {
			}

			public void dropActionChanged(DropTargetDragEvent e) {

			}

			public void drop(DropTargetDropEvent e) {
				try {
					// Accept the drop first, important!
					e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

					// Get the files that are dropped as java.util.List
					Transferable transferable = e.getTransferable();
					@SuppressWarnings("unchecked")
					List<File> list = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

					convertFiles(list, outputFormat.value, mainFrame);
				} catch (Exception ex) {
				}
			}

		});
		return target;
	}

	public static void infoBox(String infoMessage, String titleBar, Component parentComponent) {
		JOptionPane.showMessageDialog(parentComponent, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
	}

}
