package org.unichain.core.net.message;

import org.unichain.core.capsule.BlockCapsule.BlockId;
import org.unichain.protos.Protocol;
import org.unichain.protos.Protocol.BlockInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockInventoryMessage extends UnichainMessage {

  protected BlockInventory blockInventory;

  public BlockInventoryMessage(byte[] data) throws Exception {
    super(data);
    this.type = MessageTypes.BLOCK_INVENTORY.asByte();
    this.blockInventory = Protocol.BlockInventory.parseFrom(data);
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }

  private BlockInventory getBlockInventory() {
    return blockInventory;
  }

  public BlockInventoryMessage(List<BlockId> blockIds, BlockInventory.Type type) {
    BlockInventory.Builder invBuilder = BlockInventory.newBuilder();
    blockIds.forEach(blockId -> {
      BlockInventory.BlockId.Builder b = BlockInventory.BlockId.newBuilder();
      b.setHash(blockId.getByteString());
      b.setNumber(blockId.getNum());
      invBuilder.addIds(b);
    });

    invBuilder.setType(type);
    blockInventory = invBuilder.build();
    this.type = MessageTypes.BLOCK_INVENTORY.asByte();
    this.data = blockInventory.toByteArray();
  }

  public List<BlockId> getBlockIds() {
    return getBlockInventory().getIdsList().stream()
        .map(blockId -> new BlockId(blockId.getHash(), blockId.getNumber()))
        .collect(Collectors.toCollection(ArrayList::new));
  }

}
