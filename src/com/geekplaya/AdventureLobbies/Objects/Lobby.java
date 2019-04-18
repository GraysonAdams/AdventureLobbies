package com.geekplaya.AdventureLobbies.Objects;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import com.geekplaya.AdventureLobbies.AdventureLobbies;
import com.geekplaya.AdventureLobbies.MessageVariables;
import com.geekplaya.AdventureLobbies.SQLHandler;
import com.geekplaya.AdventureLobbies.ServiceHandler;

public class Lobby {
	
	private AdventureLobbies server = AdventureLobbies.plugin;
	private ServiceHandler service = AdventureLobbies.service;
	private MessageVariables msg = AdventureLobbies.msg;
	private SQLHandler sql = AdventureLobbies.sql;
	private Server bukkit = AdventureLobbies.bukkit;
	
	/** Basic information for the lobby */
	private String LOBBY_NAME;
	private Date LOBBY_DATE;
	private int LOBBY_ID;
	/** Crucial lobby details */
	private long START_TIME;
	private long END_TIME;
	/** Map-related details */
	private Map LOBBY_MAP;
	private long LOBBY_SEED;
	/** Player-related details */
	private List<String> LOBBY_PLAYERS;
	private String CREATOR_IP;
	/** Server-related details */
	private int SERVER_ID;
	/** Game settings */
	private int DIFFICULTY;
	private boolean MONSTERS;
	private boolean ANIMALS;
	private boolean FLIGHT;
	private int[] COORDINATES = new int[3];
	/** Subject to change */
	private boolean FROZEN = false;
	private boolean STARTED = false;
	private boolean PAUSED = false;
	
	private boolean containedSubscriber = false;

