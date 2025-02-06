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
package org.sonar.application;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class CodeQualityCheckerTest {

    @Test
    public void testIsLineTooLong() {
        assertThat(CodeQualityChecker.isLineTooLong("Short line")).isFalse();
        assertThat(CodeQualityChecker.isLineTooLong("A".repeat(80))).isFalse();
        assertThat(CodeQualityChecker.isLineTooLong("A".repeat(81))).isTrue();
        assertThat(CodeQualityChecker.isLineTooLong(null)).isFalse();
    }

    @Test
    public void testIsLineTooLongWithCustomLimit() {
        assertThat(CodeQualityChecker.isLineTooLong("Custom limit", 100)).isFalse();
        assertThat(CodeQualityChecker.isLineTooLong("A".repeat(101), 100)).isTrue();
    }

    @Test
    public void testIsMethodNameTooLong() {
        assertThat(CodeQualityChecker.isMethodNameTooLong("shortMethod")).isFalse();
        assertThat(CodeQualityChecker.isMethodNameTooLong("A".repeat(30))).isFalse();
        assertThat(CodeQualityChecker.isMethodNameTooLong("A".repeat(31))).isTrue();
        assertThat(CodeQualityChecker.isMethodNameTooLong(null)).isFalse();
    }

    @Test
    public void testIsMethodNameTooLongWithCustomLimit() {
        assertThat(CodeQualityChecker.isMethodNameTooLong("shortMethod", 50)).isFalse();
        assertThat(CodeQualityChecker.isMethodNameTooLong("A".repeat(51), 50)).isTrue();
    }
}