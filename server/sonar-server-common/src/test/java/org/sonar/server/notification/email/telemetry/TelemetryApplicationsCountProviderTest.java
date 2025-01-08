/*
 * SonarQube
 * Copyright (C) 2009-2025 SonarSource SA
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
package org.sonar.server.notification.email.telemetry;

import org.junit.jupiter.api.Test;
import org.sonar.db.DbClient;
import org.sonar.db.project.ProjectDao;
import org.sonar.telemetry.core.Dimension;
import org.sonar.telemetry.core.Granularity;
import org.sonar.telemetry.core.TelemetryDataType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TelemetryApplicationsCountProviderTest {
  private final DbClient dbClient = mock(DbClient.class);

  @Test
  void testGetters() {
    TelemetryApplicationsCountProvider underTest = new TelemetryApplicationsCountProvider(dbClient);
    ProjectDao projectDao = mock(ProjectDao.class);
    when(dbClient.projectDao()).thenReturn(projectDao);
    when(projectDao.countApplications(any())).thenReturn(42);
    assertThat(underTest.getMetricKey()).isEqualTo("applications_total_count");
    assertThat(underTest.getDimension()).isEqualTo(Dimension.INSTALLATION);
    assertThat(underTest.getGranularity()).isEqualTo(Granularity.ADHOC);
    assertThat(underTest.getType()).isEqualTo(TelemetryDataType.INTEGER);
    assertThat(underTest.getValue()).contains(42);
  }

}
