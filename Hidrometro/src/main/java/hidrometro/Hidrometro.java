/**
 * Criado em 24 de ago de 2022
 */
package hidrometro;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONException;
import org.json.*;

/**
 * @author Amanda
 */
public class Hidrometro extends Thread {
	private final static int TIME = 5000;
	private final static String NUVEM = "172.16.103.13";
	private int codigo;
	private int vazao;
	private int pressao;
	private int consumo;
	private boolean bloqueado;
	private String dataHora;
	
	public Hidrometro(int codigo, int vazao) {
		super();
		this.codigo = codigo;
		this.vazao = vazao;
		this.consumo = 0;
		this.bloqueado = false;
		this.pressao = vazao;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();
        this.dataHora = dtf.format(now);
        
		this.start();
	}
	public static void main (String [] args) {
		Hidrometro h1 = new Hidrometro(new Random().nextInt(20), new Random().nextInt(10));
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
			
			if (this.pressao<this.vazao) {
				jsonObj.put("vazando", true);
			} else {
				jsonObj.put("vazando", false);
			}
			
			jsonObj.put("consumo", this.consumo);
			jsonObj.put("data", this.dataHora);
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
		      JSONObject jObject = new JSONObject(line);
		      
		      if (jObject.get("bloqueado").equals(true)) {
		    	  this.setBloqueado(true);
		    	  System.out.println("HIDROMETRO BLOQUEADO!!!!");
		      } else {
		    	  this.setBloqueado(false);
		      }
		      
		      return jObject;
		    }
	    	catch (JSONException e) {
	    		System.out.println("Erro: " + e.getMessage());
	    		System.out.println("Servidor mandou mensagem em formato inválido.");
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
	    	Socket cliente = new Socket(NUVEM, 12345);
	    	PrintStream os = new PrintStream(cliente.getOutputStream(), true);
	    	os.print(mensagem+"\n");
	    	os.flush();
	    	cliente.close();
	    } catch(Exception e) {
	       System.out.println("Erro: " + e.getMessage());
	    }
	}
	
	public void simulador () {
		int leitor;
		do {
			System.out.println("Altere a pressão da água\nPressão abaixo da média gera alerta de vazamento");
			Scanner sc = new Scanner(System.in);
	        leitor = sc.nextInt();
			this.pressao = leitor;
			System.out.println("Pressao alterada para "+this.pressao);
			System.out.print("/////////////////////////////////////\r\n");
//			sc.close();
		} while (leitor!=13);
//        sc.next();
	}
	
	/**
	 * Incrementa o consumo a aprox. cada 1 segundo.
	 */
	@Override
	public void run () {
		Thread escutar = new Thread(() -> escutar());
		Thread simular = new Thread(() -> simulador());
		try { 
			while (true) {
				try {
					escutar.start();
					simular.start();
				} catch (java.lang.IllegalThreadStateException e) {
				// caso a thread já esteja em estado de execução
//					System.out.println("Hidrômetro "+this.codigo+" escutando");
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
