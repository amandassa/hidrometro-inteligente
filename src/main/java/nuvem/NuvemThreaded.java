/**
 * Criado em 13 de set de 2022
 */
package nuvem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.jasper.compiler.JavacErrorDetail;
import org.apache.jasper.tagplugins.jstl.core.Param;
import org.json.JSONObject;

/**
 * @author Amanda Silva
 */
public class NuvemThreaded {
	
	public static final int PORTA = 12345;
	public static final int N_CONEXOES = 15;
	
	private static Map<Integer, JSONObject> db = new HashMap<Integer, JSONObject>();
	
	public static void addToDatabase (JSONObject mensagem) {
		db.put(Integer.getInteger(mensagem.get("codigo").toString()), mensagem);
	}

	
	/** Método para iniciar os servidores */
	 public static void main(String[] av) {
		 new NuvemThreaded();
	 }
	
	public NuvemThreaded () {
		try {
			ServerSocket server = new ServerSocket(PORTA);
			server.setReuseAddress(true);
			for (int i=0; i< N_CONEXOES; i++) {
				 new Handler(server, i).start();
			 }
		} catch (IOException e) {
			throw new RuntimeException("Could not create ServerSocket ", e);
		}
	}
	
	// Uma classe para gerenciar cada comunicação de cliente
	class Handler extends Thread {
		ServerSocket socket;
		int n_conexao;
		Handler(ServerSocket s, int i) {
			 socket = s;
			 n_conexao = i;
			 setName("Thread " + n_conexao);
		}
		
		public void run() {
			/**
			 * Esperar por uma conexão. 
			 * Sincronizado no ServerSocket quando o método accept() é chamado.
			 */
			 while (true) {
				 try {
					 System.out.println( getName() + " waiting");
					 Socket clientSocket;
					 // Aguarda por uma nova conexão
					 synchronized(socket) {
						 clientSocket = socket.accept();
					 }
					 System.out.println(getName() + " starting, IP=" +
					 clientSocket.getInetAddress());
					 BufferedReader is = new BufferedReader(
						 new InputStreamReader(clientSocket.getInputStream()));
//					 PrintStream os = new PrintStream(
//							 clientSocket.getOutputStream(), true);
					 String line = is.readLine();
					 while ((line) != null) {
						 // line = mensagem do hidrometro
						boolean httpMatcher = Pattern.matches("(\\w+)\\s\\/(\\w*)\\s(HTTP)\\/(\\d.\\d)\\s*?", line);
			            boolean hidroMatcher = Pattern.matches("\"\\{\\\"(.+?)\\\":(\\w+)\\,?\\\"(.+?)\\\":(\\w+)\\,?\\\"(.+?)\\\":(\\w+)\\,?\\\"(.+?)\\\":(\\w+)\\,?\\}\"", line);

						if (httpMatcher) {
			            	/**
			            	 * deve tratar a requisição GET ou PUT e rotas
			            	 * -- ver hidrômetro ou bloquear hidrômetro
			            	 */
							String[] req = line.split("/");
							String[] req2 = req[1].split(" ");
							switch (req[0]) {
							case "POST ":	// bloquear hidrometro
								
								try {
									int param = Integer.parseInt(req2[0]);
									JSONObject jObject = new JSONObject();
									jObject.put("bloqueado",true);
									Socket cliente = new Socket("localhost", param);
							    	PrintStream os = new PrintStream(cliente.getOutputStream(), true);
							    	os.print(jObject.toString());
							    	os.flush();
							    	os.close();
							    	cliente.close();
								} catch (NumberFormatException e) {
									System.out.println("Parâmetro não numérico não é aceito!!");
								} catch (Exception e) {
									System.out.println("Problema ao conectar com hidrômetro (TCP) ");
								}
								
//								System.out.println("POST REQUEST"+" param: "+req2[0]);
								break;
							case "GET ":	// ver hidrometro
								System.out.println("GET REQUEST"+" param: "+req2[0]);
								break;
							default:
								break;
							}
						} else if (hidroMatcher) {
							JSONObject my_obj = new JSONObject(line);
							addToDatabase(my_obj);
			            	System.out.println("Mensagem do hidrômetro: ");
						} else {
							/**
							 * req invalida
							 */
						}
						 System.out.println(line);
						 line = is.readLine();
					 }
					 is.close();
					 clientSocket.close();
					 System.out.println(getName() + " ENDED ");
				 } catch (Exception e) {
					 System.out.println(getName() + ": IO Error on socket " + e);
					 return;
				 }
			 }
		}
	}
}
