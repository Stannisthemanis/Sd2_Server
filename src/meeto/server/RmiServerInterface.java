package meeto.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

// TODO: Auto-generated Javadoc
/**
 * Created by Diogo on 16/10/2014.
 */
/**
 * @author Diogo
 *
 */
public interface RmiServerInterface extends Remote {
	
	/**
	 * User exists.
	 *
	 * @return true, if successful
	 * @throws RemoteException
	 *             the remote exception
	 */
	public boolean userExists(String username) throws RemoteException;
	
	/**
	 * Try login.
	 *
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @return true, if successful
	 * @throws RemoteException
	 *             the remote exception
	 */
	public boolean tryLogin(String username, String password) throws RemoteException;
	
	/**
	 * Register new user.
	 *
	 * @param newUserInformation
	 *            the new user information
	 * @return username of new user
	 * @throws RemoteException
	 *             the remote exception
	 */
	public String registerNewUser(String newUserInformation) throws RemoteException;
	
	/**
	 * Removes the user from all chats.
	 *
	 * @param user
	 *            the user
	 * @throws RemoteException
	 *             the remote exception
	 */
	public void removeUserFromAllChats(String user) throws RemoteException;
	
	/**
	 * Adds the new meeting.
	 *
	 * @param newMeetingInformation
	 *            the new meeting information
	 * @throws RemoteException
	 *             the remote exception
	 */
	public void addNewMeeting(String newMeetingInformation) throws RemoteException;
	
	/**
	 * Gets the list of upcuming meetings by user.
	 *
	 * @param user
	 *            the user that requested the list
	 * @return the list of upcuming meetings by user
	 * @throws RemoteException
	 *             the remote exception
	 */
	public String getListUpcumingMeetings(String user) throws RemoteException;
	
	/**
	 * Gets the list of passed meetings by user.
	 *
	 * @param user
	 *            the user
	 * @return the list passed meetings by user
	 * @throws RemoteException
	 *             the remote exception
	 */
	public String getListPassedMeetings(String user) throws RemoteException;
	
	/**
	 * Gets the list current meetings by user.
	 *
	 * @param user
	 *            the user
	 * @return the list current meetings by user
	 * @throws RemoteException
	 *             the remote exception
	 */
	public String getListCurrentMeetings(String user) throws RemoteException;
	
	/**
	 * Gets the meeting resume.
	 *
	 * @param flag
	 *            1:FutureMeeting 2:PassedMeeting 3:CurrentMeeting
	 * @param nrMeeting
	 *            number of the meeting
	 * @param user
	 *            username
	 * @return String- resume of the meeting
	 * @throws RemoteException
	 *             the remote exception
	 */
	public String getMeetingResume(int flag, int nrMeeting, String user) throws RemoteException;
	
	/**
	 * Gets the list of agenda itens from meeting.
	 *
	 * @param flag
	 *            1:FutureMeeting 2:PassedMeeting 3:CurrentMeeting
	 * @param nrMeeting
	 *            number of the meeting
	 * @param user
	 *            username
	 * @return the list of agenda itens from meeting
	 * @throws RemoteException
	 *             the remote exception
	 */
	public String getListOfAgendaItensFromMeeting(int flag, int nrMeeting, String user) throws RemoteException;
	
	/**
	 * Gets the list of messages by user.
	 *
	 * @param user username
	 * @return the list of messages by user
	 * @throws RemoteException the remote exception
	 */
	public String getListOfMessagesUser(String user) throws RemoteException;
	
	/**
	 * Gets the resume of message.+
	 *
	 * @param user the user
	 * @param nrMessage the number of message
	 * @return the resume of message
	 * @throws RemoteException the remote exception
	 */
	public String getResumeOfMessage(String user, int nrMessage) throws RemoteException;
	
	/**
	 * Sets the reply of invite.
	 *
	 * @param user the user
	 * @param nrMessage the message number 
	 * @param reply the reply
	 * @throws RemoteException the remote exception
	 */
	public boolean setReplyOfInvite(String user, int nrMessage, boolean reply) throws RemoteException;
	
