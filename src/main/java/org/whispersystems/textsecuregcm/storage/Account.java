/**
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm.storage;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Account {

  public static final int MEMCACHE_VERION = 5;

  @JsonProperty
  private String number;

  @JsonProperty
  private Set<Device> devices = new HashSet<>();

  @JsonProperty
  private String identityKey;

  @JsonIgnore
  private Device authenticatedDevice;

  @JsonProperty
  private long deviceIdHighMark;

  public Account() {}

  @VisibleForTesting
  public Account(String number, Set<Device> devices) {
    this.number  = number;
    this.devices = devices;
  }

  public Optional<Device> getAuthenticatedDevice() {
    return Optional.fromNullable(authenticatedDevice);
  }

  public void setAuthenticatedDevice(Device device) {
    this.authenticatedDevice = device;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public String getNumber() {
    return number;
  }

  public void addDevice(Device device) {
    this.devices.remove(device);
    this.devices.add(device);
    if (deviceIdHighMark == 0 || device.getId() > deviceIdHighMark) {
      this.deviceIdHighMark = device.getId();
    }
  }

  public void removeDevice(long deviceId) {
    this.devices.remove(new Device(deviceId, null, null, null, null, null, null, null, false, 0, null, 0, 0, false, "NA"));
  }

  public Set<Device> getDevices() {
    return devices;
  }

  public Optional<Device> getDevice(long deviceId) {
    for (Device device : devices) {
      if (device.getId() == deviceId) {
        return Optional.of(device);
      }
    }

    return Optional.absent();
  }

  public boolean isVoiceSupported() {
    for (Device device : devices) {
      if (device.isActive() && device.isVoiceSupported()) {
        return true;
      }
    }

    return false;
  }

  public boolean isActive() {
    return
        getActiveDeviceCount() > 0 &&
        getLastSeen() > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365));
  }

  public long getNextDeviceId() {
    long highestDevice = deviceIdHighMark == 0 ? 1 : deviceIdHighMark;

    /* As a precaution for older databases that didn't use deviceIdHighMark. */
    for (Device device : devices) {
      if (device.getId() > highestDevice) {
        highestDevice = device.getId();
      }
    }

    return highestDevice + 1;
  }

  public int getActiveDeviceCount() {
    int count = 0;

    for (Device device : devices) {
      if (device.isActive()) count++;
    }

    return count;
  }

  public boolean isRateLimited() {
    return true;
  }

  public Optional<String> getRelay() {
    return Optional.absent();
  }

  public void setIdentityKey(String identityKey) {
    this.identityKey = identityKey;
  }

  public String getIdentityKey() {
    return identityKey;
  }

  public long getLastSeen() {
    long lastSeen = 0;

    for (Device device : devices) {
      if (device.getLastSeen() > lastSeen) {
        lastSeen = device.getLastSeen();
      }
    }

    return lastSeen;
  }
}
