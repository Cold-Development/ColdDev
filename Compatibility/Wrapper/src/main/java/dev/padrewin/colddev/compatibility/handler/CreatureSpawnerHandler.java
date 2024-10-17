package dev.padrewin.colddev.compatibility.handler;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public interface CreatureSpawnerHandler {

    EntityType getSpawnedType(CreatureSpawner creatureSpawner);

    void setSpawnedType(CreatureSpawner creatureSpawner, EntityType entityType);

    void setDelay(CreatureSpawner creatureSpawner, int delay);

}
