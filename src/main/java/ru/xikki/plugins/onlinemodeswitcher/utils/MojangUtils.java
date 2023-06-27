package ru.xikki.plugins.onlinemodeswitcher.utils;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class MojangUtils {

	public static final String HAS_JOINED_REQUEST_URL = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s";

	@Nullable
	public static WrappedGameProfile hasJoined(@NotNull String name, @NotNull String hash) {
		String urlRaw = String.format(HAS_JOINED_REQUEST_URL, name, hash);
		try {
			URL url = new URL(urlRaw);
			URLConnection connection = url.openConnection();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				StringBuilder responseBuilder = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null)
					responseBuilder.append(line);
				if (responseBuilder.isEmpty())
					return null;
				JsonObject object = JsonParser.parseString(responseBuilder.toString()).getAsJsonObject();
				if (!object.has("id") || !object.has("name"))
					return null;
				UUID uuid = UUID.fromString(object.get("id").getAsString().replaceFirst(
						"(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
				));
				name = object.get("name").getAsString();
				WrappedGameProfile profile = new WrappedGameProfile(uuid, name);
				if (object.has("properties"))
					object.getAsJsonArray("properties").forEach((jsonElement) -> {
						if (!jsonElement.isJsonObject())
							return;
						JsonObject propertyData = jsonElement.getAsJsonObject();

						String propertyName = propertyData.get("name").getAsString();
						String propertyValue = propertyData.get("value").getAsString();
						String propertySignature = propertyData.get("signature").getAsString();

						profile.getProperties().put(propertyName, new WrappedSignedProperty(propertySignature, propertyValue, propertySignature));
					});
				return profile;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
