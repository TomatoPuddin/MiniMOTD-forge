package xyz.jpenilla.minimotd.forge;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.minecraft.util.text.ITextComponent;

public class TextUtils {
    public static ITextComponent toNative(Component adventure) {
        return getComponentFromJson(ITextComponent.Serializer.GSON.toJsonTree(adventure));
    }
    public static ITextComponent getComponentFromJson(JsonElement json) {
        return ITextComponent.Serializer.GSON.fromJson(json, ITextComponent.class);
    }
}
