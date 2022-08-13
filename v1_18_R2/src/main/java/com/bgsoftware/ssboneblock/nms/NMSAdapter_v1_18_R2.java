package com.bgsoftware.ssboneblock.nms;

import com.bgsoftware.common.remaps.Remap;
import com.mojang.brigadier.StringReader;
import net.minecraft.commands.arguments.ArgumentNBTTag;
import net.minecraft.commands.arguments.blocks.ArgumentBlock;
import net.minecraft.commands.arguments.blocks.ArgumentTileLocation;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntityChest;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

public final class NMSAdapter_v1_18_R2 implements NMSAdapter {

    @Override
    public String getMappingsHash() {
        return ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion();
    }

    @Override
    public boolean isLegacy() {
        return false;
    }

    @Override
    public SimpleCommandMap getCommandMap() {
        return ((CraftServer) Bukkit.getServer()).getCommandMap();
    }

    @Remap(classPath = "net.minecraft.server.level.WorldGenRegion", name = "getBlockEntity", type = Remap.Type.METHOD, remappedName = "c_")
    @Remap(classPath = "net.minecraft.world.level.block.entity.BaseContainerBlockEntity", name = "setCustomName", type = Remap.Type.METHOD, remappedName = "a")
    @Override
    public void setChestName(Location chest, String name) {
        assert chest.getWorld() != null;
        World world = ((CraftWorld) chest.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(chest.getBlockX(), chest.getBlockY(), chest.getBlockZ());
        TileEntityChest tileEntityChest = (TileEntityChest) world.c_(blockPosition);
        assert tileEntityChest != null;
        tileEntityChest.a(CraftChatMessage.fromString(name)[0]);
    }

    @Remap(classPath = "net.minecraft.world.level.Level", name = "removeBlockEntity", type = Remap.Type.METHOD, remappedName = "m")
    @Remap(classPath = "net.minecraft.commands.arguments.blocks.BlockStateParser", name = "parse", type = Remap.Type.METHOD, remappedName = "a")
    @Remap(classPath = "net.minecraft.commands.arguments.blocks.BlockStateParser", name = "getState", type = Remap.Type.METHOD, remappedName = "b")
    @Remap(classPath = "net.minecraft.commands.arguments.blocks.BlockStateParser", name = "getProperties", type = Remap.Type.METHOD, remappedName = "a")
    @Remap(classPath = "net.minecraft.commands.arguments.blocks.BlockStateParser", name = "getNbt", type = Remap.Type.METHOD, remappedName = "c")
    @Remap(classPath = "net.minecraft.commands.arguments.blocks.BlockInput", name = "place", type = Remap.Type.METHOD, remappedName = "a")
    @Remap(classPath = "net.minecraft.commands.arguments.blocks.BlockInput", name = "getState", type = Remap.Type.METHOD, remappedName = "a")
    @Remap(classPath = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase", name = "getBlock", type = Remap.Type.METHOD, remappedName = "b")
    @Remap(classPath = "net.minecraft.world.level.LevelAccessor", name = "blockUpdated", type = Remap.Type.METHOD, remappedName = "a")
    @Override
    public void setBlock(Location location, Material type, byte data, String nbt) {
        assert location.getWorld() != null;

        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        worldServer.m(blockPosition);

        location.getBlock().setType(type);

        if (nbt != null) {
            try {
                ArgumentBlock argumentBlock = new ArgumentBlock(new StringReader(nbt), false).a(true);
                IBlockData blockData = argumentBlock.b();
                if (blockData != null) {
                    ArgumentTileLocation tileLocation = new ArgumentTileLocation(blockData,
                            argumentBlock.a().keySet(), argumentBlock.c());
                    tileLocation.a(worldServer, blockPosition, 2);
                    worldServer.a(blockPosition, tileLocation.a().b());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Remap(classPath = "net.minecraft.commands.arguments.CompoundTagArgument", name = "compoundTag", type = Remap.Type.METHOD, remappedName = "a")
    @Remap(classPath = "net.minecraft.world.entity.Entity", name = "readAdditionalSaveData", type = Remap.Type.METHOD, remappedName = "a")
    @Override
    public void applyNBTToEntity(org.bukkit.entity.LivingEntity bukkitEntity, String nbt) {
        try {
            NBTTagCompound tagCompound = ArgumentNBTTag.a().parse(new StringReader(nbt));
            ((CraftLivingEntity) bukkitEntity).getHandle().a(tagCompound);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity", name = "getMainHandItem", type = Remap.Type.METHOD, remappedName = "es")
    @Remap(classPath = "net.minecraft.server.level.WorldGenRegion", name = "getBlockState", type = Remap.Type.METHOD, remappedName = "a_")
    @Remap(classPath = "net.minecraft.world.item.ItemStack", name = "mineBlock", type = Remap.Type.METHOD, remappedName = "a")
    @Override
    public void simulateToolBreak(Player bukkitPlayer, org.bukkit.block.Block bukkitBlock) {
        EntityPlayer entityPlayer = ((CraftPlayer) bukkitPlayer).getHandle();

        ItemStack itemStack = entityPlayer.es();

        WorldServer worldServer = ((CraftWorld) bukkitBlock.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        IBlockData blockData = worldServer.a_(blockPosition);

        itemStack.a(worldServer, blockData, blockPosition, entityPlayer);
    }

}

