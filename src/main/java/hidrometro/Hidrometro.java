/**
 * Criado em 24 de ago de 2022
 */
package hidrometro;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
	
	public Hidrometro(int codigo, int vazao) {
		super();
		this.codigo = codigo;
		this.vazao = vazao;
		this.consumo = 0;
		this.bloqueado = false;
		this.start();
	}
	
	public static void main (String [] args) {
		Hidrometro h = new Hidrometro(1, 2);
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
		return jsonObj.toString();
	}
	
	/**
	 * Abre conexão e mantém escuta ao servidor.
	 * @return JSONObject correspondente ao hidrômetro
	 */
	public JSONObject escutar () {
    	// abre socket e aguarda aceite do servidor
		// esse canal nunca fecha pois o bloqueio/desbloqueio deve ser imediato
	    try (Socket cliente = new Socket("localhost", 12345)){
		      ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
		      String mensagem = (String) entrada.readObject();
		      
		      JSONObject jObject = new JSONObject(mensagem);
		      
		      if (jObject.get("bloqueado").equals("true")) {
		    	  this.setBloqueado(true);
		      } else {
		    	  this.setBloqueado(false);
		      }
		      
		      System.out.println("Mensagem do servidor: " + mensagem);
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
	    try (Socket cliente = new Socket("127.0.0.1", 12345)){
	    	ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
	    	saida.writeObject(mensagem);
	    	saida.close();
	    } catch(Exception e) {
	       System.out.println("Erro: " + e.getMessage());
	    }
	}
	
	/**
	 * Incrementa o consumo a aprox. cada 10 segundos.
	 */
	@Override
	public void run () {
		try {
			while (true) {
				this.consome();
				this.enviar(status());
				sleep(1000);
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
