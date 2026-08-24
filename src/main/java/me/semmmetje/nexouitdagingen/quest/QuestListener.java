package me.semmmetje.nexouitdagingen.quest;

import me.semmmetje.nexouitdagingen.NexoUitdagingenPlugin;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;

import java.util.*;

public final class QuestListener implements Listener {
    private final NexoUitdagingenPlugin plugin;
    public QuestListener(NexoUitdagingenPlugin plugin){this.plugin=plugin;}
    private void add(Player p,QuestType t,String target,long amount){plugin.quests().addProgress(p,t,target,amount);}
    @EventHandler(ignoreCancelled=true) public void breakBlock(BlockBreakEvent e){add(e.getPlayer(),QuestType.BREAK_BLOCK,e.getBlock().getType().name(),1);}
    @EventHandler(ignoreCancelled=true) public void placeBlock(BlockPlaceEvent e){add(e.getPlayer(),QuestType.PLACE_BLOCK,e.getBlockPlaced().getType().name(),1);}
    @EventHandler(ignoreCancelled=true) public void kill(EntityDeathEvent e){Player k=e.getEntity().getKiller();if(k==null)return;if(e.getEntity() instanceof Player)add(k,QuestType.KILL_PLAYER,"PLAYER",1);else add(k,QuestType.KILL_MOB,e.getEntityType().name(),1);}
    @EventHandler(ignoreCancelled=true) public void fish(PlayerFishEvent e){if(e.getState()==PlayerFishEvent.State.CAUGHT_FISH){String target=e.getCaught()==null?"ANY":e.getCaught().getType().name();add(e.getPlayer(),QuestType.FISH,target,1);}}
    @EventHandler(ignoreCancelled=true) public void craft(CraftItemEvent e){if(!(e.getWhoClicked() instanceof Player p)||e.getCurrentItem()==null)return;int amount=Math.max(1,e.getCurrentItem().getAmount());add(p,QuestType.CRAFT,e.getCurrentItem().getType().name(),amount);}
    @EventHandler(ignoreCancelled=true) public void furnace(FurnaceExtractEvent e){add(e.getPlayer(),QuestType.SMELT,e.getItemType().name(),e.getItemAmount());add(e.getPlayer(),QuestType.COOK,e.getItemType().name(),e.getItemAmount());}
    @EventHandler(ignoreCancelled=true) public void breed(EntityBreedEvent e){if(e.getBreeder() instanceof Player p)add(p,QuestType.BREED,e.getEntityType().name(),1);}
    @EventHandler(ignoreCancelled=true) public void tame(EntityTameEvent e){if(e.getOwner() instanceof Player p)add(p,QuestType.TAME,e.getEntityType().name(),1);}
    @EventHandler(ignoreCancelled=true) public void shear(PlayerShearEntityEvent e){add(e.getPlayer(),QuestType.SHEAR,e.getEntity().getType().name(),1);}
    @EventHandler(ignoreCancelled=true) public void bucket(PlayerBucketFillEvent e){if(e.getBucket()==Material.BUCKET)add(e.getPlayer(),QuestType.MILK,"ANY",1);}
    @EventHandler(ignoreCancelled=true) public void enchant(EnchantItemEvent e){add(e.getEnchanter(),QuestType.ENCHANT,e.getItem().getType().name(),1);}
    @EventHandler(ignoreCancelled=true) public void anvil(InventoryClickEvent e){if(!(e.getWhoClicked() instanceof Player p))return;if(e.getInventory() instanceof AnvilInventory && e.getRawSlot()==2 && e.getCurrentItem()!=null)add(p,QuestType.ANVIL,e.getCurrentItem().getType().name(),1);}
    @EventHandler(ignoreCancelled=true) public void consume(PlayerItemConsumeEvent e){add(e.getPlayer(),QuestType.CONSUME,e.getItem().getType().name(),1);}
    @EventHandler(ignoreCancelled=true) public void pickup(EntityPickupItemEvent e){if(e.getEntity() instanceof Player p)add(p,QuestType.PICKUP,e.getItem().getItemStack().getType().name(),e.getItem().getItemStack().getAmount());}
    @EventHandler(ignoreCancelled=true) public void drop(PlayerDropItemEvent e){ItemStack s=e.getItemDrop().getItemStack();add(e.getPlayer(),QuestType.DROP,s.getType().name(),s.getAmount());}
    @EventHandler(ignoreCancelled=true) public void trade(InventoryClickEvent e){if(!(e.getWhoClicked() instanceof Player p))return;if(e.getInventory() instanceof MerchantInventory && e.getRawSlot()==2 && e.getCurrentItem()!=null)add(p,QuestType.TRADE,e.getCurrentItem().getType().name(),1);}
    @EventHandler(ignoreCancelled=true) public void damage(EntityDamageByEntityEvent e){Player attacker=null;if(e.getDamager() instanceof Player p)attacker=p;else if(e.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player p)attacker=p;if(attacker!=null)add(attacker,QuestType.DAMAGE_DEALT,e.getEntityType().name(),Math.max(1,Math.round(e.getFinalDamage())));if(e.getEntity() instanceof Player victim)add(victim,QuestType.DAMAGE_TAKEN,e.getCause().name(),Math.max(1,Math.round(e.getFinalDamage())));}
    @EventHandler(ignoreCancelled=true) public void heal(EntityRegainHealthEvent e){if(e.getEntity() instanceof Player p)add(p,QuestType.HEAL,e.getRegainReason().name(),Math.max(1,Math.round(e.getAmount())));}
    @EventHandler(ignoreCancelled=true) public void xp(PlayerExpChangeEvent e){if(e.getAmount()>0)add(e.getPlayer(),QuestType.XP_GAIN,"ANY",e.getAmount());}
    @EventHandler public void level(PlayerLevelChangeEvent e){if(e.getNewLevel()>e.getOldLevel())add(e.getPlayer(),QuestType.LEVEL_GAIN,"ANY",e.getNewLevel()-e.getOldLevel());}
    @EventHandler public void death(PlayerDeathEvent e){add(e.getEntity(),QuestType.DEATH,"ANY",1);}
    @EventHandler public void bed(PlayerBedEnterEvent e){if(e.getBedEnterResult()==PlayerBedEnterEvent.BedEnterResult.OK)add(e.getPlayer(),QuestType.SLEEP,"ANY",1);}
    @EventHandler public void join(PlayerJoinEvent e){plugin.quests().ensureAssignments(e.getPlayer());add(e.getPlayer(),QuestType.JOIN,"ANY",1);plugin.quests().evaluatePlaceholderQuests(e.getPlayer());}
    @EventHandler(ignoreCancelled=true) public void advancement(PlayerAdvancementDoneEvent e){add(e.getPlayer(),QuestType.ADVANCEMENT,e.getAdvancement().getKey().toString(),1);}
    @EventHandler(ignoreCancelled=true,priority=EventPriority.MONITOR) public void move(PlayerMoveEvent e){if(e.getTo()==null||e.getFrom().getWorld()!=e.getTo().getWorld())return;double distance=e.getFrom().distance(e.getTo());if(distance<=0||distance>25)return;Player p=e.getPlayer();double dy=e.getTo().getY()-e.getFrom().getY();if(dy>0.35&&p.getVehicle()==null&&!p.isFlying()&&!p.isGliding()&&!p.isSwimming())add(p,QuestType.JUMP,"ANY",1);long meters=Math.max(0,Math.round(distance));if(meters==0)return;if(p.isGliding()||p.isFlying())add(p,QuestType.FLY,"ANY",meters);else if(p.isSwimming())add(p,QuestType.SWIM,"ANY",meters);else if(p.isSprinting())add(p,QuestType.SPRINT,"ANY",meters);else if(p.getVehicle()==null)add(p,QuestType.WALK,"ANY",meters);}
    @EventHandler(ignoreCancelled=true) public void vehicle(VehicleMoveEvent e){if(e.getFrom().getWorld()!=e.getTo().getWorld())return;double d=e.getFrom().distance(e.getTo());if(d<=0||d>50)return;long m=Math.max(1,Math.round(d));for(Entity passenger:e.getVehicle().getPassengers())if(passenger instanceof Player p){if(e.getVehicle() instanceof Boat)add(p,QuestType.BOAT_TRAVEL,"ANY",m);else if(e.getVehicle() instanceof Minecart)add(p,QuestType.MINECART_TRAVEL,"ANY",m);else if(e.getVehicle() instanceof AbstractHorse)add(p,QuestType.HORSE_TRAVEL,"ANY",m);}}
    @EventHandler public void command(PlayerCommandPreprocessEvent e){String raw=e.getMessage().substring(1).split("\\s+")[0].toLowerCase(Locale.ROOT);add(e.getPlayer(),QuestType.COMMAND,raw,1);}
}
