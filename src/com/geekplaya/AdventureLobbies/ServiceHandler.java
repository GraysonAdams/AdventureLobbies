package com.geekplaya.AdventureLobbies;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import com.geekplaya.AdventureLobbies.Objects.Lobby;

public class ServiceHandler {
	
	public HashMap<String, Integer> schedulers = new HashMap<String, Integer>();
	
	/**
	 * The most annoying thing in the world...
	 */	
	private AdventureLobbies server;
	private MessageVariables msg;
	private SQLHandler sql;
	private Server bukkit = Bukkit.getServer();
	private ServiceHandler service;
	private boolean stop = false;
	public void fetchClasses() {
		server = AdventureLobbies.plugin;
		msg = AdventureLobbies.msg;
		sql = AdventureLobbies.sql;
		service = AdventureLobbies.service;
	}
	
	public void innitiate() {
		boolean lobbyCheckHalt = false;
		
		sql.connect();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = server.sqlConnection.createStatement();
			rs = st.executeQuery("SELECT * FROM servers WHERE ip = '" + bukkit.getIp() + ":" + bukkit.getPort() + "'");
			while (rs.next()) {
				server.serverId = rs.getInt("id");
				server.log.info("Established server link. Server identification code: " + server.serverId);
				break;
			}
		} catch (SQLException e) {
			server.log.info("Unable to link server (" + bukkit.getIp() + ":" + bukkit.getPort() + ") to SQL database! Halting server.");
			stop = true;
		} finally {
		    DbUtils.closeQuietly(rs);
		    DbUtils.closeQuietly(st);
		    DbUtils.closeQuietly(server.sqlConnection);
		}
				
