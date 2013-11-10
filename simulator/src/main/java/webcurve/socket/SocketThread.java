package webcurve.socket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author dennis_d_chen@yahoo.com
 */
public class SocketThread extends Thread implements Runnable {
	static Logger  log = LoggerFactory.getLogger(SocketThread.class);

	protected SocketHandler handler;
	protected Socket socket;
	
	public SocketThread(Socket socket, SocketHandler handler)
	{
		this.handler = handler;
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try
		{
			while ( true )
			{
				if (!socket.isConnected())
				{
					log.info("socket disconnected");
					break;
				}
				if ( !handler.handleSocket(socket))
				{
					log.info("server released client socket");
					socket.close();
					break;
				}

			}
		}
		catch (Exception ex)
		{
			log.warn(ex.toString());
		}

	}

}