	public Lobby(int lobbyID) {
		if(service.schedulers.get("lobbyCheck") != null)
			bukkit.getScheduler().cancelTask(service.schedulers.get("lobbyCheck"));
		sql.update("UPDATE servers SET runningmap = '" + 1 + "' WHERE id = '" + server.serverId + "'");
		sql.connect();
		ResultSet rs = null;
		Statement st = null;
		try {
			st = server.sqlConnection.createStatement();
			String sqlQuery = "SELECT * FROM lobbies WHERE id = '" + lobbyID + "' ORDER BY time ASC LIMIT 1";
			rs = st.executeQuery(sqlQuery);
			while(rs.next()) {
				LOBBY_NAME = rs.getString("lobby");
		    	LOBBY_DATE = new Date((long)rs.getInt("time")*1000);
		    	LOBBY_ID = rs.getInt("id");
		    	LOBBY_SEED = rs.getLong("seed");
		    	PAUSED = rs.getBoolean("pause");
		    	LOBBY_MAP = new Map(rs.getString("map"), LOBBY_SEED , PAUSED);
		    	LOBBY_PLAYERS = Arrays.asList(rs.getString("players").split(","));
		    	CREATOR_IP = rs.getString("ip");
		    	SERVER_ID = rs.getInt("server");
		    	if(PAUSED)
		    		sql.update("UPDATE lobbies SET pause = '" + 0 + "' WHERE id = '" + LOBBY_ID + "'");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    DbUtils.closeQuietly(rs);
		    DbUtils.closeQuietly(st);
		    DbUtils.closeQuietly(server.sqlConnection);
		}
		
		server.log.info("Lobby name: " + LOBBY_NAME + "(" + LOBBY_ID + ")");
		server.log.info("Map: " + LOBBY_MAP.getName() + " (" + LOBBY_SEED + ")");
		server.log.info("Allowed players: " + LOBBY_PLAYERS);
		server.log.info("Resuming paused lobby: " + PAUSED);
	}
	
	public void toggleMapSettings(String mapName) {		
		sql.connect();
		ResultSet rs = null;
		Statement st = null;
		try {
			st = server.sqlConnection.createStatement();
			String sqlQuery = "SELECT * FROM maps WHERE map = '" + mapName + "'";
			rs = st.executeQuery(sqlQuery);
			if(rs.next()) {
				setAnimals(rs.getBoolean("animals"));
		    	setDifficulty(rs.getInt("difficulty"));
		    	setMonsters(rs.getBoolean("monsters"));
		    	if(!(rs.getInt("x") == 0 && rs.getInt("y") == 0 && rs.getInt("y") == 0))
		    		setSpawn(getMap().getOverworld(), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
			}
		} catch (SQLException e) {
			server.log.info("There was an error fetching map data from server!");
			e.printStackTrace();
		} finally {
		    DbUtils.closeQuietly(rs);
		    DbUtils.closeQuietly(st);
		    DbUtils.closeQuietly(server.sqlConnection);
		}
	}
	
	/* Setter Methods */
		public void setName(String name) {
			sql.update("UPDATE lobbies SET lobby = '" + name + "' WHERE id = '" + LOBBY_ID + "'");
			LOBBY_NAME = name;
		}
		
		public void setDate(Date date) {
			sql.update("UPDATE lobbies SET time = '" + date.getTime() + "' WHERE id = '" + LOBBY_ID + "'");
			LOBBY_DATE = date;
		}
		
		public void setSeed(long seed) {
			sql.update("UPDATE lobbies SET seed = '" + seed + "' WHERE id = '" + LOBBY_ID + "'");
			LOBBY_SEED = seed;
		}
		
		public void setPlayers(List<String> players) {
			StringBuilder sb = new StringBuilder();
			for (String player : players) { 
			    if (sb.length() > 0) sb.append(',');
			    sb.append(player);
			}
			sql.update("UPDATE lobbies SET players = '" + sb.toString() + "' WHERE id = '" + LOBBY_ID + "'");
			LOBBY_PLAYERS = players;
		}
		
		public void setPlayers(String players) {
			sql.update("UPDATE lobbies SET players = '" + players + "' WHERE id = '" + LOBBY_ID + "'");
			LOBBY_PLAYERS = Arrays.asList(players.replace(" ", "").split(","));
		}
		
		public void setSpawn(World w, Integer... coordinates) {
			w.setSpawnLocation(coordinates[0], coordinates[1], coordinates[2]);
			for(int i = 0; i < 3; i++)
				COORDINATES[i] = coordinates[i];
		}
		
		public void setDifficulty(int difficulty) {
			Difficulty d = null;
			if(difficulty == 0) {
				d = Difficulty.PEACEFUL;
			} else if(difficulty == 1) {
				d = Difficulty.EASY;
			} else if(difficulty == 2) {
				d = Difficulty.NORMAL;
			} else if(difficulty == 3) {
				d = Difficulty.HARD;
			}
			for(World w: Bukkit.getServer().getWorlds())
				w.setDifficulty(d);
			if(difficulty > 0 && MONSTERS)
				getMap().getOverworld().setMonsterSpawnLimit(getMap().getMonsterLimitOriginal());
			else
				getMap().getOverworld().setMonsterSpawnLimit(0);
			DIFFICULTY = difficulty;
		}
		
		public void setFlight(boolean flight) {
			Player[] players = getCurrentPlayers();
			for(Player player : players)
				player.setAllowFlight(flight);
			FLIGHT = flight;
		}
		
		public void setMonsters(boolean monsters) {
			if(monsters)
				for(World w: Bukkit.getServer().getWorlds())
					w.setMonsterSpawnLimit(getMap().getMonsterLimitOriginal());
			else
				for(World w: Bukkit.getServer().getWorlds())
					w.setMonsterSpawnLimit(0);
			MONSTERS = monsters;
		}
		
		public void setAnimals(boolean animals) {
			if(animals)
				for(World w: Bukkit.getServer().getWorlds())
					w.setAnimalSpawnLimit(getMap().getAnimalLimitOriginal());
			else
				for(World w: Bukkit.getServer().getWorlds())
					w.setAnimalSpawnLimit(0);
			ANIMALS = animals;
		}
		
		public void setStartTime(long time) {
			sql.update("UPDATE lobbies SET starttime = '" + time + "' WHERE id = '" + LOBBY_ID + "'");
			START_TIME = time;
		}	
		
		public void setEndTime(long time) {
			sql.update("UPDATE lobbies SET endtime = '" + time + "' WHERE id = '" + LOBBY_ID + "'");
			END_TIME = time;
		}
		
		public void setFrozen(boolean frozen) {
			if(frozen == false)
				if(service.schedulers.get("forumCheck") != null)
					bukkit.getScheduler().cancelTask(service.schedulers.get("forumCheck"));
				if(service.schedulers.get("postMessage") != null)
						bukkit.getScheduler().cancelTask(service.schedulers.get("postMessage"));
			FROZEN = frozen;
		}
		
		public void setStarted(boolean started) {
			STARTED = started;
		}
	
	/* Miscellaneous Methods */		
		public void prepare() {
			if(getMap().getName().contains("Generated World")) {
				new WorldCreator("amap").environment(Environment.NORMAL).seed(LOBBY_SEED).createWorld();
				new WorldCreator("amap_nether").environment(Environment.NETHER).seed(LOBBY_SEED).createWorld();
				new WorldCreator("amap_the_end").environment(Environment.THE_END).seed(LOBBY_SEED).createWorld();
			} else {
				getMap().downloadWorld();
				getMap().copyWorld();
			}
			sql.update("UPDATE lobbies SET prepared = '" + 1 + "' WHERE id = '" + LOBBY_ID + "'");
		}
		
		public void start() {
	    	toggleMapSettings(LOBBY_MAP.getName());
			/** Set Values */
			sql.update("UPDATE lobbies SET running = '" + 1 + "' WHERE id = '" + LOBBY_ID + "'");
			sql.update("UPDATE lobbies SET ready = '" + 1 + "' WHERE id = '" + LOBBY_ID + "'");
			setStartTime(server.getTime());
			setEndTime(server.getTime()+60*60*3);
			service.schedulers.put("scheduleEnd", service.scheduleEnd());	
			if(!PAUSED)
				service.schedulers.put("forumCheck", service.prepareFreeze());
			service.schedulers.put("onePlayer", service.onePlayer());
			service.schedulers.put("terminateCheck", service.terminateCheck());
			setStarted(true);
		}
		
		public void stop(String m) {
			clear(m);
			stop();
		}
		
		public void stop() {
			setStarted(false);
			clear(msg.END_LOBBY_KICK);
					
			/** Set Values */
			sql.update("UPDATE lobbies SET prepared = '" + 0 + "' WHERE id = '" + LOBBY_ID + "'");
			sql.update("UPDATE lobbies SET running = '" + 0 + "' WHERE id = '" + LOBBY_ID + "'");
			sql.update("UPDATE lobbies SET ran = '" + 1 + "' WHERE id = '" + LOBBY_ID + "'");
			sql.update("UPDATE lobbies SET ready = '" + 0 + "' WHERE id = '" + LOBBY_ID + "'");
			setEndTime(server.getTime());
			
			new File("ops.txt").delete();
			sql.update("UPDATE servers SET runningmap = '" + 0 + "' WHERE id = '" + server.serverId + "'");

			service.changeDefaultWorld();

			bukkit.dispatchCommand(bukkit.getConsoleSender(), "stop");
		}
		
		public void pause() {
			setStarted(false);
			clear(msg.PAUSE);
			
			getMap().deleteWorlds(false); // Removes world/data and world/players
			
			/** Set Values */
			sql.update("UPDATE lobbies SET running = '" + 0 + "' WHERE id = '" + LOBBY_ID + "'");
			sql.update("UPDATE lobbies SET ran = '" + 0 + "' WHERE id = '" + LOBBY_ID + "'");
			sql.update("UPDATE lobbies SET pause = '" + 1 + "' WHERE id = '" + LOBBY_ID + "'");
			sql.update("UPDATE lobbies SET prepared = '" + 1 + "' WHERE id = '" + LOBBY_ID + "'");
			sql.update("UPDATE lobbies SET ready = '" + 0 + "' WHERE id = '" + LOBBY_ID + "'");
			
			new File("ops.txt").delete();
			sql.update("UPDATE servers SET runningmap = '" + 0 + "' WHERE id = '" + server.serverId + "'");

			bukkit.dispatchCommand(bukkit.getConsoleSender(), "stop");
		}
		
		public void clear() {
			Player[] players = getCurrentPlayers();
			for(Player player : players) {
				player.kickPlayer(msg.CLEAR);
			}
		}
		
		public void clear(String m) {
			Player[] players = getCurrentPlayers();
			for(Player player : players) {
				player.kickPlayer(m);
			}
		}
		
	/* Getter Methods */	
		public String getName() {
			return LOBBY_NAME;
		}
		
		public Date getDate() {
			return LOBBY_DATE;
		}
		
		public int getID() {
			return LOBBY_ID;
		}
		
		public Map getMap() {
			return LOBBY_MAP;
		}
		
		public long getSeed() {
			return LOBBY_SEED;
		}
		
		public List<String> getPlayers() {
			return LOBBY_PLAYERS;
		}
		
		public int getMaxPlayers() {
			return getPlayers().size();
		}
		
		public int getOnlinePlayerCount() {
			int count = 0;
			Player[] players = getCurrentPlayers();
			for(int i = 0; i < players.length; i++)
				count++;
			return count;
		}
		
		public Player[] getCurrentPlayers() {
			return Bukkit.getServer().getOnlinePlayers();
		}
		
		public boolean containsSubscriber() {
			if(!containedSubscriber) {
				Player[] players = getCurrentPlayers();
				boolean containsSubscriber = false;
				for(int i = 0; i < players.length; i++) {
					ALPlayer p = new ALPlayer(players[i].getName());
					if(p.isSubscriber()) {
						containsSubscriber = true;
						break;
					}
				}
				containedSubscriber = true;
				return containsSubscriber;
			} else {
				return true;
			}
		}
		
		public String getCreatorIP() {
			return CREATOR_IP;
		}
		
		public boolean getFrozen() {
			return FROZEN;
		}
		
		public boolean getPaused() {
			return PAUSED;
		}
		
		public int getServerID() {
			return SERVER_ID;
		}
		
		public int getDifficulty() {
			return DIFFICULTY;
		}
		
		public boolean getAllowFlight() {
			return FLIGHT;
		}
		
		public boolean getAllowMonsters() {
			return MONSTERS;
		}
		
		public boolean getAllowAnimals() {
			return ANIMALS;
		}
		
		public long getStartTime() {
			return START_TIME;
		}
		
		public long getEndTime() {
			return END_TIME;
		}
		
		public boolean isStarted() {
			return STARTED;
		}
		
		public boolean shouldTerminate() {
			sql.connect();
			PreparedStatement ps = null;
			try {
				String sqlQuery = "SELECT * FROM lobbies WHERE id=?";
				ps = server.sqlConnection.prepareStatement(sqlQuery);
				ps.setInt(1, LOBBY_ID);
			    ResultSet rs = ps.executeQuery();
			    while (rs.next()) { // Set values
			    	return rs.getBoolean("terminate");
			    }
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
			    DbUtils.closeQuietly(ps);
			    DbUtils.closeQuietly(server.sqlConnection);
			}
			return false;
		}
		
		public boolean addPlayer(String player) {
			WebsiteReader check = new WebsiteReader("http://adventurelobbies.com/libs/status.php?user=" + player);
			if(getPlayers().size() < 6 && check.contains("safe", false) && !containsIgnoreCase(getPlayers(), player)) {
				List<String> players = new ArrayList<String>(getPlayers());
				players.add(player.toLowerCase());
				setPlayers(players);
				return true;
			} else {
				return false;
			}
		}
		
		public boolean containsIgnoreCase(List <String> l, String s){
			Iterator <String> it = l.iterator();
			while(it.hasNext()){
			if(it.next().equalsIgnoreCase(s))
				return true;
			}
			return false;
		}
		
		public boolean removePlayer(String player) {
			boolean result = false;
			List<String> players = new ArrayList<String>(getPlayers());
			result = players.remove(player.toLowerCase());
			setPlayers(players);
			if(bukkit.getPlayer(player) != null)
				bukkit.getPlayer(player).kickPlayer(msg.NOT_ALLOWED);
			return result;
		}
	
}
