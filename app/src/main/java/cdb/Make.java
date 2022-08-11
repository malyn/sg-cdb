/*
 * Copyright (c) 2000-2001, Michael Alyn Miller &lt;malyn@strangeGizmo.com&gt;
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice unmodified, this list of conditions, and the following
 *    disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Michael Alyn Miller nor the names of the
 *    contributors to this software may be used to endorse or promote
 *    products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package cdb;

import io.github.duckasteroid.cdb.Cdb;
import io.github.duckasteroid.cdb.CdbMake;

import java.io.IOException;


/**
 * The cdb.Make program is a command-line tool which is used to create a
 * constant database.
 *
 * @author Michael Alyn Miller &lt;malyn@strangeGizmo.com&gt;
 * @version 1.0
 */
public class Make {
    public static void main(String[] args) {
        /* Display a usage message if we didn't Get the correct number
         * of arguments. */
        if (args.length < 2) {
            System.out.println("cdb.Make: usage: cdb.Make cdb_file temp_file [ignoreCdb]");
            return;
        }
        /* Decode our arguments. */
        String cdbFile = args[0];
        String tempFile = args[1];

        /* Load the ignoreCdb if requested. */
        Cdb ignoreCdb = null;
        if (args.length > 3) {
            try {
                ignoreCdb = new Cdb(args[2]);
            } catch (IOException ioException) {
                System.out.println("Couldn't load `ignore' CdbRunner file: "
                        + ioException);
            }
        }

        /* Create the CdbRunner file. */
        try {
            CdbMake.make(System.in, cdbFile, tempFile, ignoreCdb);
        } catch (IOException ioException) {
            System.out.println("Couldn't create CdbRunner file: "
                    + ioException);
        }
    }
}
