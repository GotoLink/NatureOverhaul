package natureoverhaul;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.io.IOException;
import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions(value={"natureoverhaul"})
@IFMLLoadingPlugin.Name(value="Nature Overhaul Plugin")
public class NOPlugin extends AccessTransformer implements IFMLLoadingPlugin {
    public NOPlugin() throws IOException {
        super("natureoverhaul_at.cfg");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return "natureoverhaul.NOPlugin";
    }
}
