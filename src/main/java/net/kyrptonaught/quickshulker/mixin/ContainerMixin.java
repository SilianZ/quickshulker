package net.kyrptonaught.quickshulker.mixin;

import net.kyrptonaught.quickshulker.ItemInventoryContainer;
import net.kyrptonaught.quickshulker.api.Util;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;


@Mixin(ScreenHandler.class)
public abstract class ContainerMixin implements ItemInventoryContainer {

    @Unique
    ItemStack openededStack;


    @Shadow
    @Final
    public int syncId;


    @Shadow @Final public DefaultedList<Slot> slots;

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    public void QS$onClick(int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (slotId > 0 && slotId < slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot != null && slot.inventory instanceof PlayerInventory)
                if (hasItem()) {
                    if (Util.areItemsEqual(slot.getStack(), getOpenedItem())) {
                        ci.cancel();
                        if (player instanceof ServerPlayerEntity) {
                            ServerPlayerEntity sPlayer = (ServerPlayerEntity) player;
                            sPlayer.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, slotId, slot.getStack()));
                        }
                    }
                }
        }
    }

    @Unique
    @Override
    public ItemStack getOpenedItem() {
        return openededStack;
    }

    @Unique
    @Override
    public void setOpenedItem(ItemStack openedItem) {
        if (!Util.isEnderChest(openedItem))
            this.openededStack = openedItem;
    }
}
