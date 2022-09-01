/**
 * Criado em 31 de ago de 2022
 */
package nuvem;

import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Amanda Silva
 */
public class Nuvem {
	public static void main(String[] args) {
	    try {
	      // Instancia o ServerSocket ouvindo a porta
	      ServerSocket servidor = new ServerSocket(12345);
	      System.out.println("Servidor ouvindo a porta 12345");
	      while(true) {
			// o método accept() bloqueia a execução até que
			// o servidor receba um pedido de conexão
			Socket cliente = servidor.accept();
			System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress());
			ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
			saida.flush();
			saida.writeObject(new String("Mensagem do servidor."));
			saida.close();
			cliente.close();
	      }
	    }
	    catch(Exception e) {
	       System.out.println("Erro: " + e.getMessage());
	    }
	    finally {}
	}
}
