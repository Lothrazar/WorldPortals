package com.lothrazar.worldportals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(LatticePortalsMod.MODID)
public class LatticePortalsMod {

  public static final String MODID = "worldportals";
  public static final Logger LOGGER = LogManager.getLogger();
  public static ConfigManager CONFIG;

  public LatticePortalsMod() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    MinecraftForge.EVENT_BUS.register(new PortalEvents());
    CONFIG = new ConfigManager(FMLPaths.CONFIGDIR.get().resolve(MODID + ".toml"));
  }

  private void setup(final FMLCommonSetupEvent event) {}
}
