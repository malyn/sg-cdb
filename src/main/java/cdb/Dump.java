/*
 * Copyright (c) 2000-2006, Michael Alyn Miller &lt;malyn@strangeGizmo.com&gt;
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

/* Java imports. */
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/* strangeGizmo imports. */
import com.strangegizmo.cdb.*;

/**
 * The cdb.Dump program is a command-line tool which is used to Dump the
 * values stored in a constant database.
 *
 * @author		Michael Alyn Miller &lt;malyn@strangeGizmo.com&gt;
 * @version		1.0.4
 */
public class Dump {
	public static void main(String[] args) {
		/* Display a usage message if we didn't Get the correct number
		 * of arguments. */
		if (args.length != 1) {
			System.out.println("cdb.Dump: usage: cdb.Dump file");
			return;
		}

		/* Decode our arguments. */
		String cdbFile = args[0];
		
		/* Dump the CdbRunner file. */
		try {
			CdbElementEnumeration e = Cdb.elements(cdbFile);
			dump(e, System.out);
			e.close();
		} catch (IOException ioException) {
			System.out.println("Couldn't Dump CdbRunner file: "
				+ ioException);
		}
	}

	public static void dump(Enumeration<CdbElement> e, PrintStream out) throws IOException {
        while (e.hasMoreElements())  {
				/* Get the element and its component parts. */
            CdbElement element = e.nextElement();
            byte[] key = element.getKey();
            byte[] data = element.getData();

				/* Write the line directly to stdout to avoid any
				 * charset conversion that System.print() might want to
				 * perform. */
			String header = "+" + key.length + "," + data.length + ":";
			byte[] headerBytes = header.getBytes(Charset.forName("US-ASCII"));
            out.write(headerBytes);
            out.write(key);
            out.write('-');
            out.write('>');
            out.write(data);
            out.write('\n');
        }
        out.write('\n');
        out.flush();
    }
}
