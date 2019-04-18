package com.geekplaya.AdventureLobbies;

import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;

public class MessageVariables {
	
	private ChatColor MAGIC = ChatColor.MAGIC;
	private ChatColor RED = ChatColor.RED;
	private ChatColor BLUE = ChatColor.BLUE;
	private ChatColor GREEN = ChatColor.GREEN;
	private ChatColor WHITE = ChatColor.WHITE;
	@SuppressWarnings("unused")
	private ChatColor DARK_BLUE = ChatColor.DARK_BLUE;
	@SuppressWarnings("unused")
	private ChatColor GOLD = ChatColor.GOLD;
	private ChatColor AQUA = ChatColor.AQUA;
	private ChatColor YELLOW = ChatColor.YELLOW;
	private ChatColor GRAY = ChatColor.GRAY;
	
	/* PERMISSION */
	public Permission OWNER_PERM = new Permission("al.owner");
	public Permission DONOR_PERM = new Permission("al.donor");
	
	/* MISC */
	public String NULL = null;
	public String INDENT = "    ";
	public String DIVIDER = "-----------------------\n";
	
	/* Join */
	public String WELCOME = BLUE + "Welcome to: [LOBBY_NAME]!" + "\n" +
							DIVIDER +
							INDENT + GRAY + "Map: " + WHITE + "[MAP_NAME]" + GRAY +"; Difficulty: " + WHITE + "[DIFFICULTY]" + "\n" +
							INDENT + GRAY + "Players: [COLOR][CURRENT_PLAYERS]/[MAX_PLAYERS]" + "\n" +
							INDENT + GRAY + "Duration: " + WHITE + "[DURATION] hours" + "\n" +
							INDENT + GRAY + "Started: " + WHITE + "[START_TIME] (U.S. Eastern Time)" + "\n" +
							INDENT + GRAY + "End Time: " + WHITE + "[END_TIME] (U.S. Eastern Time)" + "\n" +
							DIVIDER +
							YELLOW + "For a full list of commands, type: " + WHITE + "/help";
							
	/* Kicking messages */
	public String PAUSE = "The server needs to be updated!" + "\n\n" +
					"But no worries, your lobby is saved and will be reloaded" + "\n" +
					"on the same exact server within 60 seconds. Please rejoin then.";
	public String NOT_STARTED = "The lobby has not started yet!" + "\n\n" +
							"Please try again in a minute.";
	public String KICKED = "You were kicked from this lobby.";
	public String NOT_ALLOWED = "You are not invited to this particular lobby.";
	public String END_LOBBY_KICK = "Thank you for using Adventure Lobbies!" + "\n\n" +
							"Your lobby has now ended.";
	public String DID_NOT_POST = "You did not post on our forum thread within 10 minutes as asked in the chat." + "\n\n" +
			"Your lobby has now ended.";
	public String NOT_ENOUGH_PLAYERS = "You must be playing with at least one other person!" + "\n\n" +
			"Your lobby has now ended.";
	public String CLEAR = END_LOBBY_KICK;
	public String BAN = "You have been banned from Adventure Lobbies due to a rule violation.";
	public String ALREADY_BANNED = "You were banned earlier from Adventure Lobbies due to a rule violation.";
	public String RESTART = "The server has been forcefully restarted due to maintenance." + "\n\n" +
			"I apologize greatly for the inconvenience! Unfortunately, the lobby could not be saved." + "\n\n" +
			"This is not a normal occurence, and it had to be done due to an error in the server.";
	
	/* Forum checking messages */
	public String FC_THANKS = GREEN + "Thank you! You may now continue playing your lobby." + "\n" +
							  YELLOW + "By donating, you will not have to do this again!";	
	public String WAIT = MAGIC + DIVIDER +
					MAGIC + DIVIDER +
					RED + "Wait!" + GRAY + " Please give just a moment of your time!" + "\n" +
					INDENT + GRAY + "Please post on the thread located at:" + "\n" +
					INDENT + INDENT + AQUA + "http://tinyurl.com/althread" + "\n" + 
					//INDENT + GRAY + "NOTE: You must include your username in the message!" + "\n" +
					INDENT + GRAY + "To prevent this from showing again, you can donate at:" + "\n" +
					INDENT + INDENT + AQUA + "http://adventurelobbies.com/donate" + "\n";
					//YELLOW + "We will be checking every 60 seconds." + "\n";
	
	/* Interact messages */
	public String SET_HOME = "Your home has been set. To teleport to your home, type: /home";
	
}
