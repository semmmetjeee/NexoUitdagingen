package me.semmmetje.nexouitdagingen.gui;

import me.clip.placeholderapi.PlaceholderAPI;
import me.semmmetje.nexouitdagingen.NexoUitdagingenPlugin;
import me.semmmetje.nexouitdagingen.quest.*;
import me.semmmetje.nexouitdagingen.util.Text;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public final class GuiManager implements Listener {
  private final NexoUitdagingenPlugin plugin; private final Map<String,YamlConfiguration> guis=new HashMap<>();
  public GuiManager(NexoUitdagingenPlugin plugin){this.plugin=plugin;}
  public void load(){guis.clear();File d=new File(plugin.getDataFolder(),"guis");File[]fs=d.listFiles((x,n)->n.endsWith(".yml"));if(fs==null)return;for(File f:fs)guis.put(f.getName().replaceFirst("\\.yml$","").toLowerCase(Locale.ROOT),YamlConfiguration.loadConfiguration(f));}
  public List<String> ids(){return guis.keySet().stream().sorted().toList();}
  public void open(Player player,String id){plugin.quests().ensureAssignments(player);YamlConfiguration y=guis.get(id.toLowerCase(Locale.ROOT));if(y==null)return;int size=norm(y.getInt("size",45));Holder h=new Holder(id);Inventory inv=Bukkit.createInventory(h,size,Text.color(resolve(player,y.getString("title","Uitdagingen"))));h.inv=inv;renderBorder(player,y.getConfigurationSection("border"),inv,h);renderGroup(player,y.getConfigurationSection("panes"),inv,h,false);renderGroup(player,y.getConfigurationSection("decorations"),inv,h,false);renderQuestSlots(player,y,inv,h);renderGroup(player,y.getConfigurationSection("items"),inv,h,true);player.openInventory(inv);}
  private void renderQuestSlots(Player p,YamlConfiguration y,Inventory inv,Holder h){ConfigurationSection sec=y.getConfigurationSection("quest-slots");if(sec==null)return;QuestCategory category;try{category=QuestCategory.valueOf(y.getString("quest-category","DAILY").toUpperCase(Locale.ROOT));}catch(Exception e){return;}List<QuestDefinition> quests=plugin.quests().assigned(p,category);List<Integer> slots=parse(sec,inv.getSize());for(int i=0;i<Math.min(slots.size(),quests.size());i++){QuestDefinition q=quests.get(i);int slot=slots.get(i);ItemStack item=questItem(p,q,sec);inv.setItem(slot,item);}}
  private ItemStack questItem(Player p,QuestDefinition q,ConfigurationSection defaults){Material m=Material.matchMaterial(q.material());if(m==null)m=Material.PAPER;ItemStack it=new ItemStack(m);ItemMeta meta=it.getItemMeta();long progress=plugin.quests().progress(p,q);boolean done=plugin.quests().completed(p,q);String name=done?defaults.getString("completed-name","&a✔ %quest_name%"):defaults.getString("active-name","&#3bd9ff&l%quest_name%");meta.setDisplayName(Text.color(repl(p,q,name,progress)));List<String> lore=q.lore().isEmpty()?defaults.getStringList("lore"):q.lore();List<String> out=new ArrayList<>();for(String l:lore)out.add(Text.color(repl(p,q,l,progress)));meta.setLore(out);if(done&&defaults.getBoolean("completed-glow",true)){meta.addEnchant(Enchantment.UNBREAKING,1,true);meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);}meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_ADDITIONAL_TOOLTIP);it.setItemMeta(meta);return it;}
  private String repl(Player p,QuestDefinition q,String s,long progress){String x=s.replace("%quest_name%",q.name()).replace("%progress%",String.valueOf(Math.min(progress,q.amount()))).replace("%amount%",String.valueOf(q.amount())).replace("%target%",q.target()).replace("%percent%",String.valueOf(Math.min(100,(int)Math.floor(progress*100D/q.amount()))));return resolve(p,x);}
  private void renderBorder(Player p,ConfigurationSection s,Inventory inv,Holder h){if(s==null)return;List<Integer> slots=parse(s,inv.getSize());if(slots.isEmpty())for(int i=0;i<inv.getSize();i++)if(i<9||i>=inv.getSize()-9||i%9==0||i%9==8)slots.add(i);renderOne(p,s,inv,h,false,slots);}
  private void renderGroup(Player p,ConfigurationSection root,Inventory inv,Holder h,boolean overwrite){if(root==null)return;for(String k:root.getKeys(false)){ConfigurationSection s=root.getConfigurationSection(k);if(s!=null)renderOne(p,s,inv,h,overwrite,parse(s,inv.getSize()));}}
  private void renderOne(Player p,ConfigurationSection s,Inventory inv,Holder h,boolean overwrite,List<Integer> slots){Material m=Material.matchMaterial(s.getString("material","STONE"));if(m==null)m=Material.STONE;ItemStack it=new ItemStack(m);ItemMeta meta=it.getItemMeta();meta.setDisplayName(Text.color(resolve(p,s.getString("display_name",s.getString("name","")))));meta.setLore(s.getStringList("lore").stream().map(x->Text.color(resolve(p,x))).toList());meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_ADDITIONAL_TOOLTIP);it.setItemMeta(meta);List<String>a=new ArrayList<>(s.getStringList("actions"));if(s.getBoolean("close",false))a.add(0,"[CLOSE]");for(int slot:slots){if(!overwrite&&inv.getItem(slot)!=null)continue;inv.setItem(slot,it);if(!a.isEmpty())h.actions.put(slot,List.copyOf(a));}}
  @EventHandler(priority=EventPriority.HIGHEST)public void click(InventoryClickEvent e){if(!(e.getView().getTopInventory().getHolder() instanceof Holder h))return;e.setCancelled(true);if(!(e.getWhoClicked() instanceof Player p))return;int slot=e.getRawSlot();if(slot<0||slot>=e.getView().getTopInventory().getSize())return;List<String>a=h.actions.get(slot);if(a!=null)plugin.actions().execute(p,a,false);}
  @EventHandler(priority=EventPriority.HIGHEST)public void drag(InventoryDragEvent e){if(e.getView().getTopInventory().getHolder() instanceof Holder)e.setCancelled(true);}
  private String resolve(Player p,String s){String x=s==null?"":s.replace("%player%",p.getName());if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))x=PlaceholderAPI.setPlaceholders(p,x);return x;}
  private static int norm(int n){n=Math.max(9,Math.min(54,n));return n%9==0?n:Math.min(54,(n/9+1)*9);}private static List<Integer>parse(ConfigurationSection s,int size){LinkedHashSet<Integer>r=new LinkedHashSet<>();if(s.contains("slot"))r.add(s.getInt("slot"));List<?>raw=s.getList("slots");if(raw!=null)for(Object o:raw){String t=String.valueOf(o);if(t.contains("-")){String[]p=t.split("-",2);try{int a=Integer.parseInt(p[0]),b=Integer.parseInt(p[1]);for(int i=Math.min(a,b);i<=Math.max(a,b);i++)r.add(i);}catch(Exception ignored){}}else try{r.add(Integer.parseInt(t));}catch(Exception ignored){}}r.removeIf(i->i<0||i>=size);return new ArrayList<>(r);}private static final class Holder implements InventoryHolder{final String id;final Map<Integer,List<String>>actions=new HashMap<>();Inventory inv;Holder(String id){this.id=id;}public Inventory getInventory(){return inv;}}
}
