/**
 * Criado em 31 de ago de 2022
 */
package nuvem;

import java.io.ObjectInputStream;
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
	    try {
		      // Instancia o ServerSocket ouvindo a porta
		      @SuppressWarnings("resource")
			ServerSocket servidor = new ServerSocket(12345);
		      System.out.println("Servidor ouvindo a porta 12345");
		      
		      while(true) {
				// o método accept() bloqueia a execução até que o servidor receba um pedido de conexão
				Socket cliente = servidor.accept();
				System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress());
				
				// isso faz se o hidrometro se conectar
				// receber mensagens do cliente
				ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
				String mensagem = (String) entrada.readObject();
				JSONObject jsonObject = new JSONObject(mensagem);
				//	SALVAR TUDO NO BANCO
				addToDatabase(jsonObject);

				System.out.println("Mensagem do hidrômetro: " + mensagem);

				cliente.close();
		      }
		    }
		    catch(Exception e) {
		       System.out.println("Erro: " + e.getMessage());
		    }
	}

}
