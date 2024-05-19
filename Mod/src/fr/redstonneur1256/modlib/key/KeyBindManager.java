package fr.redstonneur1256.modlib.key;

import arc.Core;
import arc.KeyBinds;

/**
 * Helper class for {@link KeyBinds} allowing to easily register modded keybindings, it is recommended to do an enum
 * like {@link mindustry.input.Binding} to easily manage your keybindings
 */
public class KeyBindManager {

    public static void registerKeyBinds(KeyBinds.KeyBind... binds) {
        // silently fail instead of crashing if a mod calls the method before the game is restarted
        if (Core.keybinds instanceof KeyBindAccessor) {
            ((KeyBindAccessor) Core.keybinds).registerKeyBinds(binds);
        }
    }

}
