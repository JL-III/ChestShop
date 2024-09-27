package com.Acrobot.ChestShop.Configuration;

import com.Acrobot.Breeze.Configuration.Annotations.ConfigurationComment;
import com.Acrobot.Breeze.Configuration.Annotations.Parser;
import com.Acrobot.Breeze.Configuration.Annotations.PrecededBySpace;
import com.Acrobot.Breeze.Configuration.Configuration;
import com.Acrobot.Breeze.Configuration.ValueParser;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Utils.Security;
import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Acrobot
 */
public class Properties {

    static {
        Configuration.registerParser("StringSet", new ValueParser() {
            @Override
            public <T> Object parseToJava(Class<T> type, Object object) {
                if (object instanceof Collection) {
                    return new LinkedHashSet<>((Collection<String>) object);
                }
                return object;
            }
        });
        Configuration.registerParser("MaterialSet", new ValueParser() {
            @Override
            public <T> Object parseToJava(Class<T> type, Object object) {
                if (object instanceof Collection) {
                    EnumSet<Material> set = EnumSet.noneOf(Material.class);
                    for (Object o : (Collection) object) {
                        if (o instanceof Material) {
                            set.add((Material) o);
                        } else if (o instanceof String) {
                            Material m = Material.getMaterial(((String) o).toUpperCase(Locale.ROOT));
                            if (m != null) {
                                set.add(m);
                            } else {
                                ChestShop.getBukkitLogger().log(Level.WARNING, o + " is not a valid Material name in the config!");
                            }
                        }
                    }
                    return set;
                }
                return object;
            }
        });
        Configuration.registerParser("BigDecimal", new ValueParser() {
            @Override
            public String parseToYAML(Object object) {
                if (object instanceof BigDecimal) {
                    return object.toString();
                }
                return super.parseToYAML(object);
            }

            @Override
            public <T> Object parseToJava(Class<T> type, Object object) {
                if (object instanceof Double) {
                    return BigDecimal.valueOf((Double) object);
                } else if (object instanceof Long) {
                    return BigDecimal.valueOf((Long) object);
                } else if (object instanceof Integer) {
                    return BigDecimal.valueOf((Integer) object);
                }
                return object;
            }
        });
        Configuration.registerParser("UUID", new ValueParser() {
            @Override
            public String parseToYAML(Object object) {
                if (object instanceof UUID) {
                    return object.toString();
                }
                return super.parseToYAML(object);
            }

            @Override
            public <T> Object parseToJava(Class<T> type, Object object) {
                if (object instanceof String) {
                    return UUID.fromString((String) object);
                }
                return object;
            }
        });
    }

    @ConfigurationComment("Should the plugin log some messages that are useful for debugging?")
    public static boolean DEBUG = false;

    @PrecededBySpace
    @ConfigurationComment("How large should the internal caches be?")
    public static int CACHE_SIZE = 1000;

    @PrecededBySpace
    @ConfigurationComment("The default language when the client's language can't be found.")
    public static String DEFAULT_LANGUAGE = "en";

    @ConfigurationComment("Should the plugin try to use a language file that matches the client's locale setting?")
    public static boolean USE_CLIENT_LOCALE = true;

    @ConfigurationComment("Should the plugin strip the colors from formatted price?")
    public static boolean STRIP_PRICE_COLORS = false;

    @PrecededBySpace
    @ConfigurationComment("What containers are allowed to hold a shop? (Only blocks with inventories work!)")
    @Parser("MaterialSet")
    public static Set<Material> SHOP_CONTAINERS = EnumSet.of(
            Material.CHEST,
            Material.TRAPPED_CHEST
    );

    @PrecededBySpace
    @ConfigurationComment("(In 1/1000th of a second) How often can a player use the shop sign?")
    public static int SHOP_INTERACTION_INTERVAL = 250;

    @ConfigurationComment("Do you want to block people in creative mode from using shops?")
    public static boolean IGNORE_CREATIVE_MODE = true;

    @ConfigurationComment("Do you want to block people who have access to a shop due to their permissions from using it? (owners are always ignored)")
    public static boolean IGNORE_ACCESS_PERMS = true;

    @ConfigurationComment("Can shop's chest be opened by owner with right-clicking a shop's sign?")
    public static boolean ALLOW_SIGN_CHEST_OPEN = false;

    @ConfigurationComment("If true and in 1.14+, the owner of a chest shop can click with a dye / ink sac to dye the sign.")
    public static boolean SIGN_DYING = true;

    @ConfigurationComment("If true, when you left-click your own shop sign you won't open chest's inventory, but instead you will start destroying the sign.")
    public static boolean ALLOW_LEFT_CLICK_DESTROYING = true;

    @PrecededBySpace
    @ConfigurationComment("If true, if the shop is empty, the sign is destroyed and put into the chest, so the shop isn't usable anymore.")
    public static boolean REMOVE_EMPTY_SHOPS = false;

    @ConfigurationComment("If true, if the REMOVE_EMPTY_SHOPS option is turned on, the chest is also destroyed.")
    public static boolean REMOVE_EMPTY_CHESTS = false;

    @ConfigurationComment("A list of worlds in which to remove empty shops with the previous config. Case sensitive. An empty list means all worlds.")
    @Parser("StringSet")
    public static Set<String> REMOVE_EMPTY_WORLDS = new LinkedHashSet<>(Arrays.asList("world1", "world2"));

