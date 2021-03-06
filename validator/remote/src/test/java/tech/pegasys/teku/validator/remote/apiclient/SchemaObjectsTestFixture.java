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

package tech.pegasys.teku.validator.remote.apiclient;

import java.util.List;
import tech.pegasys.teku.api.response.GetForkResponse;
import tech.pegasys.teku.api.schema.Attestation;
import tech.pegasys.teku.api.schema.BLSPubKey;
import tech.pegasys.teku.api.schema.BLSSignature;
import tech.pegasys.teku.api.schema.BeaconBlock;
import tech.pegasys.teku.api.schema.SignedAggregateAndProof;
import tech.pegasys.teku.api.schema.SignedBeaconBlock;
import tech.pegasys.teku.api.schema.SubnetSubscription;
import tech.pegasys.teku.api.schema.ValidatorDuties;
import tech.pegasys.teku.api.schema.ValidatorDutiesRequest;
import tech.pegasys.teku.datastructures.util.DataStructureUtil;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class SchemaObjectsTestFixture {

  private final DataStructureUtil dataStructureUtil = new DataStructureUtil();

  public GetForkResponse getForkResponse() {
    return new GetForkResponse(dataStructureUtil.randomForkInfo());
  }

  public BLSPubKey BLSPubKey() {
    return new BLSPubKey(dataStructureUtil.randomPublicKey());
  }

  public BLSSignature BLSSignature() {
    return new BLSSignature(dataStructureUtil.randomSignature());
  }

  public ValidatorDutiesRequest validatorDutiesRequest() {
    return new ValidatorDutiesRequest(UInt64.ONE, List.of(BLSPubKey()));
  }

  public ValidatorDuties validatorDuties() {
    return new ValidatorDuties(BLSPubKey(), 1, 1, 1, 1, List.of(UInt64.ONE), UInt64.ONE);
  }

  public BeaconBlock beaconBlock() {
    return new BeaconBlock(dataStructureUtil.randomBeaconBlock(UInt64.ONE));
  }

  public SignedBeaconBlock signedBeaconBlock() {
    return new SignedBeaconBlock(dataStructureUtil.randomSignedBeaconBlock(UInt64.ONE));
  }

  public Attestation attestation() {
    return new Attestation(dataStructureUtil.randomAttestation());
  }

  public SubnetSubscription subnetSubscription() {
    return new SubnetSubscription(1, UInt64.ONE);
  }

  public SignedAggregateAndProof signedAggregateAndProof() {
    return new SignedAggregateAndProof(dataStructureUtil.randomSignedAggregateAndProof());
  }
}
