package ck.textstorm;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.Holder;

/**
 * 
 * @author João Carlos Ferreira Gonçalves - jcfgonc@gmail.com
 *
 */
@WebService(portName = "BridgingAssociatorPort", serviceName = "BridgingAssociatorService")
public class TextStormWebService {

	public @XmlElement(nillable = false, required = true, type = String.class) String textStorm(
			@WebParam(name = "inputText") @XmlElement(nillable = false, required = true, type = String.class) String inputText) {
		try {
			String result = TextStorm.invokeTextStorm(inputText);
			System.gc();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "exception";
	}

	public void textStormV2(
			@WebParam(name = "tex") @XmlElement(nillable = false, required = true, type = String.class) String tex,
			@WebParam(name = "pro", mode = WebParam.Mode.OUT) Holder<String> pro,
			@WebParam(name = "nil", mode = WebParam.Mode.OUT) Holder<String> nil) {
		try {
			String result = TextStorm.invokeTextStorm(tex);
			pro.value = result;
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		pro.value = "error occurred";
	}
}
