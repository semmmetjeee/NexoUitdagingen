package me.semmmetje.nexouitdagingen.quest;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.semmmetje.nexouitdagingen.NexoUitdagingenPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public final class QuestPlaceholderExpansion extends PlaceholderExpansion {
  private final NexoUitdagingenPlugin plugin; public QuestPlaceholderExpansion(NexoUitdagingenPlugin plugin){this.plugin=plugin;}
  public @NotNull String getIdentifier(){return "nexouitdagingen";}public @NotNull String getAuthor(){return "Semmmetje";}public @NotNull String getVersion(){return plugin.getPluginMeta().getVersion();}public boolean persist(){return true;}
  public @Nullable String onRequest(OfflinePlayer offline,@NotNull String params){if(offline==null||!offline.isOnline())return "";Player p=offline.getPlayer();if(p==null)return "";plugin.quests().ensureAssignments(p);String key=params.toLowerCase(Locale.ROOT);for(QuestCategory c:QuestCategory.values()){String pre=c.name().toLowerCase(Locale.ROOT)+"_";if(key.startsWith(pre)){String rest=key.substring(pre.length());String[]parts=rest.split("_",2);try{int index=Integer.parseInt(parts[0])-1;List<QuestDefinition> qs=plugin.quests().assigned(p,c);if(index<0||index>=qs.size())return "";QuestDefinition q=qs.get(index);String field=parts.length>1?parts[1]:"name";long progress=plugin.quests().progress(p,q);return switch(field){case"name"->q.name();case"progress"->String.valueOf(progress);case"amount"->String.valueOf(q.amount());case"percent"->String.valueOf(Math.min(100,(int)Math.floor(progress*100D/q.amount())));case"completed"->String.valueOf(plugin.quests().completed(p,q));case"target"->q.target();case"type"->q.type().name();default->"";};}catch(Exception ignored){return "";}}}return null;}
}
