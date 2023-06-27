package ru.xikki.plugins.onlinemodeswitcher.entities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.temporary.MinimalInjector;
import com.comphenix.protocol.injector.temporary.TemporaryPlayerFactory;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.xikki.plugins.onlinemodeswitcher.OnlineModeSwitcher;
import ru.xikki.plugins.onlinemodeswitcher.utils.EncryptionUtils;
import ru.xikki.plugins.onlinemodeswitcher.utils.MojangUtils;

import javax.crypto.SecretKey;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LoginClient {

	private final Player player;
	private final String name;

	private SecretKey secretKey;
	private WrappedGameProfile profile;
	private byte[] verifyToken;

	public LoginClient(@NotNull Player player, @NotNull String name) {
		this.player = player;
		this.name = name;
	}

	@NotNull
	public Player getPlayer() {
		return player;
	}

	@NotNull
	public String getName() {
		return name;
	}

	@Nullable
	public byte[] getVerifyToken() {
		return verifyToken;
	}

	@Nullable
	public SecretKey getSecretKey() {
		return secretKey;
	}

	public void kick(@NotNull String message) {
		PacketContainer packet = new PacketContainer(PacketType.Login.Server.DISCONNECT);
		packet.getChatComponents().writeSafely(0, WrappedChatComponent.fromLegacyText(message));
		ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, packet);
		this.player.kickPlayer("Disconnected");
	}

	@NotNull
	public Object getNetworkManager() {
		try {
			MinimalInjector injectorHolder = TemporaryPlayerFactory.getInjectorFromPlayer(this.player);
			Field injectorField = injectorHolder.getClass().getDeclaredField("injector");
			injectorField.setAccessible(true);
			Object injector = injectorField.get(injectorHolder);
			Field networkManagerField = injector.getClass().getDeclaredField("networkManager");
			networkManagerField.setAccessible(true);
			return networkManagerField.get(injector);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean setupEncryption(@NotNull SecretKey secretKey) {
		this.secretKey = secretKey;

		try {
			Object networkManager = this.getNetworkManager();
			Method setupEncryptionMethod = networkManager.getClass().getMethod("setupEncryption", SecretKey.class);
			setupEncryptionMethod.invoke(networkManager, this.secretKey);
			return true;
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean loadProfile() {
		String serverId = EncryptionUtils.getServerHash("", OnlineModeSwitcher.getInstance().getServerKeys().getPublic(), secretKey);
		this.profile = MojangUtils.hasJoined(this.name, serverId);
		return this.profile != null;
	}

	public void injectProfile() {
		if (this.profile == null)
			return;
		Object networkManager = this.getNetworkManager();
		try {
			Field uuidField = networkManager.getClass().getField("spoofedUUID");
			uuidField.set(networkManager, this.profile.getUUID());
			if (!this.profile.getProperties().isEmpty()) {
				Class<?> type = this.profile.getProperties().values().stream().findFirst().get().getHandleType();
				Object array = Array.newInstance(type, this.profile.getProperties().size());
				int index = 0;
				for (WrappedSignedProperty property : this.profile.getProperties().values())
					Array.set(array, index++, property.getHandle());
				Field propertiesField = networkManager.getClass().getField("spoofedProfile");
				propertiesField.set(networkManager, array);
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void startEncryption() {
		this.verifyToken = EncryptionUtils.generateVerifyToken();

		PacketContainer packet = new PacketContainer(PacketType.Login.Server.ENCRYPTION_BEGIN);
		packet.getStrings().writeSafely(0, "");
		packet.getByteArrays().writeSafely(0, OnlineModeSwitcher.getInstance().getServerKeys().getPublic().getEncoded());
		packet.getByteArrays().writeSafely(1, this.verifyToken);

		ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, packet);
	}

}
