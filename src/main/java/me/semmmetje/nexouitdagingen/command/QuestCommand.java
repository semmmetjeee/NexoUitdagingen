package me.semmmetje.nexouitdagingen.command;

import me.semmmetje.nexouitdagingen.NexoUitdagingenPlugin;
import me.semmmetje.nexouitdagingen.quest.*;
import me.semmmetje.nexouitdagingen.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public final class QuestCommand extends BukkitCommand {
    private final NexoUitdagingenPlugin plugin;
    public QuestCommand(NexoUitdagingenPlugin plugin,String name,List<String>aliases,String permission){super(name,"NexoUitdagingen menu en beheer","/"+name,aliases);this.plugin=plugin;if(permission!=null&&!permission.isBlank())setPermission(permission);}
    @Override public boolean execute(@NotNull CommandSender sender,@NotNull String label,@NotNull String[] args){
        if(!testPermission(sender))return true;
        if(args.length==0){Player p=player(sender);if(p!=null)plugin.guis().open(p,plugin.getConfig().getString("settings.main-gui","main"));return true;}
        switch(args[0].toLowerCase(Locale.ROOT)){
            case"daily","weekly","global"->{Player p=player(sender);if(p!=null)plugin.guis().open(p,args[0].toLowerCase(Locale.ROOT));}
            case"open"->{Player p=player(sender);if(p!=null)plugin.guis().open(p,args.length>1?args[1]:plugin.getConfig().getString("settings.main-gui","main"));}
            case"reload"->{if(!admin(sender))return true;plugin.reloadEverything();sender.sendMessage(Text.color(plugin.message("reload")));}
            case"reroll"->{if(!admin(sender))return true;if(args.length<3){sender.sendMessage(Text.color("&cUsage: /"+label+" reroll <player> <daily|weekly>"));return true;}Player target=Bukkit.getPlayer(args[1]);if(target==null){sender.sendMessage(Text.color(plugin.message("unknown-player")));return true;}QuestCategory c=parsePersonal(args[2]);if(c==null){sender.sendMessage(Text.color(plugin.message("unknown-category")));return true;}plugin.quests().rerollPlayer(target,c);sender.sendMessage(Text.color(plugin.message("reroll-player").replace("%player%",target.getName()).replace("%category%",c.name().toLowerCase(Locale.ROOT))));}
            case"reset"->{if(!admin(sender))return true;if(args.length<2){sender.sendMessage(Text.color("&cUsage: /"+label+" reset <player>"));return true;}Player target=Bukkit.getPlayer(args[1]);if(target==null){sender.sendMessage(Text.color(plugin.message("unknown-player")));return true;}plugin.data().resetPlayer(target.getUniqueId());plugin.quests().ensureAssignments(target);sender.sendMessage(Text.color(plugin.message("reset-player").replace("%player%",target.getName())));}
            case"rerollglobal","resetglobal"->{if(!admin(sender))return true;plugin.quests().rerollGlobal();sender.sendMessage(Text.color(plugin.message("reset-global")));}
            case"addprogress"->{if(!admin(sender))return true;if(args.length<5){sender.sendMessage(Text.color("&cUsage: /"+label+" addprogress <player> <type> <target> <amount>"));return true;}Player target=Bukkit.getPlayer(args[1]);if(target==null){sender.sendMessage(Text.color(plugin.message("unknown-player")));return true;}try{QuestType type=QuestType.parse(args[2]);long amount=Long.parseLong(args[4]);plugin.quests().addProgress(target,type,args[3],amount);sender.sendMessage(Text.color("&aProgress toegevoegd."));}catch(Exception ex){sender.sendMessage(Text.color("&cOngeldig questtype of amount."));}}
            default->{Player p=player(sender);if(p!=null)plugin.guis().open(p,plugin.getConfig().getString("settings.main-gui","main"));}
        }return true;
    }
    private Player player(CommandSender s){if(s instanceof Player p)return p;s.sendMessage(Text.color(plugin.message("player-only")));return null;}
    private boolean admin(CommandSender s){if(s.hasPermission("nexouitdagingen.admin"))return true;s.sendMessage(Text.color(plugin.message("no-permission")));return false;}
    private static QuestCategory parsePersonal(String s){if(s.equalsIgnoreCase("daily"))return QuestCategory.DAILY;if(s.equalsIgnoreCase("weekly"))return QuestCategory.WEEKLY;return null;}
    @Override public @NotNull List<String> tabComplete(@NotNull CommandSender sender,@NotNull String alias,@NotNull String[] args){if(args.length==1){List<String>x=new ArrayList<>(List.of("daily","weekly","global","open"));if(sender.hasPermission("nexouitdagingen.admin"))x.addAll(List.of("reload","reroll","reset","rerollglobal","addprogress"));String q=args[0].toLowerCase(Locale.ROOT);return x.stream().filter(v->v.startsWith(q)).toList();}if(args.length==2&&args[0].equalsIgnoreCase("open")){String q=args[1].toLowerCase(Locale.ROOT);return plugin.guis().ids().stream().filter(v->v.startsWith(q)).toList();}if(args.length==2&&List.of("reroll","reset","addprogress").contains(args[0].toLowerCase(Locale.ROOT))){String q=args[1].toLowerCase(Locale.ROOT);return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(v->v.toLowerCase(Locale.ROOT).startsWith(q)).toList();}if(args.length==3&&args[0].equalsIgnoreCase("reroll"))return List.of("daily","weekly");if(args.length==3&&args[0].equalsIgnoreCase("addprogress")){String q=args[2].toUpperCase(Locale.ROOT);return Arrays.stream(QuestType.values()).map(Enum::name).filter(v->v.startsWith(q)).toList();}return List.of();}
}
