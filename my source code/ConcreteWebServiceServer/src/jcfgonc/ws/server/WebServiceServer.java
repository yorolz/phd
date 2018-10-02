
package jcfgonc.ws.server;

import javax.xml.ws.Endpoint;

import jcfgonc.ws.webservice.WebServices;

public class WebServiceServer {

	public static void main(String args[]) throws Exception {
		System.out.println("Starting WebServiceServer.");
		String serverUrl = "http://0.0.0.0:8080";
		Endpoint.publish(serverUrl + "/ws", new WebServices());

		System.out.println("WebServiceServer listening.");
	}
}
