package xyz.jpenilla.minimotd.forge;

import net.kyori.adventure.text.Component;
import net.minecraft.util.text.ITextComponent;

public class TextUtils {
    public static ITextComponent toNative(Component adventure) {
        return ITextComponent.Serializer.getComponentFromJson(ITextComponent.Serializer.GSON.toJsonTree(adventure));
    }
}
