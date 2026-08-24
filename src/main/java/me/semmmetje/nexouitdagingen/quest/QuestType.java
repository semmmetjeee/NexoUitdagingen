package me.semmmetje.nexouitdagingen.quest;

import java.util.Locale;

public enum QuestType {
  BREAK_BLOCK,
  PLACE_BLOCK,
  KILL_MOB,
  KILL_PLAYER,
  FISH,
  CRAFT,
  SMELT,
  COOK,
  BREED,
  TAME,
  SHEAR,
  MILK,
  ENCHANT,
  ANVIL,
  CONSUME,
  PICKUP,
  DROP,
  TRADE,
  DAMAGE_DEALT,
  DAMAGE_TAKEN,
  HEAL,
  WALK,
  SPRINT,
  SWIM,
  FLY,
  BOAT_TRAVEL,
  MINECART_TRAVEL,
  HORSE_TRAVEL,
  XP_GAIN,
  LEVEL_GAIN,
  DEATH,
  JUMP,
  SLEEP,
  JOIN,
  ADVANCEMENT,
  COMMAND,
  PLACEHOLDER;

  public static QuestType parse(String raw) {
    return valueOf(raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_'));
  }
}
