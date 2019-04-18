package com.geekplaya.AdventureLobbies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.geekplaya.AdventureLobbies.Objects.ALPlayer;

public class CommandListener implements Listener {
	
	/**
	 * The most annoying thing in the world...
	 */	
	private AdventureLobbies server;
	public void fetchClasses() {
		server = AdventureLobbies.plugin;
	}
	public LinkedHashMap<String, String> normalCommands = new LinkedHashMap<String, String>();
	public LinkedHashMap<String, String> subscriberCommands = new LinkedHashMap<String, String>();
	public LinkedHashMap<String, String> ownerCommands = new LinkedHashMap<String, String>();
	public static boolean addedClasses = false;
	
	public void establishCommands() {
		normalCommands.put("info", "Displays the information about the lobby.");
		normalCommands.put("end", "Ends the currently running lobby.");
		normalCommands.put("list", "List online players.");
		normalCommands.put("help", "Lists all usable commands.");
		normalCommands.put("tp <player [player]", "Teleports one player to another player.");
		normalCommands.put("give [player] <data-value> <amount>", "Gives specified item to targeted player.");
		normalCommands.put("gamemode [player] <0-2>", "Changes targeted player's gamemode.");
		normalCommands.put("time <0-24000>", "Sets the world time.");
		normalCommands.put("kick <player>", "Kicks targetted player from server.");
		normalCommands.put("spawn [player]", "Teleports targetted player to the spawn.");
		normalCommands.put("home", "Teleports targetted player to their home.");
		normalCommands.put("tppos [player] <x> <y> <z>", "Teleports targetted player to specified coordiantes.");
		normalCommands.put("setspawn", "Sets the spawn of map.");
		subscriberCommands.put("difficulty <0-4>", "(Donor) Changes the map difficulty.");
		subscriberCommands.put("heal [player]", "(Donor) Heals the targetted player.");
		subscriberCommands.put("invite [player]", "(Donor) Invites a new player.");
		subscriberCommands.put("uninvite [player]", "(Donor) Uninvites and kicks a player.");
		//ownerCommands.put("reset", "(Owner) Reloads the map.");
		ownerCommands.put("unfreeze", "(Owner) Unfreezes the lobby.");
		addedClasses = true;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(sender instanceof Player) {
			Player s = (Player) sender;
			ALPlayer alPlayer = new ALPlayer(s.getName());
			if(label.equals("help")) {
				if(!addedClasses)
					establishCommands();
				int page = 1;
				int listLimit = 7;

				HashMap<String, String> commands = new HashMap<String, String>(normalCommands);
				if(alPlayer.isSubscriber())
					commands.putAll(subscriberCommands);
				if(alPlayer.isOwner())
					commands.putAll(ownerCommands);
				List<String> keys = new ArrayList<String>(commands.keySet());
				List<String> values = new ArrayList<String>(commands.values());
				
				int pageLimit = 0;
				if((commands.size()/listLimit) % 1 == 0) {
					pageLimit = (int)(commands.size()/listLimit)+1;
				} else {
					pageLimit = (int)commands.size()/listLimit;
				}
				
				if(args.length == 1 && isNumeric(args[0]) && (getInt(args[0]) > pageLimit || getInt(args[0]) == 0))
					page = 1;
				else if(args.length == 1 && isNumeric(args[0]) && getInt(args[0]) <= pageLimit)
					page = getInt(args[0]);
				
				if((args.length == 1 && isNumeric(args[0])) || args.length == 0) {
					s.sendMessage(ChatColor.GREEN + "For more information about a command, type /help <command>");
					s.sendMessage(ChatColor.BLUE + "------ Page: " + ChatColor.WHITE + page + "/" + pageLimit + ChatColor.BLUE + " ------");
					
					String message = "";
					
					for(int i = (page-1)*listLimit; i <= ((page-1)*listLimit) + listLimit && i < commands.size(); i++) {
						message += ChatColor.WHITE + "  /" + keys.get(i).split(" ")[0] + " - " + ChatColor.GRAY + values.get(i) + "\n";
					}
					s.sendMessage(message);
				} else if(args.length == 1 && !isNumeric(args[0]) && containsStartsWith(keys, args[0])) {
					String commandLabel = args[0].toLowerCase();
					s.sendMessage(ChatColor.GREEN + "For the full list of commands, type /help <page>");
					s.sendMessage("  Command: /" + commandLabel);
					s.sendMessage("  Description: " + values.get(keys.indexOf(getContainsStartsWith(keys, commandLabel))));
					s.sendMessage("  Usage: /" + getContainsStartsWith(keys, commandLabel));
				} else {
					return false;
				}
			} else if(label.equals("tppos")) {
				if(args.length == 3 && isNumeric(args[0]) && isNumeric(args[1]) && isNumeric(args[2])) {
					int x = Integer.parseInt(args[0]); int y = Integer.parseInt(args[1]); int z = Integer.parseInt(args[2]);
					Location location = new Location(s.getWorld(), x, y, z);
					s.teleport(location);
					s.sendMessage("Teleported to {" + x + "," + y + "," + z +"}");
				} else if(args.length == 4 && isPlayer(args[0]) && isNumeric(args[1]) && isNumeric(args[2]) && isNumeric(args[3])) {
					int x = Integer.parseInt(args[1]); int y = Integer.parseInt(args[2]); int z = Integer.parseInt(args[3]);
					Location location = new Location(s.getWorld(), x, y, z);
					getPlayer(args[0]).teleport(location);
					getPlayer(args[0]).sendMessage("Teleported to {" + x + "," + y + "," + z +"} by " + s.getName());
				} else {
					return false;
				}
			} else if(label.equals("heal")) {
				if(args.length == 0) {
					s.setHealth(s.getMaxHealth());
					s.setFoodLevel(20);
					s.setFireTicks(0);
					s.sendMessage("You have been healed!");
					
				} else if(args.length == 1 && isPlayer(args[0])) {
					getPlayer(args[0]).setHealth(s.getMaxHealth());
					getPlayer(args[0]).setFoodLevel(20);
					getPlayer(args[0]).setFireTicks(0);
					s.sendMessage("You have healed " + getPlayer(args[0]).getName());
					getPlayer(args[0]).sendMessage("You have been healed by " + s.getName() + "!");
				} else {
					return false;
				}
			} else if(label.equals("setspawn")) {
				server.lobby.setSpawn(s.getWorld(), (int) s.getLocation().getX(), (int) s.getLocation().getY(), (int) s.getLocation().getZ());
				s.sendMessage("Spawn set to your current location.");
			} else if(label.equals("end") || label.equals("quit")) {
				if(args.length ==  0) {
					server.lobby.stop(s.getName() + " has terminated your lobby by using '/end' command.");
				} else {
					return false;
				}
			} else if(label.equals("unfreeze")) {
				server.lobby.setFrozen(false);
				s.sendMessage("All players are now (un)frozen.");
			} else if(label.equals("info")) {
				if(args.length == 0) {
					alPlayer.welcome();
				} else {
					return false;
				}
			} else if(label.equals("home")) {
				if(s.getBedSpawnLocation() != null)
					s.teleport(s.getBedSpawnLocation());
				else
					s.teleport(server.lobby.getMap().getOverworld().getSpawnLocation());
			} else if(label.equals("gamemode")) {
				if(args.length == 1 && isNumeric(args[0]) && getInt(args[0]) >= 0 && getInt(args[0]) <= 2) {
					GameMode[] gamemodes = {GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE};
					s.setGameMode(gamemodes[getInt(args[0])]);
					s.sendMessage("Gamemode is now: " + getInt(args[0]));
				} else {
					return false;
				}
			} else if(label.equals("invite")) {
				if(args.length == 1) {
					if(!server.lobby.addPlayer(args[0])) {
						s.sendMessage("There was an error adding the player!");
						s.sendMessage(" - Have you reached the maximum player limit (6)?");
						s.sendMessage(" - Is the account verified?");
						s.sendMessage(" - Did you capitalize the name correctly?");
					} else {
						s.sendMessage(args[0] + " has been invited to your lobby.");
					}
				} else {
					return false;
				}
			} else if((label.equals("uninvite") || label.equals("remove"))) {
				if(args.length == 1 && server.lobby.getPlayers().contains(args[0].toLowerCase()) && !s.getName().toLowerCase().equals(args[0].toLowerCase())) {
					server.lobby.removePlayer(args[0].toLowerCase());
					s.sendMessage(args[0] + " has been removed from your lobby.");
				} else {
					s.sendMessage("Error removing player!");
				}
			} /*else if((label.equals("reset"))) {
				server.lobby.clear("Resetting map, please rejoin in 60 seconds.");
				server.lobby.setStarted(false);
				server.lobby.getMap().unloadWorlds(true);
				server.lobby.getMap().copyWorld();
				server.lobby.getMap().loadWorlds(server.lobby.getSeed());
				server.lobby.setStarted(true);
				server.lobby.setStartTime(server.getTime());
				if(server.lobby.containsSubscriber())
					server.lobby.setEndTime(server.getTime()+60*60*5);
				else
					server.lobby.setEndTime(server.getTime()+60*60*4);
			}*/ else if(label.equals("spawn")) {
				if(args.length == 0) {
					s.teleport(server.lobby.getMap().getOverworld().getSpawnLocation()); /* Fix world reference */
				} else if(args.length == 1 && isPlayer(args[0])) {
					getPlayer(args[0]).teleport(server.lobby.getMap().getOverworld().getSpawnLocation()); /* Fix world reference */
				} else {
					return false;
				}
			} else if(label.equals("list")) {
				if(args.length == 0) {
					s.sendMessage("There are " + server.lobby.getOnlinePlayerCount() + "/" + server.lobby.getPlayers().size() + " players online:");
					ArrayList<String> players = new ArrayList<String>();
					for(Player p : server.lobby.getCurrentPlayers())
						players.add(p.getDisplayName());
					s.sendMessage(StringUtils.join(players.toArray(), ", "));
				} else {
					return false;
				}
			}
		} else {
			if(label.equals("spawn")) {
				if(args.length == 1 && isPlayer(args[0])) {
					getPlayer(args[0]).teleport(server.lobby.getMap().getOverworld().getSpawnLocation()); /* Fix world reference */
				}
			}
		}
		return true;
	}
	
	private static boolean containsStartsWith(List<String> keys, String s) {
		for(Object object : keys)
			if(object.toString().toLowerCase().startsWith(s.toLowerCase()))
				return true;
		return false;
	}
	
	private static String getContainsStartsWith(List<String> keys, String s) {
		for(Object object : keys)
			if(object.toString().toLowerCase().startsWith(s.toLowerCase()))
				return object.toString();
		return null;
	}
	
	public static boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(\\.\\d+)?");
	}
	
	public boolean isPlayer(String player) {
		if(Bukkit.getServer().getPlayer(player) != null)
			return true;
		else
			return false;
	}
	
	public boolean isBoolean(String statement) {
		if(statement.toLowerCase().contains("true") || statement.toLowerCase().contains("false")) {
			return true;
		} else {
			return false;
		}
	}
	
	public int getInt(String num) {
		return Integer.parseInt(num);
	}
	
	public Player getPlayer(String player) {
		return Bukkit.getServer().getPlayer(player);
	}
	
	public boolean getBoolean(String statement) {
		if(statement.toLowerCase() == "true") {
			return true;
		} else {
			return false;
		}
	}
	
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
		
}
