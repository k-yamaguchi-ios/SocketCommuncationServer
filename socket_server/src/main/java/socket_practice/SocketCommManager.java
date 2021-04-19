package socket_practice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Hello world!
 *
 */
public class SocketCommManager {

    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT_NUMBER = 8765;
    private static final String ADDRESS = "address";
    private static final String USER_NAME = "username";
    private static final String PASS_WORD = "password";
	private static final int WAIT = 100;
	private static final Object EXIT = "exit";

    private boolean isAccepted = false;
	private boolean isIntterupted = false;
	private boolean isReceived = false;
	private String queryResult = null; 

	BufferedReader reader = null;
	PrintWriter writer = null;
	ServerSocket sSocket = null;
	Socket socket = null;

    private QueryExecutor executor;

	class AcceptThread extends Thread{
		public void run(){
			acceptSocket();
		}
	};

	private void acceptSocket() {
		try {
		
			System.out.println("クライアントからの接続待ち状態");
			socket = null;
			
			//クライアントからの要求を待ち続けます
			while ( socket == null ) {
				try {
					socket = sSocket.accept();
					socket.setReuseAddress(true);
				} catch ( SocketTimeoutException e ) {
				}
				if ( isIntterupted ) {
					System.out.println("クライアントからの接続待ちを中断");
					return;
				}
			}

			System.out.println("クライアントからの接続を確認");
			isAccepted = true;

		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	Thread sHook = new Thread() {
		public void run() {
			System.out.println("ユーザー割り込みにより終了します");
			isIntterupted = true;
			if ( socket != null ) {
				try{
					socket.close();
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(3000);
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	};

	private void serverMain() {
		Runtime.getRuntime().addShutdownHook(sHook);

        executor = new QueryExecutor();
        executor.initializeExecutor(ADDRESS, USER_NAME, PASS_WORD);
		
		//IPアドレスとポート番号を指定してサーバー側のソケットを作成

		while(!isIntterupted) {
			try{
				sSocket = new ServerSocket();
				sSocket.bind(new InetSocketAddress
						(IP_ADDRESS,PORT_NUMBER));
				sSocket.setSoTimeout(500);
				sSocket.setReuseAddress(true);
				AcceptThread acceptThread =  new AcceptThread();
				acceptThread.start();

				while(true) {
					if(isAccepted){
						break;
					} else if ( isIntterupted ) {
						acceptThread.join();
						break;
					}
					System.out.println("waiting accept...");
					Thread.sleep( WAIT );
				}
				
				if ( !isIntterupted ) {
					//クライアントからの受取用
					reader = new BufferedReader(
							new InputStreamReader
							(socket.getInputStream()));
					
					//サーバーからクライアントへの送信用
					writer = new PrintWriter(
							socket.getOutputStream(), true);
					
					//無限ループ　byeの入力でループを抜ける
					String line = null;
					while ( !isIntterupted ) {
						
						System.out.println("クライアントからの入力待ち状態");
						line = reader.readLine();
						System.out.println(line);
						
						if ( EXIT.equals( line ) ) {
							break;
						} else if ( line != null ) {
							isReceived = false;
							executor.exeQuery(line, callback);
							while( true ) {
								if ( isReceived ) {
									writer.println( queryResult );
									break;
								} else if ( isIntterupted ) {
									break;
								} else {
									try {
										Thread.sleep( WAIT );
									} catch ( InterruptedException e ) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				}			
			}catch( IOException e ) {
				if ( isIntterupted ) {
					System.out.println("クライアントからの入力待ちを中断します");
				} else {
					e.printStackTrace();
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					if (reader!=null){
						reader.close();
					}
					if (writer!=null){
						writer.close();
					}
					if (socket!=null){
						socket.close();
					}
					if (sSocket!=null){
						sSocket.close();
					}
					isAccepted = false;
					System.out.println("サーバー側終了です");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }

    private QueryExeCallback callback = new QueryExeCallback(){
        @Override
        public void onReceive( String result ) {
			queryResult = result;
			isReceived = true;
        }
    };
    public static void main( String[] args )
    {
        SocketCommManager scm = new SocketCommManager();
        scm.serverMain();
        System.out.println( "Hello World!" );
    }
}
