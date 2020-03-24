package com.lothrazar.worldportals;

import java.nio.file.Path;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class ConfigManager {

  private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
  private static ForgeConfigSpec COMMON_CONFIG;
  public static IntValue GRID;
  public static IntValue BUFFER;
  public static IntValue LOWEST;
  public static IntValue HIGHEST;
  public static BooleanValue OVERWORLDONLY;
  static {
    initConfig();
  }

  private static void initConfig() {
    COMMON_BUILDER.comment("General settings").push(ExampleMod.MODID);
    GRID = COMMON_BUILDER.comment("Grid size").defineInRange("grid", 500, 50, 1000 * 1000);
    BUFFER = COMMON_BUILDER.comment("Grid buffer").defineInRange("buffer", 8, 2, 1000);
    LOWEST = COMMON_BUILDER.comment("Minimum Y level").defineInRange("lowestY", 50, 0, 256);
    HIGHEST = COMMON_BUILDER.comment("Maximum Y level").defineInRange("highestY", 100, 0, 256);
    OVERWORLDONLY = COMMON_BUILDER.comment("Only allowed to make nether portals in the overworld").define("overworldOnly", true);
    COMMON_BUILDER.pop();
    COMMON_CONFIG = COMMON_BUILDER.build();
  }

  public ConfigManager(Path path) {
    final CommentedFileConfig configData = CommentedFileConfig.builder(path)
        .sync()
        .autosave()
        .writingMode(WritingMode.REPLACE)
        .build();
    configData.load();
    COMMON_CONFIG.setConfig(configData);
  }
}
