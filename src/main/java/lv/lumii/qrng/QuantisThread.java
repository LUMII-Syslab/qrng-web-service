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

package lv.lumii.qrng;

import com.idquantique.quantis.Quantis;
import com.idquantique.quantis.QuantisException;

/**
 *
 * @author ID Quantique SA
 */
public class QuantisThread extends Thread {

  private Quantis.QuantisDeviceType deviceType;
  private int deviceIndex;

  private static long totalSpeed = 0;
  private static synchronized void addSpeed(long delta) {
    totalSpeed += delta;
  }

  public QuantisThread(Quantis.QuantisDeviceType deviceType, int deviceIndex) {
    this.deviceType = deviceType;
    this.deviceIndex = deviceIndex;
  }


  @Override
  public void run() {
    Quantis quantis = new Quantis(deviceType, deviceIndex);
    System.out.println("Replenishing thread started using "+deviceType.name()+" device #"+deviceIndex+"...");

    long currentDeviceSpeed = 0;
    try {
      currentDeviceSpeed = quantis.GetModulesDataRate()/1024*1024;
      // ^^^ making divisible by 1024, since our blocks are 1024 bytes each
      addSpeed(currentDeviceSpeed);

      // REPLENISH THE BUFFER HERE

    } catch (QuantisException e) {
      e.printStackTrace();
    }
    finally {
      addSpeed(-currentDeviceSpeed);
    }

  }

  public static void launchAllThreads() throws QuantisException {
    int countPci = Quantis.Count(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI);
    System.out.println("Found " + countPci + " Quantis PCI devices.");
    int countUsb = Quantis.Count(Quantis.QuantisDeviceType.QUANTIS_DEVICE_USB);
    System.out.println("Found " + countUsb + " Quantis USB devices.");

    for (int i=0; i<countPci; i++)
      new QuantisThread(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI, i).start();

    for (int i=0; i<countUsb; i++)
      new QuantisThread(Quantis.QuantisDeviceType.QUANTIS_DEVICE_USB, i).start();

    if ((countPci==0) && (countUsb==0))
      throw new QuantisException("No Quantis device installed.");
  }

  public static long getCurrentTotalSpeed() {
    return totalSpeed;
  }

  /**
   * @param args the command line arguments
   * @throws QuantisException
   */
  public static void main(String[] args) throws QuantisException {

    System.out.println("QuantisDemo for Java "+System.getProperty("os.name")+"/"+System.getProperty("os.arch")+"\n");

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
