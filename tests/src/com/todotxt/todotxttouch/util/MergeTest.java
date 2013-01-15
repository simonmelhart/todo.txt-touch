/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch.util;

import junit.framework.TestCase;

import com.todotxt.todotxttouch.remote.RemoteConflictException;

public class MergeTest extends TestCase {
	public void testTrivial() {
		// No actual merges in these cases
		assertEquals("", Util.threeWayMerge("", "", ""));
		assertEquals("abc", Util.threeWayMerge("abc", "abc", "abc"));
		assertEquals("", Util.threeWayMerge("abc", "", "abc"));
		assertEquals("", Util.threeWayMerge("abc", "abc", ""));
	}

	public void testCompletion() {
		assertEquals("abc\nx def",
				Util.threeWayMerge("abc\ndef", "abc\nx def", "abc\ndef"));
		assertEquals("abc\nx def",
				Util.threeWayMerge("abc\ndef", "abc\ndef", "abc\nx def"));
		assertEquals("x abc\nx def",
				Util.threeWayMerge("abc\ndef", "abc\nx def", "x abc\ndef"));

	}

	public void testSameLineChange() {
		assertEquals("abc\nx def @home",
				Util.threeWayMerge("abc\ndef", "abc\nx def", "abc\ndef @home"));
		assertEquals("x fix spelling\nx def @home", Util.threeWayMerge(
				"fix spellnig\ndef", "x fix spellnig\nx def",
				"fix spelling\ndef @home"));

	}

	public void testReorder() {
		// Reorder isn't a high priority and proves quite difficult
		assertEquals(
				"Dying of thirst...\nAble to run\nCan't do this\nBut not really\n",
				Util.threeWayMerge(
						"Able to run\nBut not really\nCan't do this\nDying of thirst...\n",
						"Able to run\nCan't do this\nBut not really\nDying of thirst...\n",
						"Dying of thirst...\nAble to run\nBut not really\nCan't do this\n"));

	}

	public void testConflict() {
		// Marked for completion twice
		assertEquals("abc\nx def",
				Util.threeWayMerge("abc\ndef", "abc\nx def", "abc\nx def"));
		assertEquals("x Send flowers\nx 2011-02-15 Valentines",
				Util.threeWayMerge("Send flowers\nValentines",
						"x Send flowers\nx 2011-02-14 Valentines",
						"x Send flowers\nx 2011-02-15 Valentines"));

	}

	public void testLarger() {
		// These are real-world cases for offline use
		assertEquals(
				"Verify a large merge, a real-world case for offline use.",
				"x 2012-11-09 Sell VTX +RetireEarly @Internet\n"
						+ "x 2012-11-09 Buy energy-efficient bulbs +RetireEarly\n"
						+ "(K) Mark 'War and Peace' as read @Internet +Reading\n"
						+ "(E) Customize desktop @Internet +PeaceAndSerenity\n"
						+ "Talk to Grandpa @Phone\n"
						+ "x 2012-11-09 Call Mom +FamilialPeace @Phone\n"
						+ "Attend church +PeaceAndSerenity +FamilialPeace\n"
						+ "x 2012-11-09 Talk to boss about a raise +RetireEarly @Office\n"
						+ "Find a new job +RetireEarly\n"
						+ "(D) Call Dad +FamilialPeace @Phone\n"
						+ "Sort this todo list! @Internet +PeaceAndSerenity",
				Util.threeWayMerge(
						"(A) Sell VTX +RetireEarly @Internet\n"
								+ "Buy energy-efficient bulbs +RetireEarly\n"
								+ "(B) Mark 'War and Peace' as read @Internet +Reading\n"
								+ "Customize desktop @Internet +PeaceAndSerenity\n"
								+ "Talk to Grandpa @Phone\n"
								+ "Call Mom +FamilialPeace @Phone\n"
								+ "Attend church +PeaceAndSerenity\n"
								+ "Talk to boss about a raise +RetireEarly @Office",
						"x 2012-11-09 Sell VTX +RetireEarly @Internet\n"
								+ "x 2012-11-09 Buy energy-efficient bulbs +RetireEarly\n"
								+ "(K) Mark 'War and Peace' as read @Internet +Reading\n"
								+ "Customize desktop @Internet +PeaceAndSerenity\n"
								+ "Talk to Grandpa @Phone\n"
								+ "x 2012-11-09 Call Mom +FamilialPeace @Phone\n"
								+ "Attend church +PeaceAndSerenity +FamilialPeace\n"
								+ "x 2012-11-09 Talk to boss about a raise +RetireEarly @Office\n"
								+ "Find a new job +RetireEarly",
						"(A) Sell VTX +RetireEarly @Internet\n"
								+ "Buy energy-efficient bulbs +RetireEarly\n"
								+ "(B) Mark 'War and Peace' as read @Internet +Reading\n"
								+ "(E) Customize desktop @Internet +PeaceAndSerenity\n"
								+ "Talk to Grandpa @Phone\n"
								+ "x 2012-11-09 Call Mom +FamilialPeace @Phone\n"
								+ "Attend church +PeaceAndSerenity\n"
								+ "Talk to boss about a raise +RetireEarly @Office\n"
								+ "(D) Call Dad +FamilialPeace @Phone\n"
								+ "Sort this todo list! @Internet +PeaceAndSerenity"));
	}

