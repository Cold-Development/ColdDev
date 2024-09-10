package dev.padrewin.coldplugin.compatibility.handler;

import dev.padrewin.coldplugin.utils.NMSUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Shearable;
import org.bukkit.entity.Snowman;

@SuppressWarnings("removal")
public class CurrentShearedHandler implements ShearedHandler {

    @Override
    public boolean isSheared(LivingEntity shearable) {
        if (NMSUtil.isPaper() && shearable instanceof io.papermc.paper.entity.Shearable)
            return !((io.papermc.paper.entity.Shearable) shearable).readyToBeSheared();
        if (shearable instanceof Shearable)
            return ((Shearable) shearable).isSheared();
        if (shearable instanceof Snowman)
            return ((Snowman) shearable).isDerp();
        return false;
    }

    @Override
    public void setSheared(LivingEntity shearable, boolean sheared) {
        if (shearable instanceof Shearable) ((Shearable) shearable).isSheared();
        else if (shearable instanceof Snowman) ((Snowman) shearable).isDerp();
    }

}
