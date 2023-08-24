/*
 * SonarQube
 * Copyright (C) 2009-2023 SonarSource SA
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
package org.sonar.server.platform.db.migration.version.v102;

import java.sql.SQLException;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.db.CoreDbTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class UpdateValueAndPopulatePreviousNonCompliantValueInNewCodePeriodsTest {

  private static final String TABLE_NAME = "new_code_periods";
  private static final String PROJECT_UUID = "project-uuid";
  private static final String BRANCH_UUID = "branch-uuid";

  @Rule
  public final CoreDbTester db = CoreDbTester.createForSchema(UpdateValueAndPopulatePreviousNonCompliantValueInNewCodePeriodsTest.class, "schema.sql");

  public final UpdateValueAndPopulatePreviousNonCompliantValueInNewCodePeriods underTest = new UpdateValueAndPopulatePreviousNonCompliantValueInNewCodePeriods(db.database());

  @Test
  public void execute_whenSnapshotsExist_shouldPopulatePurgedColumn() throws SQLException {
    insertNewCodePeriods("uuid-1", PROJECT_UUID, BRANCH_UUID, "PREVIOUS_VERSION", null);
    insertNewCodePeriods("uuid-2", PROJECT_UUID, null, "NUMBER_OF_DAYS", "90");
    insertNewCodePeriods("uuid-3", null, null, "NUMBER_OF_DAYS", "97");
    insertNewCodePeriods("uuid-4", null, null, "NUMBER_OF_DAYS", "135");


    underTest.execute();

    assertThat(db.select("select UUID, VALUE, PREVIOUS_NON_COMPLIANT_VALUE from new_code_periods"))
      .extracting(stringObjectMap -> stringObjectMap.get("UUID"), stringObjectMap -> stringObjectMap.get("VALUE"),
        stringObjectMap -> stringObjectMap.get("PREVIOUS_NON_COMPLIANT_VALUE"))
      .containsExactlyInAnyOrder(
        tuple("uuid-1", null, null),
        tuple("uuid-2", "90", null),
        tuple("uuid-3", "90", "97"),
        tuple("uuid-4", "90", "135"));
  }

  private void insertNewCodePeriods(String uuid, @Nullable String projectUuid, @Nullable String branchUuid, String type, String value) {
    db.executeInsert(TABLE_NAME,
      "UUID", uuid,
      "PROJECT_UUID", projectUuid,
      "BRANCH_UUID", branchUuid,
      "TYPE", type,
      "VALUE", value,
      "UPDATED_AT", System.currentTimeMillis(),
      "CREATED_AT", System.currentTimeMillis());
  }

}

