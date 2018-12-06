import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Random;

public class UDPServer extends  Thread
{
	//ATTRIBUTI, SOCKET di classe DatagramSocket
	private DatagramSocket socket;
	private static char tipoMisura;
	
	//COSTRUTTORE
	/**
	 * Costruttore: istanzia un socket sulla porta specificata
	 * e imposta un timeout di un secondo
	 * @param port: indica la porta da associare al server
	 * @throws SocketException eccezzione sollevata quando non è possibile istanziare il socket
	 */
	public UDPServer(int port, char tipoMisura) throws SocketException
	{
		//eccezione, ad esempio se la porta è già utilizzata da un altro processo
		socket=new DatagramSocket(port);
		//aggiungo timeout, in questo caso di 1 secondo. 
		//Ogni secondo solleva un eccezione che gestisco(se non riceve niente, altrimenti risponde)
		socket.setSoTimeout(1000);
		tipoMisura=this.tipoMisura;
	}
	
	public char getTipoMisura() {
		return tipoMisura;
	}

	//RUN
	/**
	 * rimane in ascolto sul Socket. 
	 * Quando riceve un datagramma estrae i dati E
	 * costruisce un datagramma di risposta con gli stessi dati
	 * Invia il datgramma di risposta alclient che aveva mandato la richiesta
	 * Se non riceve datagrammi di richiesta, genera un timeout ogni secondo
	 */
	public void run()
	{
		byte[] bufferRequest=new byte[2];//array di dati da 8 KB
		byte[] bufferAnswer=new byte[8192];
		DatagramPacket request=new DatagramPacket(bufferRequest, bufferRequest.length);
		DatagramPacket answer;
		ByteBuffer data=ByteBuffer.allocate(8);
		char messaggioRichiesta;
		int valoreMisura;
		
		while(!interrupted())
		{
			try 
			{
				//attesa del datagramma da parte del client
				//ricevo richiesta dal client
				socket.receive(request);
				//istanzio datagramma di risposta contenente i dati prelevati dal datagrammi richiesta, indirizzo IP e PORTA del client
				//creo pacchetto datagramma di risposta
				data.clear();
				data.put(request.getData());
				data.flip();
				messaggioRichiesta=data.getChar();
				
				if(messaggioRichiesta==63)
				{
					tipoMisura='T';
					valoreMisura=calcolaMisura();
					data.clear();
					data.putChar(tipoMisura);
					data.putInt(valoreMisura);
					data.flip();
					answer=new DatagramPacket(data.array(), data.limit(), request.getAddress(), request.getPort());
					//spedisco datagramma di risposta al client
					socket.send(answer);
				}
				
				
			} 
			//gestisco l'eccezione creata attraverso il timeout
			catch (SocketTimeoutException e) 
			{
				System.err.println("Timeout");
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		socket.close();
		//Usciamo dal while perhè il socket è stato interrotto,quindi chiudo il socket elibero porta
	}
	
	public int calcolaMisura()
	{
		Random x=new Random();
		int misura;
		
		if (getTipoMisura()=='T')
			misura=x.nextInt(76)-25;
		else
			misura=x.nextInt(101);
		return misura;
	}
	
	public static void main(String[] args) 
	{
		ConsoleInput tastiera=new ConsoleInput();
		
		//creo servizio server Echo a cui affidiamo la porta 7
		try {
			//istanzio il Server sulla porta 2000
			UDPServer echoServer=new UDPServer(2000, 'T');
			echoServer.start();
			tastiera.readLine();//non va avanti fino a che non premo il tasto invio
			echoServer.interrupt();//interrompe il tread echoServer
		} catch (SocketException e) {
			System.err.println("Impossibile creare il socket");
		} catch (IOException e) {
			System.err.println("Errore generico di input/output");
		}
	}

}
