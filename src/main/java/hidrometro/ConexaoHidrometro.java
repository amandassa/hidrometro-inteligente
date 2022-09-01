/**
 * Criado em 31 de ago de 2022
 */
package hidrometro;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Random;

/**
 * @author Amanda Silva
 */
public class ConexaoHidrometro {
	private Hidrometro hidrometro = new Hidrometro(new Random().nextInt(100), new Random().nextInt(10));
	
	public static void main(String[] args) {
	    try {
	      Socket cliente = new Socket("127.0.0.1", 12345);
	      ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
	      String mensagem = (String) entrada.readObject();
	      System.out.println("Mensagem do servidor: " + mensagem);
	      entrada.close();
	      System.out.println("Conexão encerrada");
	    }
	    catch(Exception e) {
	      System.out.println("Erro: " + e.getMessage());
	    }
	}
}
