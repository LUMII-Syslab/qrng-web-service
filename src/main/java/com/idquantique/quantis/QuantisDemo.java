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

import java.math.BigInteger;

/**
 *
 * @author ID Quantique SA
 */
public class QuantisDemo {

  /**
   * @param args the command line arguments
   * @throws QuantisException
   */
  public static void main(String[] args) throws QuantisException {

    System.out.println("QuantisDemo for Java\n");

    System.out
        .println("Searching Quantis library in following path:\n" + System.getProperty("java.library.path") + "\n");

    System.out.println("Using Quantis Library v" + Quantis.GetLibVersion() + "\n");

    int countPci = Quantis.Count(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI);
    System.out.println("Found " + countPci + " Quantis PCI devices.");
    int countUsb = Quantis.Count(Quantis.QuantisDeviceType.QUANTIS_DEVICE_USB);
    System.out.println("Found " + countUsb + " Quantis USB devices.");

    Quantis quantis;
    if (countPci > 0) {
      System.out.println("Using first Quantis PCI device.");
      quantis = new Quantis(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI, 0);
    } else if (countUsb > 0) {
      System.out.println("Using first Quantis USB device.");
      quantis = new Quantis(Quantis.QuantisDeviceType.QUANTIS_DEVICE_USB, 0);
    } else {
      System.out.println("No Quants device installed.");
      return;
    }

    // quantis.ModulesDisable(0xF); // Disable all modules
    // quantis.ModulesEnable(0xF); // Enables all modules

    System.out.println("Board version: " + Integer.toHexString(quantis.GetBoardVersion()).toUpperCase());

    System.out.println("Serial number: " + quantis.GetSerialNumber());

    System.out.println("Manufacturer: " + quantis.GetManufacturer());

    System.out.println("DATA RATE: " + quantis.GetModulesDataRate());
    System.out.println("MASK: " + quantis.GetModulesMask());

    // By default, the RNG mode is set. To change it to SAMPLE mode, uncomment the line //#define
    // SAMPLE_MODE in file <path-to-Quantis>\Libs-Apps\Quantis\QuantisPcie_Windows.c
    // [docs, p.54]

/*    byte[] data = quantis.Read(4);
    System.out.println("Data: " + String.format("%X", new BigInteger(data)));
    System.out.println("Double: " + quantis.ReadDouble());
    System.out.println("Float: " + quantis.ReadFloat());
    System.out.println("Int: " + quantis.ReadInt());
    System.out.println("Short: " + quantis.ReadShort());*/
    long t = System.currentTimeMillis();
    System.out.println("starting...");
    for (long i=0; i<1; i++) {
      //System.out.println(i);
      byte[] data = quantis.Read(1024*1024);
    }
    long tEnd = System.currentTimeMillis();
    System.out.println("done in "+(tEnd-t)+" ms");
  }

}
