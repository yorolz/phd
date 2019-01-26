package jcfgonc.ws.webservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.Holder;

import ck.textstorm.TextStorm;
import graph.GraphReadWrite;
import graph.StringGraph;
import graph.StringReaderUnsynchronized;
import jcfgonc.bridging.DomainSpotterLauncher;
import jcfgonc.eemapper.MapperGeneticOperations;
import jcfgonc.eemapper.MappingStructure;
import jcfgonc.genetic.GeneticAlgorithm;
import jcfgonc.genetic.GeneticAlgorithmConfig;
import jcfgonc.ws.json.JsonGraphHandler;
import matcher.AnalogySet;
import matcher.MapperLauncher;

@WebService(portName = "ConCreTePort", serviceName = "ConCreTeService")
public class WebServices {

	public void textStorm(@WebParam(name = "txt") @XmlElement(nillable = false, required = true, type = String.class) String txt,
			@WebParam(name = "pro", mode = WebParam.Mode.OUT) Holder<String> pro, @WebParam(name = "nil", mode = WebParam.Mode.OUT) Holder<String> nil) {
		try {
			String result = TextStorm.invokeTextStorm(txt);
			pro.value = result;
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		pro.value = "error occurred";
	}

	@WebMethod
	public void domainSpotterDT(@WebParam(name = "dt", mode = WebParam.Mode.IN) @XmlElement(nillable = false, required = true, type = String.class) String dti,
			@WebParam(name = "ga_generations", mode = WebParam.Mode.IN) @XmlElement(type = String.class) String gen,
			@WebParam(name = "population_size", mode = WebParam.Mode.IN) @XmlElement(type = String.class) String pop,
			@WebParam(name = "inputSpace0", mode = WebParam.Mode.OUT) Holder<String> inputSpace1, @WebParam(name = "inputSpace1", mode = WebParam.Mode.OUT) Holder<String> inputSpace2) {
		try {
			// debug
			System.out.println("gaPopulationSize:" + pop + " gaMaximumGenerations:" + gen + " graphURL:" + dti);

			int generations = 64;
			try {
				generations = Integer.parseInt(gen);
			} catch (NumberFormatException e) {
			}

			int populationSize = 256;
			try {
				populationSize = Integer.parseInt(pop);
			} catch (NumberFormatException e) {
			}

			ArrayList<String> inputSpaces = DomainSpotterLauncher.partitionFromString(dti, generations, populationSize);
			inputSpace1.value = inputSpaces.get(0);
			inputSpace2.value = inputSpaces.get(1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@WebMethod
	public void fromDivagoToJson(@WebParam(name = "dt", mode = WebParam.Mode.IN) @XmlElement(nillable = false, required = true, type = String.class) String divago,
			@WebParam(name = "json", mode = WebParam.Mode.OUT) Holder<String> json, @WebParam(name = "nil", mode = WebParam.Mode.OUT) Holder<String> nil) {
		try {
			System.out.println("RECEIVED::" + divago);
			StringGraph graph = new StringGraph();
			GraphReadWrite.readDT(new BufferedReader(new StringReader(divago)), graph);
			json.value = JsonGraphHandler.toJson(graph);
			System.out.println("SENT::" + json.value);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		json.value = "an error occurred";
	}

	@WebMethod
	public void fromJsonToDivago(@WebParam(name = "json", mode = WebParam.Mode.IN) @XmlElement(nillable = false, required = true, type = String.class) String json,
			@WebParam(name = "dt", mode = WebParam.Mode.OUT) Holder<String> divago, @WebParam(name = "nil", mode = WebParam.Mode.OUT) Holder<String> nil) {
		try {
			System.out.println("RECEIVED::" + json);
			// prevent parsing problems from known exceptions
			if (json.contains("\"graph\": {}")) {
				json = json.replace("\"graph\": {}", "\"graph\": []");
			}

			StringGraph graph = JsonGraphHandler.fromJson(json);
			StringWriter sw = new StringWriter();
			BufferedWriter bw = new BufferedWriter(sw);
			GraphReadWrite.writeDT(bw, graph, "webservice");
			divago.value = sw.toString();
			System.out.println("SENT::" + divago.value);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		divago.value = "an error occurred";
	}

	@WebMethod
	public void fromProToDT(@WebParam(name = "pro", mode = WebParam.Mode.IN) @XmlElement(nillable = false, required = true, type = String.class) String pro,
			@WebParam(name = "domain", mode = WebParam.Mode.IN) @XmlElement(nillable = false, required = true, type = String.class) String domain,
			@WebParam(name = "dt", mode = WebParam.Mode.OUT) Holder<String> dt, @WebParam(name = "nil", mode = WebParam.Mode.OUT) Holder<String> nil) {
		try {
			System.out.println("RECEIVED::" + pro);

			StringGraph graph = new StringGraph();
			GraphReadWrite.readPRO(new BufferedReader(new StringReader(pro)), graph);

			StringWriter sw = new StringWriter();
			GraphReadWrite.writeDT(new BufferedWriter(sw), graph, domain);
			dt.value = sw.toString();
			System.out.println("SENT::" + dt.value);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		dt.value = "an error occurred";
	}

	public void mapperDT(@WebParam(name = "inputSpace0", mode = WebParam.Mode.IN) @XmlElement(nillable = false, required = true, type = String.class) String is0,
			@WebParam(name = "inputSpace1", mode = WebParam.Mode.IN) @XmlElement(nillable = false, required = true, type = String.class) String is1,
			@WebParam(name = "outputIndex", mode = WebParam.Mode.IN) @XmlElement(type = String.class) String mx,
			@WebParam(name = "mapping", mode = WebParam.Mode.OUT) Holder<String> mappings, @WebParam(name = "debug", mode = WebParam.Mode.OUT) Holder<String> debug) {
		try {
			System.out.println(is0);
			System.out.println(is1);

			StringGraph inputSpace = new StringGraph();
			if (is0 != null && is0.length() > 0) {
				GraphReadWrite.readDT(new BufferedReader(new StringReader(is0)), inputSpace);
			}
			if (is1 != null && is1.length() > 0) {
				GraphReadWrite.readDT(new BufferedReader(new StringReader(is1)), inputSpace);
			}
			if (inputSpace.getVertexSet().size() > 0 && inputSpace.edgeSet().size() > 0) {
				ArrayList<AnalogySet> bestAnalogies = new ArrayList<AnalogySet>(MapperLauncher.findAllAnalogies(inputSpace, null, null));
				int nAnalogies = bestAnalogies.size();
				int index = 0;
				try {
					index = Integer.parseInt(mx) % nAnalogies;
				} catch (NumberFormatException e) {
				}
				AnalogySet firstAnalogy = bestAnalogies.get(index);

				int nMappings = firstAnalogy.size();
				debug.value = String.format("Got %d analogies with %d mappings each.\nRequested analogy %d.", nAnalogies, nMappings, index);
				StringWriter sw = new StringWriter();
				BufferedWriter bw = new BufferedWriter(sw);
				firstAnalogy.writeMappings(bw);
				mappings.value = sw.toString();
				return;
			}
			debug.value = "Nothing to do, supplied input space was empty.";
			mappings.value = "[]";

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void EEmapperDT(@WebParam(name = "is0_DT", mode = WebParam.Mode.IN) @XmlElement(nillable = false, required = true, type = String.class) String is0,
			@WebParam(name = "is1_DT", mode = WebParam.Mode.IN) @XmlElement(nillable = false, required = true, type = String.class) String is1,
			@WebParam(name = "population_size", mode = WebParam.Mode.IN) @XmlElement(type = String.class) String populationSize_str,
			@WebParam(name = "timeout_seconds", mode = WebParam.Mode.IN) @XmlElement(type = String.class) String timeout_seconds,
			@WebParam(name = "mapping", mode = WebParam.Mode.OUT) Holder<String> mapping, @WebParam(name = "deb", mode = WebParam.Mode.OUT) Holder<String> debug)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException,
			NumberFormatException {

		System.out.println(is0);
		System.out.println(is1);

		int timeout = 64;
		if (timeout_seconds != null && timeout_seconds.length() > 0) {
			timeout = Integer.parseInt(timeout_seconds);
		}

		int populationSize = 256;
		if (populationSize_str != null && populationSize_str.length() > 0) {
			populationSize = Integer.parseInt(populationSize_str);
		}

		GeneticAlgorithmConfig.POPULATION_SIZE = populationSize;
		GeneticAlgorithmConfig.MAXIMUM_TIME_SECONDS = timeout;
		GeneticAlgorithmConfig.MAXIMUM_GENERATIONS = Integer.MAX_VALUE;

		StringGraph inputSpace = null;
		inputSpace = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		if (is0 != null && is1 != null & is0.length() > 0 && is1.length() > 0) {
			GraphReadWrite.readDT(new BufferedReader(new StringReaderUnsynchronized(is0)), inputSpace);
			GraphReadWrite.readDT(new BufferedReader(new StringReaderUnsynchronized(is1)), inputSpace);
		}

		if (inputSpace != null && inputSpace.getVertexSet().size() > 0 && inputSpace.edgeSet().size() > 0) {

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			MapperGeneticOperations mgo = new MapperGeneticOperations(inputSpace);
			GeneticAlgorithm<MappingStructure<String, String>> ga = new GeneticAlgorithm<>(mgo, null);
			ga.execute();
			MappingStructure<String, String> best = ga.getBestGenes();
			mapping.value = best.writeMappingDivago();
			debug.value = best.writePairGraphDT();
			return;
		}
		mapping.value = "invalid input spaces or webservice call";
		debug.value = "invalid input spaces or webservice call";
	}

	@WebMethod
	public void fromDivagoToCSV(@WebParam(name = "dt", mode = WebParam.Mode.IN) @XmlElement(nillable = false, required = true, type = String.class) String in,
			@WebParam(name = "csv", mode = WebParam.Mode.OUT) Holder<String> out, @WebParam(name = "nil", mode = WebParam.Mode.OUT) Holder<String> nil) {
		try {
			System.out.println("RECEIVED::" + in);

			StringGraph graph = new StringGraph();
			GraphReadWrite.readDT(new BufferedReader(new StringReader(in)), graph);

			StringWriter sw = new StringWriter();
			BufferedWriter bw = new BufferedWriter(sw);
			GraphReadWrite.writeCSV(bw, graph);

			out.value = sw.toString();
			System.out.println("SENT::" + out.value);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.value = "an error occurred";
	}

	@WebMethod
	public void fromCSVtoDivago(@WebParam(name = "csv", mode = WebParam.Mode.IN) @XmlElement(nillable = false, required = true, type = String.class) String in,
			@WebParam(name = "dt", mode = WebParam.Mode.OUT) Holder<String> out, @WebParam(name = "nil", mode = WebParam.Mode.OUT) Holder<String> nil) {
		try {
			System.out.println("RECEIVED::" + in);

			StringGraph graph = new StringGraph();
			GraphReadWrite.readCSV(new BufferedReader(new StringReader(in)), graph);

			StringWriter sw = new StringWriter();
			BufferedWriter bw = new BufferedWriter(sw);
			GraphReadWrite.writeDT(bw, graph, "converted");

			out.value = sw.toString();
			System.out.println("SENT::" + out.value);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.value = "an error occurred";
	}

}
