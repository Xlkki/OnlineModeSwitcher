package ru.xikki.plugins.onlinemodeswitcher.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.xikki.plugins.onlinemodeswitcher.OnlineModeSwitcher;
import ru.xikki.plugins.onlinemodeswitcher.entities.LoginClient;
import ru.xikki.plugins.onlinemodeswitcher.utils.EncryptionUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class EncryptionPacketListener extends PacketAdapter {

	private static final String KICK_REASON = "§cInvalid Session";

	public EncryptionPacketListener(Plugin plugin) {
		super(plugin, PacketType.Login.Client.ENCRYPTION_BEGIN);
	}

	@Override
	public void onPacketReceiving(@NotNull PacketEvent event) {
		if (event.isCancelled())
			return;
		event.setCancelled(true);
		Player sender = event.getPlayer();
		PacketContainer packet = event.getPacket();
		InetSocketAddress address = sender.getAddress();

		if (address == null)
			return;
		String sessionKey = address.toString();
		LoginClient client = OnlineModeSwitcher.getInstance().getClients().remove(sessionKey);
		if (client == null)
			return;

		byte[] originalToken = client.getVerifyToken();
		if (originalToken == null) {
			client.kick(EncryptionPacketListener.KICK_REASON);
			return;
		}

		byte[] sharedSecret = packet.getByteArrays().readSafely(0);
		if (sharedSecret == null) {
			client.kick(EncryptionPacketListener.KICK_REASON);
			return;
		}
		byte[] encryptedToken = packet.getByteArrays().readSafely(1);
		if (encryptedToken == null) {
			client.kick(EncryptionPacketListener.KICK_REASON);
			return;
		}
		byte[] decryptedToken = EncryptionUtils.decrypt(encryptedToken, OnlineModeSwitcher.getInstance().getServerKeys().getPrivate());
		if (!Arrays.equals(originalToken, decryptedToken)) {
			client.kick(EncryptionPacketListener.KICK_REASON);
			return;
		}
		SecretKey secretKey = new SecretKeySpec(EncryptionUtils.decrypt(sharedSecret, OnlineModeSwitcher.getInstance().getServerKeys().getPrivate()), "AES");

		if (!client.setupEncryption(secretKey)) {
			client.kick(EncryptionPacketListener.KICK_REASON);
			return;
		}
		if (!client.loadProfile()) {
			client.kick(EncryptionPacketListener.KICK_REASON);
			return;
		}
		client.injectProfile();

		PacketContainer container = new PacketContainer(PacketType.Login.Client.START);
		container.getStrings().write(0, client.getName());
		ProtocolLibrary.getProtocolManager().receiveClientPacket(sender, container, false);
	}
}
