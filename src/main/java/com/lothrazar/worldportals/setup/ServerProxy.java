package com.lothrazar.worldportals.setup;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class ServerProxy implements IProxy {

  @Override
  public World getClientWorld() {
    throw new IllegalStateException("Client side only code on the server");
  }

  @Override
  public PlayerEntity getClientPlayer() {
    // TODO Auto-generated method stub
    return null;
  }
}
