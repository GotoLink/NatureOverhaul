package natureoverhaul;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
@TransformerExclusions(value={"natureoverhaul."})
public class NOFMLCorePlugin implements IFMLLoadingPlugin{

	@Override
	public String[] getLibraryRequestClass() {
		return null;
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {"natureoverhaul.NOTransformer"};
	}

	@Override
	public String getModContainerClass() {
		return "natureoverhaul.NatureOverhaul";
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {	}

}
