/**
 * Criado em 24 de ago de 2022
 */
package hidrometro;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Amanda
 */
public class Hidrometro extends Thread {
	private int codigo;
	private int vazao;
	private int consumo;
	private boolean bloqueado;
	private final static int TIME = 1000;
	
	public Hidrometro(int codigo, int vazao) {
		super();
		this.codigo = codigo;
		this.vazao = vazao;
		this.consumo = 0;
		this.bloqueado = false;
		this.start();
	}
	public static void main (String [] args) {
		Hidrometro h1 = new Hidrometro(new Random().nextInt(20), 2);
	}
	
	/**
	 * Envia o status do hidrometro
	 * @return	String de um json serializado
	 */
	public String status () {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("codigo",this.codigo);
			jsonObj.put("vazao", this.vazao);
			jsonObj.put("consumo", this.consumo);
			jsonObj.put("bloqueado", this.bloqueado);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String msg = jsonObj.toString();
		return msg;
	}
	
	
	/**
	 * Abre conexão e mantém escuta ao servidor.
	 * @return JSONObject correspondente ao hidrômetro
	 */
	public JSONObject escutar () {
    	// abre socket e aguarda aceite do servidor
		// esse canal nunca fecha pois o bloqueio/desbloqueio deve ser imediato
	    try {
	    	ServerSocket serverTCP = new ServerSocket(codigo);
	    	Socket cliente = serverTCP.accept();
	    	BufferedReader is = new BufferedReader(
					 new InputStreamReader(cliente.getInputStream()));
	    	String line = is.readLine();
	    	is.close();
	        System.out.println("Mensagem do servidor: " + line);
		      
//	    	Socket cliente = new Socket("localhost", 12345);
//	    	os.close();
	        
		      JSONObject jObject = new JSONObject(line);
		      
		      if (jObject.get("bloqueado").equals(true)) {
		    	  this.setBloqueado(true);
//			    	PrintStream os = new PrintStream(cliente.getOutputStream(), true);
//			    	os.print("200"+"\r\n");
//			    	os.flush();
//		    	  enviar("200");
		    	  System.out.println("HIDROMETRO BLOQUEADO!!!!");
		      } else {
		    	  this.setBloqueado(false);
		      }
		      
		      return jObject;
		    }
		    catch(Exception e) {
		      System.out.println("Erro: " + e.getMessage());
		    }
	    return null;
	}

	/**
	 * Envia uma mensagem ao servidor
	 * @param mensagem String
	 */
	public void enviar (String mensagem) {
	    try {
	    	Socket cliente = new Socket("localhost", 12345);
	    	PrintStream os = new PrintStream(cliente.getOutputStream(), true);
	    	os.print(mensagem+"\n");
	    	os.flush();
//	    	ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
//	    	saida.writeObject(mensagem);
//	    	saida.close();		// fecha do lado do cliente
//	    	saida.flush();
	    	cliente.close();
	    } catch(Exception e) {
	       System.out.println("Erro: " + e.getMessage());
	    }
	}
	
	/**
	 * Incrementa o consumo a aprox. cada 1 segundo.
	 */
	@Override
	public void run () {
		Thread escutar =  new Thread(() -> escutar());
		try {
			while (true) {
				try {
					escutar.start();
				} catch (java.lang.IllegalThreadStateException e) {
					System.out.println("Hidrômetro "+this.codigo+" escutando");
				}
				this.consome();
				Thread enviar = new Thread(() -> enviar(status()));
				enviar.start();
				sleep(TIME);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Incrementa o consumo de acordo com a vazão do hidrômetro.
	 */
	public void consome () {
		if (!this.isBloqueado()) this.setConsumo(this.getConsumo()+this.getVazao());
	}
	
	public int getCodigo() {
		return codigo;
	}
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}
	public int getVazao() {
		return vazao;
	}
	public void setVazao(int vazao) {
		this.vazao = vazao;
	}
	public int getConsumo() {
		return consumo;
	}
	public void setConsumo(int consumo) {
		this.consumo = consumo;
	}
	public boolean isBloqueado() {
		return bloqueado;
	}
	public void setBloqueado(boolean bloqueado) {
		this.bloqueado = bloqueado;
	}
}
