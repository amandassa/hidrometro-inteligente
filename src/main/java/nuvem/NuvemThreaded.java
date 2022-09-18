/**
 * Criado em 13 de set de 2022
 */
package nuvem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.apache.jasper.compiler.JavacErrorDetail;
import org.apache.jasper.tagplugins.jstl.core.Param;
import org.json.JSONObject;

/**
 * @author Amanda Silva
 */
public class NuvemThreaded {
	
	public static final int PORTA = 12345;
	public static final int N_CONEXOES = 8;
	
	private Hashtable<Integer, ArrayList<JSONObject>> db = new Hashtable<Integer, ArrayList<JSONObject>>();
	
	public boolean addToDatabase (JSONObject mensagem) {
		try {
			// se o hidrometro já existe no db (nova contagem de hidrometro)
				// encontrar arraylist dele
				// adicionar mensagem à arraylist
			Object codigo = mensagem.get("codigo");
			if (db.containsKey(Integer.parseInt(codigo.toString()))) {
				ArrayList<JSONObject> lista = db.get(codigo);
				lista.add(mensagem);
			}
			// se o hidrometro NÃO consta no db (novo hidrometro)
				// criar nova arraylist e adicionar mensagem à ela
				// adicionar arraylist à hashtable
			else {
				ArrayList<JSONObject> lista = new ArrayList<JSONObject>();
				lista.add(mensagem);
				Integer cod = (Integer) mensagem.get("codigo");
				db.put(cod, lista);
			}
			return true;
		} catch (NumberFormatException e) {
			System.out.println(e.getMessage());
			return false;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
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
					 
					 PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
					 String CRLF = "\r\n";

					 String line = is.readLine();
					 if ((line) != null) {
						 // line = mensagem do hidrometro
						boolean httpMatcher = Pattern.matches("(\\w+)\\s\\/(\\w*)\\s(HTTP)\\/(\\d.\\d)\\s*?", line);

						if (httpMatcher) {
			            	/**
			            	 * deve tratar a requisição GET ou POST
			            	 * -- ver hidrômetro ou bloquear hidrômetro
			            	 */
							String[] req = line.split("/");
							String[] req2 = req[1].split(" ");
							int param = Integer.parseInt(req2[0]);
							switch (req[0]) {
							case "POST ":	// bloquear hidrometro
								
								try {
//									System.out.println("Hidrômetro buscado:\t\t"+db.get(param).get("bloqueado").toString());
//									boolean isBlock = db.get(param).get("bloqueado").equals(true);
									JSONObject jObject = new JSONObject();
									/**
									 * Se o hidrometro já está bloqueado a req POST o desbloqueia
									 * e vice-versa.
									 * ISSO PRECISA SER ALTERADO PARA desbloqueio quando a fatura for paga
									 */
//									if (isBlock) {
//										jObject.put("bloqueado", false);
//									} else {
										jObject.put("bloqueado", true);
//									}
									
									// enviar json com bloqueio para o hidrômetro
									Socket cliente = new Socket("localhost", param);
							    	PrintStream os = new PrintStream(cliente.getOutputStream(), true);
							    	os.print(jObject.toString());
							    	os.flush();
							    	os.close();
							    	// fecha do lado do cliente (o server do hidrometro continua aberto.)
							    	cliente.close(); 

							    	// enviar res para o cliente socket (HTTP client)
							    	pw.print("HTTP/1.0 200 OK" + CRLF);
							    	pw.print("Content-type: text/html" + CRLF);
							    	pw.print("Server-name: Nuvem da concessionaria" + CRLF);
							    	String res = 
							    			"<html><head>" +
							    			 "<title>API da concessionaria</title></head>\n"+
							    			 "<h1>Bloqueio do hidrometro</h1>"+
							    			 "<p>O hidrometro ID:"+param+" foi bloqueado com sucesso.</p>";
							    	pw.print("Content-length: "+res.length()+CRLF);
							    	pw.print(CRLF);
							    	pw.print(res);
							    	pw.flush();
							    	pw.close();
							    	
							    	
							    	// receber confirmação de bloqueio (TCP Protocol)
//							    	BufferedReader input = new BufferedReader(
//											 new InputStreamReader(cliente.getInputStream()));
//							    	String res = input.readLine();
//							    	System.out.println("RES DO HIDROMETRO "+ res);
//							    	System.out.println((res == "200") ? "HIDROMETRO "+param+"BLOQUEADO" : "ERRO AO BLOQUEAR HIDRÔMETRO "+param);
//							    	input.close();
//							    	cliente.close();
								} catch (NumberFormatException e) {
									System.out.println("Parâmetro não numérico não é aceito!!");
								} catch (Exception e) {
									System.out.println("Problema ao conectar com hidrômetro (TCP) ");
								}
								
								break;
							case "GET ":	// ver hidrometro
								System.out.println("GET REQUEST"+" param: "+param);
								// 1º: mostrar no terminal o historico do hidrometro.
									// cada indice do db deve corresponder a uma LISTA de registros do hidrometro
									// iterar sobre a lista e printar cada registro.
								ArrayList<JSONObject> hidroLista = db.get(param);
								String res = "<h1>Historico Hidrometro "+param+"</h1>";
								Iterator<JSONObject> it = hidroLista.iterator();
								while (it.hasNext()) {
									res += ("Consumo: "+it.next().get("consumo").toString()+"&nbsp;&#8209;&nbsp;");
									res += ("Bloquado: "+it.next().get("bloqueado").toString());
									res += "<br>";
								}
								String head = "<html><head>" +
						    			 "<title>Nuvem da concessionaria</title></head>";

						    	pw.print("HTTP/1.0 200 OK" + CRLF);
						    	pw.print("Content-type: text/html" + CRLF);
						    	pw.print("Server-name: API da concessionaria" + CRLF);
						    	pw.print("Content-length: "+ head.length() + res.length() + CRLF);
						    	pw.print(CRLF);
						    	pw.print(head+res);
						    	pw.flush();
						    	pw.close();
								break;
							default:
								break;
							}
						} else {
							try {
								JSONObject my_obj = new JSONObject(line);
								if (addToDatabase(my_obj))
									System.out.println("Mensagem do hidrômetro: ");
							} catch (Exception e) {
								System.out.println("ERRO AO SALVAR NO DB " + e.getMessage());
								System.out.println("Requisição inválida.");
							}
						}
						 System.out.println(line);
						 line = is.readLine();
					 }
					 is.close();
					 clientSocket.close();
					 System.out.println(getName() + " ENDED ");
				 } catch(NoSuchElementException e) {
					 System.out.println(e +"\n"+ e.getMessage());
				 } catch (Exception e) {
					 System.out.println(getName() + ": IO Error on socket " + e);
					 return;
				 }
			 }
		}
	}
}
