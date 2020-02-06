package org.maxgamer.quickshop.File;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maxgamer.quickshop.Mock.MckFileConfiguration;
import org.maxgamer.quickshop.QuickShopTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileEnvelopeTest {

    private final List<String> test = new ArrayList<>();
    private QuickShopTest plugin;
    private IFile json;
    private IFile yaml;

    @BeforeEach
    void start() {
        MockBukkit.mock();
        plugin = MockBukkit.load(QuickShopTest.class);
        json = new JSONFile(plugin, "messages");
        json.create();
        yaml = new YAMLFile(plugin, "messages");
        yaml.create();
        test.add("test");
        json.set("test.list", test);
        yaml.set("test.list", test);
        json.set("test.int", 13);
        yaml.set("test.int", 13);
        json.set("test.boolean", true);
        yaml.set("test.boolean", true);
        json.set("test.short", (short) 1);
        yaml.set("test.short", (short) 1);
        json.set("test.long", (long) 1);
        yaml.set("test.long", (long) 1);
        json.set("test.byte", (byte) 1);
        yaml.set("test.byte", (byte) 1);
        json.set("test.double", (double) 1);
        yaml.set("test.double", (double) 1);
    }

    private static final String JSON_STRING = "{\n" +
            "  \"shop-staff-cleared\": \"&aSuccessfully removed all staff for your shop.\",\n" +
            "  \"failed-to-paste\": \"&cFailed to upload the data to Pastebin, Check your internet and try again. (See console for details)\",\n" +
            "  \"chest-was-removed\": \"&cThe chest was removed.\",\n" +
            "  \"thats-not-a-number\": \"&cInvalid number\",\n" +
            "  \"shop-already-owned\": \"&cThat is already a shop.\",\n" +
            "  \"buying-more-than-selling\": \"&cWARNING: You are buying items for more than you are selling them!\",\n" +
            "  \"signs\": {\n" +
            "    \"item\": \"{0}\",\n" +
            "    \"selling\": \"Selling {0}\",\n" +
            "    \"header\": \"&c{0}\",\n" +
            "    \"price\": \"{0} each\",\n" +
            "    \"unlimited\": \"Unlimited\",\n" +
            "    \"buying\": \"Buying {0}\"\n" +
            "  },\n" +
            "  \"price-too-high\": \"&c The shop price too high! You can't create one that is priced higher than {0}.\",\n" +
            "  \"no-pending-action\": \"&cYou do not have any pending action\",\n" +
            "  \"restricted-prices\": \"&cRestricted prices for {0}: min {1} , max {2}\",\n" +
            "  \"not-a-number\": \"&cThere can only be number, but you input {0}\",\n" +
            "  \"shop-purchase-cancelled\": \"&cCancelled Shop Purchase.\",\n" +
            "  \"updatenotify\": {\n" +
            "    \"onekeybuttontitle\": \"[OneKey Update]\",\n" +
            "    \"remote-disable-warning\": \"&cThis version of QuickShop is marked disabled by remote server, that mean this version may have serious problem, get details from our SpigotMC page: {0}. This warning will appear and spam your console until you use other not disabled version to replace this one, doesn't effect your server running.\",\n" +
            "    \"list\": [\n" +
            "      \"{0} is released, You are still using {1}!\",\n" +
            "      \"Boom! New update {0} incoming, Update!\",\n" +
            "      \"Surprise! {0} came out, you are on {1}\",\n" +
            "      \"Looks like you need to update, {0} is released!\",\n" +
            "      \"Ooops! {0} is now released, you are on {1}!\",\n" +
            "      \"I promise, QS has been updated to {0}, why have you not updated?\",\n" +
            "      \"Fixing and re... Sorry {0} is released!\",\n" +
            "      \"Err! Nope, this is not an error, {0} has just been released!\",\n" +
            "      \"OMG! {0} came out! Why are you still using {1}?\",\n" +
            "      \"Todays News: QuickShop has been updated to {0}!\",\n" +
            "      \"Plugin K.I.A, You should update to {0}!\",\n" +
            "      \"Fuze is fuzeing update {0}, save update!\",\n" +
            "      \"There is an update commander, {0} has just come out!\",\n" +
            "      \"Look me style---{0} updated, your still using {1}\",\n" +
            "      \"Ahhhhhhh! New update {0}! Update!\",\n" +
            "      \"What U thinking? {0} has been released! Update!\"\n" +
            "    ],\n" +
            "    \"buttontitle\": \"[Update Now]\",\n" +
            "    \"label\": {\n" +
            "      \"lts\": \"[LTS]\",\n" +
            "      \"bukkitdev\": \"[BukkitDev]\",\n" +
            "      \"github\": \"[Github]\",\n" +
            "      \"unstable\": \"[Unstable]\",\n" +
            "      \"stable\": \"[Stable]\",\n" +
            "      \"qualityverifyed\": \"[Quality]\",\n" +
            "      \"spigotmc\": \"[SpigotMC]\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"no-anythings-in-your-hand\": \"&cThere is nothing in your hand.\",\n" +
            "  \"warn-to-paste\": \"&eCollecting data and uploading it to Pastebin, this may take a while. &c&lWarning&c, The data is kept public for one week, it may leak your server configuration and other sensitive information, make sure you only send it to your &ltrusted staff/developer.\",\n" +
            "  \"the-owner-cant-afford-to-buy-from-you\": \"&cThat costs {0} but the owner only has {1}\",\n" +
            "  \"menu\": {\n" +
            "    \"sell-tax-self\": \"&aYou own this shop so you don't pay taxes.\",\n" +
            "    \"this-shop-is-selling\": \"&aThis shop is &bSELLING&a items.\",\n" +
            "    \"successful-purchase\": \"&aSuccessfully Purchased:\",\n" +
            "    \"enchants\": \"&5Enchants\",\n" +
            "    \"item-name-and-price\": \"&e{0} {1} &afor &e{2}\",\n" +
            "    \"this-shop-is-buying\": \"&aThis shop is &dBUYING&a items.\",\n" +
            "    \"effects\": \"&aEffects: &e{0}\",\n" +
            "    \"owner\": \"&aOwner: {0}\",\n" +
            "    \"total-value-of-chest\": \"&aTotal value of Chest: &e{0}\",\n" +
            "    \"commands\": {\n" +
            "      \"preview\": \"/qs silentpreview {0} {1} {2} {3}\"\n" +
            "    },\n" +
            "    \"price-per\": \"&aPrice per &e{0} &a- &e{1}\",\n" +
            "    \"space\": \"&aSpace: &e{0}\",\n" +
            "    \"sell-tax\": \"&aYou paid &e{0} &ain taxes.\",\n" +
            "    \"stored-enchants\": \"&5Stored Enchants\",\n" +
            "    \"item\": \"&aItem: &e{0}\",\n" +
            "    \"successfully-sold\": \"&aSuccessfully Sold:\",\n" +
            "    \"preview\": \"&b[Preview Item]\",\n" +
            "    \"damage-percent-remaining\": \"&e{0}% &aRemaining.\",\n" +
            "    \"shop-information\": \"&aShop Information:\",\n" +
            "    \"stock\": \"&aStock &e{0}\"\n" +
            "  },\n" +
            "  \"player-bought-from-your-store\": \"&c{0} purchased {1} {2} from your store.\",\n" +
            "  \"no-price-given\": \"&cPlease give a valid price.\",\n" +
            "  \"booleanformat\": {\n" +
            "    \"failed\": \"&c✘\",\n" +
            "    \"success\": \"&a✔\"\n" +
            "  },\n" +
            "  \"blacklisted-item\": \"&cThat item is blacklisted. You may not sell it\",\n" +
            "  \"success-created-shop\": \"&aCreated shop.\",\n" +
            "  \"shop-has-no-space\": \"&cThe shop only has room for {0} more {1}.\",\n" +
            "  \"no-creative-break\": \"&cYou cannot break other players shops in creative mode.  Use survival instead.\",\n" +
            "  \"translation-version\": \"&c&lSupport Version: &b&lReremake\",\n" +
            "  \"how-much-to-trade-for\": \"&aEnter how much you wish to trade one &e{0}&a for in chat.\",\n" +
            "  \"shop-has-changed\": \"&cThe shop you tried to use has changed since you clicked it!\",\n" +
            "  \"not-a-integer\": \"&cThere can only be integer, but you input {0}\",\n" +
            "  \"success-change-owner-to-server\": \"&aSuccessfully set the shop owner to Server.\",\n" +
            "  \"not-looking-at-shop\": \"&cNo QuickShop was found, you must be looking at one.\",\n" +
            "  \"you-dont-have-that-many-items\": \"&cYou only have {0} {1}.\",\n" +
            "  \"language-version\": 24,\n" +
            "  \"shop-not-exist\": \"&cThere had no shop.\",\n" +
            "  \"translation-country\": \"&c&lLanguage Zone: &b&lEnglish (en_US)\",\n" +
            "  \"unknown-player\": \"&cTarget player doesn't exist, please check the username you typed.\",\n" +
            "  \"you-cant-afford-to-change-price\": \"&cIt costs {0} to change the price in your shop.\",\n" +
            "  \"negative-amount\": \"&cDerp, can't trade negative amounts\",\n" +
            "  \"success-removed-shop\": \"&aShop removed.\",\n" +
            "  \"how-many-sell\": \"&aEnter how many you wish to &dSELL&a in chat. You have &e{0}&a available. Enter &ball&a to sell them all.\",\n" +
            "  \"you-cant-afford-a-new-shop\": \"&cIt costs {0} to create a new shop.\",\n" +
            "  \"shop-creation-cancelled\": \"&cCancelled Shop Creation.\",\n" +
            "  \"owner-bypass-check\": \"&eBypassed all checks, Trade successful! (You are shop owner)\",\n" +
            "  \"tableformat\": {\n" +
            "    \"left_half_line\": \"+--------------------\",\n" +
            "    \"right_half_line\": \"--------------------+\",\n" +
            "    \"full_line\": \"+---------------------------------------------------+\",\n" +
            "    \"left_begin\": \"| \"\n" +
            "  },\n" +
            "  \"shop-staff-deleted\": \"&aSuccessfully removed {0} from your shop staffs.\",\n" +
            "  \"that-is-locked\": \"&cThat shop is locked.\",\n" +
            "  \"no-nearby-shop\": \"&cNo shops matching {0} nearby.\",\n" +
            "  \"you-cant-create-shop-in-there\": \"&cYou don't have permission to create a shop at this location.\",\n" +
            "  \"shop-stock-too-low\": \"&cThe shop only has {0} {1} left\",\n" +
            "  \"fee-charged-for-price-change\": \"&aYou pay &c{0}&a to change the price.\",\n" +
            "  \"how-many-buy\": \"&aEnter how many you wish to &bBUY&a in chat. You can buy &e{0}&a. Enter &ball&a to buy them all.\",\n" +
            "  \"failed-to-put-sign\": \"&cNot enough space around the shop to place the information sign.\",\n" +
            "  \"controlpanel\": {\n" +
            "    \"mode-buying-hover\": \"&eClick to convert the shop to be in the selling mode.\",\n" +
            "    \"refill-hover\": \"&eClick to refill the shop.\",\n" +
            "    \"empty-hover\": \"&eClick to clear the inventory of the shop.\",\n" +
            "    \"price\": \"&aPrice: &b{0} &e[&d&lSet&e]\",\n" +
            "    \"unlimited\": \"&aUnlimited: {0} &e[&d&lSwitch&e]\",\n" +
            "    \"price-hover\": \"&eClick to set a new price for the shop.\",\n" +
            "    \"setowner-hover\": \"&eClick to switch owner.\",\n" +
            "    \"setowner\": \"&aOwner: &b{0} &e[&d&lChange&e]\",\n" +
            "    \"unlimited-hover\": \"&eClick to toggle if the shop is unlimited.\",\n" +
            "    \"mode-buying\": \"&aShop mode: &bBuying &e[&d&lSwitch&e]\",\n" +
            "    \"refill\": \"&aRefill: Refill the shop items &e[&d&lOK&e]\",\n" +
            "    \"mode-selling-hover\": \"&eClick to convert the shop to be in the buying mode.\",\n" +
            "    \"commands\": {\n" +
            "      \"sell\": \"/qs silentsell {0} {1} {2} {3}\",\n" +
            "      \"buy\": \"/qs silentbuy {0} {1} {2} {3}\",\n" +
            "      \"price\": \"/qs price [New Price]\",\n" +
            "      \"unlimited\": \"/qs silentunlimited {0} {1} {2} {3}\",\n" +
            "      \"remove\": \"/qs silentremove {0} {1} {2} {3}\",\n" +
            "      \"setowner\": \"/qs setowner [Player]\",\n" +
            "      \"refill\": \"/qs refill [Amount]\",\n" +
            "      \"empty\": \"/qs silentempty {0} {1} {2} {3}\"\n" +
            "    },\n" +
            "    \"remove\": \"&c&l[Remove Shop]\",\n" +
            "    \"infomation\": \"&aShop Control Panel:\",\n" +
            "    \"mode-selling\": \"&aShop mode: &bSelling &e[&d&lSwitch&e]\",\n" +
            "    \"remove-hover\": \"&eClick to remove this shop.\",\n" +
            "    \"empty\": \"&aEmpty: Remove shop all items &e[&d&lOK&e]\"\n" +
            "  },\n" +
            "  \"permission-denied-3rd-party\": \"&cPermission denied: 3rd party plugin [{0}].\",\n" +
            "  \"not-enough-space\": \"&cYou only have room for {0} more of that!\",\n" +
            "  \"average-price-nearby\": \"&aAverage Price Nearby: &e{0}\",\n" +
            "  \"not-allowed-to-create\": \"&cYou may not create a shop here.\",\n" +
            "  \"price-too-cheap\": \"&cPrice must be greater than &e${0}\",\n" +
            "  \"shop-removed-cause-ongoing-fee\": \"&cYou shop at {0} was removed cause you had no enough money to keep it!\",\n" +
            "  \"player-sold-to-your-store\": \"&a{0} sold {1} {2} to your store.\",\n" +
            "  \"no-enough-money-to-keep-shops\": \"&cYou didn't have enough money to keep your shops! All shops have now been removed...\",\n" +
            "  \"shop-staff-added\": \"&aSuccessfully added {0} to your shop staffs.\",\n" +
            "  \"nothing-to-flush\": \"&aYou had no new shop message.\",\n" +
            "  \"empty-success\": \"&aEmpty success\",\n" +
            "  \"flush-finished\": \"&aSuccessfully flushed the messages.\",\n" +
            "  \"unknown-owner\": \"Unknown\",\n" +
            "  \"bypassing-lock\": \"&cBypassing a QuickShop lock!\",\n" +
            "  \"nearby-shop-this-way\": \"&aShop is {0} blocks away from you.\",\n" +
            "  \"no-double-chests\": \"&cYou cannot create the DoubleChest shop.\",\n" +
            "  \"shop-out-of-stock\": \"&5Your shop at {0}, {1}, {2}, has run out of {3}\",\n" +
            "  \"purchase-failed\": \"&cPurchase failed: Internal Error, please contact the server administrator.\",\n" +
            "  \"price-is-now\": \"&aThe shops new price is &e{0}\",\n" +
            "  \"reached-maximum-can-create\": \"&cYou have already created a maximum of {0}/{1} shops!\",\n" +
            "  \"no-permission\": \"&cYou do not have permission to do that.\",\n" +
            "  \"shop-out-of-space\": \"&5Your shop at {0}, {1}, {2}, is now full.\",\n" +
            "  \"player-bought-from-your-store-tax\": \"&c{0} purchased {1} {2} from your store, and you paid {3} in taxes.\",\n" +
            "  \"no-permission-build\": \"&cYou can't build a shop here.\",\n" +
            "  \"refill-success\": \"&aRefill success\",\n" +
            "  \"translation-author\": \"&c&lTranslator: &b&lGhost_chu\",\n" +
            "  \"break-shop-use-supertool\": \"&eYou can break the shop by using the SuperTool.\",\n" +
            "  \"translation-contributors\": \"&c&lContributors: &b&lTimtower, Netherfoam, KaiNoMood and Mgazul\",\n" +
            "  \"command\": {\n" +
            "    \"toggle-unlimited\": {\n" +
            "      \"limited\": \"&aShop is now limited\",\n" +
            "      \"unlimited\": \"&aShop is now unlimited\"\n" +
            "    },\n" +
            "    \"no-type-given\": \"&cUsage: /qs find <item>\",\n" +
            "    \"description\": {\n" +
            "      \"info\": \"&eShow QuickShop Statistics\",\n" +
            "      \"buy\": \"&eConverts a shop to &dBUY&e mode\",\n" +
            "      \"help\": \"&eShow QuickShop helps\",\n" +
            "      \"price\": \"&eChanges the buy/selling price of one of your shops\",\n" +
            "      \"unlimited\": \"&eMakes a shop unlimited\",\n" +
            "      \"about\": \"&eShow QuickShop abouts\",\n" +
            "      \"setowner\": \"&eChanges who owns a shop\",\n" +
            "      \"owner\": \"&eChanges who owns a shop\",\n" +
            "      \"refill\": \"&eAdds a given number of items to a shop\",\n" +
            "      \"paste\": \"&eAuto upload server data to Pastebin\",\n" +
            "      \"find\": \"&eLocates the nearest shop of a specific type.\",\n" +
            "      \"staff\": \"&eManage your shop staffs\",\n" +
            "      \"sell\": \"&eConverts a shop to &bSELL&e mode\",\n" +
            "      \"supercreate\": \"&eCreate a shop bypass all protection checks\",\n" +
            "      \"title\": \"&aQuickShop Help\",\n" +
            "      \"create\": \"&eCreates a new shop at the target chest\",\n" +
            "      \"remove\": \"&eRemove your looking the shop\",\n" +
            "      \"reload\": \"&eReloads the config.yml for QuickShop\",\n" +
            "      \"amount\": \"&eExecute for your actions with amount(For chat plugin issue)\",\n" +
            "      \"clean\": \"&eRemoves all (loaded) shops with 0 stock\",\n" +
            "      \"debug\": \"&eSwitch to developer mode\",\n" +
            "      \"fetchmessage\": \"&eFetch unread shop message\",\n" +
            "      \"empty\": \"&eRemoves all stock from a shop\"\n" +
            "    },\n" +
            "    \"new-owner\": \"&aNew owner: &e{0}\",\n" +
            "    \"now-buying\": \"&aNow &dBUYING&a &e{0}\",\n" +
            "    \"now-selling\": \"&aNow &bSELLING &e{0}\",\n" +
            "    \"no-owner-given\": \"&cNo owner given. Use &a/qs setowner <player>&c\",\n" +
            "    \"no-amount-given\": \"&cNo amount given. Use &a/qs refill <amount>&c\",\n" +
            "    \"reloading\": \"&aReloading...\",\n" +
            "    \"cleaning\": \"&aCleaning up shops with 0 stock...\",\n" +
            "    \"now-nolonger-debuging\": \"&aSuccessfully switched to production mode, Reloading QuickShop...\",\n" +
            "    \"cleaned\": \"&aCleaned &e{0}&a shops\",\n" +
            "    \"now-debuging\": \"&aSuccessfully switched to developer mode, Reloading QuickShop...\",\n" +
            "    \"wrong-args\": \"&cParameters don't match, use /qs help to check help\"\n" +
            "  },\n" +
            "  \"no-price-change\": \"&cThat wouldn't result in a price change!\",\n" +
            "  \"shops-arent-locked\": \"&cRemember, shops are NOT protected from theft! If you want to stop thieves, lock it with LWC, Lockette, etc!\",\n" +
            "  \"admin-shop\": \"AdminShop\",\n" +
            "  \"you-cant-afford-to-buy\": \"&cThat costs {0}, but you only have {1}\",\n" +
            "  \"tabcomplete\": {\n" +
            "    \"range\": \"[range]\",\n" +
            "    \"amount\": \"[amount]\",\n" +
            "    \"price\": \"[price]\"\n" +
            "  },\n" +
            "  \"test\": {\n" +
            "    \"list\": [\n" +
            "      \"test\"\n" +
            "    ],\n" +
            "    \"int\": 13,\n" +
            "    \"boolean\": true,\n" +
            "    \"short\": 1,\n" +
            "    \"long\": 1,\n" +
            "    \"byte\": 1,\n" +
            "    \"double\": 1.0,\n" +
            "    \"section\": {},\n" +
            "    \"test\": \"test\",\n" +
            "    \"test123\": \"test\"\n" +
            "  }\n" +
            "}";
    private static final String YAML_STRING = "# Colors:\n" +
            "# &0-9, &a-f\n" +
            "# {0}, {1}, {2}, etc are variables. You can swap them around, but adding a new variable won't work. Removing them will work\n" +
            "# \n" +
            "# Translate data.\n" +
            "# ======================================================================\n" +
            "# For Translator: These string can changed by your self.\n" +
            "translation-author: '&c&lTranslator: &b&lGhost_chu'\n" +
            "translation-contributors: '&c&lContributors: &b&lTimtower, Netherfoam, KaiNoMood and\n" +
            "  Mgazul'\n" +
            "translation-version: '&c&lSupport Version: &b&lReremake'\n" +
            "translation-country: '&c&lLanguage Zone: &b&lEnglish (en_US)'\n" +
            "language-version: 24\n" +
            "not-looking-at-shop: '&cNo QuickShop was found, you must be looking at one.'\n" +
            "no-anythings-in-your-hand: '&cThere is nothing in your hand.'\n" +
            "no-permission: '&cYou do not have permission to do that.'\n" +
            "no-creative-break: '&cYou cannot break other players shops in creative mode.  Use\n" +
            "  survival instead.'\n" +
            "no-double-chests: '&cYou cannot create the DoubleChest shop.'\n" +
            "shop-already-owned: '&cThat is already a shop.'\n" +
            "chest-was-removed: '&cThe chest was removed.'\n" +
            "price-too-cheap: '&cPrice must be greater than &e${0}'\n" +
            "no-price-change: '&cThat wouldn''t result in a price change!'\n" +
            "you-cant-afford-a-new-shop: '&cIt costs {0} to create a new shop.'\n" +
            "player-bought-from-your-store-tax: '&c{0} purchased {1} {2} from your store, and you\n" +
            "  paid {3} in taxes.'\n" +
            "you-cant-afford-to-change-price: '&cIt costs {0} to change the price in your shop.'\n" +
            "success-created-shop: '&aCreated shop.'\n" +
            "success-removed-shop: '&aShop removed.'\n" +
            "shops-arent-locked: '&cRemember, shops are NOT protected from theft! If you want to\n" +
            "  stop thieves, lock it with LWC, Lockette, etc!'\n" +
            "shop-creation-cancelled: '&cCancelled Shop Creation.'\n" +
            "shop-purchase-cancelled: '&cCancelled Shop Purchase.'\n" +
            "shop-stock-too-low: '&cThe shop only has {0} {1} left'\n" +
            "you-cant-afford-to-buy: '&cThat costs {0}, but you only have {1}'\n" +
            "negative-amount: '&cDerp, can''t trade negative amounts'\n" +
            "not-a-number: '&cThere can only be number, but you input {0}'\n" +
            "not-a-integer: '&cThere can only be integer, but you input {0}'\n" +
            "player-bought-from-your-store: '&c{0} purchased {1} {2} from your store.'\n" +
            "shop-out-of-stock: '&5Your shop at {0}, {1}, {2}, has run out of {3}'\n" +
            "shop-has-no-space: '&cThe shop only has room for {0} more {1}.'\n" +
            "you-dont-have-that-many-items: '&cYou only have {0} {1}.'\n" +
            "the-owner-cant-afford-to-buy-from-you: '&cThat costs {0} but the owner only has {1}'\n" +
            "player-sold-to-your-store: '&a{0} sold {1} {2} to your store.'\n" +
            "shop-out-of-space: '&5Your shop at {0}, {1}, {2}, is now full.'\n" +
            "fee-charged-for-price-change: '&aYou pay &c{0}&a to change the price.'\n" +
            "price-is-now: '&aThe shops new price is &e{0}'\n" +
            "thats-not-a-number: '&cInvalid number'\n" +
            "no-price-given: '&cPlease give a valid price.'\n" +
            "average-price-nearby: '&aAverage Price Nearby: &e{0}'\n" +
            "shop-has-changed: '&cThe shop you tried to use has changed since you clicked it!'\n" +
            "shop-not-exist: '&cThere had no shop.'\n" +
            "nearby-shop-this-way: '&aShop is {0} blocks away from you.'\n" +
            "no-nearby-shop: '&cNo shops matching {0} nearby.'\n" +
            "buying-more-than-selling: '&cWARNING: You are buying items for more than you are selling\n" +
            "  them!'\n" +
            "not-enough-space: '&cYou only have room for {0} more of that!'\n" +
            "refill-success: '&aRefill success'\n" +
            "empty-success: '&aEmpty success'\n" +
            "admin-shop: AdminShop\n" +
            "unknown-owner: Unknown\n" +
            "owner-bypass-check: '&eBypassed all checks, Trade successful! (You are shop owner)'\n" +
            "reached-maximum-can-create: '&cYou have already created a maximum of {0}/{1} shops!'\n" +
            "restricted-prices: '&cRestricted prices for {0}: min {1} , max {2}'\n" +
            "no-enough-money-to-keep-shops: '&cYou didn''t have enough money to keep your shops!\n" +
            "  All shops have now been removed...'\n" +
            "nothing-to-flush: '&aYou had no new shop message.'\n" +
            "break-shop-use-supertool: '&eYou can break the shop by using the SuperTool.'\n" +
            "failed-to-put-sign: '&cNot enough space around the shop to place the information sign.'\n" +
            "failed-to-paste: '&cFailed to upload the data to Pastebin, Check your internet and\n" +
            "  try again. (See console for details)'\n" +
            "warn-to-paste: '&eCollecting data and uploading it to Pastebin, this may take a while.\n" +
            "  &c&lWarning&c, The data is kept public for one week, it may leak your server configuration\n" +
            "  and other sensitive information, make sure you only send it to your &ltrusted staff/developer.'\n" +
            "price-too-high: '&c The shop price too high! You can''t create one that is priced\n" +
            "  higher than {0}.'\n" +
            "you-cant-create-shop-in-there: '&cYou don''t have permission to create a shop at this\n" +
            "  location.'\n" +
            "unknown-player: '&cTarget player doesn''t exist, please check the username you typed.'\n" +
            "shop-staff-cleared: '&aSuccessfully removed all staff for your shop.'\n" +
            "shop-staff-added: '&aSuccessfully added {0} to your shop staffs.'\n" +
            "shop-staff-deleted: '&aSuccessfully removed {0} from your shop staffs.'\n" +
            "no-permission-build: '&cYou can''t build a shop here.'\n" +
            "success-change-owner-to-server: '&aSuccessfully set the shop owner to Server.'\n" +
            "flush-finished: '&aSuccessfully flushed the messages.'\n" +
            "purchase-failed: '&cPurchase failed: Internal Error, please contact the server administrator.'\n" +
            "no-pending-action: '&cYou do not have any pending action'\n" +
            "permission-denied-3rd-party: '&cPermission denied: 3rd party plugin [{0}].'\n" +
            "menu:\n" +
            "  successful-purchase: '&aSuccessfully Purchased:'\n" +
            "  successfully-sold: '&aSuccessfully Sold:'\n" +
            "  item-name-and-price: '&e{0} {1} &afor &e{2}'\n" +
            "  sell-tax: '&aYou paid &e{0} &ain taxes.'\n" +
            "  sell-tax-self: '&aYou own this shop so you don''t pay taxes.'\n" +
            "  enchants: '&5Enchants'\n" +
            "  stored-enchants: '&5Stored Enchants'\n" +
            "  shop-information: '&aShop Information:'\n" +
            "  owner: '&aOwner: {0}'\n" +
            "  item: '&aItem: &e{0}'\n" +
            "  preview: '&b[Preview Item]'\n" +
            "  space: '&aSpace: &e{0}'\n" +
            "  stock: '&aStock &e{0}'\n" +
            "  price-per: '&aPrice per &e{0} &a- &e{1}'\n" +
            "  total-value-of-chest: '&aTotal value of Chest: &e{0}'\n" +
            "  damage-percent-remaining: '&e{0}% &aRemaining.'\n" +
            "  this-shop-is-buying: '&aThis shop is &dBUYING&a items.'\n" +
            "  this-shop-is-selling: '&aThis shop is &bSELLING&a items.'\n" +
            "  effects: '&aEffects: &e{0}'\n" +
            "  commands:\n" +
            "    preview: /qs silentpreview {0} {1} {2} {3}\n" +
            "bypassing-lock: '&cBypassing a QuickShop lock!'\n" +
            "that-is-locked: '&cThat shop is locked.'\n" +
            "how-many-buy: '&aEnter how many you wish to &bBUY&a in chat. You can buy &e{0}&a.\n" +
            "  Enter &ball&a to buy them all.'\n" +
            "how-many-sell: '&aEnter how many you wish to &dSELL&a in chat. You have &e{0}&a available.\n" +
            "  Enter &ball&a to sell them all.'\n" +
            "not-allowed-to-create: '&cYou may not create a shop here.'\n" +
            "blacklisted-item: '&cThat item is blacklisted. You may not sell it'\n" +
            "how-much-to-trade-for: '&aEnter how much you wish to trade one &e{0}&a for in chat.'\n" +
            "command:\n" +
            "  toggle-unlimited:\n" +
            "    unlimited: '&aShop is now unlimited'\n" +
            "    limited: '&aShop is now limited'\n" +
            "  no-owner-given: '&cNo owner given. Use &a/qs setowner <player>&c'\n" +
            "  new-owner: '&aNew owner: &e{0}'\n" +
            "  now-buying: '&aNow &dBUYING&a &e{0}'\n" +
            "  now-selling: '&aNow &bSELLING &e{0}'\n" +
            "  cleaning: '&aCleaning up shops with 0 stock...'\n" +
            "  reloading: '&aReloading...'\n" +
            "  cleaned: '&aCleaned &e{0}&a shops'\n" +
            "  no-type-given: '&cUsage: /qs find <item>'\n" +
            "  no-amount-given: '&cNo amount given. Use &a/qs refill <amount>&c'\n" +
            "  now-debuging: '&aSuccessfully switched to developer mode, Reloading QuickShop...'\n" +
            "  now-nolonger-debuging: '&aSuccessfully switched to production mode, Reloading QuickShop...'\n" +
            "  wrong-args: '&cParameters don''t match, use /qs help to check help'\n" +
            "  description:\n" +
            "    title: '&aQuickShop Help'\n" +
            "    unlimited: '&eMakes a shop unlimited'\n" +
            "    setowner: '&eChanges who owns a shop'\n" +
            "    owner: '&eChanges who owns a shop'\n" +
            "    buy: '&eConverts a shop to &dBUY&e mode'\n" +
            "    sell: '&eConverts a shop to &bSELL&e mode'\n" +
            "    clean: '&eRemoves all (loaded) shops with 0 stock'\n" +
            "    price: '&eChanges the buy/selling price of one of your shops'\n" +
            "    find: '&eLocates the nearest shop of a specific type.'\n" +
            "    reload: '&eReloads the config.yml for QuickShop'\n" +
            "    refill: '&eAdds a given number of items to a shop'\n" +
            "    empty: '&eRemoves all stock from a shop'\n" +
            "    create: '&eCreates a new shop at the target chest'\n" +
            "    debug: '&eSwitch to developer mode'\n" +
            "    fetchmessage: '&eFetch unread shop message'\n" +
            "    info: '&eShow QuickShop Statistics'\n" +
            "    paste: '&eAuto upload server data to Pastebin'\n" +
            "    staff: '&eManage your shop staffs'\n" +
            "    remove: '&eRemove your looking the shop'\n" +
            "    amount: '&eExecute for your actions with amount(For chat plugin issue)'\n" +
            "    about: '&eShow QuickShop abouts'\n" +
            "    help: '&eShow QuickShop helps'\n" +
            "    supercreate: '&eCreate a shop bypass all protection checks'\n" +
            "signs:\n" +
            "  header: '&c{0}'\n" +
            "  selling: Selling {0}\n" +
            "  buying: Buying {0}\n" +
            "  item: '{0}'\n" +
            "  price: '{0} each'\n" +
            "  unlimited: Unlimited\n" +
            "controlpanel:\n" +
            "  infomation: '&aShop Control Panel:'\n" +
            "  setowner: '&aOwner: &b{0} &e[&d&lChange&e]'\n" +
            "  setowner-hover: '&eClick to switch owner.'\n" +
            "  unlimited: '&aUnlimited: {0} &e[&d&lSwitch&e]'\n" +
            "  unlimited-hover: '&eClick to toggle if the shop is unlimited.'\n" +
            "  mode-selling: '&aShop mode: &bSelling &e[&d&lSwitch&e]'\n" +
            "  mode-selling-hover: '&eClick to convert the shop to be in the buying mode.'\n" +
            "  mode-buying: '&aShop mode: &bBuying &e[&d&lSwitch&e]'\n" +
            "  mode-buying-hover: '&eClick to convert the shop to be in the selling mode.'\n" +
            "  price: '&aPrice: &b{0} &e[&d&lSet&e]'\n" +
            "  price-hover: '&eClick to set a new price for the shop.'\n" +
            "  refill: '&aRefill: Refill the shop items &e[&d&lOK&e]'\n" +
            "  refill-hover: '&eClick to refill the shop.'\n" +
            "  empty: '&aEmpty: Remove shop all items &e[&d&lOK&e]'\n" +
            "  empty-hover: '&eClick to clear the inventory of the shop.'\n" +
            "  remove: '&c&l[Remove Shop]'\n" +
            "  remove-hover: '&eClick to remove this shop.'\n" +
            "  commands:\n" +
            "    setowner: /qs setowner [Player]\n" +
            "    unlimited: /qs silentunlimited {0} {1} {2} {3}\n" +
            "    buy: /qs silentbuy {0} {1} {2} {3}\n" +
            "    sell: /qs silentsell {0} {1} {2} {3}\n" +
            "    price: /qs price [New Price]\n" +
            "    refill: /qs refill [Amount]\n" +
            "    empty: /qs silentempty {0} {1} {2} {3}\n" +
            "    remove: /qs silentremove {0} {1} {2} {3}\n" +
            "tableformat:\n" +
            "  full_line: +---------------------------------------------------+\n" +
            "  left_half_line: +--------------------\n" +
            "  right_half_line: '--------------------+'\n" +
            "  left_begin: '| '\n" +
            "booleanformat:\n" +
            "  success: '&a✔'\n" +
            "  failed: '&c✘'\n" +
            "tabcomplete:\n" +
            "  price: '[price]'\n" +
            "  range: '[range]'\n" +
            "  amount: '[amount]'\n" +
            "updatenotify:\n" +
            "  buttontitle: '[Update Now]'\n" +
            "  onekeybuttontitle: '[OneKey Update]'\n" +
            "  list:\n" +
            "  - '{0} is released, You are still using {1}!'\n" +
            "  - Boom! New update {0} incoming, Update!\n" +
            "  - Surprise! {0} came out, you are on {1}\n" +
            "  - Looks like you need to update, {0} is released!\n" +
            "  - Ooops! {0} is now released, you are on {1}!\n" +
            "  - I promise, QS has been updated to {0}, why have you not updated?\n" +
            "  - Fixing and re... Sorry {0} is released!\n" +
            "  - Err! Nope, this is not an error, {0} has just been released!\n" +
            "  - OMG! {0} came out! Why are you still using {1}?\n" +
            "  - 'Todays News: QuickShop has been updated to {0}!'\n" +
            "  - Plugin K.I.A, You should update to {0}!\n" +
            "  - Fuze is fuzeing update {0}, save update!\n" +
            "  - There is an update commander, {0} has just come out!\n" +
            "  - Look me style---{0} updated, your still using {1}\n" +
            "  - Ahhhhhhh! New update {0}! Update!\n" +
            "  - What U thinking? {0} has been released! Update!\n" +
            "  remote-disable-warning: '&cThis version of QuickShop is marked disabled by remote\n" +
            "    server, that mean this version may have serious problem, get details from our\n" +
            "    SpigotMC page: {0}. This warning will appear and spam your console until you use\n" +
            "    other not disabled version to replace this one, doesn''t effect your server running.'\n" +
            "  label:\n" +
            "    unstable: '[Unstable]'\n" +
            "    stable: '[Stable]'\n" +
            "    lts: '[LTS]'\n" +
            "    qualityverifyed: '[Quality]'\n" +
            "    github: '[Github]'\n" +
            "    spigotmc: '[SpigotMC]'\n" +
            "    bukkitdev: '[BukkitDev]'\n" +
            "shop-removed-cause-ongoing-fee: '&cYou shop at {0} was removed cause you had no enough\n" +
            "  money to keep it!'\n" +
            "test:\n" +
            "  list:\n" +
            "  - test\n" +
            "  int: 13\n" +
            "  boolean: true\n" +
            "  short: 1\n" +
            "  long: 1\n" +
            "  byte: 1\n" +
            "  double: 1.0\n" +
            "  section: {}\n" +
            "  test: test\n" +
            "  test123: test";

    @Test
    void loadFromString() throws InvalidConfigurationException {
        json.loadFromString(JSON_STRING);
        yaml.loadFromString(YAML_STRING);
    }

    @Test
    void saveToString() {
        System.out.println(
                json.saveToString()
        );

        System.out.println(
                yaml.saveToString()
        );
    }

    @Test
    void get() {
        assertEquals(
                "{0}",
                json.get("signs.item")
        );
        assertEquals(
                "{0}",
                yaml.get("signs.item")
        );
    }

    @Test
    void create() {
        json.create();
        yaml.create();
    }

    @Test
    void save() {
        json.save();
        yaml.save();
    }

    @Test
    void getString() {
        final Optional<String> optionalS = json.getString("shop-staff-cleared");
        final Optional<String> optionalSS = yaml.getString("shop-staff-cleared");

        if (!optionalS.isPresent() || !optionalSS.isPresent()) {
            throw new IllegalArgumentException("There no path called 'shop-staff-cleared'");
        }

        assertEquals(
                "&aSuccessfully removed all staff for your shop.",
                optionalS.get()
        );
        assertEquals(
                "&aSuccessfully removed all staff for your shop.",
                optionalSS.get()
        );
    }

    @Test
    void set() {
        json.set("test.test", "test");
        yaml.set("test.test", "test");
        assertEquals(
                "{0}",
                json.get("signs.item")
        );
        assertEquals(
                "{0}",
                yaml.get("signs.item")
        );
    }

    @Test
    void getWithFallback() {
        assertEquals(
                "&aSuccessfully removed all staff for your shop.",
                json.get("shop-staff-cleared", "test")
        );
        assertEquals(
                "&aSuccessfully removed all staff for your shop.",
                yaml.get("shop-staff-cleared", "test")
        );
    }

    @Test
    void getOrSet() {
        assertEquals(
                "test",
                json.getOrSet("test.test123", "test")
        );
        assertEquals(
                "test",
                yaml.getOrSet("test.test123", "test")
        );
        assertEquals(
                "test",
                json.getOrSet("test.test123", "test")
        );
        assertEquals(
                "test",
                yaml.getOrSet("test.test123", "test")
        );
    }

    @Test
    void getStringList() {
        assertEquals(
                test,
                json.getStringList("test.list")
        );
        assertEquals(
                test,
                yaml.getStringList("test.list")
        );
    }

    @Test
    void getInt() {
        assertEquals(
                13,
                json.getInt("test.int")
        );
        assertEquals(
                13,
                yaml.getInt("test.int")
        );
    }

    @Test
    void getDouble() {
        assertEquals(
                1,
                json.getDouble("test.short")
        );
        assertEquals(
                1,
                yaml.getDouble("test.short")
        );
    }

    @Test
    void getLong() {
        assertEquals(
                1,
                json.getLong("test.short")
        );
        assertEquals(
                1,
                yaml.getLong("test.short")
        );
    }

    @Test
    void getByte() {
        assertEquals(
                (byte) 1,
                json.getByte("test.short")
        );
        assertEquals(
                (byte) 1,
                yaml.getByte("test.short")
        );
    }

    @Test
    void createSection() {
        json.createSection("test.section");
        yaml.createSection("test.section");
    }

    @Test
    void getShort() {
        assertEquals(
                (short) 1,
                json.getShort("test.short")
        );
        assertEquals(
                (short) 1,
                yaml.getShort("test.short")
        );
    }

    @Test
    void getBoolean() {
        assertTrue(
                json.getBoolean("test.boolean")
        );
        assertTrue(
                yaml.getBoolean("test.boolean")
        );
    }

    @AfterEach
    void stop() {
        json.save();
        yaml.save();
        MockBukkit.unload();
    }

    @Test
    void getSection() {
        assertTrue(
                () -> !(json.getSection("test.section") instanceof MckFileConfiguration)
        );
        assertTrue(
                () -> !(yaml.getSection("test.section") instanceof MckFileConfiguration)
        );
    }

    @Test
    void getOrCreateSection() {
        assertTrue(
                () -> !(json.getOrCreateSection("test.section") instanceof MckFileConfiguration)
        );
        assertTrue(
                () -> !(yaml.getOrCreateSection("test.section") instanceof MckFileConfiguration)
        );
    }

}
