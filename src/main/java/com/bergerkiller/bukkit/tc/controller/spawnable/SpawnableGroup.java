package com.bergerkiller.bukkit.tc.controller.spawnable;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.EntityType;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.utils.LogicUtil;
import com.bergerkiller.bukkit.common.utils.ParseUtil;
import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.properties.TrainPropertiesStore;

/**
 * Stores information about a train prior to spawning
 */
public class SpawnableGroup {
    private final List<SpawnableMember> members = new ArrayList<SpawnableMember>();
    private final ConfigurationNode config;
    private CenterMode centerMode = CenterMode.NONE;

    public SpawnableGroup() {
        this.config = TrainPropertiesStore.getDefaultsByName("spawner").clone();
    }

    /**
     * Gets the train group configuration to be applied to the train's properties
     * after spawning.
     * 
     * @return train configuration
     */
    public ConfigurationNode getConfig() {
        return this.config;
    }

    /**
     * Gets the way the train should be centered when spawning
     * 
     * @return center mode
     */
    public CenterMode getCenterMode() {
        return this.centerMode;
    }

    private void addCenterMode(CenterMode mode) {
        if (this.centerMode == CenterMode.NONE || this.centerMode == mode) {
            this.centerMode = mode;
        } else {
            this.centerMode = CenterMode.MIDDLE;
        }
    }

    /**
     * Gets all the Minecarts part of this group
     * 
     * @return list of spawnable members
     */
    public List<SpawnableMember> getMembers() {
        return this.members;
    }

    /**
     * Adds a new Minecart to the end of this group
     * 
     * @param config
     */
    public void addMember(ConfigurationNode config) {
        this.members.add(new SpawnableMember(this, config));
    }

    private void addStandardMember(EntityType type) {
        ConfigurationNode cartConfig = new ConfigurationNode();
        cartConfig.set("entityType", type);
        this.addMember(cartConfig);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("{").append("center=").append(this.centerMode);
        str.append(", types=[");
        boolean first = true;
        for (SpawnableMember member : this.members) {
            if (first) {
                first = false;
            } else {
                str.append(", ");
            }
            str.append(member.toString());
        }
        str.append("]}");
        return str.toString();
    }

    /**
     * Parses the contents of a types-encoded String. This is a String token
     * in the same format as is used on the 3rd/4th lines on spawner signs.
     * 
     * @param typesText
     * @return spawnable group parsed from the types text
     */
    public static SpawnableGroup parse(String typesText) {
        SpawnableGroup result = new SpawnableGroup();
        StringBuilder amountBuilder = new StringBuilder();

        for (int typeTextIdx = 0; typeTextIdx < typesText.length(); typeTextIdx++) {
            // First check centering mode changing characters
            char c = typesText.charAt(typeTextIdx);
            if (LogicUtil.containsChar(c, "]>)}")) {
                result.addCenterMode(CenterMode.LEFT);
                continue;
            }
            if (LogicUtil.containsChar(c, "[<({")) {
                result.addCenterMode(CenterMode.RIGHT);
                continue;
            }

            // Attempt to parse a saved train name
            String name = TrainCarts.plugin.getSavedTrains().findName(typesText.substring(typeTextIdx));
            int countAdded = 0;
            if (name != null) {
                typeTextIdx += name.length() - 1;
                ConfigurationNode savedTrainConfig = TrainCarts.plugin.getSavedTrains().getConfig(name);
                for (String key : savedTrainConfig.getKeys()) {
                    if (key.equals("carts")) continue;
                    result.config.set(key, savedTrainConfig.get(key));
                }
                List<ConfigurationNode> cartConfigList = savedTrainConfig.getNodeList("carts");
                for (int i = cartConfigList.size() - 1; i >= 0; i--) {
                    result.addMember(cartConfigList.get(i));
                    countAdded++;
                }
            } else if (c == 'm' || c == 'M') {
                result.addStandardMember(EntityType.MINECART);
                countAdded++;
            } else if (c == 's' || c == 'S') {
                result.addStandardMember(EntityType.MINECART_CHEST);
                countAdded++;
            } else if (c == 'p' || c == 'P') {
                result.addStandardMember(EntityType.MINECART_FURNACE);
                countAdded++;
            } else if (c == 'h' || c == 'H') {
                result.addStandardMember(EntityType.MINECART_HOPPER);
                countAdded++;
            } else if (c == 't' || c == 'T') {
                result.addStandardMember(EntityType.MINECART_TNT);
                countAdded++;
            } else if (c == 'e' || c == 'E') {
                result.addStandardMember(EntityType.MINECART_MOB_SPAWNER);
                countAdded++;
            } else if (c == 'c' || c == 'C') {
                result.addStandardMember(EntityType.MINECART_COMMAND);
                countAdded++;
            } else if (Character.isDigit(c)) {
                amountBuilder.append(c);
            }
            if (countAdded > 0 && amountBuilder.length() > 0) {
                // Multiply the amount added with the amount put in front
                int amount = ParseUtil.parseInt(amountBuilder.toString(), 1);
                amountBuilder.setLength(0);
                if (amount == 0) {
                    // Cancel adding
                    for (int i = 0; i < countAdded; i++) {
                        result.members.remove(result.members.size() - 1);
                    }
                } else if (amount > 1) {
                    // Duplicate to add multiple times
                    int startIdx = result.members.size() - countAdded;
                    for (int n = 0; n < amount - 1; n++) {
                        for (int i = 0; i < countAdded; i++) {
                            result.members.add(result.members.get(startIdx + i).clone());
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Ways of centering a train when spawning
     */
    public static enum CenterMode {
        NONE, MIDDLE, LEFT, RIGHT
    }
}
