package me.semmmetje.nexouitdagingen.quest;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.*;

public final class QuestPoolLoader {
    private final JavaPlugin plugin;
    private final Map<QuestCategory, LinkedHashMap<String, QuestDefinition>> pools = new EnumMap<>(QuestCategory.class);

    public QuestPoolLoader(JavaPlugin plugin) { this.plugin=plugin; for(QuestCategory c:QuestCategory.values()) pools.put(c,new LinkedHashMap<>()); }

    public void load() {
        pools.values().forEach(Map::clear);
        loadFile(QuestCategory.DAILY, "quests/daily.yml");
        loadFile(QuestCategory.WEEKLY, "quests/weekly.yml");
        loadFile(QuestCategory.GLOBAL, "quests/global.yml");
        for (QuestCategory c : QuestCategory.values()) plugin.getLogger().info("Loaded " + pools.get(c).size() + " " + c.name().toLowerCase() + " quest(s).");
    }

    private void loadFile(QuestCategory category, String relative) {
        File file=new File(plugin.getDataFolder(),relative); YamlConfiguration yaml=YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root=yaml.getConfigurationSection("quests"); if(root==null)return;
        for(String id:root.getKeys(false)) {
            ConfigurationSection s=root.getConfigurationSection(id); if(s==null)continue;
            try {
                QuestType type=QuestType.parse(s.getString("type","BREAK_BLOCK"));
                long amount=Math.max(1L,s.getLong("amount",1L));
                QuestDefinition q=new QuestDefinition(id,category,s.getString("name",id),type,s.getString("target","ANY"),amount,s.getString("material","PAPER"),List.copyOf(s.getStringList("lore")),List.copyOf(s.getStringList("rewards")),s.getString("placeholder",""),s.getString("operator",">="),s.getString("value",""));
                pools.get(category).put(id.toLowerCase(Locale.ROOT),q);
            } catch(Exception ex) { plugin.getLogger().warning("Could not load quest "+id+" from "+relative+": "+ex.getMessage()); }
        }
    }

    public Collection<QuestDefinition> all(QuestCategory category){return Collections.unmodifiableCollection(pools.get(category).values());}
    public QuestDefinition get(QuestCategory category,String id){return id==null?null:pools.get(category).get(id.toLowerCase(Locale.ROOT));}
}
