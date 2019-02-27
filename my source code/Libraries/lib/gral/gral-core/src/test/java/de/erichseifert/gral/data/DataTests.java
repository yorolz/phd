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
package de.erichseifert.gral.data;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.erichseifert.gral.data.comparators.ComparatorTest;
import de.erichseifert.gral.data.filters.FiltersTests;
import de.erichseifert.gral.data.statistics.StatisticsTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// Tests for classes
	AbstractDataSourceTest.class,
	DataTableTest.class,
	DataSeriesTest.class,
	RowSubsetTest.class,
	EnumeratedDataTest.class,
	DummyDataTest.class,
	RowTest.class,
	RecordTest.class,
	ColumnTest.class,
	JdbcDataTest.class,
	// Tests for sub-packages
	ComparatorTest.class,
	StatisticsTests.class,
	FiltersTests.class
})
public class DataTests {
}
