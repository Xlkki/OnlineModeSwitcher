package ru.xikki.plugins.onlinemodeswitcher.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.xikki.plugins.onlinemodeswitcher.OnlineModeSwitcher;
import ru.xikki.plugins.onlinemodeswitcher.entities.LoginClient;
import ru.xikki.plugins.onlinemodeswitcher.events.LoginStartEvent;

import java.net.InetSocketAddress;

public class LoginPacketListener extends PacketAdapter {

	public LoginPacketListener(@NotNull Plugin plugin) {
		super(plugin, PacketType.Login.Client.START);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		if (event.isCancelled())
			return;
		Player sender = event.getPlayer();
		PacketContainer container = event.getPacket();
		InetSocketAddress address = sender.getAddress();
		if (address == null)
			return;
		String name = container.getStrings().readSafely(0);
		if (name == null)
			return;
		LoginClient client = new LoginClient(sender, name);

		LoginStartEvent startEvent = new LoginStartEvent(address, name);
		if (!startEvent.callEvent()) {
			client.kick(startEvent.getKickReason());
			return;
		}
		if (!startEvent.isOnlineMode())
			return;

		String sessionKey = address.toString();
		OnlineModeSwitcher.getInstance().getClients().put(sessionKey, client);

		event.setCancelled(true);
		client.startEncryption();
	}
}
