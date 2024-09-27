package com.Acrobot.ChestShop.Listeners.Block;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperBlockDestroy implements Listener {
    private final SignBreak signBreak;

    public PaperBlockDestroy(SignBreak signBreak) {
        this.signBreak = signBreak;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSign(BlockDestroyEvent event) {
        signBreak.handlePhysicsBreak(event.getBlock());
    }
}
