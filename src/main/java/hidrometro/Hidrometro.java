package hidrometro;
import static java.lang.Thread.sleep;

/**
 * @author Amanda
 */
public class Hidrometro extends Thread {
	private int codigo;
	private float vazao;
	private float consumo;
	private boolean bloqueado;
	
	public Hidrometro(int codigo, float vazao) {
		super();
		this.codigo = codigo;
		this.vazao = vazao;
		this.consumo = 0;
		this.bloqueado = false;
		this.start();
	}
	
	/**
	 * Incrementa o consumo a aprox. cada 5 segundos.
	 */
	@Override
	public void run () {
		try {
			while (true) {
				this.consome();
				System.out.println("Consumindo ");
				sleep(10000);
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
	public float getVazao() {
		return vazao;
	}
	public void setVazao(float vazao) {
		this.vazao = vazao;
	}
	public float getConsumo() {
		return consumo;
	}
	public void setConsumo(float consumo) {
		this.consumo = consumo;
	}
	public boolean isBloqueado() {
		return bloqueado;
	}
	public void setBloqueado(boolean bloqueado) {
		this.bloqueado = bloqueado;
	}
}
