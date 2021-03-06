/*
 * unichain-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * unichain-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.unichain.core.config.args;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.unichain.common.crypto.ECKey;
import org.unichain.common.utils.ByteArray;
import org.unichain.core.config.Parameter.ChainConstant;

import java.util.List;

@Slf4j(topic = "app")
public class LocalWitnesses {

  @Getter
  private List<String> privateKeys = Lists.newArrayList();

  private byte[] witnessAccountAddress;

  public LocalWitnesses() {
  }

  public LocalWitnesses(String privateKey) {
    addPrivateKeys(privateKey);
  }

  public LocalWitnesses(List<String> privateKeys) {
    setPrivateKeys(privateKeys);
  }

  public void setWitnessAccountAddress(final byte[] localWitnessAccountAddress) {
    this.witnessAccountAddress = localWitnessAccountAddress;
  }

  public byte[] getWitnessAccountAddress() {
    if (witnessAccountAddress == null) {
      byte[] privateKey = ByteArray.fromHexString(getPrivateKey());
      final ECKey ecKey = ECKey.fromPrivate(privateKey);
      this.witnessAccountAddress = ecKey.getAddress();
    }
    return witnessAccountAddress;
  }

  public void initWitnessAccountAddress() {
    if (witnessAccountAddress == null) {
      byte[] privateKey = ByteArray.fromHexString(getPrivateKey());
      final ECKey ecKey = ECKey.fromPrivate(privateKey);
      this.witnessAccountAddress = ecKey.getAddress();
    }
  }

  /**
   * Private key of ECKey.
   */
  public void setPrivateKeys(final List<String> privateKeys) {
    if (CollectionUtils.isEmpty(privateKeys)) {
      return;
    }
    for (String privateKey : privateKeys) {
      validate(privateKey);
    }
    this.privateKeys = privateKeys;
  }

  private void validate(String privateKey) {
    if (StringUtils.startsWithIgnoreCase(privateKey, "0X")) {
      privateKey = privateKey.substring(2);
    }

    if (StringUtils.isNotBlank(privateKey)
        && privateKey.length() != ChainConstant.PRIVATE_KEY_LENGTH) {
      throw new IllegalArgumentException(
          "Private key(" + privateKey + ") must be " + ChainConstant.PRIVATE_KEY_LENGTH
              + "-bits hex string.");
    }
  }

  public void addPrivateKeys(String privateKey) {
    validate(privateKey);
    this.privateKeys.add(privateKey);
  }

  //get the first one recently
  public String getPrivateKey() {
    if (CollectionUtils.isEmpty(privateKeys)) {
      logger.warn("privateKey is null");
      return null;
    }
    return privateKeys.get(0);
  }

}
