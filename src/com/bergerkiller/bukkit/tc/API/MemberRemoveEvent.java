package com.bergerkiller.bukkit.tc.API;

import org.bukkit.event.HandlerList;

import com.bergerkiller.bukkit.tc.MinecartMember;
import com.bergerkiller.bukkit.tc.Util;

public class MemberRemoveEvent extends MemberEvent {
	private static final long serialVersionUID = 1L;
    private static final HandlerList handlers = new HandlerList();
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

	public MemberRemoveEvent(final MinecartMember member) {
		super("MemberRemoveEvent", member);
	}
	
	public static void call(final MinecartMember member) {
		Util.call(new MemberRemoveEvent(member));
	}

}
