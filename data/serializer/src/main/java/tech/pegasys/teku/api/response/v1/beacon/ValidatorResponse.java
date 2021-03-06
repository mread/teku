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

package tech.pegasys.teku.api.response.v1.beacon;

import static tech.pegasys.teku.api.schema.SchemaConstants.EXAMPLE_UINT64;
import static tech.pegasys.teku.datastructures.util.BeaconStateUtil.compute_epoch_at_slot;
import static tech.pegasys.teku.util.config.Constants.FAR_FUTURE_EPOCH;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import tech.pegasys.teku.api.schema.Validator;
import tech.pegasys.teku.datastructures.state.BeaconState;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class ValidatorResponse {
  @JsonProperty("index")
  @Schema(
      type = "string",
      example = EXAMPLE_UINT64,
      description = "Index of validator in validator registry.")
  public final UInt64 index;

  @JsonProperty("balance")
  @Schema(
      type = "string",
      example = EXAMPLE_UINT64,
      description = "Current validator balance in gwei.")
  public final UInt64 balance;

  @JsonProperty("status")
  public final ValidatorStatus status;

  @JsonProperty("validator")
  public final Validator validator;

  @JsonCreator
  public ValidatorResponse(
      @JsonProperty("index") final UInt64 index,
      @JsonProperty("balance") final UInt64 balance,
      @JsonProperty("status") final ValidatorStatus status,
      @JsonProperty("validator") final Validator validator) {
    this.index = index;
    this.balance = balance;
    this.status = status;
    this.validator = validator;
  }

  public static ValidatorResponse fromState(final BeaconState state, final Integer index) {
    tech.pegasys.teku.datastructures.state.Validator validatorInternal =
        state.getValidators().get(index);
    final UInt64 current_epoch = compute_epoch_at_slot(state.getSlot());
    return new ValidatorResponse(
        UInt64.valueOf(index),
        validatorInternal.getEffective_balance(),
        getValidatorStatus(current_epoch, validatorInternal),
        new Validator(validatorInternal));
  }

  static ValidatorStatus getValidatorStatus(
      final UInt64 epoch, final tech.pegasys.teku.datastructures.state.Validator validator) {
    // pending
    if (validator.getActivation_epoch().isGreaterThan(epoch)) {
      return validator.getActivation_eligibility_epoch().equals(FAR_FUTURE_EPOCH)
          ? ValidatorStatus.pending_initialized
          : ValidatorStatus.pending_queued;
    }
    // active
    if (validator.getActivation_epoch().isLessThanOrEqualTo(epoch)
        && epoch.isLessThan(validator.getExit_epoch())) {
      if (validator.getExit_epoch().equals(FAR_FUTURE_EPOCH)) {
        return ValidatorStatus.active_ongoing;
      }
      return validator.isSlashed()
          ? ValidatorStatus.active_slashed
          : ValidatorStatus.active_exiting;
    }

    // exited
    if (validator.getExit_epoch().isLessThanOrEqualTo(epoch)
        && epoch.isLessThan(validator.getWithdrawable_epoch())) {
      return validator.isSlashed()
          ? ValidatorStatus.exited_slashed
          : ValidatorStatus.exited_unslashed;
    }

    // withdrawal
    if (validator.getWithdrawable_epoch().isLessThanOrEqualTo(epoch)) {
      return validator.getEffective_balance().isGreaterThan(UInt64.ZERO)
          ? ValidatorStatus.withdrawal_possible
          : ValidatorStatus.withdrawal_done;
    }
    throw new IllegalStateException("Unable to determine validator status");
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final ValidatorResponse that = (ValidatorResponse) o;
    return Objects.equals(index, that.index)
        && Objects.equals(balance, that.balance)
        && status == that.status
        && Objects.equals(validator, that.validator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(index, balance, status, validator);
  }
}
