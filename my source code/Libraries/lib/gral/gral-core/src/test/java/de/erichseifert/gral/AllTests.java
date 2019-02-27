/*
 * GRAL: GRAphing Library for Java(R)
 *
 * (C) Copyright 2009-2018 Erich Seifert <dev[at]erichseifert.de>,
 * Michael Seifert <mseifert[at]error-reports.org>
 *
 * This file is part of GRAL.
 *
 * GRAL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GRAL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GRAL.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.erichseifert.gral;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.erichseifert.gral.data.DataTests;
import de.erichseifert.gral.graphics.GraphicsTests;
import de.erichseifert.gral.io.IoTests;
import de.erichseifert.gral.navigation.NavigationTests;
import de.erichseifert.gral.plots.PlotsTests;
import de.erichseifert.gral.ui.UiTests;
import de.erichseifert.gral.util.UtilTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestUtilsTest.class,
	UtilTests.class,
	DataTests.class,
	GraphicsTests.class,
	NavigationTests.class,
	PlotsTests.class,
	IoTests.class,
	UiTests.class
})
public class AllTests {
}