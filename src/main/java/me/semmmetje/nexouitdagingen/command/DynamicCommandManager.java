package me.semmmetje.nexouitdagingen.command;

import me.semmmetje.nexouitdagingen.NexoUitdagingenPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import java.util.*;

public final class DynamicCommandManager {
    private final NexoUitdagingenPlugin plugin; private QuestCommand command;
    public DynamicCommandManager(NexoUitdagingenPlugin plugin){this.plugin=plugin;}
    public void register(){unregister();String name=sanitize(plugin.getConfig().getString("command.name","uitdagingen"));List<String>aliases=plugin.getConfig().getStringList("command.aliases").stream().map(DynamicCommandManager::sanitize).filter(x->!x.isBlank()&&!x.equals(name)).distinct().toList();command=new QuestCommand(plugin,name,aliases,plugin.getConfig().getString("command.permission","nexouitdagingen.use"));CommandMap map=Bukkit.getServer().getCommandMap();map.register(plugin.getName().toLowerCase(Locale.ROOT),command);Bukkit.getScheduler().runTask(plugin,()->Bukkit.getOnlinePlayers().forEach(Player::updateCommands));}
    public void unregister(){if(command!=null){command.unregister(Bukkit.getServer().getCommandMap());command=null;}}
    private static String sanitize(String s){return s==null?"":s.toLowerCase(Locale.ROOT).replaceFirst("^/+","").replaceAll("[^a-z0-9_:-]","");}
}
