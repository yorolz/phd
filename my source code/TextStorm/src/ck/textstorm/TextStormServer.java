package ck.textstorm;

import javax.xml.ws.Endpoint;

/**
 * 
 * @author João Carlos Ferreira Gonçalves - jcfgonc@gmail.com
 *
 */
public class TextStormServer {

	public static void main(String args[]) throws Exception {
		System.out.println("Starting Server");
		TextStormWebService implementor = new TextStormWebService();
		 String address = "http://0.0.0.0:8080/textstorm";
		//String address = "http://brunski.dei.uc.pt:8080/textstorm";
		@SuppressWarnings("unused")
		Endpoint publish = Endpoint.publish(address, implementor);
		System.out.println("TextStormServer ready...");
	}
}
