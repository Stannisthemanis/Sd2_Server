package meeto.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;

import meeto.rmiserver.RmiServerInterface;


/**
 * The Class Server.
 */
/**
 * @author Diogo
 *
 */
public class Server {

	/** The online users. */
	public static ArrayList<Connection>	onlineUsers	= new ArrayList<Connection>();
	
	/** The data base server. */
	public static RmiServerInterface	dataBaseServer;
	
	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {

		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new RMISecurityManager());
		
		String hostname = null;
		while (true) {
			try {
				hostname = checkIfMainIsRunning();
				if (hostname == null)
					createServer();
				else
					checkIfMainServerAlive(hostname);
			} catch (IOException e) {
				System.out.println("\n*** Creating Server: " + e.getMessage());
			}
		}
		
	}
		
	/**
	 * Check if main server alive.
	 *
	 * @param hostname
	 *            the hostname
	 */
	private static void checkIfMainServerAlive(String hostname) {
		DatagramSocket dataSocket = null;
		String host = hostname;
		int serverPort = 6666;
		byte[] m = new byte[1000];
		
		try {
			InetAddress aHost = InetAddress.getByName(host);
			dataSocket = new DatagramSocket();
			dataSocket.setSoTimeout(10000);
			System.out.println("\n->> Server2: Secundary Server ok...");
			while (true) {
				DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
				System.out.println("\n->> Server2: Sending request to Main...");
				dataSocket.send(request);
				byte[] buffer = new byte[1000];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				dataSocket.receive(reply);
				System.out.println("->> Server2: Received reply from Main...");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					System.out.println("*** Server2: Sleeping..");
				}
			}
		} catch (SocketException e) {
			System.out.println("\n*** Socket on Server2: " + e.getMessage());
		} catch (EOFException e) {
			System.out.println("\n*** EOF on Server2: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("\n*** IO on Server2: " + e.getMessage());
		} finally {
			if (dataSocket != null)
				dataSocket.close();
			System.out.println("\n->> Server2: Main Server Timeout...");
			System.out.println("->> Becoming Main Server...");
			try {
				createServer();
			} catch (IOException e) {
				
			}
		}
		
	}
	
	/**
	 * Creates the server.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static void createServer() throws IOException {
		int serverPort = 6000;
		ServerSocket listenSocket = new ServerSocket(serverPort);
		System.out.println("->> Server: Main Server ok...");
		System.out.println("->> Server: Main server listening IN port: " + serverPort);
		System.out.println("->> Server: LISTEN SOCKET= " + listenSocket);
		
		// Thread para responder ao 2o servidor que este ainda esta up
		new respondToSecundary();
		connectToRmi();
		
		// Aceitar novas connecçoes de cliente e lidar com elas
		while (true) {
			Socket clientSocket = listenSocket.accept();
			System.out.println("\n->> Server: Client connected with SOCKET " + clientSocket);
			new Connection(clientSocket);
		}
	}
	
	/**
	 * Connect to rmi.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void connectToRmi() throws IOException {
		// Acesso ao servidor rmi
		String rmiHost[] = { "localhost", "Roxkax", "ricardo" };
		boolean connected = false;
		int i = 0;
		while (connected == false) {
			i = (i + 1) % 3;
			try {
				dataBaseServer = (RmiServerInterface) Naming.lookup("rmi://" + rmiHost[i] + ":1099/DataBase");
				connected = true;
			} catch (MalformedURLException e) {
				System.out.println("->> URL Server: Registing to rmiServer " + e.getMessage());
				connected = false;
			} catch (NotBoundException e) {
				System.out.println("->> BOUND Server: Registing to rmiServer " + e.getMessage());
				connected = false;
			} catch (RemoteException e) {
				System.out.println("->> REMOTE Server: Registing to rmiServer " + e.getMessage());
				connected = false;
			}
		}
		System.out.println("->> Server: Connection to RmiServer ok...");
	}
	
	/**
	 * Check if main is running.
	 *
	 * @return the string
	 */
	private static String checkIfMainIsRunning() {
		int serverPort = 6000;
		Socket test = null;
		String hostname = null;
		System.out.println("a");
		try {
			test = new Socket("localhost", serverPort);
			hostname = "localhost";
		} catch (IOException e) {
			try {
				System.out.println("b");
				test = new Socket("Roxkax", serverPort);
				hostname = "Roxkax";
			} catch (IOException e1) {
				try {
					System.out.println("c");
					test = new Socket("ricardo", serverPort);
					hostname = "ricardo";
				} catch (IOException e2) {
					hostname = null;
				}
			}
		}
		if (test != null)
			try {
				test.close();
			} catch (IOException e) {
				System.out.println("*** Closing test socket" + e.getMessage());
			}
		return hostname;
	}
}

