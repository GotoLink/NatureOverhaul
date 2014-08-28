package natureoverhaul;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.Set;

/**
 * Created by Olivier on 28/08/2014.
 * A gui factory for dummies, only pass another GuiScreen class to display
 */
@SideOnly(Side.CLIENT)
public class ConfigGuiHandler implements IModGuiFactory {
    public ConfigGuiHandler(){}

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return GuiClass.class;
    }

    public static class GuiClass extends GuiConfig{
        public GuiClass(GuiScreen parentScreen) {
            super(parentScreen, NatureOverhaul.getConfigElements(), "natureoverhaul", true, false, getAbridgedConfigPath(NatureOverhaul.getConfigPath()));
        }
    }

    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}
