package xyz.jpenilla.minimotd.forge;

import net.kyori.adventure.text.Component;

public class TextUtils {
    public static net.minecraft.network.chat.Component toNative(Component adventure) {
        return net.minecraft.network.chat.Component.Serializer.fromJson(net.minecraft.network.chat.Component.Serializer.GSON.toJsonTree(adventure));
    }
}
