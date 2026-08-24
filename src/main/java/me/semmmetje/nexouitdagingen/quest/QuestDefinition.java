package me.semmmetje.nexouitdagingen.quest;

import java.util.List;

public record QuestDefinition(
    String id,
    QuestCategory category,
    String name,
    QuestType type,
    String target,
    long amount,
    String material,
    List<String> lore,
    List<String> rewards,
    String placeholder,
    String operator,
    String value
) {}
