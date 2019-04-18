package com.geekplaya.AdventureLobbies;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.geekplaya.AdventureLobbies.Objects.Lobby;

public class AdventureLobbies extends JavaPlugin {
	
	public Logger log = Logger.getLogger("Minecraft");
	public static AdventureLobbies plugin;
	public static ServiceHandler service;
	public static MessageVariables msg;
	public static SQLHandler sql;
	public static SQLHandlerForums sqlForums;
	public static PlayerListener playerListener;
	public static CommandListener commandListener;
	public static EntityListener entityListener;
	public static Server bukkit = Bukkit.getServer();
	
	
	public List<String> kickedPlayers = Collections.emptyList();
	
	public Connection sqlConnection = null;	
	public Connection sqlConnectionForums = null;	
	public Lobby lobby;
	
	public int serverId = 0;
	
	@Override
	public void onEnable()
	{
		AdventureLobbies.plugin = this;
		service = new ServiceHandler();
		msg = new MessageVariables();
		sql = new SQLHandler();
		sqlForums = new SQLHandlerForums();
		playerListener = new PlayerListener();
		commandListener = new CommandListener();
		entityListener = new EntityListener();
		fetchClasses();
		
		if(Bukkit.getWorld("world") != null) {
			try {
				FileUtils.deleteDirectory(new File("amap"));
				FileUtils.deleteDirectory(new File("amap_nether"));
				FileUtils.deleteDirectory(new File("amap_the_end"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		PluginDescriptionFile descFile = getDescription();
		String pluginName = "AdventureLobbies (v: " + descFile.getVersion() + ")";
		log.info("Now enabling " + pluginName);
		
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(commandListener, this);
		getServer().getPluginManager().registerEvents(entityListener, this);
		
		System.out.println("Does forum.txt exist?" + new File("../forum.txt").exists());

		sql.connect();
		service.innitiate();
	}
	
	@Override
	public void onDisable()
	{
		sql.update("UPDATE servers SET runningmap = '" +0  + "' WHERE id = '" + serverId + "'");
		
		if(lobby != null) {
			if(lobby.isStarted() && lobby.getOnlinePlayerCount() > 0)
				lobby.pause();
			else
				lobby.stop();
		}
//		lobby.pause();
		
		sql.update("UPDATE servers SET latest = '" + (getTime()-30) + "' WHERE id = '" + serverId + "'");
		
		PluginDescriptionFile descFile = getDescription();
		String pluginName = "AdventureLobbies (v: " + descFile.getVersion() + ")";
		log.info(pluginName + " is now disabled");
		try {
			sql.close();
			sqlForums.close();
		} catch (Exception e) { }
		//updateMultiverseYML();
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		return commandListener.onCommand(sender, command, label, args);
	}
	
	public long getTime() {
		return (System.currentTimeMillis() / 1000L);  
	}
	
	public void fetchClasses() {
		service.fetchClasses();
		sql.fetchClasses();
		sqlForums.fetchClasses();
		playerListener.fetchClasses();
		commandListener.fetchClasses();
		entityListener.fetchClasses();
		PlayerPortal.fetchClasses();
	}
	
	public void updateMultiverseYML() {
		new File("plugins/MultiWorld/config.yml").delete();
		FTPClient ftpClient = new FTPClient();
        try {
 
            ftpClient.connect("mysql.adventurelobbies.com", 21);
            ftpClient.login("ftpmaps", "rckthatock123");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 
            String remoteFile1 = "config.yml";
            File downloadFile1 = new File("plugins/MultiWorld/config.yml");
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
            boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
            outputStream1.close();
	        } catch (IOException ex) {
	            System.out.println("Error: " + ex.getMessage());
	            ex.printStackTrace();
	        } finally {
	            try {
	                if (ftpClient.isConnected()) {
	                    ftpClient.logout();
	                    ftpClient.disconnect();
	                }
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }
	}
	
}