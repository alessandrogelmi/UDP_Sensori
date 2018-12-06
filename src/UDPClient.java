import java.awt.SecondaryLoop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.spi.TimeZoneNameProvider;

public class UDPClient 
{
	//ATTRIBUTI, SOCKET di classe DatagramSocket
	private DatagramSocket socket;
	
	//COSTRUTTORE
	public UDPClient() throws SocketException
	{
		//creo il socket
		socket=new DatagramSocket();
		//creo timeout per quando aspetta risposta del server
		socket.setSoTimeout(1000);
	}
	
	//ALTRI METODI
	public void closeSocket()
	{
		socket.close();
	}
	
	public String sendAndReceive(String host, int port, char messaggio)throws UnsupportedEncodingException, UnknownHostException, IOException
	//Host=indirizzo ip serversottoformadi stringa
	//Messaggio=mesaggio inviato dal client
	//port=POrta delserver sul quale inviare il messaggio(7)
	{
		//buffer di richiesta, 8KB
		byte[] bufferRequest=new byte[2]; 
		byte[] bufferAnswers=new byte[8];
		char tipoMisura = 0;
		String risultato;
		//TextFile file=new TextFile("sensori.txt",'W');
		
		//Istanzio reference del datagramma di richiesta
		DatagramPacket request;
		DatagramPacket answers;
		
		//Istanzio oggetto di tipo inetAddress, eccezione se non conosce host,gestita nel main
		InetAddress address=InetAddress.getByName(host);
		
		//metodo che restituisce un'arrayy di byte, ogni carattere=byte utilizzando la codifica ISO
		ByteBuffer data=ByteBuffer.allocate(8);
		data.clear();
		data.putChar(messaggio);
		data.flip();
		
		request=new DatagramPacket(data.array(), data.limit(), address, port);
		answers=new DatagramPacket(bufferAnswers, bufferAnswers.length);
		socket.send(request);//Invio il datagramma request
		socket.receive(answers);//aspetto risposta dal server
		
		int valoreMisura = 0;
		if(answers.getAddress().getHostAddress().compareTo(host)==0 && answers.getPort()==port)
		{
			data.clear();
			data.put(answers.getData());
			data.flip();
			
			tipoMisura=data.getChar();
			valoreMisura=data.getInt();
		}
		
		Misura misura=new Misura(tipoMisura, valoreMisura);
		
		risultato=LocalDateTime.now().toString()+"/"+answers.getAddress().getHostAddress()+"/"+misura.toString();
		
		closeSocket();
		return risultato;
	}
	//RUN
	
	public static void main(String[] args) 
	{
		String host="127.0.0.1";
		int port=2000;
		char message='?';
		String risultato;
		TextFile file = null;
		try 
		{
			file = new TextFile("sensori.txt",'W');
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
				
		try 
		{
			UDPClient echoClient=new UDPClient();
			risultato=echoClient.sendAndReceive(host, port, message);
			System.out.println(risultato.toString());
			
			try {
				file.toFile(risultato);
				file.closeFile();
			} 
			catch (EccezioneFile e) 
			{
				System.err.println("Impossibile trovare il file");
			}
			
		} 
		catch (SocketTimeoutException e){
			System.err.println("Il server non risponde");
		}
		catch (SocketException e) {
			System.err.println("Impossibile creare il socket");
		}
		catch (UnsupportedEncodingException e) {
			System.err.println("Codifica non supportata");
		}
		catch (UnknownHostException e) {
			System.err.println("Host non conosciuto");
		}
		catch (IOException e) {
			System.err.println("Errore generico di I/O");
		}

	}

}