    @ConfigurationComment("How many decimal places are allowed at a maximum for prices?")
    public static int PRICE_PRECISION = 2;

    @ConfigurationComment("This makes sure that the UUIDs of player shop accounts match the server's online-mode setting. Disabling this might lead to issues with offline players and is therefore unsupported!")
    public static boolean ENSURE_CORRECT_PLAYER_ID = true;

    @ConfigurationComment("This regexp validates the name of the player. If the name doesn't match, the player will neither be able to create a valid shop sign, nor buy/sell from a shop.\n" +
            "Note for Bedrock support: If you have Floodgate on your server, you should set this regexp to ^\\\\.?\\\\w+$ and ENSURE_CORRECT_PLAYERID to false\n" +
            "If your Floodgate prefix is not a dot, then change the first . in the regexp (the one before the question mark) to whatever your prefix is.")
    public static String VALID_PLAYER_NAME_REGEXP = "^\\w+$";

    @PrecededBySpace
    @ConfigurationComment("Should we block shops that sell things for more than they buy? (This prevents newbies from creating shops that would be exploited)")
    public static boolean BLOCK_SHOPS_WITH_SELL_PRICE_HIGHER_THAN_BUY_PRICE = true;

    @PrecededBySpace
    @ConfigurationComment("Maximum amount of items that can be bought/sold at a shop. Default 3456 is a double chest of 64 stacks.")
    public static int MAX_SHOP_AMOUNT = 3456;

    @PrecededBySpace
    @ConfigurationComment("Do you want to allow other players to build a shop on a block where there's one already?")
    public static boolean ALLOW_MULTIPLE_SHOPS_AT_ONE_BLOCK = false;

    @ConfigurationComment("Can shops be used even when the buyer/seller doesn't have enough items, space or money? (The price will be scaled adequately to the item amount)")
    public static boolean ALLOW_PARTIAL_TRANSACTIONS = true;

    @ConfigurationComment("Can '?' be put in place of item name in order for the sign to be auto-filled?")
    public static boolean ALLOW_AUTO_ITEM_FILL = true;

    @PrecededBySpace
    @ConfigurationComment("Do you want to show \"Out of stock\" messages?")
    public static boolean SHOW_MESSAGE_OUT_OF_STOCK = true;
    @ConfigurationComment("Do you want to show \"Full shop\" messages?")
    public static boolean SHOW_MESSAGE_FULL_SHOP = true;
    @ConfigurationComment("How many seconds do you want to wait before showing notifications for the same shop to the owner again?")
    public static long NOTIFICATION_MESSAGE_COOLDOWN = 10;

    @PrecededBySpace
    @ConfigurationComment("Can players hide the \"Out of stock\" messages with /cstoggle?")
    public static boolean CSTOGGLE_TOGGLES_OUT_OF_STOCK = false;
    @ConfigurationComment("Can players hide the \"Full shop\" messages with /cstoggle?")
    public static boolean CSTOGGLE_TOGGLES_FULL_SHOP = false;

    @ConfigurationComment("Do you want to show \"You bought/sold... \" messages?")
    public static boolean SHOW_TRANSACTION_INFORMATION_CLIENT = true;

    @ConfigurationComment("Do you want to show \"Somebody bought/sold... \" messages?")
    public static boolean SHOW_TRANSACTION_INFORMATION_OWNER = true;

    @PrecededBySpace
    @ConfigurationComment("If true, plugin will log transactions in its own file")
    public static boolean LOG_TO_FILE = false;

    @ConfigurationComment("Do you want ChestShop's transaction messages to show up in console?")
    public static boolean LOG_TO_CONSOLE = true;

    @ConfigurationComment("Should all shop removals be logged?")
    public static boolean LOG_ALL_SHOP_REMOVALS = true;

    @ConfigurationComment("Do you want to use built-in protection against chest destruction?")
    public static boolean USE_BUILT_IN_PROTECTION = true;

    @ConfigurationComment("Do you want to turn off the default sign protection? Warning! Other players will be able to destroy other people's shops!")
    public static boolean TURN_OFF_SIGN_PROTECTION = false;

    @ConfigurationComment("Do you want to disable the hopper protection, which prevents Hopper-Minecarts from taking items out of shops?")
    public static boolean TURN_OFF_HOPPER_PROTECTION = false;

    @ConfigurationComment("Do you want to protect shop chests with LWC?")
    public static boolean PROTECT_CHEST_WITH_LWC = true;

    @ConfigurationComment("Of which type should the container protection be? Possible type: public, private, donate and on some LWC versions display")
    public static Security.Type LWC_CHEST_PROTECTION_TYPE = Security.Type.PRIVATE;

    @ConfigurationComment("Should the chest's LWC protection be removed once the shop sign is destroyed? ")
    public static boolean REMOVE_LWC_PROTECTION_AUTOMATICALLY = true;

    @ConfigurationComment("Should LWC limits block shop creations?")
    public static boolean LWC_LIMITS_BLOCK_CREATION = true;

    @PrecededBySpace
    @ConfigurationComment("Add stock counter to quantity line?")
    public static boolean USE_STOCK_COUNTER = true ;
}