		if(!stop) {
			sql.update("UPDATE servers SET runningmap = '" + 0 + "' WHERE id = '" + server.serverId + "'");
			sql.update("UPDATE servers SET players = '" + 0 + "' WHERE id = '" + server.serverId + "'");
			sql.update("UPDATE lobbies SET running = '" + 0 + "', ran = '" + 1 + "' WHERE running = '" + 1 + "' AND server = '" + server.serverId + "'");
			
			declareOnline();
			schedulers.put("lobbyCheck", startLobbyChecking());
		}
		
	}

	private int getLatestLobby() {
		int result = -1;
		sql.connect();
		ResultSet rs = null;
		Statement st = null;
		try {
			st = server.sqlConnection.createStatement();
			String sql = "SELECT * FROM lobbies WHERE running = '0' AND ran = '0' AND prepared = '0' AND server = '" + server.serverId + "' ORDER BY time DESC LIMIT 30";
			rs = st.executeQuery(sql);
			while(rs.next()) {
				result = rs.getInt("id");
				if(rs.getString("lobby") == null) result = -1;
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    DbUtils.closeQuietly(rs);
		    DbUtils.closeQuietly(st);
		    DbUtils.closeQuietly(server.sqlConnection);
		}
		return result;
	}
	
	private int getPreparedLobby() {
		int result = -1;
		sql.connect();
		ResultSet rs = null;
		Statement st = null;
		try {
			st = server.sqlConnection.createStatement();
			String sql = "SELECT * FROM lobbies WHERE ran = '0' AND prepared = '1' AND server = '" + server.serverId + "' ORDER BY time DESC LIMIT 30";
			rs = st.executeQuery(sql);
			while(rs.next())
				result = rs.getInt("id");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    DbUtils.closeQuietly(rs);
		    DbUtils.closeQuietly(st);
		    DbUtils.closeQuietly(server.sqlConnection);
		}
		return result;
	}
	
	private int startLobbyChecking() {
		server.log.info("Checking for new lobbies...");
		if(Bukkit.getWorld("amap") != null && getPreparedLobby() > -1) {
        	System.out.println("Lobby ready!");
        	server.lobby = new Lobby(getPreparedLobby());
    		server.lobby.start();
		} else if(Bukkit.getWorld("amap") != null) {
			service.changeDefaultWorld();
			bukkit.dispatchCommand(bukkit.getConsoleSender(), "stop");
		} else {	
			int taskId = bukkit.getScheduler().scheduleSyncRepeatingTask(server, new Runnable() {
			    @Override  
			    public void run() {
			        if(getLatestLobby() > 0) {
			        	System.out.println("Lobby found! Preparing...");
			        	server.lobby = new Lobby(getLatestLobby());
						service.changeDefaultWorld();
						sql.update("UPDATE lobbies SET prepared = '" + 1 + "' WHERE id = '" + server.lobby.getID() + "'");
						server.lobby.prepare();
			        	server.lobby = null;
						bukkit.dispatchCommand(bukkit.getConsoleSender(), "stop");
			        }
			    }
			}, 100L, 100L);
			return taskId;
		}
		return -1;
	}
	
	public int prepareFreeze() {
		schedulers.put("postMessage", bukkit.getScheduler().scheduleSyncDelayedTask(server, new Runnable() {
		    @Override  
		    public void run() {
				if(!server.lobby.containsSubscriber()) {
					//server.lobby.setFrozen(true);
					bukkit.broadcastMessage(msg.WAIT);
				}
		    }
		}, 20L * 598L)); 
		/*bukkit.getScheduler().scheduleSyncDelayedTask(server, new Runnable() {
		    @Override  
		    public void run() {
				if(server.lobby.getFrozen()) {
					server.lobby.stop(msg.DID_NOT_POST);
				}
		    }
		}, 20L * 935L);
		int websiteCheck = bukkit.getScheduler().scheduleSyncRepeatingTask(server, new Runnable() {
		    @Override  
		    public void run() {
		    	if(!server.lobby.containsSubscriber()) {
			    	if(server.lobby.getCurrentPlayers() != null) {
			    		try {
					    	String contents = FileUtils.readFileToString(new File("../forum.txt"));
					    	Player[] players = server.lobby.getCurrentPlayers();
					        for(Player player : players) {
					        	if(contents.toLowerCase().contains(player.getName().toLowerCase())) {
					        		bukkit.broadcastMessage(msg.FC_THANKS);
					        		server.lobby.setFrozen(false);
					        		break;
					        	}
					        	server.log.info("No post detected on forums!");
					        }
			    		} catch (Exception e) {
			    			e.printStackTrace();
			    		}
			    	}
		    	}
		    }
		}, 20L * 600L, 20L * 30L);*/ 
		return -1;
	}
	
	public int scheduleEnd() {
		int taskId = bukkit.getScheduler().scheduleSyncRepeatingTask(server, new Runnable() {
		    @Override  
		    public void run() {
		    	if(server.lobby != null)
		    		if(server.getTime() > server.lobby.getEndTime())
		    			server.lobby.stop();
		    }
		},  20L * (server.lobby.getEndTime() - server.lobby.getStartTime()), 60L * 20L);
		return taskId;
	}
	
	private int declareOnline() {
    	sql.update("UPDATE servers SET started = '" + server.getTime() + "' WHERE id = '" + server.serverId + "'");
		int taskId = bukkit.getScheduler().scheduleSyncRepeatingTask(server, new Runnable() {
		    @Override  
		    public void run() {
		    	sql.update("UPDATE servers SET latest = '" + server.getTime() + "' WHERE id = '" + server.serverId + "'");
		    	if(server.lobby != null)
		    		sql.update("UPDATE servers SET players = '" + server.lobby.getOnlinePlayerCount() + "' WHERE id = '" + server.serverId + "'");
		    }
		}, 0L, 15L * 20L);
		return taskId;
	}	
	
	public int terminateCheck() {
		int taskId = bukkit.getScheduler().scheduleSyncRepeatingTask(server, new Runnable() {
		    @Override  
		    public void run() {
		    	if(server.lobby != null) {
			    	if(server.lobby.shouldTerminate()) {
			    		sql.update("UPDATE lobbies SET terminate = " + 0 + " WHERE id = " + server.lobby.getID() + "");
			    		server.lobby.clear(msg.RESTART);
			    		server.lobby.stop();
			    	}
		    	}
		    }
		}, 0L, 5L * 20L);
		return taskId;
	}
	
	public int onePlayer() {
		int taskId = bukkit.getScheduler().scheduleSyncDelayedTask(server, new Runnable() {
		    @Override  
		    public void run() {
		    	if(server.lobby != null) {
			    	if(server.lobby.getOnlinePlayerCount() < 2)
			    		server.lobby.stop(msg.NOT_ENOUGH_PLAYERS);
		    	}
		    }
		}, 300L * 20L);
		return taskId;
	}
	
	public void changeDefaultWorld() {
		try {
			FileInputStream inputStream = new FileInputStream("server.properties");
	        String propContent = IOUtils.toString(inputStream);
			
			if(Bukkit.getWorld("amap") != null)
				propContent = propContent.replace("level-name=amap", "level-name=world");
			else
				propContent = propContent.replace("level-name=world", "level-name=amap");
						
			FileOutputStream outputStream = new FileOutputStream(new File("server.properties"), false);
			outputStream.write(propContent.getBytes());
			outputStream.close();
			
			inputStream.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
}