/** Class that responds to the secundary server */
class respondToSecundary extends Thread {
	
	DatagramSocket	dataSocket;
	int				dataSocketPort;
	
	respondToSecundary() {
		dataSocketPort = 6666;
		this.start();
	}
	
	public void run() {
		try {
			dataSocket = new DatagramSocket(dataSocketPort);
			System.out.println("->> Server: Socket to Secundary ready IN port " + dataSocketPort);
			while (true) {
				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				dataSocket.receive(request);
				System.out.println("\n->> Server: Received request from Secundary...");
				DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());
				System.out.println("->> Server: Responding to Secundary...");
				dataSocket.send(reply);
			}
		} catch (SocketException e) {
			System.out.println("\n*** DatagramSocket on comunication to secundary: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("\n*** Comunication to Secundary: " + e.getMessage());
		} finally {
			if (dataSocket != null)
				dataSocket.close();
		}
	}
}

/**
 * The Class that handles connections.
 */
class Connection extends Thread {
	
	DataOutputStream	out;
	DataInputStream		in;
	Socket				clientSocket;
	String				user;
	
	Connection(Socket cSocket) {
		try {
			
			this.clientSocket = cSocket;
			this.out = new DataOutputStream(clientSocket.getOutputStream());
			this.in = new DataInputStream(clientSocket.getInputStream());
			this.user = null;
			
			Server.onlineUsers.add(this); //
			this.start();
		} catch (IOException e) {
			disconnectClient();
			System.out.println("\n*** Connection of  " + user + ": " + e.getMessage());
		}
	}
	
