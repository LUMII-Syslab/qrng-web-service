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
 * Thrown by the Quantis Java library to indicate an error while dealing with a
 * Quantis device.
 */
public class QuantisException extends Exception {

    /**
     * Constructs a new QuantisExecption with null as its detail message.
     */
    public QuantisException() {
    }

    /**
     * Constructs a new QuantisExecption with the specified detail message.
     * 
     * @param message the detail message. The detail message is saved for later
     *                retrieval by the Throwable.getMessage() method.
     */
    public QuantisException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of
     * (cause==null ? null : cause.toString()) (which typically contains the class
     * and detail message of cause).
     * 
     * @param cause the cause (which is saved for later retrieval by the
     *              Throwable.getCause() method). (A null value is permitted, and
     *              indicates that the cause is nonexistent or unknown.)
     */
    public QuantisException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new QuantisExecption with the specified detail message and
     * cause. Note that the detail message associated with cause is <em>not</em>
     * automatically incorporated in this exception's detail message.
     * 
     * @param message the detail message. The detail message is saved for later
     *                retrieval by the Throwable.getMessage() method.
     * @param cause   the cause (which is saved for later retrieval by the
     *                Throwable.getCause() method). (A null value is permitted, and
     *                indicates that the cause is nonexistent or unknown.)
     */
    public QuantisException(String message, Throwable cause) {
        super(message, cause);
    }
}
