package ck.textstorm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.ws.Endpoint;

//Existe uma pasta na raiz desta máquina com a última versão funcional do TextStorm em:

///TextStorm
//
//Aquilo que o TextStorm recebe é um texto (sem nenhuma extensão em especial) e este precisa ser preprocessado para que seja possível criar o mapa conceptual (ficheiro.pro)
//
//2 linhas de comandos que são então necessárias executar para o preprocesamento:
//~>cd /TextStorm/TextStorm_python
//~>python ./TextStorm_Preprocessing.py diretoria/ficheiro_texto
//
//Cria 2 ficheiros (.lex e .syn) com o resultado do pre-processamento e os guarda na mesma diretoria onde está o ficheiro_texto.
//
//2 linhas de comandos para criar o mapa propriamente dito:
//~>cd /TextStorm/CPrologInvoker
//~>./textstorm diretoria/ ficheiro_texto

/**
 * 
 * @author João Carlos Ferreira Gonçalves - jcfgonc@gmail.com
 *
 */
public class TextStorm {

	// private static final String textStormFolder = "/TextStorm";
	private static final String textStormPythonFolder = "/TextStorm/TextStorm_python";
	private static final String textStormPrologFolder = "/TextStorm/CPrologInvoker";
	private static final String textStormWorkingFolder = "/TextStorm/webServiceData";
	private static AtomicInteger fileCounter = new AtomicInteger(0);

	public static String invokeTextStorm(String text) throws IOException, InterruptedException {
		text = cleanText(text);
		// save input text string to a file
		String inputFilename = generateFilenameWithTimestamp() + "_inputText";
		File inputTextFile = new File(textStormWorkingFolder, inputFilename);
		writeFile(text, inputTextFile);

		// invoke TextStorm_Preprocessing.py with the given text
		// python ./TextStorm_Preprocessing.py diretoria/ficheiro_texto
		ArrayList<String> arguments = new ArrayList<String>();
		arguments.add("python");
		arguments.add("./TextStorm_Preprocessing.py");
		arguments.add(inputTextFile.getCanonicalPath());
		int code = ProcessLauncher.launch(textStormPythonFolder, arguments);
		System.out.println("invocation returned code: " + code);

		// ~>cd /TextStorm/CPrologInvoker
		// ~>./textstorm diretoria/ ficheiro_texto
		arguments.clear();
		arguments.add("./textstorm");
		arguments.add(inputTextFile.getParent() + File.separatorChar);
		arguments.add(inputTextFile.getName());
		// arguments.add("/TextStorm/examples/");
		// arguments.add("2016-02-07_21-08-49_inputText");
		code = ProcessLauncher.launch(textStormPrologFolder, arguments);
		System.out.println("invocation returned code: " + code);

		// now there is a <inputTextFile>.pro at the specified directory
		// read and return it
		String pro = readFile(new File(inputTextFile.getParent(), inputTextFile.getName() + ".pro"));
		return pro;
	}

	private static String cleanText(String text) {
		// known limitation, textstorm cannot handle symbol '
		// replace ' by "
		text = text.replaceAll("\'", "");
		text = text.replaceAll("\"", "");
		return text;
	}

	public static String generateFilenameWithTimestamp() {
		int fileID = fileCounter.getAndIncrement();
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String filename = dateFormat.format(date) + "_" + Integer.toString(fileID);
		return filename;
	}

	public static void launchWebService() throws Exception {
		System.out.println("Starting Server");
		TextStormWebService implementor = new TextStormWebService();
		// String address = "http://0.0.0.0:8080/bridging"; //for local
		String address = "http://acme.dei.uc.pt:8080/textstorm"; // for public
		@SuppressWarnings("unused")
		Endpoint publish = Endpoint.publish(address, implementor);
	}

	public static String readFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		String str = new String(data, "UTF-8");
		return str;
	}

	public static void writeFile(String content, File file) throws IOException {
		System.out.println("writing text file to " + file.getCanonicalPath());
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.getParentFile().mkdir();
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close();

	}

}
