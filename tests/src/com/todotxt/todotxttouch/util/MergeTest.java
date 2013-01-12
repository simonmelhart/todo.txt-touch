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
						"(B) Sell VTX +RetireEarly @Internet\n"
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
					"(A) Sell VTX +RetireEarly @Internet\n"
							+ "Buy energy-efficient bulbs +RetireEarly\n"
							+ "(B) Mark 'War and Peace' as read @Internet +Reading\n"
							+ "Customize desktop @Internet +PeaceAndSerenity\n"
							+ "Talk to Grandpa @Phone\n"
							+ "Call Mom +FamilialPeace @Phone\n"
							+ "Attend church +PeaceAndSerenity\n"
							+ "Talk to boss about a raise +RetireEarly @Office",
					"2012-11-05 New skin for SCW https://github.com/anod/SCW-Skins/ 5-someday @internet\n"
							+ "Show CasaKids furniture to Steph @home\n"
							+ "Wax out of carpet @home\n"
							+ "Craigslist: Bike trailer @internet\n"
							+ "R/C radio on Craigslist @internet\n"
							+ "Get more rings before answering on Steph's phone? call 611 from phone @home\n"
							+ "New propane tank +EmergencyPrep @internet\n"
							+ "Mow lawn @home\n"
							+ "Review and stock 72 hour kit +EmergencyPrep @home\n"
							+ "Susan wait for me story +journal @office\n"
							+ "Small premium phone? @internet\n"
							+ "Write a Choosing Simplicity interview/description about me @anywhere 5-someday\n"
							+ "Other apps for reading on Nook? 5-someday @internet\n"
							+ "E-mail Rachel after GA 6-waiting @internet\n"
							+ "Make a mobile for Molly @home\n"
							+ "(C) Grant not in our ward anymore. Call him. @phone\n"
							+ "Talk to parents about picking up their gifts @phone\n"
							+ "Strip and Season cast iron @home 5-someday\n"
							+ "Hot glue on slipper laces @home\n"
							+ "First sync fails +todotxt\n"
							+ "Get Swype smaller @internet\n"
							+ "Journal: forgotten carols rehearsal, Michelle @office\n"
							+ "Journal: Visio goodbye meeting @office\n"
							+ "x 2012-11-14 2012-11-10 Battery door cover that's rubberized @internet\n"
							+ "@office Susan meltdown +journal Sunday, 11 Nov\n"
							+ "Add deletion tests into merge +todotxt\n"
							+ "Freshen water +EmergencyPrep @home\n"
							+ "Molly kit in the car +EmergencyPrep @home\n"
							+ "Research kerosene heater +EmergencyPrep @internet\n"
							+ "Ask Max: Imputed income from pro club? @office\n"
							+ "Look up dumbbell prices online vs local 10 lb (Amazon: $30) @shopping\n"
							+ "Completed task toast had new name, should have old one +todotxt\n"
							+ "Call Ken Hampton for appt @phone\n"
							+ "Exception for file merge if the merged file is 50% longer than either source file +todotxt\n"
							+ "Photos from cloud to phone\n",
					"2012-11-05 New skin for SCW https://github.com/anod/SCW-Skins/ 5-someday @internet\n"
							+ "Show CasaKids furniture to Steph @home\n"
							+ "Wax out of carpet @home\n"
							+ "Craigslist: Bike trailer @internet\n"
							+ "R/C radio on Craigslist @internet\n"
							+ "Get more rings before answering on Steph's phone? call 611 from phone @home\n"
							+ "Write a Choosing Simplicity interview/description about me @anywhere 5-someday\n"
							+ "Other apps for reading on Nook? 5-someday @internet\n"
							+ "E-mail Rachel after GA 6-waiting @internet\n"
							+ "Make a mobile for Molly @home\n"
							+ "(C) Grant not in our ward anymore. Call him. @phone\n"
							+ "Talk to parents about picking up their gifts @phone\n"
							+ "Strip and Season cast iron @home 5-someday\n"
							+ "Hot glue on slipper laces @home\n"
							+ "Add deletion tests into merge +todotxt\n"
							+ "Freshen water +EmergencyPrep @home\n"
							+ "Review and stock 72 hour kit +EmergencyPrep @home\n"
							+ "Molly kit in the car +EmergencyPrep @home\n"
							+ "Research kerosene heater +EmergencyPrep @internet\n"
							+ "New propane tank +EmergencyPrep @internet\n"
							+ "Mow lawn @home\n"
							+ "Susan wait for me story +journal @office\n"
							+ "First sync fails +todotxt\n"
							+ "Get Swype smaller @internet\n"
							+ "Journal: forgotten carols rehearsal, Michelle @office\n"
							+ "Journal: Visio goodbye meeting @office\n"
							+ "x 2012-11-14 2012-11-10 Battery door cover that's rubberized @internet\n"
							+ "@office Susan meltdown +journal Sunday, 11 Nov\n"
							+ "Small premium phone? @internet\n"
							+ "Ask Max: Imputed income from pro club? @office\n"
							+ "Look up dumbbell prices online vs local 10 lb (Amazon: $30) @shopping\n"
							+ "Completed task toast had new name, should have old one +todotxt\n"
							+ "Call Ken Hampton for appt @phone\n"
							+ "Exception for file merge if the merged file is 50% longer than either source file +todotxt\n"
							+ "Photos from cloud to phone\n");
			assertTrue("Exception wasn't thrown.", false);
		} catch (RemoteConflictException r) {
			assertTrue("Exception correctly thrown.", true);
		}
	}
}
