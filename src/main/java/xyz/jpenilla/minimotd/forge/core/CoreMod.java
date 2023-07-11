package xyz.jpenilla.minimotd.forge.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import xyz.jpenilla.minimotd.common.Constants;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name(Constants.PluginMetadata.NAME)
public class CoreMod implements IFMLLoadingPlugin {

    public CoreMod() {
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
