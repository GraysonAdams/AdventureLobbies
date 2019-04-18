package com.geekplaya.AdventureLobbies;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import org.apache.commons.dbutils.DbUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.geekplaya.AdventureLobbies.Objects.ALPlayer;

public class PlayerListener implements Listener {

	/**
	 * The most annoying thing in the world...
	 */	
	private AdventureLobbies server;
	private MessageVariables msg;
	private ServiceHandler service;
	private Server bukkit = Bukkit.getServer();
	private SQLHandlerForums sqlForums = AdventureLobbies.sqlForums;
	public void fetchClasses() {
		server = AdventureLobbies.plugin;
		msg = AdventureLobbies.msg;
		service = AdventureLobbies.service;
	}

	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		boolean cancel = true;
		Player s = e.getPlayer();
		ALPlayer alPlayer = new ALPlayer(s.getName());
		String[] args = e.getMessage().split(" ");
		String label = new String(args[0]);
		label = label.replace("/", "");
		
		args = Arrays.copyOfRange(args, 1, args.length);

		if(label.equals("tp")) {
			if(args.length == 1 && isPlayer(args[0])) {
				s.teleport(getPlayer(args[0]).getLocation());
			} else if(args.length == 2 && isPlayer(args[0]) && isPlayer(args[1])) {
				getPlayer(args[0]).teleport(getPlayer(args[1]).getLocation());
			} else {
				error(s, "tp <player> [player]");
			}
		} else if(label.equals("give")) {
			if(args.length == 2 && isNumeric(args[0]) && isNumeric(args[1]) && Material.getMaterial(Integer.parseInt(args[0])) != null) {
				int amount = Integer.parseInt(args[1]);
				ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(args[0])), amount);
				s.getInventory().addItem(item);
				s.sendMessage("Giving you " + amount + " of: " + Integer.parseInt(args[0]));
			} else if(args.length == 3 && isPlayer(args[0]) && isNumeric(args[1]) && isNumeric(args[2]) && Material.getMaterial(Integer.parseInt(args[1])) != null) {
				int amount = Integer.parseInt(args[2]);
				ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(args[1])), amount);
				getPlayer(args[0]).getInventory().addItem(item);
				s.sendMessage("Giving " + getPlayer(args[0]).getName() + " " + amount + " of: " + Integer.parseInt(args[1]));
			} else {
				error(s, "give [player] <data-value> <amount>");
			}
		} else if(label.equals("gamemode")) {
			if(args.length == 1 && isNumeric(args[0]) && getInt(args[0]) >= 0 && getInt(args[0]) <= 2) {
				GameMode[] gamemodes = {GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE};
				s.setGameMode(gamemodes[getInt(args[0])]);
				s.sendMessage("Gamemode is now: " + getInt(args[0]));
			} else if(args.length == 2 && isPlayer(args[0]) && isNumeric(args[1]) && getInt(args[1]) >= 0 && getInt(args[1]) <= 2) {
				GameMode[] gamemodes = {GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE};
				s.setGameMode(gamemodes[getInt(args[1])]);
				s.sendMessage(getPlayer(args[0]).getDisplayName() + "'s gamemode is now: " + getInt(args[1]));
				getPlayer(args[0]).sendMessage("Gamemode is now: " + args[1].toLowerCase());
			} else if(args.length == 2 && isPlayer(args[0]) && (args[1].equalsIgnoreCase("creative") || args[1].equalsIgnoreCase("survival") || args[1].equalsIgnoreCase("adventure"))) {
				if(args[1].equalsIgnoreCase("creative"))
					getPlayer(args[0]).setGameMode(GameMode.CREATIVE);
				else if(args[1].equalsIgnoreCase("survival"))
					getPlayer(args[0]).setGameMode(GameMode.SURVIVAL);
				else if(args[1].equalsIgnoreCase("adventure"))
					getPlayer(args[0]).setGameMode(GameMode.ADVENTURE);		
				getPlayer(args[0]).sendMessage("Gamemode is now: " + args[1].toLowerCase());
			} else {
				error(s, "gamemode [player] <0-2>");
			}
		} else if(label.equals("weather")) {
			if(args.length == 1 && args[0].equals("clear")) {
				s.getWorld().setStorm(false);
				s.getWorld().setThundering(false);
				s.sendMessage("Weather set to " + args[0]);
			} else if(args.length == 1 && args[0].equals("rain")) {
				s.getWorld().setStorm(true);
				s.getWorld().setThundering(false);
				s.sendMessage("Weather set to " + args[0]);
			} else if(args.length == 1 && args[0].equals("storm")) {
				s.getWorld().setStorm(true);
				s.getWorld().setThundering(true);
				s.sendMessage("Weather set to " + args[0]);
			} else {
				error(s, "weather <clear/rain/storm>");
			}
		} else if(label.equals("time")) {
			if(args.length == 1 && isNumeric(args[0]) && getInt(args[0]) <= 24000 && getInt(args[0]) >= 0) {
				s.getWorld().setTime(getInt(args[0]));
				s.sendMessage("World time set to: " + getInt(args[0]));
			} else {
				error(s, "time <0-24000>");
			}
		} else if(label.equals("kick")) {
			if(args.length == 1 && isPlayer(args[0])) {
				getPlayer(args[0]).kickPlayer(msg.KICKED);
				s.sendMessage("The player has been kicked.");
			} else {
				error(s, "kick <player>");
			}
		} else if(label.equals("ban") && alPlayer.isOwner()) {
			if(args.length == 1 && isPlayer(args[0])) {
				new ALPlayer(getPlayer(args[0]).getName()).ban(true);
				s.sendMessage("The player has been banned.");
			} else if(args.length == 2 && isPlayer(args[0]) && isBoolean(args[1])) {
				new ALPlayer(getPlayer(args[0]).getName()).ban(getBoolean(args[1]));
				s.sendMessage("The player has been (un)banned.");
			} else {
				error(s, "ban <player> [true/false]");
			}
		} else if(label.equals("difficulty") && (alPlayer.isSubscriber() || alPlayer.isOwner())) {
			if(args.length == 1 && isNumeric(args[0]) && getInt(args[0]) >= 0 && getInt(args[0]) <= 4) {
				server.lobby.setDifficulty(getInt(args[0]));
				s.sendMessage("Difficulty set to: " + getInt(args[0]) + "/4");
			} else {
				error(s, "difficulty <0-4>");
			}
		} else {
			cancel = false;
		}
		e.setCancelled(cancel);
	
	}
	
	public void error(Player p, String usage) {
		p.sendMessage("Syntax error! Type /" + usage);
	}
	
	@EventHandler
	public void onPlayerJoin (PlayerJoinEvent event) {
		event.setJoinMessage(msg.NULL);
		if(event.getPlayer() != null) {
			Player player = event.getPlayer();
			ALPlayer alPlayer = new ALPlayer(player.getName());			
			if(player.isBanned()) // Banned
				player.kickPlayer(msg.ALREADY_BANNED);
			if(server.lobby != null) {
				if(alPlayer.isAllowed() && server.lobby.isStarted()) {
//					player.setAllowFlight(true);
					if(alPlayer.isOwner()) { // Owner
						player.setOp(true);
						player.setDisplayName(ChatColor.BLUE + player.getName() + ChatColor.WHITE);
						player.addAttachment(server).setPermission("al.owner", true);
						player.addAttachment(server).setPermission("al.gold", true);
						if(player.getName().equals("GeekPlaya")) {
							event.setJoinMessage(ChatColor.RED + "GeekPlaya (Owner of Adventure Lobbies) has connected.");
						} else if(player.getName().equals("NextGenGuru")) {
							player.setDisplayName(ChatColor.BLUE + "Squirtle" + ChatColor.WHITE);
							event.setJoinMessage(ChatColor.RED + player.getName() + " (Adventure Lobbies Moderator) has connected.");
						} else {
							event.setJoinMessage(ChatColor.RED + player.getName() + " (Adventure Lobbies Moderator) has connected.");
						}
					} else {
						event.setJoinMessage(player.getDisplayName() + " connected.");
						if(alPlayer.isSubscriber()) { // Subscriber
							if(service.schedulers.get("forumCheck") != null)
								bukkit.getScheduler().cancelTask(service.schedulers.get("forumCheck"));
							int uid = -1;
							sqlForums.connect();
							ResultSet rs = null;
							Statement st = null;
							try {
								st = server.sqlConnectionForums.createStatement();
								rs = st.executeQuery("SELECT * FROM GDN_User WHERE MinecraftAccount = '" + player.getName() + "'");
							    while (rs.next()) {
							    	uid = rs.getInt("UserID");
							    }
							} catch(Exception e) {
								e.printStackTrace();
							} finally {
							    DbUtils.closeQuietly(rs);
							    DbUtils.closeQuietly(st);
							    DbUtils.closeQuietly(server.sqlConnectionForums);
							}
							if(sqlForums.numRows("SELECT * FROM GDN_UserRole WHERE UserID = '" + uid + "' AND RoleID = '" + sqlForums.getGoldID() + "'") > 0) {
								player.addAttachment(server).setPermission("al.gold", true);
								player.addAttachment(server).setPermission("al.donor", true);
								player.setDisplayName(ChatColor.GOLD + player.getName() + ChatColor.WHITE);
								int duration = (int) ((server.lobby.getEndTime() - server.lobby.getStartTime())/60/60);
								if(duration < 6) {
									server.lobby.setEndTime(server.lobby.getStartTime() + 60*60*6);
									event.setJoinMessage(player.getDisplayName() + " connected." + "\n" +
											ChatColor.YELLOW + "A Gold Subscriber has joined! Lobby runtime has been extended four hours.");
								}
							} else if(sqlForums.numRows("SELECT * FROM GDN_UserRole WHERE UserID = '" + uid + "' AND RoleID = '" + sqlForums.getSilverID() + "'") > 0) {
								player.addAttachment(server).setPermission("al.silver", true);
								player.addAttachment(server).setPermission("al.donor", true);
								player.setDisplayName(ChatColor.GRAY + player.getName() + ChatColor.WHITE);
								int duration = (int) ((server.lobby.getEndTime() - server.lobby.getStartTime())/60/60);
								if(duration < 4) {
									server.lobby.setEndTime(server.lobby.getStartTime() + 60*60*4);
									event.setJoinMessage(player.getDisplayName() + " connected." + "\n" +
											ChatColor.YELLOW + "A Silver Subscriber has joined! Lobby runtime has been extended two hours.");
								}
							} else if(sqlForums.numRows("SELECT * FROM GDN_UserRole WHERE UserID = '" + uid + "' AND (RoleID = '" + sqlForums.getBronzeID() + "' OR RoleID = '" + sqlForums.getDonorID() + "')") > 0) {
								player.addAttachment(server).setPermission("al.bronze", true);
								player.addAttachment(server).setPermission("al.donor", true);
								player.setDisplayName(ChatColor.DARK_RED + player.getName() + ChatColor.WHITE);
								int duration = (int) ((server.lobby.getEndTime() - server.lobby.getStartTime())/60/60);
								if(duration < 3) {
									server.lobby.setEndTime(server.lobby.getStartTime() + 60*60*3);
									event.setJoinMessage(player.getDisplayName() + " connected." + "\n" +
											ChatColor.YELLOW + "A Bronze Subscriber has joined! Lobby runtime has been extended an hour.");
								}
							}
						}
						
					}
					if(server.lobby.getOnlinePlayerCount() >= 2 || alPlayer.isOwner()) {
						if(service.schedulers.get("onePlayer") != null) {
							bukkit.getScheduler().cancelTask(service.schedulers.get("onePlayer"));
							service.schedulers.remove("onePlayer");
						}
					}
					player.setGameMode(GameMode.SURVIVAL);
					alPlayer.welcome();
				} else {
					player.kickPlayer(msg.NOT_ALLOWED);
				}
			} else {
				player.kickPlayer(msg.NOT_STARTED);
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit (PlayerQuitEvent event) {
		if(server.lobby != null) {
			if(server.lobby.getOnlinePlayerCount() < 2) {
				if(service.schedulers.get("onePlayer") == null) {
					service.schedulers.put("onePlayer", service.onePlayer());
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerKick (PlayerKickEvent event) {
		event.setReason(ChatColor.YELLOW + "" + ChatColor.BOLD + "Attention " + event.getPlayer().getName() + ":" + ChatColor.RESET + "\n--------\n\n" + ChatColor.ITALIC + event.getReason());
		event.setLeaveMessage(msg.NULL);
	}
	
	
	@EventHandler
	public void onPlayerPortal (PlayerPortalEvent event) {
		//PlayerPortal.onPlayerPortal(event);
	}
	
	@EventHandler
	public void onPlayerMove (PlayerMoveEvent event) {
		Player player = event.getPlayer();
		World currentWorld = event.getPlayer().getWorld();
		if(server.lobby.getFrozen())
			player.teleport(player.getLocation());
		if(currentWorld == bukkit.getWorld("world"))
			event.getPlayer().teleport(server.lobby.getMap().getOverworld().getSpawnLocation());
	}
	
	@EventHandler
	public void onPlayerRespawn (PlayerRespawnEvent event) {
		if(event.getPlayer().getBedSpawnLocation() != null)
			event.setRespawnLocation(event.getPlayer().getBedSpawnLocation());
		else
			event.setRespawnLocation(server.lobby.getMap().getOverworld().getSpawnLocation());
	}
	
	@EventHandler
	public void onPlayerChangedWorld (PlayerChangedWorldEvent event) {
		if(event.getPlayer().getWorld() == bukkit.getWorld("world"))
			event.getPlayer().teleport(server.lobby.getMap().getOverworld().getSpawnLocation());
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(event.getClickedBlock() != null) {
			if(event.getClickedBlock().getTypeId() == 26 && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				player.setBedSpawnLocation(player.getLocation());
				player.sendMessage(msg.SET_HOME);
			}
		}
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
	
}
