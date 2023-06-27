<img align="right" src="https://media.discordapp.net/attachments/945691411435622453/1123287838461542491/65456.png" height="140" width="140">

# Online Mode Switcher

Online mode switcher - api plugin that allows you to enable online
mode for current player on cracked minecraft server

## Dependencies

- ### [ProtocolLib](https://github.com/dmulloy2/ProtocolLib) (5.0.0 and above)
- ### [PaperMC](https://papermc.io/) (1.19.3 and above)

## Usage

All that you need to switch online mode for current player 
is to handle LoginStartEvent

```Java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import ru.xikki.plugins.onlinemodeswitcher.events.LoginStartEvent;

public class TestListener implements Listener {

	@EventHandler
	public void onPlayerLogin(@NotNull LoginStartEvent event) {
		//Check to see that this player needs to enable online mode
		event.setOnlineMode(true);
	}
	
}
```
If player logged from cracked account, it will be kicked by `Invalid Session` reason

## Examples

Server is running in offline mode<br>
<img src="https://media.discordapp.net/attachments/945691411435622453/1123284009296859156/image.png?width=1080&height=55">

But I am on the server as a licensed player (skin and heads in the tab)
<img src="https://media.discordapp.net/attachments/945691411435622453/1123284453087776788/image.png?width=959&height=584">

If someone tries to login on the server from cracked account
with my nickname, he will be kicked
<img src="https://media.discordapp.net/attachments/945691411435622453/1123285127380865124/image.png?width=960&height=584">

## Warning

The plugin has not been properly tested, so most likely there are critical errors
