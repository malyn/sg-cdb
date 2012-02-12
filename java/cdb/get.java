/*
 * Copyright (c) 2000-2001, Michael Alyn Miller <malyn@strangeGizmo.com>
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

/* strangeGizmo imports. */
import com.strangegizmo.cdb.*;

/**
 * The cdb.get program is a command-line tool which is used to retrieve
 * data from a constant database.
 *
 * @author		Michael Alyn Miller <malyn@strangeGizmo.com>
 * @version		1.0
 */
public class get {
	public static void main(String[] args) throws Exception {
		/* Display a usage message if we didn't get the correct number
		 * of arguments. */
		if ((args.length < 2) || (args.length > 3)) {
			System.out.println("cdb.get: usage: cdb.get file key [skip]");
			return;
		}

		/* Parse the arguments. */
		String file = args[0];
		byte[] key = args[1].getBytes();
		int skip = 0;
		if (args.length == 3)
			skip = Integer.parseInt(args[2]);

		/* Create the CDB object. */
		Cdb cdb = new Cdb(file);
		cdb.findstart(key);

		/* Fetch the data. */
		byte[] data;
		do {
			data = cdb.findnext(key);
			if (data == null ) return;
		} while (skip-- != 0);

		/* Display the data. */
		System.out.write(data);
		System.out.flush();
	}
}
