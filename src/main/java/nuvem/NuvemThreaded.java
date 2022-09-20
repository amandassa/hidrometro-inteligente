/**
 * Criado em 13 de set de 2022
 */
package nuvem;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.json.JSONObject;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;

/**
 * @author Amanda Silva
 */
public class NuvemThreaded {
	
	public static final int PORTA = 12345;
	public static final int N_CONEXOES = 8;
	public static final int VALOR_M3 = 2;
	private Hashtable<Integer, ArrayList<JSONObject>> db = new Hashtable<Integer, ArrayList<JSONObject>>();
	
	public static BufferedImage generateBarcodeImage(String barcodeText) throws Exception {
	    Barcode barcode = BarcodeFactory.createEAN13(barcodeText);

	    return BarcodeImageHandler.getImage(barcode);
	}
	
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

	private int mesF (Iterator<JSONObject> it) {
		String datahora = (String) it.next().get("data");
		String[] data = datahora.split(" ");
		String[] mes = data[0].split("/");
		return Integer.parseInt(mes[1]);
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
			/** Esperar por uma conexão. 
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
					 // passar a linha pelo matcher
					 	// se for http, da split no espaço
					 	// verifica o tamanho do array (3) [0 1 2]
					 	// da split nas / no indice 1
					 	// verifica o tamanho do array (tamanho = 2 se tiver rota
					 	// A rota está no índice 1
					 
					 if ((line) != null) {
						 // line = mensagem do hidrometro
						boolean httpMatcher = Pattern.matches("(\\w+)\\s\\/(\\w*)\\/?(\\w*)?\\s(HTTP)\\/(\\d.\\d)\\s*?", line);

						if (httpMatcher) {
			            	/**
			            	 * deve tratar a requisição GET ou POST
			            	 * -- ver hidrômetro ou bloquear hidrômetro
			            	 */
							String[] req = line.split(" ");
							String[] req2 = req[1].split("/");
							String rota = null;
							if (req2.length > 2) rota = req2[2];
							int param = Integer.parseInt(req2[1]);
							switch (req[0]) {
							case "POST":	// bloquear hidrometro
								//	ADICIONAR: Verificar se é administrador
								try {
									JSONObject jObject = new JSONObject();
									jObject.put("bloqueado", true);
									
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
							    	
							    	
								} catch (NumberFormatException e) {
									System.out.println("Parâmetro não numérico não é aceito!!");
								} catch (Exception e) {
									System.out.println("Problema ao conectar com hidrômetro (TCP) ");
								}
								
								break;
							case "GET":	// ver hidrometro
								// se ROTA nao é nulo: 
									// ler qual rota está pedindo
									// se FATURA ou PAGAR
								if (rota != null) {
									rota = rota.toLowerCase();
									ArrayList<JSONObject> hidroLista;
									String res = "";
									switch (rota) {
									case "fatura":
										// retorna p/ webclient VALOR E CODIGO DE BARRAS
										hidroLista = db.get(param);
										Iterator<JSONObject> it = hidroLista.iterator();
										int consumoTotal = 0;
										int mes = mesF(it);
										while (it.hasNext()) {
											consumoTotal += Integer.parseInt(it.next().get("consumo").toString());
										}
										var valorFatura = (consumoTotal*VALOR_M3);
										String head = "<html><head>" +
								    			 "<title>Nuvem da concessionaria</title></head>";
										res = "<h2>Fatura da conta "+param+"</h2>"+
												"<p>VALOR: "+valorFatura+",00"+
												"<br> Referente ao mes "+mes+"</p></html>";
										
								    	pw.print("HTTP/1.0 200 OK" + CRLF);
								    	pw.print("Content-type: text/html" + CRLF);
								    	pw.print("Server-name: Nuvem da concessionaria" + CRLF);
								    	pw.print("Content-length: "+ (head.length() + res.length()) + CRLF);
								    	pw.print(CRLF);
								    	pw.print(head + res);
								    	pw.flush();
								    	pw.close();

										break;
									case "pagar":
										// Envia p/ hidrometro json DESBLOQUEADO
										JSONObject jObject = new JSONObject();
										jObject.put("bloqueado", false);
										
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
								    	res = "<html><head>" +
								    			 "<title>API da concessionaria</title></head>\n"+
								    			 "<h2>Pagamento de fatura</h2>"+
								    			 "<p>A fatura do cliente "+param+" foi paga com sucesso.</p></html>";
								    	pw.print("Content-length: "+res.length()+CRLF);
								    	pw.print(CRLF);
								    	pw.print(res);
								    	pw.flush();
								    	pw.close();
										break;
									default:
										break;
									}
								}
								System.out.println("GET REQUEST param: "+param+" route: "+rota);
								// 1º: mostrar o historico do hidrometro.
									// cada indice do db deve corresponder a uma LISTA de registros do hidrometro
									// iterar sobre a lista e printar cada registro.
								ArrayList<JSONObject> hidroLista;
								String res = "<h1>Historico Hidrometro "+param+"</h1>";
								try {
									hidroLista = db.get(param);
									Iterator<JSONObject> it = hidroLista.iterator();
									while (it.hasNext()) {
										JSONObject next = it.next();
										res += (next.get("data").toString()+"&nbsp;&#8209;&nbsp;"+
												"Consumo: "+next.get("consumo").toString()+"&nbsp;&#8209;&nbsp;"+
												"Bloquado: "+next.get("bloqueado").toString());
										res += "<br>";
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								res += "</html>";
								String head = "<html><head>" +
						    			 "<title>Nuvem da concessionaria</title></head>";

						    	pw.print("HTTP/1.0 200 OK" + CRLF);
						    	pw.print("Content-type: text/html" + CRLF);
						    	pw.print("Server-name: API da concessionaria" + CRLF);
						    	pw.print("Content-length: "+ (head.length() + res.length()) + CRLF);
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
				 } catch (Exception e) {
					 System.out.println(getName() + ": IO Error on socket " + e);
					 return;
				 }
			 }
		}
	}
}
