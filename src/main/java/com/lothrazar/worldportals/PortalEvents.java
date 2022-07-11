package com.lothrazar.worldportals;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.world.BlockEvent.PortalSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PortalEvents {

  @SubscribeEvent
  public void onEntityMobGriefingEvent(EntityMobGriefingEvent event) {
    //sub rules for different grief types    
  }

  @SubscribeEvent
  public void onPortalSpawnEvent(PortalSpawnEvent event) {
    //    event.getPortalSize()
    //    event.getPortalSize().getHeight() 
    //    event.getPortalSize().getWidth()
    BlockPos pos = event.getPos();
    if (!(event.getWorld() instanceof Level)) {
      return;
    }
    Level world = (Level) event.getWorld();
    PortalRejectReason allowed = this.allowedHere(world, pos);
    if (allowed != PortalRejectReason.ALLOWED) {
      event.setCanceled(true);
      if (world.getBlockState(pos).getBlock() instanceof BaseFireBlock) {
        //extinguish
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
      }
      if (world.isClientSide) {
        String allowedString = this.getAllowedString(allowed);
        getClientPlayer().displayClientMessage(Component.translatable(allowedString), true);
        world.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, pos.getX(), pos.getY(), pos.getZ(), 0, 0.1, 0);
        world.addParticle(ParticleTypes.BUBBLE, pos.getX(), pos.getY(), pos.getZ(), 0, 0.1, 0);
        world.addParticle(ParticleTypes.BUBBLE, pos.getX(), pos.getY(), pos.getZ(), 0, 0.1, 0);
        world.addParticle(ParticleTypes.BUBBLE, pos.getX(), pos.getY(), pos.getZ(), 0, 0.1, 0);
        world.addParticle(ParticleTypes.BUBBLE, pos.getX(), pos.getY(), pos.getZ(), 0, 0.1, 0);
        //        world.addParticle(ParticleTypes.BARRIER, pos.getX(), pos.getY(), pos.getZ(), 0, 0.1, 0);
        getClientPlayer().displayClientMessage(Component.translatable("nope"), true);
        //chat message 
      }
    }
  }

  @OnlyIn(Dist.CLIENT)
  private Player getClientPlayer() {
    return Minecraft.getInstance().player;
  }

  public static String lang(String message) {
    return Component.translatable(message).getString();
  }

  // F3 screen text overlay event
  @SubscribeEvent
  @OnlyIn(Dist.CLIENT)
  public void onDebugOverlay(RenderGameOverlayEvent.Text event) {
    BlockPos pos = getClientPlayer().blockPosition();
    if (Minecraft.getInstance().options.renderDebug == false) {
      return;
    } //if f3 is not pressed
    String allowedString = this.getAllowedString(this.allowedHere(Minecraft.getInstance().level, pos));
    event.getLeft().add(allowedString);
  }

  private String getAllowedString(PortalRejectReason allowedHere) {
    return lang(LatticePortalsMod.MODID + ".f3." + allowedHere.name().toLowerCase());
  }

  private PortalRejectReason allowedHere(Level world, BlockPos pos) {
    boolean isOverworld = world.dimension() != Level.OVERWORLD;
    if (isOverworld
        && ConfigManager.OVERWORLDONLY.get()) {
      return PortalRejectReason.DIMENSION;
    }
    if (ConfigManager.LOWEST.get() > pos.getY() ||
        ConfigManager.HIGHEST.get() < pos.getY()) {
      return PortalRejectReason.YGRID;
    }
    //    take the range [GRID - BUFFER, GRID + BUFFER]; AND [-BUFFER, +BUFFER]
    // if current x is inside this after taking remainder of x / GRID . ( for example x=1236, means 1236/500 = 236 )
    //means that if we get a good one of 1508 / 500 = 8 remainder, thats in buffer range 
    // what if you get 499.  then you have 499/500 = 0, but we want 1.
    if (!this.inGridLine(pos.getX())) {
      return PortalRejectReason.XGRID;
    }
    if (!this.inGridLine(pos.getZ())) {
      return PortalRejectReason.ZGRID;
    }
    if (this.inGridLine(pos.getX()) && this.inGridLine(pos.getZ())) {
      return PortalRejectReason.ALLOWED;
    }
    //
    //    int x = 498;
    //    int rem = x / GRID;
    //    LOGGER.info("a " + rem);
    //    x = 1499;
    //    rem = x / GRID;
    //    LOGGER.info("b " + rem);//rem=2 and  2xGRID = 1000 , must check (rem+1)
    //    x = 1505;
    //    rem = x / GRID;
    //    LOGGER.info("c " + rem);//rem=3 and  3xGRID = 1500 , checking current rem is enough
    return PortalRejectReason.ALLOWED;
  }

  private boolean inGridLine(int coord) {
    coord = Math.abs(coord);
    int rem = coord / ConfigManager.GRID.get();
    // so if my number is 1499 or 1001 or something,
    //then take 1000 and 1500, and check if im within either of those gridlines
    //if either one of these is within
    if (this.isWithinBuffer(coord, rem * ConfigManager.GRID.get())
        || this.isWithinBuffer(coord, (rem + 1) * ConfigManager.GRID.get())) {
      return true;
    }
    return false;
  }

  private boolean isWithinBuffer(int coord, int gridline) {
    //    take the range [GRID - BUFFER, GRID + BUFFER]; AND [-BUFFER, +BUFFER]
    // example [ 492, 508 ]
    int left = gridline - ConfigManager.BUFFER.get();
    int right = gridline + ConfigManager.BUFFER.get();
    boolean buff = left < coord && coord < right;
    //
    if (!buff) {
      //fails
      //[m[32m[10:45:09] [Render thread/INFO] [co.lo.wo.ExampleMod/]: -499it is within grid -8::8
      //[m[32m[10:45:09] [Render thread/INFO] [co.lo.wo.ExampleMod/]: -499it is within grid 492::508
      //      ExampleMod.LOGGER.info(coord + "it is within grid " + left + "::" + right);
    }
    //
    return buff;
  }
}
