/*
 * SonarQube
 * Copyright (C) 2009-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.platform.db.migration.version.v85;

import java.sql.SQLException;
import java.sql.Types;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.db.CoreDbTester;
import org.sonar.server.platform.db.migration.step.MigrationStep;

public class DropOrganizationFromDefaultQProfilesTest {
  @Rule
  public CoreDbTester db = CoreDbTester.createForSchema(DropOrganizationFromDefaultQProfilesTest.class, "schema.sql");

  private MigrationStep underTest = new DropOrganizationFromDefaultQProfiles(db.database());

  @Test
  public void execute() throws SQLException {
    db.assertColumnDefinition("default_qprofiles", "organization_uuid", Types.VARCHAR, 40, false);
    underTest.execute();
    db.assertColumnDoesNotExist("default_qprofiles", "organization_uuid");
  }
}
