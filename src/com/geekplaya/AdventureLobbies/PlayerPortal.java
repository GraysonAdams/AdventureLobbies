package com.geekplaya.AdventureLobbies;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_4_R1.CraftTravelAgent;
import org.bukkit.craftbukkit.v1_4_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerPortal {

	/**
	 * The most annoying thing in the world...
	 */	
	private static AdventureLobbies server;
	private static Server bukkit = Bukkit.getServer();
	public static void fetchClasses() {
		server = AdventureLobbies.plugin;
	}
	
	public static void onPlayerPortal (PlayerPortalEvent event) {
		Player player = event.getPlayer();
		CraftTravelAgent travelAgent = new CraftTravelAgent(((CraftWorld) player.getWorld()).getHandle());
		boolean inNormal = player.getWorld().getEnvironment().equals(World.Environment.NORMAL);
		boolean inNether = player.getWorld().getEnvironment().equals(World.Environment.NETHER);	
		boolean inTheEnd = player.getWorld().getEnvironment().equals(World.Environment.THE_END);
		
		String world = "amap";
		
		if(event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) && (inNormal || inNether)) {
			travelAgent.setSearchRadius(35);
			double multiplication = 1d / 8d;
			if(inNormal) {
				world += "_nether";
				multiplication = 8d;
			}
			Location netherLocation = player.getLocation().clone();
			netherLocation.setWorld(Bukkit.getServer().getWorld(world));
			netherLocation.multiply(multiplication);
			player.teleport(travelAgent.findOrCreate(netherLocation));
			
		} else if(event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL) && (inNormal || inTheEnd)) {
			if(inNormal) {
				world += "_the_end";
				Location endLocation = new Location(bukkit.getWorld(world), 0, 72, 0);
				travelAgent.setSearchRadius(70);
				player.teleport(travelAgent.findOrCreate(endLocation));
			} else {
				Location endLocation = server.lobby.getMap().getOverworld().getSpawnLocation();
				endLocation.setWorld(bukkit.getWorld(world));
				player.teleport(endLocation);
			}
		}
	}
	
}
