public class Misura 
{
	private char tipoMisura;
	private int valoreMisura;
	
	public Misura(char tipoMisura, int valoreMisura)
	{
		this.tipoMisura=tipoMisura;
		this.valoreMisura=valoreMisura;
	}
	
	public char getTipo()
	{
		return tipoMisura;
	}
	public int getValore()
	{
		return valoreMisura;
	}
	
	public String toString()
	{
		return tipoMisura+": "+valoreMisura;
	}
}