	public void run() {
		String read = null;
		boolean login = false;
		boolean allReadyLogged = false;
		while (user == null) {
			try {
				allReadyLogged = false;
				if (read == null) {
					read = in.readUTF();
				}
				System.out.println(read);
				if (read.split(",").length == 1) {
					out.writeBoolean(Server.dataBaseServer.userExists(read.split(",")[0]));
					read = null;
				} else if (read.split(",").length == 2) {
					login = Server.dataBaseServer.tryLogin(read.split(",")[0], read.split(",")[1]);
					for (Connection onUser : Server.onlineUsers) {
						if (onUser.user != null && onUser.user.equals(read.split(",")[0])) {
							allReadyLogged = true;
						}
					}
					if (login == true && allReadyLogged == false) {
						this.user = read.split(",")[0];
						out.writeBoolean(true);
					} else {
						out.writeBoolean(false);
					}
					read = null;
				} else if (read.split(",").length == 3) {
					this.user = Server.dataBaseServer.registerNewUser(read);
					if (this.user == null)
						out.writeBoolean(false);
					else
						out.writeBoolean(true);
					read = null;
				} else {
					out.writeBoolean(false);
					read = null;
					System.out.println("\n*** Sintax incorrect for login/register..");
				}
				
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Testing login.." + e.getMessage());
				return;
			}
		}
		System.out.println("->> Server: " + user + " connected");
		int request = 0;
		while (true) {
			try {
				if (request == 0)
					request = in.read();
				switch (request) {
					case 1:
						addNewMeeting();
						request = 0;
						break;
					case 2:
						sendListUpcumingMeetingsOfUser();
						request = 0;
						break;
					case 3:
						sendListPassedMeetingsOfUser();
						request = 0;
						break;
					case 4:
						sendMeetingResume();
						request = 0;
						break;
					case 5:
						sendListOfAgendaItensFromMeeting();
						request = 0;
						break;
					case 6:
						sendListOfMessagesOfUser();
						request = 0;
						break;
					case 7:
						replyToMessage();
						request = 0;
						break;
					case 8:
						sendNumberOfMessagesUser();
						request = 0;
						break;
					case 9:
						addNewAgendaItemToMeeting();
						request = 0;
						break;
					case 10:
						removeAgendaItemFromMeeting();
						request = 0;
						break;
					case 11:
						modifyTitleAgendaItem();
						request = 0;
						break;
					case 12:
						addKeyDecisionToAgendaItem();
						request = 0;
						break;
					case 13:
						addActionItemToMeeting();
						request = 0;
						break;
					case 14:
						sendSizeOfTodoOfUser();
						request = 0;
						break;
					case 15:
						sendListOfActionItensOfUser();
						request = 0;
						break;
					case 16:
						setActionAsDone();
						request = 0;
						break;
					case 17:
						sendListCurrentMeetingsOfUser();
						request = 0;
						break;
					case 18:
						sendListActionItensFromMeeting();
						request = 0;
						break;
					case 19:
						sendChatHistoryFromAgendaItem();
						request = 0;
						break;
					case 20:
						addMessageToAgendaItemChat();
						request = 0;
						break;
					case 21:
						testIfUserExists();
						request = 0;
						break;
					case 22:
						removeUserFromChat();
						request = 0;
						break;
					case 23:
						sendChatFromPassedMeeting();
						request = 0;
						break;
					case 24:
						inviteUserToMeeting();
						request = 0;
						break;
				}
			} catch (EOFException e) {
				System.out.println("\n*** EOF Receiving request from " + user + ": " + e.getMessage());
				return;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				try {
					Server.dataBaseServer.removeUserFromAllChats(user);
				} catch (RemoteException e1) {
					System.out.println("\n*** removing from chat");
				}
				System.out.println("\n*** IO Receiving request from " + user + ": " + e.getMessage());
				return;
			}
			
		}
	}
	
