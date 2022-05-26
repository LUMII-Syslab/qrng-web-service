/*
 * Quantis Library for Java
 *
 * Copyright (c) 2010 Andxor Soluzioni Informatiche S.r.l.
 *                    Cinisello Balsamo (MI), Italy
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

import java.io.IOException;
import java.security.SecureRandomSpi;

/**
 * This class provides an implementation of SecureRandom for the Quantis Randon
 * Number Generator (QRNG).
 *
 * A caller obtains a QuantisSecureRandom instance via a constructor or one of
 * the getInstance methods:
 * 
 * <pre>
 * // Uses first available Quantis device
 * QuantisSecureRandom random = new QuantisSecureRandom();
 *
 * // Uses Quantis USB device #1
 * QuantisSecureRandom random = new QuantisSecureRandom(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI, 0);
 * </pre>
 *
 * To retrieve random bytes, invoke the following methods:
 * 
 * <pre>
 * QuantisSecureRandom random = new QuantisSecureRandom();
 * byte randomBytes[] = new byte[20];
 * random.nextBytes(randomBytes);
 * </pre>
 *
 * Callers may also invoke the <code>generateSeed</code> method to generate a
 * given number of seed bytes (to seed other random number generators, for
 * example):
 * 
 * <pre>
 * byte seed[] = random.generateSeed(20);
 * </pre>
 *
 * @see SecureRandomSpi
 */
public class QuantisSecureRandom extends SecureRandomSpi {

    private Quantis rng = null;

    /**
     * Constructs a Quantis secure random number generator (RNG).
     * 
     * The first Quantis device that is found is used as input.
     *
     * Class first searches for Quantis PCI/PCIe devices, the for Quantis USB
     * devices.
     * 
     * @throws java.io.IOException if no Quantis device is found.
     */
    public QuantisSecureRandom() throws IOException {
        if (Quantis.Count(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI) > 0) {
            rng = new Quantis(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI, 0);
        } else if (Quantis.Count(Quantis.QuantisDeviceType.QUANTIS_DEVICE_USB) > 0) {
            rng = new Quantis(Quantis.QuantisDeviceType.QUANTIS_DEVICE_USB, 0);
        } else {
            throw new IOException("No Quantis device found");
        }
    }

    /**
     * Constructs a Quantis secure random number generator (RNG) using given Quantis
     * device as input.
     * 
     * @param deviceType   the Quantis device type.
     * @param deviceNumber the device number.
     * @throws java.io.IOException if the given Quantis device is not found.
     */
    public QuantisSecureRandom(Quantis.QuantisDeviceType deviceType, int deviceNumber) throws IOException {
        if (Quantis.Count(deviceType) > deviceNumber) {
            rng = new Quantis(deviceType, deviceNumber);
        } else {
            throw new IOException("No Quantis device found");
        }
    }

    /**
     * Returns the given number of seed bytes. This call may be used to seed other
     * random number generators.
     * 
     * @param numBytes the number of seed bytes to generate.
     * @return the seed bytes.
     * @throws java.lang.RuntimeException if unable to generate random numbers.
     */
    @Override
    protected byte[] engineGenerateSeed(int numBytes) throws RuntimeException {
        try {
            return rng.Read(numBytes);
        } catch (QuantisException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a user-specified number of random bytes.
     * 
     * @param bytes the array to be filled in with random bytes.
     * @throws java.lang.RuntimeException if unable to generate random numbers.
     */
    @Override
    protected void engineNextBytes(byte[] bytes) throws RuntimeException {
        int len = bytes.length;
        byte[] buffer = engineGenerateSeed(len);
        System.arraycopy(buffer, 0, bytes, 0, len);
    }

    /**
     * Reseeds this random object. Since the Quantis RNG do not needs seed, calling
     * this function is useless and the seed is ignored.
     * 
     * @param seed the seed.
     */
    @Override
    protected void engineSetSeed(byte[] seed) {
        // Quantis do not need a seed.
        // There is nothing to do here.
    }
}
