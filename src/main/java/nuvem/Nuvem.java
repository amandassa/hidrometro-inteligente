/**
 * Criado em 31 de ago de 2022
 */
package nuvem;

import java.util.regex.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * @author Amanda Silva
 */
public class Nuvem {
	private static Map<Integer, JSONObject> db = new HashMap<Integer, JSONObject>();
	
	public static void addToDatabase (JSONObject mensagem) {
		db.put(Integer.getInteger(mensagem.get("codigo").toString()), mensagem);
	}
	
	public static void main(String[] args) {
	    try (ServerSocket server = new ServerSocket(12345)) {
		      // Instancia o ServerSocket ouvindo a porta
		      System.out.println("Servidor ouvindo a porta 12345");
		      
		        while (true) {
		            Socket clientSocket = server.accept();
		            InputStreamReader isr =  new InputStreamReader(clientSocket.getInputStream());
		            BufferedReader reader = new BufferedReader(isr);
		            String line = reader.readLine();
		            while (!line.isEmpty()) {
		            	// REGEX que identifica a 1a linha da requisição
			            boolean httpMatcher = Pattern.matches("(\\w+)\\s\\/\\(\\w*)\\s(HTTP)\\/(\\d.\\d)\\s*?", line);
			            boolean hidroMatcher = Pattern.matches("", line);
			            
			            // Requisição do cliente HTTP
			            if (httpMatcher) {
			            	/**
			            	 * deve tratar a requisição GET ou PUT
			            	 * -- ver hidrômetro ou bloquear hidrômetro
			            	 */
			            } else if (hidroMatcher) {
			            	/**
			            	 * deve guardar os dados do hidrômetro
			            	 */
			            } else {
			            	/**
			            	 * deve tratar requisição inválida
			            	 */
			            }
			            
		                System.out.println(line);
		                line = reader.readLine();
		            }
		            // testar linha 1 da req:
		        }
	      } catch(Exception e) {
	    	  System.out.println("Erro: " + e.getMessage());
	    }
	}
}
