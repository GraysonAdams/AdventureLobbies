package com.geekplaya.AdventureLobbies.Objects;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.Server;
import org.bukkit.World;

import com.geekplaya.AdventureLobbies.AdventureLobbies;

public class Map {
	
	private AdventureLobbies server = AdventureLobbies.plugin;
	private Server bukkit = AdventureLobbies.bukkit;
	
	private String MAP_NAME;
	private int MONSTER_LIMIT;
	private int ANIMAL_LIMIT;
	@SuppressWarnings("unused")
	private boolean PAUSED;
	
	public Map(String map, long seed, boolean paused) {
		MAP_NAME = map;
		PAUSED = paused;
	}
	
	public String getName() {
		return MAP_NAME;
	}
	
	public int getMonsterLimitOriginal() {
		return MONSTER_LIMIT;
	}
	
	public int getAnimalLimitOriginal() {
		return ANIMAL_LIMIT;
	}
	
	public void deleteWorlds(boolean amap) {
		try {
			if(amap) {
				FileUtils.deleteDirectory(new File("amap"));
				FileUtils.deleteDirectory(new File("amap_nether"));
				FileUtils.deleteDirectory(new File("amap_the_end"));
			}
			FileUtils.deleteDirectory(new File("world/data"));
			FileUtils.deleteDirectory(new File("world/players"));
		} catch (IOException e) {
			server.log.info("One or more old worlds or player/data folders do not exist.");
		}
	}
	
	public void downloadWorld() {
		FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect("mysql.adventurelobbies.com", 21);
            ftpClient.login("ftpmaps", "rckthatock123");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            String remoteFile1 = MAP_NAME + ".zip";
            File downloadFile1 = new File(MAP_NAME + ".zip");
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
            boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
            outputStream1.close();
            if (success) {
                System.out.println(MAP_NAME + " has been downloaded successfully.");
            }
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
                extractFolder(MAP_NAME + ".zip");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        new File(MAP_NAME + ".zip").delete();
	}
	
	static public void extractFolder(String zipFile) throws ZipException, IOException 
	{
	    System.out.println(zipFile);
	    int BUFFER = 2048;
	    File file = new File(zipFile);

	    ZipFile zip = new ZipFile(file);
	    String newPath = System.getProperty("user.dir");
	    
	    Enumeration zipFileEntries = zip.entries();

	    while (zipFileEntries.hasMoreElements())
	    {
	        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
	        String currentEntry = entry.getName();
	        File destFile = new File(newPath, currentEntry);
	        File destinationParent = destFile.getParentFile();

	        destinationParent.mkdirs();

	        if (!entry.isDirectory())
	        {
	            BufferedInputStream is = new BufferedInputStream(zip
	            .getInputStream(entry));
	            int currentByte;
	            byte data[] = new byte[BUFFER];

	            FileOutputStream fos = new FileOutputStream(destFile);
	            BufferedOutputStream dest = new BufferedOutputStream(fos,
	            BUFFER);

	            while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
	                dest.write(data, 0, currentByte);
	            }
	            dest.flush();
	            dest.close();
	            is.close();
	        }

	        if (currentEntry.endsWith(".zip"))
	        {
	            // found a zip file, try to open
	            extractFolder(destFile.getAbsolutePath());
	        }
	    }
	    zip.close();
	}
	
	public void copyWorld() {		
		try {
			if(new File(MAP_NAME).exists()) {
				FileUtils.copyDirectory(new File(MAP_NAME), new File("amap"));
				FileUtils.deleteDirectory(new File(MAP_NAME));
				FileUtils.copyDirectory(new File("amap"), new File("amap_nether"));
				FileUtils.copyDirectory(new File("amap"), new File("amap_the_end"));
				new File("amap_nether/uid.dat").delete();
				new File("amap_the_end/uid.dat").delete();
			} else {
				server.log.info("WARNING! " + MAP_NAME + " DOES NOT EXIST!");
			}
			new File("world/data").mkdir();
			new File("world/players").mkdir();
			if(new File("amap/data").exists())
				FileUtils.copyDirectory(new File("amap/data"), new File("world/data"));
		} catch (IOException e) {
			server.log.info("Error copying over the map!");
			e.printStackTrace();
		}
		bukkit.setSpawnRadius(0);
	}
	
	public World getNether() {
		return bukkit.getWorld("amap_nether");
	}
	
	public World getTheEnd() {
		return bukkit.getWorld("amap_the_end");
	}
	
	public World getOverworld() {
		return bukkit.getWorld("amap");
	}
	
}
