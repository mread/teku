/*
 * Copyright 2020 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.storage.server.rocksdb.dataaccess;

import com.google.errorprone.annotations.MustBeClosed;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlock;
import tech.pegasys.teku.datastructures.blocks.SlotAndBlockRoot;
import tech.pegasys.teku.datastructures.state.BeaconState;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.storage.server.rocksdb.core.ColumnEntry;
import tech.pegasys.teku.storage.server.rocksdb.core.RocksDbAccessor;
import tech.pegasys.teku.storage.server.rocksdb.schema.V4SchemaFinalized;

public class V4FinalizedRocksDbDao implements RocksDbFinalizedDao {
  private final RocksDbAccessor db;
  private final UInt64 stateStorageFrequency;

  public V4FinalizedRocksDbDao(final RocksDbAccessor db, final long stateStorageFrequency) {
    this.db = db;
    this.stateStorageFrequency = UInt64.valueOf(stateStorageFrequency);
  }

  @Override
  public void close() throws Exception {
    db.close();
  }

  @Override
  public Optional<SignedBeaconBlock> getFinalizedBlockAtSlot(final UInt64 slot) {
    return db.get(V4SchemaFinalized.FINALIZED_BLOCKS_BY_SLOT, slot);
  }

  @Override
  public Optional<SignedBeaconBlock> getLatestFinalizedBlockAtSlot(final UInt64 slot) {
    return db.getFloorEntry(V4SchemaFinalized.FINALIZED_BLOCKS_BY_SLOT, slot)
        .map(ColumnEntry::getValue);
  }

  @Override
  public Optional<BeaconState> getLatestAvailableFinalizedState(final UInt64 maxSlot) {
    return db.getFloorEntry(V4SchemaFinalized.FINALIZED_STATES_BY_SLOT, maxSlot)
        .map(ColumnEntry::getValue);
  }

  @Override
  @MustBeClosed
  public Stream<SignedBeaconBlock> streamFinalizedBlocks(
      final UInt64 startSlot, final UInt64 endSlot) {
    return db.stream(V4SchemaFinalized.FINALIZED_BLOCKS_BY_SLOT, startSlot, endSlot)
        .map(ColumnEntry::getValue);
  }

  @Override
  public Optional<UInt64> getSlotForFinalizedBlockRoot(final Bytes32 blockRoot) {
    return db.get(V4SchemaFinalized.SLOTS_BY_FINALIZED_ROOT, blockRoot);
  }

  @Override
  public Optional<UInt64> getSlotForFinalizedStateRoot(final Bytes32 stateRoot) {
    return db.get(V4SchemaFinalized.SLOTS_BY_FINALIZED_STATE_ROOT, stateRoot);
  }

  @Override
  public Optional<SlotAndBlockRoot> getSlotAndBlockRootForFinalizedStateRoot(
      final Bytes32 stateRoot) {
    Optional<UInt64> maybeSlot = db.get(V4SchemaFinalized.SLOTS_BY_FINALIZED_STATE_ROOT, stateRoot);
    return maybeSlot.flatMap(
        slot ->
            getFinalizedBlockAtSlot(slot)
                .map(block -> new SlotAndBlockRoot(slot, block.getRoot())));
  }

  @Override
  public Optional<SignedBeaconBlock> getFinalizedBlock(final Bytes32 root) {
    return db.get(V4SchemaFinalized.SLOTS_BY_FINALIZED_ROOT, root)
        .flatMap(this::getFinalizedBlockAtSlot);
  }

  @Override
  @MustBeClosed
  public FinalizedUpdater finalizedUpdater() {
    return new V4FinalizedRocksDbDao.V4FinalizedUpdater(db, stateStorageFrequency);
  }

  private static class V4FinalizedUpdater implements FinalizedUpdater {
    private final RocksDbAccessor.RocksDbTransaction transaction;
    private final UInt64 stateStorageFrequency;
    private Optional<UInt64> lastStateStoredSlot;

    V4FinalizedUpdater(final RocksDbAccessor db, final UInt64 stateStorageFrequency) {
      this.transaction = db.startTransaction();
      this.stateStorageFrequency = stateStorageFrequency;
      lastStateStoredSlot =
          db.getLastEntry(V4SchemaFinalized.FINALIZED_STATES_BY_SLOT).map(ColumnEntry::getKey);
    }

    @Override
    public void addFinalizedBlock(final SignedBeaconBlock block) {
      transaction.put(V4SchemaFinalized.SLOTS_BY_FINALIZED_ROOT, block.getRoot(), block.getSlot());
      transaction.put(V4SchemaFinalized.FINALIZED_BLOCKS_BY_SLOT, block.getSlot(), block);
    }

    @Override
    public void addFinalizedState(final Bytes32 blockRoot, final BeaconState state) {
      if (lastStateStoredSlot.isPresent()) {
        UInt64 nextStorageSlot = lastStateStoredSlot.get().plus(stateStorageFrequency);
        if (state.getSlot().compareTo(nextStorageSlot) >= 0) {
          addFinalizedState(state);
        }
      } else {
        addFinalizedState(state);
      }
    }

    @Override
    public void addFinalizedStateRoot(final Bytes32 stateRoot, final UInt64 slot) {
      transaction.put(V4SchemaFinalized.SLOTS_BY_FINALIZED_STATE_ROOT, stateRoot, slot);
    }

    private void addFinalizedState(final BeaconState state) {
      transaction.put(V4SchemaFinalized.FINALIZED_STATES_BY_SLOT, state.getSlot(), state);
      lastStateStoredSlot = Optional.of(state.getSlot());
    }

    @Override
    public void commit() {
      // Commit db updates
      transaction.commit();
      close();
    }

    @Override
    public void cancel() {
      transaction.rollback();
      close();
    }

    @Override
    public void close() {
      transaction.close();
    }
  }
}
