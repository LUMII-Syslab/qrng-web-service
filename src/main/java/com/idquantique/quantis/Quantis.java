/*
 * Quantis Library for Java
 *
 * Copyright (C) 2004-2020 ID Quantique SA, Carouge/Geneva, Switzerland
 * All rights reserved.
 *
 * ----------------------------------------------------------------------------
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions, and the following disclaimer,
 *    without modification.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY.
 *
 * ----------------------------------------------------------------------------
 *
 * Alternatively, this software may be distributed under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 *
 */
package com.idquantique.quantis;

/**
 * The quantis java library can be used to access Quantis Module. This library
 * is OS independent and has been tested with Windows and Linux.
 */
public class Quantis {

    // Load the Library containing the native code implementation
    // NOTE: You might need to configure your environment so the loadLibrary
    // method can find the native code library.
    static {
        try {
            System.loadLibrary("Quantis");
        }
        catch (Throwable e) { // Unsatisfied link errors are not Exceptions but Throwables!
            System.err.println("Quantis.dll ERROR");
            e.printStackTrace();
        }
    }

    /**
     * Type of Quantis device
     */
    public enum QuantisDeviceType {

        /** Quantis PCI or PCI-Express */
        QUANTIS_DEVICE_PCI(1),

        /** Quantis USB */
        QUANTIS_DEVICE_USB(2);

        private int deviceType;

        QuantisDeviceType(int deviceType) {
            this.deviceType = deviceType;
        }

        public int getType() {
            return deviceType;
        }
    }

    private QuantisDeviceType deviceType;
    private int deviceNumber;

    /**
     * Constructs a Quantis with the specified type and number.
     * 
     * @param deviceType   specify the type of Quantis device.
     * @param deviceNumber the number of the Quantis device.
     */
    public Quantis(QuantisDeviceType deviceType, int deviceNumber) {
        this.deviceType = deviceType;
        this.deviceNumber = deviceNumber;
    }

    /**
     * Resets the Quantis board. This function do not generally has to be called,
     * since the board is automatically reset.
     * 
     * @throws QuantisException when unable to perform the operation.
     */
    public void QuantisBoardReset() throws QuantisException {
        QuantisBoardReset(deviceType.getType(), deviceNumber);
    }

    /**
     * Returns the number of specific Quantis type devices that have been detected
     * on the system.
     * 
     * @param deviceType specify the type of Quantis device.
     * @return the number of Quantis devices that have been detected on the system.
     *         Returns 0 on error or when no card is installed.
     */
    public static int Count(QuantisDeviceType deviceType) {
        return QuantisCount(deviceType.getType());
    }

    /**
     * Get the version of the board.
     * 
     * @return the version of the board.
     * @throws QuantisException when unable to perform the operation.
     */
    public int GetBoardVersion() throws QuantisException {
        return QuantisGetBoardVersion(deviceType.getType(), deviceNumber);
    }

    /**
     * Returns the version of the driver as a number composed by the major and minor
     * number: <code>version = major.minor</code>.
     * 
     * @return the version of the driver.
     * @throws QuantisException when unable to perform the operation.
     */
    public float GetDriverVersion() throws QuantisException {
        return QuantisGetDriverVersion(deviceType.getType());
    }

    /**
     * Returns the library version as a number composed by the major and minor
     * number: <code>version = major.minor</code>
     * 
     * @return the library version.
     */
    public static float GetLibVersion() {
        return QuantisGetLibVersion();
    }

    /**
     * Get a pointer to the manufacturer's string of the Quantis device.
     * 
     * @return the manufacturer of the Quantis device or "Not available" when an
     *         error occured or when the device do not support the operation
     *         (currently only Quantis USB returns a valid string).
     */
    public String GetManufacturer() {
        return QuantisGetManufacturer(deviceType.getType(), deviceNumber);
    }

    /**
     * Returns the number of modules that have been detected on a Quantis device.
     * 
     * @return the number of detected modules.
     * @throws QuantisException when unable to perform the operation.
     * @see Quantis#GetModulesMask().
     */
    public int GetModulesCount() throws QuantisException {
        return QuantisGetModulesCount(deviceType.getType(), deviceNumber);
    }

    /**
     * Returns the data rate (in Bytes per second) provided by the Quantis device.
     * 
     * @return the data rate provided by the Quantis device.
     * @throws QuantisException when unable to perform the operation.
     */
    public int GetModulesDataRate() throws QuantisException {
        return QuantisGetModulesDataRate(deviceType.getType(), deviceNumber);
    }

