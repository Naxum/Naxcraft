package com.naxville.naxcraft.admin;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.bukkit.util.config.Configuration;

public class NaxcraftConfiguration extends Configuration {

	public NaxcraftConfiguration(File file){
		super(file);
	}
	
	@SuppressWarnings("rawtypes")
	public void removeProperty(String path){
        
        if (!path.contains(".")) {
        	return;
        }
        
        String[] parts = path.split("\\.");
        Map<String, Object> node = root;
        
        for (int i = 0; i < parts.length; i++) {
            Object o = node.get(parts[0]);
            
            if (((HashMap)o).containsKey(parts[1])){
            	((HashMap)o).remove(parts[1]);
            }
        }
	}
}
