package ru.xikki.plugins.onlinemodeswitcher.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

public class LoginStartEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final InetSocketAddress address;
	private final String name;
	private boolean onlineMode = false;
	private String kickReason = null;

	public LoginStartEvent(@NotNull InetSocketAddress address, @NotNull String name) {
		super(!Bukkit.isPrimaryThread());
		this.address = address;
		this.name = name;
	}

	@NotNull
	public InetSocketAddress getAddress() {
		return address;
	}

	@NotNull
	public String getName() {
		return name;
	}

	public boolean isOnlineMode() {
		return onlineMode;
	}

	public void setOnlineMode(boolean onlineMode) {
		this.onlineMode = onlineMode;
	}

	@Nullable
	public String getKickReason() {
		return kickReason;
	}

	public boolean isKicked() {
		return this.kickReason != null;
	}

	public void kick(@NotNull String kickReason) {
		this.kickReason = kickReason;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	@NotNull
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public boolean isCancelled() {
		return this.kickReason != null;
	}

	@Override
	public void setCancelled(boolean cancel) {
		throw new UnsupportedOperationException("Use LoginStartEvent#kick(Component) method");
	}

}