    /**
     * Returns a bitmask of the modules that have been detected on a Quantis device,
     * where bit <em>n</em> is set if module <em>n</em> is present. For instance
     * when 5 (1101 in binary) is returned, it means that modules 0, 2 and 3 have
     * been detected.
     * 
     * @return a bitmask of the detected modules.
     * @throws QuantisException when unable to perform the operation.
     * @see Quantis#GetModulesStatus().
     */
    public int GetModulesMask() throws QuantisException {
        return QuantisGetModulesMask(deviceType.getType(), deviceNumber);
    }

    /**
     * Get the power status of the modules.
     * 
     * @return 1 if the modules are powered, 0 if the modules are not powered.
     * @throws QuantisException when unable to perform the operation.
     */
    public int GetModulesPower() throws QuantisException {
        return QuantisGetModulesPower(deviceType.getType(), deviceNumber);
    }

    /**
     * Returns the status of the modules on the device as a bitmask as defined in
     * QuantisGetModulesMask. Bit <em>n</em> is set (equal to 1) only when module
     * <em>n</em> is enabled and functional.
     * 
     * @return A bitmask with the status of the modules.
     * @throws QuantisException when unable to perform the operation.
     * @see Quantis#GetModulesMask().
     */
    public int GetModulesStatus() throws QuantisException {
        return QuantisGetModulesStatus(deviceType.getType(), deviceNumber);
    }

    /**
     * Get a pointer to the serial number string of the Quantis device.
     * 
     * @return the serial number of the Quantis device or "S/N not available" when
     *         an error occured or when the device do not support the operation
     *         (currently only Quantis USB returns a valid serial number).
     */
    public String GetSerialNumber() {
        return QuantisGetSerialNumber(deviceType.getType(), deviceNumber);
    }

    /**
     * Disable one ore more modules.
     * 
     * @param modulesMask a bitmask of the modules (as specified in
     *                    QuantisGetModulesMask) that must be disabled.
     * @throws QuantisException when unable to perform the operation.
     */
    public void ModulesDisable(int modulesMask) throws QuantisException {
        QuantisModulesDisable(deviceType.getType(), deviceNumber, modulesMask);
    }

    /**
     * Enable one ore more modules.
     * 
     * @param modulesMask a bitmask of the modules (as specified in
     *                    QuantisGetModulesMask) that must be enabled.
     * @throws QuantisException when unable to perform the operation.
     */
    public void ModulesEnable(int modulesMask) throws QuantisException {
        QuantisModulesEnable(deviceType.getType(), deviceNumber, modulesMask);
    }

    /**
     * Reset one or more modules. This function just call Quantis#ModulesDisable()
     * and then Quantis#ModulesEnable() with the provided modulesMask.
     * 
     * @param modulesMask a bitmask of the modules (as specified in
     *                    QuantisGetModulesMask) that must be reset.
     * @throws QuantisException when unable to perform the operation.
     */
    public void ModulesReset(int modulesMask) throws QuantisException {
        QuantisModulesReset(deviceType.getType(), deviceNumber, modulesMask);
    }

    /**
     * Reads random data from the Quantis device.
     * 
     * @param size the number of bytes to read (not larger than
     *             QUANTIS_MAX_READ_SIZE).
     * @return The number of read bytes on success.
     * @throws QuantisException when unable to perform the operation.
     */
    public byte[] Read(int size) throws QuantisException {
        return QuantisRead(deviceType.getType(), deviceNumber, size);
    }

    /**
     * Reads a random double floating precision value between 0.0 (inclusive) and
     * 1.0 (exclusive) from the Quantis device.
     * 
     * @return a random double floating precision value between 0.0 (inclusive) and
     *         1.0 (exclusive).
     * @throws QuantisException when unable to perform the operation.
     */
    public double ReadDouble() throws QuantisException {
        return QuantisReadDouble01(deviceType.getType(), deviceNumber);
    }

    /**
     * Reads a random number from the Quantis device and scale it to be between min
     * (inclusive) and max (exclusive).
     * 
     * @param min the minimal value the random number can take.
     * @param max the maximal value the random number can take.
     * @return a random double floating precision value between 0.0 (inclusive) and
     *         1.0 (exclusive).
     * @throws QuantisException when unable to perform the operation.
     */
    public double ReadDouble(double min, double max) throws QuantisException {
        return QuantisReadScaledDouble(deviceType.getType(), deviceNumber, min, max);
    }

    /**
     * Reads a random single floating precision value between 0.0 (inclusive) and
     * 1.0 (exclusive) from the Quantis device.
     * 
     * @return a random single floating precision value between 0.0 (inclusive) and
     *         1.0 (exclusive).
     * @throws QuantisException when unable to perform the operation.
     */
    public float ReadFloat() throws QuantisException {
        return QuantisReadFloat01(deviceType.getType(), deviceNumber);
    }

