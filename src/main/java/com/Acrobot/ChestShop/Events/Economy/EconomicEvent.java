package com.Acrobot.ChestShop.Events.Economy;

import org.bukkit.event.Event;

public abstract class EconomicEvent extends Event {

    private boolean handled = false;

    /**
     * Get whether this event was successfully handled by a listener
     *
     * @return Whether the amount was successfully handled
     */
    public boolean wasHandled() {
        return handled;
    }

    /**
     * Set whether this event was successfully handled by a listener
     *
     * @param handled Whether the amount was successfully handled
     */
    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}