	/**
	 * Gets the number of messages.
	 *
	 * @param user the user
	 * @return the number of messages
	 * @throws RemoteException the remote exception
	 */
	public int getNumberOfMessages(String user) throws RemoteException;
	
	/**
	 * Adds the agenda item to meeting.
	 *
	 * @param nrMeeting the nr meeting
	 * @param newItem the new item
	 * @param user the user
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean addAgendaItemToMeeting(int nrMeeting,String newItem,String user) throws RemoteException;

	/**
	 * Removes the agenda item from meeting.
	 *
	 * @param n the n
	 * @param nrAgendaItem the nr agenda item
	 * @param user the user
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean removeAgendaItemFromMeeting(int n, int nrAgendaItem, String user) throws RemoteException;

	/**
	 * Modify title agenda item.
	 *
	 * @param n the n
	 * @param nrAgendaItem the nr agenda item
	 * @param newAgendaItem the new agenda item
	 * @param user the user
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean modifyTitleAgendaItem(int n, int nrAgendaItem, String newAgendaItem, String user) throws RemoteException;

	/**
	 * Adds the key decision to agenda item.
	 *
	 * @param n the n
	 * @param nrAgendaItem the nr agenda item
	 * @param newKeyDecision the new key decision
	 * @param user the user
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean addKeyDecisionToAgendaItem(int n, int nrAgendaItem, String newKeyDecision, String user) throws RemoteException;

	/**
	 * Adds the action item to meeting.
	 *
	 * @param n the n
	 * @param newItem the new item
	 * @param user the user
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean addActionItemToMeeting(int n, String newItem, String user) throws RemoteException;

	/**
	 * @param user
	 * @return
	 * @throws RemoteException
	 */
	public int getSizeOfTodo(String user) throws RemoteException;

	/**
	 * Gets the list of action itens from user.
	 *
	 * @param user the user
	 * @return the list of action itens from user
	 * @throws RemoteException the remote exception
	 */
	public String getListOfActionItensFromUser(String user) throws RemoteException;

	/**
	 * Sets the action as completed.
	 *
	 * @param user the user
	 * @param n the n
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean setActionAsCompleted(String user, int n) throws RemoteException;

	/**
	 * Gets the list action itens from meeting.
	 *
	 * @param n the n
	 * @param user the user
	 * @param flag the flag
	 * @return the list action itens from meeting
	 * @throws RemoteException the remote exception
	 */
	public String getListActionItensFromMeeting(int n, String user, int flag) throws RemoteException;

	/**
	 * Gets the chat history from agenda item.
	 *
	 * @param n the n
	 * @param numAgendaItem the num agenda item
	 * @param user the user
	 * @return the chat history from agenda item
	 * @throws RemoteException the remote exception
	 */
	public String getChatHistoryFromAgendaItem(int n, int numAgendaItem, String user) throws RemoteException;

	/**
	 * @param n
	 * @param numAgendaItem
	 * @param user
	 * @throws RemoteException
	 */
	public void addClientToChat(int n, int numAgendaItem, String user) throws RemoteException;

	/**
	 * Test if user is on chat.
	 *
	 * @param n the n
	 * @param numAgendaItem the num agenda item
	 * @param user the user
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean testIfUserIsOnChat(int n, int numAgendaItem, String user) throws RemoteException;

	/**
	 * Adds the message to chat.
	 *
	 * @param n the n
	 * @param numAgendaItem the num agenda item
	 * @param user the user
	 * @param concat the concat
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean addMessageToChat(int n, int numAgendaItem, String user, String concat)throws RemoteException;

	/**
	 * Gets the messages from passed meeting.
	 *
	 * @param n the n
	 * @param numAgendaItem the num agenda item
	 * @param user the user
	 * @return the messages from passed meeting
	 * @throws RemoteException the remote exception
	 */
	public String getMessagesFromPassedMeeting(int n, int numAgendaItem, String user) throws RemoteException;

	/**
	 * Invite user to meeting.
	 *
	 * @param n the n
	 * @param invitedUser the invited user
	 * @param user the user
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean inviteUserToMeeting(int n, String invitedUser, String user) throws RemoteException;
	
}
