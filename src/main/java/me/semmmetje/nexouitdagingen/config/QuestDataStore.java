package me.semmmetje.nexouitdagingen.config;

import me.semmmetje.nexouitdagingen.quest.QuestCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class QuestDataStore {
    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration yaml;

    public QuestDataStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        reload();
    }

    public void reload() { yaml = YamlConfiguration.loadConfiguration(file); }

    public String period(UUID player, QuestCategory category) {
        return yaml.getString(path(player, category) + ".period", "");
    }

    public void setPeriod(UUID player, QuestCategory category, String period) {
        yaml.set(path(player, category) + ".period", period);
    }

    public List<String> assigned(UUID player, QuestCategory category) {
        return new ArrayList<>(yaml.getStringList(path(player, category) + ".assigned"));
    }

    public void setAssigned(UUID player, QuestCategory category, String period, List<String> ids) {
        String path = path(player, category);
        yaml.set(path + ".period", period);
        yaml.set(path + ".assigned", ids);
        yaml.set(path + ".progress", null);
        yaml.set(path + ".completed", null);
        save();
    }

    public long progress(UUID player, QuestCategory category, String questId) {
        return yaml.getLong(path(player, category) + ".progress." + questId, 0L);
    }

    public long addProgress(UUID player, QuestCategory category, String questId, long delta) {
        String key = path(player, category) + ".progress." + questId;
        long next = Math.max(0L, yaml.getLong(key, 0L) + delta);
        yaml.set(key, next);
        return next;
    }

    public boolean completed(UUID player, QuestCategory category, String questId) {
        return yaml.getBoolean(path(player, category) + ".completed." + questId, false);
    }

    public void setCompleted(UUID player, QuestCategory category, String questId, boolean completed) {
        yaml.set(path(player, category) + ".completed." + questId, completed);
    }

    public String globalPeriod() { return yaml.getString("global.period", ""); }
    public List<String> globalAssigned() { return new ArrayList<>(yaml.getStringList("global.assigned")); }
    public void setGlobalAssigned(String period, List<String> ids) {
        yaml.set("global.period", period);
        yaml.set("global.assigned", ids);
        yaml.set("global.progress", null);
        yaml.set("global.completed", null);
        yaml.set("global.milestones", null);
        save();
    }
    public long globalProgress(String questId) { return yaml.getLong("global.progress." + questId, 0L); }
    public long addGlobalProgress(String questId, long delta) {
        String key="global.progress."+questId; long next=Math.max(0L,yaml.getLong(key,0L)+delta); yaml.set(key,next); return next;
    }
    public boolean globalCompleted(String questId) { return yaml.getBoolean("global.completed."+questId,false); }
    public void setGlobalCompleted(String questId, boolean completed) { yaml.set("global.completed."+questId,completed); }
    public boolean milestoneSent(String questId,int percent){return yaml.getBoolean("global.milestones."+questId+"."+percent,false);}
    public void setMilestoneSent(String questId,int percent){yaml.set("global.milestones."+questId+"."+percent,true);}

    public void resetPlayer(UUID uuid) { yaml.set("players." + uuid, null); save(); }

    public Set<UUID> knownPlayers() {
        ConfigurationSection section = yaml.getConfigurationSection("players");
        if (section == null) return Set.of();
        Set<UUID> result = new HashSet<>();
        for (String key : section.getKeys(false)) try { result.add(UUID.fromString(key)); } catch (IllegalArgumentException ignored) {}
        return result;
    }

    public void save() {
        try { yaml.save(file); }
        catch (IOException ex) { plugin.getLogger().severe("Could not save data.yml: " + ex.getMessage()); }
    }

    private static String path(UUID player, QuestCategory category) {
        return "players." + player + "." + category.name().toLowerCase(Locale.ROOT);
    }
}
