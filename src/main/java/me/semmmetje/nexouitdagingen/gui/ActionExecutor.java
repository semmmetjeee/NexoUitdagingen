package me.semmmetje.nexouitdagingen.gui;

import me.clip.placeholderapi.PlaceholderAPI;
import me.semmmetje.nexouitdagingen.NexoUitdagingenPlugin;
import me.semmmetje.nexouitdagingen.util.Text;
import org.bukkit.*;
import org.bukkit.entity.Player;
import java.util.*;

public final class ActionExecutor {
  private final NexoUitdagingenPlugin plugin;
  public ActionExecutor(NexoUitdagingenPlugin plugin){this.plugin=plugin;}
  public void execute(Player player,List<String> actions,boolean global){if(actions==null)return;for(String raw:actions)execute(player,raw,global);}
  private void execute(Player player,String raw,boolean global){if(raw==null||raw.isBlank())return;String value=raw;if(player!=null){value=value.replace("%player%",player.getName()).replace("%player_name%",player.getName());if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))value=PlaceholderAPI.setPlaceholders(player,value);}String up=value.toUpperCase(Locale.ROOT);try{
    if(up.startsWith("[CONSOLE]"))Bukkit.dispatchCommand(Bukkit.getConsoleSender(),strip(value,"[CONSOLE]").replaceFirst("^/",""));
    else if(up.startsWith("[PLAYER]")&&player!=null)player.performCommand(strip(value,"[PLAYER]").replaceFirst("^/",""));
    else if(up.startsWith("[MESSAGE]")&&player!=null)player.sendMessage(Text.color(strip(value,"[MESSAGE]")));
    else if(up.startsWith("[BROADCAST]"))Bukkit.broadcastMessage(Text.color(strip(value,"[BROADCAST]")));
    else if(up.startsWith("[SOUND]")&&player!=null){String[]p=strip(value,"[SOUND]").split("\\s+");player.playSound(player.getLocation(),Sound.valueOf(p[0].toUpperCase(Locale.ROOT)),p.length>1?Float.parseFloat(p[1]):1F,p.length>2?Float.parseFloat(p[2]):1F);}
    else if(up.startsWith("[GUI]")&&player!=null)plugin.guis().open(player,strip(value,"[GUI]"));
    else if(up.startsWith("[CLOSE]")&&player!=null)player.closeInventory();
    else if(global)Bukkit.dispatchCommand(Bukkit.getConsoleSender(),value.replaceFirst("^/",""));
    else plugin.getLogger().warning("Unknown action: "+raw);
  }catch(Exception ex){plugin.getLogger().warning("Action failed '"+raw+"': "+ex.getMessage());}}
  private static String strip(String s,String p){return s.substring(p.length()).trim();}
}