	public void addNewMeeting() {
		String newMeeting = null;
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("\n->> Server: Received request from " + this.user + " to create new meeting");
				System.out.println("->>Server: Waiting for meeting information");
				if (newMeeting == null)
					newMeeting = in.readUTF();
				System.out.println("->> Server: Information received");
				Server.dataBaseServer.addNewMeeting(newMeeting);
				out.writeBoolean(true);
				System.out.println("->> Server: New meeting created");
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Reply new meeting creating  " + user + ": " + e.getMessage());
				return;
			}
		}
	}
	
	public void sendListUpcumingMeetingsOfUser() {
		boolean sucess = false;
		System.out.println("\n->> Server: Received request to send all upcuming meeting of " + this.user);
		while (sucess == false) {
			try {
				System.out.println("->> Server: Sending all upcuming meeting of " + this.user);
				out.writeUTF(Server.dataBaseServer.getListUpcumingMeetings(user));
				System.out.println("-> sended.....");
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Replying upcuming meeting  " + user + ": " + e.getMessage());
				return;
			}
		}
	}
	
	public void sendListPassedMeetingsOfUser() {
		System.out.println("\n->> Server: Received request to send all passed meeting of " + this.user);
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("->> Server: Sending all passed meeting of " + this.user);
				out.writeUTF(Server.dataBaseServer.getListPassedMeetings(user));
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Replying passed Meeting: " + e.getMessage());
				return;
			}
		}
	}
	
	public void sendListCurrentMeetingsOfUser() {
		boolean sucess = false;
		System.out.println("\n->> Server: Received request to send all current meeting of " + this.user);
		while (sucess == false) {
			try {
				System.out.println("->> Server: Sending all current meeting of " + this.user);
				out.writeUTF(Server.dataBaseServer.getListCurrentMeetings(user));
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Replying current meeting  " + user + ": " + e.getMessage());
				return;
			}
		}
	}
	
	public void sendMeetingResume() {
		System.out.println("\n->> Server: Received request to send meeting information from " + this.user);
		int meeting = -1;
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("->> Server: Waiting for info of requested meeting  " + this.user);
				if (meeting == -1)
					meeting = in.read();
				System.out.println("->> Server: Sending meeting info to " + this.user);
				out.writeUTF(Server.dataBaseServer.getMeetingResume(meeting));
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Replying to send info of meeting to " + user + ": " + e.getMessage());
				return;
			}
		}
		
	}
	
	public void sendListOfAgendaItensFromMeeting() {
		System.out.println("\n->> Server: Received request to send agenda itens from a meeting  " + user);
		int n = -1;
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("->> Server: Wainting for info meeting...");
				if (n == -1)
					n = in.read();
				System.out.println("->> Server: Sending agenda itens of meeting.. ");
				out.writeUTF(Server.dataBaseServer.getListOfAgendaItensFromMeeting(n));
				System.out.println("->> Server Info send with sucess..");
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
	}
	
	public void sendListOfMessagesOfUser() {
		boolean sucess = false;
		System.out.println("\n->> Server: Received request to send messages of USER: " + user);
		while (sucess == false) {
			try {
				System.out.println("->> Server: Sending messages of " + user);
				out.writeUTF(Server.dataBaseServer.getListOfInvitesByUser(user));
				System.out.println("->> Server: Messages send with sucess ");
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
	}
	
	public void replyToMessage() {
		System.out.println("\n->> Server: Received request of " + user + " to respond to a message..");
		int n = -1;
		boolean reply = false;
		boolean readedReply = false;
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("->> Server: Waiting for message number..");
				if (n == -1)
					n = in.read();
				System.out.println("->> Server: Sending message resume..");
				out.writeUTF(Server.dataBaseServer.getMeetingResume(n));
				System.out.println("->> Server: Waiting for USER to decline or accept..");
				if (readedReply == false) {
					reply = in.readBoolean();
					readedReply = true;
				}
				out.writeBoolean(Server.dataBaseServer.setReplyOfInvite(n, reply));
				System.out.println("->> Server: Answer received with sucess");
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
		
	}
	
	public void sendNumberOfMessagesUser() {
		boolean sucess = false;
		System.out.println("\n->> Server: Received request to send number of messages of USER " + user);
		while (sucess == false) {
			try {
				out.write(Server.dataBaseServer.getNumberOfInvites(user));
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
	}
	
	public void addNewAgendaItemToMeeting() {
		String newItem = null;
		int n = -1;
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("\n->> Server: Received request to add a new agenda item ..");
				System.out.println("->> Server: Waiting for the info of the new agenda item ..");
				if (n == -1)
					n = in.read();
				if (newItem == null)
					newItem = in.readUTF();
				System.out.println("->> Server: Info received add agenda item now ..");
				out.writeBoolean(Server.dataBaseServer.addAgendaItemToMeeting(n, newItem));
				System.out.println("->> Server: New agenda item added with sucess ..");
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
		
	}
	
	public void removeAgendaItemFromMeeting() {
		int n = -1;
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("\n->> Server: Received request to remove aagenda item ..");
				System.out.println("->> Server: Waiting for the info of agenda item to remove..");
				if (n == -1)
					n = in.read();
				System.out.println("->> Server: Info received, removing agenda item now ..");
				out.writeBoolean(Server.dataBaseServer.removeAgendaItemFromMeeting(n));
				System.out.println("->> Server: Agenda item removed with sucess ..");
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
	}
	
	public void modifyTitleAgendaItem() {
		int n = -1;
		String newAgendaItem = null;
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("\n->> Server: Received request to modify agenda item ..");
				System.out.println("->> Server: Waiting for the info of agenda item to modify..");
				if (n == -1)
					n = in.read();
				System.out.println("->> Server: Info received Waiting for new agenda itemToDiscuss now ..");
				if (newAgendaItem == null)
					newAgendaItem = in.readUTF();
				out.writeBoolean(Server.dataBaseServer.modifyTitleAgendaItem(n, newAgendaItem));
				System.out.println("->> Server: Agenda item changed with sucess ..");
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
	}
	
	public void addKeyDecisionToAgendaItem() {
		int n = -1;
		String newKeyDecision = null;
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("\n->> Server: Received request to add key decision to agenda item ..");
				System.out.println("->> Server: Waiting for the info of agenda item to modify..");
				if (n == -1)
					n = in.read();
				System.out.println("->> Server: Info received Waiting for key decision now ..");
				if (newKeyDecision == null) {
					newKeyDecision = in.readUTF();
				}
				out.writeBoolean(Server.dataBaseServer.addKeyDecisionToAgendaItem(n, newKeyDecision));
				System.out.println("->> Server: Agenda item changed with sucess ..");
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
	}
	
	public void addActionItemToMeeting() {
		String newItem = null;
		String responsableUSer = null;
		int n = -1;
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("\n->> Server: Received request to add action item ..");
				System.out.println("->> Server: Waiting for the info of the new action item ..");
				if (n == -1)
					n = in.read();
				if (newItem == null)
					newItem = in.readUTF();
				if (responsableUSer == null)
					responsableUSer = in.readUTF();
				System.out.println("->> Server: Info received add action item now ..");
				out.writeBoolean(Server.dataBaseServer.addActionItemToMeeting(n, newItem, responsableUSer));
				System.out.println("->> Server: New action item added with sucess ..");
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
		
	}
	
	public void sendSizeOfTodoOfUser() {
		boolean sucess = false;
		System.out.println("\n->> Server: Received request to send number of action itens of USER " + user);
		while (sucess == false) {
			try {
				out.write(Server.dataBaseServer.getSizeOfTodo(user));
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
	}
	
	public void sendListOfActionItensOfUser() {
		boolean sucess = false;
		System.out.println("\n->> Server: Received request to send action of USER: " + user);
		
		while (sucess == false) {
			try {
				System.out.println("->> Server: Sending actions of " + user);
				out.writeUTF(Server.dataBaseServer.getListOfActionItensFromUser(user));
				System.out.println("->> Server: actions send with sucess ");
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
	}
	
	public void setActionAsDone() {
		System.out.println("\n->> Server: Received request of " + user + " to complete a action..");
		int n = -1;
		boolean reply = false;
		boolean readedReply = false;
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("->> Server: Waiting for action number..");
				if (n == -1)
					n = in.read();
				System.out.println("->> Server: Waiting for USER to decline or accept..");
				if (readedReply == false) {
					reply = in.readBoolean();
					readedReply = true;
				}
				if (reply) {
					out.writeBoolean(Server.dataBaseServer.setActionAsCompleted(n));
					System.out.println("->> Server: Action set as completed with sucess");
				} else {
					out.writeBoolean(false);
					System.out.println("->> Server: Operation canceled  USER");
				}
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
		
	}

	public void sendListActionItensFromMeeting() {

		System.out.println("\n->> Server: Received request to send action itens from a meeting  " + user);
		int n = -1;
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("->> Server: Wainting for info meeting...");
				if (n == -1) {
					n = in.read();
				}
				System.out.println("->> Server: Sending agenda itens of meeting.. ");
				out.writeUTF(Server.dataBaseServer.getListActionItensFromMeeting(n));
				System.out.println("->> Server Info send with sucess..");
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
	}
	
	public void sendChatHistoryFromAgendaItem() {
		int n = -1;
		boolean sucess = false;
		synchronized (Server.dataBaseServer) {
			while (sucess == false) {
				try {
					System.out.println("\n->> Server: Received request send messages from agenda item ..");
					System.out.println("->> Server: Waiting for the info of agenda item to send messages..");
					if (n == -1) {
						n = in.read();
					}
					System.out.println("->> Server: Info received sending messages now ..");
					out.writeUTF(Server.dataBaseServer.getChatHistoryFromAgendaItem(n));
					System.out.println("->> Server: Agenda item messages sended with sucess ..");
					sucess = true;
					Server.dataBaseServer.addClientToChat(n, user);
				} catch (RemoteException e) {
					try {
						Server.connectToRmi();
					} catch (IOException e1) {
						System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
					}
				} catch (IOException e) {
					disconnectClient();
					System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
					return;
				}
			}
		}
	}
			
	public void addMessageToAgendaItemChat() {
		int n = -1;
		boolean sucess = false;
		String messageReaded = null;
		synchronized (Server.dataBaseServer) {
			while (sucess == false) {
				ArrayList<Connection> clientsOnChat = new ArrayList<Connection>();
				try {
					System.out.println("\n->> Server: Received request add messages to agenda item ..");
					System.out.println("->> Server: Waiting for the info of meeting to add message..");
					if (n == -1) {
						n = in.read();
					}
					System.out.println("->> Server: Info of agenda item received waiting for message now ..");
					if (messageReaded == null) {
						messageReaded = in.readUTF();
					}
					System.out.println("->> Server: Message received, adding message now..");
					if (Server.dataBaseServer.addMessageToChat(n, user, messageReaded.concat("\n"))) {
						for (Connection userOn : Server.onlineUsers) {
							if (Server.dataBaseServer.testIfUserIsOnChat(n, userOn.user)) {
								clientsOnChat.add(userOn);
							}
						}
						for (Connection outs : clientsOnChat) {
							System.out.println("->> Server: Broadcasting message to " + outs.user);
							outs.out.writeUTF(messageReaded.concat("\n"));
						}
					} else
						System.out.println("->> Server: Message not send with sucess ..");
					sucess = true;
				} catch (RemoteException e) {
					try {
						Server.connectToRmi();
					} catch (IOException e1) {
						System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
					}
				} catch (IOException e) {
					disconnectClient();
					System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
					return;
				}
			}
		}
	}
	
	public void removeUserFromChat() {
		boolean sucess = false;
		while (sucess == false) {
			try {
				out.writeUTF("");
				System.out.println("\n->> Server: leaving chat ..");
				Server.dataBaseServer.removeUserFromAllChats(user);
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
		
	}
	
	public void sendChatFromPassedMeeting() {
		int n = -1;
		boolean sucess = false;
		while (sucess == false) {
			try {
				System.out.println("\n->> Server: Received request send history of messages from agenda item ..");
				System.out.println("->> Server: Waiting for the info of agenda item to send messages..");
				if (n == -1)
					n = in.read();
				System.out.println("->> Server: Info received,sending messages now ..");
				out.writeUTF(Server.dataBaseServer.getChatHistoryFromAgendaItem(n));
				sucess = true;
				System.out.println("->> Server: Agenda item messages sended with sucess ..");
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
		}
	}
	
	public void inviteUserToMeeting() {
		boolean sucess = false;
		int n = -1;
		String invitedUser = null;
		while (sucess == false) {
			try {
				if (n == -1)
					n = in.read();
				if (invitedUser == null)
					invitedUser = in.readUTF();
				out.writeBoolean(Server.dataBaseServer.inviteUserToMeeting(invitedUser,n));
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
			
		}
	}
	
	public void testIfUserExists() {
		String name = null;
		boolean sucess = false;
		while (sucess == false) {
			try {
				if (name == null)
					name = in.readUTF();
				out.writeBoolean(Server.dataBaseServer.userExists(name));
				sucess = true;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			} catch (IOException e) {
				disconnectClient();
				System.out.println("\n*** Receiving meeting number for agenda item... " + e.getMessage());
				return;
			}
			
		}
		
	}
	
	public void disconnectClient() {
		Server.onlineUsers.remove(this);
		while (true) {
			try {
				Server.dataBaseServer.removeUserFromAllChats(this.user);
				return;
			} catch (RemoteException e) {
				try {
					Server.connectToRmi();
				} catch (IOException e1) {
					System.out.println("*** Reconnecting to rmiServer" + e1.getMessage());
				}
			}
		}
	}
}

