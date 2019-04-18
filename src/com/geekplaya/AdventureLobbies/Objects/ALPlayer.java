package com.geekplaya.AdventureLobbies.Objects;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.dbutils.DbUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.geekplaya.AdventureLobbies.AdventureLobbies;
import com.geekplaya.AdventureLobbies.MessageVariables;
import com.geekplaya.AdventureLobbies.SQLHandler;
import com.geekplaya.AdventureLobbies.SQLHandlerForums;

public class ALPlayer {
	
	private AdventureLobbies server = AdventureLobbies.plugin;
	private MessageVariables msg = AdventureLobbies.msg;
	private SQLHandler sql = AdventureLobbies.sql;
	private SQLHandlerForums sqlForums = AdventureLobbies.sqlForums;
	private Server bukkit = AdventureLobbies.bukkit;
	private Location home;
	
	private Player player;
	
	public ALPlayer(String p) {
		this.player = Bukkit.getPlayer(p);
	}
	
	public String toString() {
		return this.player.getName();
	}
	
	
	public Location getHome() {
		if(home == null)
			home = server.lobby.getMap().getOverworld().getSpawnLocation();
		return home;
	}
	
	public void setHome(Location l) {
		home = l;
	}
	
	public void resetHome(Location l) {
		home = server.lobby.getMap().getOverworld().getSpawnLocation();
	}
	
	public boolean isSubscriber() {		
		int uid = -1;
		if(player.hasPermission("al.bronze") || player.hasPermission("al.silver") || player.hasPermission("al.gold")) {
			return true;
		} else {
			sqlForums.connect();
			ResultSet rs = null;
			Statement st = null;
			try {
				st = server.sqlConnectionForums.createStatement();
				rs = st.executeQuery("SELECT * FROM GDN_User WHERE MinecraftAccount = '" + player.getName() + "'");			
			    while (rs.next()) { uid = rs.getInt("UserID"); }
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
			    DbUtils.closeQuietly(rs);
			    DbUtils.closeQuietly(st);
			    DbUtils.closeQuietly(server.sqlConnectionForums);
			}
			if(sqlForums.numRows("SELECT * FROM GDN_UserRole WHERE UserID = '" + uid + "' AND (RoleID = '" + sqlForums.getBronzeID() + "' OR RoleID = '" + sqlForums.getSilverID() + "' OR RoleID = '" + sqlForums.getGoldID() + "' OR RoleID = '" + sqlForums.getDonorID() + "')") > 0) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isBanned() {
		if(sql.numRows("SELECT * FROM banned_players WHERE player = '" + player.getName() + "'") > 0) {
			return true;
		}
		try {
			if(sqlForums.numRows("SELECT * FROM GDN_UserRole WHERE MinecraftAccount = '" + player.getName() + "' AND Banned = 1") > 0) {
				return true;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isOwner() {
		if(player.hasPermission("al.owner") || player.isOp()) {
			return true;
		} else if(sql.numRows("SELECT * FROM owners WHERE username = '" + player.getName() + "'") > 0) {
			return true;
		}
		return false;
	}
	
	public void ban(boolean shouldBan) {
		sql.connect();
		PreparedStatement ps = null;
		try {
			String query = "DELETE FROM banned_players WHERE player = ? ";
			if(shouldBan) {
				player.kickPlayer("You have been perma-banned from Adventure Lobbies.");
				bukkit.broadcastMessage(player.getName() + " has been permanently banned from Adventure Lobbies.");
				query = "INSERT INTO banned_players (player)" + " values (?)";
			}
			ps = server.sqlConnection.prepareStatement(query);
			ps.setString(1, player.getName());
			ps.execute();
		} catch (SQLException e) {
			server.log.info("Could not ban player: " + player);
			e.printStackTrace();
		} finally {
		    DbUtils.closeQuietly(ps);
		    DbUtils.closeQuietly(server.sqlConnection);
		}
	}
	
	public boolean isAllowed() {
		if(server.lobby != null && server.lobby.getPlayers() != null) {
			Iterator <String> playerIterator = server.lobby.getPlayers().iterator();
			while(playerIterator.hasNext()){
				if((playerIterator.next().equalsIgnoreCase(player.getName()) || isOwner())) {
					if(server.lobby.isStarted())
						return true;
				}
			}
		}
		return false;
	}
	
	public void welcome() {     
		String[] difficulties = {"Peaceful", "Easy", "Normal", "Hard"};
		String difficulty = difficulties[server.lobby.getMap().getOverworld().getDifficulty().getValue()];
		
		int duration = (int) ((server.lobby.getEndTime() - server.lobby.getStartTime())/60/60);
		ChatColor durationColor = ChatColor.WHITE;
		if(duration > 2) durationColor = ChatColor.GOLD;
		
		Date startTimeDate = new Date();
		startTimeDate.setTime(server.lobby.getStartTime()*1000L);
		String startTime = new SimpleDateFormat("HH:mm:ss").format(startTimeDate);

		Date endTimeDate = new Date();
		endTimeDate.setTime(server.lobby.getEndTime()*1000L);
		String endTime = new SimpleDateFormat("HH:mm:ss").format(endTimeDate);
		
		ChatColor playerColor = ChatColor.WHITE;
		if(server.lobby.getOnlinePlayerCount() < 2) playerColor = ChatColor.RED;
		
		String welcomeMessage = msg.WELCOME;
		welcomeMessage = welcomeMessage.replace("[LOBBY_NAME]", server.lobby.getName());
		welcomeMessage = welcomeMessage.replace("[MAP_NAME]", server.lobby.getMap().getName());
		welcomeMessage = welcomeMessage.replace("[DIFFICULTY]", difficulty);
		welcomeMessage = welcomeMessage.replace("[CURRENT_PLAYERS]", server.lobby.getOnlinePlayerCount() + "");
		welcomeMessage = welcomeMessage.replace("[MAX_PLAYERS]", server.lobby.getMaxPlayers() + "");
		welcomeMessage = welcomeMessage.replace("[DURATION]", durationColor + "" + duration + ChatColor.WHITE);
		welcomeMessage = welcomeMessage.replace("[COLOR]" , playerColor + "");
		welcomeMessage = welcomeMessage.replace("[START_TIME]", startTime);
		welcomeMessage = welcomeMessage.replace("[END_TIME]", durationColor + "" + endTime + ChatColor.WHITE);
		
		player.sendMessage(welcomeMessage);
	}

}
