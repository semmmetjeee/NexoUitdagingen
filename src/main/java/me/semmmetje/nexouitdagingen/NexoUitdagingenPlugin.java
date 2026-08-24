package me.semmmetje.nexouitdagingen;

import me.semmmetje.nexouitdagingen.command.DynamicCommandManager;
import me.semmmetje.nexouitdagingen.config.QuestDataStore;
import me.semmmetje.nexouitdagingen.gui.*;
import me.semmmetje.nexouitdagingen.quest.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public final class NexoUitdagingenPlugin extends JavaPlugin {
  private QuestDataStore data;private QuestPoolLoader pools;private QuestManager quests;private ActionExecutor actions;private GuiManager guis;private DynamicCommandManager commands;private QuestPlaceholderExpansion expansion;
  @Override public void onEnable(){saveDefaultConfig();for(String r:new String[]{"quests/daily.yml","quests/weekly.yml","quests/global.yml","guis/main.yml","guis/daily.yml","guis/weekly.yml","guis/global.yml"})saveIfMissing(r);data=new QuestDataStore(this);pools=new QuestPoolLoader(this);quests=new QuestManager(this,pools,data);actions=new ActionExecutor(this);guis=new GuiManager(this);commands=new DynamicCommandManager(this);Bukkit.getPluginManager().registerEvents(guis,this);Bukkit.getPluginManager().registerEvents(new QuestListener(this),this);reloadEverything();if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){expansion=new QuestPlaceholderExpansion(this);expansion.register();}Bukkit.getScheduler().runTaskTimer(this,()->{quests.ensureGlobal();for(var p:Bukkit.getOnlinePlayers()){quests.ensureAssignments(p);quests.evaluatePlaceholderQuests(p);}},20L*60,20L*60);getLogger().info("NexoUitdagingen "+getPluginMeta().getVersion()+" enabled.");}
  @Override public void onDisable(){if(commands!=null)commands.unregister();if(expansion!=null)expansion.unregister();if(data!=null)data.save();}
  public void reloadEverything(){reloadConfig();pools.load();quests.reloadPeriods();guis.load();commands.register();for(var p:Bukkit.getOnlinePlayers())quests.ensureAssignments(p);quests.ensureGlobal();}
  private void saveIfMissing(String path){File f=new File(getDataFolder(),path);if(f.getParentFile()!=null)f.getParentFile().mkdirs();if(!f.exists())saveResource(path,false);}
  public String message(String key){return getConfig().getString("messages.prefix","")+getConfig().getString("messages."+key,key);}public void debug(String s){if(getConfig().getBoolean("settings.debug",false))getLogger().info("[DEBUG] "+s);}
  public QuestDataStore data(){return data;}public QuestManager quests(){return quests;}public ActionExecutor actions(){return actions;}public GuiManager guis(){return guis;}
}
