package me.semmmetje.nexouitdagingen.quest;

import me.clip.placeholderapi.PlaceholderAPI;
import me.semmmetje.nexouitdagingen.NexoUitdagingenPlugin;
import me.semmmetje.nexouitdagingen.config.QuestDataStore;
import me.semmmetje.nexouitdagingen.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public final class QuestManager {
    private final NexoUitdagingenPlugin plugin;
    private final QuestPoolLoader pools;
    private final QuestDataStore data;
    private PeriodService periods;
    private final Random random = new Random();

    public QuestManager(NexoUitdagingenPlugin plugin, QuestPoolLoader pools, QuestDataStore data) {
        this.plugin=plugin; this.pools=pools; this.data=data; this.periods=new PeriodService(plugin.getConfig());
    }

    public void reloadPeriods(){ this.periods=new PeriodService(plugin.getConfig()); }

    public void ensureAssignments(Player player) {
        ensurePersonal(player, QuestCategory.DAILY);
        ensurePersonal(player, QuestCategory.WEEKLY);
        ensureGlobal();
    }

    public void ensureGlobal() {
        String key=periods.globalKey();
        if(!key.equals(data.globalPeriod()) || data.globalAssigned().isEmpty()) rerollGlobal(key);
    }

    private void ensurePersonal(Player player, QuestCategory category) {
        String key=category==QuestCategory.DAILY?periods.dailyKey():periods.weeklyKey();
        if(!key.equals(data.period(player.getUniqueId(),category)) || data.assigned(player.getUniqueId(),category).isEmpty()) rerollPlayer(player,category,key);
    }

    public void rerollPlayer(Player player, QuestCategory category) {
        String key=category==QuestCategory.DAILY?periods.dailyKey():periods.weeklyKey();
        rerollPlayer(player,category,key);
    }

    private void rerollPlayer(Player player, QuestCategory category, String key) {
        int count=category==QuestCategory.DAILY?plugin.getConfig().getInt("settings.personal-daily-count",3):plugin.getConfig().getInt("settings.personal-weekly-count",3);
        data.setAssigned(player.getUniqueId(),category,key,randomIds(category,count));
    }

    public void rerollGlobal() { rerollGlobal(periods.globalKey()); }
    private void rerollGlobal(String key) { data.setGlobalAssigned(key,randomIds(QuestCategory.GLOBAL,plugin.getConfig().getInt("settings.global-count",3))); }

    private List<String> randomIds(QuestCategory category,int count) {
        List<String> ids=new ArrayList<>(pools.all(category).stream().map(QuestDefinition::id).toList());
        Collections.shuffle(ids,random);
        return ids.subList(0,Math.min(Math.max(0,count),ids.size()));
    }

    public List<QuestDefinition> assigned(Player player, QuestCategory category) {
        ensureAssignments(player);
        List<String> ids=category==QuestCategory.GLOBAL?data.globalAssigned():data.assigned(player.getUniqueId(),category);
        return ids.stream().map(id->pools.get(category,id)).filter(Objects::nonNull).toList();
    }

    public long progress(Player player, QuestDefinition q) {
        return q.category()==QuestCategory.GLOBAL?data.globalProgress(q.id()):data.progress(player.getUniqueId(),q.category(),q.id());
    }

    public boolean completed(Player player, QuestDefinition q) {
        return q.category()==QuestCategory.GLOBAL?data.globalCompleted(q.id()):data.completed(player.getUniqueId(),q.category(),q.id());
    }

    public void addProgress(Player player, QuestType type, String target, long amount) {
        if(amount<=0)return;
        ensureAssignments(player);
        for(QuestCategory category:List.of(QuestCategory.DAILY,QuestCategory.WEEKLY)) {
            for(QuestDefinition q:assigned(player,category)) {
                if(q.type()!=type || completed(player,q) || !matches(q.target(),target))continue;
                long next=data.addProgress(player.getUniqueId(),category,q.id(),amount);
                if(next>=q.amount()) completePersonal(player,q);
            }
        }
        ensureGlobal();
        for(String id:data.globalAssigned()) {
            QuestDefinition q=pools.get(QuestCategory.GLOBAL,id);
            if(q==null||q.type()!=type||data.globalCompleted(q.id())||!matches(q.target(),target))continue;
            long before=data.globalProgress(q.id()); long next=data.addGlobalProgress(q.id(),amount);
            checkMilestones(q,before,next);
            if(next>=q.amount()) completeGlobal(q);
        }
        data.save();
    }

    public void evaluatePlaceholderQuests(Player player) {
        if(!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))return;
        ensureAssignments(player);
        for(QuestCategory category:List.of(QuestCategory.DAILY,QuestCategory.WEEKLY)) for(QuestDefinition q:assigned(player,category)) {
            if(q.type()!=QuestType.PLACEHOLDER||completed(player,q)||q.placeholder().isBlank())continue;
            String actual=PlaceholderAPI.setPlaceholders(player,q.placeholder());
            if(compare(actual,q.operator(),q.value())) completePersonal(player,q);
        }
    }

    private void completePersonal(Player player, QuestDefinition q) {
        data.setCompleted(player.getUniqueId(),q.category(),q.id(),true);
        plugin.actions().execute(player,q.rewards(),false);
        player.sendMessage(Text.color(plugin.message("quest-completed").replace("%quest_name%",q.name())));
        if(plugin.getConfig().getBoolean("settings.announce-personal-completion",false)) Bukkit.broadcastMessage(Text.color(plugin.message("quest-completed").replace("%quest_name%",q.name()).replace("%player%",player.getName())));
    }

    private void completeGlobal(QuestDefinition q) {
        data.setGlobalCompleted(q.id(),true);
        plugin.actions().execute(null,q.rewards(),true);
        Bukkit.broadcastMessage(Text.color(plugin.message("global-completed").replace("%quest_name%",q.name())));
    }

    private void checkMilestones(QuestDefinition q,long before,long after) {
        if(!plugin.getConfig().getBoolean("settings.announce-global-progress-milestones",true))return;
        for(int percent:plugin.getConfig().getIntegerList("settings.global-progress-milestones")) {
            if(percent<=0||percent>=100||data.milestoneSent(q.id(),percent))continue;
            double threshold=q.amount()*(percent/100D);
            if(before<threshold&&after>=threshold) {
                data.setMilestoneSent(q.id(),percent);
                Bukkit.broadcastMessage(Text.color(plugin.message("global-milestone").replace("%quest_name%",q.name()).replace("%percent%",String.valueOf(percent))));
            }
        }
    }

    private static boolean matches(String expected,String actual) {
        if(expected==null||expected.isBlank()||expected.equalsIgnoreCase("ANY")||expected.equals("*"))return true;
        return actual!=null&&Arrays.stream(expected.split("\\|")).map(String::trim).anyMatch(x->x.equalsIgnoreCase(actual));
    }

    private static boolean compare(String left,String op,String right){
        Double a=num(left),b=num(right);
        if(a!=null&&b!=null)return switch(op){case">="->a>=b;case"<="->a<=b;case">"->a>b;case"<"->a<b;case"!="->Double.compare(a,b)!=0;default->Double.compare(a,b)==0;};
        int c=left.trim().compareToIgnoreCase(right.trim()); return op.equals("!=")?c!=0:c==0;
    }
    private static Double num(String raw){try{return Double.parseDouble(raw.replace(",","").replaceAll("[^0-9.+-]",""));}catch(Exception e){return null;}}

    public QuestPoolLoader pools(){return pools;}
    public QuestDataStore data(){return data;}
}
