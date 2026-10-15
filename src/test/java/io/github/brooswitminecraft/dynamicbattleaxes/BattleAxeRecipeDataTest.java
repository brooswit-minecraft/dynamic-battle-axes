package io.github.brooswitminecraft.dynamicbattleaxes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Validates the seven hand-written recipe JSON files and the lang file as
 * plain data, with no NeoForge/game bootstrap: every material must have an
 * obtainable, well-formed recipe producing exactly its own item, and a
 * display name.
 */
class BattleAxeRecipeDataTest {

    private static final String[] SHAPED_MATERIALS = {"wood", "stone", "copper", "iron", "gold", "diamond"};

    @Test
    void everyShapedMaterialHasAWellFormedTwoIngredientRecipe() throws IOException {
        for (String material : SHAPED_MATERIALS) {
            JsonObject recipe = readRecipe(material + "_battle_axe");
            assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString(), material);
            assertEquals("equipment", recipe.get("category").getAsString(), material);
            assertEquals("dynamicbattleaxes:" + material + "_battle_axe",
                    recipe.getAsJsonObject("result").get("id").getAsString(), material);

            JsonObject key = recipe.getAsJsonObject("key");
            assertTrue(key.has("X"), material + " is missing its material key");
            assertEquals("minecraft:stick", key.getAsJsonObject("#").get("item").getAsString(), material);
        }
    }

    @Test
    void netheriteIsAnUpgradeFromDiamondLikeVanillaNetheriteTools() throws IOException {
        JsonObject recipe = readRecipe("netherite_battle_axe");
        assertEquals("minecraft:smithing_transform", recipe.get("type").getAsString());
        assertEquals("minecraft:netherite_upgrade_smithing_template",
                recipe.getAsJsonObject("template").get("item").getAsString());
        assertEquals("dynamicbattleaxes:diamond_battle_axe", recipe.getAsJsonObject("base").get("item").getAsString());
        assertEquals("minecraft:netherite_ingot", recipe.getAsJsonObject("addition").get("item").getAsString());
        assertEquals("dynamicbattleaxes:netherite_battle_axe", recipe.getAsJsonObject("result").get("id").getAsString());
    }

    @Test
    void everyMaterialHasAnEnglishDisplayName() throws IOException {
        JsonObject lang = readJson("/assets/dynamicbattleaxes/lang/en_us.json");
        for (BattleAxeMaterial material : BattleAxeMaterial.values()) {
            String key = "item.dynamicbattleaxes." + material.itemId();
            assertTrue(lang.has(key), "missing lang entry for " + key);
            assertTrue(lang.get(key).getAsString().endsWith("Battle Axe"), key);
        }
    }

    private static JsonObject readRecipe(String itemId) throws IOException {
        return readJson("/data/dynamicbattleaxes/recipe/" + itemId + ".json");
    }

    private static JsonObject readJson(String resourcePath) throws IOException {
        try (InputStream in = BattleAxeRecipeDataTest.class.getResourceAsStream(resourcePath)) {
            assertNotNull(in, "missing resource " + resourcePath);
            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (NullPointerException e) {
            fail("missing resource " + resourcePath);
            throw e;
        }
    }
}
