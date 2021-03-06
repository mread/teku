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

package tech.pegasys.teku.cli.options;

import java.util.ArrayList;
import java.util.List;
import org.apache.tuweni.bytes.Bytes32;
import picocli.CommandLine.Option;
import tech.pegasys.teku.cli.converter.GraffitiConverter;

public class ValidatorOptions {
  @Option(
      names = {"--validator-keys"},
      paramLabel = "<KEY_DIR>:<PASS_DIR> | <KEY_FILE>:<PASS_FILE>",
      description =
          "<KEY_DIR>:<PASS_DIR> will find <KEY_DIR>/**.json, and expect to find <PASS_DIR>/**.txt.\n"
              + "<KEY_FILE>:<PASS_FILE> will expect that the file <KEY_FILE> exists, "
              + "and the file containing the password for it is <PASS_FILE>.\n"
              + "The path separator is operating system dependent, and should be ';' in windows rather than ':'.",
      split = ",",
      arity = "1..*")
  private List<String> validatorKeys = new ArrayList<>();

  @Option(
      names = {"--validators-key-files"},
      paramLabel = "<FILENAMES>",
      description = "The list of encrypted keystore files to load the validator keys from",
      split = ",",
      hidden = true,
      arity = "0..*")
  private List<String> validatorKeystoreFiles = new ArrayList<>();

  @Option(
      names = {"--validators-key-password-files"},
      paramLabel = "<FILENAMES>",
      description = "The list of password files to decrypt the validator keystore files",
      split = ",",
      hidden = true,
      arity = "0..*")
  private List<String> validatorKeystorePasswordFiles = new ArrayList<>();

  @Option(
      names = {"--validators-external-signer-public-keys"},
      paramLabel = "<STRINGS>",
      description = "The list of external signer public keys",
      split = ",",
      arity = "0..*")
  private List<String> validatorExternalSignerPublicKeys = new ArrayList<>();

  @Option(
      names = {"--validators-external-signer-url"},
      paramLabel = "<NETWORK>",
      description = "URL for the external signing service",
      arity = "1")
  private String validatorExternalSignerUrl = null;

  @Option(
      names = {"--validators-external-signer-timeout"},
      paramLabel = "<INTEGER>",
      description = "Timeout (in milliseconds) for the external signing service",
      arity = "1")
  private int validatorExternalSignerTimeout = 1000;

  @Option(
      names = {"--validators-graffiti"},
      converter = GraffitiConverter.class,
      paramLabel = "<GRAFFITI STRING>",
      description =
          "Graffiti to include during block creation (gets converted to bytes and padded to Bytes32).",
      arity = "1")
  private Bytes32 graffiti;

  @Option(
      names = {"--validators-performance-tracking-enabled"},
      paramLabel = "<BOOLEAN>",
      description = "Enable validator performance tracking and logging",
      fallbackValue = "true",
      arity = "0..1")
  private boolean validatorPerformanceTrackingEnabled = false;

  public boolean isValidatorPerformanceTrackingEnabled() {
    return validatorPerformanceTrackingEnabled;
  }

  @Option(
      names = {"--validators-keystore-locking-enabled"},
      paramLabel = "<BOOLEAN>",
      description = "Enable locking validator keystore files",
      arity = "1")
  private boolean validatorKeystoreLockingEnabled = true;

  public boolean isValidatorKeystoreLockingEnabled() {
    return validatorKeystoreLockingEnabled;
  }

  public List<String> getValidatorKeystoreFiles() {
    return validatorKeystoreFiles;
  }

  public List<String> getValidatorKeystorePasswordFiles() {
    return validatorKeystorePasswordFiles;
  }

  public List<String> getValidatorExternalSignerPublicKeys() {
    return validatorExternalSignerPublicKeys;
  }

  public String getValidatorExternalSignerUrl() {
    return validatorExternalSignerUrl;
  }

  public int getValidatorExternalSignerTimeout() {
    return validatorExternalSignerTimeout;
  }

  public Bytes32 getGraffiti() {
    return graffiti;
  }

  public List<String> getValidatorKeys() {
    return validatorKeys;
  }
}
