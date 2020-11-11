package com.lothrazar.worldportals;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
    if (!(event.getWorld() instanceof World)) {
      return;
    }
    World world = (World) event.getWorld();
    PortalRejectReason allowed = this.allowedHere(world, pos);
    if (allowed != PortalRejectReason.ALLOWED) {
      event.setCanceled(true);
      //      world.extinguishFire(null, pos, Direction.UP);
      if (world.getBlockState(pos).getBlock() instanceof AbstractFireBlock) {
        //extinguish
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
      }
      //      LatticePortalsMod.LOGGER.info("nope " + allowed);
      if (world.isRemote) {
        String allowedString = this.getAllowedString(allowed);
        LatticePortalsMod.proxy.getClientPlayer().sendStatusMessage(new TranslationTextComponent(allowedString), true);
        world.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, pos.getX(), pos.getY(), pos.getZ(), 0, 0.1, 0);
        world.addParticle(ParticleTypes.BUBBLE, pos.getX(), pos.getY(), pos.getZ(), 0, 0.1, 0);
        world.addParticle(ParticleTypes.BUBBLE, pos.getX(), pos.getY(), pos.getZ(), 0, 0.1, 0);
        world.addParticle(ParticleTypes.BUBBLE, pos.getX(), pos.getY(), pos.getZ(), 0, 0.1, 0);
        world.addParticle(ParticleTypes.BUBBLE, pos.getX(), pos.getY(), pos.getZ(), 0, 0.1, 0);
        world.addParticle(ParticleTypes.BARRIER, pos.getX(), pos.getY(), pos.getZ(), 0, 0.1, 0);
        LatticePortalsMod.proxy.getClientPlayer().sendStatusMessage(new TranslationTextComponent("nope"), true);
        //chat message 
      }
    }
  }

  public static String lang(String message) {
    TranslationTextComponent t = new TranslationTextComponent(message);
    return t.getString();
  }

  // F3 screen text overlay event
  @SubscribeEvent
  @OnlyIn(Dist.CLIENT)
  public void onDebugOverlay(RenderGameOverlayEvent.Text event) {
    BlockPos pos = LatticePortalsMod.proxy.getClientPlayer().getPosition();//.getPosition();
    if (Minecraft.getInstance().gameSettings.showDebugInfo == false) {
      return;
    } //if f3 is not pressed
    String allowedString = this.getAllowedString(this.allowedHere(LatticePortalsMod.proxy.getClientWorld(), pos));
    event.getLeft().add(allowedString);
  }

  private String getAllowedString(PortalRejectReason allowedHere) {
    return lang(LatticePortalsMod.MODID + ".f3." + allowedHere.name().toLowerCase());
  }

  private PortalRejectReason allowedHere(World world, BlockPos pos) {
    //    World.OVERWORLD = world.getDimensionKey()
    boolean isOverworld = world.getDimensionKey() != World.OVERWORLD;
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