    /**
     * Reads a random number from the Quantis device and scale it to be between min
     * (inclusive) and max (exclusive).
     * 
     * @param min the minimal value the random number can take.
     * @param max the maximal value the random number can take.
     * @return a random single floating precision value between 0.0 (inclusive) and
     *         1.0 (exclusive).
     * @throws QuantisException when unable to perform the operation.
     */
    public float ReadFloat(float min, float max) throws QuantisException {
        return QuantisReadScaledFloat(deviceType.getType(), deviceNumber, min, max);

    }

    /**
     * Reads a random number from the Quantis device.
     * 
     * @return a random number.
     * @throws QuantisException when unable to perform the operation.
     */
    public int ReadInt() throws QuantisException {
        return QuantisReadInt(deviceType.getType(), deviceNumber);
    }

    /**
     * Reads a random number from the Quantis device and scale it to be between min
     * and max (inclusive).
     * 
     * @param min the minimal value the random number can take.
     * @param max the maximal value the random number can take.
     * @return a random number between min and max (inclusive).
     * @throws QuantisException when unable to perform the operation.
     */
    public int ReadInt(int min, int max) throws QuantisException {
        return QuantisReadScaledInt(deviceType.getType(), deviceNumber, min, max);
    }

    /**
     * Reads a random number from the Quantis device.
     * 
     * @return a random number.
     * @throws QuantisException when unable to perform the operation.
     */
    public short ReadShort() throws QuantisException {
        return QuantisReadShort(deviceType.getType(), deviceNumber);
    }

    /**
     * Reads a random number from the Quantis device and scale it to be between min
     * and max (inclusive).
     * 
     * @param min the minimal value the random number can take.
     * @param max the maximal value the random number can take.
     * @return a random number between min and max (inclusive).
     * @throws QuantisException when unable to perform the operation.
     */
    public short ReadShort(short min, short max) throws QuantisException {
        return QuantisReadScaledShort(deviceType.getType(), deviceNumber, min, max);
    }

    // -------------------- Native methods declaration --------------------
    private static native void QuantisBoardReset(int deviceType, int deviceNumber) throws QuantisException;

    private static native int QuantisCount(int deviceType);

    private static native int QuantisGetBoardVersion(int deviceType, int deviceNumber) throws QuantisException;

    private static native float QuantisGetDriverVersion(int deviceType) throws QuantisException;

    private static native float QuantisGetLibVersion();

    private static native String QuantisGetManufacturer(int deviceType, int deviceNumber);

    private static native int QuantisGetModulesCount(int deviceType, int deviceNumber) throws QuantisException;

    private static native int QuantisGetModulesDataRate(int deviceType, int deviceNumber) throws QuantisException;

    private static native int QuantisGetModulesMask(int deviceType, int deviceNumber) throws QuantisException;

    private static native int QuantisGetModulesPower(int deviceType, int deviceNumber) throws QuantisException;

    private static native int QuantisGetModulesStatus(int deviceType, int deviceNumber) throws QuantisException;

    private static native String QuantisGetSerialNumber(int deviceType, int deviceNumber);

    private static native void QuantisModulesDisable(int deviceType, int deviceNumber, int modulesMask)
            throws QuantisException;

    private static native void QuantisModulesEnable(int deviceType, int deviceNumber, int modulesMask)
            throws QuantisException;

    private static native void QuantisModulesReset(int deviceType, int deviceNumber, int modulesMask)
            throws QuantisException;

    private static native byte[] QuantisRead(int deviceType, int deviceNumber, int size) throws QuantisException;

    private static native double QuantisReadDouble01(int deviceType, int deviceNumber) throws QuantisException;

    private static native float QuantisReadFloat01(int deviceType, int deviceNumber) throws QuantisException;

    private static native int QuantisReadInt(int deviceType, int deviceNumber) throws QuantisException;

    private static native short QuantisReadShort(int deviceType, int deviceNumber) throws QuantisException;

    private static native double QuantisReadScaledDouble(int deviceType, int deviceNumber, double min, double max)
            throws QuantisException;

    private static native float QuantisReadScaledFloat(int deviceType, int deviceNumber, float min, float max)
            throws QuantisException;

    private static native int QuantisReadScaledInt(int deviceType, int deviceNumber, int min, int max)
            throws QuantisException;

    private static native short QuantisReadScaledShort(int deviceType, int deviceNumber, short min, short max)
            throws QuantisException;
}
