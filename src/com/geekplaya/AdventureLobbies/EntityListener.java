package com.geekplaya.AdventureLobbies;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityListener implements Listener {
	
	/**
	 * The most annoying thing in the world...
	 */	
	private AdventureLobbies server;
	public void fetchClasses() {
		server = AdventureLobbies.plugin;
	}

	private List<EntityType> animals = Arrays.asList(EntityType.PIG, EntityType.COW, EntityType.MUSHROOM_COW, EntityType.CHICKEN, EntityType.SHEEP, EntityType.SQUID, EntityType.WOLF, EntityType.OCELOT);
	private List<EntityType> monsters = Arrays.asList(EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.ENDERMAN, EntityType.GHAST, EntityType.GIANT, EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SPIDER, EntityType.WITCH, EntityType.WITHER, EntityType.ZOMBIE);
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCreatureSpawn(CreatureSpawnEvent event){
		if(event.getEntity() != null && server.lobby != null) {
			if(!server.lobby.getAllowAnimals() && animals.contains(event.getEntityType()) && event.getSpawnReason() == SpawnReason.NATURAL) {
				event.setCancelled(true);
			}
			if(!server.lobby.getAllowMonsters() && monsters.contains(event.getEntityType()) && event.getSpawnReason() == SpawnReason.NATURAL) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		Entity e = event.getEntity();
		if(e instanceof Player) {
			if(server.lobby.getFrozen()) {
				event.setCancelled(true);
			}
		}
	}
	
}
