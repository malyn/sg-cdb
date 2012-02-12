/*
 * Copyright (c) 2000-2006, Michael Alyn Miller <malyn@strangeGizmo.com>
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
import java.util.*;

/* strangeGizmo imports. */
import com.strangegizmo.cdb.*;

/**
 * The cdb.dump program is a command-line tool which is used to dump the
 * values stored in a constant database.
 *
 * @author		Michael Alyn Miller <malyn@strangeGizmo.com>
 * @version		1.0.4
 */
public class dump {
	public static void main(String[] args) {
		/* Display a usage message if we didn't get the correct number
		 * of arguments. */
		if (args.length != 1) {
			System.out.println("cdb.dump: usage: cdb.dump file");
			return;
		}

		/* Decode our arguments. */
		String cdbFile = args[0];
		
		/* Dump the CDB file. */
		try {
			Enumeration e = Cdb.elements(cdbFile);
			while (e.hasMoreElements())  {
				/* Get the element and its component parts. */
				CdbElement element = (CdbElement)e.nextElement();
				byte[] key = element.getKey();
				byte[] data = element.getData();

				/* Write the line directly to stdout to avoid any
				 * charset conversion that System.print() might want to
				 * perform. */
				System.out.write(
					("+" + key.length + "," + data.length + ":").getBytes());
				System.out.write(key);
				System.out.write('-');
				System.out.write('>');
				System.out.write(data);
				System.out.write('\n');
			}
			System.out.write('\n');
		} catch (IOException ioException) {
			System.out.println("Couldn't dump CDB file: "
				+ ioException);
		}
	}
}
