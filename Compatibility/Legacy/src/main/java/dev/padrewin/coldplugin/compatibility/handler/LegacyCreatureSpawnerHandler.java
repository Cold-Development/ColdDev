package dev.padrewin.coldplugin.compatibility.handler;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public class LegacyCreatureSpawnerHandler implements CreatureSpawnerHandler {

    @Override
    public EntityType getSpawnedType(CreatureSpawner creatureSpawner) {
        return creatureSpawner.getSpawnedType();
    }

    @Override
    public void setSpawnedType(CreatureSpawner creatureSpawner, EntityType entityType) {
        creatureSpawner.setSpawnedType(entityType);
    }

    @Override
    public void setDelay(CreatureSpawner creatureSpawner, int delay) {
        creatureSpawner.setDelay(delay);
    }

}