	public void testExceptions() {
		try {
			Util.threeWayMerge(
					"Nonsense merge\n"
							+ "Totally ridiculous\n"
							+ "Nothing in common\n",
					"76 Trombones\n"
							+ "Another line",
					"Here's a line\n"
							+ "There's a line\n"
							+ "Everywhere a line, line");
			assertTrue("Exception wasn't thrown.", false);
		} catch (RemoteConflictException r) {
			assertTrue("Exception correctly thrown.", true);
		}
		try {
			Util.threeWayMerge(
					"Merge with items deleted from both local and remote\n"
							+ "tf2DRHOTM9TIgLeH\n"
							+ "l1Nr1aWRl1H2HQ3h\n"
							+ "pqgXlZSUjLhtIneb\n"
							+ "NON2YUvQm4GhGAmr\n"
							+ "ffJpa0rCNG0X2AnH\n"
							+ "a8I3fhR2XIrItA51\n"
							+ "AIQGzAIJuMbNqIJM\n"
							+ "wpyae0L7qXubJljE\n"
							+ "CgNngqhS9ojmXDVa\n"
							+ "QjAgX39aS6Wb5iHH\n"
							+ "tHxKolII8jlqRw12\n"
							+ "LzXI1GaMvWyxjpfl\n"
							+ "gTTmOaWGRfPoUCgu\n"
							+ "JcKThgaQtVWv8HF6\n"
							+ "GE40SmAXQQWK6y5W\n"
							+ "m9Qg8w0TVlFTtCJS\n"
							+ "OPt7DMMMaceH7yKu\n"
							+ "kpiL5STnZS6MvRpW\n"
							+ "ms9ZNNnGMFiEF00p\n"
							+ "RvhdO2qVkDYjTT0K\n"
							+ "vMKK9UBfhkOaz8Wk\n",
					"Merge with items deleted from both local and remote\n"
							+ "tf2DRHOTM9TIgLeH\n"
							+ "l1Nr1aWRl1H2HQ3h\n"
							+ "pqgXlZSUjLhtIneb\n"
							+ "NON2YUvQm4GhGAmr\n"
							+ "a8I3fhR2XIrItA51\n"
							+ "AIQGzAIJuMbNqIJM\n"
							+ "wpyae0L7qXubJljE\n"
							+ "CgNngqhS9ojmXDVa\n"
							+ "QjAgX39aS6Wb5iHH\n"
							+ "gTTmOaWGRfPoUCgu\n"
							+ "JcKThgaQtVWv8HF6\n"
							+ "GE40SmAXQQWK6y5W\n"
							+ "RvhdO2qVkDYjTT0K\n"
							+ "vMKK9UBfhkOaz8Wk\n",
					"Merge with items deleted from both local and remote\n"
							+ "l1Nr1aWRl1H2HQ3h\n"
							+ "NON2YUvQm4GhGAmr\n"
							+ "ffJpa0rCNG0X2AnH\n"
							+ "a8I3fhR2XIrItA51\n"
							+ "QjAgX39aS6Wb5iHH\n"
							+ "tHxKolII8jlqRw12\n"
							+ "LzXI1GaMvWyxjpfl\n"
							+ "gTTmOaWGRfPoUCgu\n"
							+ "JcKThgaQtVWv8HF6\n"
							+ "GE40SmAXQQWK6y5W\n"
							+ "ms9ZNNnGMFiEF00p\n"
							+ "RvhdO2qVkDYjTT0K\n"
							+ "vMKK9UBfhkOaz8Wk\n");
			assertTrue("Exception wasn't thrown.", false);
		} catch (RemoteConflictException r) {
			assertTrue("Exception correctly thrown.", true);
		}
	}
}
