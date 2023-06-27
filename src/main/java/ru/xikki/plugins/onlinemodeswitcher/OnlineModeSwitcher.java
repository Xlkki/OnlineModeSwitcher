package ru.xikki.plugins.onlinemodeswitcher;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.xikki.plugins.onlinemodeswitcher.entities.LoginClient;
import ru.xikki.plugins.onlinemodeswitcher.listeners.EncryptionPacketListener;
import ru.xikki.plugins.onlinemodeswitcher.listeners.LoginPacketListener;
import ru.xikki.plugins.onlinemodeswitcher.utils.EncryptionUtils;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

public final class OnlineModeSwitcher extends JavaPlugin {

	private static OnlineModeSwitcher instance;

	private final Map<String, LoginClient> clients = new HashMap<>();
	private KeyPair serverKeys;

	@Override
	public void onLoad() {
		instance = this;

		this.serverKeys = EncryptionUtils.generateKeys();
	}

	@Override
	public void onEnable() {
		if (Bukkit.getServer().getOnlineMode()) {
			this.getLogger().warning("Server working in online mode. Disabling plugin...");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		ProtocolLibrary.getProtocolManager().addPacketListener(new LoginPacketListener(this));
		ProtocolLibrary.getProtocolManager().addPacketListener(new EncryptionPacketListener(this));
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}

	@NotNull
	public Map<String, LoginClient> getClients() {
		return clients;
	}

	@NotNull
	public KeyPair getServerKeys() {
		return serverKeys;
	}

	@NotNull
	public static OnlineModeSwitcher getInstance() {
		return instance;
	}
}
