package natureoverhaul;

import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Olivier on 28/08/2014.
 * Builds a configuration file in the config folder with default name and case-sensitive categories
 */
public class NOConfiguration extends Configuration{
    public NOConfiguration(FMLPreInitializationEvent event){
        super(event.getSuggestedConfigurationFile(), true);
    }

    /**
     * Creates a boolean property with minimal comment.
     *
     * @param category Category of the property.
     * @param name Name of the property.
     * @param defaultValue Default value of the property.
     * @return The value of the new boolean property.
     */
    public boolean getBoolean(String category, String name, boolean defaultValue){
        Property prop = this.get(category, name, defaultValue);
        prop.setLanguageKey(name);
        prop.comment = "[default: " + defaultValue + "]";
        return prop.getBoolean(defaultValue);
    }

    /**
     * Creates a integer property with no specific range of values and minimal comment
     *
     * @param category Category of the property.
     * @param name Name of the property.
     * @param defaultValue Default value of the property.
     * @return The value of the new integer property.
     */
    public int getInt(String category, String name, int defaultValue){
        Property prop = this.get(category, name, defaultValue);
        prop.setLanguageKey(name);
        prop.comment = "[default: " + defaultValue + "]";
        return prop.getInt(defaultValue);
    }

    /**
     *
     * @return a list of all current parent categories as elements
     */
    @SideOnly(Side.CLIENT)
    public List<IConfigElement> getElements(){
        Set<String> names = this.getCategoryNames();
        List<IConfigElement> categories = new ArrayList<IConfigElement>(names.size());
        for(String name:names){
            ConfigCategory cat = this.getCategory(name);
            if(!cat.isChild())
                categories.add(new ConfigElement(this.getCategory(name)));
        }
        return categories;
    }
}
